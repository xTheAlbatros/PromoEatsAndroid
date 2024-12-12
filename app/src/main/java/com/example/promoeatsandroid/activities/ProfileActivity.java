package com.example.promoeatsandroid.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.utils.TokenManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);

        // Obsługa powrotu
        btnBack.setOnClickListener(v -> finish());

        // Dane użytkownika (można rozwinąć o pobieranie danych z serwera)
        TokenManager tokenManager = new TokenManager(this);

        // Przykładowe dane
        tvUserName.setText("Użytkownik Demo");
        tvUserEmail.setText("demo@example.com");

        // Ustawienie domyślnego avatara (można dodać logikę pobierania avatara z serwera)
        ivAvatar.setImageResource(R.drawable.ic_avatar);
    }
}
