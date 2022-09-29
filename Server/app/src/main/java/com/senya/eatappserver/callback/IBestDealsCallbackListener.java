package com.senya.eatappserver.callback;

import com.senya.eatappserver.model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallbackListener {
    void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
