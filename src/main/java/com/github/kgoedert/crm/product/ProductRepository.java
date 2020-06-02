package com.github.kgoedert.crm.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public Product findByUUID(String uuid) {
        return find("uuid", uuid).firstResult();
    }

    public List<Product> findByFilters(String category, int page, int pageSize, String sort) {
        Sort orderBy = this.parseSorting(sort);
        Category cat = null;
        try {
            cat = category == null ? null : Category.valueOf(category);
            Map<String, Object> params = new HashMap<>();
            params.put("category", cat);
            PanacheQuery<Product> query = find(
                    "select p from Product p where (:category is null or p.category = :category)",
                    orderBy, params);
            if (page == 0) {
                page = 1;
            }

            if (pageSize == 0) {
                pageSize = 10;
            }
            return query.page(page - 1, pageSize).list();
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }

    }

    private Sort parseSorting(String sort) {
        Sort orderBy = null;
        if (sort != null) {
            String[] parts = sort.split(",");
            for (String part : parts) {
                String direction = part.charAt(0) + "";
                String element = part.substring(1);

                if (orderBy == null) {
                    if (direction.equals("+")) {
                        orderBy = Sort.by(element, Direction.Ascending);
                    } else {
                        orderBy = Sort.by(element, Direction.Descending);
                    }
                } else {
                    if (direction.equals("+")) {
                        orderBy.and(element, Direction.Ascending);
                    } else {
                        orderBy.and(element, Direction.Descending);
                    }
                }
            }
        }
        return orderBy;
    }

}