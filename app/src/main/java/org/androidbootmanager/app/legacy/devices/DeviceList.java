package org.androidbootmanager.app.legacy.devices;

import org.androidbootmanager.app.legacy.ui.home.InstalledViewModel;
import org.androidbootmanager.app.legacy.ui.installer.DeviceInstallerViewModel;
import org.androidbootmanager.app.legacy.ui.installer.DeviceInstallerWizardPageFragment;
import org.androidbootmanager.app.legacy.ui.installer.DoInstallWizardPageFragment;
import org.androidbootmanager.app.legacy.ui.installer.DroidBootSelectorWizardPageFragment;
import org.androidbootmanager.app.legacy.ui.wizard.WizardViewModel;

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
            case "mimameid":
                d = new DeviceModel();
                d.codename = "mimameid";
                d.viewname = "Volla Phone 22";
                d.bdev = "/dev/block/mmcblk1";
                d.pbdev = "/dev/block/mmcblk1p";
                d.spartsize = 7340031f;
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
