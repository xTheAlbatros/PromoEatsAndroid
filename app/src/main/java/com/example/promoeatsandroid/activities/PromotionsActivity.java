package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.PromotionAdapter;
import com.example.promoeatsandroid.models.Promotion;
import com.example.promoeatsandroid.models.Images;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionsActivity extends AppCompatActivity {

    private RecyclerView rvPromotions;
    private PromotionAdapter adapter;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);

        rvPromotions = findViewById(R.id.rvPromotions);
        rvPromotions.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(this);

        int restaurantId = getIntent().getIntExtra("restaurantId", -1);
        fetchPromotions(restaurantId);
    }

    private void fetchPromotions(int restaurantId) {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getPromotions(token, restaurantId).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Promotion> promotions = response.body();
                    adapter = new PromotionAdapter(promotions, PromotionsActivity.this::fetchAndShowImages);
                    rvPromotions.setAdapter(adapter);
                } else {
                    Toast.makeText(PromotionsActivity.this, "Nie udało się pobrać promocji", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Promotion>> call, Throwable t) {
                Toast.makeText(PromotionsActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndShowImages(int promotionId) {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getImagesForPromotion(token, promotionId).enqueue(new Callback<List<Images>>() {
            @Override
            public void onResponse(Call<List<Images>> call, Response<List<Images>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Images> images = response.body();
                    Log.d("PromotionsActivity", "Liczba zdjęć: " + images.size());
                    for (Images image : images) {
                        Log.d("PromotionsActivity", "Zdjęcie: " + image.getPath());
                    }
                    if (!images.isEmpty()) {
                        Intent intent = new Intent(PromotionsActivity.this, ImageViewerActivity.class);
                        ArrayList<String> imageUrls = extractPaths(images);
                        intent.putStringArrayListExtra("imageUrls", imageUrls);
                        startActivity(intent);
                    } else {
                        Toast.makeText(PromotionsActivity.this, "Brak zdjęć dla tej promocji", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("PromotionsActivity", "Nieudane żądanie: " + response.errorBody());
                    Toast.makeText(PromotionsActivity.this, "Błąd pobierania zdjęć", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Images>> call, Throwable t) {
                Log.e("PromotionsActivity", "Błąd sieci: " + t.getMessage());
                Toast.makeText(PromotionsActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayList<String> extractPaths(List<Images> images) {
        ArrayList<String> paths = new ArrayList<>();
        for (Images image : images) {
            paths.add(image.getPath());
        }
        return paths;
    }

}
