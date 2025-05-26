package com.ensias.healthcareapp.doctor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.FolderItems;
import java.util.List;

public class VideoLessonsFolderAdapter extends RecyclerView.Adapter<VideoLessonsFolderAdapter.FolderViewHolder> {
    private List<FolderItems> folders;
    private OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderClick(String folderId);
    }

    public VideoLessonsFolderAdapter(List<FolderItems> folders, OnFolderClickListener listener) {
        this.folders = folders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder_in_videolessons, parent, false);
        return new FolderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderItems folder = folders.get(position);
        holder.tvFolderName.setText(folder.getName());
        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folder.getId()));
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;
        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
        }
    }
}
