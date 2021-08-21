package com.metool.constant;

import com.metool.entity.IdAndName;

import java.util.ArrayList;
import java.util.List;

public class ApiConstant {
    public static final List<IdAndName> YAPI_GROUP_LIST = new ArrayList<>();
    public static String BASE_URL = "http://192.168.33.110:3000";
    public  static String TOKEN = "_yapi_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOjExLCJpYXQiOjE2MjkyNzg3NTgsImV4cCI6MTYyOTg4MzU1OH0.0gjRZ3WXV1Rj38a8yu2J9EN-bwlKp2Z5IY3gDDhGO9w; _yapi_uid=11";
//    public static String BASE_URL = "";
//    public  static String TOKEN = "";
    public static final String GET_GROUP_URL = "/api/group/list";
    public static final String GET_PROJECT_URL = "/api/project/list";
    public static final String GET_INTERFACE_MENU_URL = "/api/interface/list_menu";

    public static final String ADD_INTERFACE = "/api/interface/add";
    public static final String UPDATE_INTERFACE = "/api/interface/up";
    public static final String DELETE_INTERFACE = "/api/interface/del";

}
