package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.ImageAdapter;


import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {

    private RecyclerView rvImages;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        rvImages = findViewById(R.id.rvImages);
        btnClose = findViewById(R.id.btnClose);

        // Odbierz listę obrazów z Intent
        Intent intent = getIntent();
        List<String> imageUrls = intent.getStringArrayListExtra("imageUrls");
        Log.d("ImageViewerActivity", "Otrzymane URL zdjęć: " + imageUrls);

        if (imageUrls == null || imageUrls.isEmpty()) {
            Toast.makeText(this, "Brak zdjęć do wyświetlenia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Skonfiguruj RecyclerView
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ImageAdapter adapter = new ImageAdapter(this, imageUrls);
        rvImages.setAdapter(adapter);

        // Zamknij aktywność
        btnClose.setOnClickListener(v -> finish());
    }

}
