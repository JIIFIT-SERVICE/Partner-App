package com.company.jifiit.model;

public class CreditCoinsModel {

    private String date;
    private String time;
    private String wallet;
    private String balance;

    public CreditCoinsModel(String date, String time, String wallet, String balance) {
        this.date = date;
        this.time = time;
        this.wallet = wallet;
        this.balance = balance;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getWallet() {
        return wallet;
    }

    public String getBalance() {
        return balance;
    }
}
