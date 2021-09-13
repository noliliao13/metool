package com.metool.controller;

import com.metool.entity.view.MenuEntity;
import com.metool.service.LocalService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class IndexController implements Initializable {
    @FXML
    private TabPane tabPane;
    @FXML
    private MenuBar menuBar;

    private LocalService localService = new LocalService();

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<MenuEntity> mList = localService.getMenu();

        for (MenuEntity entity : mList) {
            Menu menu = new Menu(entity.getName());
            for (MenuEntity child : entity.getChildren()) {
                MenuItem item = new MenuItem(child.getName());
                item.setOnAction(new EventHandler<ActionEvent>() {
                    @SneakyThrows
                    @Override
                    public void handle(ActionEvent event) {
                        String path = child.getPath();
                        Tab temp = null;
                        for (Tab tab : tabPane.getTabs()) {
                            if(tab.getText().equals(item.getText())){
                                if(child.getIsMulti()){
                                    temp = new Tab(item.getText(),FXMLLoader.load(getClass().getResource(path)));
                                    tabPane.getTabs().add(temp);
                                }else{
                                    temp = tab;
                                }
                                break;
                            }
                        }
                        if(temp == null){
                            temp = new Tab(item.getText(),FXMLLoader.load(getClass().getResource(path)));
                            tabPane.getTabs().add(temp);
                        }
                        tabPane.getSelectionModel().select(temp);
                    }
                });
                menu.getItems().add(item);
            }
            menuBar.getMenus().add(menu);
        }
        MenuEntity defaultMenu = mList.get(0).getChildren().get(0);
        tabPane.getTabs().add(new Tab(defaultMenu.getName(),FXMLLoader.load(getClass().getResource(defaultMenu.getPath()))));
    }

}