package com.metool.entity.view;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/9
 */
@Data
public class MenuEntity implements Serializable {
    private String name;
    private String path;
    private Boolean isMulti = false;
    private List<MenuEntity> children;
}
