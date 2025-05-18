package com.example.nyaritabor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Névjegy");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Visszanavigálás a bal felső nyíllal
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
