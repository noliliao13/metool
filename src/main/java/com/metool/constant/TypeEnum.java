package com.metool.constant;

public enum TypeEnum {
    INTEGER("Integer","number",0)
    ,LONG("Long","number",0)
    ,STRING("String","string","")
    ,DATE("Date","string","2000-01-01 00:00:00")
    ,LOCAL_DATA("LocalDate","string","2000-01-01")
    ,LOCAL_DATA_TIME("LocalDateTime","string","2000-01-01 00:00:00")
    ,BIG_DECIMAL("BigDecimal","number",0.0)

    ;
    private String javaType;
    private String jsonType;
    private Object defaultValue;

    public String getJavaType() {
        return javaType;
    }

    public String getJsonType() {
        return jsonType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static TypeEnum getByJavaType(String javaType) {
        for (TypeEnum value : TypeEnum.values()) {
            if(value.javaType.equals(javaType)){
                return value;
            }
        }
        return null;
    }

    public static TypeEnum getByJsonType(String jsonType) {
        for (TypeEnum value : TypeEnum.values()) {
            if(value.jsonType.equals(jsonType)){
                return value;
            }
        }
        return null;
    }

    TypeEnum(String javaType, String jsonType, Object defaultValue) {
        this.javaType = javaType;
        this.jsonType = jsonType;
        this.defaultValue = defaultValue;
    }
}
