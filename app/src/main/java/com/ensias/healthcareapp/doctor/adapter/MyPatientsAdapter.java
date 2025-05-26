package com.ensias.healthcareapp.doctor.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.ChatActivity;
import com.ensias.healthcareapp.doctor.DoctorPatientProfileActivity;
import com.ensias.healthcareapp.model.Patient;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyPatientsAdapter extends FirestoreRecyclerAdapter<Patient, MyPatientsAdapter.MyPatientsHolder> {
    private StorageReference pathReference;

    public MyPatientsAdapter(@NonNull FirestoreRecyclerOptions<Patient> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyPatientsHolder holder, int position, @NonNull final Patient patient) {
        // Имя и телефон с защитой от null
        String patientName = patient.getFullName() != null ? patient.getFullName() : "Не указано";
        String patientPhone = patient.getTel() != null ? patient.getTel() : "Не указано";
        String patientEmail = patient.getEmail() != null ? patient.getEmail() : "";

        holder.textViewTitle.setText(patientName);
        holder.textViewTelephone.setText("Tél: " + patientPhone);

        // Кнопка чата
        holder.contactButton.setOnClickListener(v -> openChatPage(v.getContext(), patient));
        // Клик по всей карточке открывает профиль пациента врача (по UID)
        holder.parentLayout.setOnClickListener(v -> openDoctorPatientProfile(v.getContext(), patient));
        // Кнопка звонка
        holder.callBtn.setOnClickListener(v -> openDialer(holder.callBtn.getContext(), patientPhone));

        // Фото пациента
        if (!patientEmail.isEmpty()) {
            String imageId = patientEmail + ".jpg";
            pathReference = FirebaseStorage.getInstance().getReference().child("PatientProfile/" + imageId);
            pathReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(holder.imageViewPatient))
                    .addOnFailureListener(e -> holder.imageViewPatient.setImageResource(R.drawable.ic_account));
        } else {
            holder.imageViewPatient.setImageResource(R.drawable.ic_account);
        }
    }

    private void openDialer(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    private void openDoctorPatientProfile(Context context, Patient patient) {
        Intent intent = new Intent(context, DoctorPatientProfileActivity.class); // Профиль по UID!
        intent.putExtra("patient_uid", patient.getUid());
        context.startActivity(intent);
    }

    private void openChatPage(Context context, Patient patient) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Intent i = new Intent(context, ChatActivity.class);
        i.putExtra("key1", patient.getEmail() + "_" + currentUserEmail);
        i.putExtra("key2", currentUserEmail + "_" + patient.getEmail());
        i.putExtra("companionName", patient.getFullName() != null ? patient.getFullName() : "Не указано");
        // Можно добавить companionPhotoUrl, если есть
        i.putExtra("companionPhotoUrl", "");
        i.putExtra("companionRole", "Patient");
        i.putExtra("companionId", patient.getEmail());
        context.startActivity(i);
    }

    @NonNull
    @Override
    public MyPatientsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_patient_item, parent, false);
        return new MyPatientsHolder(v);
    }

    static class MyPatientsHolder extends RecyclerView.ViewHolder {
        ImageButton callBtn;
        TextView textViewTitle;
        TextView textViewTelephone;
        ImageView imageViewPatient;
        ImageButton contactButton;
        RelativeLayout parentLayout;

        @SuppressLint("WrongViewCast")
        public MyPatientsHolder(@NonNull View itemView) {
            super(itemView);
            callBtn = itemView.findViewById(R.id.callBtn);
            textViewTitle = itemView.findViewById(R.id.patient_view_title);
            textViewTelephone = itemView.findViewById(R.id.text_view_telephone);
            imageViewPatient = itemView.findViewById(R.id.patient_item_image);
            contactButton = itemView.findViewById(R.id.contact);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
