package com.example.promoeatsandroid.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.RestaurantWithPromotionsAdapter;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.models.RestaurantWithPromotions;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;

    private Toolbar toolbar;
    private ApiService apiService;
    private TokenManager tokenManager;

    private TextView tvLocation;
    private Button btnGetLocation, btnToggleFavourites;
    private RecyclerView rvRestaurants;

    private List<RestaurantWithPromotions> restaurantWithPromotionsList;
    private List<Restaurant> favouriteRestaurants;
    private RestaurantWithPromotionsAdapter adapter;

    private FusedLocationProviderClient fusedLocationClient;
    private boolean showingFavourites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Konfiguracja toolbaru i API
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(getApplicationContext());

        // Inicjalizacja widoków
        tvLocation = findViewById(R.id.tvLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnToggleFavourites = findViewById(R.id.btnToggleFavourites);
        rvRestaurants = findViewById(R.id.rvRestaurantsWithPromotions);

        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));

        // Inicjalizacja adaptera z pustą listą
        restaurantWithPromotionsList = new ArrayList<>();
        favouriteRestaurants = new ArrayList<>();
        adapter = new RestaurantWithPromotionsAdapter(this, restaurantWithPromotionsList, showingFavourites);
        rvRestaurants.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Słuchacze przycisków
        btnGetLocation.setOnClickListener(v -> requestLocation());
        btnToggleFavourites.setOnClickListener(v -> toggleFavourites());

        // Pobierz ulubione i restauracje
        fetchFavourites(() -> fetchRestaurants());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu); // Menu wylogowania
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        String token = "Bearer " + tokenManager.getToken();
        apiService.logout(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    tokenManager.clearToken();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się wylogować.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci podczas wylogowywania: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRestaurants() {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getRestaurants(token).enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RestaurantWithPromotions> newData = new ArrayList<>();
                    for (Restaurant restaurant : response.body()) {
                        restaurant.setFavourite(isFavourite(restaurant.getId()));
                        newData.add(new RestaurantWithPromotions(restaurant, new ArrayList<>()));
                    }
                    adapter.updateData(newData);
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się załadować restauracji.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFavourites(Runnable callback) {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getFavourites(token).enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favouriteRestaurants.clear();
                    favouriteRestaurants.addAll(response.body());
                    if (callback != null) callback.run();
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się załadować ulubionych restauracji.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavourites() {
        showingFavourites = !showingFavourites;
        btnToggleFavourites.setText(showingFavourites ? "Strona Główna" : "Ulubione");
        if (showingFavourites) {
            fetchFavourites(() -> {
                List<RestaurantWithPromotions> favourites = new ArrayList<>();
                for (Restaurant fav : favouriteRestaurants) {
                    favourites.add(new RestaurantWithPromotions(fav, new ArrayList<>()));
                }
                adapter.updateData(favourites);
            });
        } else {
            fetchRestaurants();
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        tvLocation.setText("Twoja lokalizacja:\nLat: " + latitude + ", Lon: " + longitude);
                    } else {
                        Toast.makeText(this, "Nie udało się uzyskać lokalizacji", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd pobierania lokalizacji: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "Brak uprawnień do lokalizacji.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Błąd bezpieczeństwa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Wywołanie metody nadrzędnej
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Uprawnienie do lokalizacji jest wymagane", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private boolean isFavourite(int restaurantId) {
        for (Restaurant fav : favouriteRestaurants) {
            if (fav.getId() == restaurantId) {
                return true;
            }
        }
        return false;
    }
}
