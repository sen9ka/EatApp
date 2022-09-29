package com.senya.androideatitv2client.Callback;

import com.senya.androideatitv2client.Model.BestDealModel;
import com.senya.androideatitv2client.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
