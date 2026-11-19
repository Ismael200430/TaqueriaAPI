package com.taqueria.config;

import com.taqueria.resource.CategoriaResource;
import com.taqueria.resource.DetalleOrdenResource;
import com.taqueria.resource.OrdenResource;
import com.taqueria.resource.PingResource;
import com.taqueria.resource.ProductoResource;
import com.taqueria.resource.UsuarioResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.Set;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                UsuarioResource.class,
                CategoriaResource.class,
                ProductoResource.class,
                OrdenResource.class,
                DetalleOrdenResource.class,
                PingResource.class,
                CorsFilter.class,
                JacksonFeature.class
        );
    }
}
