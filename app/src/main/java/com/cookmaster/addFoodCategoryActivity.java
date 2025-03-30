package com.cookmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class addFoodCategoryActivity extends AppCompatActivity {
    final int PICK_IMAGE_REQUEST=22;
    Uri filePath;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_category);
        String id=setting.getTime("yyyyMMddHHmmssMS");
        EditText name=findViewById(R.id.editText),
                name_hi=findViewById(R.id.editText2),
                desc=findViewById(R.id.editText3),
                desc_hi=findViewById(R.id.editText4);
        imageView=findViewById(R.id.imageView);
        findViewById(R.id.selectImg).setOnClickListener(view ->
            startActivityForResult(Intent.createChooser(
                    new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT),
                    "Select Image from here..."),
                    22));
        findViewById(R.id.submit).setOnClickListener(view -> {
            if (filePath==null) Toast.makeText(this, "upload Icon", Toast.LENGTH_SHORT).show();
            else if (name.getText().toString().isEmpty()) name.setError("Required");
            else if (name_hi.getText().toString().isEmpty()) name_hi.setError("Required");
            else if (desc.getText().toString().isEmpty()) desc.setError("Required");
            else if (desc_hi.getText().toString().isEmpty()) desc_hi.setError("Required");
            else {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Please wait...");
                progressDialog.show();
                Map<String, String> data = new HashMap<>();
                data.put("docId-hi", name_hi.getText().toString());
                data.put("desc", desc.getText().toString());
                data.put("desc-hi", desc_hi.getText().toString());
                data.put("imgId", "cat" + id);
                FirebaseFirestore.getInstance()
                        .collection("foodCategories")
                        .document(name.getText().toString())
                        .set(data).addOnSuccessListener(runnable -> {
                            progressDialog.dismiss();
                            setting.successDialog(this, this, "Category Added");
                        });
                if (filePath != null) FirebaseStorage.getInstance()
                        .getReference()
                        .child("uploadedDocsForLeave/cat" + id)
                        .putFile(filePath);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}