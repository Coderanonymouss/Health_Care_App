package com.ensias.healthcareapp.patient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ensias.healthcareapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class FirstSigninPatientActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;

    private TextInputEditText editPhone, editAddress;
    private TextInputLayout addressLayout;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_signin_patient);

        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        addressLayout = findViewById(R.id.address);
        findViewById(R.id.btnConfirm).setOnClickListener(v -> savePatientInfo());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadUserInfo();

        addressLayout.setEndIconOnClickListener(v -> requestLocationPermissionAndFetchAddress());
    }

    private void loadUserInfo() {
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("User").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String phone = document.getString("tel");
                        if (phone != null) editPhone.setText(phone);

                        String address = document.getString("address");
                        if (address != null) editAddress.setText(address);
                    } else {
                        Toast.makeText(this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show());
    }

    private void savePatientInfo() {
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните телефон", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("tel", phone);
        updates.put("address", address);
        updates.put("firstSigninCompleted", true);

        db.collection("User").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FirstSigninPatientActivity.this,
                            com.ensias.healthcareapp.activity.patient.HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show());
        db.collection("Patient").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FirstSigninPatientActivity.this,
                            com.ensias.healthcareapp.activity.patient.HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show());
    }

    private void requestLocationPermissionAndFetchAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(FirstSigninPatientActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String fullAddress = addresses.get(0).getAddressLine(0);
                                editAddress.setText(fullAddress);
                                Toast.makeText(this, "Адрес определён", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Адрес не найден", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Местоположение не найдено", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Разрешение на определение местоположения отклонено", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
