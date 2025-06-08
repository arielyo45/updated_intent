// FoodTrack.java
package com.example.myapplication3;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Spinner categorySpinner;
    private Spinner subcategorySpinner;
    private Spinner foodSpinner;
    private EditText gramsEditText;
    private EditText customFoodEditText;
    private LinearLayout customFoodLayout;
    private Button addFoodButton;
    private TableLayout foodTableLayout;
    private TextView totalCaloriesTextView;
    private FoodApiService apiService;

    // List to store food entries for the day
    private List<FoodItem> foodItems = new ArrayList<>();
    private float totalCalories = 0;

    // Food categories and subcategories
    private Map<String, Map<String, String[]>> foodCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_track);

        initializeViews();
        setupFoodCategories();
        setupRetrofit();
        setupSpinners();
        setupAddButton();
        updateTotalCalories();
    }

    private void initializeViews() {
        resultTextView = findViewById(R.id.resultTextView);
        categorySpinner = findViewById(R.id.categorySpinner);
        subcategorySpinner = findViewById(R.id.subcategorySpinner);
        foodSpinner = findViewById(R.id.foodSpinner);
        gramsEditText = findViewById(R.id.gramsEditText);
        customFoodEditText = findViewById(R.id.customFoodEditText);
        customFoodLayout = findViewById(R.id.customFoodLayout);
        addFoodButton = findViewById(R.id.addFoodButton);
        foodTableLayout = findViewById(R.id.foodTableLayout);
        totalCaloriesTextView = findViewById(R.id.totalCaloriesTextView);
    }

    private void setupFoodCategories() {
        foodCategories = new HashMap<>();

        // Meat category - expanded
        Map<String, String[]> meatSubcategories = new HashMap<>();
        meatSubcategories.put("Grilled", new String[]{"Grilled Chicken Breast", "Grilled Beef Steak", "Grilled Salmon", "Grilled Turkey", "Grilled Pork Chop", "Grilled Lamb", "Grilled Tuna", "Grilled Shrimp", "Grilled Duck Breast"});
        meatSubcategories.put("Raw", new String[]{"Raw Salmon Sashimi", "Raw Tuna Sashimi", "Beef Tartare", "Oysters", "Raw Scallops", "Ceviche", "Carpaccio"});
        meatSubcategories.put("Fried", new String[]{"Fried Chicken", "Fried Fish", "Fried Pork", "Fish and Chips", "Fried Shrimp", "Chicken Wings", "Fried Calamari", "Schnitzel"});
        meatSubcategories.put("Baked", new String[]{"Baked Chicken", "Baked Fish", "Baked Turkey", "Baked Cod", "Baked Salmon", "Roast Beef", "Baked Ham", "Meatloaf"});
        meatSubcategories.put("Processed", new String[]{"Bacon", "Sausage", "Ham", "Salami", "Pepperoni", "Hot Dog", "Deli Turkey", "Pastrami", "Chorizo"});
        foodCategories.put("Meat", meatSubcategories);

        // Vegetables category - expanded
        Map<String, String[]> vegetableSubcategories = new HashMap<>();
        vegetableSubcategories.put("Fresh", new String[]{"Fresh Spinach", "Fresh Lettuce", "Fresh Tomato", "Fresh Cucumber", "Fresh Carrot", "Bell Peppers", "Celery", "Onion", "Garlic", "Avocado", "Radish", "Cabbage", "Kale", "Arugula"});
        vegetableSubcategories.put("Cooked", new String[]{"Steamed Broccoli", "Boiled Potato", "Roasted Sweet Potato", "Sauteed Mushrooms", "Grilled Zucchini", "Roasted Brussels Sprouts", "Steamed Asparagus", "Baked Eggplant", "Stir-fried Vegetables"});
        vegetableSubcategories.put("Frozen", new String[]{"Frozen Peas", "Frozen Corn", "Frozen Mixed Vegetables", "Frozen Broccoli", "Frozen Spinach", "Frozen Green Beans", "Frozen Cauliflower"});
        vegetableSubcategories.put("Canned", new String[]{"Canned Tomatoes", "Canned Corn", "Canned Green Beans", "Canned Peas", "Canned Mushrooms", "Canned Artichokes", "Pickles"});
        foodCategories.put("Vegetables", vegetableSubcategories);

        // Drinks category - expanded
        Map<String, String[]> drinkSubcategories = new HashMap<>();
        drinkSubcategories.put("Hot", new String[]{"Coffee", "Green Tea", "Black Tea", "Hot Chocolate", "Chai Tea", "Herbal Tea", "Espresso", "Cappuccino", "Latte", "Matcha"});
        drinkSubcategories.put("Cold", new String[]{"Orange Juice", "Apple Juice", "Soda", "Iced Tea", "Water", "Sparkling Water", "Energy Drink", "Sports Drink", "Coconut Water", "Lemonade", "Smoothie"});
        drinkSubcategories.put("Alcoholic", new String[]{"Beer", "Wine", "Whiskey", "Vodka", "Rum", "Gin", "Tequila", "Cocktail", "Champagne", "Sake"});
        foodCategories.put("Drinks", drinkSubcategories);

        // Dairy category - expanded
        Map<String, String[]> dairySubcategories = new HashMap<>();
        dairySubcategories.put("Milk", new String[]{"Whole Milk", "Skim Milk", "Almond Milk", "Soy Milk", "Oat Milk", "Coconut Milk", "Rice Milk", "Cashew Milk", "2% Milk"});
        dairySubcategories.put("Cheese", new String[]{"Cheddar Cheese", "Mozzarella Cheese", "Parmesan Cheese", "Cottage Cheese", "Swiss Cheese", "Brie", "Feta", "Goat Cheese", "Blue Cheese", "Cream Cheese"});
        dairySubcategories.put("Yogurt", new String[]{"Greek Yogurt", "Regular Yogurt", "Frozen Yogurt", "Vanilla Yogurt", "Strawberry Yogurt", "Plain Yogurt", "Probiotic Yogurt"});
        dairySubcategories.put("Other", new String[]{"Butter", "Heavy Cream", "Sour Cream", "Ice Cream", "Whipped Cream", "Milk Powder"});
        foodCategories.put("Dairy", dairySubcategories);

        // Grains category - expanded
        Map<String, String[]> grainSubcategories = new HashMap<>();
        grainSubcategories.put("Rice", new String[]{"White Rice", "Brown Rice", "Jasmine Rice", "Basmati Rice", "Wild Rice", "Arborio Rice", "Sushi Rice", "Rice Pilaf"});
        grainSubcategories.put("Bread", new String[]{"White Bread", "Whole Wheat Bread", "Sourdough Bread", "Pita Bread", "Bagel", "English Muffin", "Croissant", "Baguette", "Rye Bread"});
        grainSubcategories.put("Pasta", new String[]{"Spaghetti", "Penne", "Macaroni", "Linguine", "Fettuccine", "Lasagna", "Ravioli", "Gnocchi", "Whole Wheat Pasta"});
        grainSubcategories.put("Cereal", new String[]{"Oatmeal", "Cornflakes", "Granola", "Muesli", "Cheerios", "Rice Krispies", "Bran Flakes", "Quinoa", "Barley"});
        grainSubcategories.put("Snacks", new String[]{"Crackers", "Pretzels", "Popcorn", "Rice Cakes", "Breadsticks", "Tortilla Chips"});
        foodCategories.put("Grains", grainSubcategories);

        // Fruits category - expanded
        Map<String, String[]> fruitSubcategories = new HashMap<>();
        fruitSubcategories.put("Fresh", new String[]{"Apple", "Banana", "Orange", "Grapes", "Strawberries", "Blueberries", "Pineapple", "Mango", "Kiwi", "Watermelon", "Cantaloupe", "Peach", "Pear", "Cherry", "Plum"});
        fruitSubcategories.put("Dried", new String[]{"Dried Apricots", "Raisins", "Dried Dates", "Dried Figs", "Dried Cranberries", "Dried Mango", "Prunes", "Dried Banana Chips"});
        fruitSubcategories.put("Canned", new String[]{"Canned Peaches", "Canned Pineapple", "Canned Pears", "Fruit Cocktail", "Canned Mandarin", "Applesauce"});
        fruitSubcategories.put("Frozen", new String[]{"Frozen Berries", "Frozen Mango", "Frozen Strawberries", "Frozen Fruit Mix"});
        foodCategories.put("Fruits", fruitSubcategories);

        // Nuts & Seeds category - new
        Map<String, String[]> nutsSubcategories = new HashMap<>();
        nutsSubcategories.put("Nuts", new String[]{"Almonds", "Walnuts", "Cashews", "Peanuts", "Pistachios", "Pecans", "Brazil Nuts", "Hazelnuts", "Macadamia Nuts"});
        nutsSubcategories.put("Seeds", new String[]{"Sunflower Seeds", "Pumpkin Seeds", "Chia Seeds", "Flax Seeds", "Sesame Seeds", "Hemp Seeds"});
        nutsSubcategories.put("Nut Butters", new String[]{"Peanut Butter", "Almond Butter", "Cashew Butter", "Sunflower Seed Butter", "Tahini"});
        foodCategories.put("Nuts & Seeds", nutsSubcategories);

        // Snacks & Sweets category - new
        Map<String, String[]> snacksSubcategories = new HashMap<>();
        snacksSubcategories.put("Sweet", new String[]{"Chocolate", "Candy", "Cookies", "Cake", "Ice Cream", "Donuts", "Pie", "Muffins", "Brownies"});
        snacksSubcategories.put("Savory", new String[]{"Potato Chips", "Tortilla Chips", "Popcorn", "Pretzels", "Crackers", "Trail Mix"});
        foodCategories.put("Snacks & Sweets", snacksSubcategories);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://trackapi.nutritionix.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(FoodApiService.class);
    }

    private void setupSpinners() {
        // Setup category spinner
        List<String> categories = new ArrayList<>(foodCategories.keySet());
        categories.add(0, "Select Category");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Category spinner listener
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedCategory = categories.get(position);
                    updateSubcategorySpinner(selectedCategory);
                } else {
                    clearSubcategorySpinner();
                    clearFoodSpinner();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Subcategory spinner listener
        subcategorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && categorySpinner.getSelectedItemPosition() > 0) {
                    String selectedCategory = categories.get(categorySpinner.getSelectedItemPosition());
                    String selectedSubcategory = (String) subcategorySpinner.getSelectedItem();
                    updateFoodSpinner(selectedCategory, selectedSubcategory);
                } else {
                    clearFoodSpinner();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Food spinner listener
        foodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedFood = (String) foodSpinner.getSelectedItem();
                    if ("Other (Type your own)".equals(selectedFood)) {
                        customFoodLayout.setVisibility(View.VISIBLE);
                    } else {
                        customFoodLayout.setVisibility(View.GONE);
                    }
                } else {
                    customFoodLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void updateSubcategorySpinner(String category) {
        Map<String, String[]> subcategories = foodCategories.get(category);
        if (subcategories != null) {
            List<String> subcategoryNames = new ArrayList<>(subcategories.keySet());
            subcategoryNames.add(0, "Select Subcategory");
            ArrayAdapter<String> subcategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subcategoryNames);
            subcategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subcategorySpinner.setAdapter(subcategoryAdapter);
        }
    }

    private void updateFoodSpinner(String category, String subcategory) {
        Map<String, String[]> subcategories = foodCategories.get(category);
        if (subcategories != null && subcategories.containsKey(subcategory)) {
            String[] foods = subcategories.get(subcategory);
            List<String> foodList = new ArrayList<>();
            foodList.add("Select Food");
            for (String food : foods) {
                foodList.add(food);
            }
            // Add "Other" option at the end
            foodList.add("Other (Type your own)");

            ArrayAdapter<String> foodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foodList);
            foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            foodSpinner.setAdapter(foodAdapter);
        }
    }

    private void clearSubcategorySpinner() {
        ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Subcategory"});
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subcategorySpinner.setAdapter(emptyAdapter);
    }

    private void clearFoodSpinner() {
        ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Food"});
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodSpinner.setAdapter(emptyAdapter);
        customFoodLayout.setVisibility(View.GONE);
    }

    private void setupAddButton() {
        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foodSpinner.getSelectedItemPosition() > 0) {
                    String selectedFood = (String) foodSpinner.getSelectedItem();
                    String gramsStr = gramsEditText.getText().toString().trim();

                    if (!gramsStr.isEmpty()) {
                        try {
                            int grams = Integer.parseInt(gramsStr);
                            if (grams > 0) {
                                String finalFoodName;
                                String queryFood;

                                if ("Other (Type your own)".equals(selectedFood)) {
                                    // Handle custom food input
                                    String customFood = customFoodEditText.getText().toString().trim();
                                    if (customFood.isEmpty()) {
                                        Toast.makeText(FoodTrack.this, "Please enter a custom food name", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    finalFoodName = customFood;
                                    queryFood = customFood;
                                } else {
                                    finalFoodName = selectedFood;
                                    queryFood = selectedFood;
                                }

                                // Create the query with grams
                                String foodQuery = grams + "g " + queryFood;
                                fetchFoodData(foodQuery, finalFoodName, grams);
                            } else {
                                Toast.makeText(FoodTrack.this, "Please enter a valid amount in grams", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(FoodTrack.this, "Please enter a valid number for grams", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FoodTrack.this, "Please enter the amount in grams", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FoodTrack.this, "Please select a food item", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchFoodData(String query, String foodName, int grams) {
        resultTextView.setText("Searching for: " + query);

        FoodQuery foodQuery = new FoodQuery(query);
        Call<NutritionResponse> call = apiService.getFoodData(foodQuery);

        call.enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().foods.isEmpty()) {
                    FoodItem food = response.body().foods.get(0);
                    String result = "Found: " + food.foodName + " (" + food.calories + " kcal)";
                    resultTextView.setText(result);

                    // Create a custom food item with the selected name and grams
                    FoodItem customFood = new FoodItem();
                    customFood.foodName = foodName + " (" + grams + "g)";
                    customFood.calories = food.calories;

                    // Add to food entries and update table
                    addFoodEntry(customFood);

                    // Clear the input fields
                    gramsEditText.setText("");
                    customFoodEditText.setText("");
                    resetSpinners();
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

    private void resetSpinners() {
        categorySpinner.setSelection(0);
        clearSubcategorySpinner();
        clearFoodSpinner();
    }

    private void addFoodEntry(FoodItem food) {
        // Add to our list
        foodItems.add(food);

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
                foodItems.remove(food);
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
        for (FoodItem entry : foodItems) {
            totalCalories += entry.calories;
        }
        totalCaloriesTextView.setText(String.format("Total Calories: %.0f", totalCalories));
    }
}