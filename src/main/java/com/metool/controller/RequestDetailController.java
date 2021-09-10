package com.metool.controller;

import com.metool.entity.view.ReqItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Data;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;

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
public class RequestDetailController implements Initializable {
    @FXML
    private TreeTableView reqTreeTable;
    @FXML
    private TreeTableColumn<ReqItem,String> name;
    @FXML
    private TreeTableColumn<ReqItem,String> type;
    @FXML
    private TreeTableColumn<ReqItem,String> required;
    @FXML
    private TreeTableColumn<ReqItem,String> defaultValue;
    @FXML
    private TreeTableColumn<ReqItem,String> remark;
    @FXML
    private TextArea reqJson;
    @FXML
    private VBox jsonBox;
    @FXML
    private Label dataType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeItem root = new TreeItem(new ReqItem());
        root.setExpanded(true);
        reqTreeTable.setRoot(root);
        name.setCellValueFactory((TreeTableColumn.CellDataFeatures<ReqItem,String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
        type.setCellValueFactory((TreeTableColumn.CellDataFeatures<ReqItem,String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getType()));
        required.setCellValueFactory((TreeTableColumn.CellDataFeatures<ReqItem,String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getRequired()));
        defaultValue.setCellValueFactory((TreeTableColumn.CellDataFeatures<ReqItem,String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getDefaultValue()));
        remark.setCellValueFactory((TreeTableColumn.CellDataFeatures<ReqItem,String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getRemark()));
    }

    public void copyValue(ActionEvent event){
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.put(DataFormat.PLAIN_TEXT,reqJson.getText());
        clipboard.setContent(clipboardContent);
        ToastBarToasterService service = new ToastBarToasterService();
        service.initialize();
        service.success("提示","复制成功!", ToastParameter.builder().timeout(Duration.seconds(2)).build());
    }
}
