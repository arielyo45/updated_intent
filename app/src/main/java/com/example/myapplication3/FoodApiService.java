package com.example.myapplication3;
import Model.FoodResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


// API interface for USDA FoodData Central
public interface FoodApiService {
    @GET("foods/search")
    Call<FoodResponse> searchFood(
            @Query("query") String query,
            @Query("api_key") String apiKey
    );
}