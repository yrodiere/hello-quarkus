package com.github.kgoedert.crm.product;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
public class ProductResourceITest {
    private static final String API_PRODUCTS = "/api/products";
    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    DataSource dataSource;

    @BeforeEach
    public void cleanUp() {
        try (Connection conn = this.dataSource.getConnection(); Statement st = conn.createStatement()) {
            List<String> lines = List.of(
                    "delete from orders_products",
                    "delete from orders",
                    "delete from products",
                    "delete from customers");

            for (String line : lines) {
                st.executeUpdate(line);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void newPrductAddedSuccessfully() {
        JsonObject corn = createJsonProduct(Category.GRAIN.name(), "Corn From Mexico", "Corn", "2.50", "200",
                null);
        assertProductCreatedStatus201(corn);
    }

    @Test
    public void newProductSuccessfullyWithImage() {
        byte[] image = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("carrots.jpeg").getFile());
            image = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            fail("Could not load the image for testing");
        }
        JsonObject carrot = this.createJsonProduct(Category.VEGETABLES.name(), "Sweet Carrots", "Carrots", "1.50",
                "400", image);

        assertProductCreatedStatus201(carrot);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void productNameCantBeEmpty() {
        JsonObject jsonObj = this.createJsonProduct(Category.GRAIN.name(), "Corn from Mexico", "", "2.50", "200",
                null);

        List<String> result = given()
                .port(8081)
                .contentType("application/json")
                .body(jsonObj.toString())
                .when()
                .post(API_PRODUCTS)
                .then()
                .assertThat()
                .statusCode(400)
                .extract()
                .as(List.class);
        assertEquals(1, result.size());
        assertThat(result, hasItem("Name may not be blank"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void productNamePriceCategoryAndStockAreRequired() {
        JsonObject prod = new JsonObject();
        List<String> result = given()
                .port(8081)
                .contentType(ContentType.JSON)
                .body(prod.toString())
                .when()
                .post(API_PRODUCTS)
                .then()
                .assertThat()
                .statusCode(400)
                .extract()
                .as(List.class);
        assertEquals(4, result.size());
        assertThat(result, hasItem("Name may not be blank"));
        assertThat(result, hasItem("The product needs to have at least 1 item in stock"));
        assertThat(result, hasItem("The price has to be at least 0.1"));
        assertThat(result, hasItem("Category is required"));
    }

    @Test
    public void productCategoryNotAValidValue() {
        JsonObject jsonObj = this.createJsonProduct("SALAD", "lettuce", "lettuce", "2.50", "200", null);

        given().port(8081).contentType("application/json").body(jsonObj.toString()).when().post(API_PRODUCTS)
                .then()
                .assertThat().statusCode(400).body(equalTo(
                        "The value 'SALAD' is not allowed for the type Category. The allowed values are: [VEGETABLES, MEAT, GRAIN, DAIRY]."));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllProductsNoFilterNoPagination() {
        JsonObject carrot = this.createJsonProduct(Category.VEGETABLES.name(), "Sweet Carrots", "Carrots", "1.50",
                "400", null);
        JsonObject corn = createJsonProduct(Category.GRAIN.name(), "Corn From Mexico", "Corn", "2.50", "200",
                null);

        assertProductCreatedStatus201(carrot);
        assertProductCreatedStatus201(corn);

        List<Product> products = given().port(8081).contentType(ContentType.JSON).when().get(API_PRODUCTS).then()
                .assertThat().statusCode(200).extract().as(ArrayList.class);

        assertEquals(2, products.size());
    }

    @Test
    public void getAllProductsNoneFound() {
        Response resp = given().port(8081).contentType(ContentType.JSON).when().get(API_PRODUCTS).then()
                .assertThat().statusCode(200).extract().response();

        List result = resp.body().as(List.class);
        assertEquals(0, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void productUuidCannotBeSubmittedByUser() {
        JsonObject carrot = this.createJsonProduct(Category.VEGETABLES.name(), "Sweet Carrots", "Carrots", "1.50",
                "400", null);
        String uuid = "32999e26-049d-4db8-845c-83765c4da0a2";
        carrot.put("uuid", uuid);
        assertProductCreatedStatus201(carrot);

        List<Product> all = given().port(8081).contentType(ContentType.JSON).when().get(API_PRODUCTS).then()
                .assertThat().statusCode(200).extract().as(ArrayList.class);
        List<Product> products = this.mapper.convertValue(all, new TypeReference<List<Product>>() {
        });
        assertEquals(1, products.size());
        assertNotEquals(uuid, products.get(0).getUuid());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getProductByExistingUUIDReturnsProduct() {
        JsonObject brocoli = this.createJsonProduct(Category.VEGETABLES.name(), "Brocoli", "Brocoli", "1.50",
                "50", null);

        LinkedHashMap<String, String> prodCreated = given().port(8081).contentType(ContentType.JSON)
                .body(brocoli.toString()).when()
                .post(API_PRODUCTS).then()
                .assertThat().statusCode(201).extract().as(LinkedHashMap.class);
        String uuid = prodCreated.get("uuid");

        LinkedHashMap<String, String> prodFetched = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "/" + uuid).then().extract().as(LinkedHashMap.class);

        assertEquals(prodCreated.get("uuid"), prodFetched.get("uuid"));
        assertEquals(prodCreated.get("createdAt"), prodFetched.get("createdAt"));
    }

    @Test
    public void getProductByInexistingUUIDReturns404() {
        String uuid = "32999e26-049d-4db8-845c-83765c4da0a2";
        given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "/" + uuid).then().assertThat().statusCode(404);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getFirstPageOfProductsOrderedByNameAsc() {
        this.insertMockProducts();

        List<Product> all = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "?sort=+name&page=1&pageSize=10").then().assertThat().statusCode(200)
                .extract().as(ArrayList.class);

        assertEquals(10, all.size());

        List<Product> products = mapper.convertValue(all, new TypeReference<List<Product>>() {
        });
        assertEquals("Beef - Ground Medium", products.get(0).getName());
        assertEquals("Bread Crumbs - Japanese Style", products.get(1).getName());
        assertEquals("Cheese - Comtomme", products.get(2).getName());
        assertEquals("Chilli Paste - Sambal Oelek", products.get(3).getName());
        assertEquals("Chinese Foods - Chicken", products.get(4).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getSecondPageOfProductsDefaultSorting() {
        this.insertMockProducts();
        List<Product> all = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "?page=2&pageSize=10").then().assertThat().statusCode(200).extract()
                .as(ArrayList.class);

        assertEquals(10, all.size());
        List<Product> products = mapper.convertValue(all, new TypeReference<List<Product>>() {
        });
        assertEquals("Sauce - Marinara", products.get(0).getName());
        assertEquals("Chilli Paste - Sambal Oelek", products.get(1).getName());
        assertEquals("Stainless Steel Cleaner Vision", products.get(2).getName());
        assertEquals("Veal - Chops -  Split/ frenched", products.get(3).getName());
        assertEquals("Wine - Chateau Bonnet", products.get(4).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllProductsByCategory() {
        this.insertMockProducts();
        List<Product> all = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "?category=VEGETABLES").then().assertThat().statusCode(200).extract()
                .as(ArrayList.class);

        assertEquals(4, all.size());
        List<Product> products = mapper.convertValue(all, new TypeReference<List<Product>>() {
        });
        assertEquals("Wine - Fino Tio Pepe Gonzalez", products.get(0).getName());
        assertEquals("Chilli Paste - Sambal Oelek", products.get(1).getName());
        assertEquals("Stainless Steel Cleaner Vision", products.get(2).getName());
        assertEquals("Veal - Chops -  Split/ frenched", products.get(3).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllProductsOrderByNameAscPriceDesc() {
        this.insertMockProducts();
        List<Product> all = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "?sort=+name,-price").then().assertThat().statusCode(200).extract()
                .as(ArrayList.class);

        assertEquals(10, all.size());

        List<Product> products = mapper.convertValue(all, new TypeReference<List<Product>>() {
        });

        assertEquals("Beef - Ground Medium", products.get(0).getName());
        assertEquals(45.06, products.get(0).getPrice());

        assertEquals("Bread Crumbs - Japanese Style", products.get(1).getName());
        assertEquals(81.20, products.get(1).getPrice());

        assertEquals("Cheese - Comtomme", products.get(2).getName());
        assertEquals(22.75, products.get(2).getPrice());

        assertEquals("Chilli Paste - Sambal Oelek", products.get(3).getName());
        assertEquals(6.1, products.get(3).getPrice());

        assertEquals("Chinese Foods - Chicken", products.get(4).getName());
        assertEquals(27.05, products.get(4).getPrice());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllProductsByInexistentCategoryShouldReturnEmptyList() {
        this.insertMockProducts();
        List<Product> all = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "?category=CLEANING").then().assertThat().statusCode(200).extract()
                .as(ArrayList.class);

        assertEquals(0, all.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllProductsOrderByPriceDescAmountAsc() {
        this.insertMockProducts();
        List<Product> all = given().port(8081).contentType(ContentType.JSON).when()
                .get(API_PRODUCTS + "?sort=-price,+amount").then().assertThat().statusCode(200).extract()
                .as(ArrayList.class);

        assertEquals(10, all.size());

        List<Product> products = mapper.convertValue(all, new TypeReference<List<Product>>() {
        });

        assertEquals("Stainless Steel Cleaner Vision", products.get(0).getName());
        assertEquals(90.24, products.get(0).getPrice());
        assertEquals(2, products.get(0).getStockAmount());

        assertEquals("Wine - Fino Tio Pepe Gonzalez", products.get(1).getName());
        assertEquals(84.29, products.get(1).getPrice());
        assertEquals(50, products.get(1).getStockAmount());

        assertEquals("Bread Crumbs - Japanese Style", products.get(2).getName());
        assertEquals(81.20, products.get(2).getPrice());
        assertEquals(59, products.get(2).getStockAmount());

        assertEquals("Wine - Gewurztraminer Pierre", products.get(3).getName());
        assertEquals(78.54, products.get(3).getPrice());
        assertEquals(38, products.get(3).getStockAmount());

        assertEquals("Oil - Avocado", products.get(4).getName());
        assertEquals(75.94, products.get(4).getPrice());
        assertEquals(95, products.get(4).getStockAmount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updateProductSuccessful() {
        JsonObject chicken = this.createJsonProduct(Category.MEAT.name(), "Organic chicken", "Chicken", "10.0", "30",
                null);

        LinkedHashMap<String, String> product = given()
                .port(8081)
                .contentType(ContentType.JSON)
                .body(chicken.toString())
                .when()
                .post(API_PRODUCTS)
                .then()
                .assertThat()
                .statusCode(201).extract().as(LinkedHashMap.class);

        String uuid = product.get("uuid");
        assertEquals(30, product.get("stockAmount"));

        chicken.put("stockAmount", 10);

        LinkedHashMap<String, String> updatedProduct = given().port(8081).contentType(ContentType.JSON)
                .body(chicken.toString())
                .when().put(API_PRODUCTS + "/" + uuid).then().assertThat().statusCode(200).extract()
                .as(LinkedHashMap.class);

        assertEquals(uuid, updatedProduct.get("uuid"));
        assertEquals(10, updatedProduct.get("stockAmount"));
    }

    @Test
    public void updateProductInexistentUUIDShouldReturn404() {
        JsonObject chicken = this.createJsonProduct(Category.MEAT.name(), "Organic chicken", "Chicken", "10.0", "30",
                null);

        given().port(8081).contentType(ContentType.JSON)
                .body(chicken.toString())
                .when().put(API_PRODUCTS + "/123").then().assertThat().statusCode(404);
    }

    @Test
    @Disabled
    public void updateProductInexistentCategory() {

    }

    @Test
    @Disabled
    public void updateProductWithoutName() {

    }

    private JsonObject createJsonProduct(String category, String description, String name, String price,
            String stockAmount, byte[] image) {
        JsonObject jsonObj = new JsonObject()
                .put("category", category)
                .put("description", description)
                .put("name", name)
                .put("price", price)
                .put("stockAmount", stockAmount)
                .put("image", image);
        return jsonObj;
    }

    private void assertProductCreatedStatus201(JsonObject jsonObj) {
        given()
                .port(8081)
                .contentType(ContentType.JSON)
                .body(jsonObj.toString())
                .when()
                .post(API_PRODUCTS)
                .then()
                .assertThat()
                .statusCode(201);
    }

    private void insertMockProducts() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("mock_products.csv").getFile());
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

            lines.forEach(line -> {
                String[] product = line.split(",");
                JsonObject prod = this.createJsonProduct(product[0], "", product[1], product[2], product[3],
                        null);
                this.assertProductCreatedStatus201(prod);
            });
        } catch (IOException e) {
            fail("Could not load the image for testing");
        }
    }

}