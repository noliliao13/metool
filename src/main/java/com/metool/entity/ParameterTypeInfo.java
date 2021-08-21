package com.metool.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ParameterTypeInfo implements Serializable {
    private String reqType;
    private String resType;
    private Boolean isReqList = false;
    private Boolean isResList = false;
}
