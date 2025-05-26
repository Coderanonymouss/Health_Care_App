package com.ensias.healthcareapp.doctor.adapter;

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

    public PatientListAdapter(Context context, List<Patient> patients) {
        this.context = context;
        this.patients = patients;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.tvName.setText(patient.getFirstName() + " " + patient.getLastName());
        holder.tvEmail.setText(patient.getEmail());

        // Фото (если есть)
        if (patient.getPhotoUrl() != null && !patient.getPhotoUrl().isEmpty()) {
            Picasso.get().load(patient.getPhotoUrl()).placeholder(R.drawable.ic_account).into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_account);
        }
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
