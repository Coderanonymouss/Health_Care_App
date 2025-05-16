package com.ensias.healthcareapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.YouTubePlayerActivity;
import com.ensias.healthcareapp.model.VideoLesson;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final Context context;
    private final List<VideoLesson> videoList;
    private final OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(int position);
    }

    public VideoAdapter(Context context, List<VideoLesson> videoList, OnVideoClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoLesson video = videoList.get(position);
        holder.tvTitle.setText(video.getTitle());

        String url = video.getUrl();
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            holder.videoView.setVisibility(View.GONE);
            holder.btnPlayYouTube.setVisibility(View.VISIBLE);
            holder.btnPlayYouTube.setOnClickListener(v -> {
                Intent intent = new Intent(context, YouTubePlayerActivity.class);
                intent.putExtra("videoUrl", url);
                context.startActivity(intent);
            });
        } else {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.btnPlayYouTube.setVisibility(View.GONE);
            holder.videoView.setVideoURI(Uri.parse(url));
            holder.videoView.seekTo(1);
            holder.itemView.setOnClickListener(v -> {
                holder.videoView.start();
                listener.onVideoClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        VideoView videoView;
        ImageButton btnPlayYouTube;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvVideoTitle);
            videoView = itemView.findViewById(R.id.videoView);
            btnPlayYouTube = itemView.findViewById(R.id.btnPlayYouTube);
        }
    }
}
