package com.senya.eatappserver.callback;

import com.senya.eatappserver.model.BestDealsModel;
import com.senya.eatappserver.model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallbackListener {
    void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
