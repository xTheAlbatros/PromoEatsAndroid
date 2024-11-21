package com.example.promoeatsandroid.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.ReviewAdapter;
import com.example.promoeatsandroid.models.Review;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        int restaurantId = getIntent().getIntExtra("restaurantId", -1);
        fetchReviews(restaurantId);
    }

    private void fetchReviews(int restaurantId) {
        TokenManager tokenManager = new TokenManager(this);
        String token = "Bearer " + tokenManager.getToken();

        apiService.getReviews(token, restaurantId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new ReviewAdapter(response.body());
                    rvReviews.setAdapter(adapter);
                } else {
                    Toast.makeText(ReviewsActivity.this, "Nie udało się pobrać opinii", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Toast.makeText(ReviewsActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
