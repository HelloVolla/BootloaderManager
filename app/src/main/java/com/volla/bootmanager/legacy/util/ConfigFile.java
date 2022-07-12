package com.volla.bootmanager.legacy.util;

import android.annotation.SuppressLint;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConfigFile {
    final Map<String, String> data = new HashMap<>();

    public String get(String name) {
        return data.get(name);
    }

    public void set(String name, String value) {
        data.put(name, value);
    }

    public String exportToString() {
        StringBuilder out = new StringBuilder();
        for (String key : data.keySet()) {
            out.append(key).append(" ").append(get(key)).append("\n");
        }
        return out.toString();
    }

    public void exportToFile(File file) {
        try {
            if (!file.exists()) file.createNewFile();
            PrintWriter out = new PrintWriter(file);
            out.write(exportToString());
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("SdCardPath")
    public void exportToPrivFile(String tempfilename, String fullPath) {
        exportToFile(new File("/data/data/com.volla.bootmanager/files/" + tempfilename));
        Shell.sh("mv " + "\"/data/data/com.volla.bootmanager/files/" + tempfilename + "\" \"" + fullPath + "\"").exec();
    }

    public static ConfigFile importFromString(String s) {
        ConfigFile out = new ConfigFile();
        for (String line : s.split("\n")) {
            line = line.trim();
            out.set(line.substring(0, line.indexOf(" ")), line.substring(line.indexOf(" ")).trim());
        }
        return out;
    }

    public static ConfigFile importFromFile(File f) throws ActionAbortedCleanlyError {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        InputStream i;
        byte[] b = new byte[1024];
        int o;
        try {
            i = SuFileInputStream.open(f);
        } catch (FileNotFoundException e) {
            throw new ActionAbortedCleanlyError(e);
        }
        while (true) {
            try {
                if (!((o = i.read(b)) > 1)) break;
                s.write(b, 0, o);
            } catch (IOException e) {
                throw new ActionAbortedCleanlyError(e);
            }
        }
        try {
            i.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return importFromString(new String(s.toByteArray()));
    }

    public static ConfigFile importFromFile(String s) throws ActionAbortedCleanlyError {
        return importFromFile(new File(s));
    }
}
