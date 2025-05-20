package com.ensias.healthcareapp.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class AdminSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        setTitle("Параметрлер");

        // TODO: Добавить логику настроек (например, смена темы, параметры приложения и т.д.)

        Toast.makeText(this, "Параметрлер әзірленуде", Toast.LENGTH_SHORT).show();
    }
}
