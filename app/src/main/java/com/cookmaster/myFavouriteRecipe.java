package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class myFavouriteRecipe extends AppCompatActivity {
    private RecyclerView recyclerView;
    ArrayList<DocumentSnapshot> recipeList=new ArrayList<>();
    recipeListAdapter RecipeListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_category_list_activity);
        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        TextView title=findViewById(R.id.listTitle);
        title.setText(getString(R.string.my_favourite_recipe));
        String userEmail=getSharedPreferences("user",MODE_PRIVATE).getString("email",null);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SearchView searchView=findViewById(R.id.listSearchView);
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
            if (documentSnapshot.contains("favouriteRecipe"))favouriteRecipe=(ArrayList<String>) documentSnapshot.get("favouriteRecipe");
            assert favouriteRecipe != null;
            if(favouriteRecipe.isEmpty()) findViewById(R.id.noRecipeFound).setVisibility(View.VISIBLE);
            RecipeListAdapter = new recipeListAdapter(this, recipeList,favouriteRecipe,userEmail,getSharedPreferences("setting",MODE_PRIVATE).getString("appLanguage","en"));
            recyclerView.setAdapter(RecipeListAdapter);
            assert favouriteRecipe != null;
            fetchInitialData();
        });
    }
    ArrayList<String> favouriteRecipe=new ArrayList<>();
    private void search(String query) {
        FirebaseFirestore.getInstance().collection("recipes")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot d:queryDocumentSnapshots)
                        if(favouriteRecipe.contains(d.getId())) {
                            recipeList.add(d);
                            RecipeListAdapter.notifyDataSetChanged();
                        }
                });
    }
    private void fetchInitialData(){
        recipeList.clear();
        for (String docId:favouriteRecipe) FirebaseFirestore.getInstance()
                .collection("recipes").document(docId).get()
                .addOnSuccessListener(documentSnapshot1 -> {
                    recipeList.add(documentSnapshot1);
                    RecipeListAdapter.notifyDataSetChanged();
                });
    }
}