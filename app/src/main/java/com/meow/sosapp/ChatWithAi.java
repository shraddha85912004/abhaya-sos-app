package com.meow.sosapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatWithAi extends AppCompatActivity {

    private EditText inputField;
    private ImageButton sendBtn;
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    
    // NOTE: Keep API key secure in production. Using it here for prototype as provided.
    private static final String API_KEY = "AIzaSyBvch0OqhjXfOuxKYavBPlEwAEi-mVLKhc";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        inputField = findViewById(R.id.inputField);
        sendBtn = findViewById(R.id.sendBtn);
        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);

        sendBtn.setOnClickListener(v -> sendMessage());

        // Add welcome message
        addMessage("AI", "Hello! I'm your safety assistant. Ask me anything about personal safety, emergency procedures, or self-defense tips.", false);
        
        // Add quick suggestions
        addSuggestions();
    }
    
    private void addSuggestions() {
        LinearLayout suggestionsLayout = new LinearLayout(this);
        suggestionsLayout.setOrientation(LinearLayout.HORIZONTAL);
        suggestionsLayout.setPadding(0, 16, 0, 16);
        
        String[] suggestions = {"Safety tips", "What to do if followed", "Self-defense basics"};
        
        for (String suggestion : suggestions) {
            TextView chip = new TextView(this);
            chip.setText(suggestion);
            chip.setTextColor(0xFFFFFFFF);
            chip.setBackgroundResource(R.drawable.bg_card);
            chip.setPadding(24, 12, 24, 12);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);
            
            chip.setOnClickListener(v -> {
                inputField.setText(suggestion);
                sendMessage();
                suggestionsLayout.setVisibility(View.GONE);
            });
            
            suggestionsLayout.addView(chip);
        }
        
        chatContainer.addView(suggestionsLayout);
    }

    private void sendMessage() {
        String userMessage = inputField.getText().toString().trim();
        if (userMessage.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        addMessage("You", userMessage, true);
        inputField.setText("");

        // Show typing indicator
        TextView typingIndicator = addTypingIndicator();

        // Call AI API in background thread
        new Thread(() -> {
            try {
                String response = callGeminiAPI(userMessage);
                runOnUiThread(() -> {
                    chatContainer.removeView(typingIndicator);
                    addMessage("AI", response, false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    chatContainer.removeView(typingIndicator);
                    addMessage("AI", "Error: " + e.getMessage(), false);
                });
            }
        }).start();
    }
    
    private TextView addTypingIndicator() {
        TextView typing = new TextView(this);
        typing.setText("AI is typing...");
        typing.setTextColor(0xFF888888);
        typing.setTextSize(12);
        typing.setPadding(16, 8, 16, 8);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        typing.setLayoutParams(params);
        chatContainer.addView(typing);
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
        
        return typing;
    }

    private void addMessage(String sender, String message, boolean isUser) {
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = isUser ? Gravity.END : Gravity.START;
        layoutParams.setMargins(0, 8, 0, 8);
        
        // Define max width (around 80% of screen)
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextColor(0xFFFFFFFF);
        messageView.setTextSize(16);
        messageView.setPadding(32, 24, 32, 24);
        messageView.setMaxWidth(maxWidth);
        
        if (isUser) {
            messageView.setBackgroundResource(R.drawable.bg_sos_button); // Recycled red styling
            messageView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE74C3C));
        } else {
            messageView.setBackgroundResource(R.drawable.bg_card); // Dark card style
        }
        
        messageLayout.addView(messageView);
        messageLayout.setLayoutParams(layoutParams);
        
        chatContainer.addView(messageLayout);
        
        // Auto scroll to bottom
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String callGeminiAPI(String message) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", "You are a safety assistant. Answer questions about personal safety, emergency procedures, and self-defense. Keep answers concise. Question: " + message);
        parts.put(part);
        JSONObject contentObj = new JSONObject();
        contentObj.put("parts", parts);
        JSONArray contents = new JSONArray();
        contents.put(contentObj);
        content.put("contents", contents);

        OutputStream os = conn.getOutputStream();
        os.write(content.toString().getBytes(StandardCharsets.UTF_8));
        os.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
    }
}