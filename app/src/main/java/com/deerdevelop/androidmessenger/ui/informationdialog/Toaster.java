package com.deerdevelop.androidmessenger.ui.informationdialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Toaster {
    public static void show(final Context context, final String text, boolean longDuration) {
        int duration = Toast.LENGTH_SHORT;

        if (longDuration) duration = Toast.LENGTH_LONG;

        Handler handler = new Handler(Looper.getMainLooper());

        int finalDuration = duration;

        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, finalDuration).show();
            }
        });
    }
}
