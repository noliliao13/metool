package com.metool.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
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
public class ParameterController implements Initializable {
    @FXML
    private TextArea json;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
