package com.example.myapplication3;

import ChatModel.ChatGptRequest;
import ChatModel.ChatGptResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatAPI {

    @Headers({
            "Content-Type: application/json",
            "Authorization: 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"
    })
    @POST("v1/chat/completions")
    Call<ChatGptResponse> getChatResponse(@Body ChatGptRequest request);
}
