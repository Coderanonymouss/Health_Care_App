package com.ensias.healthcareapp.admin.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class EditDoctorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_doctor);
        setTitle("Докторды өзгерту");

        // TODO: загрузить данные доктора по id из Intent и реализовать редактирование
        Toast.makeText(this, "Бұл экран әзірленуде", Toast.LENGTH_SHORT).show();
    }
}
