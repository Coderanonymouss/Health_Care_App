package com.ensias.healthcareapp.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserEdit(User user);
        void onUserDelete(User user);
    }

    private final List<User> users;
    private final OnUserClickListener listener;

    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.nameText.setText(user.getFullName());
        holder.emailText.setText(user.getEmail());

        holder.editBtn.setOnClickListener(v -> listener.onUserEdit(user));
        holder.deleteBtn.setOnClickListener(v -> listener.onUserDelete(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText;
        ImageButton editBtn, deleteBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textUserName);
            emailText = itemView.findViewById(R.id.textUserEmail);
            editBtn = itemView.findViewById(R.id.btnEditUser);
            deleteBtn = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
