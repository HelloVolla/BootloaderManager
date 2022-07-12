package com.volla.bootmanager.legacy.roms;

import com.volla.bootmanager.legacy.ui.addrom.AddROMViewModel;

public interface CmdlineGenerator {
    void gen(AddROMViewModel imodel, String menuName, String folderName);
}
