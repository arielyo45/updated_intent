package com.example.myapplication3;

import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class ChatCall {

    private static final String API_KEY = "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";
    private static final String URL = "https://api.openai.com/v1/chat/completions";

    public abstract void onTipReceived(String tip);

    public interface OpenAICallback {
        void onTipReceived(String tip);
        void onError(String error);
    }

    public static void sendToChat(String prompt, final OpenAICallback callback) {
        RequestQueue queue = Volley.newRequestQueue(MyApplication.getAppContext());

        JSONObject body = new JSONObject();
        try {
            body.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a professional health and fitness advisor. Give realistic and encouraging tips.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            messages.put(systemMessage);
            messages.put(userMessage);

            body.put("messages", messages);

        } catch (JSONException e) {
            callback.onError("Error creating JSON body: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, body,
                response -> {
                    try {
                        String tip = response.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        callback.onTipReceived(tip.trim());
                    } catch (JSONException e) {
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "OpenAI request failed: ";
                    if (error.networkResponse != null) {
                        errorMsg += "Status Code: " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            errorMsg += "\nResponse: " + new String(error.networkResponse.data);
                        }
                    } else {
                        errorMsg += error.toString();
                    }
                    Log.e("ChatCall", errorMsg); // הדפסה ללוג
                    callback.onError(errorMsg);  // או הצגה למשתמש
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }
}
