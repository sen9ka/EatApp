package com.senya.eatappserver.callback;

import com.senya.eatappserver.model.CategoryModel;
import com.senya.eatappserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
