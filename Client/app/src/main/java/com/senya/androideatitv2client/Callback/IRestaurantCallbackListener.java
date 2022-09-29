package com.senya.androideatitv2client.Callback;

import com.senya.androideatitv2client.Model.CategoryModel;
import com.senya.androideatitv2client.Model.RestaurantModel;

import java.util.List;

public interface IRestaurantCallbackListener {
    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);
    void onRestaurantLoadFailed(String message);
}
