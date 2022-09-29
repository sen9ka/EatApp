package com.senya.eatappserver.callback;

import com.senya.eatappserver.model.ShippingOrderModel;

public interface ISingleShippingOrderCallbackListener {
    void onSingleShippingOrderLoadSuccess(ShippingOrderModel shippingOrderModel);
}
