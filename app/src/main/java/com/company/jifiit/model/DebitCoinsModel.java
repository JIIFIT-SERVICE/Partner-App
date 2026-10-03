package com.company.jifiit.model;

public class DebitCoinsModel {
    private String order_id,fareType,fareValue,farebalance;
    private String date;
    private String time;

    public DebitCoinsModel(String order_id, String fareType, String fareValue, String farebalance, String date, String time) {
        this.order_id = order_id;
        this.fareType = fareType;
        this.fareValue = fareValue;
        this.farebalance = farebalance;
        this.date = date;
        this.time = time;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getFareType() {
        return fareType;
    }

    public String getFareValue() {
        return fareValue;
    }

    public String getFarebalance() {
        return farebalance;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
