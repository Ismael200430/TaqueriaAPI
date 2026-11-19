package com.taqueria.dao;

import com.taqueria.config.ConexionDB;
import com.taqueria.model.Orden;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OrdenDAO {

    private static final String SELECT_BASE =
            "SELECT id_orden, numero_mesa, fecha_hora, estado, total, id_usuario FROM orden";

    public List<Orden> listar() throws SQLException {
        List<Orden> ordenes = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY id_orden";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ordenes.add(mapear(rs));
            }
        }
        return ordenes;
    }

    public Orden buscarPorId(Integer id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_orden = ?";

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

    public int insertar(Orden orden) throws SQLException {
        String sql = "INSERT INTO orden (numero_mesa, fecha_hora, estado, total, id_usuario) "
                + "VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setInteger(ps, 1, orden.getNumeroMesa());
            setFechaHora(ps, 2, orden.getFechaHora());
            ps.setString(3, (orden.getEstado() == null || orden.getEstado().isBlank())
                    ? "Abierta" : orden.getEstado());
            ps.setBigDecimal(4, orden.getTotal() == null ? BigDecimal.ZERO : orden.getTotal());
            setInteger(ps, 5, orden.getIdUsuario());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID generado de la orden");
    }

    public boolean actualizar(Orden orden) throws SQLException {
        String sql = "UPDATE orden SET numero_mesa = ?, "
                + "fecha_hora = COALESCE(?, fecha_hora), "
                + "estado = COALESCE(?, estado), "
                + "total = COALESCE(?, total), "
                + "id_usuario = ? WHERE id_orden = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setInteger(ps, 1, orden.getNumeroMesa());
            setFechaHora(ps, 2, orden.getFechaHora());
            ps.setString(3, orden.getEstado());
            if (orden.getTotal() == null) {
                ps.setNull(4, Types.DECIMAL);
            } else {
                ps.setBigDecimal(4, orden.getTotal());
            }
            setInteger(ps, 5, orden.getIdUsuario());
            ps.setInt(6, orden.getIdOrden());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM orden WHERE id_orden = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Orden mapear(ResultSet rs) throws SQLException {
        Orden orden = new Orden();
        orden.setIdOrden(rs.getInt("id_orden"));

        int numeroMesa = rs.getInt("numero_mesa");
        orden.setNumeroMesa(rs.wasNull() ? null : numeroMesa);

        // MySQL entrega DATETIME como "yyyy-MM-dd HH:mm:ss".
        orden.setFechaHora(rs.getString("fecha_hora"));
        orden.setEstado(rs.getString("estado"));
        orden.setTotal(rs.getBigDecimal("total"));

        int idUsuario = rs.getInt("id_usuario");
        orden.setIdUsuario(rs.wasNull() ? null : idUsuario);

        return orden;
    }

    private void setFechaHora(PreparedStatement ps, int indice, String fechaHora)
            throws SQLException {
        Timestamp timestamp = parseFechaHora(fechaHora);
        if (timestamp == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(indice, timestamp);
        }
    }

    private Timestamp parseFechaHora(String fechaHora) throws SQLException {
        if (fechaHora == null || fechaHora.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(fechaHora.trim());
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                    "Formato de fecha inválido. Use yyyy-MM-dd HH:mm:ss", e);
        }
    }

    private void setInteger(PreparedStatement ps, int indice, Integer valor)
            throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, valor);
        }
    }
}
