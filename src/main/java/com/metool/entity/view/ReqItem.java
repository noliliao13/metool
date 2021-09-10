package com.metool.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/9
 */
@Data@AllArgsConstructor
@NoArgsConstructor
public class ReqItem implements Serializable {
    private String name = "";
    private String type = "";
    private String required = "";
    private String defaultValue = "";
    private String remark = "";
}
