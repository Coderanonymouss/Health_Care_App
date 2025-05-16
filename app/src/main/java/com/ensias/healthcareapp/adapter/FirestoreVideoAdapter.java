package com.ensias.healthcareapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Video;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FirestoreVideoAdapter extends FirestoreRecyclerAdapter<Video, FirestoreVideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onClick(Video video);
    }

    private final OnVideoClickListener listener;

    public FirestoreVideoAdapter(@NonNull FirestoreRecyclerOptions<Video> options, OnVideoClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull Video model) {
        holder.title.setText(model.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onClick(model));
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    static class VideoViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView title;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitleText);
        }
    }
}
