package com.company.jifiit.model;

import java.math.BigInteger;

public class BookingModel {
    private String customer_name;
    private String massage_name;
    private String start_time;
    private String duration;
    private String Address;
    private String payment_status;
    private String id;

    public BookingModel(String customer_name, String massage_name, String start_time, String duration, String address, String payment_status,String id) {
        this.customer_name = customer_name;
        this.massage_name = massage_name;
        this.start_time = start_time;
        this.duration = duration;
        this.Address = address;
        this.payment_status = payment_status;
        this.id = id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getMassage_name() {
        return massage_name;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getDuration() {
        return duration;
    }

    public String getAddress() {
        return Address;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public String getId() {
        return id;
    }
}
