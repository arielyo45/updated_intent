package com.example.myapplication3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import FoodModel.FoodItem;
import FoodModel.FoodQuery;
import FoodModel.NutritionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FoodTrack extends AppCompatActivity {
    private TextView textView;
    private FoodApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_track);

        textView = findViewById(R.id.FoodTrack);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://trackapi.nutritionix.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(FoodApiService.class);

        fetchFoodData("apple");  // Example query
    }

    private void fetchFoodData(String foodName) {
        FoodQuery foodQuery = new FoodQuery(foodName);
        Call<NutritionResponse> call = apiService.getFoodData(foodQuery);

        call.enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FoodItem food = response.body().foods.get(0);
                    String result = "Food: " + food.foodName + "\n" +
                            "Calories: " + food.calories + " kcal\n" +
                            "Carbs: " + food.carbs + " g\n" +
                            "Fat: " + food.totalFat + " g\n" +
                            "Protein: " + food.protein + " g";

                    textView.setText(result);
                } else {
                    textView.setText("Food not found.");
                }
            }

            @Override
            public void onFailure(Call<NutritionResponse> call, Throwable t) {
                textView.setText("API request failed: " + t.getMessage());
            }
        });
    }
}