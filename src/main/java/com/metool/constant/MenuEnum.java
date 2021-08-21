package com.metool.constant;

/**
 * 菜单
 */
public enum MenuEnum {
    MENU_JSON_TOOL("menu_json_tool","JSON格式化","/view/json-tool.fxml"),
    MENU_DATE_TOOL("menu_date_tool","日期工具","/view/date-tool.fxml"),
    MENU_INTERFACE_TOOL("menu_interface_tool","接口工具","/view/interface-tool.fxml"),
    MENU_OTHER_TOOL("menu_other_tool","其他工具","/view/other-tool.fxml");
    private String code;
    private String name;
    private String fxmlPath;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public static MenuEnum getByCode(String code) {
        for (MenuEnum value : MenuEnum.values()) {
            if(value.code.equals(code)){
                return value;
            }
        }
        return null;
    }
    public static MenuEnum getByName(String name) {
        for (MenuEnum value : MenuEnum.values()) {
            if(value.name.equals(name)){
                return value;
            }
        }
        return null;
    }

    MenuEnum(String code, String name, String fxmlPath) {
        this.code = code;
        this.name = name;
        this.fxmlPath = fxmlPath;
    }

    public static MenuEnum getJson(){
        return MENU_JSON_TOOL;
    }
    public static MenuEnum getDate(){
        return MENU_DATE_TOOL;
    }
    public static MenuEnum getInterface(){
        return MENU_INTERFACE_TOOL;
    }
    public static MenuEnum getOther(){
        return MENU_OTHER_TOOL;
    }
}
