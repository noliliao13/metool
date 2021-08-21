package com.metool.entity.view;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metool.constant.TypeEnum;
import com.metool.controller.InterfaceListItemController;
import com.metool.entity.InterfaceDetail;
import com.metool.entity.InterfaceParameterBody;
import com.metool.vo.AddInterfaceVO;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import lombok.SneakyThrows;
import java.util.Map;

public class InterfaceListCall extends ListCell<InterfaceDetail>  {

    @SneakyThrows
    @Override
    protected void updateItem(InterfaceDetail item, boolean empty) {
        super.updateItem(item, empty);
        if(item == null){
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/view/interface-list-item.fxml"));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        GridPane gridPane = fxmlLoader.load();
        InterfaceListItemController controller = fxmlLoader.getController();
        controller.getApiPath().setText(item.getPath());
        controller.getApiController().setText(item.getController());
        controller.getApiMethod().setText(item.getMethod());
        if(item.getReqEntity() != null){
            Object obj = getJson(item.getReqEntity());
            if(obj instanceof JSONObject && ((JSONObject)obj).size() == 0){
                controller.getRequestBody().setText("");
            }else{
                controller.getRequestBody().setText(JSON.toJSONString(obj));
            }
        }
        if(item.getResEntity() != null){
            Object obj = getJson(item.getResEntity());
            if(obj instanceof JSONObject && ((JSONObject)obj).size() == 0){
                controller.getResponseBody().setText("");
            }else{
                controller.getResponseBody().setText(JSON.toJSONString(obj));
            }
        }

        controller.getCheckApi().setUserData(item.getMethod()+item.getPath());
        controller.getApiName().setUserData(item.getMethod()+item.getPath());
        controller.getChooseProject().setUserData(item.getMethod()+item.getPath());
        controller.getChooseInterfaceType().setUserData(item.getMethod()+item.getPath());
        controller.getBtnPushYapi().setUserData(item.getMethod()+item.getPath());
        setGraphic(gridPane);

        AddInterfaceVO addInterfaceVO = new AddInterfaceVO();
        addInterfaceVO.setInterfaceDetail(item);
        BeanUtil.copyProperties(item,addInterfaceVO);
        InterfaceListItemController.addInterfaceVOMap.put(item.getMethod()+item.getPath(),addInterfaceVO);
    }

    private Object getJson(InterfaceParameterBody entity) {
        JSON result = new JSONObject();
        if(entity == null || entity.getType() == null){
            return result;
        }
        if(entity.getType().equals("array")){
            Object obj = getJson(entity.getItems());
            result = new JSONArray();
            ((JSONArray)result).add(obj);
        }else if(entity.getType().equals("object")){
            result = new JSONObject();
            for (Map.Entry<String, InterfaceParameterBody> b : entity.getProperties().entrySet()) {
                if(b.getValue().getType().equals("array")){
                    Object obj = getJson(b.getValue().getItems());
                    JSONArray array = new JSONArray();
                    array.add(obj);
                    ((JSONObject)result).put(b.getKey(),array);
                }else if(b.getValue().getType().equals("object")){
                    JSONObject obj = new JSONObject();
                    for (Map.Entry<String, InterfaceParameterBody> sb : b.getValue().getProperties().entrySet()) {
                        obj.put(sb.getKey(),getJson(sb.getValue()));
                    }
                   ((JSONObject)result).put(b.getKey(),obj);
                }else{
                    ((JSONObject)result).put(b.getKey(), TypeEnum.getByJavaType(b.getValue().getJavaType()).getDefaultValue());
                }
            }
        }else {
            return TypeEnum.getByJavaType(entity.getJavaType()).getDefaultValue();
        }

        return result;
    }
}
