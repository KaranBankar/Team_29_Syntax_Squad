package com.chaitany.agewell

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class VideoAdapter(private var videos: List<VideoItem>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
        val title: TextView = view.findViewById(R.id.videoTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.title.text = video.title

        // Extract the video ID
        val videoId = extractYouTubeVideoId(video.url)
        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/0.jpg"

        // Create shimmer effect as placeholder
        val shimmer = Shimmer.AlphaHighlightBuilder()
            .setDuration(1000)
            .setBaseAlpha(0.7f)
            .setHighlightAlpha(1.0f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .build()

        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }

        // Load the thumbnail with Picasso and use shimmer effect
        Picasso.get()
            .load(thumbnailUrl)
            .placeholder(shimmerDrawable)  // Show shimmer effect while loading
            .error(R.drawable.error_img)  // Show error image if loading fails
            .into(holder.thumbnail, object : Callback {
                override fun onSuccess() {
                    // Thumbnail loaded successfully
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                }
            })

        // Set click listener to open YouTube
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = videos.size

    fun updateVideos(newVideos: List<VideoItem>) {
        videos = newVideos
        notifyDataSetChanged()
    }

    private fun extractYouTubeVideoId(url: String): String {
        return when {
            url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
            url.contains("watch?v=") -> url.substringAfter("watch?v=").substringBefore("&")
            else -> ""
        }
    }
}
