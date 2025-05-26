package com.ensias.healthcareapp.activity.doctor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.ProfileDoctorActivity;
import com.google.android.gms.location.*;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.*;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditProfileDoctorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String TAG = "EditProfileDoctor";

    private ImageView profileImage;
    private ImageButton selectImage;
    private Button updateProfile;
    private TextInputEditText nameText, phoneText, addressText;
    private TextInputLayout addressLayout;

    private Uri uriImage;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private String currentUserId;


    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_doctor);

        firestore = FirebaseFirestore.getInstance();
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        storageRef = FirebaseStorage.getInstance().getReference("DoctorProfile");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI elements
        profileImage = findViewById(R.id.image_profile);
        selectImage = findViewById(R.id.select_image);
        updateProfile = findViewById(R.id.update);
        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.phoneText);
        addressText = findViewById(R.id.addressText);
        addressLayout = findViewById(R.id.address);

        firestore.collection("Doctor").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nameText.setText(documentSnapshot.getString("fullName"));
                        phoneText.setText(documentSnapshot.getString("tel"));
                        addressText.setText(documentSnapshot.getString("adresse"));
                        // Можно добавить ещё поля, если нужны
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        storageRef.child(currentUserId + ".jpg").getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).placeholder(R.drawable.doctor).into(profileImage))
                .addOnFailureListener(e -> profileImage.setImageResource(R.drawable.ic_anon_user_48dp));

        // Загрузка текущего фото из Firebase Storage
        storageRef.child(currentUserId + ".jpg").getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).placeholder(R.drawable.doctor).into(profileImage))
                .addOnFailureListener(e -> profileImage.setImageResource(R.drawable.ic_anon_user_48dp));

        // Выбор фото
        selectImage.setOnClickListener(v -> openFileChooser());

        // Обновление данных
        updateProfile.setOnClickListener(v -> {
            uploadProfileImage();
            updateDoctorInfo(
                    nameText.getText().toString(),
                    addressText.getText().toString(),
                    phoneText.getText().toString()
            );
        });

        // Автозаполнение адреса
        addressLayout.setEndIconOnClickListener(v -> {
            Log.d("LOCATION_ICON", "Clicked!");
            requestLocationPermissionAndFetchAddress();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            Picasso.get().load(uriImage).into(profileImage);
        }
    }

    private void uploadProfileImage() {
        if (uriImage != null) {
            StorageReference fileRef = storageRef.child(currentUserId + ".jpg");
            fileRef.putFile(uriImage)
                    .addOnSuccessListener(task -> Log.d(TAG, "Фото врача обновлено"))
                    .addOnFailureListener(e -> Toast.makeText(this, "Суретті жүктеу қатесі: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateDoctorInfo(String name, String address, String phone) {
        DocumentReference document = firestore.collection("Doctor").document(currentUserId);
        document.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot docSnapshot = task.getResult();
                if (docSnapshot.exists()) {
                    // Документ существует, обновляем
                    document.update("fullName", name, "adresse", address, "tel", phone)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ProfileDoctorActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, e.getMessage());
                            });
                } else {
                    // Документ не существует, создаем
                    Map<String, Object> data = new HashMap<>();
                    data.put("fullName", name);
                    data.put("adresse", address);
                    data.put("tel", phone);
                    document.set(data)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Профиль создан", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ProfileDoctorActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, e.getMessage());
                            });
                }
            } else {
                Toast.makeText(this, "Ошибка при получении документа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, task.getException().getMessage());
            }
        });

    }

    private void requestLocationPermissionAndFetchAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
                        Geocoder geocoder = new Geocoder(EditProfileDoctorActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String fullAddress = addresses.get(0).getAddressLine(0);
                                addressText.setText(fullAddress);
                                Toast.makeText(this, "Мекенжай табылды", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Мекенжай табылмады", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Орналасу табылмады", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            Toast.makeText(this, "Орналасуға рұқсат қажет", Toast.LENGTH_SHORT).show();
        }
    }
}
