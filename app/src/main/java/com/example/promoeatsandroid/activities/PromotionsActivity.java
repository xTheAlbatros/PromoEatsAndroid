package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

        if (restaurantId == -1) {
            Toast.makeText(this, "Nieprawidłowe dane restauracji", Toast.LENGTH_SHORT).show();
            finish(); // Zakończ aktywność, jeśli brak danych restauracji
            return;
        }

        fetchPromotions(restaurantId);
    }

    private void fetchPromotions(int restaurantId) {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getPromotions(token, restaurantId).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Promotion> promotions = response.body();
                    if (promotions.isEmpty()) {
                        Toast.makeText(PromotionsActivity.this, "Brak promocji dla tej restauracji", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adapter = new PromotionAdapter(promotions, PromotionsActivity.this::fetchAndShowImages);
                    rvPromotions.setAdapter(adapter);
                } else {
                    showErrorToast("Nie udało się pobrać promocji. Spróbuj ponownie później.");
                }
            }

            @Override
            public void onFailure(Call<List<Promotion>> call, Throwable t) {
                showErrorToast("Błąd sieci: " + t.getMessage());
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

                    if (!images.isEmpty()) {
                        Intent intent = new Intent(PromotionsActivity.this, ImageViewerActivity.class);
                        ArrayList<String> imageUrls = extractPaths(images);
                        intent.putStringArrayListExtra("imageUrls", imageUrls);
                        startActivity(intent);
                    } else {
                        Toast.makeText(PromotionsActivity.this, "Brak zdjęć dla tej promocji", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showErrorToast("Nie udało się pobrać zdjęć.");
                }
            }

            @Override
            public void onFailure(Call<List<Images>> call, Throwable t) {
                showErrorToast("Błąd sieci: " + t.getMessage());
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

    private void showErrorToast(String message) {
        Toast.makeText(PromotionsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
