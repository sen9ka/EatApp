package com.senya.eatappserver.callback;

import com.senya.eatappserver.model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadOnlyTimeSuccess(long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
