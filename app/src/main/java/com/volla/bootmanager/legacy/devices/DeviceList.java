package com.volla.bootmanager.legacy.devices;

import com.volla.bootmanager.legacy.ui.home.InstalledViewModel;
import com.volla.bootmanager.legacy.ui.installer.DeviceInstallerViewModel;
import com.volla.bootmanager.legacy.ui.installer.DeviceInstallerWizardPageFragment;
import com.volla.bootmanager.legacy.ui.installer.DoInstallWizardPageFragment;
import com.volla.bootmanager.legacy.ui.installer.DroidBootSelectorWizardPageFragment;
import com.volla.bootmanager.legacy.ui.wizard.WizardViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DeviceList {
    public static final ArrayList<String> deviceList = new ArrayList<>();
    public static final HashMap<String, List<String>> bspList = new HashMap<>();
    static {
        deviceList.add("mimameid");
        deviceList.add("yggdrasil");
        deviceList.add("yggdrasilx");
        bspList.put("k63v2_64_bsp", Collections.singletonList("yggdrasil"));
        bspList.put("GS5", Collections.singletonList("mimameid"));
    }

    public static DeviceModel getModel(String codename) {
        DeviceModel d;
        //noinspection SwitchStatementWithTooFewBranches
        switch(codename) {
            case "yggdrasil":
                d = new DeviceModel();
                d.codename = "yggdrasil";
                d.viewname = "Volla Phone";
                d.bdev = "/dev/block/mmcblk1";
                d.pbdev = "/dev/block/mmcblk1p";
                d.spartsize = 7340031f;
                d.flow = Arrays.asList(DeviceInstallerWizardPageFragment.class, DroidBootSelectorWizardPageFragment.class, DoInstallWizardPageFragment.class);
                break;
            case "yggdrasilx":
                d = new DeviceModel();
                d.codename = "yggdrasilx";
                d.viewname = "Volla Phone X";
                d.bdev = "/dev/block/mmcblk1";
                d.pbdev = "/dev/block/mmcblk1p";
                d.spartsize = 9437184f;
                d.flow = Arrays.asList(DeviceInstallerWizardPageFragment.class, DroidBootSelectorWizardPageFragment.class, DoInstallWizardPageFragment.class);
                break;
            case "mimameid":
                d = new DeviceModel();
                d.codename = "mimameid";
                d.viewname = "Volla Phone 22";
                d.bdev = "/dev/block/mmcblk1";
                d.pbdev = "/dev/block/mmcblk1p";
                d.spartsize = 9437184f;
                d.flow = Arrays.asList(DeviceInstallerWizardPageFragment.class, DroidBootSelectorWizardPageFragment.class, DoInstallWizardPageFragment.class);
                break;
            default:
                throw new RuntimeException(new IllegalStateException("DeviceModel not found: unknown device '" + codename + "'"));
        }
        return d;
    }

    public static DeviceModel getModel(InstalledViewModel m) {
        return getModel(m.getCodename().getValue());
    }

    public static DeviceModel getModel(WizardViewModel m) {
        return getModel(m.getCodename().getValue());
    }

    public static DeviceModel getModel(DeviceInstallerViewModel m) {
        return getModel(m.getCodename().getValue());
    }

    public static List<DeviceModel> getModels() {
        List<DeviceModel> list = new ArrayList();
        deviceList.forEach(device ->  list.add(DeviceList.getModel(device)));

        return list;
    }
}
