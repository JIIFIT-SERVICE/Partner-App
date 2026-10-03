package com.company.jifiit.model;

public class BookingHistoryModel {
    private String customer_name;
    private String massage_name;
    private String start_time;
    private String duration;
    private String address;
    private String payment_status;
    private String id;
    private String order_date;
    private String payment_type;
    private String fare,customers_id,ratings,amount,bookingType;

    public BookingHistoryModel(String customer_name, String massage_name, String start_time, String duration, String address, String payment_status, String id, String order_date, String payment_type, String fare,String customers_id,String ratings,String amount,String bookingType) {
        this.customer_name = customer_name;
        this.massage_name = massage_name;
        this.start_time = start_time;
        this.duration = duration;
        this.address = address;
        this.payment_status = payment_status;
        this.id = id;
        this.order_date = order_date;
        this.payment_type = payment_type;
        this.fare = fare;
        this.customers_id = customers_id;
        this.ratings = ratings;
        this.amount = amount;
        this.bookingType = bookingType;
    }

    // Getters
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
        return address;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public String getId() {
        return id;
    }

    public String getOrder_date() {
        return order_date;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public String getFare() {
        return fare;
    }

    public String getCustomers_id() {
        return customers_id;
    }

    public String getRatings() {
        return ratings;
    }

    public String getAmount() {
        return amount;
    }

    public String getBookingType() {
        return bookingType;
    }
}
