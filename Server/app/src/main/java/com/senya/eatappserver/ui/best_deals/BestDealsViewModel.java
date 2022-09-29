package com.senya.eatappserver.ui.best_deals;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senya.eatappserver.callback.IBestDealsCallbackListener;
import com.senya.eatappserver.common.Common;
import com.senya.eatappserver.model.BestDealsModel;

import java.util.ArrayList;
import java.util.List;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<BestDealsModel>> bestDealsListMutable;
    private IBestDealsCallbackListener bestDealsCallbackListener;

    public BestDealsViewModel(){
        bestDealsCallbackListener = this;
    }

    public MutableLiveData<List<BestDealsModel>> getBestDealsListMutable() {
        if(bestDealsListMutable == null)
            bestDealsListMutable = new MutableLiveData<>();
        loadBestDeals();
        return bestDealsListMutable;
    }

    public void loadBestDeals() {
        List<BestDealsModel> temp = new ArrayList<>();
        DatabaseReference bestDealsRef = FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.BEST_DEALS);
        bestDealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot bestDealsSnapShot:snapshot.getChildren())
                {
                    BestDealsModel bestDealsModel = bestDealsSnapShot.getValue(BestDealsModel.class);
                    bestDealsModel.setKey(bestDealsSnapShot.getKey());
                    temp.add(bestDealsModel);
                }
                bestDealsCallbackListener.onListBestDealsLoadSuccess(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bestDealsCallbackListener.onListBestDealsLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels) {
        bestDealsListMutable.setValue(bestDealsModels);
    }

    @Override
    public void onListBestDealsLoadFailed(String message) {
        messageError.setValue(message);
    }
}