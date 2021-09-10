package com.metool.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/8
 */
@Data
public class ProjectInfo implements Serializable {
    private Integer id;
    private String name;
}
