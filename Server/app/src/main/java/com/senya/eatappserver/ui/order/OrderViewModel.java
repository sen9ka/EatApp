package com.senya.eatappserver.ui.order;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.senya.eatappserver.callback.IOrderCallbackListener;
import com.senya.eatappserver.common.Common;
import com.senya.eatappserver.model.OrderModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderViewModel extends ViewModel implements IOrderCallbackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallbackListener listener;

    public OrderViewModel() {
        orderModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderByStatus(0);
        return orderModelMutableLiveData;
    }

    public void loadOrderByStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapShot:snapshot.getChildren())
                {
                    OrderModel orderModel = itemSnapShot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapShot.getKey());
                    orderModel.setOrderNumber(itemSnapShot.getKey());
                    tempList.add(orderModel);
                }
                listener.onOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onOrderLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {
        if(orderModelList.size() > 0)
        {
            Collections.sort(orderModelList,(orderModel,t1)->{
                if(orderModel.getCreateDate() < t1.getCreateDate())
                    return -1;
                return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }
        orderModelMutableLiveData.setValue(orderModelList);
    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}
