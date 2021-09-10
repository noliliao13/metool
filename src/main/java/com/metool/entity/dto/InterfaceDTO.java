package com.metool.entity.dto;

import com.metool.entity.FormDataItem;
import com.metool.entity.ReqHeader;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/8
 */
@Data
public class InterfaceDTO {
    private String token;
    private Integer project_id;
    private Integer catid;
    private String method;
    private String path;
    private String title;
    private Integer id;
    private String req_body_other;
    private String res_body;

    private List<ReqHeader> req_headers = new ArrayList<>();
    private List<FormDataItem> req_body_form = new ArrayList<>();

    private String req_body_type = "json";
    private String res_body_type = "json";
    private Boolean api_opened = false;
    private Boolean req_body_is_json_schema = true;
    private Boolean res_body_is_json_schema = false;

}
