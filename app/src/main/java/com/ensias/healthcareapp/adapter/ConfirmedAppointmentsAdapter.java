package com.ensias.healthcareapp.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.databinding.ConfirmedAppointementsItemBinding;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ConfirmedAppointmentsAdapter extends FirestoreRecyclerAdapter<ApointementInformation, ConfirmedAppointmentsAdapter.ConfirmedAppointmentsHolder> {

    public ConfirmedAppointmentsAdapter(@NonNull FirestoreRecyclerOptions<ApointementInformation> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ConfirmedAppointmentsHolder holder, int position, @NonNull ApointementInformation appointment) {
        holder.binding.appointementDate.setText(appointment.getTime());
        holder.binding.patientName.setText(appointment.getPatientName());
        holder.binding.appointementType.setText(appointment.getApointementType());

        String imageId = appointment.getPatientId() + ".jpg";
        StorageReference pathReference = FirebaseStorage.getInstance().getReference().child("DoctorProfile/" + imageId);

        pathReference.getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get()
                        .load(uri)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .fit()
                        .centerCrop()
                        .into(holder.binding.patientImage))
                .addOnFailureListener(exception -> {
                    // Ошибка загрузки изображения (можно добавить лог или плейсхолдер)
                });
    }

    @NonNull
    @Override
    public ConfirmedAppointmentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConfirmedAppointementsItemBinding binding = ConfirmedAppointementsItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ConfirmedAppointmentsHolder(binding);
    }

    static class ConfirmedAppointmentsHolder extends RecyclerView.ViewHolder {
        ConfirmedAppointementsItemBinding binding;

        public ConfirmedAppointmentsHolder(@NonNull ConfirmedAppointementsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
