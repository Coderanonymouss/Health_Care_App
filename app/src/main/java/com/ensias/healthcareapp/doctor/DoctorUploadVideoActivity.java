package com.ensias.healthcareapp.doctor;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorUploadVideoActivity extends AppCompatActivity {
    EditText inputTitle, inputUrl;
    Spinner spinnerFolder;
    Button btnChooseGallery, btnUpload;
    TextView tvFileName;

    private List<String> folderIds = new ArrayList<>();
    private Uri selectedVideoUri = null;
    private String selectedFileName = "";

    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        inputTitle = findViewById(R.id.input_title); // Имя видеоурока
        inputUrl = findViewById(R.id.input_url);     // Ссылка YouTube
        spinnerFolder = findViewById(R.id.spinner_folder);
        btnChooseGallery = findViewById(R.id.btn_choose_gallery);
        btnUpload = findViewById(R.id.btn_upload);
        tvFileName = findViewById(R.id.tv_file_name);

        setupFolderSpinner();

        btnChooseGallery.setOnClickListener(v -> pickVideoFromGallery());
        btnUpload.setOnClickListener(v -> uploadVideo());
    }

    // Диалог загрузки
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
            loadingDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create();
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // Галерея
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedVideoUri = result.getData().getData();
                    if (selectedVideoUri != null) {
                        selectedFileName = getFileName(selectedVideoUri);
                        tvFileName.setText("Выбрано: " + selectedFileName);
                        inputUrl.setText(""); // если выбрали файл, URL обнуляем
                    }
                }
            });

    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        galleryLauncher.launch(intent);
    }

    private String getFileName(Uri uri) {
        String result = "";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0)
                        result = cursor.getString(nameIndex);
                }
            }
        }
        if (result.isEmpty()) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "";
    }

    private void setupFolderSpinner() {
        FirebaseFirestore.getInstance().collection("video_folders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> folderNames = new ArrayList<>();
                    folderIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        folderNames.add(doc.getString("name"));
                        folderIds.add(doc.getId());
                    }
                    if (folderNames.isEmpty()) folderNames.add("Папкалар жоқ");
                    ArrayAdapter<String> folderAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, folderNames);
                    folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerFolder.setAdapter(folderAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Папкалар жүктелмеді", Toast.LENGTH_SHORT).show());
    }

    private void uploadVideo() {
        String title = inputTitle.getText().toString().trim();
        String urlFromInput = inputUrl.getText().toString().trim();
        String doctorEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";
        int selectedIndex = spinnerFolder.getSelectedItemPosition();

        if (title.isEmpty()) {
            Toast.makeText(this, "Имя видеоурока обязательно!", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((urlFromInput.isEmpty() && selectedVideoUri == null)
                || folderIds.isEmpty() || selectedIndex < 0) {
            Toast.makeText(this, "Введите ссылку или выберите видео!", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog();

        String folderId = folderIds.get(selectedIndex);

        if (!urlFromInput.isEmpty()) {
            // Сохраняем ссылку (YouTube/интернет)
            saveVideoToFirestore(folderId, urlFromInput, doctorEmail, title);
        } else if (selectedVideoUri != null) {
            uploadVideoToStorageAndSave(folderId, selectedVideoUri, doctorEmail, title);
        }
    }

    private void uploadVideoToStorageAndSave(String folderId, Uri videoUri, String doctorEmail, String title) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("videos/" + System.currentTimeMillis() + "_" + title + ".mp4");

        storageRef.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            saveVideoToFirestore(folderId, downloadUrl, doctorEmail, title);
                        }))
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveVideoToFirestore(String folderId, String videoUrl, String doctorEmail, String title) {
        Map<String, Object> videoData = new HashMap<>();
        videoData.put("videoUrl", videoUrl);
        videoData.put("createdBy", doctorEmail);
        videoData.put("createdAt", FieldValue.serverTimestamp());
        videoData.put("title", title);

        FirebaseFirestore.getInstance()
                .collection("video_folders")
                .document(folderId)
                .collection("Videos")
                .add(videoData)
                .addOnSuccessListener(documentReference -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Видеоурок успешно сохранён", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, DoctorVideoListActivity.class);
                    intent.putExtra("folderId", folderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Ошибка Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
