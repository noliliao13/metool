package com.metool.comment;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Data;

import java.util.Objects;

/**
 * @Desccription tost tool
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/6
 */
@Data
public class ProgressStage {
    private Stage stage;
    private Task<?> work;
    private boolean isEnd = false;

    private ProgressStage() {}

    public static ProgressStage of(Stage parent,String ad,Task<?> work){
        ProgressStage ps = new ProgressStage();
        ps.work = Objects.requireNonNull(work);
        ps.initUI(parent,ad);
        return ps;
    }

    public static ProgressStage of(Stage parent, Pane node, Task<?> work){
        ProgressStage ps = new ProgressStage();
        ps.work = Objects.requireNonNull(work);
        ps.initUI(parent,node);
        return ps;
    }

    public void show(){
        new Thread(work).start();
        stage.show();
    }

    private void initUI(Stage parent, String ad) {
        stage = new Stage();
        stage.initOwner(parent);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        Label adLbl = new Label(ad);
        adLbl.setTextFill(Color.BLUE);

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.setPrefHeight(40);
        indicator.setPrefWidth(40);
        indicator.progressProperty().bind(work.progressProperty());

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setBackground(Background.EMPTY);
        vBox.getChildren().addAll(indicator,adLbl);

        Scene scene = new Scene(vBox);
        scene.setFill(null);
        stage.setScene(scene);
        int v = ad.length() * 8 +10;
        if(v < 40){
            v = 40;
        }
        stage.setWidth(v);
        stage.setHeight(v);

        double x = parent.getX()+(parent.getWidth()-stage.getWidth())/2;
        double y = parent.getY()+(parent.getHeight()-stage.getHeight())/2;
        stage.setX(x);
        stage.setY(y);

        work.setOnCancelled(e -> stage.close());
        work.setOnScheduled(e -> stage.close());
    }

    private void initUI(Stage parent, Pane node) {
        stage = new Stage();
        stage.initOwner(parent);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);


        Scene scene = new Scene(node);
        scene.setFill(null);
        stage.setScene(scene);
        stage.setWidth(node.getPrefWidth()+20);
        stage.setHeight(node.getPrefHeight()+20);

        double x = parent.getX()+(parent.getWidth()-stage.getWidth())/2;
        double y = parent.getY()+(parent.getHeight()-stage.getHeight())/2;
        stage.setX(x);
        stage.setY(y);

        work.setOnCancelled(e -> {
            stage.close();
            isEnd = true;
        });
        work.setOnScheduled(e -> {
            stage.close();
            isEnd = true;
        });
    }


}
