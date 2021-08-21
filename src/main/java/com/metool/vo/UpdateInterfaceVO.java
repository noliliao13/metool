package com.metool.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateInterfaceVO implements Serializable {
    private Integer project_id;
    //分类ID
    private Integer catid;
    private String method;
    private String path;
    private String title;

    private Integer id;
    private String req_body_other;
    private String res_body;

    private List req_headers = new ArrayList();
    private Boolean api_opened = false;
    private String req_body_type = "json";
    private Boolean req_body_is_json_schema = true ;
    private String res_body_type = "json";
    private Boolean res_body_is_json_schema = true ;
}

