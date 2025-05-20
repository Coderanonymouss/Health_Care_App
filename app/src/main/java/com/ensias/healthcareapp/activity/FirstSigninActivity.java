package com.ensias.healthcareapp.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FirstSigninActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 71;

    private TextInputEditText fullName, birthday, tel;
    private Spinner specialiteSpinner;
    private Button confirmBtn, buttonSelectImage ;

    private ImageButton backButton;
    private ImageView profileImageView;

    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private Uri imageUri;

    private StorageReference storageReference;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_signin);

        // Инициализация UI
        backButton = findViewById(R.id.ic_back);
        profileImageView = findViewById(R.id.profileImageView);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        fullName = findViewById(R.id.firstSignFullName);
        birthday = findViewById(R.id.firstSignBirthDay);
        tel = findViewById(R.id.firstSignTel);
        specialiteSpinner = findViewById(R.id.specialite_spinner);
        confirmBtn = findViewById(R.id.confirmeBtn);

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        backButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(FirstSigninActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        // Выбор даты рождения
        birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FirstSigninActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        birthday.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Заполнение спиннера специальностей (предполагается, что есть массив в ресурсах)
        ArrayAdapter<CharSequence> adapterSpecialite = ArrayAdapter.createFromResource(
                this, R.array.specialite_spinner, android.R.layout.simple_spinner_item);
        adapterSpecialite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specialiteSpinner.setAdapter(adapterSpecialite);

        // Кнопка выбора фото
        buttonSelectImage.setOnClickListener(v -> chooseImage());

        // Кнопка подтверждения
        confirmBtn.setOnClickListener(v -> saveProfile());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfile() {
        String name = fullName.getText().toString().trim();
        String birth = birthday.getText().toString().trim();
        String phone = tel.getText().toString().trim();
        String specialite = specialiteSpinner.getSelectedItem().toString();
        String email = firebaseUser != null ? firebaseUser.getEmail() : null;

        if (name.isEmpty() || birth.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseUser == null || email == null) {
            Toast.makeText(this, "Ошибка пользователя. Пожалуйста, войдите заново.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Загружаем фото в Firebase Storage, если выбрали
        if (imageUri != null) {
            StorageReference imageRef = storageReference.child("profile_images/" + firebaseUser.getUid() + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUserData(name, birth, phone, specialite, email, uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
                        Log.e("UploadImage", e.getMessage(), e);
                    });
        } else {
            // Если фото не выбрано, сохраняем без фото
            saveUserData(name, birth, phone, specialite, email, null);
        }
    }

    private void saveUserData(String name, String birth, String phone, String specialite, String email, String photoUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("fullName", name);
        userData.put("birthday", birth);
        userData.put("tel", phone);
        userData.put("type", "Doctor");
        userData.put("specialite", specialite);
        userData.put("firstSigninCompleted", true);
        if (photoUrl != null) {
            userData.put("photoUrl", photoUrl);
        }

        db.collection("User").document(firebaseUser.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    db.collection("Doctor").document(firebaseUser.getUid())
                            .set(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Доктор успешно зарегистрирован", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка при регистрации врача", Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error saving doctor data", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка сохранения данных пользователя", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error saving user data", e);
                });
    }
}
