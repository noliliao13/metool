package com.metool.constant;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/8
 */
public class RegexConstant {
    public static final String LIST = "^(List|ArrayList|LinkedList|Set|HashSet)+((<(\\w|\\d)+\\>)|(-(\\w|\\d)+))*$";
    public static final String LIST_ = "\\s+(List|ArrayList|LinkedList|Set|HashSet)+?(\\s|<)+";
    public static final String LIST_T = "^(List|ArrayList|LinkedList|Set|HashSet)+((<T>)|-T)$";
}
