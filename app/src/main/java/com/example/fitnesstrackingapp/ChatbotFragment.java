package com.example.fitnesstrackingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class ChatbotFragment extends Fragment {
    private LottieAnimationView typingIndicator;
    private static final String API_URL = "https://huyhoang04-fitness.hf.space/chat";
    private static final int MAX_HISTORY_SIZE = 2;
    private ChatAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .build();

    // Current emotion prompt
    private String currentEmotionPrompt = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        typingIndicator = view.findViewById(R.id.typingIndicator);
        typingIndicator.setVisibility(View.GONE);

        // Setup RecyclerView with stackFromEnd=true
        RecyclerView rv = view.findViewById(R.id.rvChat);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        adapter = new ChatAdapter(messages);
        rv.setAdapter(adapter);

        EditText et = view.findViewById(R.id.etMessage);
        FloatingActionButton btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (text.isEmpty()) return;
            addMessage(text, true);
            et.setText("");
            callFastAPI(text);
        });

        // Initialize emotion buttons
        view.findViewById(R.id.btnHappy)
                .setOnClickListener(v -> selectEmotion("Vui vẻ",
                        "Please adopt a bright, energetic voice full of warmth and enthusiasm—think sunshine, smiles, and an irresistible sense of optimism"));
        view.findViewById(R.id.btnSad)
                .setOnClickListener(v -> selectEmotion("Trầm lắng",
                        "Please speak in a quietly somber, reflective tone that conveys heartfelt sorrow and poetic depth, as if sharing a bittersweet memory"));
        view.findViewById(R.id.btnAngry)
                .setOnClickListener(v -> selectEmotion("Cởi mở",
                        "Please respond as an open-hearted, welcoming companion—warm, approachable, and eager to connect."));
        view.findViewById(R.id.btnNeutral)
                .setOnClickListener(v -> selectEmotion("Điềm tĩnh",
                        "Please maintain a calm, impartial tone—clear, concise, and free of bias, focusing strictly on facts and logical clarity"));

        return view;
    }

    private void selectEmotion(String label, String prompt) {
        currentEmotionPrompt = prompt;
        if (getContext() != null) {
            Toast.makeText(getContext(), "Cảm xúc: " + label, Toast.LENGTH_SHORT).show();
        }
    }

    private void addMessage(String text, boolean isUser) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            messages.add(new ChatMessage(text, isUser));
            int pos = messages.size() - 1;
            adapter.notifyItemInserted(pos);

            // Scroll RecyclerView to bottom
            RecyclerView rv = getView().findViewById(R.id.rvChat);
            rv.scrollToPosition(pos);
        });
    }

    private void callFastAPI(String userText) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> typingIndicator.setVisibility(View.VISIBLE));
        
        try {
            // Combine emotion prompt with user message
            String combined = currentEmotionPrompt.isEmpty()
                    ? userText
                    : currentEmotionPrompt + "\n" + userText;

            // Create limited history list
            JSONArray historyArray = new JSONArray();
            int startIdx = Math.max(0, messages.size() - MAX_HISTORY_SIZE);
            for (int i = startIdx; i < messages.size(); i++) {
                ChatMessage msg = messages.get(i);
                historyArray.put(msg.getContent());
            }

            // Create request body according to new API format
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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> typingIndicator.setVisibility(View.GONE));
                    }
                }

                @Override
                public void onResponse(Call call, Response resp) throws IOException {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> typingIndicator.setVisibility(View.GONE));
                    }
                    
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