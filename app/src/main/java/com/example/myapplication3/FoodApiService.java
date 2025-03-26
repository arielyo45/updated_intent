package com.example.myapplication3;
import Model.FoodQuery;
import Model.NutritionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


// nutritionix
public interface FoodApiService {
    @Headers({
            "x-app-id: b8029c45",
            "x-app-key: 16b2d1fc46f643aa2b155d77ac1db01e",
            "Content-Type: application/json"
    })
    @POST("v2/natural/nutrients")
    Call<NutritionResponse> getFoodData(@Body FoodQuery foodQuery);
}