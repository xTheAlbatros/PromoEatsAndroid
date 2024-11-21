package com.example.promoeatsandroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.Promotion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;


public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private List<Promotion> promotionList;

    public PromotionAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);

        holder.tvPromotionDescription.setText(promotion.getDescription());
        holder.tvPromotionDates.setText(formatDates(promotion.getStartTime(), promotion.getEndTime()));
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    private String formatDates(String startTime, String endTime) {
        try {
            // Użyj SimpleDateFormat do parsowania i formatowania dat
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            Date startDate = inputFormat.parse(startTime);
            Date endDate = inputFormat.parse(endTime);

            String formattedStart = outputFormat.format(startDate);
            String formattedEnd = outputFormat.format(endDate);

            return "Od: " + formattedStart + " Do: " + formattedEnd;
        } catch (Exception e) {
            e.printStackTrace();
            return "Nieprawidłowy format daty";
        }
    }

    public static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPromotionDescription, tvPromotionDates;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPromotionDescription = itemView.findViewById(R.id.tvPromotionDescription);
            tvPromotionDates = itemView.findViewById(R.id.tvPromotionDates);
        }
    }
}
