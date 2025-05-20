package com.ensias.healthcareapp.activity.doctor;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Request;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class DoctorRequestsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter<Request, RequestViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_requests);

        RecyclerView recyclerView = findViewById(R.id.requestRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String currentDoctor = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query query = db.collection("Request")
                .whereEqualTo("id_doctor", currentDoctor)
                .whereEqualTo("status", "pending");

        FirestoreRecyclerOptions<Request> options = new FirestoreRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Request, RequestViewHolder>(options) {
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = getLayoutInflater().inflate(R.layout.item_request_row, parent, false);
                return new RequestViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request model) {
                holder.patientEmail.setText(model.getId_patient());

                holder.acceptBtn.setOnClickListener(v -> {
                    getSnapshots().getSnapshot(position).getReference().update("status", "accepted");
                    Toast.makeText(DoctorRequestsActivity.this, "Принято", Toast.LENGTH_SHORT).show();
                });

                holder.rejectBtn.setOnClickListener(v -> {
                    getSnapshots().getSnapshot(position).getReference().update("status", "rejected");
                    Toast.makeText(DoctorRequestsActivity.this, "Отклонено", Toast.LENGTH_SHORT).show();
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView patientEmail;
        Button acceptBtn, rejectBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            patientEmail = itemView.findViewById(R.id.text_patient_email);
            acceptBtn = itemView.findViewById(R.id.btn_accept);
            rejectBtn = itemView.findViewById(R.id.btn_reject);
        }
    }
}
