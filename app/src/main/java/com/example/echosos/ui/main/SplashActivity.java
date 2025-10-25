package com.example.echosos.ui.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.echosos.data.dao.UserDao;
import com.example.echosos.utils.Prefs;

public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        long uid = Prefs.getUserId(this);
        if (uid > 0 && !new UserDao(this).exists(uid)) {
            Prefs.setUserId(this, -1);
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}