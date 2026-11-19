package com.taqueria.dao;

import com.taqueria.config.ConexionDB;
import com.taqueria.model.DetalleOrden;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DetalleOrdenDAO {

    private static final String SELECT_BASE =
            "SELECT id_detalle, cantidad, subtotal, notas, id_orden, id_producto "
            + "FROM detalle_orden";

    public List<DetalleOrden> listar() throws SQLException {
        List<DetalleOrden> detalles = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY id_detalle";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                detalles.add(mapear(rs));
            }
        }
        return detalles;
    }

    public DetalleOrden buscarPorId(Integer id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_detalle = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public int insertar(DetalleOrden detalle) throws SQLException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = insertarDetalle(con, detalle);
                recalcularTotal(con, detalle.getIdOrden());
                con.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            }
        }
    }

    public boolean actualizar(DetalleOrden detalle) throws SQLException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer idOrdenAnterior = obtenerIdOrden(con, detalle.getIdDetalle());
                if (idOrdenAnterior == null) {
                    con.rollback();
                    return false;
                }

                Integer idOrdenNuevo = detalle.getIdOrden() != null
                        ? detalle.getIdOrden() : idOrdenAnterior;

                String sql = "UPDATE detalle_orden SET cantidad = ?, subtotal = ?, notas = ?, "
                        + "id_orden = ?, id_producto = ? WHERE id_detalle = ?";
                int filas;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setObject(1, detalle.getCantidad(), Types.INTEGER);
                    ps.setBigDecimal(2, detalle.getSubtotal());
                    ps.setString(3, detalle.getNotas());
                    ps.setInt(4, idOrdenNuevo);
                    ps.setObject(5, detalle.getIdProducto(), Types.INTEGER);
                    ps.setInt(6, detalle.getIdDetalle());
                    filas = ps.executeUpdate();
                }

                if (filas == 0) {
                    con.rollback();
                    return false;
                }

                recalcularTotal(con, idOrdenAnterior);
                if (!idOrdenNuevo.equals(idOrdenAnterior)) {
                    recalcularTotal(con, idOrdenNuevo);
                }

                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            }
        }
    }

    public boolean eliminar(Integer idDetalle) throws SQLException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer idOrden = obtenerIdOrden(con, idDetalle);
                if (idOrden == null) {
                    con.rollback();
                    return false;
                }

                int filas;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM detalle_orden WHERE id_detalle = ?")) {
                    ps.setInt(1, idDetalle);
                    filas = ps.executeUpdate();
                }

                if (filas == 0) {
                    con.rollback();
                    return false;
                }

                recalcularTotal(con, idOrden);
                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(con, e);
                throw e;
            }
        }
    }

    private int insertarDetalle(Connection con, DetalleOrden detalle) throws SQLException {
        String sql = "INSERT INTO detalle_orden (cantidad, subtotal, notas, id_orden, id_producto) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, detalle.getCantidad(), Types.INTEGER);
            ps.setBigDecimal(2, detalle.getSubtotal());
            ps.setString(3, detalle.getNotas());
            ps.setObject(4, detalle.getIdOrden(), Types.INTEGER);
            ps.setObject(5, detalle.getIdProducto(), Types.INTEGER);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID generado del detalle");
    }

    private Integer obtenerIdOrden(Connection con, Integer idDetalle) throws SQLException {
        String sql = "SELECT id_orden FROM detalle_orden WHERE id_detalle = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_orden");
                }
            }
        }
        return null;
    }

    private void recalcularTotal(Connection con, Integer idOrden) throws SQLException {
        if (idOrden == null) {
            return;
        }

        // Bloquea la orden durante la transacción para evitar totales inconsistentes
        // si dos detalles de la misma orden se modifican al mismo tiempo.
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_orden FROM orden WHERE id_orden = ? FOR UPDATE")) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                // Solo se necesita bloquear la fila.
            }
        }

        BigDecimal total;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COALESCE(SUM(subtotal), 0) FROM detalle_orden WHERE id_orden = ?")) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getBigDecimal(1);
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE orden SET total = ? WHERE id_orden = ?")) {
            ps.setBigDecimal(1, total == null ? BigDecimal.ZERO : total);
            ps.setInt(2, idOrden);
            ps.executeUpdate();
        }
    }

    private void rollback(Connection con, Throwable causa) {
        try {
            con.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);
        }
    }

    private DetalleOrden mapear(ResultSet rs) throws SQLException {
        DetalleOrden detalle = new DetalleOrden();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        detalle.setNotas(rs.getString("notas"));
        detalle.setIdOrden(rs.getInt("id_orden"));
        detalle.setIdProducto(rs.getInt("id_producto"));
        return detalle;
    }
}
