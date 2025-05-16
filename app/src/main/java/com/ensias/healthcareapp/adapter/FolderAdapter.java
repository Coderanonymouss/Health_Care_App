package com.ensias.healthcareapp.adapter;

import android.view.LayoutInflater;
import android.view.MenuInflater;
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

    private final OnFolderClickListener listener;

    public FolderAdapter(@NonNull FirestoreRecyclerOptions<VideoFolder> options, OnFolderClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull FolderViewHolder holder, int position, @NonNull VideoFolder model) {
        // Получаем id документа Firestore и кладём в модель
        String docId = getSnapshots().getSnapshot(position).getId();
        model.setId(docId);

        holder.folderName.setText(model.getName());
        holder.itemView.setOnClickListener(v -> listener.onFolderClick(model));
        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.menuButton);
            popupMenu.inflate(R.menu.menu_folder_options);
            popupMenu.setOnMenuItemClickListener(item -> {
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
            popupMenu.show();
        });
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageButton menuButton;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            menuButton = itemView.findViewById(R.id.btnFolderMenu);
        }
    }


    public interface OnFolderClickListener {
        void onFolderClick(VideoFolder folder);
        void onEditFolder(VideoFolder folder);
        void onDeleteFolder(VideoFolder folder);
    }
}

