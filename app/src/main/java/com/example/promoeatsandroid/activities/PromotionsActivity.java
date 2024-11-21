package com.example.promoeatsandroid.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.PromotionAdapter;
import com.example.promoeatsandroid.models.Promotion;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionsActivity extends AppCompatActivity {

    private RecyclerView rvPromotions;
    private PromotionAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);

        rvPromotions = findViewById(R.id.rvPromotions);
        rvPromotions.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        int restaurantId = getIntent().getIntExtra("restaurantId", -1);
        fetchPromotions(restaurantId);
    }

    private void fetchPromotions(int restaurantId) {
        // Tworzenie instancji TokenManager
        TokenManager tokenManager = new TokenManager(this);
        String token = "Bearer " + tokenManager.getToken();

        apiService.getPromotions(token, restaurantId).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new PromotionAdapter(response.body());
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
}
