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

    private static final String API_KEY = "AIzaSyBDS_iJmEubwS4VklJckj1RCyUgVdgUHo0";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyBDS_iJmEubwS4VklJckj1RCyUgVdgUHo0"
    ;

    public interface GeminiCallback {
        void onTipReceived(String tip);
        void onError(String error);
    }

    public static void sendToGemini(String userPrompt, final GeminiCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(MyApplication.getAppContext());

        String systemInstruction = "You are a professional health and fitness advisor. Give realistic and encouraging tips. dont make the tips too long make it 5-8 lines.";
        String fullPrompt = systemInstruction + "\nUser: " + userPrompt;

        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObj = new JSONObject();
        JSONArray parts = new JSONArray();

        try {
            JSONObject part = new JSONObject();
            part.put("text", fullPrompt);

            parts.put(part);
            contentObj.put("parts", parts);
            contents.put(contentObj);
            requestBody.put("contents", contents);
        } catch (JSONException e) {
            callback.onError("Error creating request body: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, requestBody,
                response -> {
                    try {
                        JSONArray candidates = response.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            String tip = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            callback.onTipReceived(tip.trim());
                        } else {
                            callback.onError("No response candidates received.");
                        }
                    } catch (JSONException e) {
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Gemini request failed: ";
                    if (error.networkResponse != null) {
                        errorMsg += "Status Code: " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            errorMsg += "\nResponse: " + new String(error.networkResponse.data);
                        }
                    } else {
                        errorMsg += error.toString();
                    }
                    callback.onError(errorMsg);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }
}
