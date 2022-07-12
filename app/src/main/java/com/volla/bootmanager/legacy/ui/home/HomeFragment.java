package com.volla.bootmanager.legacy.ui.home;

import static com.volla.bootmanager.legacy.util.SDUtils.generateMeta;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import com.volla.bootmanager.R;
import com.volla.bootmanager.legacy.devices.DeviceList;
import com.volla.bootmanager.legacy.util.SDUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends Fragment {

    InstalledViewModel model;

    public boolean hasDualBootBootloader() {
        boolean hasABM = false;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method getBoolean = c.getMethod("getBoolean", String.class, boolean.class);

            hasABM = (boolean) getBoolean.invoke(c, "ro.boot.has_dualboot", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasABM;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(InstalledViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        TextView statusText0 = root.findViewById(R.id.home_installedWorking_supported);
        TextView statusText1 = root.findViewById(R.id.home_installedWorking_sdcard);
        TextView statusText2 = root.findViewById(R.id.home_installedWorking_meta);

        MaterialButton installButton = root.findViewById(R.id.home_installButton);
        AtomicBoolean bootloader = new AtomicBoolean(false);
        AtomicBoolean sd = new AtomicBoolean(false);
        AtomicBoolean metacheck = new AtomicBoolean(false);
        AtomicBoolean checkCodename = new AtomicBoolean(false);

        ImageView statusImg = root.findViewById(R.id.home_installedWorking_image);
        bootloader.set(hasDualBootBootloader()); // Root
        checkCodename.set(SuFile.open("/data/abm/codename.cfg").exists());
        if (checkCodename.get()) {
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
            if (meta.get() != null && meta.get().p.size() > 0 && meta.get().p.get(0).code.equals("8301"))
                metacheck.set(true);
        }

        installButton.setOnClickListener((v) -> Navigation.findNavController(v).navigate(R.id.nav_sd));
        installButton.setVisibility((bootloader.get() && sd.get() && !metacheck.get()) ? View.VISIBLE : View.INVISIBLE);

        ((NavigationView) requireActivity().findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_sd).setEnabled(bootloader.get() && sd.get());
        ((NavigationView) requireActivity().findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_roms).setEnabled(bootloader.get() && sd.get() && metacheck.get());

        statusImg.setImageDrawable(ContextCompat.getDrawable(requireActivity(), bootloader.get() && sd.get() && metacheck.get() ? R.drawable.ic_ok : R.drawable.ic_no));
        statusText0.setText(bootloader.get() ? R.string.ok : R.string.failure);
        statusText1.setText(sd.get() ? R.string.ok : R.string.failure);
        statusText2.setText(metacheck.get() ? R.string.ok : R.string.failure);

        return root;
    }
}