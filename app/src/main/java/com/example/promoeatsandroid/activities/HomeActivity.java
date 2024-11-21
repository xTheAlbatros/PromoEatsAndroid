package com.example.promoeatsandroid.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.example.promoeatsandroid.models.Promotion;
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
    private Button btnGetLocation;
    private RecyclerView rvRestaurantsWithPromotions;

    private FusedLocationProviderClient fusedLocationClient;
    private List<RestaurantWithPromotions> restaurantData;
    private RestaurantWithPromotionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(getApplicationContext());

        tvLocation = findViewById(R.id.tvLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        rvRestaurantsWithPromotions = findViewById(R.id.rvRestaurantsWithPromotions);

        rvRestaurantsWithPromotions.setLayoutManager(new LinearLayoutManager(this));
        restaurantData = new ArrayList<>();
        adapter = new RestaurantWithPromotionsAdapter(this, restaurantData);
        rvRestaurantsWithPromotions.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGetLocation.setOnClickListener(view -> requestLocation());

        fetchRestaurants();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
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
                tokenManager.clearToken();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    tvLocation.setText("Twoja lokalizacja:\nLat: " + latitude + ", Lon: " + longitude);
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się uzyskać lokalizacji", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchRestaurants() {
        String token = "Bearer " + tokenManager.getToken();

        apiService.getRestaurants(token).enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = response.body();
                    fetchPromotionsForRestaurants(restaurants);
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się załadować restauracji: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPromotionsForRestaurants(List<Restaurant> restaurants) {
        String token = "Bearer " + tokenManager.getToken();

        for (Restaurant restaurant : restaurants) {
            apiService.getPromotions(token, restaurant.getId()).enqueue(new Callback<List<Promotion>>() {
                @Override
                public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        restaurantData.add(new RestaurantWithPromotions(restaurant, response.body()));
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<Promotion>> call, Throwable t) {
                    Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Uprawnienie do lokalizacji jest wymagane", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
