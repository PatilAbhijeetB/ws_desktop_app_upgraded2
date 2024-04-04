/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.mainwindow.project;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.ProjectTaskType;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.presentation.service.TaskCreateService;
import de.saxsys.mvvmfx.ViewModel;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;

/**
 *
 * @author jade_m
 */
public class TaskCreationModalViewModel implements ViewModel {

    private JFXDialog parentDialog;
    private ObservableMap<CompanyProject, ObservableList<TaskViewModel>> taskViewModelMap;

    private final ObservableList<ProjectTaskType> projectTaskTypes;
    private final ObservableList<CompanyProject> companyProjects;
    private final SimpleBooleanProperty projectSelectorDisability;
    private final SimpleBooleanProperty createBtnDisability;
    private final SimpleBooleanProperty projectTypeSelectorDisability;

    private final SimpleObjectProperty<ProjectTaskType> selectedTaskType;
    private final SimpleObjectProperty<CompanyProject> selectedProject;
    private final SimpleStringProperty taskTitle;
    private final SimpleStringProperty taskDescription;
     private final SimpleStringProperty projectName;
    private final SimpleStringProperty formErrorTxt;
    private final SimpleObjectProperty<LocalDate> taskDueDate;
    private final SimpleStringProperty hoursEstimationValue;
    private final SimpleStringProperty minutesEstimationValue;
    private final SimpleStringProperty daysEstimationValue;
    private final SimpleStringProperty createBtnTxt;

    private ProjectTask projectTask;
    private ListView<TaskViewModel> parentTaskViewModelListView;
    private ObservableList<TaskViewModel> parentRenderedTaskList;
    private final UserSession userSession;

    public TaskCreationModalViewModel() {

        // property initialization
        this.projectTaskTypes = FXCollections.observableArrayList();
        this.selectedProject = new SimpleObjectProperty<>();
        this.companyProjects = FXCollections.observableArrayList();
        this.projectSelectorDisability = new SimpleBooleanProperty(false);
        this.createBtnDisability = new SimpleBooleanProperty(false);
        this.projectTypeSelectorDisability = new SimpleBooleanProperty(false);

        this.selectedTaskType = new SimpleObjectProperty<>(null);
        this.taskTitle = new SimpleStringProperty(null);
        this.taskDescription = new SimpleStringProperty(null);
        this.projectName = new SimpleStringProperty(null);
        this.taskDueDate = new SimpleObjectProperty<>();
        this.hoursEstimationValue = new SimpleStringProperty("");
        this.minutesEstimationValue = new SimpleStringProperty("");
        this.daysEstimationValue = new SimpleStringProperty("");
        this.createBtnTxt = new SimpleStringProperty("Add Task");
        this.formErrorTxt = new SimpleStringProperty("");

        userSession = StateStorage.getCurrentState(StateName.USER_SESSION);

        // populate project task types
        populateProjectTaskTypes();

        // task type change listener
        this.selectedTaskType.addListener((var obValue, var oldValue, var newValue) -> {
            if (newValue != null && newValue.getKey().equals(ProjectTask.MEETING_TASK_TYPE)) {
                this.projectSelectorDisability.set(true);
                CompanyProject generalProject = this.companyProjects.stream()
                        .filter((CompanyProject element) -> {
                            String projectType = element.getType();
                            return projectType != null
                                    && projectType.equals(CompanyProject.GENERAL_PROJECT_TYPE);
                        }).findFirst().orElse(null);
                this.selectedProject.set(generalProject);
            } else {
                this.projectSelectorDisability.set(false);
            }
        });
    }

    public ObservableList<ProjectTaskType> getProjectTaskTypes() {
        return projectTaskTypes;
    }

    public ObservableList<CompanyProject> getCompanyProjects() {
        return companyProjects;
    }

    public SimpleBooleanProperty getProjectSelectorDisability() {
        return projectSelectorDisability;
    }

    public SimpleObjectProperty<ProjectTaskType> getSelectedTaskType() {
        return selectedTaskType;
    }

    public SimpleObjectProperty<CompanyProject> getSelectedProject() {
        return selectedProject;
    }

    public void setParentDialog(JFXDialog parentDialog) {
        this.parentDialog = parentDialog;
    }

    public void setTaskViewModelMap(
            ObservableMap<CompanyProject, ObservableList<TaskViewModel>> taskViewModelMap) {
        this.taskViewModelMap = taskViewModelMap;
    }

    public SimpleStringProperty getTaskTitle() {
        return taskTitle;
    }

    public SimpleStringProperty getTaskDescription() {
        return taskDescription;
    }
      public SimpleStringProperty getprojectName() {
        return projectName;
    }

    public SimpleObjectProperty<LocalDate> getTaskDueDate() {
        return taskDueDate;
    }

    public SimpleStringProperty getHoursEstimationValue() {
        return hoursEstimationValue;
    }

    public SimpleStringProperty getMinutesEstimationValue() {
        return minutesEstimationValue;
    }

    public SimpleStringProperty getDaysEstimationValue() {
        return daysEstimationValue;
    }

    public SimpleBooleanProperty getCreateBtnDisability() {
        return createBtnDisability;
    }

    public SimpleStringProperty getCreateBtnTxt() {
        return createBtnTxt;
    }

    public ProjectTask getProjectTask() {
        return projectTask;
    }

    public SimpleBooleanProperty getProjectTypeSelectorDisability() {
        return projectTypeSelectorDisability;
    }

    public void setProjectTask(ProjectTask projectTask) {
        Gson gson = new Gson();
        JsonElement jsonObj = gson.toJsonTree(projectTask);
        this.projectTask = projectTask == null
                ? null
                : gson.fromJson(jsonObj, ProjectTask.class);
        bindFormDataToView(projectTask);
    }

    public SimpleStringProperty getFormErrorTxt() {
        return formErrorTxt;
    }

    public void setParentTaskViewModelListView(ListView<TaskViewModel> parentListView) {
        this.parentTaskViewModelListView = parentListView;
    }

    public void setParentRenderedTaskList(ObservableList<TaskViewModel> renderedList) {
        this.parentRenderedTaskList = renderedList;
    }

    private void bindFormDataToView(ProjectTask projectTask) {

        if (projectTask == null) {
            this.projectTask = new ProjectTask();
            createBtnTxt.set("Add Task");
            return;
        }

        createBtnTxt.set("Update Task");
        projectTypeSelectorDisability.set(true);
        String taskType = projectTask.getType();
        if (taskType != null && !taskType.isEmpty()) {
            ProjectTaskType _taskType = projectTaskTypes.stream()
                    .filter((ProjectTaskType curType) -> {
                        return curType.getKey() != null
                                && curType.getKey().equals(projectTask.getType());
                    }).findFirst().orElse(null);

            if (_taskType != null) {
                selectedTaskType.set(_taskType);
            }
        }

        projectSelectorDisability.set(true);
        String projectId = projectTask.getProjectId();
        if (projectId != null && !projectId.isEmpty()) {
            CompanyProject project = companyProjects.stream()
                    .filter((CompanyProject curProject) -> {
                        String curId = curProject.getId();
                        return curId != null && curId.equals(projectId);
                    }).findFirst().orElse(null);
            if (project != null) {
                selectedProject.set(project);
            }
        }

        taskTitle.set(
                projectTask.getTitle() != null ? projectTask.getTitle() : "");
        taskDescription.set(
                projectTask.getDescription() != null ? projectTask.getDescription() : "");

        Long dueDate = projectTask.getDueDate();
        if (dueDate != null && dueDate > 0) {
            taskDueDate.set(
                    Instant.ofEpochSecond(dueDate)
                            .atOffset(ZoneOffset.UTC).toLocalDate());
        }

        Long estimateTime = projectTask.getEstimate();
        if (estimateTime != null && estimateTime > 0) {
            minutesEstimationValue.set(Long.toString(
                    Duration.ofSeconds(estimateTime).toMinutes()));
            setCalculatedEstimationValues();
        }
    }

    // populate project task types
    private void populateProjectTaskTypes() {

        List<ProjectTask> projectTasks = new ArrayList<>();
        companyProjects.forEach((CompanyProject curProject) -> {
            projectTasks.addAll(curProject.getProjectTasks());
        });
        ProjectTask meetingTask = projectTasks.stream().filter((ProjectTask curTask) -> {
            String taskType = curTask.getType();
            return taskType != null && taskType.equals(ProjectTask.MEETING_TASK_TYPE);
        }).findFirst().orElse(null);

        if (meetingTask == null) {
            ProjectTaskType meetingTaskType = new ProjectTaskType(ProjectTask.MEETING_TASK_TYPE, "Meeting", true);
            projectTaskTypes.add(meetingTaskType);
        }

        if (userSession.isIsAllowOfflineTask()) {
            ProjectTaskType offlineTaskTaskType = new ProjectTaskType(ProjectTask.OFFLINE_TASK_TYPE, "Offline/Manual",
                    true);

            projectTaskTypes.add(offlineTaskTaskType);
        }
    }

    // add company projects into companyProjects observerList
    public void addCompanyProjects(List<CompanyProject> companyProjects) {
        this.companyProjects.addAll(companyProjects);

        CompanyProject generalProject = this.companyProjects.stream()
                .filter((CompanyProject element) -> {
                    String projectType = element.getType();
                    return projectType != null && projectType.equals(
                            CompanyProject.GENERAL_PROJECT_TYPE);
                }).findFirst().orElse(null);

        if (generalProject != null) {
            List<ProjectTask> projectTasks = generalProject.getProjectTasks();
            ProjectTask meetingTask = projectTasks.stream().filter(((ProjectTask element) -> {
                return element.getType().equals(ProjectTask.MEETING_TASK_TYPE);
            })).findFirst().orElse(null);

          //  if (meetingTask != null) {
          //      this.projectTaskTypes.removeIf((ProjectTaskType element) -> {
          //          return element.getKey().equals(ProjectTask.MEETING_TASK_TYPE);
          //      });
          //  }
        }
    }

    // cancelBtn event handler
    public void onClickCancelBtn(ActionEvent event) {
        if (this.parentDialog != null) {
            this.parentDialog.close();
        }
    }

    // createBtn event handler
    public void onClickCreateBtn(ActionEvent event) {
        boolean isNewTask = projectTask == null;
        projectTask = projectTask != null ? projectTask : new ProjectTask();
        projectTask.setTitle(this.taskTitle.get());
        projectTask.setDescription(this.taskDescription.get());
        projectTask.setProjectName(this.selectedProject.getName());
        int minutes = this.minutesEstimationValue.get() != null
                && !this.minutesEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.minutesEstimationValue.get())
                        : 0;
        Duration minutesDuration = Duration.ofMinutes(minutes);

        int hours = this.hoursEstimationValue.get() != null
                && !this.hoursEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.hoursEstimationValue.get())
                        : 0;
        Duration hoursDuration = Duration.ofHours(hours);

        int days = this.daysEstimationValue.get() != null
                && !this.daysEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.daysEstimationValue.get())
                        : 0;
        Duration daysDuration = Duration.ofDays(days);

        Duration totalDuration = minutesDuration.plus(hoursDuration)
                .plus(daysDuration);
     
        projectTask.setEstimate(totalDuration.toSeconds());
       
        LocalDate localDate = taskDueDate.get();

        if (localDate != null) {
            ZonedDateTime utcDateTime = localDate.atStartOfDay(ZoneOffset.UTC);
            projectTask.setDueDate(utcDateTime.toEpochSecond());
        }

        ProjectTaskType selectedProjectTaskType = selectedTaskType.get();
       
        if(selectedProjectTaskType!=null){
        projectTask.setType(selectedProjectTaskType.getKey());
       // projectTask.setProjectName("General Work");
        }

        CompanyProject companyProject = selectedProject.get();
        if(companyProject!=null){
        projectTask.setProjectId(companyProject.getId());
        }

        UserSession userSession = StateStorage
                .getCurrentState(StateName.USER_SESSION);
        projectTask.setUserId(userSession != null ? userSession.getId() : null);

        // validate Project Task
        boolean isValidProjectTask = validateProjectTask();
        if (!isValidProjectTask)
            return;

        TaskCreateService taskCreateService = new TaskCreateService(projectTask, isNewTask);
        taskCreateService.setOnSucceeded((WorkerStateEvent stateEvent) -> {
            Response response = taskCreateService.getValue();

            if (response.isError()) {
                parentDialog.close();
                displayErrorModal(response);
                return;
            }

            String taskType = projectTask.getType();
            boolean isMeetingTask = taskType != null
                    && taskType.equals(ProjectTask.MEETING_TASK_TYPE);

            // recheck visibility of createTaskBtn
            if (isMeetingTask) {
                boolean isAllowOfflineTask = this.userSession.isIsAllowOfflineTask();
                ProjectViewModel.getCreateTaskButtonVisibility().set(isAllowOfflineTask);
            }

            Gson gson = new Gson();
            ProjectTask createdTask = gson.fromJson(response.getData(), ProjectTask.class);
            boolean isCreatedTask = projectTask.getId() == null;

            List<ProjectTask> projectTasks = companyProject.getProjectTasks();
            Map.Entry<CompanyProject, ObservableList<TaskViewModel>> taskViewModelEntry;
            taskViewModelEntry = taskViewModelMap.entrySet().stream()
                    .filter((var currentEntry) -> {
                        CompanyProject project = currentEntry.getKey();
                        String projectId = project.getId();
                        return projectId != null
                                && projectId.equals(companyProject.getId());
                    }).findFirst().orElse(null);

            TaskViewModel taskViewModel = new TaskViewModel(
                    createdTask, ProjectViewModel.getGLOBAL_USER_WORKSTATUS());

            int index = 0;
            if (taskViewModelMap != null) {
                if (isCreatedTask) {
                    projectTasks.add(createdTask);
                    taskViewModel.setCompanyProjects(companyProjects);

                    if (taskViewModelEntry != null) {
                        ObservableList<TaskViewModel> taskViewModels = taskViewModelEntry.getValue();
                        taskViewModels.add(taskViewModel);
                    }
                } else {

                    projectTasks = projectTasks.stream().filter((ProjectTask curTask) -> {
                        String curId = curTask.getId();
                        return !(curId == null || !curId.equals(projectTask.getId()));
                    }).collect(Collectors.toList());
                    projectTasks.add(projectTask);
                    companyProject.setProjectTasks(projectTasks);

                    if (taskViewModelEntry != null) {
                        ObservableList<TaskViewModel> viewModels = taskViewModelEntry.getValue();

                        TaskViewModel viewModel = viewModels.stream().filter((var curViewModel) -> {
                            ProjectTask task = curViewModel.getTask();
                            String taskId = task.getId();
                            String strProjectName=task.getProjectName();
                            
                            return taskId != null && taskId.equals(projectTask.getId());
                        }).findFirst().orElse(null);
                        viewModels.remove(viewModel);
                        viewModels.add(taskViewModel);

                        if (parentRenderedTaskList != null) {
                            TaskViewModel renderedModel = parentRenderedTaskList.stream()
                                    .filter((TaskViewModel curModel) -> {
                                        ProjectTask curTask = curModel.getTask();
                                        return curTask.getId() != null
                                                && curTask.getId().equals(projectTask.getId());
                                    }).findFirst().orElse(null);
                            if (renderedModel != null) {
                                index = parentRenderedTaskList.indexOf(renderedModel);
                                boolean isRemovedViewModel = parentRenderedTaskList.remove(renderedModel);

                                if (!isRemovedViewModel) {
                                    return;
                                }
                            }
                        }
                    }
                }

                String currentTaskListProjectId = ProjectViewModel.getCURRENT_SELECTED_PROJECT_ID();
                boolean shouldAddedRenderedList = currentTaskListProjectId != null
                        && currentTaskListProjectId.equals(projectTask.getProjectId());

                if (shouldAddedRenderedList && parentRenderedTaskList != null) {
                    parentRenderedTaskList.add(index, taskViewModel);
                    if (parentTaskViewModelListView != null) {
                        parentTaskViewModelListView.refresh();
                    }
                }
            }

            parentDialog.close();
            displaySuccessModal(response);
        });
        taskCreateService.start();

    }

    public void onFoucsChangeEstimateInput(
            ObservableValue<? extends Boolean> obValuem, Boolean oldValue, Boolean newValue) {

        if (!newValue) {
            setCalculatedEstimationValues();
        }
    }

    private void setCalculatedEstimationValues() {
        int minutes = this.minutesEstimationValue.get() != null
                && !this.minutesEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.minutesEstimationValue.get())
                        : 0;

        int totalHours = this.hoursEstimationValue.get() != null
                && !this.hoursEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.hoursEstimationValue.get())
                        : 0;

        int totalDays = this.daysEstimationValue.get() != null
                && !this.daysEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.daysEstimationValue.get())
                        : 0;

        int hours = minutes / 60;
        minutes %= 60;
        totalHours += hours;
        int days = totalHours / 24;
        totalHours %= 24;
        totalDays += days;

        this.daysEstimationValue.set(Integer.toString(totalDays));
        this.hoursEstimationValue.set(Integer.toString(totalHours));
        this.minutesEstimationValue.set(Integer.toString(minutes));
    }

    private void displayErrorModal(Response response) {

      PopupDialogHeading headingComponent = new PopupDialogHeading(
               PopupDialogHeading.PopupType.ERROR, "Unable to Update Project Task");
        String strErrorMessage = response.getMessage();
        if (strErrorMessage.equals("User already has meeting task")) {
           headingComponent = new PopupDialogHeading(
                PopupDialogHeading.PopupType.ERROR, "User already has meeting task");
        } 
        
        
        PopupDialogBox errorDialog = new PopupDialogBox(headingComponent);
        errorDialog.setDescription(
                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION,
                response.getMessage());
        JFXButton closeBtn = errorDialog.getDialogButton(
                PopupDialogBox.DialogButton.RIGHT_MOST);
        closeBtn.setDisable(false);
        closeBtn.setVisible(true);
        closeBtn.setText("Close");
        closeBtn.setOnAction((ActionEvent event) -> {
            errorDialog.close();
        });
        errorDialog.load();
    }

    private void displaySuccessModal(Response response) {
        PopupDialogHeading headingComponent = new PopupDialogHeading(
                PopupDialogHeading.PopupType.SUCCESS,
                projectTask.getId() == null ? "Create Project Task" : "Update Project Task");
        PopupDialogBox successDialog = new PopupDialogBox(headingComponent);
        successDialog.setDescription(
                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION,
                projectTask.getId() == null ? "Successfully created task" : "Successfully updated task");
        JFXButton closeButton = successDialog.getDialogButton(
                PopupDialogBox.DialogButton.RIGHT_MOST);
        closeButton.setVisible(true);
        closeButton.setDisable(false);
        closeButton.setText("OK");
        closeButton.setOnAction((ActionEvent event) -> {
            successDialog.close();
        });
        successDialog.load();

    }

    private boolean validateProjectTask() {
        this.formErrorTxt.set("");

        ProjectTaskType taskType = selectedTaskType.get();

        if (taskType == null) {
            this.projectTask=null;
            this.formErrorTxt.set("Task Type is required field");
            return false;
        }

        var isMeetingType = ProjectTask.MEETING_TASK_TYPE.equals(taskType.getKey());
        if (isMeetingType) {
            return true;
        }

        CompanyProject _selectedProject = selectedProject.get();

        if (_selectedProject == null) {
            this.projectTask=null;
            this.formErrorTxt.set("Project is required field");
            return false;
        }

        String title = this.taskTitle.get();

        if (title == null || title.isBlank()) {
            this.projectTask=null;
            this.formErrorTxt.set("Task Title is required field");
            return false;
        }

        String _estimationMinutes = this.minutesEstimationValue.get();
        String _estimationHours = this.hoursEstimationValue.get();
        String _estimationDays = this.daysEstimationValue.get();

        int minutes = 0;
        int hours = 0;
        int days = 0;
        if (_estimationMinutes != "") {
            minutes = _estimationMinutes == null ? 0 : Integer.parseInt(_estimationMinutes);
        }
        if (_estimationHours != "") {
            hours = _estimationHours == null ? 0 : Integer.parseInt(_estimationHours);
        }
        if (_estimationDays != "") {
            days = _estimationDays == null ? 0 : Integer.parseInt(_estimationDays);
        }

        int estimation = minutes + hours + days;

        if (estimation == 0) {
            this.projectTask=null;
            this.formErrorTxt.set("Estimation is required field");
            return false;
        }

         int minutes2 = this.minutesEstimationValue.get() != null
                && !this.minutesEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.minutesEstimationValue.get())
                        : 0;

        int totalHours2 = this.hoursEstimationValue.get() != null
                && !this.hoursEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.hoursEstimationValue.get())
                        : 0;

        int totalDays2 = this.daysEstimationValue.get() != null
                && !this.daysEstimationValue.get().isBlank()
                        ? Integer.parseInt(this.daysEstimationValue.get())
                        : 0;

        int hours2 = minutes2 / 60;
        minutes2 %= 60;
        totalHours2 += hours2;
        int days2 = totalHours2 / 24;
        totalHours2 %= 24;
        totalDays2 += days2;
        
        if(totalDays2>=366){
            this.projectTask=null;
            this.formErrorTxt.set("Estimation should not be greated than 365 Days");
            return false;
        }
        
        LocalDate dueDate = taskDueDate.get();

        if (dueDate != null) {
            ZonedDateTime utcDateTime = dueDate.atStartOfDay(ZoneOffset.UTC);
            Instant dueDateInstant = utcDateTime.toInstant();

            LocalDate todayDate = Instant.now().atZone(ZoneOffset.UTC).toLocalDate();
            Instant todayInstant = todayDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            if (dueDateInstant.isBefore(todayInstant)) {
                this.projectTask=null;
                this.formErrorTxt.set("Due Date can not be a past day");
                return false;
            }
        }

        return true;
    }
}
