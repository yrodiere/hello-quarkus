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
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kgoedert.crm.StringEnumeration;
import com.github.kgoedert.crm.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "products")
@Access(AccessType.FIELD)
@Schema(name = "Product", description = "Represents a product in the inventory")
public class Product extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "productSeq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "productSeq", sequenceName = "product_id_seq", allocationSize = 1)
    @JsonIgnore
    private Long id;

    @Column(name = "name")
    @NotBlank(message = "Name may not be blank")
    @Schema(required = true, example = "Rice")
    private String name;

    @Column(name = "price")
    @DecimalMin(value = "0.1", message = "The price has to be at least 0.1")
    @Schema(required = true, example = "10.50")
    private double price;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    @NotNull
    @StringEnumeration(enumClass = Category.class)
    @Schema(required = true, example = "GRAIN")
    private Category category;

    @Column(name = "description")
    @Schema(required = false, example = "Imported rice from China")
    private String description;

    @Column(name = "amount")
    @Min(value = 1, message = "The product needs to have at least 1 item in stock")
    @Schema(required = true, example = "100")
    private int stockAmount;

    @Column(name = "date_created")
    @Transient
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(required = false, readOnly = true, example = "Date and time product was registered in the server")
    private LocalDateTime createdAt;

    @Lob
    @Schema(required = false, example = "Base64 of an image")
    private String image;

    @UUID
    @Schema(required = true, readOnly = true, example = "123e4567-e89b-12d3-a456-4266141740000")
    private String uuid;

    public Product() {
        // for jpa
    }

    public Product(String uuid, String name, double price, String category, String description, int stockAmount,
            String image) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = Category.valueOf(category);
        this.description = description;
        this.stockAmount = stockAmount;
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