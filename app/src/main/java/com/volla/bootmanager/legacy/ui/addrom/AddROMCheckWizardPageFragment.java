package com.volla.bootmanager.legacy.ui.addrom;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.volla.bootmanager.R;
import com.volla.bootmanager.legacy.devices.DeviceList;
import com.volla.bootmanager.legacy.ui.update.UpdateBase;
import com.volla.bootmanager.legacy.ui.update.UpdateBaseInfo;
import com.volla.bootmanager.legacy.ui.wizard.WizardViewModel;
import com.volla.bootmanager.legacy.util.SDUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AddROMCheckWizardPageFragment extends Fragment {
    protected WizardViewModel model;
    protected AddROMViewModel imodel;

    private static UpdateBaseInfo parseJsonUpdate(JSONObject object) throws JSONException {
        UpdateBase update = new UpdateBase();
        update.setTimestamp(object.getLong("datetime"));
        update.setName(object.getString("filename"));
        update.setDownloadId(object.getString("id"));
        update.setType(object.getString("romtype"));
        update.setFileSize(object.getLong("size"));
        update.setDownloadUrl(object.getString("url"));
        update.setVersion(object.getString("version"));
        return update;
    }

    public static List<UpdateBaseInfo> parseJson(File file)
            throws IOException, JSONException {
        List<UpdateBaseInfo> updates = new ArrayList<>();

        StringBuilder json = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
                json.append(line);
            }
        }

        JSONObject obj = new JSONObject(json.toString());
        JSONArray updatesList = obj.getJSONArray("response");
        for (int i = 0; i < updatesList.length(); i++) {
            if (updatesList.isNull(i)) {
                continue;
            }
            try {
                UpdateBaseInfo update = parseJsonUpdate(updatesList.getJSONObject(i));
                updates.add(update);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return updates;
    }

    private void checkForOnline(Context context) {
        String serverUrl = context.getString(R.string.config_server_url);
        serverUrl = serverUrl.replace("{device}", model.getCodename().getValue())
                .replace("{type}", imodel.getROM().getValue().type.toString());
        String outJson = "/data/data/com.volla.bootmanager/cache/roms.json";

        try {
            Files.delete(Paths.get(outJson));
        } catch (IOException e) {
            e.printStackTrace();
        }

        URL url = null;
        try {
            url = new URL(serverUrl);
            try(InputStream inputStream = url.openStream()){
                Files.copy(inputStream, Paths.get(outJson));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            List<UpdateBaseInfo> updates = parseJson(new File(outJson));
            if (!updates.isEmpty()) {
                updates.sort((u1, u2) -> Long.compare(u2.getTimestamp(), u1.getTimestamp()));
                imodel.getROM().getValue().updates = updates;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        imodel = new ViewModelProvider(requireActivity()).get(AddROMViewModel.class);
        model.setNegativeFragment(null);
        model.setPositiveAction(null);
        model.setNegativeAction(() -> requireActivity().finish());
        model.setNegativeText(getString(R.string.cancel));
        model.setPositiveText("");
        final View root = inflater.inflate(R.layout.wizard_installer_check, container, false);
        final ProgressBar progressBar = root.findViewById(R.id.check_progressBar);
        final ImageView statusImg = root.findViewById(R.id.check_logo);

        new Thread(() -> {
            checkForOnline(requireActivity());

            requireActivity().runOnUiThread(() -> {
                Boolean finalCheck = true;
                long freeBytesInternal = new File(getContext().getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
                int neededSpace = 6; // 6 GB
                Boolean isEnoughFreeSpace = freeBytesInternal > neededSpace * 1024L * 1024L * 1024L;
                TextView internal = root.findViewById(R.id.check_internal);
                if (isEnoughFreeSpace)
                    internal.setText(R.string.ok);
                else
                    internal.setText(getString(R.string.internal_space_failed, neededSpace));
                finalCheck = finalCheck && isEnoughFreeSpace;

                Integer systems = imodel.getROM().getValue().flashes.size();
                if (systems > 0) {
                    final SDUtils.SDPartitionMeta meta = SDUtils.generateMeta(DeviceList.getModel(model));
                    Integer parts = 0;
                    for (SDUtils.Partition partition : meta.p) {
                        if (partition.code.equals("8305"))
                            parts++;
                    }
                    Boolean systemCheck = parts >= systems;
                    finalCheck = finalCheck && systemCheck;
                    LinearLayout keysLayout = root.findViewById(R.id.check_check_keys);
                    LinearLayout valuesLayout = root.findViewById(R.id.check_check_values);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    TextView keyText = new TextView(getContext());
                    TextView valueText = new TextView(getContext());
                    keyText.setText(R.string.system_part);
                    keysLayout.addView(keyText, layoutParams);

                    if (systemCheck) {
                        valueText.setText(R.string.ok);
                    } else {
                        valueText.setText(getString(R.string.partition_check_failed, systems));
                    }
                    valuesLayout.addView(valueText, layoutParams);
                }

                Integer users = imodel.getROM().getValue().parts.size();
                if (users > 0) {
                    final SDUtils.SDPartitionMeta meta = SDUtils.generateMeta(DeviceList.getModel(model));
                    Integer parts = 0;
                    for (SDUtils.Partition partition : meta.p) {
                        if (partition.code.equals("8302"))
                            parts++;
                    }
                    Boolean userCheck = parts >= users;
                    finalCheck = finalCheck && userCheck;
                    LinearLayout keysLayout = root.findViewById(R.id.check_check_keys);
                    LinearLayout valuesLayout = root.findViewById(R.id.check_check_values);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    TextView keyText = new TextView(getContext());
                    TextView valueText = new TextView(getContext());
                    keyText.setText(R.string.data_part);
                    keysLayout.addView(keyText, layoutParams);

                    if (userCheck) {
                        valueText.setText(R.string.ok);
                    } else {
                        valueText.setText(getString(R.string.partition_check_failed, users));
                    }
                    valuesLayout.addView(valueText, layoutParams);
                }

                if (finalCheck) {
                    model.setPositiveText(getString(R.string.next));
                    if (imodel.getROM().getValue().updates.isEmpty())
                        model.setPositiveFragment(DeviceROMInstallerWizardPageFragment.class);
                    else
                        model.setPositiveFragment(DownloadROMInstallerWizardPageFragment.class);
                } else {
                    model.setPositiveText("");
                    model.setPositiveFragment(null);
                }

                statusImg.setImageDrawable(ContextCompat.getDrawable(requireActivity(), finalCheck ? R.drawable.ic_ok : R.drawable.ic_no));
                progressBar.setVisibility(View.GONE);
                statusImg.setVisibility(View.VISIBLE);
            });
        }).start();

        return root;
    }

}
