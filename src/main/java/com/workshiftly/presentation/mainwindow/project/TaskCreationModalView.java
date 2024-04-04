/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.mainwindow.project;

import com.jfoenix.controls.JFXButton;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.ProjectTaskType;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 * @author jade_m
 */
public class TaskCreationModalView implements Initializable, FxmlView<TaskCreationModalViewModel> {
    
    @FXML
    private VBox rootWrappr;

    @FXML
    private Pane headerContainer;

    @FXML
    private VBox formDataContainer;

    @FXML
    private ComboBox<ProjectTaskType> taskTypeSelector;

    @FXML
    private ComboBox<CompanyProject> projectTypeSelector;
    
    @FXML
    private Label projectName;

    @FXML
    private VBox taskFormDataElementContainer;

    @FXML
    private TextField titleTxtField;

    @FXML
    private TextArea descriptTxtArea;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private Label estimationTxtInput;

    @FXML
    private HBox footerContainer;
    
     @FXML
    private TextField daysEstimationInput;

    @FXML
    private TextField hoursEstimationInput;

    @FXML
    private TextField minutesEstimationInput;

    @FXML
    private JFXButton createBtn;

   

    @FXML
    private JFXButton cancelBtn;

   
    
    @FXML
    private Pane TaskErrorWrapper;

    @FXML
    private Label FormErrorTxt;

    @InjectViewModel
    private TaskCreationModalViewModel viewModel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
         //   Image createBtnImg = new Image(
         //           getClass().getResourceAsStream("/images/common/check.png")
         //   );
         //   createBtnImgView.setImage(createBtnImg);
            
          //  Image cancelBtnImg = new Image(
           //         getClass().getResourceAsStream("/images/common/cancel.png")
          //  );
           // cancelBtnImgView.setImage(cancelBtnImg);
            
        } catch (Exception ex)  {
            String errorMsg = "Unable to load button images";
            LoggerService.LogRecord(getClass(), errorMsg, InternalLogger.LOGGER_LEVEL.ALL, ex);
        }
        
        // project task type selector configs
        taskTypeSelector.setItems(viewModel.getProjectTaskTypes());
        taskTypeSelector.setConverter(new StringConverter<ProjectTaskType>() {
            @Override
            public String toString(ProjectTaskType projectTaskType) {
                return projectTaskType.getValue();
            }

            @Override
            public ProjectTaskType fromString(String selectedItem) {
                ObservableList<ProjectTaskType> taskTypes = viewModel.getProjectTaskTypes();
                ProjectTaskType selectedType = taskTypes.stream().filter((ProjectTaskType taskType) -> {
                    return taskType.getValue().equals(selectedItem);
                }).findFirst().orElse(null);
                return selectedType;
            }
        });
        
        taskTypeSelector.valueProperty().addListener(((var ov, var oldValue, var newValue) -> {
            
            if (newValue != null && newValue.getKey() != null) {
                boolean isMeetingTask = newValue.getKey().equals(ProjectTask.MEETING_TASK_TYPE);
                
                taskFormDataElementContainer.visibleProperty().set(!isMeetingTask);
            }
        }));
        viewModel.getSelectedTaskType().bindBidirectional(taskTypeSelector.valueProperty());
        taskTypeSelector.disableProperty().bind(viewModel.getProjectTypeSelectorDisability());
        
        // project selector combo box configs
        projectTypeSelector.setItems(viewModel.getCompanyProjects());
        projectTypeSelector.setConverter(new StringConverter<CompanyProject>() {
            @Override
            public String toString(CompanyProject companyProject) {
                return companyProject.getName();
            }

            @Override
            public CompanyProject fromString(String projectName) {
                ObservableList<CompanyProject> companyProjects = viewModel.getCompanyProjects();
                return companyProjects.stream().filter((CompanyProject element) -> {
                    return element.getName().equals(projectName);
                }).findFirst().orElse(null);
            }
        });
        projectTypeSelector.disableProperty().bind(viewModel.getProjectSelectorDisability());
        projectTypeSelector.valueProperty().bindBidirectional(viewModel.getSelectedProject());
        
        // form elements bindings and configs
        this.titleTxtField.textProperty().bindBidirectional(viewModel.getTaskTitle());
        this.descriptTxtArea.textProperty().bindBidirectional(viewModel.getTaskDescription());
        this.dueDatePicker.valueProperty().bindBidirectional(viewModel.getTaskDueDate());
        this.daysEstimationInput.textProperty().bindBidirectional(viewModel.getDaysEstimationValue());
        this.minutesEstimationInput.textProperty().bindBidirectional(viewModel.getMinutesEstimationValue());
        this.hoursEstimationInput.textProperty().bindBidirectional(viewModel.getHoursEstimationValue());
        
        // text formatters - estimation inputs
        final Pattern digitsOnlyRegex = Pattern.compile("^\\d+$");
        
        this.daysEstimationInput.setTextFormatter(new TextFormatter<>((TextFormatter.Change change) -> {
            String newValue = change.getControlNewText();
            
            if (newValue.isEmpty()) {
                return change;
            }
            
            boolean isOnlyContainsDigits = digitsOnlyRegex.matcher(newValue).matches();
            return isOnlyContainsDigits ? change : null;
        }));
        
        this.minutesEstimationInput.setTextFormatter(new TextFormatter<>((TextFormatter.Change change) -> {
            String newValue = change.getControlNewText();
            
            if (newValue.isEmpty()) {
                return change;
            }
            
            boolean isOnlyContainsDigits = digitsOnlyRegex.matcher(newValue).matches();
            return isOnlyContainsDigits ? change : null;
        }));
        
        this.hoursEstimationInput.setTextFormatter(new TextFormatter<>((TextFormatter.Change change) -> {
            String newValue = change.getControlNewText();
            
            if (newValue.isEmpty()) {
                return change;
            }
            
            boolean isOnlyContainsDigits = digitsOnlyRegex.matcher(newValue).matches();
            return isOnlyContainsDigits ? change : null;
        }));
        
        // estimation inputs - focus out events
        this.minutesEstimationInput.focusedProperty().addListener(viewModel::onFoucsChangeEstimateInput);
        this.hoursEstimationInput.focusedProperty().addListener(viewModel::onFoucsChangeEstimateInput);
        this.daysEstimationInput.focusedProperty().addListener(viewModel::onFoucsChangeEstimateInput);
        
        // close button configs
        cancelBtn.setOnAction(viewModel::onClickCancelBtn);
        // createBtn button configs
        createBtn.setOnAction(viewModel::onClickCreateBtn);
        createBtn.disableProperty().bind(viewModel.getCreateBtnDisability());
        createBtn.textProperty().bind(viewModel.getCreateBtnTxt());
        
        // Form Error Text
        FormErrorTxt.setText("");
        FormErrorTxt.textProperty().bind(viewModel.getFormErrorTxt());
    }
    
}
