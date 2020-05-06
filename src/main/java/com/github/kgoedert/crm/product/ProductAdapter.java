package com.github.kgoedert.crm.product;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

public class ProductAdapter implements JsonbAdapter<Product, JsonObject> {

    @Override
    public JsonObject adaptToJson(Product obj) throws Exception {
        return Json.createObjectBuilder()
                .add("uuid", obj.getUuid())
                .add("name", obj.getName())
                .add("price", obj.getPrice())
                .add("category", obj.getCategory().name())
                .add("stockAmount", obj.getStockAmount())
                .add("description", obj.getDescription())
                .build();
    }

    @Override
    public Product adaptFromJson(JsonObject obj) throws Exception {
        Product prod = new Product(
                obj.getString("uuid"),
                obj.getString("name"),
                obj.getJsonNumber("price").doubleValue(),
                obj.getString("category"),
                obj.getString("description"),
                obj.getInt("stockAmount"),
                obj.getString("image"));
        return prod;
    }

}