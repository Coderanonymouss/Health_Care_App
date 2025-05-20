package com.ensias.healthcareapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.ensias.healthcareapp.model.Doctor;
import com.ensias.healthcareapp.model.Patient;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DoctorAppointementAdapter extends FirestoreRecyclerAdapter<ApointementInformation, DoctorAppointementAdapter.MyDoctorAppointementHolder> {

    public DoctorAppointementAdapter(@NonNull FirestoreRecyclerOptions<ApointementInformation> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyDoctorAppointementHolder holder, @SuppressLint("RecyclerView") int position, @NonNull final ApointementInformation info) {
        holder.dateAppointement.setText(info.getTime());
        holder.patientName.setText(info.getPatientName());
        holder.patientPhone.setText(info.getPatientPhone() == null ? "—" : info.getPatientPhone());
        holder.appointementType.setText(info.getApointementType());

        // Загрузка фото пациента (если есть)
        String imageId = info.getPatientId() + ".jpg";
        StorageReference pathReference = FirebaseStorage.getInstance().getReference().child("PatientProfile/" + imageId);
        pathReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.ic_account)
                    .fit()
                    .centerCrop()
                    .into(holder.patient_image);
        }).addOnFailureListener(exception -> {
            holder.patient_image.setImageResource(R.drawable.ic_account);
        });

        holder.approveBtn.setOnClickListener(v -> {
            info.setType("Accepted");
            FirebaseFirestore.getInstance()
                    .collection("Patient").document(info.getPatientId())
                    .collection("calendar")
                    .document(info.getTime().replace("/", "_"))
                    .set(info);

            if (info.getChemin() != null && !info.getChemin().isEmpty()) {
                FirebaseFirestore.getInstance()
                        .document(info.getChemin())
                        .update("type", "Accepted");
            }

            FirebaseFirestore.getInstance()
                    .collection("Doctor").document(info.getDoctorId())
                    .collection("calendar")
                    .document(info.getTime().replace("/", "_"))
                    .set(info);

            // --- Вот это добавляет пациента врачу в MyPatients ---
            FirebaseFirestore.getInstance()
                    .collection("Patient")
                    .whereEqualTo("email", info.getPatientId()) // patientId это email пациента
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Patient patient = queryDocumentSnapshots.getDocuments().get(0).toObject(Patient.class);
                            FirebaseFirestore.getInstance()
                                    .collection("Doctor")
                                    .document(info.getDoctorId())
                                    .collection("MyPatients")
                                    .document(info.getPatientId())
                                    .set(patient);
                        }
                    });
            // 2. В профиль пациента записываем doctorId (один доктор)
            FirebaseFirestore.getInstance()
                    .collection("Patient")
                    .document(info.getPatientId())
                    .update("doctorId", info.getDoctorId());

            // Удаляем из списка заявок (если нужно)
            getSnapshots().getSnapshot(position).getReference().delete();
            Toast.makeText(holder.itemView.getContext(), "Заявка принята", Toast.LENGTH_SHORT).show();
        });

        // Кнопка "Отклонить"
        holder.cancelBtn.setOnClickListener(v -> {
            info.setType("Refused");
            FirebaseFirestore.getInstance()
                    .collection("Patient").document(info.getPatientId())
                    .collection("calendar")
                    .document(info.getTime().replace("/", "_"))
                    .set(info);
            if (info.getChemin() != null && !info.getChemin().isEmpty()) {
                FirebaseFirestore.getInstance()
                        .document(info.getChemin())
                        .delete();
            }
            getSnapshots().getSnapshot(position).getReference().delete();
            Toast.makeText(holder.itemView.getContext(), "Заявка отклонена", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public MyDoctorAppointementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_appointement_item, parent, false);
        return new MyDoctorAppointementHolder(v);
    }

    static class MyDoctorAppointementHolder extends RecyclerView.ViewHolder {
        TextView dateAppointement, patientName, patientPhone, appointementType;
        Button approveBtn, cancelBtn;
        ImageView patient_image;

        public MyDoctorAppointementHolder(@NonNull View itemView) {
            super(itemView);
            dateAppointement = itemView.findViewById(R.id.appointement_date);
            patientName      = itemView.findViewById(R.id.patient_name);
            patientPhone     = itemView.findViewById(R.id.patient_phone);
            appointementType = itemView.findViewById(R.id.appointement_type);
            approveBtn       = itemView.findViewById(R.id.btn_accept);
            cancelBtn        = itemView.findViewById(R.id.btn_decline);
            patient_image    = itemView.findViewById(R.id.patient_image);
        }
    }
}
