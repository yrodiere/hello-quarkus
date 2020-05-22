package com.github.kgoedert.crm.product;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
public class ProductResourceITest {
      @Inject
      DataSource dataSource;

      @AfterAll
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
                        .post("/api/product")
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
                        .post("/api/product")
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

            given().port(8081).contentType("application/json").body(jsonObj.toString()).when().post("/api/product")
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

            List<Product> products = given().port(8081).contentType(ContentType.JSON).when().get("/api/product").then()
                        .assertThat().statusCode(200).extract().as(List.class);

            assertEquals(2, products.size());
      }

      @Test
      @Disabled
      public void productUuidCannotBeSubmittedByUser() {
            JsonObject carrot = this.createJsonProduct(Category.VEGETABLES.name(), "Sweet Carrots", "Carrots", "1.50",
                        "400", null);
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
                        .post("/api/product")
                        .then()
                        .assertThat()
                        .statusCode(201);
      }

}