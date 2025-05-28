package com.ensias.healthcareapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.YouTubePlayerActivity;
import com.ensias.healthcareapp.model.VideoLesson;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FirestoreVideoAdapter
        extends FirestoreRecyclerAdapter<VideoLesson, FirestoreVideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(VideoLesson videoLesson);
    }

    private final Context context;
    private final OnVideoClickListener listener;

    public FirestoreVideoAdapter(Context context,
                                 @NonNull FirestoreRecyclerOptions<VideoLesson> options,
                                 OnVideoClickListener listener) {
        super(options);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull VideoViewHolder holder,
                                    int position,
                                    @NonNull VideoLesson model) {
        // Заголовок видео
        holder.tvTitle.setText(model.getTitle());

        // Кнопка воспроизведения
        holder.btnPlay.setOnClickListener(v -> {
            String url = model.getVideoUrl();
            if (url == null || url.isEmpty()) return;

            // Если это YouTube
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                Intent i = new Intent(context, YouTubePlayerActivity.class);
                i.putExtra("videoUrl", url);
                context.startActivity(i);
            } else {
                // Обычный mp4 или другой видеофайл — открыть внешним плеером
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.setDataAndType(Uri.parse(url), "video/*");
                context.startActivity(i);
            }
            listener.onVideoClick(model);
        });
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView     tvTitle;
        ImageButton  btnPlay;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tvVideoTitle);
            btnPlay  = itemView.findViewById(R.id.btnPlayYouTube);
        }
    }
}
