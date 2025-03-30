package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class adminHome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        findViewById(R.id.button).setOnClickListener(view -> startActivity(new Intent(this, addFoodCategoryActivity.class)));
        findViewById(R.id.addRecipe).setOnClickListener(view -> startActivity(new Intent(this, addNewRecipe.class)));
    }
}