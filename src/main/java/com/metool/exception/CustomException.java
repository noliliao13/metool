package com.metool.exception;

public class CustomException extends RuntimeException{
    public CustomException() {
    }

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }

    public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static CustomException error(String message){
        return new CustomException(message);
    }
    public static CustomException error(Throwable cause){
        return new CustomException(cause);
    }
    public static CustomException error(String message,Throwable cause){
        return new CustomException(message,cause);
    }

}
