package com.example.promoeatsandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.activities.AddReviewActivity;
import com.example.promoeatsandroid.activities.PromotionsActivity;
import com.example.promoeatsandroid.activities.ReviewsActivity;
import com.example.promoeatsandroid.models.RestaurantWithPromotions;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantWithPromotionsAdapter extends RecyclerView.Adapter<RestaurantWithPromotionsAdapter.RestaurantViewHolder> {

    private List<RestaurantWithPromotions> data;
    private Context context;
    private boolean showingFavourites;
    private ApiService apiService;
    private TokenManager tokenManager;

    public RestaurantWithPromotionsAdapter(Context context, List<RestaurantWithPromotions> data, boolean showingFavourites) {
        this.context = context;
        this.data = data;
        this.showingFavourites = showingFavourites;
        this.apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        this.tokenManager = new TokenManager(context);
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        RestaurantWithPromotions restaurantWithPromotions = data.get(position);

        holder.tvRestaurantName.setText(restaurantWithPromotions.getRestaurant().getName());
        holder.tvRestaurantDetails.setText("Telefon: " + restaurantWithPromotions.getRestaurant().getPhone());
        holder.tvRestaurantWebsite.setText("Strona: " + restaurantWithPromotions.getRestaurant().getWebsite());

//        if (restaurantWithPromotions.getRestaurant().getLocation() != null) {
//            double latitude = restaurantWithPromotions.getRestaurant().getLocation().getLatitude();
//            double longitude = restaurantWithPromotions.getRestaurant().getLocation().getLongitude();
//            holder.tvRestaurantCoordinates.setText("Lat: " + latitude + ", Lon: " + longitude);
//        } else {
//            holder.tvRestaurantCoordinates.setText("Brak danych lokalizacji");
//        }

        holder.itemView.setOnClickListener(v -> {
            boolean expanded = restaurantWithPromotions.getRestaurant().isExpanded();
            restaurantWithPromotions.getRestaurant().setExpanded(!expanded);
            holder.btnContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        });

        holder.btnShowPromotions.setOnClickListener(v -> {
            Intent intent = new Intent(context, PromotionsActivity.class);
            intent.putExtra("restaurantId", restaurantWithPromotions.getRestaurant().getId());
            context.startActivity(intent);
        });

        holder.btnShowReviews.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewsActivity.class);
            intent.putExtra("restaurantId", restaurantWithPromotions.getRestaurant().getId());
            context.startActivity(intent);
        });

        holder.btnAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddReviewActivity.class);
            intent.putExtra("restaurantId", restaurantWithPromotions.getRestaurant().getId());
            context.startActivity(intent);
        });

        // Ustaw ikonę serca na podstawie stanu ulubionych
        updateFavouriteIcon(holder, restaurantWithPromotions);

        holder.btnFavourite.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            RestaurantWithPromotions currentRestaurant = data.get(currentPosition);
            String token = "Bearer " + tokenManager.getToken();

            boolean isCurrentlyFavourite = currentRestaurant.getRestaurant().isFavourite();
            currentRestaurant.getRestaurant().setFavourite(!isCurrentlyFavourite);

            if (isCurrentlyFavourite) {
                apiService.deleteFavourite(token, currentRestaurant.getRestaurant().getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("Adapter", "Usunięto z ulubionych: " + currentRestaurant.getRestaurant().getName());
                            if (showingFavourites) {
                                data.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                            } else {
                                updateFavouriteIcon(holder, currentRestaurant);
                            }
                        } else {
                            przywrocStan(holder, currentRestaurant, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        przywrocStan(holder, currentRestaurant, true);
                    }
                });
            } else {
                apiService.addFavourite(token, currentRestaurant.getRestaurant().getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("Adapter", "Dodano do ulubionych: " + currentRestaurant.getRestaurant().getName());
                            updateFavouriteIcon(holder, currentRestaurant);
                        } else {
                            przywrocStan(holder, currentRestaurant, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        przywrocStan(holder, currentRestaurant, false);
                    }
                });
            }
        });
    }

    private void updateFavouriteIcon(RestaurantViewHolder holder, RestaurantWithPromotions restaurant) {
        boolean isFavourite = restaurant.getRestaurant().isFavourite();
        holder.btnFavourite.setImageResource(isFavourite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void przywrocStan(RestaurantViewHolder holder, RestaurantWithPromotions restaurant, boolean wasFavourite) {
        restaurant.getRestaurant().setFavourite(wasFavourite);
        updateFavouriteIcon(holder, restaurant);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<RestaurantWithPromotions> newData) {
        // Zachowaj aktualny stan ulubionych
        for (RestaurantWithPromotions newRestaurant : newData) {
            for (RestaurantWithPromotions oldRestaurant : data) {
                if (newRestaurant.getRestaurant().getId() == oldRestaurant.getRestaurant().getId()) {
                    newRestaurant.getRestaurant().setFavourite(oldRestaurant.getRestaurant().isFavourite());
                }
            }
        }

        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvRestaurantDetails, tvRestaurantWebsite, tvRestaurantCoordinates;
        LinearLayout btnContainer;
        Button btnShowPromotions, btnShowReviews, btnAddReview;
        ImageButton btnFavourite;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantDetails = itemView.findViewById(R.id.tvRestaurantDetails);
            tvRestaurantWebsite = itemView.findViewById(R.id.tvRestaurantWebsite);
            //tvRestaurantCoordinates = itemView.findViewById(R.id.tvRestaurantCoordinates);
            btnContainer = itemView.findViewById(R.id.btnContainer);
            btnShowPromotions = itemView.findViewById(R.id.btnShowPromotions);
            btnShowReviews = itemView.findViewById(R.id.btnShowReviews);
            btnAddReview = itemView.findViewById(R.id.btnAddReview);
            btnFavourite = itemView.findViewById(R.id.btnFavourite);
        }
    }
}
