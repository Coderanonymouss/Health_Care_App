package com.ensias.healthcareapp.doctor.patients.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Patient;
import com.squareup.picasso.Picasso;

import java.util.List;
public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.PatientViewHolder> {
    private List<Patient> patients;
    private Context context;
    private OnPatientClickListener listener; // 👈

    // Добавь интерфейс
    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientListAdapter(Context context, List<Patient> patients, OnPatientClickListener listener) {
        this.context = context;
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        // Первая строка — имя и фамилия
        holder.tvName.setText(patient.getFirstName() + " " + patient.getLastName());
        // Вторая строка — иин
        holder.tvEmail.setText(patient.getIin());
        if (patient.getPhotoUrl() != null && !patient.getPhotoUrl().isEmpty()) {
            Picasso.get().load(patient.getPhotoUrl()).placeholder(R.drawable.ic_account).into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_account);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPatientClick(patient);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        ImageView ivPhoto;
        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPatientName);
            tvEmail = itemView.findViewById(R.id.tvPatientEmail);
            ivPhoto = itemView.findViewById(R.id.ivPatientPhoto);
        }
    }
}
