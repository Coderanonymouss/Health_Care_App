package com.ensias.healthcareapp.doctor.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.ProfileDoctorActivity;
import com.ensias.healthcareapp.activity.patient.ProfilePatientActivity;
import com.ensias.healthcareapp.doctor.adapter.MessageAdapter;
import com.ensias.healthcareapp.model.Message;
import com.ensias.healthcareapp.patient.chat.ProfileDoctorInPatientActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private CollectionReference MessageRef1, MessageRef2;
    private MessageAdapter adapter;
    private TextInputEditText envoyer;
    private ImageButton btnSend, btnPhoto, btnAudio, btnSendAudio;
    private MediaRecorder recorder;
    private String audioFilePath;
    private boolean isRecording = false;

    private LinearLayout recordPanel, messageInputPanel;
    private TextView recordTimer;
    private ImageView voiceWave;

    private Handler timerHandler = new Handler();
    private long recordStartTime;

    private String companionRole;
    private String companionId, companionName, companionPhotoUrl;
    private String myName;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        companionRole = getIntent().getStringExtra("companionRole");
        companionId = getIntent().getStringExtra("companionId");
        companionName = getIntent().getStringExtra("companionName");
        companionPhotoUrl = getIntent().getStringExtra("companionPhotoUrl");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get()
                .addOnSuccessListener(doc -> myName = doc.contains("name") ? doc.getString("name") : "Вы");

        // Header
        TextView headerName = findViewById(R.id.header_name);
        ImageView headerAvatar = findViewById(R.id.header_avatar);
        headerName.setText(companionName != null ? companionName : "Пользователь");
        if (companionPhotoUrl != null && !companionPhotoUrl.isEmpty()) {
            Picasso.get().load(companionPhotoUrl).placeholder(R.drawable.ic_account_circle).into(headerAvatar);
        } else {
            headerAvatar.setImageResource(R.drawable.ic_account_circle);
        }
        findViewById(R.id.chat_header).setOnClickListener(v -> {
            Intent intent;
            if ("Doctor".equals(companionRole)) {
                intent = new Intent(ChatActivity.this, ProfileDoctorInPatientActivity.class);
            } else {

                intent = new Intent(ChatActivity.this, ProfilePatientActivity.class);
            }
            intent.putExtra("userId", companionId);
            startActivity(intent);
        });

        // Chat
        Bundle extras = getIntent().getExtras();
        MessageRef1 = FirebaseFirestore.getInstance().collection("chat")
                .document(Objects.requireNonNull(extras.getString("key1")))
                .collection("message");
        MessageRef2 = FirebaseFirestore.getInstance().collection("chat")
                .document(Objects.requireNonNull(extras.getString("key2")))
                .collection("message");

        envoyer = findViewById(R.id.activity_mentor_chat_message_edit_text);
        btnSend = findViewById(R.id.activity_mentor_chat_send_button);
        btnPhoto = findViewById(R.id.btn_attach_photo);
        btnAudio = findViewById(R.id.btn_audio);

        messageInputPanel = findViewById(R.id.message_input_panel);
        recordPanel = findViewById(R.id.record_panel);
        btnSendAudio = findViewById(R.id.btn_send_audio);
        recordTimer = findViewById(R.id.record_timer);
        voiceWave = findViewById(R.id.voice_wave);

        // --------- RecyclerView & LayoutManager ----------
        recyclerView = findViewById(R.id.activity_mentor_chat_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Важно! Чат всегда снизу
        recyclerView.setLayoutManager(layoutManager);

        Query query = MessageRef1.orderBy("dateCreated");
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();
        adapter = new MessageAdapter(options, this);
        recyclerView.setAdapter(adapter);

        // Автоскролл при новом сообщении
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });

        btnSend.setOnClickListener(v -> sendText());
        btnPhoto.setOnClickListener(v -> choosePhoto());

        btnAudio.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    showRecordPanel();
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    long elapsed = System.currentTimeMillis() - recordStartTime;
                    stopRecording();
                    hideRecordPanel();
                    if (elapsed > 500) { // Не отправлять слишком короткие записи
                        new Handler().postDelayed(this::uploadAudio, 500);
                    } else {
                        if (audioFilePath != null) new File(audioFilePath).delete();
                    }
                    return true;
            }
            return false;
        });

        btnSendAudio.setOnClickListener(v -> {
            uploadAudio();
            hideRecordPanel();
        });
    }

    private void showRecordPanel() {
        recordPanel.setVisibility(View.VISIBLE);
        messageInputPanel.setVisibility(View.GONE);
        recordTimer.setText("00:00");
        recordStartTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
    }

    private void hideRecordPanel() {
        recordPanel.setVisibility(View.GONE);
        messageInputPanel.setVisibility(View.VISIBLE);

        envoyer.setEnabled(true);
        envoyer.setFocusable(true);
        envoyer.setFocusableInTouchMode(true);
        envoyer.requestFocus();
        envoyer.setAlpha(1.0f);
        envoyer.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(envoyer, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        timerHandler.removeCallbacks(timerRunnable);
        recordTimer.setText("00:00");
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - recordStartTime;
            int seconds = (int) (elapsed / 1000);
            recordTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
            timerHandler.postDelayed(this, 200);
        }
    };

    private void sendText() {
        String msgText = Objects.requireNonNull(envoyer.getText()).toString();

        // Обрезаем пробелы и энтеры В НАЧАЛЕ и В КОНЦЕ, но не трогаем в середине
        String trimmed = msgText.trim();

        // Если после trim() строка пустая — не отправляем!
        if (trimmed.isEmpty()) return;

        String sender = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        Message msg = new Message(trimmed, null, sender, myName, "text"); // trimmed, а не msgText!
        MessageRef1.document().set(msg);
        MessageRef2.document().set(msg);
        envoyer.setText("");
    }



    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        String fileName = "chat_images/" + System.currentTimeMillis() + ".jpg";
        FirebaseStorage.getInstance().getReference().child(fileName)
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String sender = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                            Message imageMsg = new Message(null, uri.toString(), sender, myName, "image");
                            MessageRef1.document().set(imageMsg);
                            MessageRef2.document().set(imageMsg);
                        })
                );
    }

    private void startRecording() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 123);
            return;
        }
        audioFilePath = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath()
                + "/audio_" + System.currentTimeMillis() + ".mp4";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(96000);
        recorder.setOutputFile(audioFilePath);
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            }
        }
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                recorder.stop();
                recorder.release();
                isRecording = false;
            } catch (Exception e) {
                Log.e("AUDIO", "Ошибка при остановке записи: " + e.getMessage());
            }
        }
    }

    private void uploadAudio() {
        if (audioFilePath == null) return;
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists() || audioFile.length() < 1000) {
            Log.d("AUDIO", "Файл слишком маленький или отсутствует, не отправляем.");
            return;
        }
        Uri fileUri = Uri.fromFile(audioFile);
        String fileName = "chat_audio/" + System.currentTimeMillis() + ".mp4";
        int durationMs = 0;
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(audioFile.getAbsolutePath());
            mp.prepare();
            durationMs = mp.getDuration();
            mp.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (durationMs < 500) {
            audioFile.delete();
            return;
        }
        int finalDurationMs = durationMs;
        FirebaseStorage.getInstance().getReference().child(fileName)
                .putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String sender = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                            Message audioMsg = new Message(null, uri.toString(), sender, myName, "audio", finalDurationMs);
                            MessageRef1.document().set(audioMsg);
                            MessageRef2.document().set(audioMsg);
                        })
                );
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

        // Скролл вниз при старте
        new Handler().postDelayed(() -> {
            if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        }, 300);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
