package com.ensias.healthcareapp.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.VideoFolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FolderAdapter extends FirestoreRecyclerAdapter<VideoFolder, FolderAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(VideoFolder folder);
        void onEditFolder(VideoFolder folder);
        void onDeleteFolder(VideoFolder folder);

    }

    private final OnFolderClickListener listener;

    public FolderAdapter(@NonNull FirestoreRecyclerOptions<VideoFolder> options,
                         OnFolderClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull FolderViewHolder holder, int position, @NonNull VideoFolder model) {
        holder.folderName.setText(model.getName());

        holder.itemView.setOnClickListener(v -> listener.onFolderClick(model));

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnMenu);
            popup.inflate(R.menu.menu_folder_options);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEditFolder(model);
                    return true;
                } else if (id == R.id.action_delete) {
                    listener.onDeleteFolder(model);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @NonNull @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(v);
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageButton btnMenu;
        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            btnMenu    = itemView.findViewById(R.id.btnFolderMenu);
        }
    }
}
