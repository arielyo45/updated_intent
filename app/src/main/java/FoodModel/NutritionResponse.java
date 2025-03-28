package FoodModel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NutritionResponse {
    @SerializedName("foods") public List<FoodItem> foods;
}

