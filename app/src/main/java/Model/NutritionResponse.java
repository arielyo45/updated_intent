package Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import Model.FoodItem;

public class NutritionResponse {
    @SerializedName("foods") public List<FoodItem> foods;
}

