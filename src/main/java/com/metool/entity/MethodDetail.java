package com.metool.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MethodDetail implements Serializable {
    private String path;
    private String type;
    private ParameterTypeInfo parameterTypeInfo;
}
