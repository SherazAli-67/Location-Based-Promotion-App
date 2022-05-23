package com.app.promotionapp.model;

public class SubscribedStore {
   String store_id;
   String store_title;
   String retailer_id;
   String locationCoordinates;
   String subscribedUserID;
   String userCoordinates;

    public SubscribedStore() {
    }

    public SubscribedStore(String store_id, String store_title, String retailer_id, String locationCoordinates, String subscribedUserID, String userCoordinates) {
        this.store_id = store_id;
        this.store_title = store_title;
        this.retailer_id = retailer_id;
        this.locationCoordinates = locationCoordinates;
        this.subscribedUserID = subscribedUserID;
        this.userCoordinates = userCoordinates;
    }

    public String getStore_id() {
        return store_id;
    }

    public String getStore_title() {
        return store_title;
    }

    public String getRetailer_id() {
        return retailer_id;
    }

    public String getLocationCoordinates() {
        return locationCoordinates;
    }

    public String getSubscribedUserID() {
        return subscribedUserID;
    }

    public String getUserCoordinates() {
        return userCoordinates;
    }
}
