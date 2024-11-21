package com.example.promoeatsandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.activities.PromotionsActivity;
import com.example.promoeatsandroid.activities.ReviewsActivity;
import com.example.promoeatsandroid.models.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurantList;
    private Context context;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.tvRestaurantName.setText(restaurant.getName());
        holder.tvRestaurantDetails.setText("Phone: " + restaurant.getPhone() + " | Email: " + restaurant.getEmail());

        if (restaurant.getLocation() != null) {
            String coordinates = "Lat: " + restaurant.getLocation().getLatitude() +
                    ", Lon: " + restaurant.getLocation().getLongitude();
            holder.tvRestaurantCoordinates.setText(coordinates);
        } else {
            holder.tvRestaurantCoordinates.setText("Brak lokalizacji");
        }

        // Obsługa kliknięcia na nazwę restauracji, aby pokazać/ukryć przyciski
        holder.itemView.setOnClickListener(v -> {
            if (holder.btnContainer.getVisibility() == View.GONE) {
                holder.btnContainer.setVisibility(View.VISIBLE);
            } else {
                holder.btnContainer.setVisibility(View.GONE);
            }
        });

        // Obsługa przycisku "Pokaż promocje"
        holder.btnShowPromotions.setOnClickListener(v -> {
            Intent intent = new Intent(context, PromotionsActivity.class);
            intent.putExtra("restaurantId", restaurant.getId());
            context.startActivity(intent);
        });

        // Obsługa przycisku "Pokaż opinie"
        holder.btnShowReviews.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewsActivity.class);
            intent.putExtra("restaurantId", restaurant.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvRestaurantDetails, tvRestaurantCoordinates;
        LinearLayout btnContainer;
        Button btnShowPromotions, btnShowReviews;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantDetails = itemView.findViewById(R.id.tvRestaurantDetails);
            tvRestaurantCoordinates = itemView.findViewById(R.id.tvRestaurantCoordinates);
            btnContainer = itemView.findViewById(R.id.btnContainer);
            btnShowPromotions = itemView.findViewById(R.id.btnShowPromotions);
            btnShowReviews = itemView.findViewById(R.id.btnShowReviews);
        }
    }
}
