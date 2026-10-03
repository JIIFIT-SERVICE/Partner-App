package com.company.jifiit.model;

public class AddToCartModel {
    String productName,Duration,quantity,productId,type,orderId,discountPrice,originalPrice;

    public AddToCartModel(String productName, String duration, String quantity, String productId, String type, String orderId, String discountPrice,String originalPrice) {
        this.productName = productName;
        Duration = duration;
        this.quantity = quantity;
        this.productId = productId;
        this.type = type;
        this.orderId = orderId;
        this.discountPrice = discountPrice;
        this.originalPrice = originalPrice;
    }

    public String getProductName() {
        return productName;
    }

    public String getDuration() {
        return Duration;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getType() {
        return type;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }
    public String getOriginalPrice() {
        return originalPrice;
    }
}
