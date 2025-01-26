package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.User;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ApiService apiService;
    private TokenManager tokenManager;

    private TextView tvName, tvEmail;
    private ImageView btnBack;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicjalizacja widoków
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnBack = findViewById(R.id.btnBack);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(this);

        // Obsługa przycisku powrotu
        btnBack.setOnClickListener(v -> finish());

        // Obsługa przycisku resetowania hasła
        btnResetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // Pobierz dane użytkownika
        fetchUserData();
    }

    private void fetchUserData() {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getUserByToken(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvName.setText(user.getName() + " " + user.getSurname());
                    tvEmail.setText(user.getEmail());
                } else {
                    Toast.makeText(ProfileActivity.this, "Nie udało się pobrać danych użytkownika", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
            }
        });
    }
}
