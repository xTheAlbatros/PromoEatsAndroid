package com.example.promoeatsandroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.Promotion;
import com.example.promoeatsandroid.models.RestaurantWithPromotions;

import java.util.List;

public class RestaurantWithPromotionsAdapter extends RecyclerView.Adapter<RestaurantWithPromotionsAdapter.RestaurantViewHolder> {

    private List<RestaurantWithPromotions> data;

    public RestaurantWithPromotionsAdapter(List<RestaurantWithPromotions> data) {
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

        holder.tvRestaurantName.setText(restaurantWithPromotions.getRestaurant().getName());
        holder.tvRestaurantDetails.setText(restaurantWithPromotions.getRestaurant().getEmail());
        holder.tvRestaurantCoordinates.setText("Lat: " + restaurantWithPromotions.getRestaurant().getLocation().getLatitude() +
                ", Lon: " + restaurantWithPromotions.getRestaurant().getLocation().getLongitude());

        holder.itemView.setOnClickListener(v -> {
            boolean expanded = restaurantWithPromotions.getRestaurant().isExpanded();
            restaurantWithPromotions.getRestaurant().setExpanded(!expanded);
            notifyItemChanged(position);
        });

        boolean expanded = restaurantWithPromotions.getRestaurant().isExpanded();
        holder.promotionRecyclerView.setVisibility(expanded ? View.VISIBLE : View.GONE);

        PromotionAdapter promotionAdapter = new PromotionAdapter(restaurantWithPromotions.getPromotions());
        holder.promotionRecyclerView.setAdapter(promotionAdapter);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvRestaurantDetails, tvRestaurantCoordinates;
        RecyclerView promotionRecyclerView;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantDetails = itemView.findViewById(R.id.tvRestaurantDetails);
            tvRestaurantCoordinates = itemView.findViewById(R.id.tvRestaurantCoordinates);
            promotionRecyclerView = itemView.findViewById(R.id.rvPromotions);
            promotionRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}
