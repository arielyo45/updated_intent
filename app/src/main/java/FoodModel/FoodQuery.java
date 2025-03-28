package FoodModel;

import com.google.gson.annotations.SerializedName;

public class FoodQuery {
    @SerializedName("query")  private String query;

    public FoodQuery(String query) {
        this.query = query;
    }
}