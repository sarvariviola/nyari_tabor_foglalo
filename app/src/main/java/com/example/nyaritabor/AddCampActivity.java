package com.example.nyaritabor;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddCampActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AddCampActivity";

    private EditText etName, etPrice, etPlace, etDescription,  etRating, etDate, etAgeGroup;
    private Button btnSave;

    private FirebaseFirestore mFirestore;
    private CollectionReference campsRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camp);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etPlace = findViewById(R.id.etPlace);
        etDescription = findViewById(R.id.etDescription);
        etRating = findViewById(R.id.etRating);
        etDate = findViewById(R.id.etDate);
        etAgeGroup = findViewById(R.id.etAgeGroup);
        btnSave = findViewById(R.id.btnSave);

        mFirestore = FirebaseFirestore.getInstance();
        campsRef = mFirestore.collection("Camps");

        btnSave.setOnClickListener(v -> saveCamp());
    }

    private void saveCamp() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String place = etPlace.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        String ratingStr = etRating.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String ageGroup = etAgeGroup.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(place) ||
                TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(ratingStr) || TextUtils.isEmpty(date) || TextUtils.isEmpty(ageGroup)) {
            Toast.makeText(this, "Kérlek tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        float rating;

        try {
            price = Integer.parseInt(priceStr);
            rating = Float.parseFloat(ratingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ár, szabad hely vagy értékelés formátuma nem megfelelő!", Toast.LENGTH_SHORT).show();
            return;
        }

        BookingCamp newCamp = new BookingCamp(
                0,          // imageResource - most nincs kép, 0 az alapértelmezett
                description,
                name,
                price,
                rating,
                ageGroup,
                date,
                150,
                place
        );

        campsRef.add(newCamp)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tábor sikeresen hozzáadva!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Hiba tábor hozzáadásakor: " + e.getMessage());
                    Toast.makeText(this, "Hiba történt a tábor hozzáadásakor.", Toast.LENGTH_SHORT).show();
                });
    }
}
