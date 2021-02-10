package org.androidbootmanager.app.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.topjohnwu.superuser.Shell;

import org.androidbootmanager.app.R;

public class MiscUtils {
    public static ProgressDialog prog;
    public static void sure(Context c, DialogInterface d, String msg, DialogInterface.OnClickListener v) {
        d.dismiss();
        new AlertDialog.Builder(c)
                .setPositiveButton(R.string.ok, v)
                .setNegativeButton(R.string.cancel, (id, p) -> id.dismiss())
                .setCancelable(true)
                .setTitle(R.string.sure_title)
                .setMessage(msg)
                .show();
    }

    public static void sure(Context c, DialogInterface d, int msg, DialogInterface.OnClickListener v) {
        sure(c,d, c.getString(msg), v);
    }

    public static void w(Context c, String msg, Runnable r) {
        //TODO: no more deprecated ProgressDialog
        prog = new ProgressDialog(c);
        prog.setIndeterminate(true);
        prog.setCanceledOnTouchOutside(false);
        prog.setCancelable(false);
        prog.setTitle(R.string.wait);
        prog.setMessage(msg);
        prog.show();
        r.run();
    }

    public static void w(Context c, int msg, Runnable r) {
        w(c, c.getString(msg), r);
    }

    public static Shell.ResultCallback w2(Shell.ResultCallback r) {
        return (a) -> {
            r.onResult(a);
            prog.dismiss();
        };
    }
}