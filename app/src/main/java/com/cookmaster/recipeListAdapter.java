package com.cookmaster;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class recipeListAdapter extends RecyclerView.Adapter<recipeListAdapter.viewHolder> {
    private final Context context;
    private final ArrayList<DocumentSnapshot> recipeList;
    private final ArrayList<String> favouriteRecipe;
    private final String userEmail,language;
    public recipeListAdapter(Context context, ArrayList<DocumentSnapshot> recipeList,ArrayList<String> favouriteRecipe,String userEmail,String language) {
        this.context = context;
        this.recipeList = recipeList;
        this.favouriteRecipe=favouriteRecipe;
        this.userEmail=userEmail;
        this.language=language;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.recipe_list_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        fillColor(holder.cardView,i);
        if(!recipeList.isEmpty()) {
            DocumentSnapshot doc = recipeList.get(i);
            try {
                File localFile = File.createTempFile("images", "jpg");
                FirebaseStorage.getInstance().getReference("recipeImages/" + doc.getString("iconId"))
                        .getFile(localFile)
                        .addOnSuccessListener(taskSnapshot -> holder.imageView.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (language.equals("hi")){
                holder.recipeName.setText(doc.getString("name-hi"));
            }
            else{
                holder.recipeName.setText(doc.getString("name"));
            }
            holder.time.setText(doc.get("time")+" min");
            holder.energy.setText(doc.get("energy")+" cal");
            holder.views.setText(doc.get("views")+" views");
            if(!Objects.requireNonNull(doc.getString("videoId")).isEmpty()) holder.video.setVisibility(View.VISIBLE);
            if(Objects.requireNonNull(doc.getLong("totalRatingNumber"))==0) holder.ratingNumber.setVisibility(View.GONE);
            else holder.ratingNumber.setText(String.valueOf(new DecimalFormat("#.0").format(Objects.requireNonNull(doc.getDouble("totalRatingNumber"))/Objects.requireNonNull(doc.getDouble("ratedPeopleNumber")))));
            if(favouriteRecipe.contains(doc.getId())) holder.imageButton.setImageResource(R.drawable.baseline_favorite_24);
            else holder.imageButton.setImageResource(R.drawable.baseline_favorite_border_24);
            holder.imageButton.setOnClickListener(view -> {
                if(favouriteRecipe.contains(doc.getId())){
                    holder.imageButton.setImageResource(R.drawable.baseline_favorite_border_24);
                    favouriteRecipe.remove(doc.getId());
                    FirebaseFirestore.getInstance().collection("users").document(userEmail).update("favouriteRecipe", FieldValue.arrayRemove(doc.getId()));
                }
                else{
                    holder.imageButton.setImageResource(R.drawable.baseline_favorite_24);
                    favouriteRecipe.add(doc.getId());
                    FirebaseFirestore.getInstance().collection("users").document(userEmail).update("favouriteRecipe",FieldValue.arrayUnion(doc.getId()));
                }
            });
            holder.cardView.setOnClickListener(view -> context.startActivity(new Intent(context, recipeViewActivity.class).putExtra("recipeId",doc.getId())));
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView recipeName, time, energy, video, ratingNumber, views;
        CardView cardView;
        ImageView imageView;
        ImageButton imageButton;
        public viewHolder(View v) {
            super(v);
            imageView =v.findViewById(R.id.imageView);
            cardView=v.findViewById(R.id.cardView);
            recipeName =v.findViewById(R.id.recipeName);
            time =v.findViewById(R.id.time);
            energy =v.findViewById(R.id.energy);
            video=v.findViewById(R.id.video);
            imageButton=v.findViewById(R.id.addToFavBtn);
            views=v.findViewById(R.id.views);
            ratingNumber=v.findViewById(R.id.ratingNumber);
        }
    }
    private void fillColor(CardView cardView, int position){
        switch (position%5) {
            case 0: cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green)); break;
            case 1: cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red)); break;
            case 2: cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange)); break;
            case 3: cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.yellow)); break;
            case 4: cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_blue)); break;
        }
    }
}

