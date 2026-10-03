package com.company.jifiit.model;

public class PaymentRequestModel {

    private String id;
    private String startDate;
    private String endDate;
    private String requestedEarning;
    private String totalBooking;
    private String deductionForBed;
    private String deductionForLateFine;
    private String deductionForCanceledBooking;
    private String deductionForOthers;
    private String totalPaid;
    private String status;
    private String receipt;

    private String paymentRequestDate,UpdatedDate,PlarfromFee;

    public PaymentRequestModel(String id, String startDate, String endDate, String requestedEarning, String totalBooking, String deductionForBed, String deductionForLateFine, String deductionForCanceledBooking, String totalPaid, String status, String receipt , String deductionForOthers, String paymentRequestDate,
                               String updatedDate, String plarfromFee) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestedEarning = requestedEarning;
        this.totalBooking = totalBooking;
        this.deductionForBed = deductionForBed;
        this.deductionForLateFine = deductionForLateFine;
        this.deductionForCanceledBooking = deductionForCanceledBooking;
        this.totalPaid = totalPaid;
        this.status = status;
        this.receipt = receipt;
        this.deductionForOthers = deductionForOthers;
        this.paymentRequestDate = paymentRequestDate;
        this.UpdatedDate = updatedDate;
        this.PlarfromFee = plarfromFee;
    }


    public String getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getRequestedEarning() {
        return requestedEarning;
    }

    public String getTotalBooking() {
        return totalBooking;
    }

    public String getDeductionForBed() {
        return deductionForBed;
    }

    public String getDeductionForLateFine() {
        return deductionForLateFine;
    }

    public String getDeductionForCanceledBooking() {
        return deductionForCanceledBooking;
    }

    public String getTotalPaid() {
        return totalPaid;
    }

    public String getStatus() {
        return status;
    }

    public String getReceipt() {
        return receipt;
    }

    public String getDeductionForOthers() {
        return deductionForOthers;
    }

    public String getPaymentRequestDate() {
        return paymentRequestDate;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    public String getPlarfromFee() {
        return PlarfromFee;
    }
}
