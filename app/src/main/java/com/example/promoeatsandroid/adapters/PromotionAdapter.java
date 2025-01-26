package com.example.promoeatsandroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.Promotion;
import com.example.promoeatsandroid.models.Images;
import com.example.promoeatsandroid.network.ApiService;
import com.example.promoeatsandroid.network.RetrofitClient;
import com.example.promoeatsandroid.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private final List<Promotion> promotionList;
    private final OnShowImagesClickListener onShowImagesClickListener;

    public PromotionAdapter(List<Promotion> promotionList, OnShowImagesClickListener onShowImagesClickListener) {
        this.promotionList = promotionList;
        this.onShowImagesClickListener = onShowImagesClickListener;
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

        // Domyślnie ukryj przycisk
        holder.btnShowImages.setVisibility(View.GONE);

        // Sprawdzanie dostępności zdjęć
        TokenManager tokenManager = new TokenManager(holder.itemView.getContext());
        String token = "Bearer " + tokenManager.getToken();
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.getImagesForPromotion(token, promotion.getId()).enqueue(new Callback<List<Images>>() {
            @Override
            public void onResponse(Call<List<Images>> call, Response<List<Images>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Images> images = response.body();

                    // Filtrujemy tylko zdjęcia z poprawnymi URL
                    boolean hasValidImages = images.stream().anyMatch(image -> image.getPath() != null && !image.getPath().isEmpty());

                    if (hasValidImages) {
                        holder.btnShowImages.setVisibility(View.VISIBLE);
                        holder.btnShowImages.setOnClickListener(v -> {
                            if (onShowImagesClickListener != null) {
                                onShowImagesClickListener.onShowImagesClick(promotion.getId());
                            }
                        });
                    } else {
                        // Brak zdjęć - ukryj przycisk
                        holder.btnShowImages.setVisibility(View.GONE);
                    }
                } else {
                    // Odpowiedź serwera nie jest prawidłowa - ukryj przycisk
                    holder.btnShowImages.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Images>> call, Throwable t) {
                // W razie błędu ukryj przycisk
                holder.btnShowImages.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    private String formatDates(String startTime, String endTime) {
        try {
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
        Button btnShowImages;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPromotionDescription = itemView.findViewById(R.id.tvPromotionDescription);
            tvPromotionDates = itemView.findViewById(R.id.tvPromotionDates);
            btnShowImages = itemView.findViewById(R.id.btnShowImages);
        }
    }

    public interface OnShowImagesClickListener {
        void onShowImagesClick(int promotionId);
    }
}
