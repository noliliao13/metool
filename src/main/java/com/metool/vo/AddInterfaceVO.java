package com.metool.vo;

import com.metool.entity.InterfaceDetail;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddInterfaceVO implements Serializable {
    private Integer project_id = 0;
    private Boolean isCheck = false;
    //分类ID
    private Integer catid = 0;
    private String method = "";
    private String path  = "";
    private String title = "";
    private InterfaceDetail interfaceDetail = new InterfaceDetail();
}
