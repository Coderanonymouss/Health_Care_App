package com.ensias.healthcareapp.patient.progress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.VideoProgressRow;

import java.sql.Date;
import java.util.List;
public class VideoProgressAdapter extends RecyclerView.Adapter<VideoProgressAdapter.ViewHolder> {
    private final List<VideoProgressRow> data;

    public VideoProgressAdapter(List<VideoProgressRow> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoProgressRow row = data.get(position);
        holder.title.setText(row.title);
        holder.status.setText(row.watched ? "✅" : "❌");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status;
        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvTitle);
            status = view.findViewById(R.id.tvWatched);

        }
    }
}
