package com.ensias.healthcareapp.doctor.patients;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.chat.DoctorPatientProfileActivity;
import com.ensias.healthcareapp.doctor.patients.adapter.PatientListAdapter;
import com.ensias.healthcareapp.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private PatientListAdapter adapter;
    private List<Patient> patientList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_patients);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Пациенты");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewAllPatients);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientListAdapter(this, patientList, patient -> {
            // При клике запускаем профиль пациента
            Intent intent = new Intent(this, DoctorPatientProfileActivity.class);
            intent.putExtra("patient_uid", patient.getUid()); // UID пациента
            intent.putExtra("patient_email", patient.getEmail());
            intent.putExtra("firstName", patient.getFirstName());
            intent.putExtra("lastName", patient.getLastName());
            intent.putExtra("iin", patient.getIin()); // если у тебя поле iin, иначе замени на uid
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadPatientsForCurrentDoctor();
    }


    private void loadPatientsForCurrentDoctor() {
        progressBar.setVisibility(View.VISIBLE);
        String currentDoctorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance()
                .collection("Patient")
                .whereEqualTo("doctorId", currentDoctorEmail) // <-- только пациенты текущего доктора
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    patientList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Patient patient = doc.toObject(Patient.class);
                        patientList.add(patient);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                });
    }

}
