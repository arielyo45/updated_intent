package com.example.myapplication3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import Model.FoodItem;
import Model.FoodResponse;
import Model.Nutrient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodTrack extends AppCompatActivity {
    private static final String API_KEY = "6pNY3wwk4sclwlCwcm64bVdBSzeMweg425DZzIGg"; // Replace with your USDA API key
    private TextView textView;
    String resultText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_track);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textView = findViewById(R.id.FoodTrack);
        fetchFoodData("Chicken, ground, with additives, raw\n");
    }
    private void fetchFoodData(String query) {
        FoodApiService apiService = RetrofitClient.getClient().create(FoodApiService.class);
        Call<FoodResponse> call = apiService.searchFood(query, API_KEY);

        call.enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse foodResponse = response.body();
                    if (!foodResponse.foods.isEmpty()) {
                        FoodItem food = foodResponse.foods.get(0);  // Get the first food result
                         resultText = "Food: " + food.description + "\nNutrients:\n";

                        for (Nutrient nutrient : food.foodNutrients) {
                            resultText += nutrient.nutrientName + ": " + nutrient.value + " " + nutrient.unitName + "\n";
                        }

                        textView.setText(resultText);
                    } else {
                        textView.setText("No results found.");
                    }
                } else {
                    textView.setText("API response failed.");
                }
            }

            @Override
            public void onFailure(Call<FoodResponse> call, Throwable t) {
                textView.setText("Request failed: " + t.getMessage());
            }
        });
    }
}