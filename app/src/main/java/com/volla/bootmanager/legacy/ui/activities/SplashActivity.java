package com.volla.bootmanager.legacy.ui.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;

import com.volla.bootmanager.BuildConfig;
import com.volla.bootmanager.R;
import com.volla.bootmanager.legacy.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SplashActivity extends AppCompatActivity {

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(10));
    }

    boolean fail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        MainActivity.exit = false;
        new Thread(() -> {
            copyAssets();
            runOnUiThread(() -> {
                if (!fail) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.fatal)
                            .setMessage(R.string.cp_err_msg)
                            .setNegativeButton(R.string.cancel, (p1, p2) -> this.finish())
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, (p1, p2) -> {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                finish();
                            })
                            .show();
                }
            });
        }).start();
    }

    private void copyAssets() {
        if (!new File(Constants.assetDir).exists()) fail = fail | !new File(Constants.assetDir).mkdir();
        if (!new File(Constants.filesDir).exists()) fail = fail | !new File(Constants.filesDir).mkdir();
        if (!new File(Constants.tempDir).exists()) fail = fail | !new File(Constants.tempDir).mkdir();
        copyAssets("Toolkit", "Toolkit");
        copyAssets("Scripts", "Scripts");
        copyAssets("cp", "");
        Shell.sh("chmod -R +x /data/data/com.volla.bootmanager/assets").exec();
    }

    private void copyAssets(String src, String outp) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(src);
        } catch (IOException e) {
            Log.e("ABM_AssetCopy", "Failed to get asset file list.", e);
            fail = true;
        }

        assert files != null;
        for (String filename : files) {
            copyAssets(src, outp, assetManager, filename);
        }
    }

    private void copyAssets(String src, String outp, AssetManager assetManager, String filename) {
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(src + "/" + filename);
            File outFile = new File(Constants.assetDir + outp, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            Log.d("ABM_AssetCopy","Result of mkdir #1: " + new File(Constants.assetDir + outp + File.separator).mkdir());
            Log.d("ABM_AssetCopy",Log.getStackTraceString(e));
            try {
                assetManager.open(src + File.separator + filename).close();
                copyAssets(src, outp, assetManager, filename);
            } catch (FileNotFoundException e2) {

                Log.d("ABM_AssetCopy","Result of mkdir #2: " + new File(Constants.assetDir + outp + File.separator + filename).mkdir());
                Log.d("ABM_AssetCopy",Log.getStackTraceString(e2));
                copyAssets(src + File.separator + filename, outp + File.separator + filename);
            } catch (IOException ex) {
                Log.e("ABM_AssetCopy", "Failed to copy asset file: " + filename, ex);
                fail = true;
            }
        } catch (IOException e) {
            Log.e("ABM_AssetCopy", "Failed to copy asset file: " + filename, e);
            fail = true;
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}