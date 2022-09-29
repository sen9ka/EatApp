package com.senya.androideatitv2client.Callback;

import com.senya.androideatitv2client.Model.BestDealModel;
import com.senya.androideatitv2client.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
