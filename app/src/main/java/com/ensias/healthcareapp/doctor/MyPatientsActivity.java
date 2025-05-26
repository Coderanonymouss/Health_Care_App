    package com.ensias.healthcareapp.doctor;

    import android.os.Bundle;
    import android.util.Log;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.ensias.healthcareapp.R;
    import com.ensias.healthcareapp.doctor.adapter.MyPatientsAdapter;
    import com.ensias.healthcareapp.model.Patient;
    import com.firebase.ui.firestore.FirestoreRecyclerOptions;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.CollectionReference;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;

    public class MyPatientsActivity extends AppCompatActivity {
        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Теперь используем коллекцию "Patient"
        private CollectionReference patientsRef = db.collection("Patient");

        private RecyclerView recyclerView;
        private MyPatientsAdapter adapter;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_my_patients);

            recyclerView = findViewById(R.id.ListMyPatients); // <-- вынеси сюда
            setUpRecyclerView();
        }

        public void setUpRecyclerView() {

            // Получаем email текущего доктора
            final String doctorID = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // Запрос: все пациенты, у которых doctorId == текущий email доктора
            Query query = patientsRef.whereEqualTo("doctorId", doctorID)
                    .orderBy("firstName", Query.Direction.ASCENDING); // сортировка по имени, например

            FirestoreRecyclerOptions<Patient> options = new FirestoreRecyclerOptions.Builder<Patient>()
                    .setQuery(query, Patient.class)
                    .build();

            adapter = new MyPatientsAdapter(options);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (recyclerView.getAdapter() == null) recyclerView.setAdapter(adapter);
            if (adapter != null) adapter.startListening();
        }


        @Override
        protected void onStop() {
            if (adapter != null) adapter.stopListening();
            if (recyclerView != null) recyclerView.setAdapter(null);
            super.onStop();
        }

    }
