package com.github.kgoedert.crm.product;

import static io.restassured.RestAssured.given;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
public class ProductResourceITest {
   @Inject
   DataSource dataSource;

   @Test
   public void new_product_added_successfully() {
      JsonObject jsonObj = new JsonObject()
            .put("category", "GRAIN")
            .put("description", "Corn from Mexico")
            .put("name", "Corn")
            .put("price", "2.50")
            .put("stockAmount", "200");

      given()
            .port(8081)
            .contentType("application/json")
            .body(jsonObj.toString())
            .when()
            .post("/product")
            .then()
            .assertThat()
            .statusCode(201);
   }

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

}