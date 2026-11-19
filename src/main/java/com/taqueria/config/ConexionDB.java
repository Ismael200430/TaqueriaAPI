package com.taqueria.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado", e);
        }

        // Si existen variables de entorno (Nube/Aiven), las usa; si no, usa tu MySQL local
        String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
        String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
        String db   = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "taqueria_db";
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
        String pass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "root"; // Pon tu contraseña local si tienes
        String ssl  = System.getenv("DB_SSL")  != null ? System.getenv("DB_SSL")  : "false";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db 
                   + "?useSSL=" + ssl + "&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City";
                   
        return DriverManager.getConnection(url, user, pass);
    }
}
