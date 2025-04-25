// FoodTrack.java
package com.example.myapplication3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import FoodModel.FoodItem;
import FoodModel.FoodQuery;
import FoodModel.NutritionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FoodTrack extends AppCompatActivity {
    private TextView resultTextView;
    private EditText foodInputEditText;
    private Button addFoodButton;
    private TableLayout foodTableLayout;
    private TextView totalCaloriesTextView;
    private FoodApiService apiService;

    // List to store food entries for the day
    private List<FoodEntry> foodEntries = new ArrayList<>();
    private float totalCalories = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_track);

        // Initialize UI components
        resultTextView = findViewById(R.id.resultTextView);
        foodInputEditText = findViewById(R.id.foodInputEditText);
        addFoodButton = findViewById(R.id.addFoodButton);
        foodTableLayout = findViewById(R.id.foodTableLayout);
        totalCaloriesTextView = findViewById(R.id.totalCaloriesTextView);

        // Set up Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://trackapi.nutritionix.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(FoodApiService.class);

        // Set up add food button click listener
        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String foodName = foodInputEditText.getText().toString().trim();
                if (!foodName.isEmpty()) {
                    fetchFoodData(foodName);
                } else {
                    Toast.makeText(FoodTrack.this, "Please enter a food item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize total calories display
        updateTotalCalories();
    }

    private void fetchFoodData(String foodName) {
        resultTextView.setText("Searching for: " + foodName);

        FoodQuery foodQuery = new FoodQuery(foodName);
        Call<NutritionResponse> call = apiService.getFoodData(foodQuery);

        call.enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().foods.isEmpty()) {
                    FoodItem food = response.body().foods.get(0);
                    String result = "Found: " + food.foodName + " (" + food.calories + " kcal)";
                    resultTextView.setText(result);

                    // Add to food entries and update table
                    addFoodEntry(food);

                    // Clear the input field
                    foodInputEditText.setText("");
                } else {
                    resultTextView.setText("Food not found. Try something else.");
                }
            }

            @Override
            public void onFailure(Call<NutritionResponse> call, Throwable t) {
                resultTextView.setText("API request failed: " + t.getMessage());
            }
        });
    }

    private void addFoodEntry(FoodItem food) {
        // Create a new food entry
        FoodEntry entry = new FoodEntry(
                food.foodName,
                food.calories,
                food.carbs,
                food.protein,
                food.totalFat
        );

        // Add to our list
        foodEntries.add(entry);

        // Add to the table
        TableRow row = new TableRow(this);

        // Food name column
        TextView nameView = new TextView(this);
        nameView.setText(food.foodName);
        nameView.setPadding(10, 10, 10, 10);
        row.addView(nameView);

        // Calories column
        TextView calView = new TextView(this);
        calView.setText(String.format("%.0f", food.calories));
        calView.setPadding(10, 10, 10, 10);
        row.addView(calView);

        // Delete button column
        Button deleteButton = new Button(this);
        deleteButton.setText("X");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove from entries list
                foodEntries.remove(entry);
                // Remove from table
                foodTableLayout.removeView(row);
                // Update total
                updateTotalCalories();
            }
        });
        row.addView(deleteButton);

        // Add the row to the table
        foodTableLayout.addView(row);

        // Update total calories
        updateTotalCalories();
    }

    private void updateTotalCalories() {
        totalCalories = 0;
        for (FoodEntry entry : foodEntries) {
            totalCalories += entry.calories;
        }
        totalCaloriesTextView.setText(String.format("Total Calories: %.0f", totalCalories));
    }

    // Helper class to store food entries
    private static class FoodEntry {
        String name;
        float calories;
        float carbs;
        float protein;
        float fat;

        FoodEntry(String name, float calories, float carbs, float protein, float fat) {
            this.name = name;
            this.calories = calories;
            this.carbs = carbs;
            this.protein = protein;
            this.fat = fat;
        }
    }
}