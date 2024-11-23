package com.example.promoeatsandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.promoeatsandroid.models.Promotion;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.activities.AddReviewActivity;
import com.example.promoeatsandroid.activities.PromotionsActivity;
import com.example.promoeatsandroid.activities.ReviewsActivity;
import com.example.promoeatsandroid.models.Restaurant;
import com.example.promoeatsandroid.models.RestaurantWithPromotions;

import java.util.List;
import android.util.Log;
import android.app.Activity;

public class RestaurantWithPromotionsAdapter extends RecyclerView.Adapter<RestaurantWithPromotionsAdapter.RestaurantViewHolder> {

    private List<RestaurantWithPromotions> data;
    private Context context;

    public RestaurantWithPromotionsAdapter(Context context, List<RestaurantWithPromotions> data) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be an instance of Activity");
        }
        this.context = context;
        this.data = data;
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
        Restaurant restaurant = restaurantWithPromotions.getRestaurant();

        holder.tvRestaurantName.setText(restaurant.getName());
        holder.tvRestaurantDetails.setText("Phone: " + restaurant.getPhone() + " | Email: " + restaurant.getEmail());
        holder.tvRestaurantWebsite.setText("Website: " + restaurant.getWebsite());

        if (restaurant.getLocation() != null) {
            String coordinates = "Lat: " + restaurant.getLocation().getLatitude() +
                    ", Lon: " + restaurant.getLocation().getLongitude();
            holder.tvRestaurantCoordinates.setText(coordinates);
        } else {
            holder.tvRestaurantCoordinates.setText("Brak lokalizacji");
        }

        holder.itemView.setOnClickListener(v -> {
            boolean expanded = restaurant.isExpanded();
            restaurant.setExpanded(!expanded);
            holder.btnContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        });

        holder.btnShowPromotions.setOnClickListener(v -> {
            Intent intent = new Intent(context, PromotionsActivity.class);
            intent.putExtra("restaurantId", restaurant.getId());
            context.startActivity(intent);
        });

        holder.btnShowReviews.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewsActivity.class);
            intent.putExtra("restaurantId", restaurant.getId());
            context.startActivity(intent);
        });

        holder.btnAddReview.setOnClickListener(v -> {
            Log.d("RestaurantAdapter", "Add Review button clicked for restaurantId: " + restaurant.getId());
            try {
                Intent intent = new Intent(context, AddReviewActivity.class);
                intent.putExtra("restaurantId", restaurant.getId());
                Log.d("RestaurantAdapter", "Intent created with restaurantId: " + restaurant.getId());
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("RestaurantAdapter", "Error while starting AddReviewActivity", e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvRestaurantDetails, tvRestaurantCoordinates, tvRestaurantWebsite;
        LinearLayout btnContainer;
        Button btnShowPromotions, btnShowReviews, btnAddReview;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantDetails = itemView.findViewById(R.id.tvRestaurantDetails);
            tvRestaurantWebsite = itemView.findViewById(R.id.tvRestaurantWebsite);
            tvRestaurantCoordinates = itemView.findViewById(R.id.tvRestaurantCoordinates);
            btnContainer = itemView.findViewById(R.id.btnContainer);
            btnShowPromotions = itemView.findViewById(R.id.btnShowPromotions);
            btnShowReviews = itemView.findViewById(R.id.btnShowReviews);
            btnAddReview = itemView.findViewById(R.id.btnAddReview);
        }
    }
}
