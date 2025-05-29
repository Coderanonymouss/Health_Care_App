package com.ensias.healthcareapp.activity.patient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import com.ensias.healthcareapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.*;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.google.android.gms.location.*;

public class EditProfilePatientActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String TAG = "EditProfilePatient";

    private FusedLocationProviderClient fusedLocationClient;

    private ImageView profileImage;
    private TextInputEditText nameText, phoneText, addressText;

    private Uri uriImage;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private String patientUid;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_patient);

        firestore = FirebaseFirestore.getInstance();
        patientUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        storageRef = FirebaseStorage.getInstance().getReference("PatientProfile");

        profileImage = findViewById(R.id.image_profile);
        ImageButton selectImage = findViewById(R.id.select_image);
        Button updateProfile = findViewById(R.id.update);
        phoneText = findViewById(R.id.phoneText);
        addressText = findViewById(R.id.addressText);
        TextInputLayout addressLayout = findViewById(R.id.address);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        phoneText.setText(intent.getStringExtra("CURRENT_PHONE"));
        addressText.setText(intent.getStringExtra("CURRENT_ADDRESS"));



        // Загрузка и обновление фото
        storageRef.child(patientUid + ".jpg").getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).placeholder(R.drawable.ic_user_placeholder).into(profileImage))
                .addOnFailureListener(e -> profileImage.setImageResource(R.drawable.ic_user_placeholder));

        selectImage.setOnClickListener(v -> openFileChooser());

        updateProfile.setOnClickListener(v -> {
            uploadProfileImage();
            updatePatientInfo(
                    Objects.requireNonNull(addressText.getText()).toString(),
                    Objects.requireNonNull(phoneText.getText()).toString()
            );
        });

        addressLayout.setEndIconOnClickListener(v -> requestLocationPermissionAndFetchAddress());
    }

    // Сохранять только адрес и телефон
    private void updatePatientInfo(String address, String phone) {
        DocumentReference document = firestore.collection("Patient").document(patientUid);
        document.update("address", address, "tel", phone)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Профиль жаңартылды", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfilePatientActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
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

    // Сохраняем фото по patientUid
    private void uploadProfileImage() {
        if (uriImage != null) {
            StorageReference fileRef = storageRef.child(patientUid + ".jpg");
            fileRef.putFile(uriImage)
                    .addOnSuccessListener(task -> Log.d(TAG, "Photo updated"))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updatePatientInfo(String name, String address, String phone) {
        DocumentReference document = firestore.collection("Patient").document(patientUid);
        document.update("fullName", name, "address", address, "tel", phone)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Профиль жаңартылды", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfilePatientActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                });
    }

    private void requestLocationPermissionAndFetchAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(EditProfilePatientActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String fullAddress = addresses.get(0).getAddressLine(0);
                                addressText.setText(fullAddress);
                                Toast.makeText(this, "Адрес анықталды", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Адрес табылмады", Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            Toast.makeText(this, "Орналасу рұқсаты қажет", Toast.LENGTH_SHORT).show();
        }
    }
}
