package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.RestaurantWithPromotionsAdapter;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.models.RestaurantWithPromotions;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;
import com.example.promoeatsandroid.models.RestaurantRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ApiService apiService;
    private TokenManager tokenManager;

    private Button btnToggleFavourites;
    private RecyclerView rvRestaurants;

    private List<RestaurantWithPromotions> restaurantWithPromotionsList;
    private List<Restaurant> favouriteRestaurants;
    private RestaurantWithPromotionsAdapter adapter;

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
        btnToggleFavourites = findViewById(R.id.btnToggleFavourites);
        rvRestaurants = findViewById(R.id.rvRestaurantsWithPromotions);
        Button btnLocationActivity = findViewById(R.id.btnLocationActivity);

        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));

        // Inicjalizacja adaptera z pustą listą
        restaurantWithPromotionsList = new ArrayList<>();
        favouriteRestaurants = new ArrayList<>();
        adapter = new RestaurantWithPromotionsAdapter(this, restaurantWithPromotionsList, showingFavourites);
        rvRestaurants.setAdapter(adapter);

        // Odbierz obiekt RestaurantRequest z Intent
        RestaurantRequest restaurantRequest = (RestaurantRequest) getIntent().getSerializableExtra("restaurantRequest");
        if (restaurantRequest != null) {
            // Wyświetl obiekt dla testów
            Toast.makeText(this, restaurantRequest.toString(), Toast.LENGTH_LONG).show();
            // Możesz również wyświetlić w logach
            Log.d("HomeActivity", "Otrzymano RestaurantRequest: " + restaurantRequest);
        }

        // Słuchacze przycisków
        btnToggleFavourites.setOnClickListener(v -> toggleFavourites());
        btnLocationActivity.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
            startActivity(intent);
        });

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

    private boolean isFavourite(int restaurantId) {
        for (Restaurant fav : favouriteRestaurants) {
            if (fav.getId() == restaurantId) {
                return true;
            }
        }
        return false;
    }
}
