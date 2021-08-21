package com.metool.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class InterfaceDetail implements Serializable {
    private String controller = "";
    private String path = "";
    private String title = "";
    private String method ="";
    private List<KeyValue> parameterPath = new ArrayList<>();
    private Boolean isReqList = false;
    private Boolean isResList = false;
//    private JSON reqJson;
//    private JSON resJson;
    private InterfaceParameterBody reqEntity;
    private InterfaceParameterBody resEntity;

    @Data
    public static class KeyValue implements Serializable{
        private String key = "";
        private String value = "";
    }
}
