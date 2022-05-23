package com.app.promotionapp.model;

public class Store {
   String store_id;
   String store_title;
   String location_title;
   String location_subtitle;
   String locationCoordiantes;
   String user_id;

    public Store() {
    }

    public Store(String store_id, String store_title, String location_title, String location_subtitle, String locationCoordiantes, String user_id) {
        this.store_id = store_id;
        this.store_title = store_title;
        this.location_title = location_title;
        this.location_subtitle = location_subtitle;
        this.locationCoordiantes = locationCoordiantes;
        this.user_id = user_id;
    }

    public String getStore_id() {
        return store_id;
    }

    public String getStore_title() {
        return store_title;
    }

    public String getLocation_title() {
        return location_title;
    }

    public String getLocation_subtitle() {
        return location_subtitle;
    }

    public String getLocationCoordiantes() {
        return locationCoordiantes;
    }

    public String getUser_id() {
        return user_id;
    }
}
