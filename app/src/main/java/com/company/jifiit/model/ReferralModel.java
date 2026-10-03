package com.company.jifiit.model;

public class ReferralModel {

    private String name;
    private String created_at;
    private String months;
    private String referral_payment;


    public ReferralModel(String name, String created_at, String months, String referral_payment) {
        this.name = name;
        this.created_at = created_at;
        this.months = months;
        this.referral_payment = referral_payment;
    }


    public String getName() {
        return name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getMonths() {
        return months;
    }

    public String getReferral_payment() {
        return referral_payment;
    }
}
