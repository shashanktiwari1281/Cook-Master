package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class addNewRecipe extends AppCompatActivity {
    Uri iconFilePath, thumbnailFilePath;
    ImageView icon, thumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_recipe);
        CheckBox checkBox=findViewById(R.id.checkBox);
        String id=setting.getTime("yyyyMMddHHmmssMS");
        EditText name=findViewById(R.id.name),
                name_hi=findViewById(R.id.name_hi),
                desc=findViewById(R.id.aboutRecipe),
                desc_hi=findViewById(R.id.aboutRecipe_hi),
                time=findViewById(R.id.cookingTime),
                energy=findViewById(R.id.energy),
                serve=findViewById(R.id.serve),
                ingredient=findViewById(R.id.ingredient),
                ingredient_hi=findViewById(R.id.ingredient_hi),
                instruction=findViewById(R.id.instruction),
                instruction_hi=findViewById(R.id.instruction_hi),
                videoId=findViewById(R.id.videoId);
        Spinner categorySpinner=findViewById(R.id.categorySpinner);
        ArrayList<String> categoryArr=new ArrayList<>(),categoryArr_hi=new ArrayList<>();
        categoryArr.add("Select Category");
        FirebaseFirestore.getInstance().collection("foodCategories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot queryDocumentSnapshot:queryDocumentSnapshots) {
                categoryArr.add(queryDocumentSnapshot.getId());
                categoryArr_hi.add(queryDocumentSnapshot.getString("docId-hi"));
            }
        });
        categorySpinner.setAdapter(new ArrayAdapter<>(this,androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,categoryArr));
        icon=findViewById(R.id.icon);
        thumbnail=findViewById(R.id.thumbnailImage);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                findViewById(R.id.cardView2).setVisibility(View.GONE);
                findViewById(R.id.chooseIconBtn).setVisibility(View.GONE);
            } else {
                findViewById(R.id.cardView2).setVisibility(View.VISIBLE);
                findViewById(R.id.chooseIconBtn).setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.chooseIconBtn).setOnClickListener(view ->
                startActivityForResult(Intent.createChooser(
                        new Intent()
                                .setType("image/*")
                                .setAction(Intent.ACTION_GET_CONTENT),
                        "Choose Icon for recipe..."),
                        22));
        findViewById(R.id.chooseThumbnailBtn).setOnClickListener(view ->
                startActivityForResult(Intent.createChooser(
                        new Intent()
                                .setType("image/*")
                                .setAction(Intent.ACTION_GET_CONTENT),
                        "Choose thumbnail for recipe..."),
                        23));
        findViewById(R.id.submit).setOnClickListener(view -> {
            Map<String, Object> data = new HashMap<>();
            if(thumbnailFilePath==null) Toast.makeText(this, "Upload Thumbnail", Toast.LENGTH_SHORT).show();
            else if(!checkBox.isChecked()&&iconFilePath==null) Toast.makeText(this, "Upload Icon", Toast.LENGTH_SHORT).show();
            else if(name.getText().toString().equals("")) name.setError(getString(R.string.required));
            else if(name_hi.getText().toString().equals("")) name_hi.setError(getString(R.string.required));
            else if (categorySpinner.getSelectedItem().equals(R.string.select_category)) categorySpinner.performClick();
            else if(time.getText().toString().equals("")) time.setError(getString(R.string.required));
            else if(energy.getText().toString().equals("")) energy.setError(getString(R.string.required));
            else if(serve.getText().toString().equals("")) serve.setError(getString(R.string.required));
            else if(desc.getText().toString().equals("")) desc.setError(getString(R.string.required));
            else if(desc_hi.getText().toString().equals("")) desc_hi.setError(getString(R.string.required));
            else if(ingredient.getText().toString().equals("")) ingredient.setError(getString(R.string.required));
            else if(ingredient_hi.getText().toString().equals("")) ingredient_hi.setError(getString(R.string.required));
            else if(instruction.getText().toString().equals("")) instruction.setError(getString(R.string.required));
            else if(instruction_hi.getText().toString().equals("")) instruction_hi.setError(getString(R.string.required));
            //else if(videoId.getText().toString().equals("")) videoId.setError(getString(R.string.required));
            else {
                ProgressDialog progressDialog=new ProgressDialog(this);
                progressDialog.setTitle(getString(R.string.please_wait));
                progressDialog.show();
                data.put("name", name.getText().toString());
                data.put("name-hi", name_hi.getText().toString());
                data.put("category", categorySpinner.getSelectedItem().toString());
                data.put("category-hi", categoryArr_hi.get(categoryArr.indexOf(categorySpinner.getSelectedItem().toString()) - 1));
                data.put("energy", energy.getText().toString());
                data.put("time", time.getText().toString());
                data.put("serve", serve.getText().toString());
                data.put("desc", desc.getText().toString());
                data.put("views",2);
                data.put("desc-hi", desc_hi.getText().toString());
                ArrayList<String> ingredientsArr = setting.stringToStringArray(ingredient.getText().toString());
                data.put("ingredient", ingredientsArr);
                ArrayList<String> ingredients_hiArr = setting.stringToStringArray(ingredient_hi.getText().toString());
                data.put("ingredient-hi", ingredients_hiArr);
                ArrayList<String> instructionsArr = setting.stringToStringArray(instruction.getText().toString());
                data.put("instruction", instructionsArr);
                ArrayList<String> instructions_hiArr = setting.stringToStringArray(instruction_hi.getText().toString());
                data.put("instruction-hi", instructions_hiArr);
                if(checkBox.isChecked()) data.put("iconId","thumbnail" + id);
                else data.put("iconId", "icon" + id);
                data.put("thumbnailId","thumbnail" + id);
                data.put("videoId",videoId.getText().toString());
                data.put("totalRatingNumber",5);
                data.put("ratedPeopleNumber",1);
                FirebaseFirestore.getInstance()
                        .collection("recipes")
                        .document()
                        .set(data).addOnSuccessListener(runnable -> {
                            progressDialog.dismiss();
                            setting.successDialog(this,this,getString(R.string.recipe_added));
                        });
                if (!checkBox.isChecked()&&iconFilePath != null) FirebaseStorage.getInstance()
                        .getReference()
                        .child("recipeImages/icon" + id)
                        .putFile(iconFilePath);
                if (thumbnailFilePath != null) FirebaseStorage.getInstance()
                        .getReference()
                        .child("recipeImages/thumbnail" + id)
                        .putFile(thumbnailFilePath);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 22 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            iconFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        iconFilePath);
                icon.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == 23 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            thumbnailFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        thumbnailFilePath);
                thumbnail.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}