package com.volla.bootmanager.legacy.roms;

import com.volla.bootmanager.legacy.ui.update.UpdateBase;
import com.volla.bootmanager.legacy.ui.update.UpdateBaseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ROM {
    public String viewname;
    public String scriptname;
    public String fullPath;
    public Map<String, String> requiredFiles;
    public ArrayList<String> parts;
    public HashMap<String, String> strings;
    public HashMap<String, String[]> flashes;
    public CmdlineGenerator gen;
    public ROMType type;
    public List<UpdateBaseInfo> updates;
}
