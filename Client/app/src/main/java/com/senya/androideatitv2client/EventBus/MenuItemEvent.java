package com.senya.androideatitv2client.EventBus;

import com.senya.androideatitv2client.Model.RestaurantModel;

public class MenuItemEvent {
    private boolean success;
    private RestaurantModel restaurantModel;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public RestaurantModel getRestaurantModel() {
        return restaurantModel;
    }

    public void setRestaurantModel(RestaurantModel restaurantModel) {
        this.restaurantModel = restaurantModel;
    }

    public MenuItemEvent(boolean success, RestaurantModel restaurantModel) {
        this.success = success;
        this.restaurantModel = restaurantModel;


    }
}
