package com.github.kgoedert.crm.product;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public Product findByUUID(String uuid) {
        return find("uuid", uuid).firstResult();
    }

}