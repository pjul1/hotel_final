package com.hotel.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexionDB {

    public static Connection getConexion(){
        Properties props = new Properties();

        try(InputStream input = ConexionDB.class.getResourceAsStream("/database.properties.example")){
            if(input == null) { throw new RuntimeException("No se encontró el archivo 'database.properties' en el classpath."); }
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            return DriverManager.getConnection(url, user, password);
        }catch (Exception e) {
            throw new RuntimeException("Error crítico: No se pudo establecer la conexión a la base de datos.", e);
        }
    }

}
