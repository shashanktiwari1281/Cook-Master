package com.cookmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class recipeViewActivity extends AppCompatActivity {
    private SharedPreferences user, setting;
    private ArrayList<String> favouriteRecipe=new ArrayList<>();
    ImageView thumbnail;
    TextView name,category,ratingNumber,time,energy,serveNum,desc,ingredient,instruction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);
        user =getSharedPreferences("user",MODE_PRIVATE);
        setting=getSharedPreferences("setting",MODE_PRIVATE);
        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        thumbnail=findViewById(R.id.thumbnailImage);
        name=findViewById(R.id.recipeName);
        category=findViewById(R.id.category);
        ratingNumber=findViewById(R.id.ratingNumber);
        time=findViewById(R.id.cookingTime);
        energy=findViewById(R.id.caloriesContains);
        serveNum=findViewById(R.id.serveNumber);
        desc=findViewById(R.id.desc);
        ingredient=findViewById(R.id.ingredients);
        instruction=findViewById(R.id.instructions);
        FirebaseFirestore.getInstance().collection("recipes").document(getIntent().getStringExtra("recipeId")).update("views",FieldValue.increment(1));
//        ProgressDialog progressDialog=new ProgressDialog(this);
//        progressDialog.setTitle("Loading");
//        progressDialog.show();
        ImageButton favouriteBtn=findViewById(R.id.addToFavBtn);
        Task<DocumentSnapshot> task=FirebaseFirestore.getInstance().collection("users").document(user.getString("email",null)).get();
        FirebaseFirestore.getInstance().collection("recipes").document(getIntent().getStringExtra("recipeId")).get()
                        .addOnSuccessListener(doc ->{
                            task.addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.contains("favouriteRecipe")) favouriteRecipe= (ArrayList<String>) documentSnapshot.get("favouriteRecipe");
                                if (favouriteRecipe!=null&&favouriteRecipe.contains(doc.getId()))
                                    favouriteBtn.setImageResource(R.drawable.baseline_favorite_24);
                                else
                                    favouriteBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                            });
                            try {
                                File localFile = File.createTempFile("images", "jpg");
                                FirebaseStorage.getInstance().getReference("recipeImages/" + doc.getString("thumbnailId"))
                                        .getFile(localFile)
                                        .addOnSuccessListener(taskSnapshot -> thumbnail.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath())));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            //progressDialog.dismiss();
                            ratingNumber.setText(String.valueOf(new DecimalFormat("#.0").format(Objects.requireNonNull(doc.getDouble("totalRatingNumber"))/Objects.requireNonNull(doc.getDouble("ratedPeopleNumber")))));
                            if(!Objects.equals(doc.get("time"), "N/A")) time.setText(doc.get("time")+" min");
                            if(!Objects.equals(doc.get("energy"), "N/A")) energy.setText(doc.get("energy")+" cal");
                            if(!Objects.equals(doc.get("serve"), "N/A")) serveNum.setText(doc.get("serve")+" serve");
                            if(setting.getString("appLanguage","en").equals("en")) setEnglishView(doc);
                            else setHindiViews(doc);
                            if(!Objects.equals(doc.getString("videoId"), "")) {
                                YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
                                youTubePlayerView.setVisibility(View.VISIBLE);
                                findViewById(R.id.videoTV).setVisibility(View.VISIBLE);
                                getLifecycle().addObserver(youTubePlayerView);
                                youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                    @Override
                                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                                        youTubePlayer.loadVideo(Objects.requireNonNull(doc.getString("videoId")), 0);
                                        youTubePlayer.pause();
                                    }
                                });
                            }
                        });
        favouriteBtn.setOnClickListener(view -> {
            if(favouriteRecipe.contains(getIntent().getStringExtra("recipeId"))){
                favouriteBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                favouriteRecipe.remove(getIntent().getStringExtra("recipeId"));
                FirebaseFirestore.getInstance().collection("users").document(user.getString("email",null)).update("favouriteRecipe", FieldValue.arrayRemove(getIntent().getStringExtra("recipeId")));
            }
            else{
                favouriteBtn.setImageResource(R.drawable.baseline_favorite_24);
                favouriteRecipe.add(getIntent().getStringExtra("recipeId"));
                FirebaseFirestore.getInstance().collection("users").document(user.getString("email",null)).update("favouriteRecipe",FieldValue.arrayUnion(getIntent().getStringExtra("recipeId")));
            }
        });
        MobileAds.initialize(this,initializationStatus -> {});
        setGoogleAd();
        RatingBar ratingBar=findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            Map<String,Object> data=new HashMap<>(2);
            data.put("ratedPeopleNumber",FieldValue.increment(1));
            data.put("totalRatingNumber",FieldValue.increment(rating));
            FirebaseFirestore.getInstance().collection("recipes").document(getIntent().getStringExtra("recipeId")).update(data);
            Toast.makeText(recipeViewActivity.this, "Thank You for rating.", Toast.LENGTH_SHORT).show();
        });
    }
    void setGoogleAd(){
        AdRequest adRequest=new AdRequest.Builder().build();
        AdView adView=findViewById(R.id.googleAd);
        adView.loadAd(adRequest);
        AdView adView2=findViewById(R.id.googleAd2);
        adView2.loadAd(adRequest);
        AdView adView3=findViewById(R.id.googleAd3);
        adView3.loadAd(adRequest);
    }
    void setHindiViews(DocumentSnapshot doc){
        name.setText(doc.getString("name-hi"));
        category.setText(doc.getString("category-hi"));
        desc.setText(doc.getString("desc-hi"));
        StringBuilder s2= new StringBuilder();
        for (String s:(ArrayList<String>) Objects.requireNonNull(doc.get("ingredient-hi"))) s2.append("• ").append(s).append("\n");
        ingredient.setText(s2.toString());
        StringBuilder s3= new StringBuilder();
        for (String s:(ArrayList<String>) Objects.requireNonNull(doc.get("instruction-hi"))) s3.append("• ").append(s).append("\n");
        instruction.setText(s3.toString());
    }
    void setEnglishView(DocumentSnapshot doc){
        name.setText(doc.getString("name"));
        category.setText(doc.getString("category"));
        desc.setText(doc.getString("desc"));
        StringBuilder s2= new StringBuilder();
        for (String s:(ArrayList<String>) Objects.requireNonNull(doc.get("ingredient"))) s2.append("• ").append(s).append("\n").append("\n");
        ingredient.setText(s2.toString());
        StringBuilder s3= new StringBuilder();
        for (String s:(ArrayList<String>) Objects.requireNonNull(doc.get("instruction"))) s3.append("• ").append(s).append("\n").append("\n");
        instruction.setText(s3.toString());
    }
}