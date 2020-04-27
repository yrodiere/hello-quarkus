package com.github.kgoedert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GreetingServiceTest {
    @Test
    public void testGreetingService() {
        GreetingService service = new GreetingService();
        assertEquals("hello Quarkus", service.greeting("Quarkus"));
    }
}