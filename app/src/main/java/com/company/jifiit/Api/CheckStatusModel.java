package com.company.jifiit.Api;

public class CheckStatusModel {

    private String status;
    private String transactionId;
    private String message;
    private boolean success;

    // Empty constructor (Retrofit/Gson এর জন্য দরকার)


    // Parameterized constructor
    public CheckStatusModel(String status, String transactionId, String message, boolean success) {
        this.status = status;
        this.transactionId = transactionId;
        this.message = message;
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;   // ✅ return করতে হবে
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
