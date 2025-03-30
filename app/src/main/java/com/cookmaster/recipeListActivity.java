package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

public class recipeListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    ArrayList<DocumentSnapshot> recipeList=new ArrayList<>();
    recipeListAdapter RecipeListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_category_list_activity);
        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        TextView title=findViewById(R.id.listTitle);
        if(getSharedPreferences("setting",MODE_PRIVATE).getString("appLanguage","en").equals("hi")) title.setText(getIntent().getStringExtra("category_hi"));
        else title.setText(getIntent().getStringExtra("category"));
        String userEmail=getSharedPreferences("user",MODE_PRIVATE).getString("email",null);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SearchView searchView=findViewById(R.id.listSearchView);
        searchView.setQueryHint(getString(R.string.recipe_search));
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
        FirebaseFirestore.getInstance().collection("users").document(userEmail).get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> favouriteRecipe=new ArrayList<>();
            if (documentSnapshot.contains("favouriteRecipe"))favouriteRecipe=(ArrayList<String>) documentSnapshot.get("favouriteRecipe");
            RecipeListAdapter = new recipeListAdapter(this, recipeList,favouriteRecipe,userEmail,getSharedPreferences("setting",MODE_PRIVATE).getString("appLanguage","en"));
            recyclerView.setAdapter(RecipeListAdapter);
            fetchInitialData();
        });
    }
    private void search(String query) {
        FirebaseFirestore.getInstance().collection("recipes")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot d:queryDocumentSnapshots) if(Objects.equals(d.getString("category"), getIntent().getStringExtra("category"))) recipeList.add(d);
                    RecipeListAdapter.notifyDataSetChanged();
                });
    }
    private void fetchInitialData(){
        FirebaseFirestore.getInstance().collection("recipes")
                .whereEqualTo("category",getIntent().getStringExtra("category")).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot d : queryDocumentSnapshots) recipeList.add(d);
                    RecipeListAdapter.notifyDataSetChanged();
                });
    }
}