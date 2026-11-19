package com.taqueria.resource;

import com.taqueria.dao.OrdenDAO;
import com.taqueria.model.Orden;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.sql.SQLException;
import java.util.Map;

@Path("/ordenes")
@Produces(MediaType.APPLICATION_JSON)
public class OrdenResource {

    private final OrdenDAO dao = new OrdenDAO();

    @Context
    private UriInfo uriInfo;

    @GET
    public Response listar() {
        try {
            return Response.ok(dao.listar()).build();
        } catch (SQLException | RuntimeException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") Integer id) {
        try {
            Orden orden = dao.buscarPorId(id);
            if (orden == null) {
                return error(Response.Status.NOT_FOUND, "Orden no encontrada");
            }
            return Response.ok(orden).build();
        } catch (SQLException | RuntimeException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(Orden orden) {
        try {
            orden.setIdOrden(null);
            int id = dao.insertar(orden);
            Orden creada = dao.buscarPorId(id);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(id))
                    .build();
            return Response.created(location).entity(creada).build();
        } catch (SQLException | RuntimeException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") Integer id, Orden orden) {
        try {
            if (dao.buscarPorId(id) == null) {
                return error(Response.Status.NOT_FOUND, "Orden no encontrada");
            }

            orden.setIdOrden(id);
            if (!dao.actualizar(orden)) {
                return error(Response.Status.NOT_FOUND, "Orden no encontrada");
            }

            return Response.ok(dao.buscarPorId(id)).build();
        } catch (SQLException | RuntimeException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") Integer id) {
        try {
            if (dao.buscarPorId(id) == null) {
                return error(Response.Status.NOT_FOUND, "Orden no encontrada");
            }

            if (!dao.eliminar(id)) {
                return error(Response.Status.NOT_FOUND, "Orden no encontrada");
            }

            return Response.ok(Map.of("mensaje", "Orden eliminada"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException | RuntimeException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Response error(Response.Status status, String mensaje) {
        String texto = (mensaje == null || mensaje.isBlank())
                ? "Error interno del servidor" : mensaje;
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", texto))
                .build();
    }
}
