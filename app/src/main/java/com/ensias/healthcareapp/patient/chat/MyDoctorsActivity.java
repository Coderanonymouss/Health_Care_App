package com.ensias.healthcareapp.patient.chat;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.patient.chat.adapter.MyDoctorsAdapter;
import com.ensias.healthcareapp.model.Doctor;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MyDoctorsActivity extends AppCompatActivity {

    private RecyclerView contactsRecycler;
    private MyDoctorsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctors);

        contactsRecycler = findViewById(R.id.contactsRecycler);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        String currentPatientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Находим пациента, получаем его doctorId
        FirebaseFirestore.getInstance()
                .collection("Patient")
                .whereEqualTo("email", currentPatientEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String doctorId = querySnapshot.getDocuments().get(0).getString("doctorId");
                        if (doctorId != null && !doctorId.isEmpty()) {
                            CollectionReference doctorsRef = FirebaseFirestore.getInstance().collection("Doctor");
                            Query query = doctorsRef.whereEqualTo("email", doctorId); // doctorId = email врача
                            FirestoreRecyclerOptions<Doctor> options = new FirestoreRecyclerOptions.Builder<Doctor>()
                                    .setQuery(query, Doctor.class)
                                    .build();

                            adapter = new MyDoctorsAdapter(options);
                            contactsRecycler.setAdapter(adapter);
                            adapter.startListening();
                        } else {
                            Toast.makeText(this, "Доктор не назначен", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки доктора", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        if (adapter != null) adapter.stopListening();
        super.onStop();
    }
}
