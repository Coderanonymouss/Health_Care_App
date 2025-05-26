package com.ensias.healthcareapp.patient;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Активити для просмотра видеоуроков в папке
public class PatientVideosActivity extends AppCompatActivity {

    ListView listVideos;
    List<String> videoTitles = new ArrayList<>();
    List<String> videoUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_videos);

        listVideos = findViewById(R.id.list_videos);

        String folderId = getIntent().getStringExtra("folderId");
        if (folderId != null) {
            loadVideosFromFolder(folderId);
        } else {
            Toast.makeText(this, "Папка табылмады", Toast.LENGTH_SHORT).show();
        }
    }

    // Получаем видеоуроки из выбранной папки
    private void loadVideosFromFolder(String folderId) {
        FirebaseFirestore.getInstance()
                .collection("video_folders")
                .document(folderId)
                .collection("Videos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    videoTitles.clear();
                    videoUrls.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        videoTitles.add(doc.getString("title"));
                        videoUrls.add(doc.getString("videoUrl"));
                    }
                    updateVideosList();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Видеолар жүктелмеді", Toast.LENGTH_SHORT).show());
    }

    // Показываем список видеоуроков (можно сделать с превью)
    private void updateVideosList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, videoTitles);
        listVideos.setAdapter(adapter);

        // по клику можно запускать видео плеер, например открыть видео по ссылке
        listVideos.setOnItemClickListener((parent, view, position, id) -> {
            String url = videoUrls.get(position);
            // тут можно открыть свое видео-активити, или просто:
            // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // startActivity(intent);
        });
    }
}
