package org.androidbootmanager.app.legacy.ui.addrom;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.androidbootmanager.app.R;
import org.androidbootmanager.app.legacy.ui.wizard.WizardViewModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DownloadROMInstallerWizardPageFragment extends Fragment {
    protected ProgressBar progressBar;
    protected TextView log;

    private enum ProgressType {
        INTEGER,
        REMOVE_LASTTEXT,
        TEXT
    }

    private class Progress {
        ProgressType type;
        String text;
        Integer percent;

        public Progress(String text) {
            this.type = ProgressType.TEXT;
            this.text = text;
            this.percent = 0;
        }

        public Progress(Integer percent) {
            this.type = ProgressType.INTEGER;
            this.text = "";
            this.percent = percent;
        }

        public Progress() {
            this.type = ProgressType.REMOVE_LASTTEXT;
            this.text = "";
            this.percent = 0;
        }

        public ProgressType getType() {
            return type;
        }

        public void setType(ProgressType type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getPercent() {
            return percent;
        }

        public void setPercent(Integer percent) {
            this.percent = percent;
        }
    }

    private boolean moveToCache(String image) {
        try (Stream<Path> paths = Files.walk(Paths.get("/data/data/org.androidbootmanager.app/cache/online"))) {
            Path path = paths.filter(p -> p.getFileName().toString().equals(image))
                    .findFirst().orElse(null);
            if (path == null) {
                return false;
            } else {
                Files.move(path, Paths.get("/data/data/org.androidbootmanager.app/cache/" + image), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private class DownloadTask extends AsyncTask<String, Progress, Integer> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    publishProgress(new Progress("Download error: " + "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage()));
                    return -1;
                }

                int fileLength = connection.getContentLength();

                publishProgress(new Progress("Downloading images archive, size: " + FileUtils.byteCountToDisplaySize(fileLength)));

                input = connection.getInputStream();
                output = new FileOutputStream("/data/data/org.androidbootmanager.app/cache/online/rom.zip");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                progressBar.setIndeterminate(false);
                int lastPercent = 0;
                publishProgress(new Progress("(0%) Downloaded"));
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return -1;
                    }
                    total += count;

                    if (fileLength > 0) {
                        int percent = (int)(total * 100 / fileLength);
                        if (percent > lastPercent) {
                            publishProgress(new Progress(percent / 2));
                            publishProgress(new Progress());
                            publishProgress(new Progress(FileUtils.byteCountToDisplaySize(total) + " (" + percent + "%) Downloaded"));
                        }
                        lastPercent = percent;
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                publishProgress(new Progress("Download error: " + e.toString()));
                return -1;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            publishProgress(new Progress("Images archive downloaded"));

            publishProgress(new Progress(("Extracting archive")));
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile("/data/data/org.androidbootmanager.app/cache/online/rom.zip");
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress(new Progress("Failed to get images archive"));
                return -1;
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int counter = 0;
            int totalentries = zipFile.size();
            while (entries.hasMoreElements()) {
                counter++;
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File("/data/data/org.androidbootmanager.app/cache/online", entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream(entryDestination);
                        IOUtils.copy(zipFile.getInputStream(entry), outStream);
                        outStream.close();
                        publishProgress(new Progress(50 + (int) (counter * 50 / totalentries)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        publishProgress(new Progress("Archive entry not found"));
                        return -1;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return -1;
                    }
                }
            }

            if (Objects.requireNonNull(imodel.getROM().getValue()).requiredFiles.size() > 0) {
                List<String> requiredFiles = imodel.getROM().getValue().requiredFiles.keySet().stream().collect(Collectors.toList());
                requiredFiles.forEach(key -> {
                    if (moveToCache(key)) {
                        publishProgress(new Progress("Moved " + key + " to cache"));
                        imodel.getROM().getValue().requiredFiles.remove(key);
                    } else {
                        publishProgress(new Progress(key + " not found in archive"));
                    }
                });
            }
            if (imodel.getROM().getValue().flashes.size() > 0) {
                imodel.getROM().getValue().flashes.keySet().stream().forEach(key -> {
                    if (moveToCache(key))
                        publishProgress(new Progress("Moved " + key + " to cache"));
                    else
                        publishProgress(new Progress(key + " not found in archive"));
                });
            }

            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Progress... progress) {
            super.onProgressUpdate(progress);
            if (progress[0].getType() == ProgressType.INTEGER)
                progressBar.setProgress(progress[0].getPercent());
            if (progress[0].getType() == ProgressType.TEXT)
                log.append(progress[0].getText() + "\n");
            if (progress[0].getType() == ProgressType.REMOVE_LASTTEXT) {
                String currentValue = log.getText().toString().trim();
                String newValue = currentValue.substring(0, currentValue.lastIndexOf("\n"));
                log.setText(newValue);
                log.append("\n");
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            mWakeLock.release();

            progressBar.setIndeterminate(false);
            if (result == 0) {
                progressBar.setProgress(100);
                log.append("Done successfully\n");
                model.setNegativeAction(() -> {
                    requireActivity().finish();
                });
                model.setPositiveText(getString(R.string.next));
                model.setPositiveFragment(DeviceROMInstallerWizardPageFragment.class);
            } else {
                log.append("Failed, check logs for details\n");
                model.setPositiveText(getString(R.string.manual));
                model.setPositiveFragment(DeviceROMInstallerWizardPageFragment.class);
            }
        }
    }

    private void prepareRomFiles() {
        File onlinedir = new File("/data/data/org.androidbootmanager.app/cache/online");
        try {
            FileUtils.deleteDirectory(onlinedir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        onlinedir.mkdirs();

        progressBar.setIndeterminate(true);
        progressBar.setMax(100);
        final DownloadTask downloadTask = new DownloadTask(requireContext());
        //downloadTask.execute("http://88.99.101.25:8080/job/mimameid-ubuntutouch/ws/stable-images.zip");
        downloadTask.execute("http://192.168.90.185/stable-images.zip");
        model.setNegativeAction(() -> {
            downloadTask.cancel(true);
            requireActivity().finish();
        });
    }

    protected WizardViewModel model;
    protected AddROMViewModel imodel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        imodel = new ViewModelProvider(requireActivity()).get(AddROMViewModel.class);
        model.setPositiveFragment(DeviceROMInstallerWizardPageFragment.class);
        model.setNegativeFragment(null);
        model.setPositiveAction(null);
        model.setNegativeAction(() -> requireActivity().finish());
        model.setPositiveText(getString(R.string.manual));
        model.setNegativeText(getString(R.string.cancel));
        final View root = inflater.inflate(R.layout.wizard_installer_download, container, false);
        final Button downloadBtn = root.findViewById(R.id.wizard_installer_download_btn);
        log = root.findViewById(R.id.wizard_installer_download_log);
        progressBar = root.findViewById(R.id.wizard_installer_progressBar);
        downloadBtn.setOnClickListener(v -> {
            model.setPositiveFragment(null);
            model.setPositiveText("");
            downloadBtn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            prepareRomFiles();
        });
        return root;
    }

}
