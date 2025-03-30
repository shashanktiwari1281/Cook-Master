package com.cookmaster;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class foodCategoryAdapter extends RecyclerView.Adapter<foodCategoryAdapter.viewHolder> {
    private final Context context;
    private final ArrayList<DocumentSnapshot> categoryList;
    String language;
    public foodCategoryAdapter(Context context, ArrayList<DocumentSnapshot> categoryList, String language) {
        this.context = context;
        this.categoryList=categoryList;
        this.language=language;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.food_category_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        fillColor(holder.cardView,i);
        if(!categoryList.isEmpty()) {
            DocumentSnapshot doc = categoryList.get(i);
            try {
                File localFile = File.createTempFile("images", "jpg");
                FirebaseStorage.getInstance().getReference("categoryImages/" + doc.getString("imgId"))
                        .getFile(localFile)
                        .addOnSuccessListener(taskSnapshot -> {
                            holder.imageView.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (language.equals("hi")) {
                holder.categoryName.setText(doc.getString("docId-hi"));
                holder.categoryDesc.setText(doc.getString("desc-hi"));
            } else {
                holder.categoryName.setText(doc.getId());
                holder.categoryDesc.setText(doc.getString("desc"));

            }
            Intent intent= new Intent(context, recipeListActivity.class)
                    .putExtra("category", doc.getId());
            if(language.equals("hi")) intent.putExtra("category_hi", doc.getString("docId-hi"));
            holder.cardView.setOnClickListener(view -> context.startActivity(intent));
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, categoryDesc;
        CardView cardView;
        ImageView imageView;
        public viewHolder(View v) {
            super(v);
            imageView =v.findViewById(R.id.imageView);
            cardView=v.findViewById(R.id.cardView);
            categoryName=v.findViewById(R.id.categoryName);
            categoryDesc=v.findViewById(R.id.categoryDesc);
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

