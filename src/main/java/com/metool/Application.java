package com.metool;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    public static Stage mainStage = null;

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/view/index.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), rectangle2D.getWidth(), rectangle2D.getHeight()-72);
        stage.setTitle("编程ME工具");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}