package com.metool.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class InterfaceParameterBody implements Serializable {

    private String type;
    private String javaType;
    private String description;
    private Map<String,InterfaceParameterBody> properties = new HashMap<>();
    private InterfaceParameterBody items;
    private List<String> required = new ArrayList<>();

    public InterfaceParameterBody() {

    }
    public InterfaceParameterBody(String type) {
        this.type = type;
    }
}
