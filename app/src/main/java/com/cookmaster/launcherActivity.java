package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class launcherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        setting.setLocale(getSharedPreferences("setting",MODE_PRIVATE).getString("appLanguage","en"),this);
        new Handler().postDelayed(()->{
            if(getSharedPreferences("setting",MODE_PRIVATE).getBoolean("isLoggedIn",false))
                startActivity(new Intent(this, foodCategoryListActivity.class));
            else startActivity(new Intent(this,loginActivity.class));
            finish();
        },4000);
    }
}