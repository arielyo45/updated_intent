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
