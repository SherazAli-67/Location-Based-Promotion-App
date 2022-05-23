package com.app.promotionapp.model;

public class User {
    String name;
    String email;
    String password;
    boolean retailer;

    public User() {
    }

    public User(String name, String email, String password, boolean isRetailer) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.retailer = isRetailer;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRetailer() {
        return retailer;
    }
}
