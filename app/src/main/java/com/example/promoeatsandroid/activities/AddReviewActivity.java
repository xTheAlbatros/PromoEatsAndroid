package com.example.promoeatsandroid.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.models.Review;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReviewActivity extends AppCompatActivity {

    private static final String TAG = "AddReviewActivity";

    private EditText etRate, etComment;
    private Button btnSubmitReview;
    private ApiService apiService;
    private TokenManager tokenManager;
    private int restaurantId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AddReviewActivity started.");
        setContentView(R.layout.activity_add_review);

        // Inicjalizacja widoków
        etRate = findViewById(R.id.etRate);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        Log.d(TAG, "Views initialized successfully.");

        // Inicjalizacja API i tokenu
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(this);

        // Pobierz ID restauracji z Intent
        restaurantId = getIntent().getIntExtra("restaurantId", -1);
        Log.d(TAG, "Received restaurantId: " + restaurantId);

        if (restaurantId == -1) {
            Log.e(TAG, "Invalid restaurantId received. Finishing activity.");
            Toast.makeText(this, "Brak ID restauracji!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obsługa kliknięcia przycisku dodania opinii
        btnSubmitReview.setOnClickListener(v -> {
            Log.d(TAG, "Submit review button clicked.");
            submitReview();
        });
    }

    private void submitReview() {
        Log.d(TAG, "Submitting review...");

        // Pobierz token
        String token = "Bearer " + tokenManager.getToken();
        Log.d(TAG, "Token: " + token);

        // Pobierz dane z pól
        String rateText = etRate.getText().toString();
        String comment = etComment.getText().toString();

        // Walidacja danych wejściowych
        if (TextUtils.isEmpty(rateText) || TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Wszystkie pola są wymagane!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Empty fields");
            return;
        }

        int rate;
        try {
            rate = Integer.parseInt(rateText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ocena musi być liczbą!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Invalid rate input " + rateText);
            return;
        }

        Log.d(TAG, "Rate: " + rate + ", Comment: " + comment + ", RestaurantId: " + restaurantId);

        // Utwórz obiekt Restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        // Utwórz obiekt Review
        Review review = new Review(rate, comment, restaurant);
        Gson gson = new Gson();
        String reviewJson = gson.toJson(review);
        Log.d(TAG, "Review JSON to be sent: " + reviewJson);

        // Wyślij opinię do API
        apiService.addReview(token, review).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "API Response: " + response.toString());
                if (response.isSuccessful()) {
                    Toast.makeText(AddReviewActivity.this, "Opinia została dodana!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Review added successfully.");
                    finish();
                } else {
                    // Log szczegółów odpowiedzi
                    Log.e(TAG, "API Error: Response code " + response.code() + ", message: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    Toast.makeText(AddReviewActivity.this, "Nie udało się dodać opinii. Kod: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddReviewActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network Error: " + t.getMessage());
            }
        });
    }

}
