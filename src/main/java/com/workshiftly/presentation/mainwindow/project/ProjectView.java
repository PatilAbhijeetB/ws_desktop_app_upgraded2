/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.project;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.presentation.mainwindow.dashboard.AttendanceWidget;
import com.workshiftly.presentation.mainwindow.dashboard.AttendanceWidgetModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.utils.viewlist.CachedViewModelCellFactory;
import de.saxsys.mvvmfx.utils.viewlist.ViewListCellFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 *
 * @author hashan
 */
public class ProjectView implements Initializable, FxmlView<ProjectViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ProjectView.class);
    private static ProjectViewModel GLOBAL_PROJECTVIEWMODEL; 
     private static final String AttHead = "/images/atthead.png";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<CompanyProject> projectDropdown;

  
    

    @FXML
    private ListView<TaskViewModel> taskListView;
    
    @FXML
    private ListView<TaskViewModel> prevTaskListView;
    
    @FXML
    private Button projectRefreshBtn;

    @FXML
    private ImageView projectRefreshBtnImageView;
    
    @FXML
    private Button createMeetingTaskBtn;
    
    @FXML
    private Button taskCreationBtn;
    
     @FXML
    private Pane attendanceWidgetPane;

    @FXML
    private ImageView taskCreationImgView;

    
    @FXML
    private Pane attheadWrapper;
    
    @FXML
    private ImageView attheadLogo;
     
    @InjectViewModel
    private ProjectViewModel viewModel;
    
 @FXML
    private TabPane parentTabPane;


    public static ProjectViewModel getGLOBAL_PROJECTVIEWMODEL() {
        return GLOBAL_PROJECTVIEWMODEL;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            final String reloadImagePath = "/images/icons/arrowrotateright.png";
            InputStream reloadImageStream = getClass().getResourceAsStream(reloadImagePath);
            Image reloadImage = new Image(reloadImageStream);
            projectRefreshBtnImageView.setImage(reloadImage);
            
            final String taskPlusImgPath = "/images/icons/add.png";
            InputStream taskPlusImgStream = getClass().getResourceAsStream(taskPlusImgPath);
            Image taskPlusImage = new Image(taskPlusImgStream);
            
            taskCreationImgView.setImage(taskPlusImage);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Failed to load project refresh icon", ex);
        }
         try {
            InputStream inputStream = getClass().getResourceAsStream(AttHead);
            Image attheadimg = new Image(inputStream);
            attheadLogo.setImage(attheadimg);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Attendance Widget Initialization Exception", ex);
        }
        try {
            ProjectView.GLOBAL_PROJECTVIEWMODEL = viewModel;
            StateStorage.set(
                    StateName.PROJECT_VIEWMODEL, ProjectViewModel.class, viewModel
            );
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Unable to set state", 
                    ex
            );
        }
         try {
            ViewTuple<AttendanceWidget, AttendanceWidgetModel> attendanceWidgetTuple
                    = FluentViewLoader.fxmlView(AttendanceWidget.class).load();
            Node attendanceWidgetNode = attendanceWidgetTuple.getView();
            attendanceWidgetPane.getChildren().add(attendanceWidgetNode);
        } catch (Exception ex) {
            System.out.println("Initialize Attendance Widget >>> " + ex);
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Initialize Attendance Widget", ex);
        }
        
        
        this.projectDropdown.setItems(viewModel.getProjectList());
        this.projectDropdown.setConverter(new StringConverter<CompanyProject>() {
            @Override
            public String toString(CompanyProject project) {
               // return project.getName().toUpperCase();
               return project.getName();
            }

            @Override
            public CompanyProject fromString(String string) {
                return viewModel.getProjectList().parallelStream()
                        .filter((CompanyProject element) -> {
                            return element.getName().equals(string);
                        })
                        .findFirst()
                        .orElse(null);
            }
        });
        
        projectDropdown.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                viewModel.getSelectedProjectIdProperty().setValue(newSelection.getId());
            }
        });
        
      
       
        projectDropdown.disableProperty().bind(viewModel.getIsDisabledProjectAndTaskView());
        taskListView.disableProperty().bind(viewModel.getIsDisabledProjectAndTaskView());
        prevTaskListView.disableProperty().bind(viewModel.getIsDisabledProjectAndTaskView());
        
        taskListView.setItems(viewModel.getTaskViewModelList());
        ViewListCellFactory<TaskViewModel> taskListCellFactory =
                CachedViewModelCellFactory.createForFxmlView(TaskView.class);
        
        prevTaskListView.setItems(viewModel.getPrevTaskViewModelList());
        ViewListCellFactory<TaskViewModel> prevtaskListCellFactory =
                CachedViewModelCellFactory.createForFxmlView(TaskView.class);
        
        taskListView.setCellFactory(taskListCellFactory);
        viewModel.setTaskViewModelListView(taskListView);
        
        prevTaskListView.setCellFactory(prevtaskListCellFactory);
        viewModel.setprevTaskViewModelListView(prevTaskListView);
        
        
        
        
        
        RotateTransition refreshIconTransition = new RotateTransition();
        refreshIconTransition.setNode(projectRefreshBtn);
        refreshIconTransition.setAxis(Rotate.Z_AXIS);
        refreshIconTransition.setByAngle(360);
        refreshIconTransition.setCycleCount(500);
        refreshIconTransition.setDuration(Duration.millis(1000));
        
        projectRefreshBtn.setOnAction((ActionEvent actionEvent) -> {
            viewModel.onClickProjectRefreshBtn(refreshIconTransition, taskListView);
            viewModel.onClickProjectRefreshBtn(refreshIconTransition, prevTaskListView);
        });
        
        try {
            createMeetingTaskBtn.setOnMouseClicked((MouseEvent event) -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    viewModel.getCreateMeetingBtnVisibility().set(false);
                    CompanyProject curProject = projectDropdown.getValue();
                    viewModel.createProjectMeetingTask(curProject);
                }
            });
            createMeetingTaskBtn.visibleProperty().set(false);
            
//            createMeetingTaskBtn.visibleProperty()
//                    .bind(viewModel.getCreateMeetingBtnVisibility());
//            createMeetingTaskBtn.disableProperty().bind(
//                    viewModel.getCreateMeetingBtnDisability()
//            );
            
            taskCreationBtn.setOnAction(viewModel::onClickTaskCreateBtn);
            taskCreationBtn.visibleProperty().bind(
                    ProjectViewModel.getCreateTaskButtonVisibility()
            );
        } catch (Exception ex) {
            InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
            String errorMsg = "Error occurred while creting meeting task";
            LoggerService.LogRecord(this.getClass(), errorMsg, logLevel, ex);
        }
        
    }
}
