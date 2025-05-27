package com.example.ai_chatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private LottieAnimationView typingIndicator;
    // Thay đổi URL API sang FastAPI
    private static final String API_URL = "https://huyhoang04-fitness.hf.space/chat";
    private static final int MAX_HISTORY_SIZE = 2; // Giới hạn số lượng tin nhắn lưu trong history
    private ChatAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .build();

    // Biến lưu cảm xúc hiện tại
    private String currentEmotionPrompt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        typingIndicator = findViewById(R.id.typingIndicator);
        typingIndicator.setVisibility(View.GONE);

        // Thiết lập RecyclerView với stackFromEnd=true
        RecyclerView rv = findViewById(R.id.rvChat);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        adapter = new ChatAdapter(messages);
        rv.setAdapter(adapter);

        EditText et = findViewById(R.id.etMessage);
        FloatingActionButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (text.isEmpty()) return;
            addMessage(text, true);
            et.setText("");
            callFastAPI(text);
        });

        // Khởi tạo các nút cảm xúc
        findViewById(R.id.btnHappy)
                .setOnClickListener(v -> selectEmotion("Vui vẻ",
                        "Bạn hãy trả lời với giọng điệu vui vẻ, lạc quan, yêu đời"));
        findViewById(R.id.btnSad)
                .setOnClickListener(v -> selectEmotion("Buồn",
                        "Bạn hãy trả lời với giọng điệu buồn bã, trầm lắng nhưng vô cùng sâu sắc"));
        findViewById(R.id.btnAngry)
                .setOnClickListener(v -> selectEmotion("Cởi mở",
                        "Bạn hãy trả lời với tính cách vô cùng cởi mở, thân thiện, hòa đồng"));
        findViewById(R.id.btnNeutral)
                .setOnClickListener(v -> selectEmotion("Bình thường",
                        "Bạn hãy trả lời với giọng điệu trung lập, khách quan."));
    }

    private void selectEmotion(String label, String prompt) {
        currentEmotionPrompt = prompt;
        Toast.makeText(this, "Cảm xúc: " + label, Toast.LENGTH_SHORT).show();
    }

    private void addMessage(String text, boolean isUser) {
        runOnUiThread(() -> {
            messages.add(new ChatMessage(text, isUser));
            int pos = messages.size() - 1;
            adapter.notifyItemInserted(pos);

            // Cuộn RecyclerView xuống cuối
            RecyclerView rv = findViewById(R.id.rvChat);
            rv.scrollToPosition(pos);
        });
    }

    private void callFastAPI(String userText) {
        runOnUiThread(() -> typingIndicator.setVisibility(View.VISIBLE));
        try {
            // Gộp prompt cảm xúc vào cùng user message
            String combined = currentEmotionPrompt.isEmpty()
                    ? userText
                    : currentEmotionPrompt + "\n" + userText;

            // Tạo danh sách history giới hạn
            JSONArray historyArray = new JSONArray();
            int startIdx = Math.max(0, messages.size() - MAX_HISTORY_SIZE);
            for (int i = startIdx; i < messages.size(); i++) {
                ChatMessage msg = messages.get(i);
                historyArray.put(msg.getContent());
            }

            // Tạo request body theo định dạng API mới
            JSONObject body = new JSONObject()
                    .put("message", combined)
                    .put("history", historyArray)
                    .put("max_tokens", 80)
                    .put("temperature", 0.7);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(body.toString(),
                            MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    addMessage("Error: " + e.getMessage(), false);
                    runOnUiThread(() -> typingIndicator.setVisibility(View.GONE));
                }

                @Override
                public void onResponse(Call call, Response resp) throws IOException {
                    runOnUiThread(() -> typingIndicator.setVisibility(View.GONE));
                    if (!resp.isSuccessful()) {
                        addMessage("Failed: " + resp.message(), false);
                        return;
                    }
                    String json = resp.body().string();
                    try {
                        JSONObject o = new JSONObject(json);
                        String ai = o.getString("response");
                        addMessage(ai.trim(), false);
                    } catch (JSONException e) {
                        addMessage("Parse error", false);
                    }
                }
            });

        } catch (JSONException ex) {
            addMessage("JSON error", false);
        }
    }
}
