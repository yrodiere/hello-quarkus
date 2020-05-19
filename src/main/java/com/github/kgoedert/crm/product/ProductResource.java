package com.github.kgoedert.crm.product;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    @Inject
    ProductRepository productRepository;
    @Inject
    ProductService productService;

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "POST a product", description = "Add a new product to the inventory")
    @APIResponse(responseCode = "200", description = "Product registration successful")
    @APIResponse(responseCode = "400", description = "Invalid Product")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    @APIResponse(description = "Product", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NewProduct.class)))
    public Response add(NewProduct newProduct) {
        try {
            Product product = new Product(newProduct);
            productService.validateProduct(product);
            productRepository.persist(product);
            return Response.status(Status.CREATED).entity(product).build();
        } catch (ConstraintViolationException e) {
            String message = e.getConstraintViolations().stream()
            .map(cv -> cv.getMessage())
            .collect(Collectors.joining(", "));

            return Response.status(Status.BAD_REQUEST).entity(message).build();
        }
    }
}