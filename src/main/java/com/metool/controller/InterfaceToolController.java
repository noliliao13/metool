package com.metool.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.metool.Application;
import com.metool.comment.ProgressStage;
import com.metool.constant.SystemConstant;
import com.metool.entity.*;
import com.metool.entity.dto.InterfaceDTO;
import com.metool.entity.view.ReqItem;
import com.metool.exception.CustomException;
import com.metool.service.LocalService;
import com.metool.service.YapiService;
import com.metool.util.ExceptionUtil;
import com.metool.vo.AddInterfaceVO;
import com.metool.constant.TypeEnum;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InterfaceToolController implements Initializable {
    @FXML
    private TextField projectDir;
    @FXML
    private TextField controllerName;
    @FXML
    private TextField rejectControllerName;
    @FXML
    private TextField dirName;
    @FXML
    private Label scanningCount;
    @FXML
    private ComboBox filterInterfaceType;
    @FXML
    private ComboBox filterController;
    @FXML
    private TableView<InterfaceDetail> interfaceList;
    @FXML
    private TextField yapiBaseUrl;
    @FXML
    private TextField yapiToken;
    @FXML
    private Button btnScanning;
    @FXML
    private ComboBox<String> chooseInterfaceType;
    @FXML
    private ComboBox<String> chooseInterfaceStatus;
    @FXML
    private BorderPane batchOperationArea;
    @FXML
    private TableColumn choose;
    @FXML
    private TableColumn statusCB;
    @FXML
    private TableColumn interfaceCat;
    @FXML
    private TableColumn interfaceName;
    @FXML
    private TableColumn reqMethod;
    @FXML
    private TableColumn interfacePath;
    @FXML
    private TableColumn reqDataType;
    @FXML
    private TableColumn controllerClass;
    @FXML
    private TableColumn operation;


    private List<IdAndName> cats = new ArrayList<>();
    private String baseUrl = "";
    private String token = "";
    private YapiService yapiService = new YapiService();
    private LocalService localService = new LocalService();
    private ProgressStage p;
    private ObservableList<InterfaceDetail> cacheInterfaceList = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        scanningCount.setFont(Font.font(null, FontWeight.BOLD,12));
        yapiBaseUrl.setText(baseUrl);
        yapiToken.setText(token);
        initCats();
        yapiBaseUrl.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                baseUrl = newValue;
                initCats();
            }
        });

        yapiToken.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                token = newValue;
                initCats();
            }
        });
        chooseInterfaceType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() < 0){
                    return;
                }
                String name = chooseInterfaceType.getItems().get(newValue.intValue());
                for (InterfaceDetail item : interfaceList.getItems()) {
                    item.getCatType().setValue(name);
                }
                for (InterfaceDetail interfaceDetail : cacheInterfaceList) {
                    interfaceDetail.getCatType().setValue(name);
                }
            }
        });
        chooseInterfaceStatus.setItems(FXCollections.observableArrayList(Arrays.asList("???????????????","?????????","?????????")));
        chooseInterfaceStatus.setValue("???????????????");
        chooseInterfaceStatus.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() < 0){
                    return;
                }
                String value = chooseInterfaceStatus.getItems().get(newValue.intValue());
                if(value.equals("???????????????")){
                    return;
                }
                for (InterfaceDetail item : interfaceList.getItems()) {
                    item.getStatusCB().setValue(value);
                }
                for (InterfaceDetail item : cacheInterfaceList) {
                    item.getStatusCB().setValue(value);
                }
            }
        });
        filterController.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() < 0){
                    return;
                }
                String controller = filterController.getItems().get(newValue.intValue()).toString();
                if (controller.equals("?????????CONTROLLER")){
                    interfaceList.setItems(cacheInterfaceList);
                }else{
                    interfaceList.setItems(FXCollections.observableArrayList(cacheInterfaceList.stream().filter(item -> item.getController().equals(controller)).collect(Collectors.toList())));
                }
            }
        });
        filterInterfaceType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() < 0){
                    return;
                }
                Object type = filterInterfaceType.getItems().get(newValue.intValue());
                if (type.equals("????????????")){
                    interfaceList.setItems(cacheInterfaceList);
                }else{
                    interfaceList.setItems(FXCollections.observableArrayList(cacheInterfaceList.stream().filter(item -> item.getCatType().getValue().equals(type)).collect(Collectors.toList())));
                }
            }
        });

    }

    private void initCats() {
        ProgressStage.of(Application.mainStage,"?????????...",new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                List<IdAndName> list = yapiService.getInterfaceCatList(baseUrl,token);
                cats.clear();
                cats.addAll(list);
                Platform.runLater(()->{
                    if(list != null){
                        list.add(0,new IdAndName(0,"???????????????"));
                        chooseInterfaceType.setItems(FXCollections.observableArrayList(list.stream().map(idAndName -> idAndName.getName()).collect(Collectors.toList())));
                        chooseInterfaceType.setValue(list.get(0).getName());
                    }
                });
                return null;
            }
        }).show();
    }

    /**
     * ????????????
     * @param event
     * @throws Exception
     */
    public void btnScanningInterface(ActionEvent event) throws Exception{
        interfaceList.getItems().clear();
        localService.init(projectDir.getText(),dirName.getText());
        List<List<Node>> gpItems = new ArrayList<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/view/scanning-interface.fxml"));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        BorderPane bp = fxmlLoader.load();
        ScanningInterfaceController controller = fxmlLoader.getController();
        ScrollPane sp = controller.getSp();
        GridPane gp = controller.getGp();
        Button b = controller.getCancelScanningBtn();
        HBox titleBox = controller.getTitleBox();
        Label st = new Label("??????????????????????????????");

        ProgressStage pp = ProgressStage.of(Application.mainStage, "???????????????", new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> controllerFiles = localService.getControllerFiles(projectDir.getText(),dirName.getText(),controllerName.getText(),rejectControllerName.getText());
                log.info("????????? {} ?????????",controllerFiles.size());
                return controllerFiles;
            }
        });
        pp.show();
        List<String> controllerFiles = new ArrayList<>();
        while (true){
            if(pp.getWork().isDone()){
                controllerFiles = (List<String>) pp.getWork().get();
                break;
            }
        }

        Integer totalController = controllerFiles.size();
        if(totalController == 0){
            return;
        }

        List<String> finalControllerFiles = controllerFiles;
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() ->{
                    if(totalController != null && finalControllerFiles != null){
                        st.setText("???????????? "+totalController+" ?????????");
                        double height = totalController * 42 > 700 ? 700 : totalController * 42;
                        height = height < 200 ? 200 : height;
                        bp.setPrefHeight(height);
                        Stage stage = p.getStage();
                        stage.setHeight(height+20);
                        Stage parent = Application.mainStage;
                        double x = parent.getX()+(parent.getWidth()-stage.getWidth())/2;
                        double y = parent.getY()+(parent.getHeight()-stage.getHeight())/2;
                        stage.setX(x);
                        stage.setY(y);
                        ObservableList<String> controllerItems = FXCollections.observableArrayList();
                        for (Integer i = 0; i < totalController; i++) {
                            String s = finalControllerFiles.get(i);
                            String className = s.split("\\"+ SystemConstant.FILE_SEPARATOR)[s.split("\\"+ SystemConstant.FILE_SEPARATOR).length-1].split("\\.")[0];
                            gpItems.add(i,Arrays.asList(new Label(String.valueOf(i+1)),new Label(className),new Label()));
                            controllerItems.add(className);
                        }
                        controllerItems.set(0,"?????????CONTROLLER");
                        filterController.setItems(controllerItems);
                        filterController.getSelectionModel().select(controllerItems.get(0));
                        for (int i = 0; i < gpItems.size(); i++) {
                            gp.addRow(i, (Node[]) gpItems.get(i).toArray());
                        }
                    }
                });
                List<InterfaceDetail> interfaceDetailList = localService.getAllInterface(finalControllerFiles,gpItems,sp);
                Platform.runLater(() -> {
                    if(interfaceDetailList != null){
                        if(localService.getIsFlag()){
                            return;
                        }
                        if(CollectionUtil.isEmpty(interfaceDetailList)){
                            scanningCount.setText("??????????????????");
                            return;
                        }
                        batchOperationArea.setVisible(true);
                        batchOperationArea.setManaged(true);
                        Map<String, InterfaceDTO> interfaceMap = new HashMap<>();
                        List<InterfaceDTO> interfaceList_ = null;
                        try {
                            interfaceList_ = yapiService.getAllInterfaceDetail(baseUrl,token);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(interfaceList_ != null){
                            interfaceList_.forEach(item ->{
                                interfaceMap.put(item.getMethod()+ item.getPath(),item);
                            });
                        }
                        scanningCount.setText("???????????? "+interfaceDetailList.size()+" ?????????");
                        List<String> catList = cats.stream().map(item -> item.getName()).collect(Collectors.toList());
                        catList.add(0,"???????????????");
                        ObservableList catObListV = FXCollections.observableArrayList(catList);
                        catObListV.add(0,"????????????");
                        filterInterfaceType.setItems(catObListV);
                        filterInterfaceType.getSelectionModel().select(catObListV.get(0));
                        for (int i = 0 ; i < interfaceDetailList.size() ; i++) {
                            InterfaceDetail item = interfaceDetailList.get(i);
                            ObservableList<String> observableList = FXCollections.observableList(catList);
                            item.getCatType().setItems(observableList);
                            AddInterfaceVO addInterfaceVO = new AddInterfaceVO();
                            InterfaceDTO d = interfaceMap.get(item.getMethod()+item.getPath());
                            String catName = "";
                            if(d!=null){
                                addInterfaceVO.setCatid(d.getCatid());
                                item.setId(d.getId());
                                item.setCatid(d.getCatid());
                                item.setReqType(item.getIsReqFormData() ? 1 : 0);
                                IdAndName cat = yapiService.getCatById(item.getCatid(),cats);
                                if(cat != null){
                                    catName = cat.getName();
                                }
                                item.getStatusCB().setValue(d.getStatus().equals("done") ? "?????????" : "?????????");
                            }else{
                                catName = "???????????????";
                            }
                            item.getTitleField().setText(item.getTitle());
                            item.getCatType().setValue(catName);
                            addInterfaceVO.setInterfaceDetail(item);
                            BeanUtil.copyProperties(item,addInterfaceVO);

                            int finalI = i;
                            item.getCheckBox().selectedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    cacheInterfaceList.get(finalI).getCheckBox().setSelected(newValue);
                                }
                            });
                            item.getCatType().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                                @Override
                                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                    cacheInterfaceList.get(finalI).getCatType().setValue(newValue);
                                }
                            });
                            item.getTitleField().textProperty().addListener(new ChangeListener<String>() {
                                @Override
                                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                    cacheInterfaceList.get(finalI).getTitleField().setText(newValue);
                                }
                            });
                        }
                        ObservableList<InterfaceDetail> list = FXCollections.observableArrayList(interfaceDetailList);
                        interfaceList.setItems(list);
                        cacheInterfaceList.clear();
                        cacheInterfaceList.addAll(list);
                        initList();
                    }
                });
                return null;
            }
        };

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.setPrefWidth(15);
        indicator.setPrefHeight(15);
        indicator.progressProperty().bind(task.progressProperty());
        titleBox.getChildren().addAll(st,indicator);

        p = ProgressStage.of(Application.mainStage,bp,task);
        p.show();

        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Task t = p.getWork();
                t.cancel();
                localService.setIsFlag(true);
                ProgressStage.of(Application.mainStage, "?????????...", new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (true){
                            if(localService.getIsEnd()){
                                break;
                            }
                        }
                        return null;
                    }
                }).show();
            }
        });
    }

    private void initList() {
        interfaceList.setEditable(true);
        CheckBox selectAll = new CheckBox();
        selectAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                CheckBox cb = (CheckBox) event.getTarget();
                if(cb.isSelected()){
                    for (InterfaceDetail item : interfaceList.getItems()) {
                        item.getCheckBox().setSelected(true);
                    }
                }else{
                    for (InterfaceDetail item : interfaceList.getItems()) {
                        item.getCheckBox().setSelected(false);
                    }
                }
            }
        });
        choose.setGraphic(selectAll);
        choose.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.05));
        statusCB.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.05));
        interfaceCat.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.15));
        interfaceName.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.15));
        reqMethod.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.05));
        interfacePath.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.15));
        reqDataType.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.05));
        controllerClass.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.1));
        operation.prefWidthProperty().bind(interfaceList.widthProperty().multiply(0.2));

        choose.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,CheckBox>("checkBox"));
        interfaceCat.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,ComboBox>("catType"));
        interfaceName.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,TextField>("titleField"));
        reqMethod.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,String>("method"));
        interfacePath.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,String>("path"));
        controllerClass.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,String>("controller"));
        statusCB.setCellValueFactory(new PropertyValueFactory<InterfaceDetail,ComboBox>("statusCB"));
        reqDataType.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InterfaceDetail,String>, ObservableValue>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<InterfaceDetail,String> param) {
                InterfaceDetail d = param.getValue();
                return new ReadOnlyObjectWrapper<String>(d.getReqType().equals(0) ? "json" : "form");
            }
        });

        operation.setCellFactory(col ->{
            TableCell<InterfaceDetail,String> cell = new TableCell<InterfaceDetail,String>(){
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(!empty){
                        InterfaceDetail entity = interfaceList.getItems().get(this.getIndex());
                        Button reqBtn = new Button("???????????????");
                        Button resBtn = new Button("???????????????");
                        Button pushBtn = new Button("??????");

                        pushBtn.setStyle("-fx-base: yellow;");
                        HBox hBox = new HBox();
                        hBox.setAlignment(Pos.CENTER);
                        hBox.setSpacing(10);
                        hBox.getChildren().addAll(reqBtn,resBtn,pushBtn);
                        setGraphic(hBox);

                        if(!entity.getIsReqFormData() && entity.getReqEntity() == null){
                            reqBtn.setDisable(true);
                        }
                        if(entity.getResEntity() == null){
                            resBtn.setDisable(true);
                        }

                        reqBtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    Stage stage = new Stage();
                                    stage.initModality(Modality.APPLICATION_MODAL);
                                    Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
                                    FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/view/request-detail.fxml"));
                                    Scene scene = new Scene(fxmlLoader.load(),rectangle2D.getWidth()*0.8,rectangle2D.getHeight()*0.8-72);
                                    RequestDetailController controller = fxmlLoader.getController();
                                    if(entity.getIsReqFormData()){
                                        controller.getJsonBox().setVisible(false);
                                        controller.getDataType().setText("form-data");
                                    }else {
                                        controller.getJsonBox().setVisible(true);
                                        Object obj = localService.getJson(entity.getReqEntity());
                                        String result = JSON.toJSONString(obj, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue);
                                        controller.getReqJson().setText(result);
                                        String type = obj.getClass().getTypeName().split("\\.")[obj.getClass().getTypeName().split("\\.").length-1];
                                        if(TypeEnum.getByJavaType(type) != null){
                                            controller.getDataType().setText(TypeEnum.getByJavaType(type).getJsonType());
                                        }
                                    }
                                    TreeItem<ReqItem> root = yapiService.createReqRoot(entity);
                                    root.setExpanded(true);
                                    controller.getReqTreeTable().setRoot(root);
                                    stage.setTitle("???????????????");
                                    stage.setScene(scene);
                                    stage.show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });

                        resBtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    Stage stage = new Stage();
                                    stage.initModality(Modality.APPLICATION_MODAL);
                                    Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
                                    FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/view/request-detail.fxml"));
                                    Scene scene = new Scene(fxmlLoader.load(),rectangle2D.getWidth()*0.8,rectangle2D.getHeight()*0.8-72);
                                    RequestDetailController controller = fxmlLoader.getController();

                                    Object obj = localService.getJson(entity.getResEntity());
                                    String result = JSON.toJSONString(obj, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue);
                                    controller.getReqJson().setText(result);
                                    String type = obj.getClass().getTypeName().split("\\.")[obj.getClass().getTypeName().split("\\.").length-1];
                                    if(TypeEnum.getByJavaType(type) != null){
                                        controller.getDataType().setText(TypeEnum.getByJavaType(type).getJsonType());
                                    }
                                    TreeItem<ReqItem> root = yapiService.createRoot(entity.getResEntity());
                                    root.setExpanded(true);
                                    controller.getReqTreeTable().setRoot(root);
                                    stage.setTitle("???????????????");
                                    stage.setScene(scene);
                                    stage.show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });

                        pushBtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                ProgressStage.of(Application.mainStage, "?????????", new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                       try {
                                           String name = (String) entity.getCatType().getValue();
                                           IdAndName idAndName = yapiService.getCatByName(name,cats);
                                           if(idAndName != null){
                                               entity.setCatid(idAndName.getId());
                                           }
                                           entity.setTitle(entity.getTitleField().getText());
                                           entity.setStatus(entity.getStatusCB().getValue().toString());
                                           yapiService.pushApi(baseUrl,token,entity);
                                           ToastBarToasterService service = new ToastBarToasterService();
                                           service.initialize();
                                           service.success("????????????????????????","????????????", ToastParameter.builder().timeout(Duration.seconds(2)).build());
                                       }catch (Exception e){
                                           ExceptionUtil.doException(e);
                                       }
                                        return null;
                                    }
                                }).show();
                            }
                        });
                    }
                }
            };
            return cell;
        });


    }

    public void pushToYapi(ActionEvent event) throws IOException {
        ProgressStage.of(Application.mainStage, "?????????", new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    List<InterfaceDetail> list = interfaceList.getItems().stream().filter(item -> item.getCheckBox().isSelected()).map(item ->{
                        String name = (String) item.getCatType().getValue();
                        IdAndName idAndName = yapiService.getCatByName(name,cats);
                        if(idAndName != null){
                            item.setCatid(idAndName.getId());
                        }
                        item.setTitle(item.getTitleField().getText());
                        item.setStatus(item.getStatusCB().getValue().toString());
                        return item;
                    }).collect(Collectors.toList());
                    if(list.size() == 0){
                        throw CustomException.error("??????????????????????????????");
                    }
                    //????????????
                    checkParameter(list);
                    List<InterfaceDetail> successList = new ArrayList<>();
                    List<InterfaceDetail> failList = new ArrayList<>();
                    for (InterfaceDetail entity : list) {
                        try {
                            yapiService.pushApi(baseUrl,token,entity);
                            successList.add(entity);
                        }catch (Exception e){
                            e.printStackTrace();
                            failList.add(entity);
                        }
                    }
                    ToastBarToasterService service = new ToastBarToasterService();
                    service.initialize();
                    if(failList.size() == 0){
                        service.success("????????????????????????","??????????????????????????????("+successList.size()+"???)", ToastParameter.builder().timeout(Duration.seconds(2)).build());
                    }else{
                        service.info("????????????????????????","??????: "+successList.size()+" ????????????: "+failList.size()+" ???", ToastParameter.builder().timeout(Duration.seconds(4)).build());
                    }
                }catch (Exception e){
                    ExceptionUtil.doException(e);
                }
                return null;
            }
        }).show();
    }

    private void checkParameter(List<InterfaceDetail> list) throws Exception {
        for (InterfaceDetail v :list) {
            if(v.getCheckBox().isSelected()){
                if(v.getCatid().equals(0)){
                    throw CustomException.error("???????????????????????????");
                }
                if(StringUtils.isBlank(v.getTitle())){
                    throw CustomException.error("???????????????????????????");
                }
            }
        }
    }

    public void chooseProjectDir(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(new Stage());
        if(file != null){
            projectDir.setText(file.getPath());
            btnScanning.setDisable(false);
        }
    }

}