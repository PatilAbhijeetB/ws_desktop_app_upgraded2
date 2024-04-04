/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.project;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.Project;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.CommonUtility;
import com.workshiftly.domain.ProjectTaskModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.animation.RotateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;

/**
 *
 * @author hashan
 */
public class ProjectViewModel implements ViewModel {
    private static final InternalLogger LOGGER 
            = LoggerService.getLogger(ProjectViewModel.class);
    
    private static final ReadOnlyObjectWrapper<WorkStatusLog.WorkStatus> GLOBAL_USER_WORKSTATUS 
            = new ReadOnlyObjectWrapper<>();
    private static final SimpleObjectProperty<ProjectTask> CURRENT_WORKING_TASK 
            = new SimpleObjectProperty<>();
    private static final SimpleStringProperty CURRENT_SELECTED_PROJECT_ID
            = new SimpleStringProperty();
     private static final SimpleStringProperty CURRENT_SELECTED_PROJECT_NAME
            = new SimpleStringProperty();
    private static final SimpleBooleanProperty CREATE_TASK_BUTTON_VISIBILITY
            = new SimpleBooleanProperty(false);
    
    private final SimpleStringProperty projectCountLabelTextProperty;
    private final SimpleStringProperty projectNameLabelTextProperty;
    private final SimpleStringProperty selectedProjectIdProperty;
    private final SimpleBooleanProperty isDisabledProjectAndTaskView;
    private final ObservableList<TaskViewModel> taskViewModelList;
    
    private final ObservableList<TaskViewModel> prevTaskListViewList;
    
    private final ObservableList<CompanyProject> companyProjects;
    private final ObservableMap<CompanyProject, ObservableList<TaskViewModel>>
            companyProjectTaskMap;
    
    private final ObservableMap<CompanyProject, ObservableList<TaskViewModel>>
            prevcompanyProjectTaskMap;
    
    private final SimpleBooleanProperty createMeetingBtnVisibility;
    private final SimpleBooleanProperty createMeetingBtnDisability;
    private final SimpleBooleanProperty createTaskButtonVisibility;
    private TaskCreationModalViewModel taskCreationModalViewModel;
    private ListView<TaskViewModel> TaskViewModelListView;
    private ListView<TaskViewModel> PrevTaskViewModelListView;
    private final UserSession userSession;
    
    public ProjectViewModel() throws Exception {
        
        projectCountLabelTextProperty = new SimpleStringProperty("0");
        projectNameLabelTextProperty = new SimpleStringProperty(null);
        selectedProjectIdProperty = new SimpleStringProperty();
        taskViewModelList = FXCollections.observableArrayList();
        prevTaskListViewList= FXCollections.observableArrayList();
        isDisabledProjectAndTaskView = new SimpleBooleanProperty(false);
        createTaskButtonVisibility = new SimpleBooleanProperty(false);
        
        companyProjects = FXCollections.observableArrayList();
        companyProjectTaskMap = FXCollections.observableHashMap();
        prevcompanyProjectTaskMap= FXCollections.observableHashMap();
        GLOBAL_USER_WORKSTATUS.bind(
                AuthenticatedMainWindowViewModel.
                        getUSER_WORK_STATUS_objectProperty()
        );
        
        createMeetingBtnVisibility = new SimpleBooleanProperty(false);
        createMeetingBtnDisability = new SimpleBooleanProperty(true);
        populatedUserAssignedProjects();
        
        userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        registerCurrentProjectIdChangeListener();
    }
    
    public void onProjectChange(Project project) {
        selectedProjectIdProperty.set(project.getId());
    }
    
    public SimpleStringProperty getProjectCountLabelTextProperty() {
        return projectCountLabelTextProperty;
    }

    public SimpleStringProperty getProjectNameLabelTextProperty() {
        return projectNameLabelTextProperty;
    }

    public SimpleStringProperty getSelectedProjectIdProperty() {
        return selectedProjectIdProperty;
    }

    public ObservableList<CompanyProject> getProjectList() {
        return this.companyProjects;
    }

    public void setProjectCountLabelText(String text) {
        this.projectCountLabelTextProperty.set(text);
    }

    public void setProjectNameLabelText(String text) {
        this.projectNameLabelTextProperty.set(text);
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProjectIdProperty.set(selectedProject);
    }

    public ObservableList<TaskViewModel> getTaskViewModelList() {
        return taskViewModelList;
    }
    
    public ObservableList<TaskViewModel> getPrevTaskViewModelList() {
        return prevTaskListViewList;
    }

    public SimpleBooleanProperty getIsDisabledProjectAndTaskView() {
        return isDisabledProjectAndTaskView;
    }
    
    public void setProjectTaskViewDisability(boolean value) {
        isDisabledProjectAndTaskView.set(value);
    }

    public static SimpleObjectProperty<ProjectTask> getCURRENT_WORKING_TASK() {
        return CURRENT_WORKING_TASK;
    }

    public SimpleBooleanProperty getCreateMeetingBtnVisibility() {
        return createMeetingBtnVisibility;
    }

    public SimpleBooleanProperty getCreateMeetingBtnDisability() {
        return createMeetingBtnDisability;
    }

    public static ReadOnlyObjectWrapper<WorkStatusLog.WorkStatus> getGLOBAL_USER_WORKSTATUS() {
        return GLOBAL_USER_WORKSTATUS;
    }

    public TaskCreationModalViewModel getTaskCreationModalViewModel() {
        return taskCreationModalViewModel;
    }

    public void setTaskCreationModalViewModel(TaskCreationModalViewModel viewModel) {
        this.taskCreationModalViewModel = viewModel;
    }

    public ListView<TaskViewModel> getTaskViewModelListView() {
        return TaskViewModelListView;
    }

    public void setTaskViewModelListView(ListView<TaskViewModel> TaskViewModelListView) {
        this.TaskViewModelListView = TaskViewModelListView;
    }

    public ListView<TaskViewModel> getprevTaskViewModelListView() {
        return PrevTaskViewModelListView;
    }

    public void setprevTaskViewModelListView(ListView<TaskViewModel> PrevTaskViewModelListView) {
        this.PrevTaskViewModelListView = PrevTaskViewModelListView;
    }
    public static SimpleBooleanProperty getCreateTaskButtonVisibility() {
        return CREATE_TASK_BUTTON_VISIBILITY;
    }

    public static String getCURRENT_SELECTED_PROJECT_ID() {
        return CURRENT_SELECTED_PROJECT_ID.get();
    }
     public static String getCURRENT_SELECTED_PROJECT_NAME() {
        return CURRENT_SELECTED_PROJECT_NAME.get();
    }
    
    private void populatedUserAssignedProjects() {
        
        Service<Response> service = new Service<Response>() {
            @Override
            protected javafx.concurrent.Task<Response> createTask() {
                return new javafx.concurrent.Task<>() {
                    @Override
                    protected Response call() throws Exception {
                        StateStorage.set(
                                StateName.IS_SUCCESS_GET_REMOTE_PROJECT_TASK, 
                                Boolean.class, 
                                false
                        );
                        companyProjects.clear();
                        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                        return projectTaskModule.getUserAssignedProjects();
                    }
                };
            };
        };
        
        service.setOnReady((WorkerStateEvent arg0) -> {
            MainScreenController.showLoadingSplashScreen();
        });
        
        service.setOnFailed((var stateEvent) -> {
            MainScreenController.hideLoadingSplashScreen();
            
            PopupDialogHeading dialogHeading = new PopupDialogHeading(
                    PopupDialogHeading.PopupType.ERROR
            );
            dialogHeading.setHeadingTxt("Unable to Retrieve project");
            
            PopupDialogBox dialogBox = new PopupDialogBox(dialogHeading);
            dialogBox.setDescription(
                    PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                    "Unexpected error occured while retrieving projects and tasks"
            );
            JFXButton retryBtn = dialogBox
                    .getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
            retryBtn.setText("Retry");
            retryBtn.setVisible(true); retryBtn.setDisable(false);
            retryBtn.setOnAction((var actionEvent) -> {
                dialogBox.close();
                service.cancel();
                populatedUserAssignedProjects();
            });
        });
        
        service.setOnSucceeded((WorkerStateEvent arg0) -> {
            Gson gson = new Gson();
            MainScreenController.hideLoadingSplashScreen();
            Response response = service.getValue();
            
            if (!response.isError()) {
                Type projectListType = new TypeToken<List<CompanyProject>>(){}.getType();
                List<CompanyProject> userProjects 
                        = gson.fromJson(response.getData(), projectListType);
                userProjects = userProjects.stream().filter((var project) -> {
                    return CommonUtility.parseToBoolean(project.getIsActive());
                }).collect(Collectors.toList());
                this.companyProjects.clear();
                this.companyProjects.setAll(userProjects);
                long CurrentsunixTimestamp = Instant.now().getEpochSecond();
    
                try {
                    companyProjects.forEach((CompanyProject project) -> {
                        List<ProjectTask> projectTasks = project.getProjectTasks();
                        ObservableList<TaskViewModel> viewModels
                                = FXCollections.observableArrayList();
                        
                         projectTasks.forEach((ProjectTask task) -> {
                            TaskViewModel viewModel = new TaskViewModel(
                                    task, GLOBAL_USER_WORKSTATUS
                            );
                            viewModel.setCompanyProjects(companyProjects);
                             String strTaskType=task.getType();
                             if("meeting".equals(strTaskType))
                             {
                              viewModels.add(viewModel);
                             }
                            if (task.getDueDate() != null && task.getDueDate() != 0){
                                long unixTimestamp1 = task.getDueDate(); // Replace this with your first Unix timestamp
                                 long unixTimestamp2 = CurrentsunixTimestamp; // Replace this with your second Unix timestamp
                               
                                 // Convert Unix timestamps to LocalDate
                                LocalDate date1 = Instant.ofEpochSecond(unixTimestamp1)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();

                                LocalDate date2 = Instant.ofEpochSecond(unixTimestamp2)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();
                                LocalDate currentDate = LocalDate.now();
                                if (date1.isEqual(currentDate) || date1.isAfter(currentDate))
                            {
                                    viewModels.add(viewModel);
                                }
                            }
                         });
                        
                         List<ProjectTask> prevprojectTasks = project.getProjectTasks();
                        ObservableList<TaskViewModel> prevviewModels
                                = FXCollections.observableArrayList();
                        
                        
                        prevprojectTasks.forEach((ProjectTask task) -> {
                            TaskViewModel prevviewModel = new TaskViewModel(
                                    task, GLOBAL_USER_WORKSTATUS
                            );
                            prevviewModel.setCompanyProjects(companyProjects);
                           if (task.getDueDate() != null && task.getDueDate() != 0){
                               long unixTimestamp1 = task.getDueDate(); // Replace this with your first Unix timestamp
                                 long unixTimestamp2 = CurrentsunixTimestamp; // Replace this with your second Unix timestamp
        
                                 // Convert Unix timestamps to LocalDate
                                LocalDate date1 = Instant.ofEpochSecond(unixTimestamp1)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();

                                LocalDate date2 = Instant.ofEpochSecond(unixTimestamp2)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();
                                LocalDate currentDate = LocalDate.now();
                                if (date1.isBefore(currentDate))
                           {
                                    prevviewModels.add(prevviewModel);
                                }
                            }
                          //  viewModels.add(viewModel);
                        });
                        
                        companyProjectTaskMap.put(project, viewModels);
                        prevcompanyProjectTaskMap.put(project, prevviewModels);
                    });
                    
                    if (companyProjects.isEmpty()) {
                        CREATE_TASK_BUTTON_VISIBILITY.set(false);
                    } else {
                        CompanyProject generalProject = companyProjects.stream().filter((CompanyProject curProject) -> {
                            String projectType = curProject.getType();
                            return projectType != null 
                                    && projectType.equals(CompanyProject.GENERAL_PROJECT_TYPE);
                        }).findFirst().orElse(null);

                        List<ProjectTask> projectTasks = generalProject == null 
                                ? new ArrayList() : generalProject.getProjectTasks();
                        ProjectTask meetingTask = projectTasks.stream().filter((ProjectTask curTask) -> {
                            String taskType = curTask.getType();
                            return taskType != null && taskType.equals(ProjectTask.MEETING_TASK_TYPE);
                        }).findFirst().orElse(null);
                        boolean isAbleToCreate = meetingTask == null || userSession.isIsAllowOfflineTask();
                        CREATE_TASK_BUTTON_VISIBILITY.set(isAbleToCreate);
                    }
                } catch (Exception ex) {
                    InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
                    String msg = "Error occurred while populating project and tasks";
                    LOGGER.logRecord(logLevel, msg, ex);
                    // TODO: Exception handling here
                }
                
                int numberOfAssignedProjects = companyProjects.size();
                projectCountLabelTextProperty.set(
                        Integer.toString(numberOfAssignedProjects)
                );
                
            }
            
            // error handling scenraios
        });
        service.start();
    }
    
    public void onClickProjectRefreshBtn(
            RotateTransition rotateTransition, 
            ListView<TaskViewModel> listView
    ) {
        try {
            Service<Response> domainService = getRefreshProjectAndTasksService();
            
            domainService.setOnRunning((WorkerStateEvent stateEvent) -> {
                rotateTransition.play();
                isDisabledProjectAndTaskView.set(true);
            });
        
            domainService.setOnSucceeded((new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent stateEvent) {
                    Gson gson = new Gson();
                    Response domainResponse = domainService.getValue();
                    isDisabledProjectAndTaskView.set(false);
                    rotateTransition.pause();
                    rotateTransition.setByAngle(0);
                    
                    if (domainResponse.isError()) {
                        PopupDialogBox popupDialogBox = new PopupDialogBox();

                        PopupDialogHeading headingComponent = new PopupDialogHeading(
                                PopupDialogHeading.PopupType.ERROR
                        );
                        headingComponent.setHeadingTxt("Unable to Refresh Projects");
                        popupDialogBox.setHeadingComponent(headingComponent);

                        popupDialogBox.setDescription(
                                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                                domainResponse.getMessage()
                        );

                        JFXButton closeBtn = popupDialogBox
                                .getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
                        closeBtn.setText("Close");
                        closeBtn.setVisible(true);
                        closeBtn.setOnAction((var event) -> { 
                            popupDialogBox.getjFXDialog().close();
                        });
                        popupDialogBox.load();
                        return;
                    }
                    
                    Type projectListType = new TypeToken<List<CompanyProject>>(){}.getType();
                    List<CompanyProject> refreshProjects = gson
                            .fromJson(domainResponse.getData(), projectListType);

                    List<String> existingProjectIds = companyProjects.stream()
                            .map(CompanyProject::getId).collect(Collectors.toList());
                    List<String> refreshedProjectIds = refreshProjects.stream()
                            .map(CompanyProject::getId).collect(Collectors.toList());

                    ObservableMap<CompanyProject, ObservableList<TaskViewModel>> 
                            tempProjectTaskMap = FXCollections.observableHashMap();
                    
                    ObservableMap<CompanyProject, ObservableList<TaskViewModel>> 
                            prevtempProjectTaskMap = FXCollections.observableHashMap();

                    String projectIdBeforeRefresh = selectedProjectIdProperty.getValue();
                    boolean hadProjectBeforeRefresh = projectIdBeforeRefresh != null 
                            && !projectIdBeforeRefresh.isEmpty();
                    
                    // remove unassigned projects
                    boolean isRemoved = companyProjects.removeIf((CompanyProject project) -> {
                        return !refreshedProjectIds.contains(project.getId());
                    });
                    
                    for (CompanyProject currentProject : refreshProjects) {
                        String currentProjectId = currentProject.getId();
                        boolean isActiveProject = 
                                CommonUtility.parseToBoolean(currentProject.getIsActive());
                        boolean isGeneralProject = currentProject.getType() != null 
                                && currentProject.getType().equals(CompanyProject.GENERAL_PROJECT_TYPE);
                        
                        boolean isContainedTasks = isGeneralProject 
                                || (currentProject.getProjectTasks() != null 
                                && !currentProject.getProjectTasks().isEmpty());
                        
                        if (!existingProjectIds.contains(currentProjectId) && !isContainedTasks) {
                            continue;
                        }
                        
                        if (existingProjectIds.contains(currentProjectId) 
                                && !(isActiveProject && isContainedTasks)) {
                            CompanyProject inActiveProject = companyProjects.stream()
                                    .filter((CompanyProject element) -> {
                                        String projectId = element.getId();
                                        if (projectId != null) {
                                            return projectId.equals(currentProjectId);
                                        }
                                        return false;
                            }).findFirst().orElse(null);
                            
                            if (inActiveProject != null) {
                                companyProjects.remove(inActiveProject);
                                continue;
                            }
                        }
                        
                        if (!existingProjectIds.contains(currentProjectId)) {
                            companyProjects.add(currentProject);
                        }
                        
                        ObservableList<TaskViewModel> taskViewModels 
                                = FXCollections.observableArrayList();
                        List<ProjectTask> projectTasks = currentProject.getProjectTasks();
                        long currentunixTimestamp = Instant.now().getEpochSecond();
   
                        for (ProjectTask currentTask : projectTasks) {
                            TaskViewModel currentTaskViewModel 
                                    = new TaskViewModel(currentTask, GLOBAL_USER_WORKSTATUS);
                            currentTaskViewModel.setCompanyProjects(companyProjects);
                            String strTaskType=currentTask.getType();
                            projectNameLabelTextProperty.set("Project -"+currentTask.getProjectName());
                            if("meeting".equals(strTaskType))
                             {
                              taskViewModels.add(currentTaskViewModel);
                             }
                            if (currentTask.getDueDate() != null && currentTask.getDueDate() != 0){
                               long unixTimestamp1 = currentTask.getDueDate(); // Replace this with your first Unix timestamp
                                 long unixTimestamp2 = currentunixTimestamp; // Replace this with your second Unix timestamp
                               
                                 // Convert Unix timestamps to LocalDate
                                LocalDate date1 = Instant.ofEpochSecond(unixTimestamp1)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();

                                LocalDate date2 = Instant.ofEpochSecond(unixTimestamp2)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();
                                 LocalDate currentDate = LocalDate.now();
                                if (date1.isEqual(currentDate) || date1.isAfter(currentDate))                                 
                                {
                                taskViewModels.add(currentTaskViewModel);
                                }
                                
                            }
                        }
                        
                        tempProjectTaskMap.put(currentProject, taskViewModels);
                        
                        if (hadProjectBeforeRefresh && currentProjectId.equals(projectIdBeforeRefresh)) {
                            taskViewModelList.clear();
                            taskViewModelList.addAll(taskViewModels);
                            
                            listView.refresh();
                        }
                        
                         ObservableList<TaskViewModel> prevTaskViewModels 
                                = FXCollections.observableArrayList();
                        List<ProjectTask> prevprojectTasks = currentProject.getProjectTasks();
                       
   
                        for (ProjectTask prevcurrentTask : prevprojectTasks) {
                            TaskViewModel prevcurrentTaskViewModel 
                                    = new TaskViewModel(prevcurrentTask, GLOBAL_USER_WORKSTATUS);
                            prevcurrentTaskViewModel.setCompanyProjects(companyProjects);
                            if (prevcurrentTask.getDueDate() != null && prevcurrentTask.getDueDate() != 0){
                                long unixTimestamp1 = prevcurrentTask.getDueDate(); // Replace this with your first Unix timestamp
                                 long unixTimestamp2 = currentunixTimestamp; // Replace this with your second Unix timestamp
        
                                 // Convert Unix timestamps to LocalDate
                                LocalDate date1 = Instant.ofEpochSecond(unixTimestamp1)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();

                                LocalDate date2 = Instant.ofEpochSecond(unixTimestamp2)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDate();
                                LocalDate currentDate = LocalDate.now();
                                if (date1.isBefore(currentDate))                                 
                                {
                                prevTaskViewModels.add(prevcurrentTaskViewModel);
                                }
                            }
                        }
                        
                        prevtempProjectTaskMap.put(currentProject, prevTaskViewModels);
                        
                        if (hadProjectBeforeRefresh && currentProjectId.equals(projectIdBeforeRefresh)) {
                            prevTaskListViewList.clear();
                            prevTaskListViewList.addAll(prevTaskViewModels);
                            
                            listView.refresh();
                        }
                    }

                    companyProjectTaskMap.clear();
                    companyProjectTaskMap.putAll(tempProjectTaskMap);
                    
                    prevcompanyProjectTaskMap.clear();
                    prevcompanyProjectTaskMap.putAll(prevtempProjectTaskMap);
                    
                    int numberOfAssignedProjects = companyProjects.size();
                    projectCountLabelTextProperty.set(Integer.toString(numberOfAssignedProjects));
                }
            }));
            domainService.start();
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Unable to refresh projects", ex);
        }
    }
    
    private Service<Response> getRefreshProjectAndTasksService() {
        
        Service<Response> domainService = new Service<Response>() {
            @Override
            protected javafx.concurrent.Task<Response> createTask() {
                return new javafx.concurrent.Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        Response workingTaskUpdate = updateCurrentWorkTaskSpentTime();
                        
                        if (workingTaskUpdate.isError()) {
                            return workingTaskUpdate;
                        }
                        
                        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                        Response domainResponse = projectTaskModule.refreshProjectAndTask();
                        return domainResponse;
                    }
                };
            }
        };
        return domainService;
    }
    
    /****
     * Handle create meeting task for current selected project
     * @param project 
     */
    void createProjectMeetingTask(CompanyProject project) {
        
        Service<Response> createMeetingTaskService = new Service<Response>() {
            @Override
            protected javafx.concurrent.Task<Response> createTask() {
                return new javafx.concurrent.Task<>() {
                    @Override
                    protected Response call() throws Exception {
                        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                        
                        Response moduleResponse = projectTaskModule
                                .createEmployeeMeetingTask(project);
                        return moduleResponse;
                    }
                };
            }
        };
        
        createMeetingTaskService.setOnReady(((WorkerStateEvent event) -> {
            createMeetingBtnDisability.set(true);
            createMeetingBtnVisibility.set(false);
            MainScreenController.showLoadingSplashScreen();
        }));
        
        createMeetingTaskService.setOnSucceeded(((WorkerStateEvent event) -> {
            MainScreenController.hideLoadingSplashScreen();
            Response moduleResponse = createMeetingTaskService.getValue();
            createMeetingBtnDisability.set(false);
            
            if (!moduleResponse.isError()) {
                JsonElement responseData = moduleResponse.getData();
                ProjectTask createdTask = new Gson().fromJson(
                        responseData, ProjectTask.class
                );
                List<ProjectTask> projectTasks = project.getProjectTasks();
                projectTasks.add(createdTask);
                
                TaskViewModel viewModel = new TaskViewModel(createdTask, GLOBAL_USER_WORKSTATUS);
                viewModel.setCompanyProjects(companyProjects);
                
                Map.Entry<CompanyProject, ObservableList<TaskViewModel>> projectMapEntry = 
                        companyProjectTaskMap.entrySet().stream().filter((var curMapEntry) -> {
                            CompanyProject curProject = curMapEntry.getKey();
                            return curProject.getId().equals(project.getId());
                }).findFirst().orElse(null);
                
                ObservableList<TaskViewModel> taskViewModels = projectMapEntry.getValue();
                
                if (taskViewModels != null) {
                    taskViewModels.add(viewModel);
                    boolean didAdded = taskViewModelList.add(viewModel);
                    createMeetingBtnVisibility.set(false);
                    
                    if (!didAdded) {
                        moduleResponse.setError(true);
                        String errorMsg = "Application error occurred.";
                        moduleResponse.setMessage(errorMsg);
                    } else {
                        PopupDialogBox popupDialogBox = new PopupDialogBox();
                        PopupDialogHeading popupHeading = new PopupDialogHeading(
                                PopupDialogHeading.PopupType.SUCCESS, 
                                "Task Creation Successfully"
                        );
                        popupDialogBox.setHeadingComponent(popupHeading);
                        
                        popupDialogBox.setDescription(
                                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                                "A meeting task was created successfully under General Work project. "
                                        + "You may use this task to record meetings"
                        );
                        JFXButton closeBtn = popupDialogBox.getDialogButton(
                                PopupDialogBox.DialogButton.RIGHT_MOST
                        );
                        closeBtn.setVisible(true); closeBtn.setDisable(false);
                        closeBtn.setText("OK, I noted");
                        closeBtn.setOnAction((var btnEvent) -> {
                            popupDialogBox.close();
                        });
                        popupDialogBox.load();
                    }
                }
            }
            // prompt create meeting task response error and success
            if (moduleResponse.isError()) {
                createMeetingBtnVisibility.set(true);
                handlePopupCreateMeetingTask(moduleResponse);
            }
        }));
        createMeetingTaskService.start();
    }
    
    /****
     * handle success and error response which dispatches from module response
     * for createProjectMeetingTask method
     * @param response 
     */
    private void handlePopupCreateMeetingTask(Response response) {
        try {
            PopupDialogHeading.PopupType popupType = response.isError() 
                ? PopupDialogHeading.PopupType.ERROR 
                : PopupDialogHeading.PopupType.SUCCESS;
            PopupDialogHeading dialogHeading = new PopupDialogHeading(popupType);
            
            String headingTxt = response.isError() ? "Error" : "Success";
            dialogHeading.setHeadingTxt(headingTxt);

            String responseMsg = response.getMessage();
            PopupDialogBox popupDialogBox = new PopupDialogBox(dialogHeading);
            popupDialogBox.setDescription(
                    PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                    responseMsg
            );
            JFXButton closeBtn= popupDialogBox.getDialogButton(
                    PopupDialogBox.DialogButton.RIGHT_MOST
            );
            closeBtn.setText("Close");
            
            closeBtn.setOnAction((var event) -> {
                popupDialogBox.getjFXDialog().close();
            });
            closeBtn.setVisible(true);
            popupDialogBox.load();
        } catch (Exception ex) {
            InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
            String errorTxt = "Failed to display handlePopupCreateMeetingTask popup";
            LOGGER.logRecord(logLevel, errorTxt, ex);
        }
    }
    
    /**
     * Method: updateCurrentWorkTaskSpentTime
     * Description: Update current working task spend time if current working task is present
     * Return: Response object indicates whether execution is success or not and error
     *         details
     * @return 
     */
    public Response updateCurrentWorkTaskSpentTime() throws Exception {
        ProjectTask currentWorkingTask = CURRENT_WORKING_TASK.get();
        
        if (currentWorkingTask == null) {
            return new Response(false, StatusCode.SUCCESS, "No working task at the moment");
        }

        Set<CompanyProject> _companyProjects = companyProjectTaskMap.keySet();
        CompanyProject workingProject = _companyProjects.stream().filter((var item) -> {
            String itemId = item.getId();
            return itemId.equals(currentWorkingTask.getProjectId());
        }).findFirst().orElse(null);
        
        if (workingProject == null) {
            return new Response(
                    true, StatusCode.PROJECT_NOT_FOUND, "Your working project not found"
            );
        }

        ObservableList<TaskViewModel> taskViewModels = 
                companyProjectTaskMap.get(workingProject);
        TaskViewModel _taskViewModel = taskViewModels.stream()
                .filter((var taskViewModel) -> {
            ProjectTask task = taskViewModel.getTask();
                    return task.getId() != null && 
                            task.getId().equals(currentWorkingTask.getId());
        }).findFirst().orElse(null);
        
        if (_taskViewModel == null) {
            return new Response(
                    true, StatusCode.TASK_VIEWMODEL_NOT_FOUND, 
                    "View element of working task not found"
            );
        }

        Duration spendTimeDuration = _taskViewModel.getSpendTimeDuration();
        currentWorkingTask.setSpentTime(spendTimeDuration.toSeconds());

        ProjectTaskModule projectTaskModule = new ProjectTaskModule();
        Response taskUpdate = projectTaskModule.updateTaskSpendTime(
                currentWorkingTask
        );
        
        if (!taskUpdate.isError()) {
            StateStorage.set(StateName.RECENT_WORKING_TASK, ProjectTask.class, currentWorkingTask);
        }
        return taskUpdate;
    }
    
    public Response startRecentTaskOnSlientMode(ProjectTask projectTask) {
        
        if (projectTask == null) {
            return new Response(true, StatusCode.TASK_NOT_FOUND, "Unable to update null reference task");
        }
        
        Set<CompanyProject> _comCompanyProjects = companyProjectTaskMap.keySet();
        CompanyProject companyProject = _comCompanyProjects.stream().filter((CompanyProject element) -> {
            String elementId = element.getId();
            return elementId != null && elementId.equals(projectTask.getProjectId());
        }).findFirst().orElse(null);
        
        if (companyProject == null) {
            String errorMsg = "Could not able to find project related to previous working task";
            return new Response(true, StatusCode.PROJECT_NOT_FOUND, errorMsg);
        }
        
        ObservableList<TaskViewModel> taskViewModels = companyProjectTaskMap.get(companyProject);
        TaskViewModel viewModel;
        viewModel = taskViewModels.stream().filter((TaskViewModel taskViewModel) -> {
            ProjectTask curTask = taskViewModel.getTask();
            String taskId = curTask.getId();
            return taskId != null && taskId.equals(projectTask.getId());
        }).findFirst().orElse(null);
        
        if (viewModel == null) {
            String errorMsg = "View element of working task not found";
            return new Response(true, StatusCode.TASK_VIEWMODEL_NOT_FOUND, errorMsg);
        }
        
        Service<Response> taskStartPauseService = viewModel.startTaskOnSlientMode();
        taskStartPauseService.start();
        
        return new Response(false, StatusCode.SUCCESS, "Successful");
    }
    
    public void onClickTaskCreateBtn(ActionEvent event) {
        ViewTuple<TaskCreationModalView, TaskCreationModalViewModel> taskCreationViewTuple 
                = FluentViewLoader.fxmlView(TaskCreationModalView.class).load();
        
        Parent parentView = taskCreationViewTuple.getView();
        TaskCreationModalViewModel parentViewModel = taskCreationViewTuple.getViewModel();
        setTaskCreationModalViewModel(parentViewModel);
        
        JFXDialog dialogComponent = new JFXDialog(
                null, (Region) parentView, JFXDialog.DialogTransition.NONE
        );
        parentViewModel.addCompanyProjects(companyProjects);
        parentViewModel.setTaskViewModelMap(companyProjectTaskMap);
        parentViewModel.setParentDialog(dialogComponent);
        parentViewModel.setParentTaskViewModelListView(TaskViewModelListView);
        parentViewModel.setParentRenderedTaskList(taskViewModelList);
        
        MainScreenController.showJFXDialog(dialogComponent);
        
    }
    
    private void registerCurrentProjectIdChangeListener() {
        
        selectedProjectIdProperty.addListener((
                ObservableValue<? extends String> observable, 
                String previousProjectId, 
                String nextProjectId
        ) -> {
            CURRENT_SELECTED_PROJECT_ID.set(nextProjectId);
            boolean isEmptyTaskViewModelList = taskViewModelList.isEmpty();
            if (!isEmptyTaskViewModelList) {
                taskViewModelList.clear();
                prevTaskListViewList.clear();
            }
            
            for (Map.Entry<CompanyProject, ObservableList<TaskViewModel>> currentProjectTaskEntry 
                    : companyProjectTaskMap.entrySet()) {
                
                CompanyProject currentProject = currentProjectTaskEntry.getKey();
                String currentProjectId = currentProject.getId();
                
                if (nextProjectId != null && currentProjectId.equals(nextProjectId)) {
                   // String currentProjectName = currentProject.getName().toUpperCase();
                    String currentProjectName = currentProject.getName();
                    CURRENT_SELECTED_PROJECT_NAME.set(currentProjectName);
                    projectNameLabelTextProperty.set(currentProjectName);
                    List<TaskViewModel> nextTaskViewModels = currentProjectTaskEntry.getValue();
                    //List<TaskViewModel> prevnextTaskViewModels = currentProjectTaskEntry.getValue();
                    boolean didAddedNewTaskModels = taskViewModelList.addAll(nextTaskViewModels);
                   // boolean prevdidAddedNewTaskModels = prevTaskListViewList.addAll(prevnextTaskViewModels);
                    
                    if (!didAddedNewTaskModels) {
                        // TODO:error handling code should be included
                    }
                    break;
                }
            }
             for (Map.Entry<CompanyProject, ObservableList<TaskViewModel>> currentProjectTaskEntry 
                    : prevcompanyProjectTaskMap.entrySet()) {
                
                CompanyProject currentProject = currentProjectTaskEntry.getKey();
                String currentProjectId = currentProject.getId();
                
                if (nextProjectId != null && currentProjectId.equals(nextProjectId)) {
                   // String currentProjectName = currentProject.getName().toUpperCase();
                    String currentProjectName = currentProject.getName();
                    projectNameLabelTextProperty.set(currentProjectName);
                   // List<TaskViewModel> nextTaskViewModels = currentProjectTaskEntry.getValue();
                    List<TaskViewModel> prevnextTaskViewModels = currentProjectTaskEntry.getValue();
                 //  boolean didAddedNewTaskModels = taskViewModelList.addAll(nextTaskViewModels);
                    boolean prevdidAddedNewTaskModels = prevTaskListViewList.addAll(prevnextTaskViewModels);
                    
                   
                    break;
                }
            }
        });
    }

}
