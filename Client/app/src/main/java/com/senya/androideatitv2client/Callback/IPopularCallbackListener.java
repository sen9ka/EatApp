package com.senya.androideatitv2client.Callback;

import com.senya.androideatitv2client.Model.PopularCategoryModel;

import java.util.List;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategoryModel>popularCategoryModels);
    void onPopularLoadFailed(String message);
}
