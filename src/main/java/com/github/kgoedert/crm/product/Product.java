package com.github.kgoedert.crm.product;

import java.time.LocalDateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.github.kgoedert.crm.UUID;

import org.hibernate.annotations.Type;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "products")
@Access(AccessType.FIELD)
public class Product extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "productSeq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "productSeq", sequenceName = "product_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name")
    @NotBlank(message = "Name may not be blank")
    private String name;

    @Column(name = "price")
    @DecimalMin(value = "0.1", message = "The price has to be at least 0.1")
    private double price;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    private Category category;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    @Min(value = 1, message = "The product needs to have at least 1 item in stock")
    private int stockAmount;

    @Column(name = "date_created")
    private LocalDateTime createdAt;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] image;

    @UUID
    private String uuid;

    public Product() {
        // for jpa
    }

    public Product(NewProduct product) {
        this.addUUID();
        this.createdAt = LocalDateTime.now();
        this.name = product.name;
        this.description = product.description;
        this.price = product.price;
        this.category = product.category;
        this.stockAmount = product.stockAmount;
    }

    public void addUUID() {
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public int getStockAmount() {
        return stockAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDescription() {
        return description;
    }
}