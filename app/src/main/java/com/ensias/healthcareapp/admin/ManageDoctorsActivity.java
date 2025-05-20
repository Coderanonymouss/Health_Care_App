package com.ensias.healthcareapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.admin.activity.EditDoctorActivity;
import com.ensias.healthcareapp.admin.adapter.DoctorAdapter;
import com.ensias.healthcareapp.model.User;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctorsActivity extends AppCompatActivity implements DoctorAdapter.OnDoctorClickListener {

    private RecyclerView recyclerView;
    private DoctorAdapter adapter;
    private List<User> doctorList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_doctors);
        setTitle("Докторларды басқару");

        recyclerView = findViewById(R.id.recyclerViewDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAdapter(doctorList, this);
        recyclerView.setAdapter(adapter);

        loadDoctors();
    }

    private void loadDoctors() {
        db.collection("User")
                .whereEqualTo("type", "doctor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    doctorList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) doctorList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Қате жүктеу: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDoctorDelete(User doctor) {
        // Удаление пользователя
        db.collection("User").document(doctor.getEmail())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Доктор удален", Toast.LENGTH_SHORT).show();
                    doctorList.remove(doctor);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDoctorEdit(User doctor) {
        // Здесь можно открыть экран редактирования доктора, например:
        Intent intent = new Intent(this, EditDoctorActivity.class);
        intent.putExtra("doctorId", doctor.getEmail());
        startActivity(intent);
    }
}

