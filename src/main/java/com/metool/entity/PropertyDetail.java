package com.metool.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import java.io.Serializable;
import java.util.Collection;

@Data
public class PropertyDetail implements Serializable {
    private String type;
    private String name;
    private String path;
    private Boolean isArray = false;
    private String description;
    private Boolean isRequired = false;
    private Boolean isValid = false;
    private String key;
}
