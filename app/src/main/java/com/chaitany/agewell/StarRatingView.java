package com.chaitany.agewell;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class StarRatingView extends LinearLayout {
    private final List<ImageView> stars = new ArrayList<>();
    private int rating = 0;
    private OnRatingChangeListener listener;
    private boolean isClickable = true; // Flag to control clickability

    public interface OnRatingChangeListener {
        void onRatingChanged(int rating);
    }

    public StarRatingView(Context context) {
        super(context);
        init(context);
    }

    public StarRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);

        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(context);
            star.setImageResource(R.drawable.star_empty);

            int padding = dpToPx(4);
            star.setPadding(padding, padding, padding, padding);

            final int starIndex = i;
            star.setOnClickListener(v -> {
                if (isClickable) { // Check if clickable
                    setRating(starIndex + 1);
                }
            });

            addView(star);
            stars.add(star);

            LayoutParams params = (LayoutParams) star.getLayoutParams();
            params.width = dpToPx(48);
            params.height = dpToPx(48);
            star.setLayoutParams(params);
        }
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
        updateStars();
        if (listener != null) {
            listener.onRatingChanged(rating);
        }
    }

    private void updateStars() {
        for (int i = 0; i < stars.size(); i++) {
            if (i < rating) {
                stars.get(i).setImageResource(R.drawable.star_filled);
            } else {
                stars.get(i).setImageResource(R.drawable.star_empty);
            }
        }
    }

    public void setOnRatingChangeListener(OnRatingChangeListener listener) {
        this.listener = listener;
    }

    public void setClickable(boolean clickable) {
        this.isClickable = clickable; // Set the clickability flag
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}