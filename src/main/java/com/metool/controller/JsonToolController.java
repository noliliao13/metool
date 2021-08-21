package com.metool.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.Audio;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.net.URL;
import java.util.ResourceBundle;

public class JsonToolController implements Initializable {
    @FXML
    private TextArea sourceJson;
    @FXML
    private TextArea targetJson;
    @FXML
    private Button btnCopyValue;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sourceJson.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String str = "";
                try {
                    if(StringUtils.isNoneBlank(newValue.trim())){
                        JSONObject json = JSONObject.parseObject(newValue);
                        str = JSON.toJSONString(json, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue);
                    }
                    targetJson.setText(str);
                }catch (Exception e){

                }
            }
        });

    }

    public void copyValue(ActionEvent event){
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.put(DataFormat.PLAIN_TEXT,targetJson.getText());
        clipboard.setContent(clipboardContent);
        ToastBarToasterService service = new ToastBarToasterService();
        service.initialize();
        service.success("提示","复制成功!",ToastParameter.builder().timeout(Duration.seconds(2)).build());
    }
}