package com.ensias.healthcareapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.VideoLesson;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(VideoLesson video);
    }

    private final List<VideoLesson> videos;
    private final OnVideoClickListener listener;

    public VideoAdapter(List<VideoLesson> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder h, int pos) {
        VideoLesson video = videos.get(pos);
        h.tvTitle.setText(video.getTitle());
        // если у вас есть флаг watched в модели — показывайте соответствующую иконку:
        h.ivStatus.setImageResource(
                video.isWatched()
                        ? R.drawable.ic_watched
                        : R.drawable.ic_unwatched
        );
        // кнопка Play
        h.btnPlay.setOnClickListener(v -> listener.onVideoClick(video));
        // можно также весь элемент:
        h.itemView.setOnClickListener(v -> listener.onVideoClick(video));
    }

    @Override public int getItemCount() { return videos.size(); }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView    ivStatus;
        TextView     tvTitle;
        ImageButton  btnPlay;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvTitle  = itemView.findViewById(R.id.tvVideoTitle);
            btnPlay  = itemView.findViewById(R.id.btnPlayYouTube);
        }
    }
}
