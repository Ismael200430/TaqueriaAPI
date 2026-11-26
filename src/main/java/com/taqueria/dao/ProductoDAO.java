package com.taqueria.dao;

import com.taqueria.config.ConexionDB;
import com.taqueria.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SELECT_BASE =
            "SELECT id_producto, nombre, precio, disponible, id_categoria, imagen FROM producto";

    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY id_producto";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }

    public Producto buscarPorId(Integer id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_producto = ?";

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

    public int insertar(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (nombre, precio, disponible, id_categoria, imagen) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getNombre());
            ps.setBigDecimal(2, producto.getPrecio());
            ps.setBoolean(3, producto.getDisponible() == null ? true : producto.getDisponible());
            setInteger(ps, 4, producto.getIdCategoria());
            ps.setString(5, normalizarImagen(producto.getImagen()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID generado del producto");
    }

    public boolean actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, disponible = ?, "
                + "id_categoria = ?, imagen = ? WHERE id_producto = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setBigDecimal(2, producto.getPrecio());
            ps.setBoolean(3, producto.getDisponible() == null ? true : producto.getDisponible());
            setInteger(ps, 4, producto.getIdCategoria());
            ps.setString(5, normalizarImagen(producto.getImagen()));
            ps.setInt(6, producto.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto = ?";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecio(rs.getBigDecimal("precio"));

        boolean disponible = rs.getBoolean("disponible");
        producto.setDisponible(rs.wasNull() ? null : disponible);

        int idCategoria = rs.getInt("id_categoria");
        producto.setIdCategoria(rs.wasNull() ? null : idCategoria);

        producto.setImagen(rs.getString("imagen"));

        return producto;
    }

    private String normalizarImagen(String imagen) {
        return (imagen == null || imagen.isBlank()) ? "taco_pastor" : imagen;
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
