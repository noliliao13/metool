package com.metool.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.metool.constant.ApiConstant;
import com.metool.constant.TypeEnum;
import com.metool.entity.*;
import com.metool.entity.dto.InterfaceDTO;
import com.metool.entity.view.ReqItem;
import com.metool.exception.CustomException;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Desccription YAPI系统 服务操作类
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/8
 */
@Slf4j
public class YapiService {

    private Map<Integer,IdAndName> catMapId = new HashMap<>();
    private Map<String,IdAndName> catMapName = new HashMap<>();

    public List<IdAndName> getInterfaceCatList(String baseUrl,String token) throws InterruptedException {
        if(StringUtils.isBlank(baseUrl) || StringUtils.isBlank(token)){
            return new ArrayList<>();
        }
        List<IdAndName> list = new ArrayList<>();
        try {
            String result = HttpUtil.get(baseUrl+ ApiConstant.CAT_GET_ALL, MapUtil.of(Pair.of("token",token)),3000);
            JSONObject obj = JSONObject.parseObject(result);
            if(obj.getInteger("errcode") == 0){
                for (Object data : obj.getJSONArray("data")) {
                    JSONObject o = (JSONObject) data;
                    list.add(new IdAndName(o.getInteger("_id"),o.getString("name")));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public void pushApi(String baseUrl, String token, InterfaceDetail interfaceDetail){
        if(interfaceDetail.getCatid() == 0){
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.fail("参数校验失败","请选择接口类型", ToastParameter.builder().timeout(Duration.seconds(2)).build());
            throw CustomException.error("参数校验失败,请选择接口类型！");
        }
        //获取接口
        if(interfaceDetail.getId() != null){
            InterfaceDTO detail = getInterfaceDetail(baseUrl,token,interfaceDetail.getId());
            if(detail == null){
                throw CustomException.error("接口不存在(id="+interfaceDetail.getId()+")");
            }
        }
        try {
            InterfaceParameterBody reqParameter = interfaceDetail.getReqEntity();
            InterfaceParameterBody resParameter = interfaceDetail.getResEntity();
            InterfaceDTO vo = new InterfaceDTO();
            vo.setToken(token);
            BeanUtil.copyProperties(interfaceDetail,vo);
            vo.setStatus(vo.getStatus().equals("已完成") ? "done" : "undone");
            if(interfaceDetail.getIsReqFormData()){
                vo.setReq_body_form(interfaceDetail.getFormDataItems().stream().map(item ->{
                    if(item.getType().equals("MultipartFile")){
                        item.setType("file");
                    }else{
                        item.setType("text");
                    }
                    return item;
                }).collect(Collectors.toList()));
                vo.setReq_headers(interfaceDetail.getRedHeaders());
                vo.setReq_body_type("form");
            }else{
                vo.setReq_headers(Arrays.asList(new ReqHeader("Content-Type","application/json")));
            }
            vo.setReq_body_other(JSON.toJSONString(reqParameter));
            vo.setRes_body(JSON.toJSONString(resParameter));
            String param = JSON.toJSONString(vo,getFilter());
            log.info("param：{}",param);
            String result = HttpUtil.createPost(baseUrl+ApiConstant.INTERFACE_SAVE)
                    .header("Content-Type","application/json")
                    .body(param)
                    .timeout(3000)
                    .execute().body();
            JSONObject json = JSONObject.parseObject(result);
            if(json.getInteger("errcode") != 0){
                throw CustomException.error(json.getString("errmsg"));
            }
        }catch (Exception e){
            throw e;
        }
    }

    private InterfaceDTO getInterfaceDetail(String baseUrl, String token, Integer id) {
        try {
            String str = HttpUtil.get(baseUrl+ApiConstant.INTERFACE_GET,MapUtil.<String,Object>of(Pair.of("token",token),Pair.of("id",id)),3000);
            JSONObject json = JSONObject.parseObject(str);
            if(json.getInteger("errcode") != 0){
                throw CustomException.error(json.getString("errmsg"));
            }
            return json.getObject("data",InterfaceDTO.class);
        }catch (Exception e){
            throw e;
        }

    }

    public List<InterfaceDTO> getAllInterfaceDetail(String baseUrl, String token){
        if(StringUtils.isBlank(baseUrl) || StringUtils.isBlank(token)){
            return new ArrayList<>();
        }
        try {
            Integer pid = getProjectInfo(baseUrl,token).getId();
            String str = HttpUtil.get(baseUrl+ApiConstant.INTERFACE_GET_ALL,MapUtil.<String,Object>of(Pair.of("token",token),
                    Pair.of("project_id",pid),Pair.of("limit",1000)
            ),3000);
            JSONObject json = JSONObject.parseObject(str);
            if(json.getInteger("errcode") != 0){
                throw CustomException.error(json.getString("errmsg"));
            }
            return json.getJSONObject("data").getJSONArray("list").toJavaList(InterfaceDTO.class);
        }catch (Exception e){
            throw e;
        }
    }

    private ProjectInfo getProjectInfo(String baseUrl, String token) {
        try {
            String str = HttpUtil.get(baseUrl+ApiConstant.PROJECT_GET,MapUtil.<String,Object>of(Pair.of("token",token)),3000);
            JSONObject json = JSONObject.parseObject(str);
            if(json.getInteger("errcode") != 0){
                throw CustomException.error(json.getString("errmsg"));
            }
            ProjectInfo result = new ProjectInfo();
            result.setId(json.getJSONObject("data").getInteger("_id"));
            result.setName(json.getJSONObject("data").getString("name"));
            return result;
        }catch (Exception e){
            throw e;
        }
    }

    public TreeItem<ReqItem> createReqRoot(InterfaceDetail entity){
        TreeItem<ReqItem> root = new TreeItem<>(new ReqItem());
        if(entity.getIsReqFormData()){
            List list = entity.getFormDataItems().stream().map(item ->{
                ReqItem r = new ReqItem(item.getName(),item.getType(),item.getRequired(),item.getDefaultValue(),item.getDes());
                return new TreeItem(r);
            }).collect(Collectors.toList());
            root.getChildren().addAll(list);
            root.getValue().setType("form-data");
        }else {
            root = createRoot(entity.getReqEntity());
            if(entity.getIsReqList()){
                root.getValue().setType("array");
            }else{
                root.getValue().setType(entity.getReqEntity().getType());
            }
        }
        return root;
    }

    public TreeItem<ReqItem> createRoot(InterfaceParameterBody entity) {
        TreeItem<ReqItem> result = new TreeItem<>(new ReqItem());
        if(entity == null || StringUtils.isBlank(entity.getType())){
            return result;
        }
        if(entity.getType().equals("array")){
            TreeItem<ReqItem> array = createRoot(entity.getItems());
            result.getChildren().add(array);
        }else if(entity.getType().equals("object")){
            for (Map.Entry<String, InterfaceParameterBody> e : entity.getProperties().entrySet()) {
                InterfaceParameterBody  b = e.getValue();

                TreeItem<ReqItem> i = createRoot(e.getValue().getItems());
                i.getValue().setName(e.getKey());
                i.getValue().setType(e.getValue().getType());
                i.getValue().setRequired(entity.getRequired().contains(e.getKey()) ? "是" : "否");

                if(b.getType().equals("array")){
                    if(TypeEnum.getByJsonType(e.getValue().getItems().getType()) != null){
                        ReqItem ie = new ReqItem();
                        ie.setType(e.getValue().getItems().getType());
                        i.getChildren().add(new TreeItem<>(ie));
                    }else{
                        TreeItem<ReqItem> c = new TreeItem<>(new ReqItem());
                        c.getValue().setType("object");
                        List<TreeItem<ReqItem>> ch = i.getChildren().stream().map(item ->{
                            TreeItem<ReqItem> it = new TreeItem<>(item.getValue());
                            return it;
                        }).collect(Collectors.toList());
                        c.getChildren().addAll(ch);
                        i.getChildren().clear();
                        i.getChildren().add(c);
                    }
                }
                result.getChildren().add(i);
            }
        }else{
            result = new TreeItem<>(new ReqItem("",entity.getType(),"","",entity.getDescription()));
        }
        result.getValue().setType(entity.getType());
        return result;
    }

    public IdAndName getCatById(Integer id,List<IdAndName> cats){
        catMapId.clear();
        for (IdAndName cat : cats) {
            catMapId.put(cat.getId(),cat);
        }
        return catMapId.get(id);
    }

    public IdAndName getCatByName(String name,List<IdAndName> cats){
        catMapId.clear();
        for (IdAndName cat : cats) {
            catMapName.put(cat.getName(),cat);
        }
        return catMapName.get(name);
    }

    private PropertyFilter getFilter() {
        PropertyFilter filter = (source,key,value) -> {
            if(value == null){
                return false;
            }
            if(value instanceof List && ((List)value).size() == 0){
                return false;
            }
            if(value instanceof Set && ((Set)value).size() == 0){
                return false;
            }
            if(value instanceof String && StringUtils.isBlank(value.toString())){
                return false;
            }
            return true;
        };
        return filter;
    }

}
