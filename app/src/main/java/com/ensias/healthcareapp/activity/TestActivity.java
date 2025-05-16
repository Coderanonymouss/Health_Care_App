package com.ensias.healthcareapp.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.databinding.ActivityTestBinding;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Common.CurrentUserid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Common.CurrentUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        // 👉 Подтверждаем при запуске
        confirmAppointment();
    }
    private void confirmAppointment() {
        String formattedDate = Common.simpleFormat.format(Common.currentDate.getTime());

        ApointementInformation apInfo = new ApointementInformation();
        apInfo.setApointementType(Common.Currentaappointementatype);
        apInfo.setDoctorId(Common.CurreentDoctor);
        apInfo.setDoctorName(Common.CurrentDoctorName);
        apInfo.setPatientName(Common.CurrentUserName);
        apInfo.setPatientId(Common.CurrentUserid);
        apInfo.setChemin("Doctor/" + Common.CurreentDoctor + "/" + formattedDate + "/" + Common.currentTimeSlot);
        apInfo.setType("Checked");
        apInfo.setTime(Common.convertTimeSlotToString(Common.currentTimeSlot) + " at " + formattedDate);
        apInfo.setSlot((long) Common.currentTimeSlot);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 👉 Создаём запись в расписании врача
        db.collection("Doctor")
                .document(Common.CurreentDoctor)
                .collection(formattedDate)
                .document(String.valueOf(Common.currentTimeSlot))
                .set(apInfo)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Консультация успешно назначена!", Toast.LENGTH_SHORT).show();
                    finish(); // Закрываем экран
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show())
                .addOnCompleteListener(task -> {
                    // 👉 Добавляем запрос врачу
                    db.collection("Doctor")
                            .document(Common.CurreentDoctor)
                            .collection("apointementrequest")
                            .document(apInfo.getTime().replace("/", "_"))
                            .set(apInfo);

                    // 👉 Добавляем отправленный запрос пациенту
                    db.collection("Patient")
                            .document(Common.CurrentUserid)
                            .collection("apointementrequest")
                            .document(apInfo.getTime().replace("/", "_"))
                            .set(apInfo);

                    // 👉 Также добавляем в календарь пациента (для удобства)
                    db.collection("Patient")
                            .document(Common.CurrentUserid)
                            .collection("calendar")
                            .document(apInfo.getTime().replace("/", "_"))
                            .set(apInfo);
                });
    }

}
