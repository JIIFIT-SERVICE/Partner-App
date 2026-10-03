package com.company.jifiit.model;

public class CommissionModel {

    private String bookingId;
    private String paymentStatus;
    private String bookingType;
    private String bookingDate;
    private String bookingTime;
    private String customerName;
    private String massageName;
    private final String bookingAmounts;
    private String totalAmount;
    private String commission;
    private String gst;
    private String netAmount;
    private String customerPaidStr;
    private String product_costStr;

    private Integer th_payment_status;

    public CommissionModel(
            String bookingId,
            String bookingType,
            String bookingDate,
            String bookingTime,
            String customerName,
            String massageName,
            String bookingAmounts,
            String commission,
            String gst,
            String netAmount,
            String customerPaidStr,
            String product_costStr,
            Integer th_payment_status
    ) {
        this.bookingId = bookingId;
        this.bookingType = bookingType;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.customerName = customerName;
        this.massageName = massageName;
        this.bookingAmounts = bookingAmounts;
        this.totalAmount = totalAmount;
        this.commission = commission;
        this.gst = gst;
        this.netAmount = netAmount;
        this.customerPaidStr = customerPaidStr;
        this.product_costStr = product_costStr;
        this.th_payment_status = th_payment_status;
    }

    public String getBookingId() {
        return bookingId;
    }


    public String getBookingType() {
        return bookingType;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMassageName() {
        return massageName;
    }

    public String bookingAmounts() {
        return bookingAmounts;
    }

    public String getCommission() {
        return commission;
    }

    public String getGst() {
        return gst;
    }

    public String getNetAmount() {
        return netAmount;
    }

    public String getcustomerPaidStr(){
        return customerPaidStr;
    }

    public String getproduct_costStr(){
        return product_costStr;
    }

    public Integer getTh_payment_status(){
        return th_payment_status;
    }
}

