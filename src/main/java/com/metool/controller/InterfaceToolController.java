package com.metool.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metool.entity.*;
import com.metool.vo.AddInterfaceVO;
import com.metool.constant.ApiConstant;
import com.metool.constant.TypeEnum;
import com.metool.entity.view.InterfaceListCall;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class InterfaceToolController implements Initializable {
    @FXML
    private TextField projectDir;
    @FXML
    private TextField controllerName;
    @FXML
    private TextField dirName;
    @FXML
    private Label scanningCount;
    @FXML
    private ListView interfaceList;
    @FXML
    private TextField yapiBaseUrl;
    @FXML
    private TextField yapiToken;
    @FXML
    private Button btnScanningInterface;
    @FXML
    private ComboBox chooseGroup;
    @FXML
    private ComboBox chooseProject;
    @FXML
    private ComboBox chooseInterfaceType;
    @FXML
    private Button btnPushYapi;
    @FXML
    private HBox batchOperationArea;

    private AddInterfaceVO addInterfaceVO = new AddInterfaceVO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //初始化分组
        initGroup();
        yapiBaseUrl.setText(ApiConstant.BASE_URL);
        yapiToken.setText(ApiConstant.TOKEN);
        yapiBaseUrl.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ApiConstant.BASE_URL = newValue;
            }
        });

        yapiToken.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ApiConstant.TOKEN = newValue;
            }
        });

        projectDir.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(StringUtils.isBlank(newValue)){
                    btnScanningInterface.setVisible(false);
                }else {
                    btnScanningInterface.setVisible(true);
                }
            }
        });

        btnScanningInterface.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                initGroup();
                batchOperationArea.setVisible(true);
                batchOperationArea.setManaged(true);
                List<InterfaceDetail> interfaceDetailList = getAllInterface();
                if (CollectionUtils.isEmpty(interfaceDetailList)) {
                    scanningCount.setText("未扫描到接口");
                    return;
                }
                log.info(JSON.toJSONString(interfaceDetailList));
                scanningCount.setText("共扫描到"+interfaceDetailList.size()+"个接口");

                ObservableList<InterfaceDetail> list = FXCollections.observableArrayList(interfaceDetailList);
                interfaceList.getItems().clear();
                interfaceList.setItems(list);
                interfaceList.setCellFactory(interfaceList -> new InterfaceListCall());
            }
        });
    }

    public void pushToYapi(ActionEvent event) throws IOException {
        Map<String, AddInterfaceVO> map = InterfaceListItemController.addInterfaceVOMap;
        try {
            //校验参数：
            initParameter(map);
        } catch (Exception e) {
            ToastBarToasterService service = new ToastBarToasterService();
            service.initialize();
            service.fail("参数错误",e.getMessage(),ToastParameter.builder().timeout(Duration.seconds(2)).build());
            return;
        }
        List<AddInterfaceVO> successList = new ArrayList<>();
        List<AddInterfaceVO> failList = new ArrayList<>();
        for (Map.Entry<String, AddInterfaceVO> entity : map.entrySet()) {
            AddInterfaceVO addInterfaceVO = entity.getValue();
            if(addInterfaceVO.getIsCheck()){
                try {
                    InterfaceListItemController.pushApi(addInterfaceVO);
                    successList.add(addInterfaceVO);
                } catch (Exception e) {
                   e.printStackTrace();
                    failList.add(addInterfaceVO);
                }
            }
        }
        ToastBarToasterService service = new ToastBarToasterService();
        service.initialize();
        if(failList.size() == 0){
            service.success("保存接口结果详情","恭喜你，全部保存成功("+successList.size()+")",ToastParameter.builder().timeout(Duration.seconds(2)).build());
        }else{
            service.info("保存接口结果详情","成功:"+successList.size()+"个，失败:"+failList.size()+"个",ToastParameter.builder().timeout(Duration.seconds(2)).build());
        }
    }

    private void initParameter(Map<String, AddInterfaceVO> map) throws Exception {
        for (Map.Entry<String, AddInterfaceVO> entity : map.entrySet()) {
            AddInterfaceVO v = entity.getValue();
            if(v.getIsCheck()){
                if(v.getProject_id().equals(0)){
                    if(addInterfaceVO.getProject_id().equals(0)){
                        throw new Exception("请正确选择项目");
                    }
                    v.setProject_id(addInterfaceVO.getProject_id());
                }
                if(v.getCatid().equals(0)){
                    if(addInterfaceVO.getCatid().equals(0)){
                        throw new Exception("请正确选择接口分类");
                    }
                    v.setCatid(addInterfaceVO.getCatid());
                }
                if(StringUtils.isBlank(v.getTitle())){
                    throw new Exception("请正确填写接口标题");
                }
            }
        }
    }

    private void initGroup() {
        boolean isVisible = StringUtils.isNoneBlank(ApiConstant.BASE_URL) && StringUtils.isNoneBlank(ApiConstant.TOKEN);
        if(isVisible){
            try {
                String groupResult = HttpUtil.createGet(ApiConstant.BASE_URL+ApiConstant.GET_GROUP_URL).header("Cookie",ApiConstant.TOKEN).timeout(3000).execute().body();
                List<IdAndName> groupList = JSONObject.parseObject(groupResult).getJSONArray("data").stream().map(item ->{
                    JSONObject jb = (JSONObject) item;
                    IdAndName idAndName = new IdAndName(jb.getInteger("_id"),jb.getString("group_name"));
                    return idAndName;
                }).collect(Collectors.toList());
                groupList.add(0,new IdAndName(0,"请选择分组"));
                ApiConstant.YAPI_GROUP_LIST.clear();
                ApiConstant.YAPI_GROUP_LIST.addAll(groupList);

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
                                addInterfaceVO.setProject_id(projectId);
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
                                        addInterfaceVO.setCatid(interfaceMenuId);
                                        btnPushYapi.setVisible(true);
                                    }
                                });
                            }
                        });
                    }
                });
            }catch (Exception e){

            }
        }
    }

    public void chooseProjectDir(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(new Stage());
        if(file != null){
            projectDir.setText(file.getPath());
            btnScanningInterface.setVisible(true);
        }
    }

    private List<InterfaceDetail> getAllInterface() {
        List<InterfaceDetail> result = new ArrayList<>();
        try {
            List<String> controllerFiles = getAllControllerFile(projectDir.getText());
            if(StringUtils.isNotBlank(controllerName.getText())){
                String name = controllerName.getText().replace(".java","")+".java";
                controllerFiles = controllerFiles.stream().filter(item -> item.endsWith(name)).collect(Collectors.toList());
            }
            if(StringUtils.isNotBlank(dirName.getText())){
                String name = dirName.getText();
                controllerFiles = controllerFiles.stream().filter(item -> !item.contains(name)).collect(Collectors.toList());
            }
            if(CollectionUtils.isEmpty(controllerFiles)){
                return result;
            }
            for (String path : controllerFiles) {
                List<InterfaceDetail> interfaceDetailList = getInterfaceDetailListByPath(path);
                if(CollectionUtils.isNotEmpty(interfaceDetailList)){
                    result.addAll(interfaceDetailList);
                }
            }
        }catch (IOException ioe){

        }
        return result;
    }

    private List<InterfaceDetail> getInterfaceDetailListByPath(String path) {
        String str = new cn.hutool.core.io.file.FileReader(path).readString();
        String rootPath = getRootPath(str);
        List<MethodDetail> methodList = getMethodList(str);
        if(CollectionUtils.isEmpty(methodList)){
           return null;
        }
        return methodList.stream().map(item ->{
            InterfaceDetail interfaceDetail = new InterfaceDetail();
            interfaceDetail.setMethod(item.getType());
            interfaceDetail.setPath(rootPath+item.getPath());
            interfaceDetail.setController(path.split("main"+"\\"+fileSeparator+"java"+"\\"+fileSeparator)[1].replaceAll("\\"+fileSeparator,".").replace(".java",""));
            ParameterTypeInfo parameterTypeInfo = item.getParameterTypeInfo();
            PropertyDetail entity = new PropertyDetail();
            entity.setIsArray(parameterTypeInfo.getIsReqList());

            if(StringUtils.isNoneBlank(parameterTypeInfo.getReqType())){
                //根据类路径获取请求体对应的所有属性
                entity.setPath(parameterTypeInfo.getReqType());
                InterfaceParameterBody reqEntity = getParameterJson(entity);
                interfaceDetail.setReqEntity(reqEntity);
                interfaceDetail.setIsReqList(parameterTypeInfo.getIsReqList());

                entity.setPath(parameterTypeInfo.getResType());
                InterfaceParameterBody resEntity = getParameterJson(entity);
                interfaceDetail.setResEntity(resEntity);
                interfaceDetail.setIsResList(parameterTypeInfo.getIsResList());

            }
            return interfaceDetail;
        }).collect(Collectors.toList());
    }

    private InterfaceParameterBody getParameterJson(PropertyDetail entity){
        String path = entity.getPath();
        Boolean isList = entity.getIsArray();
        String des = entity.getDescription();
        Boolean isRequired = entity.getIsRequired();

        InterfaceParameterBody result = new InterfaceParameterBody();
        if(StringUtils.isBlank(path) || isList == null){
            return result;
        }

        if(isList){
            result.setType("array");
            result.setItems(new InterfaceParameterBody());
        }else {
            result.setType("object");
            result.setProperties(new HashMap<String,InterfaceParameterBody>());
        }

        if(TypeEnum.getByJavaType(path) != null){
            InterfaceParameterBody ib = new InterfaceParameterBody(TypeEnum.getByJavaType(path).getJsonType());
            ib.setJavaType(path);
            result.setDescription(des);
            if(isRequired){
                result.getRequired().add(entity.getName());
            }
            result.setItems(ib);
            return result;
        }

        if(isList){
            result.getItems().setType("object");
        }

        String absolutePath = path;//getAbsolutePath(path);
        if(StringUtils.isBlank(absolutePath)){
            return result;
        }

        List<PropertyDetail> propertyDetailList = getClassProperty(absolutePath);
        if(CollectionUtils.isEmpty(propertyDetailList)){
            return result;
        }
        for (PropertyDetail propertyDetail : propertyDetailList) {
            if(TypeEnum.getByJavaType(propertyDetail.getType()) == null){
                if(propertyDetail.getIsRequired()){
                    result.getRequired().add(propertyDetail.getName());
                }
                if(propertyDetail.getIsArray()){
                    InterfaceParameterBody t = getParameterJson(propertyDetail);
                    t.setDescription(propertyDetail.getDescription());
                    if(isList){
                        result.getItems().getProperties().put(propertyDetail.getName(),t);
                    }else {
                        result.getProperties().put(propertyDetail.getName(),t);
                    }
                }else{
                    InterfaceParameterBody t = getParameterJson(propertyDetail);
                    t.setDescription(propertyDetail.getDescription());
                    if(isList){
                        result.getItems().getProperties().put(propertyDetail.getName(),t);
                    }else {
                        result.getProperties().put(propertyDetail.getName(),getParameterJson(propertyDetail));
                    }
                }
            }else{
                InterfaceParameterBody rb = new InterfaceParameterBody();
                rb.setJavaType(propertyDetail.getType());
                rb.setType(TypeEnum.getByJavaType(propertyDetail.getType()).getJsonType());
                rb.setDescription(propertyDetail.getDescription());
                if(propertyDetail.getIsRequired()){
                    result.getRequired().add(propertyDetail.getName());
                }
                if(isList){
                    result.getItems().getProperties().put(propertyDetail.getName(),rb);
                }else {
                    result.getProperties().put(propertyDetail.getName(),rb);
                }
            }
        }
        return result;
    }

    private List<PropertyDetail> getClassProperty(String absolutePath) {
        List<PropertyDetail> result = new ArrayList<>();
        String str = new cn.hutool.core.io.file.FileReader(absolutePath).readString();
        String className = absolutePath.split("\\"+fileSeparator)[absolutePath.split("\\"+fileSeparator).length-1].split("\\.")[0];

        //扫描父类
        Pattern rp =  Pattern.compile(className+"\\s+extends\\s+\\w+");
        Matcher mp = rp.matcher(str);
        if(mp.find()){
            String parentPath = mp.group();
            parentPath = parentPath.split("\\s+")[parentPath.split("\\s+").length-1];
            String parentPathAbs = getTypePath(str,parentPath);;
            List<PropertyDetail> list = getClassProperty(parentPathAbs);
            if(CollectionUtils.isNotEmpty(list)){
                result.addAll(list);
            }
        }

        //扫描每个属性，必须是标准java bean，属性用private修饰
        Pattern r = Pattern.compile("private\\s+(\\w|\\<|\\>)+\\s+\\w+?;");
        Matcher m = r.matcher(str);
        while(m.find()){
            String temp = m.group().trim();
            String [] array = temp.substring(0,temp.length()-1).split("\\s");
            PropertyDetail propertyDetail = new PropertyDetail();
            propertyDetail.setType(array[1]);
            propertyDetail.setName(array[2]);
            if(TypeEnum.getByJavaType(propertyDetail.getType()) == null){
                String path = getTypePath(str,propertyDetail.getType().replaceAll("List|Set|\\<|\\>|\\s+",""));
                propertyDetail.setPath(path);
            }
            result.add(propertyDetail);
        }
        //判断非空校验及des
        for (PropertyDetail propertyDetail : result) {
            String s = remoreRemake(str);
            String rs = "(@NotNull|@NotBlank)+(message|\\s|=|\\)|\\(|(\".*?\"))+private\\s+(\\w|\\<|\\>)+\\s+"+propertyDetail.getName()+";";
            Matcher mn = Pattern.compile(rs).matcher(s);
            if(mn.find()){
                propertyDetail.setIsRequired(true);
                String s2 = mn.group();
                String des = getDes(s2);
                propertyDetail.setDescription(des.replaceAll("\"",""));
            }
        }
        return result;
    }

    private String getDes(String str) {
        Matcher ms = Pattern.compile("(message|\\s|=|\\)|\\(|(\".*?\"))+").matcher(str);
        if(ms.find()){
            String ss = ms.group();
            Matcher ms2 = Pattern.compile("\".*?\"").matcher(ss);
            if(ms2.find()){
                return ms2.group();
            }
        }
        return "";
    }

    String fileSeparator = System.getProperty("file.separator");
    private String getAbsolutePath(String parameterClassPath) {
        String root = projectDir.getText();
        String className = parameterClassPath.split("\\.")[parameterClassPath.split("\\.").length-1];
        String firstName = parameterClassPath.split("\\.")[0];

        String path = "";
        String cPath = parameterClassPath.replaceAll("\\.","\\"+fileSeparator);
        if(root.contains(firstName)
                && (root.endsWith(firstName) || root.charAt(root.indexOf(firstName)+firstName.length()) == '\\')){
            path = root.substring(0,root.indexOf(firstName))+cPath;
        }else{
            File file = FileUtil.loopFiles(root, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String temp = cPath.replace(fileSeparator+className,"");
                    temp = StringUtils.join(temp,fileSeparator,className,".java");
                    return pathname.getPath().contains(temp);
                }
            }).get(0);
            path = file.getPath();
        }
        return path;
    }


    private List<MethodDetail> getMethodList(String str) {
        List<MethodDetail> result = new ArrayList<>();
        str = str.replaceAll("//.*","");
        str = str.replaceAll("\\s|\t|\r|\n"," ");
        Pattern r = Pattern.compile("(@GetMapping|@PostMapping).+?public.+?\\{");
        Matcher m = r.matcher(str);
        String temp = null;
        while (m.find()){
            temp = m.group().replaceAll("\\s+"," ");
            String method = "";
            if(temp.startsWith("@Get")){
                method = "GET";
            }
            if(temp.startsWith("@Post")){
                method = "POST";
            }
            String path = temp.split(" ")[0].replaceAll("@GetMapping|@PostMapping|\"|\\(|\\)","");

            ParameterTypeInfo parameterType = getParameterType(str,temp);

            MethodDetail md = new MethodDetail();
            md.setPath(path);
            md.setType(method);
            md.setParameterTypeInfo(parameterType);
            result.add(md);
        }
        return result;
    }

    private ParameterTypeInfo getParameterType(String s,String str) {
        ParameterTypeInfo result = new ParameterTypeInfo();
        String reqType = "";
        String resType = "";
        if(StringUtils.isBlank(str)){
            return result;
        }
        Pattern r = Pattern.compile("public.+?\\)");
        Matcher m = r.matcher(str);
        if(m.find()){
            reqType= m.group();
            if(reqType.replaceAll("\\s+","").contains("()")){
                return result;
            }
            String[] array = reqType.replaceAll("\\s+"," ").split(" ");

            resType = array[1].equals("void") ? "" : array[1];
            if(array[array.length-1].trim().length() > 1){
                reqType = array[array.length-2];
            }else {
                reqType = array[array.length-3];
            }
        }
        if(reqType.split("\\(").length == 2){
            reqType = reqType.split("\\(")[1];
        }
        if(StringUtils.isNotBlank(reqType)){
            if(reqType.startsWith("List") || reqType.startsWith("Set")){
                result.setIsReqList(true);
            }
            reqType = reqType.replaceAll("List|Set|\\<|\\>","");
            if(TypeEnum.getByJavaType(reqType) == null){
                /*Pattern rp = Pattern.compile("(\\w+\\.)+"+reqType);
                Matcher mp = rp.matcher(s);
                if(mp.find()){
                    reqType = mp.group();
                }else{
                   Pattern rp2 = Pattern.compile("package.+?;");
                    Matcher mp2 = rp2.matcher(s);
                    if(mp2.find()){
                        String temp  = mp2.group().split("\\s+")[1];
                        reqType = temp.substring(0,reqType.length()-1)+"."+reqType;
                    }

                }*/
                reqType = getTypePath(s,reqType);
            }
        }

        if(StringUtils.isNotBlank(resType)){
            if(resType.startsWith("List") || resType.startsWith("Set")){
                result.setIsResList(true);
            }
            resType = resType.replaceAll("List|Set|\\<|\\>","");

            if(TypeEnum.getByJavaType(resType) == null){
                /*Pattern rp = Pattern.compile("(\\w+\\.)+"+resType);
                Matcher mp = rp.matcher(s);
                if(mp.find()){
                    resType = mp.group();
                }else{
                    Pattern rp2 = Pattern.compile("package.+?;");
                    Matcher mp2 = rp2.matcher(s);
                    if(mp2.find()){
                        String temp  = mp2.group().split("\\s+")[1];
                        resType = temp.substring(0,resType.length()-1)+"."+resType;
                    }
                }*/
                resType = getTypePath(s,resType);
            }

        }
        result.setReqType(reqType);
        result.setResType(resType);
        return result;
    }

    private String getTypePath(String s,String reqType) {
        String result = "";
        //1 找出同名的所有文件
        List<String> files = findFilesByName(projectDir.getText(),reqType+".java");
        s:for (String file : files) {
            //2 匹配 impoer xxx.*
            Matcher mi = Pattern.compile("import\\s+((\\w|\\d)|\\.)+?\\.\\*\\s*;").matcher(s);
            String st = file.replaceAll("\\"+fileSeparator,"\\.");
            while (mi.find()){
                String mis = mi.group().replaceAll("import|\\s|;|\\*","");
                if(st.contains(mis)){
                    result = file;
                    break s;
                }
            }
            //3 匹配 当前包
            Pattern rp2 = Pattern.compile("package.+?;");
            Matcher mp2 = rp2.matcher(s);
            if(mp2.find()){
                String temp  = mp2.group().split("\\s+")[1];
                temp = temp.substring(0,reqType.length()-1);
                if(st.contains(temp)){
                    result = file;
                    break s;
                }
            }
        }
        return result;
    }

    private List<String> findFilesByName(String root,String name) {
        File parent = new File(root);
        if(parent.isDirectory()){
            return FileUtil.loopFiles(root, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String s =  pathname.getName();
                    return s.endsWith(name);
                }
            }).stream().map(item -> item.getPath()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        String s = "import com.test.component.KafkaProducer;" +
                "import com.test.entity.* ;" +
                "import com.test.entity2.*;" +
                "import org.springframework.beans.factory.annotation.Autowired;" +
                "import org.springframework.web.bind.annotation.PostMapping;" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n";
        Matcher m = Pattern.compile("import\\s+((\\w|\\d)|\\.)+?\\.\\*\\s*;").matcher(s);
        while (m.find()){
            System.out.println(m.group());
        }
    }
    private String getRootPath(String str) {
        if(StringUtils.isBlank(str)){
            return "";
        }
        Pattern r = Pattern.compile("@RequestMapping.*");
        Matcher m = r.matcher(str);
        if(m.find()){
            String temp = m.group();
            return temp = temp.replaceAll("@RequestMapping|\\(|\\)|\"","").trim();
        }
        return "";
    }

    private static String remoreRemake(String str) {
        //去除单行注释
        String d = "//[^\r\n]*+";
        //去除多行注释
        String d2 = "/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
        return str = str.replaceAll(d,"").replaceAll(d2,"");
    }

    private List<String> getAllControllerFile(String path) throws IOException {
        File parent = new File(path);
        List<String> files = new ArrayList<>();
        if(parent.isDirectory()){
            File[] children = parent.listFiles();
            for (File file : children) {
                if(file.isDirectory()){
                    files.addAll(getAllControllerFile(file.getPath()));
                }else if(file.getName().toLowerCase().endsWith(".java")){
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String str = null;
                    while ((str = br.readLine()) != null){
                        if(str.trim().toLowerCase().startsWith("//") || str.trim().toLowerCase().startsWith("/*")){
                            continue;
                        }
                        if(str.contains("@Controller") || str.contains("@RestController")){
                            files.add(file.getPath());
                            break;
                        }
                    }
                }
            }
        }else{
            files.add(path);
        }
        return files;
    }
}