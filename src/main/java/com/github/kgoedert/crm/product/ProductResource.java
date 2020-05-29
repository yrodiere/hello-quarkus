package com.github.kgoedert.crm.product;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "GETs a product by its uuid", description = "GETs a single product")
    @APIResponse(responseCode = "200", description = "Product search successful")
    @APIResponse(responseCode = "404", description = "Product not found")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    public Response get(@PathParam("uuid") String uuid) {
        Product product = productRepository.findByUUID(uuid);
        if (product == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.status(Status.OK).entity(product).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "GETs all products", description = "GETs all the products")
    @APIResponse(responseCode = "200", description = "Product search successful. If the product is not found, you will receive null as a response")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    @Parameters({
            @Parameter(name = "category", description = "The category to filter for", required = false, example = "DAIRY"),
            @Parameter(name = "page", description = "The page number to fetch. Page numbers start at 1", required = false, example = "1"),
            @Parameter(name = "pageSize", description = "The numbers of elements to fetch", required = false, example = "10"),
            @Parameter(name = "sort", description = "The name of the attribute you want to sort for, and its direction", required = false, example = "+name,-category") })
    public Response get(@QueryParam("category") String category, @QueryParam("page") int page,
            @QueryParam("pageSize") int pageSize, @QueryParam("sort") String sort) {

        List<Product> all = productRepository.findByFilters(category, page, pageSize, sort);
        return Response.status(Status.OK).entity(all).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Modifies a product")
    @APIResponse(responseCode = "201", description = "Mofication was successful")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    @APIResponse(responseCode = "400", description = "Invalid Product")
    public Response modify(Product product) {
        return null;
    }

    @DELETE
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Deletes a product by its uuid")
    @APIResponse(responseCode = "200", description = "Product exclusion successful")
    @APIResponse(responseCode = "400", description = "Invalid product uuid")
    @APIResponse(responseCode = "500", description = "Server unavailable")
    public Response delete(@PathParam("uuid") String uuid) {
        return null;
    }

}