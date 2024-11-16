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
import com.example.promoeatsandroid.adapters.RestaurantAdapter;
import com.example.promoeatsandroid.models.Location;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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
    private FusedLocationProviderClient fusedLocationClient;

    private RecyclerView rvRestaurants; // RecyclerView for displaying restaurants

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        tokenManager = new TokenManager(getApplicationContext());

        // Initialize UI elements for location
        tvLocation = findViewById(R.id.tvLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);

        // Initialize RecyclerView
        rvRestaurants = findViewById(R.id.rvRestaurants);
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGetLocation.setOnClickListener(view -> requestLocation());

        // Fetch and display restaurants
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
        Call<Void> call = apiService.logout(token);
        call.enqueue(new Callback<Void>() {
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
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Location userLocation = new Location(latitude, longitude);
                                tvLocation.setText(userLocation.toString());
                            } else {
                                Toast.makeText(HomeActivity.this, "Nie udało się uzyskać lokalizacji", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void fetchRestaurants() {
        // Fetch restaurants from the API
        String token = "Bearer " + tokenManager.getToken(); // Pobierz token z TokenManager
        apiService.getRestaurants(token).enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = response.body();
                    RestaurantAdapter adapter = new RestaurantAdapter(restaurants);
                    rvRestaurants.setAdapter(adapter);
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się załadować restauracji: " + response.code(), Toast.LENGTH_SHORT).show();
                    System.out.println("Błąd: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
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
