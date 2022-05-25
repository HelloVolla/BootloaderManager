package org.androidbootmanager.app.legacy.ui.addrom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.androidbootmanager.app.R;
import org.androidbootmanager.app.legacy.devices.DeviceList;
import org.androidbootmanager.app.legacy.ui.wizard.WizardViewModel;
import org.androidbootmanager.app.legacy.util.SDUtils;

import java.io.File;

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
        model.setNegativeText(getString(R.string.cancel));
        final View root = inflater.inflate(R.layout.wizard_installer_check, container, false);
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

        ImageView statusImg = root.findViewById(R.id.check_logo);
        statusImg.setImageDrawable(ContextCompat.getDrawable(requireActivity(), finalCheck ? R.drawable.ic_ok : R.drawable.ic_no));

        if (finalCheck) {
            model.setPositiveText(getString(R.string.next));
            switch (imodel.getROM().getValue().type) {
                case UBUNTU:
                    model.setPositiveFragment(DownloadROMInstallerWizardPageFragment.class);
                    break;
                default:
                    model.setPositiveFragment(DeviceROMInstallerWizardPageFragment.class);
                    break;
            }
        } else {
            model.setPositiveText("");
            model.setPositiveFragment(null);
        }

        return root;
    }

}
