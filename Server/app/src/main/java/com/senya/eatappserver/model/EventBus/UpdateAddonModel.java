package com.senya.eatappserver.model.EventBus;

import com.senya.eatappserver.model.AddonModel;

import java.util.List;

public class UpdateAddonModel {
    private List<AddonModel> addonModels;

    public UpdateAddonModel() {

    }

    public List<AddonModel> getAddonModels() {
        return addonModels;
    }

    public void setAddonModels(List<AddonModel> addonModels) {
        this.addonModels = addonModels;
    }
}
