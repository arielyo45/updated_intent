package Model;

import com.google.gson.annotations.SerializedName;

public class FoodItem {
    @SerializedName("food_name") public String foodName;

    @SerializedName("nf_calories") public float calories;

    @SerializedName("nf_total_fat")  public float totalFat;

    @SerializedName("nf_total_carbohydrate") public float carbs;

    @SerializedName("nf_protein") public float protein;
}
