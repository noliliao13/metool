package com.metool.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Desccription 请求头
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqHeader implements Serializable {
    private String name;
    private String value;
}
