package com.hotel.vistas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class IniciadorVentana extends Application{
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(IniciadorVentana.class.getResource("/views/login/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 400);
        stage.setTitle("Log in");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
