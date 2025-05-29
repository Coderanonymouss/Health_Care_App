package com.ensias.healthcareapp.patient.search;

import android.os.Bundle;
import android.util.Log; // <--- добавь этот импорт
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.patient.search.adapter.ChatAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<String> messages = new ArrayList<>();

    private final String PROXY_URL = "https://health-backend-d1ug.onrender.com/api/ai-chat";
    private static final String TAG = "AIChat"; // тег для логов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        chatAdapter = new ChatAdapter(messages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = editTextMessage.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    messages.add("Вы: " + userMessage);
                    chatAdapter.notifyDataSetChanged();
                    editTextMessage.setText("");
                    sendMessageToProxy(userMessage);
                }
            }
        });
    }

    private void sendMessageToProxy(String userMessage) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("message", userMessage);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonBody.toString()
            );

            Request request = new Request.Builder()
                    .url(PROXY_URL)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Log.d(TAG, "Отправка запроса на сервер: " + userMessage);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Ошибка сети: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(AIChatActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String res = response.body().string();
                            Log.d(TAG, "Ответ от сервера: " + res);

                            JSONObject jsonResponse = new JSONObject(res);
                            String aiMessage = jsonResponse.getString("response");

                            runOnUiThread(() -> {
                                messages.add("ИИ: " + aiMessage.trim());
                                chatAdapter.notifyDataSetChanged();
                                recyclerViewChat.scrollToPosition(messages.size() - 1);
                                // Временный Toast для подтверждения работы
                                Toast.makeText(AIChatActivity.this, "Ответ от ИИ получен!", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка разбора ответа: " + e.getMessage());
                            runOnUiThread(() -> Toast.makeText(AIChatActivity.this, "Ошибка разбора ответа", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "нет тела";
                        Log.e(TAG, "Ошибка AI сервера: " + errorBody);
                        runOnUiThread(() -> Toast.makeText(AIChatActivity.this, "Ошибка AI сервера: " + errorBody, Toast.LENGTH_LONG).show());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
