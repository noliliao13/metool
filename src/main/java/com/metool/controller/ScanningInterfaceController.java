package com.metool.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/9
 */
@Data
public class ScanningInterfaceController implements Initializable {

    @FXML
    private ScrollPane sp;
    @FXML
    private GridPane gp;
    @FXML
    private HBox titleBox;
    @FXML
    private Button cancelScanningBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
