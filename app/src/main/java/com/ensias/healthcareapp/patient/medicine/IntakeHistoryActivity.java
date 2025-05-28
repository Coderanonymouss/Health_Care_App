package com.ensias.healthcareapp.patient.medicine;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Intake;
import com.ensias.healthcareapp.patient.medicine.adapter.IntakeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntakeHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IntakeAdapter adapter;
    private final List<Intake> intakeList = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId, medId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intake_history);

        recyclerView = findViewById(R.id.recyclerViewIntakes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IntakeAdapter(intakeList);
        recyclerView.setAdapter(adapter);

        medId = getIntent().getStringExtra("medId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Убираем дефолтный заголовок
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // Показываем иконку "назад"
        toolbar.setNavigationOnClickListener(v -> finish());     // Клик — закрыть Activity

        fetchIntakes();
    }

    private void fetchIntakes() {
        db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medId)
                .collection("intakes")
                .get()
                .addOnSuccessListener(snapshot -> {
                    intakeList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Intake intake = doc.toObject(Intake.class);
                        if (intake != null && intake.getDatetime() != null) intakeList.add(intake);
                    }
                    // Сортируем по времени (от новых к старым)
                    Collections.sort(intakeList, (a, b) -> {
                        if (a.getDatetime() == null) return 1;
                        if (b.getDatetime() == null) return -1;
                        return b.getDatetime().compareTo(a.getDatetime());
                    });
                    adapter.notifyDataSetChanged();
                });
    }
}
