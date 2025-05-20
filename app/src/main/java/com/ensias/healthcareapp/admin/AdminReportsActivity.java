package com.ensias.healthcareapp.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class AdminReportsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);
        setTitle("Есептер");

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView reportsText = findViewById(R.id.textReports);
        // Здесь можно получить данные с Firestore или API и отобразить

        reportsText.setText("Бұл жерде есептер мен статистика шығады.");
    }
}
