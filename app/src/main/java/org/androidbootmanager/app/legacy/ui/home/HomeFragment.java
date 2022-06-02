package org.androidbootmanager.app.legacy.ui.home;

import static org.androidbootmanager.app.legacy.util.SDUtils.generateMeta;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import org.androidbootmanager.app.R;
import org.androidbootmanager.app.legacy.devices.DeviceList;
import org.androidbootmanager.app.legacy.ui.activities.MainActivity;
import org.androidbootmanager.app.legacy.ui.installer.InstallerWelcomeWizardPageFragment;
import org.androidbootmanager.app.legacy.ui.wizard.WizardActivity;
import org.androidbootmanager.app.legacy.util.Constants;
import org.androidbootmanager.app.legacy.util.SDUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends Fragment {

    InstalledViewModel model;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(InstalledViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        TextView statusText0 = root.findViewById(R.id.home_installedWorking_superUser);
        TextView statusText1 = root.findViewById(R.id.home_installedWorking_install1);
        TextView statusText2 = root.findViewById(R.id.home_installedWorking_install2);
        TextView statusText3 = root.findViewById(R.id.home_installedWorking_install3);
        MaterialButton installButton = root.findViewById(R.id.home_installButton);
        AtomicBoolean check0 = new AtomicBoolean(false);
        AtomicBoolean check1 = new AtomicBoolean(false);
        AtomicBoolean check2 = new AtomicBoolean(false);
        AtomicBoolean check3 = new AtomicBoolean(false);
        AtomicBoolean sd = new AtomicBoolean(false);
        ImageView statusImg = root.findViewById(R.id.home_installedWorking_image);
            check0.set(true); // Root
            statusText0.setText(check0.get() ? R.string.ok : R.string.failure);
            check1.set(true); // Script pass
            statusText1.setText(check1.get() ? R.string.ok : R.string.failure);
            check2.set(true); // Has ABM bootloader
            statusText2.setText(check2.get() ? R.string.ok : R.string.failure);
            check3.set(SuFile.open("/data/abm/codename.cfg").exists());
            statusText3.setText(check3.get() ? R.string.ok : R.string.failure);
            statusImg.setImageDrawable(ContextCompat.getDrawable(requireActivity(),check1.get() && check2.get() && check3.get() ? R.drawable.ic_ok : R.drawable.ic_no));
            installButton.setVisibility((!(check1.get() && check2.get()) && (!check3.get())) && check0.get() ? View.VISIBLE : View.INVISIBLE);
            if (check3.get()) {
                try {
                    byte[] buf = new byte[100];
                    int len;
                    ByteArrayOutputStream s = new ByteArrayOutputStream();
                    InputStream i = SuFileInputStream.open("/data/abm/codename.cfg");
                    while ((len = i.read(buf)) > 0)
                        s.write(buf, 0, len);
                    model.setCodename(s.toString("UTF-8").replace("\n", ""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (DeviceList.deviceList.contains(Build.DEVICE)) {
                    model.setCodename(Build.DEVICE);
                } else if (DeviceList.bspList.containsKey(Build.DEVICE) && DeviceList.bspList.get(Build.DEVICE).size() == 1) {
                    model.setCodename(DeviceList.bspList.get(Build.DEVICE).get(0));
                }
            }
            if (!model.getCodename().getValue().isEmpty())
                sd.set(SuFile.open(DeviceList.getModel(model).bdev).exists());

            if (sd.get()) {
                AtomicReference<SDUtils.SDPartitionMeta> meta = new AtomicReference<>(generateMeta(DeviceList.getModel(model)));
                if (!meta.get().p.get(0).code.equals("8301"))
                    Toast.makeText(requireActivity(), "Bad metadata", Toast.LENGTH_LONG).show();
                else
                    check3.set(true);
            }
            installButton.setOnClickListener((v) -> startActivity(new Intent(requireActivity(), WizardActivity.class).putExtra("StartFragment", InstallerWelcomeWizardPageFragment.class)));
            ((NavigationView) requireActivity().findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_roms).setEnabled(check2.get() && check3.get() && sd.get());
            ((NavigationView) requireActivity().findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_sd).setEnabled(check2.get() && sd.get());
        return root;
    }
}