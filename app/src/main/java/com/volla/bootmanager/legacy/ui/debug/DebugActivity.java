package com.volla.bootmanager.legacy.ui.debug;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;

import com.volla.bootmanager.R;

import java.util.ArrayList;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    public void errorTest(View v) {
        shelldialog("echo out && echo err 1>&2");
    }

    public void shelldialog(String j) {
        ArrayList<String> o = new ArrayList<>();
        ArrayList<String> e = new ArrayList<>();
        Shell.Result r = Shell.sh(j).to(o, e).exec();
        new AlertDialog.Builder(this)
                .setTitle(j)
                .setMessage("OUT channel:\n" + String.join("\n", o) + "\nOUT channel using get:\n" + String.join("\n", r.getOut()) + "\nERR channel:\n" + String.join("\n", e) + "\nERR channel using get:\n" + String.join("\n", r.getErr()))
                .show();
    }
}