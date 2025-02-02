package com.example.promoeatsandroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.promoeatsandroid.R;
import com.example.promoeatsandroid.models.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Ustaw ocenę w RatingBar
        holder.ratingBar.setRating(review.getRate());

        // Ustaw komentarz
        holder.tvComment.setText(review.getComment());

        // Sformatuj i ustaw datę
        holder.tvDate.setText(formatDate(review.getCreatedTime()));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private String formatDate(String rawDate) {
        try {
            // Wejściowy format daty
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            // Wyjściowy format daty
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("pl", "PL"));
            Date date = inputFormat.parse(rawDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Nieznana data";
        }
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView tvComment, tvDate;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            // Powiąż widoki
            ratingBar = itemView.findViewById(R.id.rbRate);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
