package com.ensias.healthcareapp.patient.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Contact;
import com.ensias.healthcareapp.patient.ChatPatientActivity;
import com.ensias.healthcareapp.patient.ProfileDoctorInPatientActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private List<Contact> contacts;

    public ContactsAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);

        holder.name.setText(contact.getName());
        holder.role.setText(contact.getRole());

        if (contact.getPhotoUrl() != null && !contact.getPhotoUrl().isEmpty()) {
            Picasso.get().load(contact.getPhotoUrl()).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_account);
        }

        holder.btnChat.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            String patientEmail = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail();
            Log.d("CONTACTS_DEBUG", "Открываем чат: UID врача: " + contact.getDoctorUid() + ", email: " + contact.getEmail());
            Intent i = new Intent(context, ChatPatientActivity.class);
            i.putExtra("companionId", contact.getDoctorUid()); // UID доктора
            i.putExtra("key1", contact.getEmail() + "_" + patientEmail);
            i.putExtra("key2", patientEmail + "_" + contact.getEmail());
            i.putExtra("companionName", contact.getName());
            i.putExtra("companionRole", contact.getRole());
            i.putExtra("companionPhotoUrl", contact.getPhotoUrl());
            i.putExtra("companionEmail", contact.getEmail());
            context.startActivity(i);
        });


        holder.btnCall.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Log.d("CONTACTS_DEBUG", "Открываем профиль врача: UID врача: " + contact.getDoctorUid());
            if (contact.getTel() != null && !contact.getTel().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.getTel()));
                context.startActivity(intent);
            }
        });

        // Открытие профиля доктора по UID:
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ProfileDoctorInPatientActivity.class);
            intent.putExtra("userId", contact.getDoctorUid()); // UID доктора
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, role;
        ImageButton btnChat, btnCall;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.contact_image);
            name = itemView.findViewById(R.id.contact_name);
            role = itemView.findViewById(R.id.contact_role);
            btnChat = itemView.findViewById(R.id.contact_chat);
            btnCall = itemView.findViewById(R.id.contact_call);
        }
    }
}
