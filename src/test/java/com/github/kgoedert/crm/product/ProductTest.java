package com.github.kgoedert.crm.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class ProductTest {
    @Test
    public void creationDateIsImmutable() {
        Product prod = new Product();
        assertNull(prod.getCreatedAt());

        prod.addCreationDate();

        LocalDateTime creationDate = prod.getCreatedAt();
        prod.addCreationDate();

        assertEquals(creationDate, prod.getCreatedAt());
    }
}