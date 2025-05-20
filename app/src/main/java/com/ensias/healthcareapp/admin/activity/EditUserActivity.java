package com.ensias.healthcareapp.admin.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class EditUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        setTitle("Пайдаланушыны өзгерту");

        // TODO: загрузить данные пользователя по id и реализовать редактирование
        Toast.makeText(this, "Бұл экран әзірленуде", Toast.LENGTH_SHORT).show();
    }
}
