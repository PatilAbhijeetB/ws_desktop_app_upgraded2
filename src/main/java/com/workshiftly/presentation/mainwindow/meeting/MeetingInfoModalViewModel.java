package com.workshiftly.presentation.mainwindow.meeting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.MeetingTimeLog;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.CommonUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.domain.ProjectTaskModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowViewModel;
import com.workshiftly.presentation.mainwindow.project.ProjectViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 *
 * @author jade_m
 */
public class MeetingInfoModalViewModel implements ViewModel {
    
    private static final java.time.Duration ONE_SECOND 
            = java.time.Duration.ofSeconds(1);
    private static final int MAX_PARTICIPANT_COUNT = 10;
    private static final SimpleObjectProperty<ProjectTask> GLOBAL_WORKING_PROJECT_TASK 
            = ProjectViewModel.getCURRENT_WORKING_TASK();
    
    private ObservableList<CompanyProject> companyProjects;
    private ObservableList<
            ViewTuple<ParticipantListItem, ParticipantListItemModel>
            > currentParicipantElements;
    
    private ObservableList<Parent> leftParticipantListItems;
    private ObservableList<Parent> rightParticipantListItems;
    
    private SimpleBooleanProperty didStartMeeting;
    private SimpleBooleanProperty meetingTimeVisibility;
    private SimpleObjectProperty<ProjectTask> meetingTaskProp;
    private SimpleObjectProperty<MeetingTimeLog> meetingTimeLogProp;
    private SimpleStringProperty currentParticipant;
    private SimpleStringProperty formErrorTxt;
    private SimpleStringProperty meetingBtnTxt;
    private SimpleStringProperty meetingElapsedTime;
    private SimpleStringProperty meetingSummary;
    private SimpleStringProperty participantSlotCountLbl;
    
    private JFXDialogLayout parentLayout;
    private JFXDialog parentDialog;
    private Pane meetingBtnWrapper;
    
    private java.time.Duration meetingTimeCounter;
    private Timeline meetingCounterTimeline;
    private MeetingTimeLog meetingTimeLog = null;
    
    ComboBox<CompanyProject> projectSelectInput;
            
    public MeetingInfoModalViewModel() {
        
        this.currentParticipant = new SimpleStringProperty();
        
        this.companyProjects = FXCollections.observableArrayList();
        this.meetingTaskProp = new SimpleObjectProperty<>(null);
        
        this.currentParicipantElements = FXCollections.observableArrayList();
        this.leftParticipantListItems = FXCollections.observableArrayList();
        this.rightParticipantListItems = FXCollections.observableArrayList();
        
        this.formErrorTxt = new SimpleStringProperty("");
        this.didStartMeeting = new SimpleBooleanProperty(false);
        
        this.meetingTimeVisibility = new SimpleBooleanProperty(false);
        this.meetingBtnTxt = new SimpleStringProperty("Start Meeting");
        this.meetingElapsedTime = new SimpleStringProperty("00:00:00");
        
        this.meetingSummary = new SimpleStringProperty("");
        this.meetingTimeLogProp = new SimpleObjectProperty<>(null);
        this.participantSlotCountLbl = new SimpleStringProperty("Available slots:10");
        
        initMeetingTimeCounter();
        ListenParticipantElementsChanges();
    }
    
    public MeetingInfoModalViewModel(ProjectTask projectTask) {
        super();
        this.meetingTaskProp.set(projectTask);
    }

    public void setCompanyProjects(ObservableList<CompanyProject> companyProjects) {
        this.companyProjects.addAll(companyProjects);
    }

    public ObservableList<CompanyProject> getCompanyProjects() {
        return companyProjects;
    }

    public SimpleStringProperty getCurrentParticipant() {
        return currentParticipant;
    }

    public SimpleObjectProperty<ProjectTask> getMeetingTask() {
        return this.meetingTaskProp;
    }

    public void setMeetingTask(ProjectTask meetingTask) {
        this.meetingTaskProp.set(meetingTask);
    }

    public ObservableList<Parent> getLeftParticipantListItems() {
        return leftParticipantListItems;
    }

    public ObservableList<Parent> getRightParticipantListItems() {
        return rightParticipantListItems;
    }
    
    public boolean addMeetingParticipant(String participant) {
        boolean isValidParticipant = validParticipantInput(participant);

        if (!isValidParticipant) {
            return false;
        }
        return addParticipantTagItem(participant);
    }

    public void setParentLayout(JFXDialogLayout parentLayout) {
        this.parentLayout = parentLayout;
    }

    public JFXDialogLayout getParentLayout() {
        return parentLayout;
    }

    public void setParentDialog(JFXDialog parentDialog) {
        this.parentDialog = parentDialog;
    }

    public SimpleStringProperty getFormErrorTxt() {
        return formErrorTxt;
    }

    public SimpleBooleanProperty getDidStartMeeting() {
        return didStartMeeting;
    }

    public SimpleBooleanProperty getMeetingTimeVisibility() {
        return meetingTimeVisibility;
    }

    public SimpleStringProperty getMeetingBtnTxt() {
        return meetingBtnTxt;
    }

    public SimpleStringProperty getMeetingElapsedTime() {
        return meetingElapsedTime;
    }

    public SimpleStringProperty getMeetingSummary() {
        return meetingSummary;
    }

    public void setDidStartMeeting(SimpleBooleanProperty didStartMeeting) {
        this.didStartMeeting = didStartMeeting;
    }

    public void setMeetingBtnTxt(SimpleStringProperty meetingBtnTxt) {
        this.meetingBtnTxt = meetingBtnTxt;
    }

    public void setMeetingTimeLog(MeetingTimeLog meetingTimeLog) {
        this.meetingTimeLog = meetingTimeLog;
    }

    public void setMeetingTimeLogProp(SimpleObjectProperty<MeetingTimeLog> meetingTimeLogProp) {
        this.meetingTimeLogProp = meetingTimeLogProp;
    }

    public void setMeetingSummary(SimpleStringProperty meetingSummary) {
        this.meetingSummary = meetingSummary;
    }

    public void setMeetingBtnWrapper(Pane meetingBtnWrapper) {
        this.meetingBtnWrapper = meetingBtnWrapper;
    }

    SimpleStringProperty getParticipantSlotCountLbl() {
        return participantSlotCountLbl;
    }

    public void setProjectSelectInput(ComboBox<CompanyProject> projectSelectInput) {
        this.projectSelectInput = projectSelectInput;
    }
    
    /**
     * Method Name : onClickCloseBtn
     * Purpose : Handle close button 
     * 
     * @param participant
     * @return True if added unless return false 
     */
    void onClickMeetingCloseButton() {
        if (parentDialog != null) {
            parentDialog.close();
        }
    }
    
    /**
     * Method Name : addParticipantTagItem
     * Purpose : Added meeting participant and event dispatch from view
     * 
     * @param participant
     * @return True if added unless return false 
     */
    private boolean addParticipantTagItem(String participant) {
        try {
            ViewTuple<ParticipantListItem, ParticipantListItemModel> itemViewTuple;
            itemViewTuple = FluentViewLoader.fxmlView(ParticipantListItem.class).load();
            currentParicipantElements.add(itemViewTuple);
            
            ParticipantListItemModel itemViewModal = itemViewTuple.getViewModel();
            itemViewModal.setParticipantName(participant);
            itemViewModal.setParentViewModel(this);
        } catch (Exception ex) {
            // handle exceptions
            return false;
        }
        return true;
    }
    
    boolean removeParticipantTagItem(ParticipantListItemModel itemModel) {
        ViewTuple<ParticipantListItem, ParticipantListItemModel> removingParticipant;
        removingParticipant = currentParicipantElements.stream()
                .filter((ViewTuple<ParticipantListItem, ParticipantListItemModel> curElement) -> {
                    return curElement.getViewModel() == itemModel;    
                }).findFirst().orElse(null);
        return currentParicipantElements.remove(removingParticipant);
    }
    
    private void ListenParticipantElementsChanges() {
        ListChangeListener<
                ViewTuple<ParticipantListItem, ParticipantListItemModel>
                > listChangeListener;
        listChangeListener = (var change) -> {
            boolean isNextChange = change.next();
            if (!isNextChange) {
                return;
            }
            
            int currentSize = currentParicipantElements.size();
            int availableSlots = MAX_PARTICIPANT_COUNT - currentSize;
            participantSlotCountLbl.set("Available slots:" + availableSlots);
            
            if (change.wasAdded() && change.getAddedSize() == 1) {
                if (!(change.getAddedSubList() == null || change.getAddedSubList().isEmpty())) {
                    ViewTuple<ParticipantListItem, ParticipantListItemModel> addedViewTuple;
                    addedViewTuple = change.getAddedSubList().get(0);
                    
                    ObservableList<Parent> listItemList = currentParicipantElements.size() <= 5 
                            ? leftParticipantListItems : rightParticipantListItems;
                    listItemList.add(addedViewTuple.getView());
                }
            }
            
            if (change.wasRemoved() && change.getRemovedSize() == 1) {
                if (!(change.getRemoved() == null || change.getRemoved().isEmpty())) {
                    ViewTuple<ParticipantListItem, ParticipantListItemModel> removedViewTuple;
                    removedViewTuple = change.getRemoved().get(0);
                    
                    ObservableList<Parent> containerList = leftParticipantListItems
                            .contains(removedViewTuple.getView()) 
                            ? leftParticipantListItems : rightParticipantListItems;
                    boolean didRemoved = containerList.remove(removedViewTuple.getView());
                    
                    if (didRemoved) {
                        List<Parent> viewElements = Stream.concat(
                                leftParticipantListItems.stream(), 
                                rightParticipantListItems.stream()
                        ).collect(Collectors.toList());
                        
                        leftParticipantListItems.clear(); rightParticipantListItems.clear();
                        
                        if (viewElements.size() > 5) {
                            leftParticipantListItems.addAll(viewElements.subList(0, 5));
                            rightParticipantListItems.addAll(viewElements.subList(5, viewElements.size()));
                        } else {
                            leftParticipantListItems.addAll(viewElements);
                        }
                    }
                }
            }
        };
        currentParicipantElements.addListener(listChangeListener);
        
    }
    
    /**
     * Method Name : validParticipantInput
     * Purpose : Validate meeting participant text input
     * 
     * @param participant
     * @return True if valid unless return false 
     */
    private boolean validParticipantInput(String participant) {
        
        this.formErrorTxt.set("");
        String errorMsg;
        if (participant.isBlank()) {
            errorMsg = "Please enter participant name";
            this.formErrorTxt.set(errorMsg); return false;
        }
        
        if (currentParicipantElements.size() == 10) {
            errorMsg = "Unable to add more than 10 participants";
            this.formErrorTxt.set(errorMsg); return false;
        }
        
        String needle = CommonUtility.removeWhitespaces(participant).toLowerCase();
        var matchedElement = currentParicipantElements.stream()
                .filter((var element) -> {
                    ParticipantListItemModel viewModel = element.getViewModel();
                    var participantNameProp = viewModel.getParticipantName();
                    String equalityOperand = CommonUtility.removeWhitespaces(participantNameProp.get())
                            .toLowerCase();
                    return equalityOperand.equals(needle); 
                }).findFirst().orElse(null);
        
        if (matchedElement != null) {
            errorMsg = "Participant already exists in added participants";
            this.formErrorTxt.set(errorMsg); return false;
        }
        return true;
    }
    
    /**
     * Method Name : onClickMeetingButton
     * Purpose : MeetingInfoModalView meeting button event handler
     * 
     * @return void 
     */
    void onClickMeetingButton(ActionEvent event) {
        formErrorTxt.set("");
        boolean isMeetingStarted = this.didStartMeeting.get();
        event.consume();
        
        if (isMeetingStarted) {
            handleEndMeeting().start();
        } else if (validateSubmitStartMeeting()){
            handleStartMeeting().start();
        }
    }
    
    private boolean validateSubmitStartMeeting() {
        CompanyProject selectedProject = projectSelectInput.getValue();
        if (selectedProject == null) {
            formErrorTxt.set("Please select a project");
            return false;
        }
        
        String meetingPurpose = meetingSummary.get();
        if (meetingPurpose == null || meetingPurpose.isBlank()) {
            formErrorTxt.set("Please enter meeting purpose");
            return false;
        }
        return true;
    }
    
    /**
     * Method Name : handleStartMeeting
     * Purpose : Handle event start meeting task
     * 
     * @return Service<Response> instance which handles domain services 
     */
    private Service<Response> handleStartMeeting() {
        Service<Response> domainService;
        domainService = new Service<>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<>() {
                    @Override
                    protected Response call() throws Exception {
                        
                        ProjectTask meetingTask = meetingTaskProp.get();
                        ProjectTaskModule module = new ProjectTaskModule();
                        
                        List<String> participants = currentParicipantElements.stream()
                                .map((var curViewTuple) -> {
                                    ParticipantListItemModel viewModel = curViewTuple.getViewModel();
                                    return viewModel.getParticipantName().get();
                                }).collect(Collectors.toList());
                        
                        String summary = meetingSummary.get();
                        CompanyProject project = projectSelectInput.getValue();
                        
                        Response response = module.startProjectMeetingTask(
                                project, meetingTask, participants, summary
                        );
                        return response;
                    }
                };
            }
        };
        
        domainService.setOnRunning((WorkerStateEvent t) -> {
            MainScreenController.showLoadingSplashScreen();
        });
        
        domainService.setOnSucceeded((WorkerStateEvent t) -> {
            MainScreenController.hideLoadingSplashScreen();
            Response response = domainService.getValue();
            
            if (response.isError()) {
                formErrorTxt.set(response.getMessage());
                return;
            }
            
            GLOBAL_WORKING_PROJECT_TASK.set(meetingTaskProp.get());
            
            Gson gson = new Gson();
            JsonObject responseData = response.getData().getAsJsonObject();
            meetingTimeLog = gson.fromJson(
                    responseData.get("MeetingTimeLog"), MeetingTimeLog.class
            );
            
            WorkStatusLog workStatusLog = gson.fromJson(
                    responseData.get("WorkStatusLog"), WorkStatusLog.class
            );
            AuthenticatedMainWindowViewModel.getWORK_STATUS_LOGS().add(workStatusLog);
            AuthenticatedMainWindowViewModel.setUSER_WORK_STATUS(
                    WorkStatusLog.WorkStatus.IN_MEETING
            );
            meetingCounterTimeline.play();
            didStartMeeting.set(true);
            
        });
        
        return domainService;
    }
    
    /**
     * Method Name : initMeetingTimeCounter
     * Purpose : To Initialize UI meeting time counter component
     * 
     * @return void 
     */
    private void initMeetingTimeCounter() {
        
        meetingTimeCounter = java.time.Duration.ZERO;
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), ((ActionEvent t) -> {
            meetingTimeCounter = meetingTimeCounter.plus(ONE_SECOND);
            String readableTime = TimeUtility.formatDuration(meetingTimeCounter);
            meetingElapsedTime.set(readableTime);
        }));
        
        meetingCounterTimeline = new Timeline(keyFrame);
        meetingCounterTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    /**
     * Method Name : handleEndMeeting
     * Purpose : Handle event end meeting task
     * 
     * @return Service<Response> instance which handles domain services 
     */
    private Service<Response> handleEndMeeting() {
        
        ProjectTask projectTask = meetingTaskProp.get();
        
        if (projectTask != null) {
            long newSpentTime = meetingTimeCounter.toSeconds();
            long totalSpentTime = projectTask.getSpentTime() + newSpentTime;
            projectTask.setSpentTime(totalSpentTime);
            projectTask.setIsDirty(true);
        }
        
        Service<Response> domainService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        ProjectTask projectTask = meetingTaskProp.get();
                        ProjectTaskModule domainModule = new ProjectTaskModule();
                        return domainModule.endProjectMeetingTask(projectTask, meetingTimeLog);
                    }
                };
            }
        };
        
        domainService.setOnRunning((var t) -> {
            MainScreenController.showLoadingSplashScreen();
        });
        
        domainService.setOnSucceeded((var t) -> {
            MainScreenController.hideLoadingSplashScreen();
            Response response = domainService.getValue();
            
            if (response.isError()) {
                return;
            }
            
            Gson gson = new Gson();
            AuthenticatedMainWindowViewModel.setUSER_WORK_STATUS(
                    WorkStatusLog.WorkStatus.START
            );
            JsonObject responseData = response.getData().getAsJsonObject();
            WorkStatusLog workStatusLog = gson.fromJson(
                    responseData.get("WorkStatusLog"), WorkStatusLog.class
            );
            AuthenticatedMainWindowViewModel.getWORK_STATUS_LOGS().add(workStatusLog);
            GLOBAL_WORKING_PROJECT_TASK.set(null);
            
            meetingCounterTimeline.stop();
            try {
                parentDialog.close();
            } catch (NullPointerException ex) {
                LoggerService.LogRecord(
                        MeetingInfoModalViewModel.class, 
                        "Unable to close meeting dialog", 
                        InternalLogger.LOGGER_LEVEL.ALL, 
                        ex
                );
            }
        });
        return domainService;
    }
    
    /**
     * Method Name : popEndMeetingInfoModal
     * Purpose : Pop End Meeting Info modal
     * 
     * @return 
     */
    private void popEndMeetingInfoModal() {
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        
        try {
            ViewTuple<MeetingInfoModalView, MeetingInfoModalViewModel> viewTuple 
                    = FluentViewLoader.fxmlView(MeetingInfoModalView.class).load();
            
            Node viewNode = viewTuple.getView();
            MeetingInfoModalViewModel viewModel = viewTuple.getViewModel();
            
            viewModel.meetingTaskProp.set(this.meetingTaskProp.get());
            viewModel.companyProjects.addAll(this.companyProjects);
            viewModel.didStartMeeting.set(true);
            viewModel.meetingTimeVisibility.set(true);
            viewModel.meetingTimeLog = this.meetingTimeLog;
            
            if (viewModel.projectSelectInput != null) {
                CompanyProject selectedProject = this.projectSelectInput.getValue();
                viewModel.projectSelectInput.setValue(selectedProject);
            }
            
            if (meetingTimeLog != null && meetingTimeLog.getSummary() != null) {
                viewModel.meetingSummary.set(meetingTimeLog.getSummary());
            }
            
            if (viewModel.meetingBtnWrapper != null) {
                ObservableList<Node> btnWrapperElements 
                        = viewModel.meetingBtnWrapper.getChildren();
                btnWrapperElements.clear();
                
                JFXButton endMeetingBtn = new JFXButton("End Meeting");
                endMeetingBtn.getStyleClass().add("meeting-info-modal-btn");
                
                Font btnFont = new Font(Font.getDefault().getName(), 13);
                endMeetingBtn.setFont(btnFont);
                
                endMeetingBtn.setLayoutX(120);
                endMeetingBtn.setLayoutY(10);
                endMeetingBtn.setPrefHeight(30);
                endMeetingBtn.setOnAction((ActionEvent event) -> {
                    viewModel.onClickMeetingButton(event);
                });
                btnWrapperElements.add(endMeetingBtn);
            }
            viewModel.currentParicipantElements.addAll(currentParicipantElements);
            
            viewModel.setMeetingTimeLogProp(meetingTimeLogProp);

            dialogLayout.setBody(viewNode);
            JFXDialog dialog = new JFXDialog(
                    null, dialogLayout, JFXDialog.DialogTransition.NONE
            );

            dialog.setOverlayClose(false);
            MainScreenController.showJFXDialog(dialog);
            viewModel.setParentDialog(dialog);
            viewModel.meetingCounterTimeline.play();
        } catch (Exception ex) {
            System.out.println("##### Exception ex >>>> " + ex);
        }
    }
}
