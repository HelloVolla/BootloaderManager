package com.volla.bootmanager.legacy.ui.updatelk;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.topjohnwu.superuser.Shell;

import com.volla.bootmanager.R;
import com.volla.bootmanager.legacy.ui.wizard.WizardViewModel;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class DoUpdateWizardPageFragment extends Fragment {
    protected WizardViewModel model;
    final Handler handler = new Handler();

    @SuppressLint("SdCardPath")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);
        model.setPositiveFragment(null);
        model.setNegativeFragment(null);
        model.setPositiveAction(() -> {});
        model.setNegativeAction(() -> {});
        model.setPositiveText("");
        model.setNegativeText("");
        final View root = inflater.inflate(R.layout.wizard_installer_do, container, false);
        final TextView log = root.findViewById(R.id.wizard_installer_do_log);
        final LinkedList<String> queue = new LinkedList<>();
        AtomicBoolean hdone = new AtomicBoolean(false);
        Shell.sh("/data/data/com.volla.bootmanager/assets/Scripts/update/" + model.getCodename().getValue() + ".droid.sh").to(queue).submit((out)-> hdone.set(true));
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!queue.isEmpty()) log.append("\n" + queue.poll());
                if (!queue.isEmpty()) handler.post(this); else if (!hdone.get()) handler.postDelayed(this, 50); else if (hdone.get())  {
                    model.setPositiveText(getString(R.string.ok));
                    model.setPositiveAction(() -> requireActivity().finish());
                }
            }
        });
        return root;
    }
}
