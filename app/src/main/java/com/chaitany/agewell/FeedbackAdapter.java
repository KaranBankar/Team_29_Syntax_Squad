package com.chaitany.agewell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chaitany.agewell.Feedback;
import com.chaitany.agewell.R;
import com.chaitany.agewell.StarRatingView;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<Feedback> feedbackList;
    private boolean isClickable; // Flag to control clickability

    public FeedbackAdapter(List<Feedback> feedbackList, boolean isClickable) {
        this.feedbackList = feedbackList;
        this.isClickable = isClickable; // Set the flag
    }

    @Override
    public FeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_list_item, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.userNameText.setText(feedback.getUserName());
        holder.commentText.setText(feedback.getComment());
        holder.starRatingView.setRating(feedback.getRating());

        // Set clickability based on the flag
        holder.starRatingView.setClickable(isClickable);
        holder.starRatingView.setFocusable(isClickable);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView commentText;
        TextView userNameText;
        StarRatingView starRatingView;

        public FeedbackViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.feedback_comment);
            userNameText = itemView.findViewById(R.id.feedback_user_name);
            starRatingView = itemView.findViewById(R.id.feedback_star_rating);
        }
    }
}