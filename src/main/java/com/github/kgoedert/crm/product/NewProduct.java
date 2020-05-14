package com.github.kgoedert.crm.product;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "NewProduct", description = "Represents a product in the inventory")
public class NewProduct {
    @Schema(required = true, example = "Rice")
    public String name;

    @Schema(required = true, example = "10.50")
    public double price;

    @Schema(required = true, example = "GRAIN")
    public Category category;

    @Schema(required = true, example = "100")
    public int stockAmount;

    @Schema(required = false)
    public byte[] image;

    @Schema(required = false, example = "Rice imported from Japan")
    public String description;

}