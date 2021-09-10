package com.metool.entity;

import com.alibaba.fastjson.annotation.JSONField;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class InterfaceDetail implements Serializable {
    private Integer id;
    //分类ID
    private Integer catid = 0;
    private String controller = "";
    private String status = "";
    private Integer reqType = 0;
    private String path = "";
    private String title = "";
    private String method ="";
    private Boolean isReqFormData = false;
    private List<KeyValue> parameterPath = new ArrayList<>();
    private Boolean isReqList = false;
    private Boolean isResList = false;
    private List<ReqHeader> redHeaders = new ArrayList<>();
    private List<FormDataItem> formDataItems = new ArrayList<>();
    private InterfaceParameterBody reqEntity;
    private InterfaceParameterBody resEntity;

    @JSONField(serialize = false)
    private TextField titleField = new TextField();
    @JSONField(serialize = false)
    private CheckBox checkBox = new CheckBox();
    @JSONField(serialize = false)
    private ComboBox catType = new ComboBox();
    @JSONField(serialize = false)
    private ComboBox statusCB = new ComboBox(FXCollections.observableArrayList(Arrays.asList("请选择状态","未完成","已完成")));

    @Data
    public static class KeyValue implements Serializable{
        private String key = "";
        private String value = "";
    }
}
