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
import com.example.promoeatsandroid.models.RestaurantWithPromotions;

import java.util.List;

public class RestaurantWithPromotionsAdapter extends RecyclerView.Adapter<RestaurantWithPromotionsAdapter.RestaurantViewHolder> {

    private List<RestaurantWithPromotions> data;
    private Context context;

    public RestaurantWithPromotionsAdapter(Context context, List<RestaurantWithPromotions> data) {
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
        RestaurantWithPromotions restaurant = data.get(position);

        holder.tvRestaurantName.setText(restaurant.getRestaurant().getName());
        holder.itemView.setOnClickListener(v -> {
            boolean expanded = restaurant.getRestaurant().isExpanded();
            restaurant.getRestaurant().setExpanded(!expanded);
            holder.btnContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
        });

        holder.btnShowPromotions.setOnClickListener(v -> {
            Intent intent = new Intent(context, PromotionsActivity.class);
            intent.putExtra("restaurantId", restaurant.getRestaurant().getId());
            context.startActivity(intent);
        });

        holder.btnShowReviews.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewsActivity.class);
            intent.putExtra("restaurantId", restaurant.getRestaurant().getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName;
        LinearLayout btnContainer;
        Button btnShowPromotions, btnShowReviews;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            btnContainer = itemView.findViewById(R.id.btnContainer);
            btnShowPromotions = itemView.findViewById(R.id.btnShowPromotions);
            btnShowReviews = itemView.findViewById(R.id.btnShowReviews);
        }
    }
}
