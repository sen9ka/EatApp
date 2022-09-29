package com.senya.androideatitv2client.Callback;

import com.senya.androideatitv2client.Database.CartItem;
import com.senya.androideatitv2client.Model.CategoryModel;
import com.senya.androideatitv2client.Model.FoodModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
