package com.example.promoeatsandroid.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.Location;
import com.example.promoeatsandroid.models.RestaurantRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

public class LocationActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;

    private TextView tvAutoLocation, tvManualLocation;
    private EditText etAddress, etRange;
    private Button btnGetLocation, btnGoHome, btnGetLocationFromAddress, btnSearchRestaurants;
    private FusedLocationProviderClient fusedLocationClient;

    private Location autoLocation = null;
    private Location manualLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Inicjalizacja widoków
        tvAutoLocation = findViewById(R.id.tvAutoLocation);
        tvManualLocation = findViewById(R.id.tvManualLocation);
        etAddress = findViewById(R.id.etAddress);
        etRange = findViewById(R.id.etRange);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnGoHome = findViewById(R.id.btnGoHome);
        btnGetLocationFromAddress = findViewById(R.id.btnGetLocationFromAddress);
        btnSearchRestaurants = findViewById(R.id.btnSearchRestaurants);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Słuchacze przycisków
        btnGetLocation.setOnClickListener(v -> requestLocation());
        btnGetLocationFromAddress.setOnClickListener(v -> getLocationFromAddress());
        btnSearchRestaurants.setOnClickListener(v -> searchRestaurants());
        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(LocationActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
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
                        autoLocation = new Location(latitude, longitude);
                        tvAutoLocation.setText("Pobrano lokalizację!");
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

    private void getLocationFromAddress() {
        String address = etAddress.getText().toString();
        if (address.isEmpty()) {
            Toast.makeText(this, "Proszę wprowadzić adres", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocationName(address, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    manualLocation = new Location(latitude, longitude);

                    runOnUiThread(() -> {
                        tvManualLocation.setText("Znaleziono lokalizację");
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Nie znaleziono lokalizacji dla podanego adresu", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Błąd geokodowania: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void searchRestaurants() {
        // Wybierz lokalizację
        Location selectedLocation = null;
        if (autoLocation != null) {
            selectedLocation = autoLocation;
        } else if (manualLocation != null) {
            selectedLocation = manualLocation;
        } else {
            Toast.makeText(this, "Proszę wybrać lub wprowadzić lokalizację", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pobierz zakres
        String rangeStr = etRange.getText().toString();
        if (rangeStr.isEmpty()) {
            Toast.makeText(this, "Proszę wprowadzić zakres wyszukiwania", Toast.LENGTH_SHORT).show();
            return;
        }

        int range;
        try {
            range = Integer.parseInt(rangeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Zakres musi być liczbą całkowitą", Toast.LENGTH_SHORT).show();
            return;
        }

        // Utwórz obiekt RestaurantRequest
        RestaurantRequest restaurantRequest = new RestaurantRequest(selectedLocation, range);

        // Przekieruj do HomeActivity z obiektem RestaurantRequest
        Intent intent = new Intent(LocationActivity.this, HomeActivity.class);
        intent.putExtra("restaurantRequest", restaurantRequest);
        startActivity(intent);
        finish(); // Opcjonalnie zakończ tę aktywność
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Uprawnienie do lokalizacji jest wymagane", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
