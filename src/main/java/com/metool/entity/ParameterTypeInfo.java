package com.metool.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ParameterTypeInfo implements Serializable {
    private String reqPath;
    private String resPath;
    private String reqType;
    private String resType;
    private Boolean isReqList = false;
    private Boolean isResList = false;
    private Boolean isReqFormData = false;
    private List<FormDataItem> parameters = new ArrayList<>();
    private List<String> validatedList = new ArrayList<>();
    private Boolean isValidated = false;

}
