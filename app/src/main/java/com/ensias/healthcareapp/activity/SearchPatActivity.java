package com.ensias.healthcareapp.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.adapter.DoctorAdapterFiltred;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchPatActivity extends AppCompatActivity {

    private CollectionReference doctorRef;
    private CollectionReference requestRef;
    private DoctorAdapterFiltred adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pat);

        doctorRef  = FirebaseFirestore.getInstance().collection("Doctor");
        requestRef = FirebaseFirestore.getInstance().collection("Request");

        configureToolbar();
        setUpRecyclerView();
    }

    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        toolbar.setTitle("Список врачей");
        setSupportActionBar(toolbar);
    }

    private void setUpRecyclerView() {
        RecyclerView rv = findViewById(R.id.serachPatRecycle);
        rv.setLayoutManager(new LinearLayoutManager(this));

        doctorRef.orderBy("fullName", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<Doctor> list = query.toObjects(Doctor.class);
                    adapter = new DoctorAdapterFiltred(list);
                    rv.setAdapter(adapter);

                    adapter.setOnAddDoctorClickListener(doctor -> {
                        String patientId = FirebaseAuth.getInstance()
                                .getCurrentUser().getEmail();
                        String doctorId  = doctor.getEmail();

                        // Проверка на дублирующий запрос (по email пациента и врача)
                        requestRef
                                .whereEqualTo("patientId", patientId)
                                .whereEqualTo("doctorId", doctorId)
                                .get()
                                .addOnSuccessListener(docs -> {
                                    if (!docs.isEmpty()) {
                                        Toast.makeText(this, "Вы уже отправили запрос этому врачу", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Создание контроль-запроса с chemin (если нужно для другой логики)
                                        // String chemin будет формироваться, если контроль
                                        Map<String, Object> req = new HashMap<>();
                                        req.put("patientId", patientId);
                                        req.put("doctorId", doctorId);

                                        // Если это не просто Request, а ControlRequest
                                        // String chemin = "ControlRequests/" + docRef.getId();
                                        // req.put("chemin", chemin);

                                        requestRef.add(req)
                                                .addOnSuccessListener(doc -> Toast.makeText(
                                                        this,
                                                        "Запрос отправлен врачу",
                                                        Toast.LENGTH_SHORT
                                                ).show())
                                                .addOnFailureListener(e -> Toast.makeText(
                                                        this,
                                                        "Ошибка: " + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show());
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(
                                        this,
                                        "Ошибка проверки заявки: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки докторов: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_bar, menu);

        Drawable icon = getResources().getDrawable(R.drawable.ic_local_hospital_black_24dp);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        SpannableString sb = new SpannableString(" Specialité");
        sb.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SearchView sv = (SearchView) menu.findItem(R.id.action_search).getActionView();
        sv.setQueryHint(Html.fromHtml("<font color='#000000'>Search doctor</font>"));
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q)  { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                DoctorAdapterFiltred.specialiteSearch = false;
                if (adapter != null) adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_all:
                DoctorAdapterFiltred.specialiteSearch = true;
                if (adapter != null) adapter.getFilter().filter("");
                return true;
            case R.id.option_general:
                DoctorAdapterFiltred.specialiteSearch = true;
                if (adapter != null) adapter.getFilter().filter("Médecin général");
                return true;
            // … остальные пункты …
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
