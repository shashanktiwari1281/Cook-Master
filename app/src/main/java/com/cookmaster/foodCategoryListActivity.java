package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class foodCategoryListActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView2;
    ArrayList<DocumentSnapshot> categoryList=new ArrayList<>(),recipeList=new ArrayList<>();
    foodCategoryAdapter FoodCategoryAdapter;
    recipeListAdapter RecipeListAdapter;
    ArrayList<String> favouriteRecipe=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_category_list_activity);
        findViewById(R.id.logo).setOnLongClickListener(view -> {
            if(getSharedPreferences("user",MODE_PRIVATE).getString("email",null).equals("shashanktiwari1281@gmail.com")) {
                Dialog dialog=new Dialog(this);
                dialog.setContentView(R.layout.admin_credential);
                dialog.show();
                dialog.findViewById(R.id.closeBtn).setOnClickListener(view1 -> dialog.dismiss());
                dialog.findViewById(R.id.submitBtn).setOnClickListener(view1 -> {
                    EditText userId=dialog.findViewById(R.id.userId),
                            password=dialog.findViewById(R.id.password);
                    if (userId.getText().toString().isEmpty()) userId.setError(getString(R.string.required));
                    else if (password.getText().toString().isEmpty()) password.setError(getString(R.string.required));
                    else {
                        FirebaseFirestore.getInstance().collection("admin")
                                .document("adminDetails").get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (userId.getText().toString().equals(documentSnapshot.getString("userId")) &&
                                            password.getText().toString().equals(documentSnapshot.getString("password"))) {
                                        dialog.dismiss();
                                        startActivity(new Intent(this, adminHome.class));
                                    }
                                });
                    }
                });
            }
            return false;
        });
        sharedPreferences=getSharedPreferences("setting",MODE_PRIVATE);
        setting.setLocale(sharedPreferences.getString("appLanguage","en"),this);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2=findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        FoodCategoryAdapter= new foodCategoryAdapter(this,
                categoryList,
                sharedPreferences.getString("appLanguage","en"));
        recyclerView.setAdapter(FoodCategoryAdapter);
        TextView listTitle=findViewById(R.id.listTitle);
        FirebaseFirestore.getInstance().collection("users")
                .document(getSharedPreferences("user",MODE_PRIVATE).getString("email",null))
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.contains("favouriteRecipe")) favouriteRecipe= (ArrayList<String>) documentSnapshot.get("favouriteRecipe");
                    RecipeListAdapter=new recipeListAdapter(this,
                            recipeList,
                            favouriteRecipe,
                            getSharedPreferences("user",MODE_PRIVATE).getString("email",null),
                            sharedPreferences.getString("appLanguage","en"));
                    recyclerView2.setAdapter(RecipeListAdapter);
                    fetchInitialData();
                });
        findViewById(R.id.relativeLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.backButton).setVisibility(View.GONE);
        TextView name=findViewById(R.id.userName);
        name.setText("Hii "+getSharedPreferences("user",MODE_PRIVATE).getString("firstName","User")+",");
        findViewById(R.id.cardView3).setOnClickListener(view -> startActivity(new Intent(this, myFavouriteRecipe.class)));
        listTitle.setText(R.string.categories);
        SearchView searchView=findViewById(R.id.listSearchView);
        searchView.setQueryHint(getString(R.string.category_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    fetchInitialData();
                } else {
                    search(newText);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            fetchInitialData();
            return false;
        });
        navigationDrawer();
    }
    private String APP_LINK="";
    void navigationDrawer(){
        ImageButton navBtn=findViewById(R.id.menuButton);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navBtn.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        NavigationView navigationView = findViewById(R.id.nav_view);
        FirebaseFirestore.getInstance().collection("appDetails")
                .document("appDetails").get()
                .addOnSuccessListener(documentSnapshot -> APP_LINK=documentSnapshot.getString("appLink"));
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.changeLanguage) setting.changeLanguage(this,this, sharedPreferences);
            else if(id==R.id.myFavouriteRecipe) startActivity(new Intent(this, myFavouriteRecipe.class));
            else if (id==R.id.reportIssue){
                Dialog dialog=new Dialog(this);
                dialog.setContentView(R.layout.text_area_layout);
                TextView textView=dialog.findViewById(R.id.title);
                textView.setText(R.string.report_issue);
                EditText editText=dialog.findViewById(R.id.editText);
                dialog.findViewById(R.id.submitBtn).setOnClickListener(view -> {
                    if(editText.getText().toString().isEmpty()) editText.setError(getString(R.string.required));
                    else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("reportedBy", getSharedPreferences("user", MODE_PRIVATE).getString("firstName", null));
                        data.put("email", getSharedPreferences("user", MODE_PRIVATE).getString("email", null));
                        data.put("issue", editText.getText().toString());
                        FirebaseFirestore.getInstance().collection("reportedProblem")
                                .document().set(data).addOnSuccessListener(runnable -> {
                                    dialog.dismiss();
                                    setting.thankYouDialog(this, getString(R.string.reportResponse));
                                });
                    }
                });
                dialog.show();
            }
            else if (id==R.id.suggestFeature){
                Dialog dialog=new Dialog(this);
                dialog.setContentView(R.layout.text_area_layout);
                TextView textView=dialog.findViewById(R.id.title);
                textView.setText(R.string.suggest_a_feature);
                EditText editText=dialog.findViewById(R.id.editText);
                dialog.findViewById(R.id.submitBtn).setOnClickListener(view -> {
                    if(editText.getText().toString().isEmpty()) editText.setError(getString(R.string.required));
                    else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("suggestedBy", getSharedPreferences("user", MODE_PRIVATE).getString("firstName", null));
                        data.put("email", getSharedPreferences("user", MODE_PRIVATE).getString("email", null));
                        data.put("suggestion", editText.getText().toString());
                        FirebaseFirestore.getInstance().collection("suggestion")
                                .document().set(data).addOnSuccessListener(runnable -> {
                                    dialog.dismiss();
                                    setting.thankYouDialog(this, getString(R.string.suggestionResponse));
                                });
                    }
                });
                dialog.show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
    private void search(String query) {
        FirebaseFirestore.getInstance().collection("foodCategories")
                .orderBy(FieldPath.documentId())
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                        categoryList.clear();
                    for (DocumentSnapshot d:queryDocumentSnapshots) categoryList.add(d);
                    FoodCategoryAdapter.notifyDataSetChanged();
                });
        FirebaseFirestore.getInstance().collection("recipes")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot d : queryDocumentSnapshots) recipeList.add(d);
                    RecipeListAdapter.notifyDataSetChanged();
                });
    }
    private void fetchInitialData(){
        FirebaseFirestore.getInstance()
                .collection("foodCategories")
                .orderBy(FieldPath.documentId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    recipeList.clear();
                    for (DocumentSnapshot d : queryDocumentSnapshots) categoryList.add(d);
                    FoodCategoryAdapter.notifyDataSetChanged();
                    RecipeListAdapter.notifyDataSetChanged();
                });
    }
}