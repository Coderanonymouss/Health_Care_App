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

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    public interface OnDoctorClickListener {
        void onDoctorEdit(User doctor);
        void onDoctorDelete(User doctor);
    }

    private final List<User> doctors;
    private final OnDoctorClickListener listener;

    public DoctorAdapter(List<User> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        User doctor = doctors.get(position);
        holder.nameText.setText(doctor.getFullName());
        holder.emailText.setText(doctor.getEmail());

        holder.editBtn.setOnClickListener(v -> listener.onDoctorEdit(doctor));
        holder.deleteBtn.setOnClickListener(v -> listener.onDoctorDelete(doctor));
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText;
        ImageButton editBtn, deleteBtn;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textDoctorName);
            emailText = itemView.findViewById(R.id.textDoctorEmail);
            editBtn = itemView.findViewById(R.id.btnEditDoctor);
            deleteBtn = itemView.findViewById(R.id.btnDeleteDoctor);
        }
    }
}
