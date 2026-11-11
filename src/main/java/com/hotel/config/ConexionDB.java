package com.hotel.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/** Clase que maneja la conexión con la base de datos. */
public class ConexionDB {

    public static Connection getConexion(){
         /** Crea una instancia de tipo props:
         *  permite guardar datos de tipo clave=valor, para asegurar que todos puedan
         *  usar la misma base de datos sin cambiar los valores, y evita mostrar las credenciales.
         */
        Properties props = new Properties();

        /**
         *  Guarda dentro de un InputStream los clave=valor dentro de database.properties
         */
        try(InputStream input = ConexionDB.class.getResourceAsStream("/database.properties")){
            if(input == null) { throw new RuntimeException("No se encontró el archivo 'database.properties' en el classpath."); }

            // Guarda input en formato properties
            props.load(input);

            // Obtiene cada valor usando las claves.
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            return DriverManager.getConnection(url, user, password);
        }catch (Exception e) {
            /**
             * Se lanza una RuntimeException excepción para indicar error en la conexión.
             */
            throw new RuntimeException("Error crítico: No se pudo establecer la conexión a la base de datos.", e);
        }
    }

}
