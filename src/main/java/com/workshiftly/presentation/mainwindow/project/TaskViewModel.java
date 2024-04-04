/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.project;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.TaskStatusLog;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.domain.ProjectTaskModule;
import com.workshiftly.domain.RawDataModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowViewModel;
import com.workshiftly.presentation.mainwindow.meeting.MeetingInfoModalView;
import com.workshiftly.presentation.mainwindow.meeting.MeetingInfoModalViewModel;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.joda.time.DateTimeZone;

/**
 *
 * @author hashan
 */
public class TaskViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(TaskViewModel.class);

    private final java.time.Duration ONE_SECOND_DURATION = java.time.Duration.ofSeconds(1);
    
    private final SimpleStringProperty titleLabelTextProperty;
    private final SimpleStringProperty descLabelTextProperty;
    private final SimpleStringProperty projLabelTextProperty ;
  
   // private final SimpleStringProperty dueDateLabelTextProperty;
    //private final SimpleStringProperty estimatedTimeLabelTextProperty;
    private final SimpleStringProperty spendTimeLabelTextProperty;
    
    private final SimpleBooleanProperty listViewBtnVisibilityProperty;
    private final SimpleBooleanProperty playPuaseBtnDisableProperty;
    private final SimpleBooleanProperty stopBtnDisableProperty;
    private final SimpleBooleanProperty taskEditBtnVisiblity;
    
    private final ObjectProperty<TaskStatusLog.TaskStatus> taskStatusProperty;
    private final SimpleObjectProperty<Image> playBtnImageProperty;
    private final SimpleObjectProperty<Image> ExpBtnImageProperty;
     private final SimpleObjectProperty<Image> LessBtnImageProperty;
    private final SimpleObjectProperty<Image> stopBtnImageProperty;
    private final SimpleObjectProperty<ProjectTask> globalWorkingTaskProperty;

    private final Image playButtonImage;
    private final Image pauseButtonImage;
    private final Image stopButtonImage;
    private final Image upButtonImage;
    private final Image downButtonImage;
    
    private final Timeline spendTimeTimeline;
    private Duration spendTimeDuration;
    private final ReadOnlyObjectWrapper<WorkStatusLog.WorkStatus> globalUserWorkStatusProperty;
    
    private ProjectTask projectTask;
    private ObservableList<CompanyProject> companyProjects;

    public TaskViewModel(
            ProjectTask task, 
            ReadOnlyObjectWrapper<WorkStatusLog.WorkStatus> globalUserWorkStatusProperty
    ) {
        this.projectTask = task;
        this.globalUserWorkStatusProperty = globalUserWorkStatusProperty;
        this.globalWorkingTaskProperty = ProjectViewModel.getCURRENT_WORKING_TASK();
        
        titleLabelTextProperty = new SimpleStringProperty();
        descLabelTextProperty=new SimpleStringProperty();
        projLabelTextProperty=new SimpleStringProperty();
        //dueDateLabelTextProperty = new SimpleStringProperty();
        //estimatedTimeLabelTextProperty = new SimpleStringProperty();
        spendTimeLabelTextProperty = new SimpleStringProperty();
        
        taskStatusProperty = new SimpleObjectProperty<>(TaskStatusLog.TaskStatus.TODO);
        playBtnImageProperty = new SimpleObjectProperty<>();
        stopBtnImageProperty = new SimpleObjectProperty<>();
        ExpBtnImageProperty=new SimpleObjectProperty<>();
        LessBtnImageProperty=new SimpleObjectProperty<>();
        // taskEditBtnVisibility boolean property
        boolean isOfflineTask = projectTask.getType() != null 
                && projectTask.getType().equals(ProjectTask.OFFLINE_TASK_TYPE);
        
        taskEditBtnVisiblity = new SimpleBooleanProperty(isOfflineTask);
        
        ProjectTask globalWorkingTask = this.globalWorkingTaskProperty.get();
        String globalWorkingTaskId = globalWorkingTask != null ? globalWorkingTask.getId() : null;
        
        boolean playBtnDisability = !(globalWorkingTaskId == null || globalWorkingTaskId.equals(task.getId()));
        playPuaseBtnDisableProperty = new SimpleBooleanProperty(playBtnDisability);
        
        registerCurrentWorkingTaskChangeListener();
        registerGlobalWorkStatusChangeListener(globalUserWorkStatusProperty);
        
        final String playBtnImagePath = "/images/mainwindow/runtask.png";
        InputStream playBtnStream = getClass().getResourceAsStream(playBtnImagePath);
        playButtonImage = new Image(playBtnStream);
        playBtnImageProperty.set(playButtonImage);

        
        final String pauseBtnImagePath = "/images/mainwindow/main_window_pause_icon1.png";
        InputStream pauseBtnStream = getClass().getResourceAsStream(pauseBtnImagePath);
        pauseButtonImage = new Image(pauseBtnStream);
        
        final String upBtnImagePath = "/images/mainwindow/up.png";
        InputStream upBtnStream = getClass().getResourceAsStream(upBtnImagePath);
        upButtonImage = new Image(upBtnStream);
        LessBtnImageProperty.set(upButtonImage);
        
        final String downBtnImagePath = "/images/mainwindow/down.png";
        InputStream downBtnStream = getClass().getResourceAsStream(downBtnImagePath);
        downButtonImage = new Image(downBtnStream);
        ExpBtnImageProperty.set(downButtonImage);
        
        TaskStatusLog mostRecentTaskStatusLog = StateStorage.getCurrentState(StateName.LAST_TASK_STATUS_LOG);
        
        if (mostRecentTaskStatusLog != null && mostRecentTaskStatusLog.getTaskId().equals(task.getId())) {
            TaskStatusLog.TaskStatus previouseTaskStatus = mostRecentTaskStatusLog.getTaskStatus();
            if (previouseTaskStatus == TaskStatusLog.TaskStatus.START) {
                playBtnImageProperty.set(pauseButtonImage);
            }
        }
        
        final String stopBtnImagePath = "/images/mainwindow/tickcircle.png";
        InputStream stopBtnStream = getClass().getResourceAsStream(stopBtnImagePath);
        stopButtonImage = new Image(stopBtnStream);
        stopBtnImageProperty.set(stopButtonImage);
        
        stopBtnDisableProperty = new SimpleBooleanProperty(false);
        if (projectTask.getType() != null) {
            boolean isMeetingTask = projectTask.getType().equals(ProjectTask.MEETING_TASK_TYPE);
            stopBtnDisableProperty.set(isMeetingTask);
        }
        
        boolean didTaskCompleted = projectTask.isHasCompleted();
        listViewBtnVisibilityProperty = new SimpleBooleanProperty(!didTaskCompleted);
        
        titleLabelTextProperty.set(projectTask.getTitle());
        descLabelTextProperty.set(projectTask.getDescription());
        
        //String ProjectName= "Project -"+projectTask.getProjectName();
        projLabelTextProperty.set("Project -"+projectTask.getProjectName());
        Long taskDueDate = projectTask.getDueDate();
        
        if (taskDueDate != null && taskDueDate > 0) {
            Duration dueDateDuratiion = Duration.ofSeconds(taskDueDate);
            String readableDueDate = TimeUtility
                    .getHumanReadbleDateTime(
                            dueDateDuratiion, DateTimeZone.UTC, "yyyy/MM/dd"
                    );
           // dueDateLabelTextProperty.set(readableDueDate);
        } else {
          //  dueDateLabelTextProperty.set("----/--/--");
        }
        
        Long taskEstimation = projectTask.getEstimate();
        
        if (taskEstimation != null && taskEstimation > 0) {
            Duration estimationDuration = Duration.ofSeconds(taskEstimation);
            String formattedEstimationDuration = TimeUtility.formatDuration(estimationDuration);
          //  estimatedTimeLabelTextProperty.set(formattedEstimationDuration);
        } else {
           // estimatedTimeLabelTextProperty.set("--:--:--");
        }
        
        spendTimeDuration = Duration.ofSeconds(
                projectTask.getSpentTime() != null ? projectTask.getSpentTime() : 0
        );
        String formattedSpendTimeDuration = TimeUtility.formatDuration(spendTimeDuration);
        spendTimeLabelTextProperty.set(formattedSpendTimeDuration);
        
        javafx.util.Duration keyFrameDuration = javafx.util.Duration.seconds(1.0);
        
        KeyFrame spendTimeKeyFrame = new KeyFrame(keyFrameDuration, (ActionEvent event) -> {
            spendTimeDuration = spendTimeDuration.plus(ONE_SECOND_DURATION);
            String formattedDuration = TimeUtility.formatDuration(spendTimeDuration);
            spendTimeLabelTextProperty.set(formattedDuration);
            
        });
        
        spendTimeTimeline = new Timeline(spendTimeKeyFrame);
        spendTimeTimeline.setCycleCount(Timeline.INDEFINITE);
         
         if (globalWorkingTask != null) {
             boolean isGlobalWorkingTask = globalWorkingTask.getId() != null 
                     && globalWorkingTask.getId().equals(projectTask.getId());
             
             if (isGlobalWorkingTask) {
                 taskStatusProperty.set(TaskStatusLog.TaskStatus.START);
                 
                 projectTask.setSpentTime(globalWorkingTask.getSpentTime());
                 spendTimeDuration = Duration.ofSeconds(projectTask.getSpentTime());
                 spendTimeTimeline.play();
             }
         } else {
            if (projectTask.isHasCompleted()) {
                taskStatusProperty.set(TaskStatusLog.TaskStatus.STOP);
            }
         }
         
         if (projectTask.getType().equals(ProjectTask.MEETING_TASK_TYPE)) {
             meetingTaskActivityChangeListener();
         }
         
         
          
              
                   
    }
    
    public TaskViewModel() {
        
        globalUserWorkStatusProperty = null;
        titleLabelTextProperty = new SimpleStringProperty();
        descLabelTextProperty=new SimpleStringProperty();
        projLabelTextProperty=new SimpleStringProperty();
       // dueDateLabelTextProperty = new SimpleStringProperty();
       // estimatedTimeLabelTextProperty = new SimpleStringProperty();
        spendTimeLabelTextProperty = new SimpleStringProperty();
        
        taskStatusProperty = new SimpleObjectProperty<>(TaskStatusLog.TaskStatus.TODO);
        playBtnImageProperty = new SimpleObjectProperty<>();
        stopBtnImageProperty = new SimpleObjectProperty<>();
        globalWorkingTaskProperty = ProjectViewModel.getCURRENT_WORKING_TASK();
        taskEditBtnVisiblity = new SimpleBooleanProperty(false);
        ExpBtnImageProperty = new SimpleObjectProperty<>();
        LessBtnImageProperty = new SimpleObjectProperty<>();
        
        listViewBtnVisibilityProperty = new SimpleBooleanProperty(true);
        playPuaseBtnDisableProperty = new SimpleBooleanProperty(false);
        registerCurrentWorkingTaskChangeListener();
        
        final String playBtnImagePath = "/images/mainwindow/main_window_play_icon.png";
        InputStream playBtnStream = getClass().getResourceAsStream(playBtnImagePath);
        playButtonImage = new Image(playBtnStream);
        playBtnImageProperty.set(playButtonImage);
        
         final String upBtnImagePath = "/images/mainwindow/up.png";
        InputStream upBtnStream = getClass().getResourceAsStream(upBtnImagePath);
        upButtonImage = new Image(upBtnStream);
        LessBtnImageProperty.set(upButtonImage);
        
        final String downBtnImagePath = "/images/mainwindow/down.png";
        InputStream downBtnStream = getClass().getResourceAsStream(downBtnImagePath);
        downButtonImage = new Image(downBtnStream);
        ExpBtnImageProperty.set(downButtonImage);
        
        final String pauseBtnImagePath = "/images/mainwindow/main_window_pause_icon.png";
        InputStream pauseBtnStream = getClass().getResourceAsStream(pauseBtnImagePath);
        pauseButtonImage = new Image(pauseBtnStream);
        
        final String stopBtnImagePath = "/images/mainwindow/main_window_stop_icon.png";
        InputStream stopBtnStream = getClass().getResourceAsStream(stopBtnImagePath);
        stopButtonImage = new Image(stopBtnStream);
        stopBtnImageProperty.set(stopButtonImage);
        
        stopBtnDisableProperty = new SimpleBooleanProperty(false);
        
        javafx.util.Duration keyFrameDuration = javafx.util.Duration.seconds(1.0);
        
        KeyFrame spendTimeKeyFrame = new KeyFrame(keyFrameDuration, (ActionEvent event) -> {
            spendTimeDuration = spendTimeDuration.plus(ONE_SECOND_DURATION);
            String formattedDuration = TimeUtility.formatDuration(spendTimeDuration);
            spendTimeLabelTextProperty.set(formattedDuration);
        });
        
        spendTimeTimeline = new Timeline(spendTimeKeyFrame);
        spendTimeTimeline.setCycleCount(Timeline.INDEFINITE);
    }


    public SimpleStringProperty getTitleLabelTextProperty() {
        return titleLabelTextProperty;
    }
    
     public SimpleStringProperty getDescLabelTextProperty() {
        return descLabelTextProperty;
    }
    
       public SimpleStringProperty getProjNameLabelTextProperty() {
        return projLabelTextProperty;
    }

    //public SimpleStringProperty getDueDateLabelTextProperty() {
   //     return dueDateLabelTextProperty;
   // }

   // public SimpleStringProperty getEstimatedTimeLabelTextProperty() {
  //      return estimatedTimeLabelTextProperty;
 //   }

    public SimpleStringProperty getSpendTimeLabelTextProperty() {
        return spendTimeLabelTextProperty;
    }

    public ObjectProperty<TaskStatusLog.TaskStatus> getTaskStatusProperty() {
        return taskStatusProperty;
    }

    public SimpleObjectProperty<Image> getPlayBtnImageProperty() {
        return playBtnImageProperty;
    }
    
    public SimpleObjectProperty<Image> getExpBtnImageProperty() {
        return ExpBtnImageProperty;
    }
    
     public SimpleObjectProperty<Image> getLessBtnImageProperty() {
        return LessBtnImageProperty;
    }

    public SimpleObjectProperty<Image> getStopBtnImageProperty() {
        return stopBtnImageProperty;
    }

    public void setCompanyProjects(ObservableList<CompanyProject> companyProjects) {
        this.companyProjects = companyProjects;
    }

    public SimpleBooleanProperty getStopBtnDisableProperty() {
        return stopBtnDisableProperty;
    }
    
    public SimpleBooleanProperty getTaskEditBtnVisibility() {
        return taskEditBtnVisiblity;
    }
    
    public void onClickStartPauseBtn(ActionEvent event) {
        
        WorkStatusLog.WorkStatus userWorkStatus = this.globalUserWorkStatusProperty.getValue();
        if (userWorkStatus == WorkStatusLog.WorkStatus.BREAK 
                || userWorkStatus == WorkStatusLog.WorkStatus.STOP) {
            showUserWorkStatusBreakAlertBox();
            return;
        }
        
        try {
            String taskType = this.projectTask.getType();

            if (taskType.equals(ProjectTask.MEETING_TASK_TYPE)) {
                popupMeetingInfoFormModal();
                return;
            }

            Service<Response> domainService = getTaskStartPauseService();
            domainService.start();
        } catch (Exception ex) {
            String loggerMsg = "Unable to handle Taskview button action";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, loggerMsg, ex);
        }
            
    }

    public SimpleBooleanProperty getListViewBtnVisibilityProperty() {
        return listViewBtnVisibilityProperty;
    }

    public SimpleBooleanProperty getPlayPuaseBtnDisableProperty() {
        return playPuaseBtnDisableProperty;
    }

    public ProjectTask getTask() {
        return projectTask;
    }

    public void setTask(ProjectTask task) {
        this.projectTask = task;
    }

    public Duration getSpendTimeDuration() {
        return spendTimeDuration;
    }
    
    public void onClickStopBtn(ActionEvent event) {
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Text("Task Completion Confirmation"));
        dialogLayout.setBody(new Text("Are you sure to mark this task as completed?"));
        
        JFXButton yesButton = new JFXButton("OK, Mark as completed");
        yesButton.getStyleClass().add("dialog-modal-primary-btn");
        
        JFXButton noButton = new JFXButton("Cancel");
        noButton.getStyleClass().add("dialog-modal-primary-btn");
        dialogLayout.setActions(yesButton, noButton);
        
        JFXDialog dialog 
                = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.CENTER);
        
        yesButton.setOnAction((ActionEvent clickEvent) -> {
            clickEvent.consume();
            dialog.close();
            
            Service<Response> domainService = getTaskStopService();
            domainService.start();
        });
        
        noButton.setOnAction((ActionEvent clickEvent) -> {
            clickEvent.consume();
            dialog.close();
        });
        MainScreenController.showJFXDialog(dialog);
    }
    
    private Service<Response> getTaskStartPauseService() {
        
        Service<Response> domainService;
        domainService = new Service<Response>() {
            @Override
            protected javafx.concurrent.Task<Response> createTask() {
                return new javafx.concurrent.Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        TaskStatusLog.TaskStatus currentStaskStatus = taskStatusProperty.get();
                        
                        if (currentStaskStatus == TaskStatusLog.TaskStatus.START) {
                            projectTask.setSpentTime(spendTimeDuration.toSeconds());
                        }
                        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                        Response response = projectTaskModule.recordStartPauseTaskStatus(
                                projectTask, currentStaskStatus
                        );
                        return response;
                    };
                };
            }
        };
        
        domainService.setOnRunning((WorkerStateEvent stateEvent) -> {
            playPuaseBtnDisableProperty.set(true);
            toggleProjectViewDisability(true);
        });
        
        domainService.setOnSucceeded((var stateEvent) -> {

            playPuaseBtnDisableProperty.set(false);

            Gson gson = new Gson();
            Response response = domainService.getValue();
            toggleProjectViewDisability(false);

            if (!response.isError()) {
                JsonElement responseData = response.getData();
                
                TaskStatusLog taskStatusLog = gson.fromJson(responseData, TaskStatusLog.class);
                TaskStatusLog.TaskStatus newTaskStatus = taskStatusLog.getTaskStatus();
                taskStatusProperty.setValue(newTaskStatus);
                switch (newTaskStatus) {
                    case START:
                        this.globalWorkingTaskProperty.set(projectTask);
                        playBtnImageProperty.set(pauseButtonImage);
                       
                        
                        
                       
                        spendTimeTimeline.play();
                        break;
                    case BREAK:
                        this.globalWorkingTaskProperty.set(null);
                        playBtnImageProperty.set(playButtonImage);
                        boolean isOfflineTask = projectTask.getType() != null 
                         && projectTask.getType().equals(ProjectTask.OFFLINE_TASK_TYPE);
                       if(isOfflineTask)
                       {
                        taskEditBtnVisiblity.set(true);
                       }
                        spendTimeTimeline.pause();
                        break;
                    case STOP:
                        taskEditBtnVisiblity.set(false);
                        spendTimeTimeline.stop();
                        this.globalWorkingTaskProperty.set(null);
                        break;
                }
                
                String taskType = projectTask.getType();
                
                if (taskType != null && taskType.equals(ProjectTask.OFFLINE_TASK_TYPE)) {
                    WorkStatusLog.WorkStatus nextWorkStatus = newTaskStatus.equals(TaskStatusLog.TaskStatus.START)
                            ? WorkStatusLog.WorkStatus.OFFLINE_TASK : WorkStatusLog.WorkStatus.START;
                    startRecordWorkStatusLog(nextWorkStatus);
                }
                return;
            }
                
            showRecordTaskStatusLogErrors(response);
        });
        return domainService;
    }
     public void onClickExpandBtn(ActionEvent event) {
        
        WorkStatusLog.WorkStatus userWorkStatus = this.globalUserWorkStatusProperty.getValue();
        if (userWorkStatus == WorkStatusLog.WorkStatus.BREAK 
                || userWorkStatus == WorkStatusLog.WorkStatus.STOP) {
            showUserWorkStatusBreakAlertBox();
            return;
        }
        
        try {
            String taskType = this.projectTask.getType();

            if (taskType.equals(ProjectTask.MEETING_TASK_TYPE)) {
                popupMeetingInfoFormModal();
                return;
            }

            Service<Response> domainService = getTaskStartPauseService();
            domainService.start();
        } catch (Exception ex) {
            String loggerMsg = "Unable to handle Taskview button action";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, loggerMsg, ex);
        }
            
    }

    private void startRecordWorkStatusLog(WorkStatusLog.WorkStatus workStatus) {
        Service<Response> service = new Service<>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<>() {
                    @Override
                    protected Response call() throws Exception {
                        RawDataModule rawDataModule = new RawDataModule();
                        return rawDataModule.captureWorkStatusLog(
                                workStatus, null, true
                        );
                    }
                };
            };
        };
        service.setOnSucceeded((WorkerStateEvent event) -> {
            Response response = service.getValue();
            
            if (!response.isError()) {
                WorkStatusLog workStatusLog = new Gson().fromJson(
                        response.getData(), WorkStatusLog.class
                );
                ObservableList<WorkStatusLog> globalWorkStatusLogs 
                        = AuthenticatedMainWindowViewModel.getWORK_STATUS_LOGS();
                globalWorkStatusLogs.add(workStatusLog);
                
                SimpleObjectProperty<WorkStatusLog.WorkStatus> globalWorkStatus = AuthenticatedMainWindowViewModel
                        .getUSER_WORK_STATUS_objectProperty();
                globalWorkStatus.set(workStatusLog.getWorkStatus());
            }
        });
        service.start();
    }
    
    private void showRecordTaskStatusLogErrors(Response response) {
                
        String errorHeadingText;
        String errorBodyTxt = response.getMessage();
        
        switch (response.getStatusCode()) {
            case SESSION_INVALID:
                errorHeadingText = "Authentication Error";
                break;
            case TASK_NOT_FOUND:
                errorHeadingText = "Task Not Found";
                break;
            case TASK_ALREADY_COMPLETED:
                errorHeadingText = "Task Aleady Completed";
                break;
            case TASK_ALREADY_STOPPED:
                errorHeadingText = "Task Already Stopped";
                break;
            default:
                errorHeadingText = "Internal Error";
                errorBodyTxt = "Application error is occurred. Please try again";
                break;
        }
        
        JFXDialogLayout errorDialogLayout = new JFXDialogLayout();
        errorDialogLayout.setHeading(new Text(errorHeadingText));
        errorDialogLayout.setBody(new Text(errorBodyTxt));
        
        JFXButton dialogBtn = new JFXButton("Close");
        dialogBtn.getStyleClass().add("dialog-modal-primary-btn");
        errorDialogLayout.setActions(dialogBtn);
        
        JFXDialog dialog = new JFXDialog(null, errorDialogLayout, JFXDialog.DialogTransition.CENTER);
        dialogBtn.setOnAction((ActionEvent event) -> {
            dialog.close();
        });
    }

    private Service<Response> getTaskStopService() {
        
        Service<Response> domainService = new Service<Response>() {
            @Override
            protected javafx.concurrent.Task<Response> createTask() {
                return new javafx.concurrent.Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                        
                            projectTask.setSpentTime(spendTimeDuration.toSeconds());
                        
                        Response response = projectTaskModule.recordStopTaskStatus(projectTask);
                        return response;
                    };
                };
            }
        };
        
        domainService.setOnRunning((WorkerStateEvent stateEvent) -> {
            listViewBtnVisibilityProperty.set(false);
            spendTimeTimeline.pause();
            MainScreenController.showLoadingSplashScreen();
        });
        
        domainService.setOnSucceeded((WorkerStateEvent stateEvent) -> {
            
            Gson gson = new Gson();
            Response response = domainService.getValue();
            MainScreenController.hideLoadingSplashScreen();
            
            if (!response.isError()) {
                JsonElement responseData = response.getData();
                TaskStatusLog taskStatusLog = gson.fromJson(responseData, TaskStatusLog.class);
                TaskStatusLog.TaskStatus newTaskStatus = taskStatusLog.getTaskStatus();
                
                if (newTaskStatus == TaskStatusLog.TaskStatus.STOP) {
                    projectTask.setHasCompleted(true);
                    spendTimeTimeline.stop();
                    listViewBtnVisibilityProperty.set(false);
                    
                    ProjectTask currentUserWorkingTask = this.globalWorkingTaskProperty.get();
                    String currentUserWorkingTaskId = currentUserWorkingTask != null 
                            ? currentUserWorkingTask.getId() : null;
                    
                    if (currentUserWorkingTaskId != null 
                            && currentUserWorkingTaskId.equals(this.projectTask.getId())) {
                        this.globalWorkingTaskProperty.set(null);
                        String taskType = projectTask.getType();
                        
                        boolean isOfflineTask = taskType != null 
                                && taskType.equals(ProjectTask.OFFLINE_TASK_TYPE);
                        if (isOfflineTask) {
                            startRecordWorkStatusLog(WorkStatusLog.WorkStatus.START);
                        }
                    }
                }
            } else {
                listViewBtnVisibilityProperty.set(true);
                showRecordTaskStatusLogErrors(response);
            }
        });
        return domainService;
    }
    
    private void registerCurrentWorkingTaskChangeListener() {
        
        this.globalWorkingTaskProperty.addListener(
                (
                        ObservableValue<? extends ProjectTask> observableValue, 
                        ProjectTask previousValue, 
                        ProjectTask nextValue
                ) -> {
            String nextTaskId = nextValue != null ? nextValue.getId() : null;
            boolean palyButtonDisability = !(nextTaskId == null || nextTaskId.equals(projectTask.getId()));
            playPuaseBtnDisableProperty.set(palyButtonDisability);
        });
    }
    
    private void registerGlobalWorkStatusChangeListener(ReadOnlyObjectWrapper<WorkStatusLog.WorkStatus> workStatusProperty) {
        
        workStatusProperty.addListener((
                ObservableValue<? extends WorkStatusLog.WorkStatus> observable, 
                WorkStatusLog.WorkStatus oldValue, 
                WorkStatusLog.WorkStatus newValue) -> {
            
            ProjectTask gloablUserWorkingTask = this.globalWorkingTaskProperty.get();
            String currentWorkingTaskId = gloablUserWorkingTask != null 
                    ? gloablUserWorkingTask.getId() : null;
             
            if (currentWorkingTaskId != null) {
                boolean isWorkOnThisTask = currentWorkingTaskId.equals(projectTask.getId());
                
                if (isWorkOnThisTask && newValue == WorkStatusLog.WorkStatus.BREAK) {
                    Service<Response> pauseTaskService = getTaskStartPauseService();
                    pauseTaskService.start();
                } 
            }
        });
    }
    
    private void showUserWorkStatusBreakAlertBox() {
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Text("Idle State Alert"));
        dialogLayout.setBody(new Text("You are idle state in daily working. "
                + "Before start a task, please start daily working"));
        
        JFXButton yesBtn = new JFXButton("OK, I got it");
        yesBtn.getStyleClass().add("dialog-modal-primary-btn");
        dialogLayout.setActions(yesBtn);
        
        JFXDialog dialog = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.CENTER);
        yesBtn.setOnAction((arg0) -> {
            dialog.close();
        });
        MainScreenController.showJFXDialog(dialog);
    }
    
    private void popupMeetingInfoFormModal() {
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        
        ViewTuple<MeetingInfoModalView, MeetingInfoModalViewModel> viewTuple 
                = FluentViewLoader.fxmlView(MeetingInfoModalView.class).load();
        Node viewNode = viewTuple.getView();
        dialogLayout.setBody(viewNode);
        
        MeetingInfoModalViewModel viewModel = viewTuple.getViewModel();
        viewModel.setMeetingTask(projectTask);
        
        viewModel.setCompanyProjects(companyProjects);
        viewModel.setParentLayout(dialogLayout);
        
        JFXDialog dialog = new JFXDialog(
                null, dialogLayout, JFXDialog.DialogTransition.CENTER
        );
        viewModel.setParentDialog(dialog);
        dialog.setOverlayClose(false);
        MainScreenController.showJFXDialog(dialog);
    }
    
    private void toggleProjectViewDisability(boolean value) {
        try {
            ProjectViewModel projectViewModel  = 
                    StateStorage.getCurrentState(StateName.PROJECT_VIEWMODEL);
            if (projectViewModel != null) {
                projectViewModel.setProjectTaskViewDisability(value);
            }
        } catch (Exception ex) {
            String errorMsg = "Unable to get project view ";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }
        
    }
    
    private void meetingTaskActivityChangeListener() {
        this.globalWorkingTaskProperty.addListener(
                (var obValue, var oldValue, var newValue) -> {
            
            // meeting task start scenario
            if (oldValue == null && newValue != null 
                    && newValue.getType().equals(ProjectTask.MEETING_TASK_TYPE)) {
                spendTimeTimeline.play();
                playBtnImageProperty.set(pauseButtonImage);
            }
            
            // meeting task end scenario
            if (newValue == null && oldValue != null 
                    && oldValue.getType().equals(ProjectTask.MEETING_TASK_TYPE)) {
                spendTimeTimeline.pause();
                playBtnImageProperty.set(playButtonImage);
            }
        });
    }
    
    public Service<Response> startTaskOnSlientMode() {
        
        Service<Response> taskService = new Service<>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        TaskStatusLog.TaskStatus currentStaskStatus = taskStatusProperty.get();
                        
                        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                        Response response = projectTaskModule.recordStartPauseTaskStatus(
                                projectTask, currentStaskStatus
                        );
                        return response;
                    }
                }; 
            };
        };
        
        taskService.setOnRunning((var event) -> {
            playPuaseBtnDisableProperty.set(true);
            toggleProjectViewDisability(true);
        });
        
        taskService.setOnSucceeded((var event) -> {
            
            playPuaseBtnDisableProperty.set(false);

            Gson gson = new Gson();
            Response response = taskService.getValue();
            toggleProjectViewDisability(false);

            if (!response.isError()) {
                JsonElement responseData = response.getData();
                
                TaskStatusLog taskStatusLog = gson.fromJson(responseData, TaskStatusLog.class);
                TaskStatusLog.TaskStatus newTaskStatus = taskStatusLog.getTaskStatus();
                taskStatusProperty.setValue(newTaskStatus);
                
                switch (newTaskStatus) {
                    case START:
                        this.globalWorkingTaskProperty.set(projectTask);
                        playBtnImageProperty.set(pauseButtonImage);
                        spendTimeTimeline.play();
                        break;
                    case BREAK:
                        this.globalWorkingTaskProperty.set(null);
                        playBtnImageProperty.set(playButtonImage);
                        spendTimeTimeline.pause();
                        break;
                    case STOP:
                        spendTimeTimeline.stop();
                        this.globalWorkingTaskProperty.set(null);
                        break;
                }
            }
        });
        return taskService;
    }
    
    void onClickTaskEditBtn(ActionEvent event) {
        
        ProjectViewModel viewModel = ProjectView.getGLOBAL_PROJECTVIEWMODEL();
        viewModel.onClickTaskCreateBtn(event);
        
        TaskCreationModalViewModel taskCreateViewModel 
                = viewModel.getTaskCreationModalViewModel();
        taskCreateViewModel.setProjectTask(projectTask);
        
    }
    public String GetProjectName(String projectId)
    {
        try{
       UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
       String companyId = userSession.getCompanyId();
        //  String deviceId = userSession.
       HttpRequestCaller httpRequestCaller = new HttpRequestCaller();  
       String ProjectName="";
       Response response = httpRequestCaller.getProjectNameByID(projectId, companyId); 
       if(!response.isError())
       {
        JsonObject responseData = response.getData().getAsJsonObject();
        ProjectName= responseData.get("name").getAsString();
       // return responseData.get("timestamp").getAsLong();
       }
       return ProjectName;
        }catch(Exception ec){
        throw ec;
        }
       
//       throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  
    }
}
