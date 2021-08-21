package com.metool.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.metool.constant.ApiConstant;
import com.metool.entity.IdAndName;
import com.metool.entity.InterfaceDetail;
import com.metool.entity.InterfaceParameterBody;
import com.metool.vo.AddInterfaceVO;
import com.metool.vo.UpdateInterfaceVO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class InterfaceListItemController implements Initializable {

    @FXML
    private Text apiController;
    @FXML
    private Text apiPath;
    @FXML
    private TextField apiName;
    @FXML
    private Text apiMethod;
    @FXML
    private TextArea requestBody;
    @FXML
    private TextArea responseBody;
    @FXML
    private ComboBox chooseGroup;
    @FXML
    private ComboBox chooseProject;
    @FXML
    private ComboBox chooseInterfaceType;
    @FXML
    private Button btnPushYapi;
    @FXML
    private CheckBox checkApi;

    public static Map<String, AddInterfaceVO> addInterfaceVOMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boolean isVisible = StringUtils.isNoneBlank(ApiConstant.BASE_URL) && StringUtils.isNoneBlank(ApiConstant.TOKEN);

        apiName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                addInterfaceVOMap.get(apiName.getUserData()).getInterfaceDetail().setTitle(newValue);
                addInterfaceVOMap.get(apiName.getUserData()).setTitle(newValue);
            }
        });

        checkApi.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                addInterfaceVOMap.get(apiName.getUserData()).setIsCheck(newValue);
            }
        });

        if(!isVisible){
            return;
        }

        List<IdAndName> yApiGroupList = ApiConstant.YAPI_GROUP_LIST;
        chooseGroup.setItems(FXCollections.observableArrayList(yApiGroupList.stream().map(idAndName -> idAndName.getName()).collect(Collectors.toList())));
        chooseGroup.setValue(yApiGroupList.get(0).getName());
        chooseGroup.setVisible(true);
        chooseGroup.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() < 1 || newValue.intValue() >= yApiGroupList.size()){
                    return;
                }
                Integer groupId = yApiGroupList.get(newValue.intValue()).getId();
                //初始化工程
                String projectResult = null;
                try {
                    projectResult = HttpUtil.createGet(ApiConstant.BASE_URL+ApiConstant.GET_PROJECT_URL+"?group_id="+groupId+"&page=1&limit=1000").header("Cookie",ApiConstant.TOKEN).timeout(3000).execute().body();
                }catch (Exception e){

                }
                if(StringUtils.isBlank(projectResult)){
                    return;
                }
                List<IdAndName> projectList = JSONObject.parseObject(projectResult).getJSONObject("data").getJSONArray("list").stream().map(item ->{
                    JSONObject jb = (JSONObject) item;
                    IdAndName idAndName = new IdAndName(jb.getInteger("_id"),jb.getString("name"));
                    return idAndName;
                }).collect(Collectors.toList());
                projectList.add(0,new IdAndName(0,"请选择工程"));
                chooseProject.setItems(FXCollections.observableArrayList(projectList.stream().map(idAndName -> idAndName.getName()).collect(Collectors.toList())));
                chooseProject.setValue(projectList.get(0).getName());
                chooseProject.setVisible(true);

                chooseProject.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if(newValue.intValue() < 1 || newValue.intValue() >= projectList.size()){
                            return;
                        }
                        Integer projectId = projectList.get(newValue.intValue()).getId();
                        addInterfaceVOMap.get(chooseProject.getUserData()).setProject_id(projectId);

                        String interfaceTypeResult = null;
                        try {
                            interfaceTypeResult = HttpUtil.createGet(ApiConstant.BASE_URL+ApiConstant.GET_INTERFACE_MENU_URL+"?project_id="+projectId).header("Cookie",ApiConstant.TOKEN).timeout(3000).execute().body();
                        }catch (Exception e){

                        }
                        if(StringUtils.isBlank(interfaceTypeResult)){
                            return;
                        }
                        List<IdAndName> interfaceList = JSONObject.parseObject(interfaceTypeResult).getJSONArray("data").stream().map(item ->{
                            JSONObject jb = (JSONObject) item;
                            IdAndName idAndName = new IdAndName(jb.getInteger("_id"),jb.getString("name"));
                            return idAndName;
                        }).collect(Collectors.toList());
                        interfaceList.add(0,new IdAndName(0,"请选择分类"));
                        chooseInterfaceType.setItems(FXCollections.observableArrayList(interfaceList.stream().map(idAndName -> idAndName.getName()).collect(Collectors.toList())));
                        chooseInterfaceType.setValue(interfaceList.get(0).getName());
                        chooseInterfaceType.setVisible(true);
                        chooseInterfaceType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                if(newValue.intValue() < 1 || newValue.intValue() >= projectList.size()){
                                    return;
                                }
                                Integer interfaceMenuId = interfaceList.get(newValue.intValue()).getId();
                                addInterfaceVOMap.get(chooseInterfaceType.getUserData()).setCatid(interfaceMenuId);
                                btnPushYapi.setVisible(true);
                            }
                        });
                    }
                });
            }
        });

    }

    public void pushToYapi(ActionEvent event) {
        Button button = (Button) event.getTarget();
        AddInterfaceVO requestBody = addInterfaceVOMap.get(button.getUserData());
        try {
            pushApi(requestBody);
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.success("保存接口结果详情","恭喜你，接口保存成功",ToastParameter.builder().timeout(Duration.seconds(2)).build());
        } catch (Exception e) {
            e.printStackTrace();
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.fail("保存接口失败",e.getMessage(),ToastParameter.builder().timeout(Duration.seconds(2)).build());
        }
    }



    public static void pushApi(AddInterfaceVO requestBody) throws Exception {
        if(StringUtils.isBlank(requestBody.getTitle())){
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.fail("参数校验失败","请填写接口标题", ToastParameter.builder().timeout(Duration.seconds(2)).build());
            return;
        }
        if(requestBody.getProject_id().equals(0)){
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.fail("参数校验失败","请选择工程", ToastParameter.builder().timeout(Duration.seconds(2)).build());
            return;
        }
        if(requestBody.getCatid().equals(0)){
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.fail("参数校验失败","请选择接口类型", ToastParameter.builder().timeout(Duration.seconds(2)).build());
            return;
        }
        InterfaceDetail interfaceDetail = requestBody.getInterfaceDetail();
        BeanUtil.copyProperties(interfaceDetail,requestBody);
        Integer id = 0;
        try {
            String result = HttpUtil.createPost(ApiConstant.BASE_URL+ApiConstant.ADD_INTERFACE).header("Cookie",ApiConstant.TOKEN).body(JSON.toJSONString(requestBody,getFilter())).timeout(3000).execute().body();
            log.info(JSON.toJSONString(result));
            JSONObject json = JSONObject.parseObject(result);
            if(json.getInteger("errcode").equals(0)){
                id = json.getJSONObject("data").getInteger("_id");
            }else {
                throw new Exception("系统错误，稍后再试！");
            }
        }catch (Exception e){
            throw new Exception("系统错误，稍后再试！");
        }
        if(!id.equals(0)){
            try {
                InterfaceParameterBody reqParameter = interfaceDetail.getReqEntity();
                InterfaceParameterBody resParameter = interfaceDetail.getResEntity();
                log.info("reqParameter:{}",JSON.toJSONString(reqParameter));
                log.info("resParameter:{}",JSON.toJSONString(resParameter));
                UpdateInterfaceVO updateInterfaceVO = new UpdateInterfaceVO();
                BeanUtil.copyProperties(requestBody,updateInterfaceVO);
                updateInterfaceVO.setId(id);
                updateInterfaceVO.setReq_body_other(JSON.toJSONString(reqParameter));
                updateInterfaceVO.setRes_body(JSON.toJSONString(resParameter));
                log.info("updateInterfaceVO:{}",JSON.toJSONString(updateInterfaceVO));
                String result = HttpUtil.createPost(ApiConstant.BASE_URL+ApiConstant.UPDATE_INTERFACE).header("Cookie",ApiConstant.TOKEN).body(JSON.toJSONString(updateInterfaceVO,getFilter())).timeout(3000).execute().body();
                log.info(JSON.toJSONString(result));
                JSONObject json = JSONObject.parseObject(result);
                if(!json.getInteger("errcode").equals(0)){
                    throw new Exception("系统错误，稍后再试！");
                }
            }catch (Exception e){
                deleteApi(id);
                throw new Exception("系统错误，稍后再试！");
            }
        }
    }

    private static PropertyFilter getFilter() {
        PropertyFilter filter = (source, key, value) -> {
            if(value == null){
                return false;
            }
            if(value instanceof List && ((List)value).size() == 0){
                return false;
            }
            if(value instanceof Set && ((Set)value).size() == 0){
                return false;
            }
            if(value instanceof String && StringUtils.isBlank((String)value)){
                return false;
            }
            return true;
        };
        return filter;
    }

    private static void deleteApi(Integer id) {
        String result = HttpUtil.createPost(ApiConstant.BASE_URL+ApiConstant.DELETE_INTERFACE).header("Cookie",ApiConstant.TOKEN).body(JSON.toJSONString(MapUtil.builder().put("id",id))).timeout(3000).execute().body();
        log.info("删除API(id="+id+"):{}",result);
    }

    public void formatRequestParameter(ActionEvent event) {
        String newValue = requestBody.getText();
        String str = "";
        try {
            if(StringUtils.isNoneBlank(newValue.trim())){
                JSONObject json = JSONObject.parseObject(newValue);
                str = JSON.toJSONString(json, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue);
            }
            requestBody.setText(str);
        }catch (Exception e){

        }
    }

    public void formatResponseParameter(ActionEvent event) {
        String newValue = responseBody.getText();
        String str = "";
        try {
            if(StringUtils.isNoneBlank(newValue.trim())){
                JSONObject json = JSONObject.parseObject(newValue);
                str = JSON.toJSONString(json, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue);
            }
            responseBody.setText(str);
        }catch (Exception e){

        }
    }
}
