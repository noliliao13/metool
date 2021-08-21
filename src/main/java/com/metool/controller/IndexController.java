package com.metool.controller;

import com.metool.constant.MenuEnum;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class IndexController implements Initializable {
    @FXML
    private BorderPane indexPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            indexPane.setCenter(FXMLLoader.load(getClass().getResource(MenuEnum.MENU_JSON_TOOL.getFxmlPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showOk(ActionEvent event) {
        Button btn = (Button) event.getTarget();
        try {
            String path = MenuEnum.getByName(btn.getText()).getFxmlPath();
            indexPane.setCenter(FXMLLoader.load(getClass().getResource(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}