package com.ensias.healthcareapp.patient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Contact;
import com.ensias.healthcareapp.model.Doctor;
import com.ensias.healthcareapp.patient.adapter.ContactsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyDoctorsActivity extends AppCompatActivity {

    private RecyclerView contactsRecycler;
    private ContactsAdapter adapter;
    private List<Contact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctors);

        contactsRecycler = findViewById(R.id.contactsRecycler);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactsAdapter(contactList);
        contactsRecycler.setAdapter(adapter);

        loadDoctor();
    }

    private void loadDoctor() {
        String currentPatientEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        FirebaseFirestore.getInstance()
                .collection("Patient")
                .whereEqualTo("email", currentPatientEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                        String doctorUid = snapshot.getString("doctorUid"); // ПОЛЕ doctorUid ДОЛЖНО БЫТЬ В Patient
                        if (doctorUid != null && !doctorUid.isEmpty()) {
                            loadDoctorData(doctorUid);
                        } else {
                            Toast.makeText(this, "Доктор не назначен", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки доктора", Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDoctorData(String doctorUid) {
        FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(doctorUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Doctor doctor = snapshot.toObject(Doctor.class);
                    if (doctor != null) {
                        StorageReference imgRef = FirebaseStorage.getInstance()
                                .getReference("DoctorProfile/" + doctorUid + ".jpg");
                        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            contactList.add(new Contact(
                                    doctorUid,                 // UID доктора
                                    doctor.getEmail(),
                                    doctor.getFullName(),
                                    "Дәрігер",
                                    uri.toString(),
                                    doctor.getTel()
                            ));
                            adapter.notifyDataSetChanged();
                        }).addOnFailureListener(e -> {
                            contactList.add(new Contact(
                                    doctorUid,
                                    doctor.getEmail(),
                                    doctor.getFullName(),
                                    "Дәрігер",
                                    "",
                                    doctor.getTel()
                            ));
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
    }
}
