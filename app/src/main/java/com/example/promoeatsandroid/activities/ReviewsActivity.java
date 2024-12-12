package com.example.promoeatsandroid.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsActivity extends AppCompatActivity {

    private static final String TAG = "ReviewsActivity";
    private RecyclerView rvReviews;
    private ReviewAdapter adapter;
    private ApiService apiService;
    private TextView tvAverageRating, tvNoReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        rvReviews = findViewById(R.id.rvReviews);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        ImageView btnBack = findViewById(R.id.btnBack);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        int restaurantId = getIntent().getIntExtra("restaurantId", -1);
        Log.d(TAG, "Restaurant ID: " + restaurantId); // Debugging restaurant ID
        fetchReviews(restaurantId);

        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchReviews(int restaurantId) {
        TokenManager tokenManager = new TokenManager(this);
        String token = "Bearer " + tokenManager.getToken();

        apiService.getReviews(token, restaurantId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();

                    // Log received reviews
                    for (Review review : reviews) {
                        Log.d(TAG, "Review: Rate=" + review.getRate() +
                                ", Comment=" + review.getComment() +
                                ", Date=" + review.getCreatedTime());
                    }

                    if (reviews.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        tvAverageRating.setVisibility(View.GONE);
                        Log.d(TAG, "No reviews available for this restaurant."); // Debugging empty reviews
                    } else {
                        tvNoReviews.setVisibility(View.GONE);

                        // Sort reviews by date (newest first)
                        sortReviewsByDate(reviews);

                        adapter = new ReviewAdapter(reviews);
                        rvReviews.setAdapter(adapter);

                        double average = calculateAverageRating(reviews);
                        tvAverageRating.setVisibility(View.VISIBLE);
                        tvAverageRating.setText("Średnia ocena: " + String.format("%.1f", average));

                        Log.d(TAG, "Average Rating: " + average); // Debugging average rating
                    }
                } else {
                    Toast.makeText(ReviewsActivity.this, "Nie udało się pobrać opinii", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to fetch reviews. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Toast.makeText(ReviewsActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
            }
        });
    }

    private double calculateAverageRating(List<Review> reviews) {
        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRate();
        }
        return sum / reviews.size();
    }

    private void sortReviewsByDate(List<Review> reviews) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Collections.sort(reviews, new Comparator<Review>() {
            @Override
            public int compare(Review r1, Review r2) {
                try {
                    Date date1 = format.parse(r1.getCreatedTime());
                    Date date2 = format.parse(r2.getCreatedTime());
                    return date2.compareTo(date1); // Sortowanie malejące (najnowsze na górze)
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
    }
}
