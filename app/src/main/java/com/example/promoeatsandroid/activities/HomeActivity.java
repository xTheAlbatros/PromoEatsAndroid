package com.example.promoeatsandroid.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.adapters.RestaurantWithPromotionsAdapter;
import com.example.promoeatsandroid.models.Category;
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
    private TextView tvNoLocationInfo;
    private Button btnLocationActivity;

    private List<RestaurantWithPromotions> restaurantWithPromotionsList;
    private List<Restaurant> favouriteRestaurants;
    private RestaurantWithPromotionsAdapter adapter;

    private boolean showingFavourites = false;

    private RestaurantRequest restaurantRequest; // obiekt przekazany z LocationActivity

    // Widoki do kategorii
    private TextView tvCategoriesHeader;
    private View llCategoriesContainer;
    private View hsvCategories;

    private List<String> selectedCategories = new ArrayList<>(); // wybrane kategorie

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

        // Widoki do kategorii
        tvCategoriesHeader = findViewById(R.id.tvCategoriesHeader);
        llCategoriesContainer = findViewById(R.id.llCategoriesContainer);
        hsvCategories = findViewById(R.id.hsvCategories);

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
            // Brak wybranej lokalizacji
            rvRestaurants.setVisibility(View.GONE);
            btnToggleFavourites.setVisibility(View.GONE);
            tvNoLocationInfo.setVisibility(View.VISIBLE);

            // Ukrywamy kategorię
            tvCategoriesHeader.setVisibility(View.GONE);
            hsvCategories.setVisibility(View.GONE);
        } else {
            // Jest lokalizacja
            tvNoLocationInfo.setVisibility(View.GONE);
            rvRestaurants.setVisibility(View.VISIBLE);
            btnToggleFavourites.setVisibility(View.VISIBLE);

            // Pokazujemy kategorię
            tvCategoriesHeader.setVisibility(View.VISIBLE);
            hsvCategories.setVisibility(View.VISIBLE);

            // Pobierz restauracje i ulubione
            fetchRestaurantsAndFavouritesWithLocation();

            // Pobierz kategorie
            fetchCategories();
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
        } else if (item.getItemId() == R.id.action_profile) { // Obsługa przejścia do profilu
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
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

        // Sprawdź czy mamy wybrane kategorie
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            // Jeśli kategorie są wybrane, używamy nowego endpointu
            apiService.getRestaurantRestaurantsByLocationAndCategories(token, latitude, longitude, range, selectedCategories)
                    .enqueue(new Callback<List<Restaurant>>() {
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
                                Toast.makeText(HomeActivity.this, "Brak restauracji z wybranymi kategoriami w podanej lokalizacji", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                            Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Brak kategorii - korzystamy ze starego endpointu
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
                        Toast.makeText(HomeActivity.this, "Brak restauracji w podanej lokalizacji", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                    Toast.makeText(HomeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
            // Po powrocie z ulubionych na stronę główną wczytujemy restauracje (z lokalizacją i ew. kategoriami)
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

    private void fetchCategories() {
        String token = "Bearer " + tokenManager.getToken();
        apiService.getAllCategories(token).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    displayCategories(categories);
                } else {
                    Toast.makeText(HomeActivity.this, "Nie udało się pobrać kategorii.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Błąd sieci podczas pobierania kategorii: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCategories(List<Category> categories) {
        if (!(llCategoriesContainer instanceof android.widget.LinearLayout)) return;

        android.widget.LinearLayout container = (android.widget.LinearLayout) llCategoriesContainer;
        container.removeAllViews(); // Wyczyść poprzednie przyciski, jeśli jakieś są

        for (Category cat : categories) {
            Button btnCat = new Button(this);
            btnCat.setText(cat.getName());
            btnCat.setAllCaps(false);
            btnCat.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            btnCat.setPadding(12, 12, 12, 12);

            // Domyślnie szary kolor przycisku (nie wybrany)
            btnCat.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray)));

            // Listener kliknięcia
            btnCat.setOnClickListener(v -> {
                String catName = cat.getName();
                if (selectedCategories.contains(catName)) {
                    // Usuwamy z listy wybranych
                    selectedCategories.remove(catName);
                    // Zmieniamy kolor na szary
                    btnCat.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray)));
                } else {
                    // Dodajemy do listy wybranych
                    selectedCategories.add(catName);
                    // Zmieniamy kolor na zielony
                    btnCat.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_light)));
                }

                // Za każdym razem gdy zmienia się lista kategorii, pobierz ponownie restauracje
                // (możemy to zrobić tu lub dodać osobny przycisk "Zastosuj filtr"; na razie zrobimy od razu)
                if (restaurantRequest != null) {
                    fetchRestaurantsWithLocation(true);
                }
            });

            container.addView(btnCat);
        }
    }
}
