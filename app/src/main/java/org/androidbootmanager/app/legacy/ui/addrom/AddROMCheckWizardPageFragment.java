package org.androidbootmanager.app.legacy.ui.addrom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.androidbootmanager.app.R;
import org.androidbootmanager.app.legacy.ui.wizard.WizardViewModel;

public class AddROMCheckWizardPageFragment extends Fragment {
    protected WizardViewModel model;
    protected AddROMViewModel imodel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        imodel = new ViewModelProvider(requireActivity()).get(AddROMViewModel.class);
        model.setNegativeFragment(null);
        model.setPositiveAction(null);
        model.setNegativeAction(() -> requireActivity().finish());
        model.setPositiveText(getString(R.string.next));
        model.setNegativeText(getString(R.string.cancel));
        switch (imodel.getROM().getValue().type) {
            case UBUNTU:
                model.setPositiveFragment(DownloadROMInstallerWizardPageFragment.class);
                break;
            default:
                model.setPositiveFragment(DeviceROMInstallerWizardPageFragment.class);
                break;
        }
        final View root = inflater.inflate(R.layout.wizard_installer_check, container, false);
        return root;
    }

}
