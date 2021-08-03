package com.github.kgoedert.crm;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Provider
public class InvalidFormatExceptionHandler implements ExceptionMapper<InvalidFormatException> {

    @Override
    public Response toResponse(InvalidFormatException exception) {
        String sentValue = exception.getValue().toString();
        String target = exception.getTargetType().getSimpleName();

        String message;
        if ( Enum.class.isAssignableFrom( exception.getTargetType() ) ) {
            Object[] constants = exception.getTargetType().getEnumConstants();

            List<String> values = Arrays.stream( constants )
                    .map( c -> (Enum) c )
                    .map( c -> c.name() ).collect( Collectors.toList() );

            message = String.format(
                    "The value '%s' is not allowed for the type %s. The allowed values are: %s.",
                    sentValue, target, values
            );
        }
        else {
            message = exception.getMessage();
        }

        return Response.status(Status.BAD_REQUEST).entity(message).build();
    }

}