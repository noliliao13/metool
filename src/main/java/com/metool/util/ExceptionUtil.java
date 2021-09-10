package com.metool.util;

import com.metool.exception.CustomException;
import javafx.util.Duration;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;

/**
 * @Desccription 异常工具类
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/6
 */
public class ExceptionUtil {
    public static void doException(Exception e){
        ToastBarToasterService service = new ToastBarToasterService();
        service.initialize();
        e.printStackTrace();
        if(e instanceof CustomException){
            service.fail("错误",e.getMessage(), ToastParameter.builder().timeout(Duration.seconds(2)).build());
        }else{
            service.fail("错误","系统异常，请稍后重试！", ToastParameter.builder().timeout(Duration.seconds(2)).build());
        }
    }
}
