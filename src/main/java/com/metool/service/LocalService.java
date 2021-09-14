package com.metool.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metool.constant.RegexConstant;
import com.metool.constant.SystemConstant;
import com.metool.constant.TypeEnum;
import com.metool.entity.*;
import com.metool.entity.view.MenuEntity;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Desccription
 * @Author Nilo
 * @Version 1.0.0
 * @Since 1.0
 * Date 2021/9/8
 */
@Data
@Slf4j
public class LocalService {
    private volatile Boolean isFlag = false;
    private volatile Boolean isEnd = false;
    private Map<String,String> filePath = new HashMap<>();
    private Map<String, InterfaceParameterBody> cacheInterfaceParameterBodyMap = new HashMap<>();
    private Map<String, List<PropertyDetail>> cacheClassFiledMap = new HashMap<>();
    private Set<String> nbl = new HashSet<>();
    private String projectDir;
    private String dirName;

    public void init(){
        init(projectDir,dirName);
    }

    public void init(String projectDir, String dirName) {
        this.projectDir = projectDir;
        this.dirName = dirName;
        this.isEnd = false;
        this.isFlag = false;
        this.nbl.clear();
        this.cacheClassFiledMap.clear();
        this.cacheInterfaceParameterBodyMap.clear();
    }

    /**
     * 获取所有的controller文件
     * @param root 根目录
     * @param rejectDir 忽略的目录
     * @param controllers 包含的文件，多个用逗号分割
     * @param rejectControllers 不包含的文件，多个用逗号分割
     * @return controller文件路径列表
     * @throws Exception
     */
    public List<String> getControllerFiles (String root,String rejectDir,String controllers,String rejectControllers) throws Exception {
        List<String> files = new ArrayList<>();

        if(StringUtils.isNotBlank(rejectDir) && Arrays.asList(rejectDir.split(",")).contains(root)){
            return files;
        }

        File parent = new File(root);
        if(parent.isDirectory()){
            File[] children = parent.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(!pathname.getName().toLowerCase().endsWith(".java") && !pathname.isDirectory()){
                        return false;
                    }
                    if(StringUtils.isNotBlank(rejectDir) && Arrays.asList(rejectDir.split(",")).contains(pathname.getPath())){
                        return false;
                    }
                    if(StringUtils.isNotBlank(controllers)){
                        if(!pathname.isDirectory() && !Arrays.asList(controllers.split(",")).contains(pathname.getName().split("\\.")[0])){
                            return false;
                        }
                    }
                    if(StringUtils.isNotBlank(rejectControllers)){
                        if(!pathname.isDirectory() && Arrays.asList(rejectControllers.split(",")).contains(pathname.getName().split("\\.")[0])){
                            return false;
                        }
                    }
                    return true;
                }
            });
            for (File file : children) {
                if(file.isDirectory()){
                    files.addAll(getControllerFiles(file.getPath(),rejectDir,controllers,rejectControllers));
                }else{
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String str = null;
                    Pattern p = Pattern.compile("^(@Controller|@RestController)(\\s|[\r\n])+");
                    while ((str = br.readLine())!= null){
                        if(str.trim().toLowerCase().startsWith("//") || str.trim().toLowerCase().startsWith("/*") || StringUtils.isBlank(str.trim())){
                            continue;
                        }
                        if(str.equals("@Controller") || str.equals("@RestController") || p.matcher(str).find()){
                            files.add(file.getPath());
                            break;
                        }
                    }
                }
            }
        }
        return files;
    }

    public List<InterfaceDetail> getAllInterface(List<String> controllerFiles, List<List<Node>> gpItems, ScrollPane sp){
        List<InterfaceDetail> result = new ArrayList<>();
        try {
            if(CollectionUtil.isEmpty(controllerFiles)){
                return result;
            }
            Integer totalController = controllerFiles.size();
            for (int i = 0; i < controllerFiles.size(); i++) {
                if(isFlag){
                    log.info("任务被取消");
                    isEnd = true;
                    break;
                }
                String path = controllerFiles.get(i);
                String className = path.split("\\"+ SystemConstant.FILE_SEPARATOR)[path.split("\\"+ SystemConstant.FILE_SEPARATOR).length-1].split("\\.")[0];
                int finalI = i;
                Platform.runLater(() ->{
                    if(StringUtils.isNotBlank(className)){
                        Label l = (Label) gpItems.get(finalI).get(2);
                        l.setText("处理中 ...");
                        l.setTextFill(Color.RED);
                        double xy = new BigDecimal(finalI).divide(new BigDecimal(totalController),4,BigDecimal.ROUND_DOWN).doubleValue();
                        xy += new BigDecimal(0.2).divide(new BigDecimal(totalController),4,BigDecimal.ROUND_DOWN).doubleValue();
                        sp.setVvalue(xy);
                    }
                });
                List<InterfaceDetail> interfaceDetailList = getInterfaceDetailListByPath(path);
                if(isFlag){
                    continue;
                }
                result.addAll(interfaceDetailList);
                log.info("本次发现{}个 / 累计发现{}个 , {}",interfaceDetailList.size(),result.size(),className);
                Platform.runLater( () ->{
                    if(interfaceDetailList != null && gpItems.size() > 0){
                        Label l = (Label) gpItems.get(finalI).get(2);
                        l.setText(interfaceDetailList.size()+"个接口");
                        l.setTextFill(Color.BLUE);
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            // todo toast 错误提示

        }

        return result;
    }

    private List<InterfaceDetail> getInterfaceDetailListByPath(String path) {
        List<InterfaceDetail> result = new ArrayList<>();
        String str = new cn.hutool.core.io.file.FileReader(path).readString();
        String rootPath = getRootPath(str);
        rootPath = rootPath.startsWith("/") ? rootPath : "/" + rootPath;
        List<MethodDetail> methodDetails = getMethodList(rootPath,str);
        if(CollectionUtil.isEmpty(methodDetails)){
            return result;
        }
        for (MethodDetail item : methodDetails) {
            if(isFlag){
                break;
            }
            InterfaceDetail interfaceDetail = new InterfaceDetail();
            interfaceDetail.setTitle(item.getTitle());
            interfaceDetail.setMethod(item.getType());
            interfaceDetail.setPath((rootPath +"/"+item.getPath()).replaceAll("//","/"));

//            String controller = path.split("main"+"\\"+SystemConstant.FILE_SEPARATOR+"java"+"\\"+SystemConstant.FILE_SEPARATOR)[1]
//                    .replaceAll("\\"+SystemConstant.FILE_SEPARATOR,".").replace(".java","");
            String controller = path.split("\\"+ SystemConstant.FILE_SEPARATOR)[path.split("\\"+ SystemConstant.FILE_SEPARATOR).length-1].split("\\.")[0];
            interfaceDetail.setController(controller);

            ParameterTypeInfo parameterTypeInfo = item.getParameterTypeInfo();
            if(parameterTypeInfo.getIsReqFormData()){
                interfaceDetail.setIsReqFormData(true);
                interfaceDetail.setRedHeaders(Arrays.asList(new ReqHeader("Content-Type","multipart/form-data")));
                interfaceDetail.setFormDataItems(parameterTypeInfo.getParameters());
            }
            PropertyDetail entity = new PropertyDetail();
            entity.setIsArray(parameterTypeInfo.getIsReqList());
            String key = "/" + interfaceDetail.getMethod()+"/"+interfaceDetail.getPath();
            key = key.replaceAll("//","/");
            entity.setKey(key);
            if(StringUtils.isNotBlank(parameterTypeInfo.getReqType())){
                entity.setType(parameterTypeInfo.getReqType());
                InterfaceParameterBody reqEntity = getParameterJson(entity,parameterTypeInfo);
                interfaceDetail.setReqEntity(reqEntity);
                interfaceDetail.setIsReqList(parameterTypeInfo.getIsReqList());
            }
            if(StringUtils.isNotBlank(parameterTypeInfo.getResType())){
                entity.setType(parameterTypeInfo.getResType());
                InterfaceParameterBody resEntity = getParameterJson(entity,parameterTypeInfo);
                interfaceDetail.setResEntity(resEntity);
                interfaceDetail.setIsResList(parameterTypeInfo.getIsResList());
            }
            result.add(interfaceDetail);
        }
        return result;
    }

    private InterfaceParameterBody getParameterJson(PropertyDetail entity, ParameterTypeInfo parameterTypeInfo) {
        InterfaceParameterBody result = cacheInterfaceParameterBodyMap.get(entity.getKey()+":"+entity.getType());
        if(result != null){
            return result;
        }
        result = getParameterJson_(entity,parameterTypeInfo,true);
        cacheInterfaceParameterBodyMap.put(entity.getKey()+":"+entity.getType(),result);
        return result;
    }

    private InterfaceParameterBody getParameterJson_(PropertyDetail entity, ParameterTypeInfo parameterTypeInfo, boolean isValid) {
        InterfaceParameterBody result = cacheInterfaceParameterBodyMap.get(entity.getKey()+":"+entity.getType());
        if(result != null){
            return result;
        }else {
            result = new InterfaceParameterBody();
        }
        String type = entity.getType();
        Boolean isList = Pattern.compile(RegexConstant.LIST).matcher(type.split("-")[0]).find();
        String des = entity.getDescription();
        Boolean isRequired = entity.getIsRequired();

        if(TypeEnum.getByJavaType(type) != null){
            result.setJavaType(type);
            result.setType(TypeEnum.getByJavaType(type).getJavaType());
            result.setDescription(des);
            if(isRequired){
                result.getRequired().add(entity.getName());
            }
            return result;
        }

        if(isList){
            result.setType("array");
            PropertyDetail entity_ = new PropertyDetail();
            entity_.setKey(entity.getKey());
            entity_.setType(type.replaceAll("^(\\w*(List|Set)+-)",""));
            InterfaceParameterBody items = new InterfaceParameterBody();
            String s = entity_.getType().replaceAll(RegexConstant.LIST,"");
            if (StringUtils.isNotBlank(s) && !s.equals("<T>") && !s.equals("T")){
                items = getParameterJson_(entity_,parameterTypeInfo,isValid);
            }
            result.setItems(items);
        }else {
            result.setType("object");
            result.setProperties(new HashMap<>());
            String[] types = type.split("-");
            if(types.length == 1){
                String path = entity.getPath();
                if(StringUtils.isBlank(path)){
                    path = filePath.get(entity.getKey()+":"+entity.getType());
                }
                if(StringUtils.isBlank(path) && entity.getType().equals("PageInfo")){
                    path = this.getClass().getResource("/data/PageInfo.java").getPath().substring(1);
                    path = path.replaceAll("/","\\\\");
                    filePath.put(entity.getKey()+":"+entity.getType(),path);
                }
                if (StringUtils.isBlank(path)){
                    return result;
                }

                List<PropertyDetail> propertyDetails = getClassProperty(entity.getKey(),path,entity.getType(),parameterTypeInfo,isValid);
                if(CollectionUtil.isEmpty(propertyDetails)){
                    return result;
                }
                for (PropertyDetail propertyDetail : propertyDetails) {
                    if(TypeEnum.getByJavaType(propertyDetail.getType()) != null){
                        if(propertyDetail.getIsRequired() && isValid){
                            result.getRequired().add(propertyDetail.getName());
                        }
                        InterfaceParameterBody rb = new InterfaceParameterBody();
                        rb.setJavaType(propertyDetail.getType());
                        rb.setType(TypeEnum.getByJavaType(propertyDetail.getType()).getJsonType());
                        rb.setDescription(propertyDetail.getDescription());
                        if(isList){
                            result.getItems().getProperties().put(propertyDetail.getName(),rb);
                        }else{
                            result.getProperties().put(propertyDetail.getName(),rb);
                        }
                    }else if(isMap(propertyDetail.getType())){
                        boolean isArray =  Pattern.compile(RegexConstant.LIST).matcher(propertyDetail.getType().split("-")[0]).find();
                        InterfaceParameterBody interfaceParameterBody = new InterfaceParameterBody();
                        if(isArray){
                            interfaceParameterBody.setType("array");
                            interfaceParameterBody.setItems(new InterfaceParameterBody());
                        }else {
                            interfaceParameterBody.setType("object");
                        }
                        result.getProperties().put(propertyDetail.getName(),interfaceParameterBody);
                        if(propertyDetail.getIsRequired() && isValid){
                            result.getRequired().add(propertyDetail.getName());
                        }
                    }else if(isSelf(path,propertyDetail)){
                        boolean isArray =  Pattern.compile(RegexConstant.LIST).matcher(propertyDetail.getType().split("-")[0]).find();
                        InterfaceParameterBody interfaceParameterBody = new InterfaceParameterBody();
                        interfaceParameterBody.setType("object");
                        interfaceParameterBody.setJavaType(propertyDetail.getType());
                        if(isArray){
                            InterfaceParameterBody items = new InterfaceParameterBody();
                            items.setType("array");
                            items.setItems(interfaceParameterBody);
                            result.getProperties().put(propertyDetail.getName(),items);
                        }else {
                            result.getProperties().put(propertyDetail.getName(),interfaceParameterBody);
                        }

                        if(propertyDetail.getIsRequired() && isValid){
                            result.getRequired().add(propertyDetail.getName());
                        }
                    }else{
                        propertyDetail.setKey(entity.getKey());
                        InterfaceParameterBody b = getParameterJson_(propertyDetail,parameterTypeInfo,propertyDetail.getIsValid());
                        if(propertyDetail.getIsRequired()){
                            result.getRequired().add(propertyDetail.getName());
                        }
                        result.getProperties().put(propertyDetail.getName(),b);
                    }
                }
            }else{
                InterfaceParameterBody ib = null;
                if(types.length == 2){
                    PropertyDetail e = new PropertyDetail();
                    e.setKey(entity.getKey());
                    e.setType(types[1]);
                    ib = getParameterJson_(e,parameterTypeInfo,isValid);
                }else if(types.length == 3 && Pattern.compile(RegexConstant.LIST).matcher(types[1]).find()){
                    PropertyDetail e = new PropertyDetail();
                    e.setKey(entity.getKey());
                    e.setType(types[2]);
                    InterfaceParameterBody b = new InterfaceParameterBody();
                    b.setType("array");
                    b.setItems(getParameterJson_(e,parameterTypeInfo,isValid));
                    ib = b;
                }else{
                    for (int i = types.length-1; i >1 ; i--) {
                        PropertyDetail e = new PropertyDetail();
                        e.setKey(entity.getKey());
                        e.setType(types[i]);
                        InterfaceParameterBody b = getParameterJson_(e,parameterTypeInfo,isValid);
                        String ss = types[i-1];
                        int f = 0;
                        if(i >=types.length-2 && Pattern.compile(RegexConstant.LIST).matcher(ss).find()){
                            f = i-2;
                        }else {
                            f = i-1;
                        }
                        e.setType(types[f]);
                        InterfaceParameterBody bp = getParameterJson_(e,parameterTypeInfo,isValid);
                        String path = filePath.get(entity.getKey()+":"+types[f]);
                        List<String> lm = getTName(path);
                        for (Map.Entry<String, InterfaceParameterBody> eb : bp.getProperties().entrySet()) {
                            if(lm.contains(eb.getKey())){
                                if(eb.getValue().getType().equals("array") || Pattern.compile(RegexConstant.LIST).matcher(ss).find()){
                                    InterfaceParameterBody p = new InterfaceParameterBody();
                                    p.setType("array");
                                    p.setItems(b);
                                    eb.setValue(p);
                                }else{
                                    eb.setValue(b);
                                }
                            }
                        }

                        if(f == 0){
                            ib = bp;
                            break;
                        }
                        if(f== types.length -1 && Pattern.compile(RegexConstant.LIST).matcher(types[f-1]).find()){
                            InterfaceParameterBody p = new InterfaceParameterBody();
                            p.setType("array");
                            p.setItems(bp);
                            ib = p;
                            break;
                        }
                        ib = bp;
                    }
                }

                if(Pattern.compile(RegexConstant.LIST).matcher(types[0]).find()){
                    result = ib;
                }else{
                    String path = filePath.get(entity.getKey()+":"+types[0]);
                    List<PropertyDetail> propertyDetails = getClassProperty(entity.getKey(),path,entity.getType(),parameterTypeInfo,isValid);
                    for (PropertyDetail propertyDetail : propertyDetails) {
                        if(TypeEnum.getByJavaType(propertyDetail.getType()) != null){
                            InterfaceParameterBody rb = new InterfaceParameterBody();
                            rb.setJavaType(propertyDetail.getType());
                            rb.setType(TypeEnum.getByJavaType(propertyDetail.getType()).getJsonType());
                            rb.setDescription(propertyDetail.getDescription());
                            if(propertyDetail.getIsRequired()){
                                result.getRequired().add(propertyDetail.getName());
                            }
                            if(isList){
                                result.getItems().getProperties().put(propertyDetail.getName(),rb);
                            }else{
                                result.getProperties().put(propertyDetail.getName(),rb);
                            }
                        }else if(propertyDetail.getType().equals("T")){
                            if(isList){
                                result.getItems().getProperties().put(propertyDetail.getName(),ib);
                            }else{
                                result.getProperties().put(propertyDetail.getName(),ib);
                            }
                        }else if(Pattern.compile(RegexConstant.LIST_T).matcher(propertyDetail.getType()).find()){
                            InterfaceParameterBody items = new InterfaceParameterBody();
                            items.setType("array");
                            items.setItems(ib);
                            result.getProperties().put(propertyDetail.getName(),items);
                        }else if(Pattern.compile(RegexConstant.LIST).matcher(propertyDetail.getType()).find()){
                            InterfaceParameterBody items = new InterfaceParameterBody();
                            items.setType("array");
                            items.setItems(getParameterJson_(propertyDetail,parameterTypeInfo,propertyDetail.getIsValid()));
                            propertyDetail.setKey(entity.getKey());
                            result.getProperties().put(propertyDetail.getName(),items);
                        }else{
                            propertyDetail.setKey(entity.getKey());
                            result.getProperties().put(propertyDetail.getName(),getParameterJson_(propertyDetail,parameterTypeInfo,propertyDetail.getIsValid()));
                        }
                    }
                }

            }
        }
        return result;
    }

    private List<PropertyDetail> getClassProperty(String key, String path, String type, ParameterTypeInfo parameterTypeInfo, boolean isValid) {
        List<PropertyDetail> result = cacheClassFiledMap.get(key+path+type);
        if(result != null){
            return result;
        }
        result = getClassProperty_(key,path,type,parameterTypeInfo,isValid);
        cacheClassFiledMap.put(key+path+type,result);
        return result;
    }

    private List<PropertyDetail> getClassProperty_(String key, String path, String type, ParameterTypeInfo parameterTypeInfo, boolean isValid) {
        List<PropertyDetail> result = cacheClassFiledMap.get(key+path+type);
        if(result != null){
            return result;
        }else {
            result = new ArrayList<>();
        }
        if(StringUtils.isBlank(path)){
            log.info("type:{}, 的path为空",type);
            return result;
        }

        String oldStr = new cn.hutool.core.io.file.FileReader(path).readString();
        String str = oldStr;
        String className = path.split("\\"+ SystemConstant.FILE_SEPARATOR)[path.split("\\"+ SystemConstant.FILE_SEPARATOR).length-1].split("\\.")[0];

        if(StringUtils.isNotBlank(type) && nbl.contains(type)){
            //替换内部类属性
            str = getNblStr(str,className,type);
        }else if(StringUtils.isNotBlank(type) && type.contains(".")){
            //替换内部类属性
            str = getNblStr(str,className,type.split("\\.")[1]);
        }else {
            //删除内部类属性
            str = removeNblInfo(str,className);
        }

        //扫描父类
        Matcher mp = Pattern.compile(className+"\\s+extends\\s+\\w+").matcher(str);
        if(mp.find()){
            String parentPath = mp.group().split("\\s+")[mp.group().split("\\s+").length-1];
            parentPath = getTypePath(str,parentPath);
            List<PropertyDetail> list = getClassProperty(key,parentPath,"",parameterTypeInfo,isValid);
            if(CollectionUtil.isNotEmpty(list)){
                result.addAll(list);
            }
        }

        //扫描每个属性，必须是标准java bean
        String s2 = removeRemake(str);
        Matcher m = Pattern.compile("(?!//)private\\s+(\\w|\\d|\\<|\\>|,|\\s)+\\s+\\w+?(\\s|=|;)+").matcher(s2);
        while (m.find()){
            String temp = m.group().trim();
            if(Pattern.compile("\\s+static").matcher(temp).find()){
                continue;
            }
            PropertyDetail propertyDetail = new PropertyDetail();
            temp = temp.replaceAll("\\s+=","").replaceAll(";","");
            //Map处理
            String t2 = temp.replaceAll("\\s*private\\s+","");
            if(isMap(t2)){
                String [] ss = t2.split("\\>");
                String name_ = ss[ss.length-1].trim();
                propertyDetail.setName(name_);
                if(Pattern.compile("^(List|Set|ArrayList|LinkedList|HashSet)+.*").matcher(t2).find()){
                    propertyDetail.setIsArray(true);
                    propertyDetail.setType("List-Map");
                }else{
                    propertyDetail.setType("Map");
                }
            }else{
                String [] array = temp.split("\\s+");
                propertyDetail.setType(getInType(array[1].split("\\.")[0]));
                propertyDetail.setName(array[2]);
                if(TypeEnum.getByJavaType(propertyDetail.getType()) == null){
                    Matcher mc = Pattern.compile(RegexConstant.LIST).matcher(propertyDetail.getType());
                    if(mc.find()){
                        propertyDetail.setIsArray(true);
                    }
                    String path_ = getTypePath(str,propertyDetail.getType().replaceAll(RegexConstant.LIST,""));
                    propertyDetail.setPath(path_);
                }
            }
            result.add(propertyDetail);
        }

        //引用类型 @Valid的判断
        for (PropertyDetail propertyDetail : result) {
            String s = removeRemake(str);
            String rs = "@Valid(@|\\s|\\(|\\)|=|,|\\.|\\{|\\}|\\w|<|>)+\\s+"+propertyDetail.getName()+"(\\s|=|;)+";
            Matcher mn = Pattern.compile(rs).matcher(s);
            if(mn.find()){
                propertyDetail.setIsValid(true);
            }
        }

        //判断非空校验
        if(parameterTypeInfo.getIsValidated() && isValid){
            for (PropertyDetail propertyDetail : result) {
                String s = removeRemake(str);
                String rs = "(@NotNull|@NotBlank)+(\\s|\\w|@|=|,|\\(|\\)|\\{|\\}|\\.|(\".*\"))+?"+propertyDetail.getName()+"(\\s|=|;|(\".*\")*)+";
                Matcher mn = Pattern.compile(rs).matcher(s);
                if(mn.find()){
                    String s3 = mn.group();
                    Matcher mn2 = Pattern.compile("groups\\s*=(\\w|\\s|\\.|\\{|\\}|,)+").matcher(s3);
                    List<String> fl = new ArrayList<>();
                    if(mn2.find()){
                        String[] s4 = mn2.group().split("=")[1].replaceAll("\\{|\\}|\\.class","").trim().split(",");
                        fl.addAll(Arrays.asList(s4));
                    }
                    if(CollectionUtil.isNotEmpty(parameterTypeInfo.getValidatedList()) && CollectionUtil.isNotEmpty(fl)){
                        for (String s1 : parameterTypeInfo.getValidatedList()) {
                            if(fl.contains(s1)){
                                propertyDetail.setIsRequired(true);
                                break;
                            }
                        }
                    }
                    if(CollectionUtil.isEmpty(parameterTypeInfo.getValidatedList()) && CollectionUtil.isEmpty(fl)){
                        propertyDetail.setIsRequired(true);
                    }
                }
            }
        }

        //获取字段说明
        for (PropertyDetail propertyDetail : result) {
            String des = getFiledDesc(propertyDetail.getName(),str);
            propertyDetail.setDescription(des);
        }

        //初始化非基础类型字段的文件地址
        for (PropertyDetail propertyDetail : result) {
            if(TypeEnum.getByJavaType(propertyDetail.getType()) == null){
                if(isNBL(oldStr,propertyDetail.getType(),className)){
                    for (String name : propertyDetail.getType().split("-")) {
                        nbl.add(name);
                        if(StringUtils.isBlank(filePath.get(key+":"+name))){
                            filePath.put(key+":"+name,path);
                        }
                    }
                }else{
                    findPathAndCache(key,str,propertyDetail.getType());
                }
            }
        }
        return result;
    }

    private String getNblStr(String str, String className, String type) {
        //读取包头信息
        String packageInfo = "";
        Matcher m = Pattern.compile("public\\s+class\\s+"+className+"(\\s|\\w|\\d|<|>)*?\\{").matcher(str);
        if(m.find()){
            packageInfo = str.substring(0,m.start());
        }
        //读取内部类信息
        String classInfo = "";
        Matcher m2 = Pattern.compile("class\\s+"+type+"(\\s|\\w|\\d|<|>)*?\\{").matcher(str);
        StringBuffer body = new StringBuffer();
        while (m2.find()){
            classInfo = m2.group();
            if(!Arrays.asList(classInfo.split("\\{")[0].trim().split("\\s+")).contains(type)){
                continue;
            }
            String temp = str.substring(m2.end(),str.length());
            String[] sa = temp.split("[\r\n]");

            int x = 0;
            int y = 0;
            int t = 0;
            boolean z = true;
            for (String s : sa) {
                body.append("\n");
                Matcher m3 = Pattern.compile("\\{").matcher(s.trim());
                Matcher m4 = Pattern.compile("}").matcher(s.trim());
                String s2 = s.replaceAll("\".*\"","");
                if(Pattern.compile("/\\*").matcher(s2).find()){
                    z = false;
                }
                if(Pattern.compile("\\*/").matcher(s2).find()){
                    z = true;
                }
                if(z){
                    while (m3.find()){
                        x++;
                    }
                    while (m4.find()){
                        y++;
                    }
                }
                if(y > x){
                    String z1 = "";
                    for (int i = 0; i < y-x; i++) {
                        z1 += "}";
                    }
                    s = s.replaceAll("}","");
                    s += z1;
                    body = new StringBuffer(body.substring(0,t));
                    body.append(s);
                    break;
                }
            }
            break;
        }
        return packageInfo+classInfo+body.toString();
    }

    private String removeNblInfo(String str, String className) {
        String result = str;
        String p = "(@(\\w|\\d)+(\\s|[\r\n])+)*class\\s+(?!"+className+")"+"(\\s|\\w|\\d|<|>)*?\\{";
        Matcher m = Pattern.compile(p).matcher(str);
        if(m.find()){
            int start = m.start();
            String temp = str.substring(m.end(),str.length());
            BufferedReader br = new BufferedReader(new StringReader(temp));
            int x = 0;
            int y = 0;
            int t = 0;
            boolean z = true;
            boolean isStart = false;
            StringBuffer se = new StringBuffer();
            String[] ss = temp.split("\r\n");
            for (String s : ss) {
                Matcher m3 = Pattern.compile("\\{").matcher(s.trim());
                Matcher m4 = Pattern.compile("}").matcher(s.trim());
                String s2 = s.replaceAll("\".*\"","");
                if(Pattern.compile("/\\*").matcher(s2).find()){
                    z = false;
                }
                if(Pattern.compile("\\*/").matcher(s2).find()){
                    z = true;
                }
                if(z){
                    while (m3.find()){
                        x++;
                    }
                    while (m4.find()){
                        y++;
                    }
                }
                if(y > x+1){
                    isStart = true;
                }
                if(isStart){
                    se.append("\r\n");
                    se.append(s);
                }
            }
            String sstr = str.substring(0,start);
            result = sstr+se.toString();
        }
        Matcher m2 = Pattern.compile(p).matcher(result);
        if(m2.find()){
            return removeNblInfo(result,className);
        }
        return result;
    }

    private String getFiledDesc(String name, String str) {
        Matcher m = Pattern.compile("((//)+?.*)(\\s|[\r\n])+(@.*?(\\s|[\r\n])+)*private+\\s+(\\w|<|>|\\.)+\\s"+name+"+?(\\s|-|;)+").matcher(str);
        if(m.find()){
            String result = m.group();
            Matcher m1 = Pattern.compile("((//)+?.*)?\\s|[\r\n]").matcher(result);
            if(m1.find()){
                result = m1.group().replaceAll("//","").trim();
            }
            return result;
        }
        return "";
    }

    private String getInType(String str) {
        switch (str){
            case "long": str = "Long";break;
            case "int": str = "Integer";break;
            case "short": str = "Short";break;
            case "byte": str = "Byte";break;
            case "char": str = "Char";break;
            case "double": str = "Double";break;
            case "float": str = "Float";break;
            case "boolean": str = "Boolean";break;
        }
        str = str.trim().replaceAll("<","-").replaceAll(">","").trim();
        return str;
    }


    private boolean isNBL(String str, String type, String className) {
        if(className.equals(type)){
            return false;
        }
        if(type.contains("-")){
            type = type.split("-")[1];
        }
        return Pattern.compile("class\\s+"+type).matcher(str).find();
    }

    private boolean isMap(String type) {
        List array = Arrays.asList(type.split("\\<"));
        return array.contains("Map") || array.contains("HashMap");
    }

    private boolean isSelf(String path,PropertyDetail propertyDetail) {
        String className = path.split("\\"+ SystemConstant.FILE_SEPARATOR)[path.split("\\"+ SystemConstant.FILE_SEPARATOR).length-1].split("\\.")[0];
        String type = propertyDetail.getType().split("-")[propertyDetail.getType().split("-").length-1];
        return className.equals(type);
    }

    private List<String> getTName(String path) {
        List<String> result = new ArrayList<>();
        String str = new cn.hutool.core.io.file.FileReader(path).readString();
        str = removeRemake(str);
        Matcher m = Pattern.compile("private\\s+(\\w|\\d|\\<|\\>)+\\s+\\w+?(\\s|=|;)+").matcher(str);
        while (m.find()){
            String[] ss = m.group().replaceAll("private","").split("=")[0].trim().split("\\s+");
            if(ss[0].equals("T") || Pattern.compile(RegexConstant.LIST_T).matcher(ss[0]).find()){
                result.add(ss[1].replaceAll(";","".trim()));
            }
        }
        return result;
    }

    private List<MethodDetail> getMethodList(String rootPath, String str) {
        List<MethodDetail> result = new ArrayList<>();
        String str_ = removeRemake(str);
        Matcher m = Pattern.compile("(\\s|;|})+(@GetMapping|@PostMapping)+(.|\\s|[\r\n])+?(public.+?(\\)(\\s|\\w)*\\{))").matcher(str_);
        String temp = null;
        while (m.find()){
            if(isFlag){
                break;
            }
            String ts = m.group();
            temp = ts.replaceAll("\\s|;|\\}"," ").trim();
            String method = "";
            if(temp.startsWith("@Get")){
                method = "GET";
            }
            if(temp.startsWith("@Post")){
                method = "POST";
            }
            String path = "";
            Matcher mp = Pattern.compile(".+?public").matcher(str);
            if(mp.find()){
                path = m.group().split("\"")[1].trim();
            }
            String key = "/"+method+"/"+rootPath+"/"+path;
            key = key.replaceAll("//","/");
            ParameterTypeInfo parameterTypeInfo = getParameterTypeInfo(key,str,temp);

            MethodDetail md = new MethodDetail();
            md.setPath(path);
            md.setType(method);
            md.setParameterTypeInfo(parameterTypeInfo);
            //获取接口名称
            String des = getInterfaceDes(md,str);
            md.setTitle(des);
            //判断接口是否废弃
            if(!getInterfaceDelete(md,str)){
                result.add(md);
            }
        }
        return result;
    }

    private boolean getInterfaceDelete(MethodDetail md, String str) {
        str = removeRemake(str);
        String s1 = md.getType().toLowerCase();
        String mapping = s1.substring(0,1).toUpperCase()+s1.substring(1)+"Mapping";
        Matcher m1 = Pattern.compile("@"+mapping+"(\\s|\\(|\")+"+md.getPath()+"(\\s|\\)|\")+(\\s|(@\\w+(\\(.*\\))*))*?@Deprecated").matcher(str);
        if (m1.find()){
            return true;
        }
        Matcher m2 = Pattern.compile("@Deprecated(\\s|\\(|\")"+"(\\s|(@\\w+(\\(.*\\))*))*"+"?@"+mapping+"(\\s|\\(|\")"+md.getPath()+"(\\s|\\)|\")+").matcher(str);
        if (m2.find()){
            return true;
        }
        return false;
    }

    private String getInterfaceDes(MethodDetail md, String str) {
        str = str.replaceAll("//[^\r\n]*+","");
        String s1 = md.getType().toLowerCase();
        String mapping = s1.substring(0,1).toUpperCase()+s1.substring(1)+"Mapping";
        String p = "(/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/)(\\s|[\r\n])(@.*|\\s|[\r\n])*?@"+mapping+"(\\s|\\(|\")+"+md.getPath()+"(\\s|\\)|\")+";
        Matcher m = Pattern.compile(p).matcher(str);
        if(m.find()){
            String st = m.group();
            m = Pattern.compile("@Description?.*").matcher(st);
            if (m.find()){
                st = m.group().replaceAll("@Description|:","").trim();
                return st;
            }
        }
        return "";
    }

    private ParameterTypeInfo getParameterTypeInfo(String key, String str, String type) {
        ParameterTypeInfo result = new ParameterTypeInfo();
        String reqType = "";
        String resType = "";
        if(StringUtils.isBlank(type)){
            return result;
        }
        if(type.contains("@RequestParam")){
            result.setIsReqFormData(true);
            Matcher r = Pattern.compile("@RequestParam\\s*\\((\\w|=|,|\"|\\s)+\\)\\s*\\w+\\s+\\w+").matcher(str);
            while (r.find()){
                String s = r.group();
                String[] array = s.split("\\)");
                String type_ = array[1].trim().split("\\s+")[0];
                String name,des = "",required = "";
                Matcher m1 = Pattern.compile("value\\s*=\\s*\".*\"").matcher(array[0].trim());
                if(m1.find()){
                    name = m1.group().trim().split("=")[1].replaceAll("\"","").trim();
                }else{
                    name = array[0].trim().replaceAll("@RequestParam|\\(|\"|\\s","").trim();
                }
                Matcher md = Pattern.compile("required\\s*=\\s*false").matcher(array[0].trim());
                if(md.find()){
                    required = "否";
                }else{
                    required = "是";
                }
                FormDataItem item = new FormDataItem();
                item.setType(type_);
                item.setName(name);
                item.setDes(des);
                item.setRequired(required);
                result.getParameters().add(item);
            }

            Matcher m = Pattern.compile("public.+?\\)").matcher(type);
            if(m.find()){
                String temp = m.group();
                String[] array = temp.replaceAll("\\s+"," ").replaceAll("HttpServletRequest\\s+(\\w|\\d)+","").split(" ");
                resType = array[1].equals("void") ? "" : array[1];
            }
            if(StringUtils.isNotBlank(resType)){
                resType = resType.replaceAll("\\s+|[\r\n]","").replaceAll("\\<","-").replaceAll("\\>","");
                findPathAndCache(key,str,resType);
            }
            result.setResType(resType);
        }else{
            Matcher m = Pattern.compile("public.+?(\\)(\\s|\\w)*\\{)+").matcher(type);
            if(m.find()){
                String mg = m.group().replaceAll("throws\\s+\\w+","");
                String temp = mg.split("public")[1].trim();
                resType = temp.split("\\s+")[0];
                if(resType.equalsIgnoreCase("void")){
                    resType = "";
                }
                Matcher mt = Pattern.compile("\\(.+?(\\s*\\)\\s*\\{)").matcher(temp);
                if(mt.find()){
                    reqType = mt.group();
                    reqType = reqType.replaceAll("(@\\w+(\\((\\{|\\w|\\.|\\}|\\,|\\s)+\\))*)|((HttpRequest|HttpResponse|HttpServletRequest|HttpServletResponse)+\\s+\\w+)|,|\\(|\\)|\\{","").trim().split("\\s+")[0];
                    Matcher m2 = Pattern.compile("@Validated").matcher(mg);
                    if(m2.find()){
                        result.setIsValidated(true);
                        Matcher m3 = Pattern.compile("@Validated\\s+\\((\\s|\\{|,|\\.|w)+\\.class(\\s|\\})*\\)").matcher(mg);
                        while (m3.find()){
                            String s = m3.group().replaceAll("@Validated|\\.class|\\{|}|\\(|\\)","-");
                            List<String> groupList = new ArrayList<>();
                            for (String s1 : s.split(",")) {
                                groupList.add(s1.replaceAll("-","").trim());
                            }
                            result.setValidatedList(groupList);
                        }
                    }
                }
            }
        }
        if(reqType.split("\\(").length == 2){
            reqType = resType.split("\\(")[1];
        }
        if(StringUtils.isNotBlank(reqType)){
            reqType = reqType.replaceAll("\\s+|[\r\n]","").replaceAll("\\<","-").replaceAll("\\>","");
            findPathAndCache(key,str,reqType);
        }
        if(StringUtils.isNotBlank(resType)){
            resType = resType.replaceAll("\\s+|[\r\n]","").replaceAll("\\<","-").replaceAll("\\>","");
            findPathAndCache(key,str,resType);
        }
        result.setReqType(reqType);
        result.setResType(resType);
        return result;
    }

    private void findPathAndCache(String key, String str, String type) {
        key = key.replaceAll("//","/");
        for (String name : type.split("-")){
            String path = filePath.get(key+":"+name);
            if(StringUtils.isBlank(path)){
                filePath.put(key+":"+name,getTypePath(str,name));
            }
        }
    }

    private String getTypePath(String str, String type) {
        String result = "";
        type = type.split("\\.")[0];
        //1 找出同名的所有文件
        List<String> files = findFilesByName(projectDir,type+".java");
        if(files.size() == 1){
            return files.get(0);
        }
        s:for (String file :files){
            //2 匹配文件
            String st = file.replaceAll("\\"+SystemConstant.FILE_SEPARATOR,"\\.");
            if(Pattern.compile("import\\s+((\\w|\\d)|\\.)+?\\."+type+"\\s*;").matcher(str).find()){
                return file;
            }
            //3 匹配 import xxx.*
            Matcher mi = Pattern.compile("import\\s+((\\w|\\d)|\\.)+?\\.\\*\\s*;").matcher(str);
            while (mi.find()){
                String mis = mi.group().replaceAll("import|\\s|;|\\*","");
                if(st.contains(mis)){
                    result = file;
                    break s;
                }
            }
            //4 匹配当前包
            Matcher mp2 = Pattern.compile("package.+?;").matcher(str);
            if(mp2.find()){
                String temp = mp2.group().split("\\s+")[1];
                temp = temp.substring(0,type.length()-1);
                if(st.contains(temp)){
                    result = file;
                    break s;
                }
            }
        }
        return result;
    }

    private List<String> findFilesByName(String root, String name) {
        File parent = new File(root);
        String s2 = SystemConstant.FILE_SEPARATOR+name;
        if(parent.isDirectory()){
            List<String> result = FileUtil.loopFiles(root, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean isFlag = true;
                    if(StringUtils.isNotBlank(dirName)){
                        for (String s : dirName.split(",")){
                            if(pathname.getPath().contains(s)){
                                isFlag = false;
                                break;
                            }
                        }
                    }
                    if(isFlag){
                        return pathname.getPath().endsWith(s2);
                    }
                    return false;
                }
            }).stream().map(item -> item.getPath()).collect(Collectors.toList());
            return result;
        }else {
            return root.endsWith(s2) ? Arrays.asList(root) : new ArrayList<>();
        }
    }


    private String removeRemake(String str) {
        //去除单行注释
        String d = "//[^\r\n]*+";
        //去除多行注释
        String dd = "/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
        return str.replaceAll(d,"").replaceAll(dd,"");
    }

    private String getRootPath(String str) {
        if(StringUtils.isBlank(str)){
            return "/";
        }
        Matcher m = Pattern.compile("@RequestMapping.*").matcher(str);
        if(m.find()){
            String result = m.group().replaceAll("@RequestMapping|\\(|\\)|\"","").trim();
            return  StringUtils.isBlank(result) ? "/" : result;
        }
        return "/";
    }

    public List<MenuEntity> getMenu() {
        String str = FileUtil.readString(this.getClass().getResource("/data/menu.json"),"UTF-8");
        List<MenuEntity> result = JSON.parseArray(str).toJavaList(MenuEntity.class);
        return result;
    }


    public Object getJson(InterfaceParameterBody entity) {
        JSON result = new JSONObject();
        if(entity == null || entity.getType() == null){
            return result;
        }
        if(entity.getType().equals("array")){
            Object obj = getJson(entity.getItems());
            result = new JSONArray();
            ((JSONArray) result).add(obj);
        }else if(entity.getType().equals("object")){
            for (Map.Entry<String, InterfaceParameterBody> b : entity.getProperties().entrySet()) {
                if(b.getValue().getType() == null){
                    continue;
                }
                if(b.getValue().getType().equals("array")){
                    Object obj = getJson(b.getValue().getItems());
                    JSONArray array = new JSONArray();
                    array.add(obj);
                    ((JSONObject) result).put(b.getKey(),array);
                }else if(b.getValue().getType().equals("object")){
                    JSONObject obj = new JSONObject();
                    for (Map.Entry<String, InterfaceParameterBody> sb : b.getValue().getProperties().entrySet()) {
                        obj.put(sb.getKey(),getJson(sb.getValue()));
                    }
                    ((JSONObject) result).put(b.getKey(),obj);
                }else{
                    ((JSONObject) result).put(b.getKey(),TypeEnum.getByJavaType(b.getValue().getJavaType()).getDefaultValue());
                }
            }
        }else {
            return TypeEnum.getByJavaType(entity.getJavaType()).getDefaultValue();
        }
        return result;
    }


}
