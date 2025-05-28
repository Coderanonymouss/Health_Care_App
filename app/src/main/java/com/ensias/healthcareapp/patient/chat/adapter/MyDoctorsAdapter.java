package com.ensias.healthcareapp.patient.chat.adapter;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.chat.ChatActivity;
import com.ensias.healthcareapp.model.Doctor;
import com.ensias.healthcareapp.patient.chat.ProfileDoctorInPatientActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MyDoctorsAdapter extends FirestoreRecyclerAdapter<Doctor, MyDoctorsAdapter.MyDoctorsHolder> {
    private StorageReference pathReference;

    public MyDoctorsAdapter(@NonNull FirestoreRecyclerOptions<Doctor> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyDoctorsHolder holder, int position, @NonNull final Doctor doctor) {
        String doctorName = doctor.getFullName() != null ? doctor.getFullName() : "Не указано";
        String doctorPhone = doctor.getTel() != null ? doctor.getTel() : "Не указано";
        String doctorEmail = doctor.getEmail() != null ? doctor.getEmail() : "";

        holder.textViewTitle.setText(doctorName);
        holder.textViewTelephone.setText("Tél: " + doctorPhone);

        // Кнопка чата
        holder.contactButton.setOnClickListener(v -> openChatPage(v.getContext(), doctor));
        // Клик по всей карточке открывает профиль врача (по UID)
        holder.parentLayout.setOnClickListener(v -> openDoctorProfile(v.getContext(), doctor));
        // Кнопка звонка
        holder.callBtn.setOnClickListener(v -> openDialer(holder.callBtn.getContext(), doctorPhone));

        // Фото доктора
        if (!doctorEmail.isEmpty()) {
            String imageId = doctorEmail + ".jpg";
            pathReference = FirebaseStorage.getInstance().getReference().child("DoctorProfile/" + imageId);
            pathReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(holder.imageViewDoctor))
                    .addOnFailureListener(e -> holder.imageViewDoctor.setImageResource(R.drawable.ic_account_circle));
        } else {
            holder.imageViewDoctor.setImageResource(R.drawable.ic_account_circle);
        }
    }

    private void openDialer(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    private void openDoctorProfile(Context context, Doctor doctor) {
        // TODO: реализуй просмотр профиля доктора (если нужно)
         Intent intent = new Intent(context, ProfileDoctorInPatientActivity.class);
         intent.putExtra("user_uid", doctor.getEmail());
         intent.putExtra("companionRole","Doctor");
         context.startActivity(intent);
    }

    private void openChatPage(Context context, Doctor doctor) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Intent i = new Intent(context, ChatActivity.class);
        i.putExtra("key1", currentUserEmail + "_" + doctor.getEmail());
        i.putExtra("key2", doctor.getEmail() + "_" + currentUserEmail);
        i.putExtra("companionName", doctor.getFullName() != null ? doctor.getFullName() : "Не указано");
        i.putExtra("companionPhotoUrl", doctor.getPhotoUrl());
        i.putExtra("companionRole", "Doctor");
        i.putExtra("companionId", doctor.getEmail());
        context.startActivity(i);
    }

    @NonNull
    @Override
    public MyDoctorsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_doctor_item, parent, false);
        return new MyDoctorsHolder(v);
    }

    static class MyDoctorsHolder extends RecyclerView.ViewHolder {
        ImageButton callBtn;
        TextView textViewTitle;
        TextView textViewTelephone;
        ImageView imageViewDoctor;
        ImageButton contactButton;
        RelativeLayout parentLayout;

        public MyDoctorsHolder(@NonNull View itemView) {
            super(itemView);
            callBtn = itemView.findViewById(R.id.callBtn);
            textViewTitle = itemView.findViewById(R.id.doctor_view_title);
            textViewTelephone = itemView.findViewById(R.id.text_view_telephone);
            imageViewDoctor = itemView.findViewById(R.id.doctor_item_image);
            contactButton = itemView.findViewById(R.id.contact);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
