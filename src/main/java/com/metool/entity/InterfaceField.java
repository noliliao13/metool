package com.metool.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterfaceField implements Serializable {
    private String type;
    private String description;
}
