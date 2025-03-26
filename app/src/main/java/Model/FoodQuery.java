package Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FoodQuery {
    @SerializedName("query")  private String query;

    public FoodQuery(String query) {
        this.query = query;
    }
}