package com.company.jifiit.model;

public class SelectMassageModel {
    String ProductId,originalPrice,DiscountPrice,discounts,duration,type;

    public SelectMassageModel(String ProductId, String originalPrice, String discountPrice, String discounts, String duration, String type) {
        this.ProductId = ProductId;
        this.originalPrice = originalPrice;
        DiscountPrice = discountPrice;
        this.discounts = discounts;
        this.duration = duration;
        this.type = type;
    }

    public String getProductId() {
        return ProductId;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public String getDiscountPrice() {
        return DiscountPrice;
    }

    public String getDiscounts() {
        return discounts;
    }

    public String getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }
}
