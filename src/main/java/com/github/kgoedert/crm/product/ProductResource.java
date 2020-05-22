package com.github.kgoedert.crm.product;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.vertx.core.json.JsonObject;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    @Inject
    ProductRepository productRepository;
    @Inject
    ProductService productService;

    @POST
    @Transactional
    @Operation(summary = "POST a product", description = "Add a new product to the inventory")
    @APIResponse(responseCode = "200", description = "Product registration successful")
    @APIResponse(responseCode = "400", description = "Invalid Product")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    public Response add(Product product) {
        try {
            productService.validateProduct(product);
            productRepository.persist(product);
            return Response.status(Status.CREATED).entity(product).build();
        } catch (ConstraintViolationException e) {
            List<String> messages = e.getConstraintViolations().stream()
                    .map(cv -> cv.getMessage())
                    .collect(Collectors.toList());
            JsonObject resp = new JsonObject();
            resp.put("errors", messages);

            return Response.status(Status.BAD_REQUEST).entity(messages).build();
        }
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "GETs a product by its uuid", description = "GETs a single product")
    @APIResponse(responseCode = "200", description = "Product search successful")
    @APIResponse(responseCode = "400", description = "Invalid product uuid")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    public Response get(@PathParam("uuid") String uuid) {
        Product product = productRepository.findByUUID(uuid);
        return Response.status(Status.OK).entity(product).build();
    }

    @GET
    @Operation(summary = "GETs all products", description = "GETs all the products")
    @APIResponse(responseCode = "200", description = "Product search successful. If the product is not found, you will receive null as a response")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    public Response get() {
        List<Product> all = productRepository.findAll().list();
        return Response.status(Status.OK).entity(all).build();
    }
}