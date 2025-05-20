package com.ensias.healthcareapp.activity.doctor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.ensias.healthcareapp.adapter.DoctorAppointementAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DoctorAppointementActivity extends AppCompatActivity {
    private DoctorAppointementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointement);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Ошибка: не удалось получить email пользователя", Toast.LENGTH_LONG).show();
            return;
        }
        String doctorEmail = user.getEmail();

        CollectionReference ref = FirebaseFirestore
                .getInstance()
                .collection("Doctor")
                .document(doctorEmail)
                .collection("apointementrequest");

        // Предпочтительно сортировать по server timestamp (requestedAt)
        Query query = ref.orderBy("requestedAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ApointementInformation> options =
                new FirestoreRecyclerOptions.Builder<ApointementInformation>()
                        .setQuery(query, ApointementInformation.class)
                        .build();

        adapter = new DoctorAppointementAdapter(options);
        RecyclerView rv = findViewById(R.id.DoctorAppointement);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
