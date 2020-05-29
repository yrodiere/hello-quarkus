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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.kgoedert.crm.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.Type;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "products")
@Access(AccessType.FIELD)
@Schema(name = "Product", description = "Represents a product in the inventory")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
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
    @NotNull(message = "Category is required")
    @Schema(required = true, example = "GRAIN")
    private Category category;

    @Column(name = "description")
    @Schema(required = false, example = "Rice imported from Japan")
    private String description;

    @Column(name = "amount")
    @Min(value = 1, message = "The product needs to have at least 1 item in stock")
    @Schema(required = true, example = "100")
    private int stockAmount;

    @Column(name = "date_created")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape = Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    @Schema(required = false, readOnly = true, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    @Schema(required = false, description = "A byte[] representing the image")
    private byte[] image;

    @UUID
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(required = false, readOnly = true)
    private String uuid;

    public Product() {
        // for jpa
    }

    public void addUUID() {
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public void addCreationDate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getStockAmount() {
        return stockAmount;
    }

    public byte[] getImage() {
        return image;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }
}