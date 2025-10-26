package com.example.echosos.ui.main;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("About");

        String version = "1.0";
        try {
            PackageInfo p = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = p.versionName + " (" + p.versionCode + ")";
        } catch (PackageManager.NameNotFoundException ignored) {}

        String text = "EchoSOS\n"
                + "Version: " + version + "\n"
                + "Package: " + getPackageName() + "\n"
                + "SDK: " + Build.VERSION.SDK_INT + "\n\n"
                + "A safety app with SOS, location tracking, emergency calls and auto-recording.";

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16f);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(tv);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }
}
