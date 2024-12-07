package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;

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
    private TextView tvNoLocationInfo; // Tekst informujący o konieczności ustawienia lokalizacji
    private Button btnLocationActivity; // Przycisk do przejścia do ustalania lokalizacji

    private List<RestaurantWithPromotions> restaurantWithPromotionsList;
    private List<Restaurant> favouriteRestaurants;
    private RestaurantWithPromotionsAdapter adapter;

    private boolean showingFavourites = false;

    private RestaurantRequest restaurantRequest; // obiekt przekazany z LocationActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Konfiguracja Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView toolbarLogo = findViewById(R.id.toolbarLogo);
        toolbarLogo.setImageResource(R.drawable.ic_promo_eats_logo);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(getApplicationContext());

        // Inicjalizacja widoków
        btnToggleFavourites = findViewById(R.id.btnToggleFavourites);
        rvRestaurants = findViewById(R.id.rvRestaurantsWithPromotions);
        btnLocationActivity = findViewById(R.id.btnLocationActivity);
        tvNoLocationInfo = findViewById(R.id.tvNoLocationInfo);

        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));

        // Inicjalizacja adaptera z pustą listą
        restaurantWithPromotionsList = new ArrayList<>();
        favouriteRestaurants = new ArrayList<>();
        adapter = new RestaurantWithPromotionsAdapter(this, restaurantWithPromotionsList, showingFavourites);
        rvRestaurants.setAdapter(adapter);

        // Odbierz obiekt RestaurantRequest z Intent
        restaurantRequest = (RestaurantRequest) getIntent().getSerializableExtra("restaurantRequest");

        // Słuchacze przycisków
        btnToggleFavourites.setOnClickListener(v -> toggleFavourites());
        btnLocationActivity.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        // Sprawdzamy czy mamy RestaurantRequest
        if (restaurantRequest == null) {
            // Brak wybranej lokalizacji - wyświetlamy info i ukrywamy listę
            rvRestaurants.setVisibility(View.GONE);
            btnToggleFavourites.setVisibility(View.GONE);
            tvNoLocationInfo.setVisibility(View.VISIBLE);
        } else {
            // Jest lokalizacja - pobieramy restauracje z uwzględnieniem lokalizacji
            tvNoLocationInfo.setVisibility(View.GONE);
            rvRestaurants.setVisibility(View.VISIBLE);
            btnToggleFavourites.setVisibility(View.VISIBLE);

            if (restaurantRequest != null) {
                Toast.makeText(this, restaurantRequest.toString(), Toast.LENGTH_LONG).show();
                Log.d("HomeActivity", "Otrzymano RestaurantRequest: " + restaurantRequest);
            }

            // Pobierz restauracje i ulubione
            fetchRestaurantsAndFavouritesWithLocation();
        }
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

    private void fetchRestaurantsAndFavouritesWithLocation() {
        fetchFavourites(() -> fetchRestaurantsWithLocation(true));
    }

    private void fetchRestaurantsWithLocation(boolean updateFavourites) {
        if (restaurantRequest == null) return;

        String token = "Bearer " + tokenManager.getToken();
        double latitude = restaurantRequest.getLocation().getLatitude();
        double longitude = restaurantRequest.getLocation().getLongitude();
        int range = restaurantRequest.getRange();

        apiService.getRestaurantRestaurantsByLocation(token, latitude, longitude, range).enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RestaurantWithPromotions> newData = new ArrayList<>();
                    for (Restaurant restaurant : response.body()) {
                        if (updateFavourites) {
                            restaurant.setFavourite(isFavourite(restaurant.getId()));
                        }
                        // Tworzymy obiekt RestaurantWithPromotions z pustą listą promocji
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
                } else {
                    favouriteRestaurants.clear();
                }
                if (callback != null) callback.run();
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci podczas ładowania ulubionych: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (callback != null) callback.run();
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
            // Po powrocie z ulubionych na stronę główną wczytujemy restauracje z lokalizacją
            if (restaurantRequest != null) {
                fetchRestaurantsWithLocation(false);
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
