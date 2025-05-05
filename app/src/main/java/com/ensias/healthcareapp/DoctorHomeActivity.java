package com.ensias.healthcareapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.databinding.ActivityDoctorHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class DoctorHomeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private ActivityDoctorHomeBinding binding; // ✅ правильный биндинг

    static String doc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorHomeBinding.inflate(getLayoutInflater()); // ✅ биндим layout активности
        setContentView(binding.getRoot());

        // Установить глобальные переменные
        Common.CurreentDoctor = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Common.CurrentUserType = "doctor";

        // Профиль
        binding.profile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileDoctorActivity.class));
        });

        // Календарь
        binding.myCalendarBtn.setOnClickListener(v -> {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MyCalendarDoctorActivity.class));
        });

        // Выход из аккаунта
        binding.signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Подтвержденные записи
        binding.btnRequst.setOnClickListener(v -> {
            startActivity(new Intent(this, ConfirmedAppointmensActivity.class));
        });

        // Список пациентов
        binding.listPatients.setOnClickListener(v -> {
            startActivity(new Intent(this, MyPatientsActivity.class));
        });

        // Переход к приему
        binding.appointement.setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorAppointementActivity.class));
        });
    }

    public void showDatePickerDialog(Context context) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = "month_day_year: " + month + "_" + dayOfMonth + "_" + year;
        openPage(view.getContext(), doc, date);
    }

    private void openPage(Context context, String doctorEmail, String day) {
        Intent intent = new Intent(context, AppointementActivity.class);
        intent.putExtra("key1", doctorEmail);
        intent.putExtra("key2", day);
        intent.putExtra("key3", "doctor");
        context.startActivity(intent);
    }
}
