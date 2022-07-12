package com.volla.bootmanager.legacy.ui.addrom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.volla.bootmanager.R;
import com.volla.bootmanager.legacy.roms.ROMsList;
import com.volla.bootmanager.legacy.ui.roms.ROMFragment;
import com.volla.bootmanager.legacy.ui.wizard.WizardViewModel;

import java.util.Objects;

public class UpROMWelcomeWizardPageFragment extends Fragment {
    protected WizardViewModel model;
    protected AddROMViewModel imodel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        imodel = new ViewModelProvider(requireActivity()).get(AddROMViewModel.class);
        model.setPositiveFragment(AddROMCheckWizardPageFragment.class);
        model.setNegativeFragment(null);
        model.setPositiveAction(null);
        model.setNegativeAction(() -> requireActivity().finish());
        model.setPositiveText(getString(R.string.next));
        model.setNegativeText(getString(R.string.cancel));
        ROMsList l = new ROMsList(model.getCodename().getValue(), requireContext());
        ROMFragment.Entry e = ROMFragment.current;
        if (e.config.get("xsystem") != null)
            Objects.requireNonNull(imodel.getParts().getValue()).add(Integer.parseInt(e.config.get("xsystem")));
        if (e.config.get("xdata") != null)
            Objects.requireNonNull(imodel.getParts().getValue()).add(Integer.parseInt(e.config.get("xdata")));
        if (e.config.get("xtype").equals("UT"))
            imodel.setROM(l.getROMs().stream().filter(r -> r.scriptname.equals("add_ubuntutouch_systemimage_haliumboot.sh")).findFirst().get());
        else if (e.config.get("xtype").equals("SFOS"))
            imodel.setROM(l.getROMs().stream().filter(r -> r.scriptname.equals("add_sailfish.sh")).findFirst().get());
        else
            imodel.setROM(l.getROMs().stream().filter(r -> r.scriptname.equals("other_os.sh")).findFirst().get());
        imodel.getName().add(e.config.get("title"));
        imodel.getName().add(e.file.replace("/data/abm/bootset/lk2nd/entries/","").replace(".conf",""));
        Objects.requireNonNull(imodel.getROM().getValue()).parts.clear();
        Objects.requireNonNull(imodel.getROM().getValue()).strings.clear();
        Objects.requireNonNull(imodel.getROM().getValue()).fullPath = "FORMATDATA=false " + imodel.getROM().getValue().fullPath;
        final View root = inflater.inflate(R.layout.wizard_installer_welcome, container, false);
        final TextView message = root.findViewById(R.id.wizard_installer_welcome_txt);
        message.setText(R.string.kernel_update_msg);
        return root;
    }
}
