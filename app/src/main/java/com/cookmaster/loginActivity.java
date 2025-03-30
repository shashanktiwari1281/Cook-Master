package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class loginActivity extends AppCompatActivity {
    private String activeView, userName;
    boolean isLoggedIn=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activeView="Welcome";
        EditText userNameET=findViewById(R.id.userName);
        ImageView imageView=findViewById(R.id.backgroundImg);
        findViewById(R.id.nextBtn).setOnClickListener(view -> {
            switch (activeView) {
                case "Welcome":
                    animation();
                    new Handler().postDelayed(() -> {
                        findViewById(R.id.welcomeLinearLayout).setVisibility(View.GONE);
                        findViewById(R.id.userNameLinearLayout).setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.drawable.foodimg2);
                    }, 200);
                    activeView = "userName";
                    break;
                case "userName":
                    userName=userNameET.getText().toString();
                    if (userName.isEmpty()) userNameET.setError("Enter Your name");
                    else {
                        animation();
                        new Handler().postDelayed(() -> {
                            findViewById(R.id.userNameLinearLayout).setVisibility(View.GONE);
                            findViewById(R.id.emailLinearLayout).setVisibility(View.VISIBLE);
                            imageView.setImageResource(R.drawable.foodimg3);
                        }, 200);
                        activeView = "email";
                    }
                    break;
                case "email":
                    EditText emailET=findViewById(R.id.emailAddress);
                    String email=emailET.getText().toString();
                    if(email.isEmpty()) emailET.setError("Enter Your Email");
                    else if(!email.contains("@")||!email.contains(".")) emailET.setError("Not a valid Email");
                    else {
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.signInAnonymously().addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseFirestore.getInstance().collection("users").document(email)
                                                .get().addOnSuccessListener(documentSnapshot -> {
                                                    if (documentSnapshot.exists())
                                                        FirebaseFirestore.getInstance().collection("users").document(email)
                                                                .update("name", userName).addOnSuccessListener(runnable -> {
                                                                    login(userName, email);
                                                                });
                                                    else {
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("name", userName);
                                                        FirebaseFirestore.getInstance().collection("users").document(email)
                                                                .set(data).addOnSuccessListener(runnable -> {
                                                                    login(userName, email);
                                                                });
                                                    }
                                                });
                                    }
                                    else Toast.makeText(this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                });
                        animation();
                        new Handler().postDelayed(() -> {
                            findViewById(R.id.emailLinearLayout).setVisibility(View.GONE);
                            findViewById(R.id.languageLinearLayout).setVisibility(View.VISIBLE);
                            imageView.setImageResource(R.drawable.foodimg4);
                            findViewById(R.id.nextBtn).setVisibility(View.GONE);
                        }, 200);
                        activeView = "language";
                    }
                    break;
            }
        });
        findViewById(R.id.englishBtn).setOnClickListener(view -> setLanguage("en"));
        findViewById(R.id.hindiButton).setOnClickListener(view -> setLanguage("hi"));
    }
    void setLanguage(String language){
        getSharedPreferences("setting",MODE_PRIVATE).edit().putString("appLanguage",language).putBoolean("isLoggedIn",true).apply();
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Please wait...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    if (isLoggedIn) {
                        setting.setLocale(language,loginActivity.this);
                        progressDialog.dismiss();
                        startActivity(new Intent(loginActivity.this,foodCategoryListActivity.class));
                        finish();
                    }
                    else handler.postDelayed(this, 1000);
                }
            };
            handler.post(r);
    }
    void animation(){
        RelativeLayout relativeLayout=findViewById(R.id.relativeLayout);
        ObjectAnimator animator = ObjectAnimator.ofFloat(relativeLayout, "translationX", 0, -setting.getScreenWidth(this));
        animator.setDuration(200);
        animator.start();
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(relativeLayout, "translationX",  setting.getScreenWidth(this), 0);
        animator2.setStartDelay(200);
        animator2.setDuration(200);
        animator2.start();
    }
    void login(String userName,String email){
        StringBuilder firstName= new StringBuilder();
        for (int i=0;i<userName.length();i++){
            if (userName.charAt(i)==' ') break;
            firstName.append(userName.charAt(i));
        }
        getSharedPreferences("user",MODE_PRIVATE).edit().putString("name",userName).putString("firstName", String.valueOf(firstName)).putString("email",email).apply();
        isLoggedIn=true;
    }
}