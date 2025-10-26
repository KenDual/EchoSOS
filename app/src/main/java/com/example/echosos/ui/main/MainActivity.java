package com.example.echosos.ui.main;

import static android.text.TextUtils.replace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.services.recording.UploadRetryService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private AlertDialog noInternetDialog;

    private final BroadcastReceiver netUi = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            String a = i.getAction();
            if ("com.example.echosos.NETWORK_OFFLINE".equals(a)) {
                showNoInternetDialog();
            } else if ("com.example.echosos.NETWORK_ONLINE".equals(a)) {
                dismissNoInternetDialog();
                startService(new Intent(MainActivity.this, UploadRetryService.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView nav = findViewById(R.id.bottom_nav);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replace(new HomeFragment());
                return true;
            } else if (id == R.id.nav_map) {
                replace(new MapFragment());
                return true;
            } else if (id == R.id.nav_contacts) {
                replace(new ContactsFragment());
                return true;
            } else if (id == R.id.nav_call) {
                replace(new CallFragment());
                return true;
            } else if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return false; // giữ tab hiện tại
            } else if (id == R.id.action_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return false; // giữ tab hiện tại
            }
            return false;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter f = new IntentFilter();
        f.addAction("com.example.echosos.NETWORK_ONLINE");
        f.addAction("com.example.echosos.NETWORK_OFFLINE");
        // FIX: dùng ContextCompat.registerReceiver với flag NOT_EXPORTED
        ContextCompat.registerReceiver(
                this,
                netUi,
                f,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onStop() {
        try { unregisterReceiver(netUi); } catch (Exception ignored) {}
        dismissNoInternetDialog();
        super.onStop();
    }

    private void showNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) return;
        noInternetDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.no_internet)
                .setMessage(getString(R.string.no_internet))
                .setPositiveButton(R.string.open_wifi_settings,
                        (d, w) -> startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
        noInternetDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) { // optional
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void dismissNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
        }
    }

    //Helper
    private void replace(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }

}
