package com.taqueria.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ALLOW_ORIGIN = "*";
    private static final String ALLOW_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String ALLOW_HEADERS =
            "Content-Type, Accept, Authorization, Origin, X-Requested-With";
    private static final String EXPOSE_HEADERS = "Location";
    private static final String MAX_AGE = "3600";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            Response.ResponseBuilder builder = Response.ok();
            agregarCabeceras(builder);
            requestContext.abortWith(builder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", ALLOW_ORIGIN);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", ALLOW_METHODS);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", ALLOW_HEADERS);
        responseContext.getHeaders().putSingle("Access-Control-Expose-Headers", EXPOSE_HEADERS);
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", MAX_AGE);
    }

    private void agregarCabeceras(Response.ResponseBuilder builder) {
        builder.header("Access-Control-Allow-Origin", ALLOW_ORIGIN)
                .header("Access-Control-Allow-Methods", ALLOW_METHODS)
                .header("Access-Control-Allow-Headers", ALLOW_HEADERS)
                .header("Access-Control-Expose-Headers", EXPOSE_HEADERS)
                .header("Access-Control-Max-Age", MAX_AGE);
    }
}
