package com.company.jifiit.model;

public class CartModel {
    String discountPrice;
    String originalPrice;
    String product_name;
    String save;
    String duration;
    String nameCount;
    String discounts;
    String images;


    String table_name;
    String id;

    public CartModel(String discountPrice, String originalPrice, String product_name, String save, String duration, String nameCount, String discounts, String images, String table_name, String id) {
        this.discountPrice = discountPrice;
        this.originalPrice = originalPrice;
        this.product_name = product_name;
        this.save = save;
        this.duration = duration;
        this.nameCount = nameCount;
        this.discounts = discounts;
        this.images = images;
        this.table_name = table_name;
        this.id = id;
    }


    public String getDiscountPrice() {
        return discountPrice;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getSave() {
        return save;
    }

    public String getDuration() {
        return duration;
    }

    public String getNameCount() {
        return nameCount;
    }

    public String getDiscounts() {
        return discounts;
    }

    public String getImages() {
        return images;
    }

    public String getTable_name() {
        return table_name;
    }

    public String getId() {
        return id;
    }
}
