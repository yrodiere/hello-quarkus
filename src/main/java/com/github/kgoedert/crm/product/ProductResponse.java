package com.github.kgoedert.crm.product;

import java.time.LocalDateTime;

public class ProductResponse extends ProductRequest {
    public String uuid;
    public LocalDateTime createdAt;

    public ProductResponse(String uuid, String name, double price, Category category, int amount, byte[] image,
            String description, LocalDateTime created) {
        this.uuid = uuid;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockAmount = amount;
        this.image = image;
        this.description = description;
        this.createdAt = created;
    }
}