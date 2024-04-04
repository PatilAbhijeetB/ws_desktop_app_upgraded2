/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.BreakReason;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.model.CompanyConfiguration;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.domain.RawDataModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.presentation.mainwindow.component.BreakReasonModal;
import com.workshiftly.presentation.mainwindow.component.BreakReasonModalModel;
import com.workshiftly.presentation.mainwindow.component.HeaderNavigation;
import com.workshiftly.presentation.mainwindow.project.ProjectViewModel;
import com.workshiftly.presentation.service.CheckAppUpdateService;
import com.workshiftly.presentation.service.SlientModeWorkStatusActivator;
import com.workshiftly.presentation.service.UserRecoveryService;
import com.workshiftly.presentation.service.WorkingTaskUpdater;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.util.Duration;

import org.jnativehook.NativeHookException;
import org.joda.time.DateTimeZone;

import javafx.application.Platform;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.workshiftly.presentation.viewmodel.LoginViewModelNew;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author chamara
 */
public class AuthenticatedMainWindowViewModel implements ViewModel {
    
    // Internal logger uses sentry and error log
    private static final InternalLogger LOGGER;
    // Interval value for idle action tracking scheduler task
    private static final Duration IDLE_TIME_LISTENER_INTERVAL;
    // User current working status
    private static final SimpleObjectProperty<WorkStatusLog.WorkStatus> USER_WORK_STATUS;
    // User work status logs collection
    private static final ObservableList<WorkStatusLog> WORK_STATUS_LOGS;
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    // static initializer
    static {
        LOGGER = LoggerService.getLogger(AuthenticatedMainWindowViewModel.class);
        IDLE_TIME_LISTENER_INTERVAL = Duration.seconds(1);
        USER_WORK_STATUS = new SimpleObjectProperty<>(WorkStatusLog.WorkStatus.BEGINNING);
        WORK_STATUS_LOGS = FXCollections.observableArrayList();
    }
    
    // instance properties
    private final java.time.Duration ONE_SECOND_DURATION;
    private final SimpleStringProperty mainNavigationIdentifier;
    private final SimpleObjectProperty<Image> timeButtonImageProperty;
    private final SimpleStringProperty workingTimeLabel;
    private final SimpleBooleanProperty timerButtonDisability;
    private final SimpleBooleanProperty isDisplayedAppIdlePopup;
    private final SimpleLongProperty userIdlCountDownTimer;
    private final SimpleDoubleProperty idleTimeDifference;
    private final SimpleStringProperty syncStatusLabel;
    private static ScheduledService<Long> userIdleTimeExcuetionService;
    private final Image playButtonImage;
    private final Image pauseButtonImage;
    private java.time.Duration workTimerSeconds;
    private final Timeline workTimerTimeline;
    private final RawDataModule rawDataModule;
    private PopupDialogBox userWorkingConfirmationDialogBox;
    private PopupDialogBox userForceIdleAlertDialogBox;
    private final SimpleLongProperty autoIdleCountDownTimer;
    private final SimpleObjectProperty<CompanyConfiguration> companyConfiguration;
    private final SimpleBooleanProperty isWritingRecordStatusLog;
    private final SimpleStringProperty fullNameLabelTextProperty = new SimpleStringProperty();
     private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
     
    private Label filePathLabel;
    private String struploadSpeed;
    private String strdownloadSpeed;
    private String strjitter;
    private String strPing;
    private long prevBytesSent;
    private long prevBytesReceived;
    private List<Double> latList;
    
    private ScheduledExecutorService scheduler;
     private static final Duration ONE_HOUR_DURATION = Duration.minutes(1);
    private Timeline hourlyTimer;
  
    public AuthenticatedMainWindowViewModel() throws Exception {
        
        // Instance Properties initialization
        this.ONE_SECOND_DURATION = java.time.Duration.ofSeconds(1);
        this.rawDataModule = new RawDataModule();
        
        this.mainNavigationIdentifier = new SimpleStringProperty();
        mainNavigationIdentifier.bind(HeaderNavigation.getCurrentActivatedNavigationName());
        
        this.workingTimeLabel = new SimpleStringProperty();
        this.timerButtonDisability = new SimpleBooleanProperty(false);
        this.isDisplayedAppIdlePopup = new SimpleBooleanProperty(false);
        this.autoIdleCountDownTimer = new SimpleLongProperty(0);
        this.userIdlCountDownTimer = new SimpleLongProperty(0);
        this.idleTimeDifference = new SimpleDoubleProperty(0);
        this.syncStatusLabel = new SimpleStringProperty();
        this.isWritingRecordStatusLog = new SimpleBooleanProperty(false);
        
        final String playBtnImagePath = "/images/icons/play_white.png";
        InputStream playBtnStream = getClass().getResourceAsStream(playBtnImagePath);
        this.playButtonImage = new Image(playBtnStream);
        
        final String pauseBtnImagePath = "/images/icons/pause.png";
        InputStream pauseBtnStream = getClass().getResourceAsStream(pauseBtnImagePath);
        this.pauseButtonImage = new Image(pauseBtnStream);
        
        this.timeButtonImageProperty = new SimpleObjectProperty<>(pauseButtonImage);
       // ExecutorService executor = Executors.newFixedThreadPool(1);
        // Runnable longRunningTask = () -> {
            // Your function's code that takes a long time to execute
       //     hourlyTimer = new Timeline(
       //     new KeyFrame(ONE_HOUR_DURATION, this::HourlyTask)
      //  );
      //       try {
      //              Thread.sleep(1000); // Simulate a long-running operation
      //          } catch (InterruptedException e) {
      //              e.printStackTrace();
      //          }
      //          hourlyTimer.setCycleCount(Timeline.INDEFINITE); // Execute hourly task indefinitely
      //  hourlyTimer.play(); // Start the hourly timer
        
      //  };
      //   executor.execute(longRunningTask);
       scheduler = Executors.newScheduledThreadPool(1);
        
        // Schedule the task to run every hour
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Call your function here
                try {
                           getlocation();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.HOURS); // 0 initial delay, run every 1 hour
        
        // To stop the scheduler after certain duration, you can add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Handle interruption
            }
        }));

        
        
        
     
        
        
        
        
       // AuthenticationModule authenticationModule = new AuthenticationModule();
        // current working count up timer implementation
        workTimerSeconds = java.time.Duration.ZERO;
        KeyFrame workTimerKeyFrame;
        workTimerKeyFrame = new KeyFrame(Duration.seconds(1), (ActionEvent event ) -> {
            workTimerSeconds = workTimerSeconds.plus(ONE_SECOND_DURATION);
            String parseReadableFormat = TimeUtility.formatDuration(workTimerSeconds);
            workingTimeLabel.set(parseReadableFormat);
        });
        workTimerTimeline = new Timeline(workTimerKeyFrame);
        workTimerTimeline.setCycleCount(Timeline.INDEFINITE);
        AuthenticationModule authenticationModule = new AuthenticationModule();
        Response response = authenticationModule.syncUserStatus("online");
        
        // default company configuration initailization
        CompanyConfiguration _companyConfiguration 
                = StateStorage.getCurrentState(StateName.COMPANY_CONFIGURATION);
        this.companyConfiguration = new SimpleObjectProperty<>(_companyConfiguration);
        
        // user idle Time listener service 
        userIdleTimeExcuetionService = initUserIdleTimeExcuetionService();
        // populate workstatus logs collection
        populateWorkStatusLogsIntoGlobalList();
        // user work status global listener
        addGlobalWorkStatusListener();
        // sync status change listener
        this.syncStatusLabel.set("Not synced yet");
        addSyncStatusChangeListenr();
        // app update availability check
        checkAppUpdateAvialability();
        // Run user recovery service
        runUserRecoveryService();
    }
     
   public SimpleStringProperty getFullNameLabelTextProperty() {
                 UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
                  fullNameLabelTextProperty.set("Welcome Back,Nick");
        if (userSession != null) {
             fullNameLabelTextProperty.set("");
            String fullName = new StringBuilder()
                     .append("Welcome Back, ")
                    .append(userSession.getFirstName())
                     .append("!")
                    .toString();
            fullNameLabelTextProperty.set(fullName);
        return fullNameLabelTextProperty;
    }
        return fullNameLabelTextProperty;
   }
    // getter methods
   // public SimpleStringProperty mainNavigationIdentifierProperty() {
   //     return this.mainNavigationIdentifier;
    //}
    
    public SimpleObjectProperty<Image> timeButtonImageProperty() {
        return this.timeButtonImageProperty;
    }
    
    public SimpleStringProperty workedTimeTxtProperty() {
        return this.workingTimeLabel;
    }
    
    public SimpleBooleanProperty timerButtonDisability() {
        return this.timerButtonDisability;
    }

    public SimpleStringProperty getSyncStatusTxtProperty() {
        return syncStatusLabel;
    }
    
    public static final WorkStatusLog.WorkStatus getGloable_USER_WORK_STATUS() {
        return USER_WORK_STATUS.getValue();
    }
    
    public static final SimpleObjectProperty<WorkStatusLog.WorkStatus> getUSER_WORK_STATUS_objectProperty() {
        return USER_WORK_STATUS;
    }
    
    public static ObservableList<WorkStatusLog> getWORK_STATUS_LOGS() {
        return WORK_STATUS_LOGS;
    }
    
    // setter methods
    public static final void setUSER_WORK_STATUS(WorkStatusLog.WorkStatus workStatus) {
        USER_WORK_STATUS.set(workStatus);
    }

    // Add Listener to changes on Global User Work Status and relevent handlers
    private void addGlobalWorkStatusListener() {
       
        USER_WORK_STATUS.addListener((var observable, var oldValue, var newValue) -> {
            if (newValue == null) { return; }
            switch (newValue) {
                case BEGINNING: {
                    break;
                }
                case START:
                   // Response response = authenticationModule.syncUserStatus("online");
                    onChangeToStartWrokStatus();
                    break;
                case BREAK:
                   // Response response1 = authenticationModule.syncUserStatus("offline");
                    onChangeToBreakWorkStatus();
                    break;
                case OFFLINE_TASK:
                case IN_MEETING:
                   // Response response3 = authenticationModule.syncUserStatus("online");
                    onChangeToMeetingWorkStatus();
                    break;
            }
        });
    }
    
    // Handler: Global User Work Status change to START
    private void onChangeToStartWrokStatus() {
        AuthenticationModule authenticationModule = new AuthenticationModule();
        Response response = authenticationModule.syncUserStatus("online");
        workTimerTimeline.play();
        timeButtonImageProperty.set(pauseButtonImage);
        Worker.State userIdleTimerServiceState 
                = userIdleTimeExcuetionService.getState();
        
        if (userIdleTimerServiceState == Worker.State.CANCELLED) {
            userIdleTimeExcuetionService = initUserIdleTimeExcuetionService();
        }
        
        if (userIdleTimerServiceState != Worker.State.RUNNING 
                && userIdleTimerServiceState != Worker.State.SCHEDULED) {
            userIdleTimeExcuetionService.start();
        }  
    }
    
    // Handler: Global User Work Status change to BREAK
    private void onChangeToBreakWorkStatus() {
        AuthenticationModule authenticationModule = new AuthenticationModule();
        Response response = authenticationModule.syncUserStatus("idle");
        workTimerTimeline.pause();
        timeButtonImageProperty.set(playButtonImage);
        boolean didCanceled = userIdleTimeExcuetionService.cancel();
    }
    
    // Handler: Global User Work Status change to IN_MEETING
    private void onChangeToMeetingWorkStatus() {
        AuthenticationModule authenticationModule = new AuthenticationModule();
        Response response = authenticationModule.syncUserStatus("online");
        userIdleTimeExcuetionService.cancel();
    }
    
    // Add Chanage Listener on Sync Status Chnages
    private void addSyncStatusChangeListenr() throws Exception {
        PublishSubject<Long> currentLastSyncTime = StateStorage.getCurrentState(StateName.LAST_SYNCED_TIME);
        if (currentLastSyncTime == null) {
            currentLastSyncTime = PublishSubject.create();
            StateStorage.set(StateName.LAST_SYNCED_TIME, PublishSubject.class, currentLastSyncTime);
        }

        currentLastSyncTime.subscribe((value) -> {
            if (value != null) {
                DateTimeZone userDateTimeZone = StateStorage
                        .getCurrentState(StateName.USER_TIMEZONE);
                String syncTimeString = "Last synced: ".concat(
                        TimeUtility.getHumanReadbleDateTime(value, userDateTimeZone));
                Platform.runLater(() -> {
                    syncStatusLabel.set(syncTimeString);
                });
            }
        });
    }
    
    public void onClickMainTimerButton() {
        
        WorkStatusLog workStatusLog = StateStorage.getCurrentState(StateName.LAST_WORK_STATUS_LOG);
        
        WorkStatusLog.WorkStatus nextState = workStatusLog.getWorkStatus() == WorkStatusLog.WorkStatus.START 
                ? WorkStatusLog.WorkStatus.BREAK : WorkStatusLog.WorkStatus.START;
        
        if (nextState == WorkStatusLog.WorkStatus.BREAK) {
            
            CompanyConfiguration _companyConfiguration = this.companyConfiguration.get();
            boolean allowUsersToAddBreakReasons = _companyConfiguration != null 
                ? _companyConfiguration.isAllowUsersToAddBreakReasons()
                : false;
            
            if (allowUsersToAddBreakReasons) {
                MainScreenController.showJFXDialog(getBreakReasonDialog());
            } else {
                updateWorkingTaskAndRecordWorkStatusLog(WorkStatusLog.WorkStatus.BREAK, true);
            }
        } else {
            recordWorkStatusLog(nextState, true);
        }
    }
    
    private void recordWorkStatusLog(WorkStatusLog.WorkStatus workStatus, boolean isUserAction) {
        
        if (isWritingRecordStatusLog.get()) {
            return;
        }
        
        BreakReason breakReason = StateStorage.getCurrentState(StateName.CURRENT_BREAK_REASON);
        Service<Response> domainService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        return rawDataModule.captureWorkStatusLog(workStatus, breakReason, isUserAction);
                    }
                };
            }
        };
        
        domainService.setOnRunning((WorkerStateEvent event) -> { 
            this.isWritingRecordStatusLog.set(true);
            timerButtonDisability.set(true); 
        });
        
        domainService.setOnSucceeded((WorkerStateEvent event) -> {
            
            Response result = domainService.getValue();
            
            if (result.isError()) {
                return;
            }
            
            WorkStatusLog workStatusLog = StateStorage
                    .getCurrentState(StateName.LAST_WORK_STATUS_LOG);
            WORK_STATUS_LOGS.add(workStatusLog);
            USER_WORK_STATUS.set(workStatusLog.getWorkStatus());
            timerButtonDisability.set(false);
            isWritingRecordStatusLog.set(false);
        });
        
        domainService.start();
    }
    
    private ScheduledService<Long> initUserIdleTimeExcuetionService() {
        
        ScheduledService<Long> service =  new ScheduledService<Long>() {
            @Override
            protected Task<Long> createTask() {
                return new Task<Long>() {
                    @Override
                    protected Long call() throws Exception {
                        Long userIdleTime = rawDataModule.getUserIdleTime();
                        return userIdleTime;
                    }
                };
            }
        };
        
        service.setOnSucceeded((WorkerStateEvent stateEvent) -> {
            
            // two way handling with comapny config : slient tracking
            Long currentIdleTime = service.getValue();
            CompanyConfiguration companyConfig = this.companyConfiguration.get();
            
            boolean isActiveSilentTracking = companyConfig.isActiveSilentTracking();
            
            if (isActiveSilentTracking) {
                activeSlientTrackingHandler(currentIdleTime);
                return;
            }
            
            inActiveSlientTrackingHandler(currentIdleTime);
        });
        service.setPeriod(IDLE_TIME_LISTENER_INTERVAL);
        return service;
    }

    private JFXDialog getBreakReasonDialog() {
        try {
            StateStorage.set(StateName.CURRENT_BREAK_REASON, BreakReason.class, null);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Fail to reset current break reason", ex);
        }

        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Text("Break Reason"));
       

        
        ViewTuple<BreakReasonModal, BreakReasonModalModel> breakReasonModalTuple
                = FluentViewLoader.fxmlView(BreakReasonModal.class).load();
        BreakReasonModalModel breakReasonModalModel = breakReasonModalTuple.getViewModel();
        Node breakResonModalNode = breakReasonModalTuple.getView();

        dialogLayout.setBody(breakResonModalNode);

        JFXDialog dialog = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.NONE);
        dialog.setOverlayClose(false);

        JFXButton yesBtn = new JFXButton("Take a Break");
        yesBtn.getStyleClass().add("dialog-modal-primary-btn");
      //  yesBtn.setStyle("-fx-background-color: #3E5BF2;-fx-text-fill: white");
         yesBtn.setOnAction((ActionEvent event) -> {
            event.consume();

           // validate break reason
            boolean isValidBreakReason = isValidBreakReason(breakReasonModalModel);
            if (!isValidBreakReason) {
                return;
            }
            dialog.close();

            // set break reason
            BreakReason breakReason = breakReasonModalModel.getSelectedBreakReasonProperty().get();
            try {
                StateStorage.set(StateName.CURRENT_BREAK_REASON, BreakReason.class, breakReason);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to persist break reason in states", ex);
            }

            // take break
            updateWorkingTaskAndRecordWorkStatusLog(WorkStatusLog.WorkStatus.BREAK, true);
        });

        JFXButton noBtn = new JFXButton("Cancel");
        noBtn.getStyleClass().add("dialog-modal-primary-btn");

        noBtn.setOnAction((ActionEvent event) -> {
            event.consume();
            dialog.close();

            // reset current break reason
            try {
                StateStorage.set(StateName.CURRENT_BREAK_REASON, BreakReason.class, null);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Fail to reset current break reason", ex);
            }
        });

        dialogLayout.setActions(yesBtn, noBtn);
        return dialog;
    }

    public static ScheduledService<Long> getIdleTimerScheduleService() {
        return AuthenticatedMainWindowViewModel.userIdleTimeExcuetionService;
    }
    
    private void populateWorkStatusLogsIntoGlobalList() {
        
        Service<Response> domainService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<>() {
                    @Override
                    protected Response call() throws Exception {
                       RawDataModule rawDataModule = new RawDataModule();
                        return rawDataModule.getTodayWorkingStatusLogs(); 
                    };
                };
            }
        };
        
        domainService.setOnSucceeded((WorkerStateEvent stateEvent) -> {
            
            Gson gson = new Gson();
            Response response = domainService.getValue();
            
            if (!response.isError()) {
                Type workStatusLogsListType = new TypeToken<List<WorkStatusLog>>(){}.getType();
                List<WorkStatusLog> workStatusLogs = gson.fromJson(response.getData(), workStatusLogsListType);
                WORK_STATUS_LOGS.addAll(workStatusLogs);
                return;
            }
            
            // error handling scenarios
            System.out.println("#### populateWorkStatusLogsIntoGlobalList ----> ERROR_HANDLING");
        });
        domainService.start();
        recordWorkStatusLog(WorkStatusLog.WorkStatus.START, true);
        
    }

    private boolean isValidBreakReason(BreakReasonModalModel model) {
        BreakReason breakReason = model.getSelectedBreakReasonProperty().get();
        String otherReasonText = model.getOtherReasonTextInputProperty().get();

        if (breakReason == null) {
            model.getBreakReasonErrorLabelTextProperty().set("Please select valid reason");
            return false;
        } else {
            model.getBreakReasonErrorLabelTextProperty().set(null);
        }

        if (breakReason.getTitle().equals(StateName.OTHER_BREAK_REASON_TITLE)
                && AppValidator.isNullOrEmptyOrBlank(otherReasonText)) {
            model.getOtherReasonErrorLabelTextProperty().set("Please enter valid reason");
            return false;
        } else {
            model.getOtherReasonErrorLabelTextProperty().set(null);
        }

        return true;
    }
    
    private void runUserRecoveryService() {
        
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            
            if (userSession == null || userSession.getId() == null) {
                return;
            }
            
            UserRecoveryService userRecoveryService = new UserRecoveryService(userSession.getId());
            userRecoveryService.setOnSucceeded((WorkerStateEvent event) -> {
                Response response = userRecoveryService.getValue();
                
                if (response.isError()) {
                    popupRecoveryExceptionalDialogBox();
                    return;
                }
                
                if (response.getData() != null) {
                    JsonObject responseData = response.getData().getAsJsonObject();
                    boolean shouldRecover = responseData.has("shouldRecover")
                            ? responseData.get("shouldRecover").getAsBoolean() : false;
                    if (shouldRecover) {
                        popupRecoveryConfirmation(responseData);
                    }
                }
            });
            userRecoveryService.setOnFailed((WorkerStateEvent event) -> {
                event.consume();
                popupRecoveryExceptionalDialogBox();
            });
            
            userRecoveryService.start();
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    this.getClass(), 
                    "Error occurred while running recovery service", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
    
    private void popupRecoveryExceptionalDialogBox() {
        
        JFXButton closeBtn = new JFXButton("Close");
        closeBtn.getStyleClass().add("dialog-modal-primary-btn");
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Text("Warning"));
        dialogLayout.setBody(new Text(
                "Error occurred while retrieving recovery data\n"
                + "Application will discard recoverying previous working\n\n"
                + "You can continue daily working without checking up recovery"
        ));
        dialogLayout.setActions(closeBtn);
        
        JFXDialog dialog = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        
        closeBtn.setOnAction((ActionEvent event) -> {
            event.consume();
            dialog.close();
        });
        
        MainScreenController.showJFXDialog(dialog);
    }
    
    private void popupRecoveryConfirmation(JsonObject recoveryData) {
        
        JFXButton acceptBtn = new JFXButton("Yes, Recover");
        acceptBtn.getStyleClass().add("dialog-modal-primary-btn");
        
        JFXButton declineBtn = new JFXButton("No");
        declineBtn.getStyleClass().add("dialog-modal-primary-btn");
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Text("Warning"));
        dialogLayout.setBody(new Text(
                "Your previous daily workings are not in complete state\n"
                + "Do you want to continue previous daily workings?"
        ));
        dialogLayout.setActions(acceptBtn, declineBtn);
        
        JFXDialog dialog = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        
        declineBtn.setOnAction((ActionEvent event) -> {
            event.consume();
            dialog.close();
        });
        
        acceptBtn.setOnAction((ActionEvent event) -> {
            event.consume();
            dialog.close();
            
            long workedDuration = recoveryData.get("totalWorkedDuration").getAsLong();
            workTimerSeconds = java.time.Duration.ofSeconds(workedDuration);
        });
        MainScreenController.showJFXDialog(dialog);
    }
    
    private void initUserWorkingConfirmationDialogBox() {
        
        PopupDialogHeading dialogHeading = new PopupDialogHeading(PopupDialogHeading.PopupType.WARNING);
        dialogHeading.setHeadingTxt("Working state confirmation");
        
        this.userWorkingConfirmationDialogBox = new PopupDialogBox(dialogHeading);
        this.userWorkingConfirmationDialogBox.setDescription(
                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                "You have almost reached the maximum limit of keep idle while working."
                        + " Are you still working?"
        );
        setAutoIdleCountDownText();
        
        JFXButton workingConfirmationButton = this.userWorkingConfirmationDialogBox
                .getDialogButton(PopupDialogBox.DialogButton.RIGHT_CENTER);
        workingConfirmationButton.setText("Yes,I am working");
        workingConfirmationButton.setVisible(true);
        workingConfirmationButton.setOnAction((ActionEvent event) -> {
            
            event.consume();
            try {
                rawDataModule.resetIdleTimer();
            } catch (NativeHookException ex) {
                LoggerService.LogRecord(
                        this.getClass(),
                        "Error occured while work confirmation reset idle timer", 
                        InternalLogger.LOGGER_LEVEL.ALL, 
                        ex
                );
            }

            this.userWorkingConfirmationDialogBox.close();
            MainScreenController.setMainStageIconified(true);
            isDisplayedAppIdlePopup.set(false);
        });
        
        JFXButton idleConfirmationButton = this.userWorkingConfirmationDialogBox
                .getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
        idleConfirmationButton.setText("No,I am idle");
        idleConfirmationButton.setVisible(true);
        idleConfirmationButton.setOnAction((ActionEvent event) -> {
            isDisplayedAppIdlePopup.set(false);
            
            Platform.runLater(() -> {
                recordWorkStatusLog(WorkStatusLog.WorkStatus.BREAK, true);
            });
            this.userWorkingConfirmationDialogBox.close();
        });
        
        JFXButton inMeetingCofirmationButton = this.userWorkingConfirmationDialogBox
                .getDialogButton(PopupDialogBox.DialogButton.LEFT_MOST);
        inMeetingCofirmationButton.setText("I am in Meeting");
        inMeetingCofirmationButton.setVisible(false);
        inMeetingCofirmationButton.setOnAction((ActionEvent event) -> {
            this.userWorkingConfirmationDialogBox.close();
            Platform.runLater(()-> {
                showMeetingInfoModal();
            });
        });
    }
    
    private void setAutoIdleCountDownText() {
        
        if (this.userWorkingConfirmationDialogBox == null) {
            return;
        }
        
        Double currentIdleTimeDiff = this.idleTimeDifference.getValue();
        long remainingTime = (long) (60.0 - (60.0 - currentIdleTimeDiff));
        
        this.userWorkingConfirmationDialogBox.setDescription(
            PopupDialogBox.MainContentDescriotion.SUB_DESCRIPTION, 
            String.format(
                    "You will be automatically set auto idle state in %d seconds", 
                    remainingTime
            )
        );          
    }
    
    private void initForceIdleAlertDialogBox() {
        
        PopupDialogHeading dialogHeading = new PopupDialogHeading(PopupDialogHeading.PopupType.INFORMATION);
        dialogHeading.setHeadingTxt("Force Break State");
        
        this.userForceIdleAlertDialogBox = new PopupDialogBox(dialogHeading);
        this.userForceIdleAlertDialogBox.setDescription(
                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                "You are exceeded time limit of maximum idle time limit while working. "
                        + "Further you have been set to auto idle state"
        );
        this.userForceIdleAlertDialogBox.setDescription(
                PopupDialogBox.MainContentDescriotion.SUB_DESCRIPTION, 
                "Do you want to start your working?"
        );
        
        JFXButton startWorkingButton = this.userForceIdleAlertDialogBox
                .getDialogButton(PopupDialogBox.DialogButton.RIGHT_CENTER);
        startWorkingButton.setText("Yes, start again");
        startWorkingButton.setOnAction((ActionEvent event) -> {
            event.consume();
            Platform.runLater(() -> {
                recordWorkStatusLog(WorkStatusLog.WorkStatus.START, true);
                AuthenticatedMainWindowViewModel.this.userForceIdleAlertDialogBox.close();
            }); 
        });
        startWorkingButton.setVisible(true);
        
        JFXButton noActionButton = this.userForceIdleAlertDialogBox
                .getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
        noActionButton.setText("No, start later");
        noActionButton.setOnAction((ActionEvent event) -> {
            event.consume();
            this.userForceIdleAlertDialogBox.close();
        });
        noActionButton.setVisible(true);
        
        /**
         * Implement later
         */
//        JFXButton signoutButton = this.userForceIdleAlertDialogBox
//                .getDialogButton(PopupDialogBox.DialogButton.LEFT_MOST);
//        signoutButton.setText("Sign out");
//        signoutButton.setOnAction((ActionEvent event) -> {
//            event.consume();
//            this.userForceIdleAlertDialogBox.close();
//            Platform.runLater(() -> {
//                Service<Response> recordWorkStatusLog = recordWorkStatusLog(WorkStatusLog.WorkStatus.STOP);
//                recordWorkStatusLog.start();
//            });
//        });
//        signoutButton.setVisible(false);
    }
    
    
    private void showMeetingInfoModal() {
        boolean isInitializedNavItems = HeaderNavigation.isInitialized();
        if (isInitializedNavItems) {
            HeaderNavigation.activateNavigationItem(StateName.PROJECT_NAVIGATION_ITEM);
            
            PopupDialogBox popupDialogBox = new PopupDialogBox();
            
            PopupDialogHeading dialogHeading = new PopupDialogHeading(
                    PopupDialogHeading.PopupType.INFORMATION, "Meeting Task"
            );
            popupDialogBox.setHeadingComponent(dialogHeading);
            
            String mainDescription = "Please start the meeting task relavant to the project";
            popupDialogBox.setDescription(
                    PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, mainDescription
            );
            JFXButton closeButton 
                    = popupDialogBox.getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
            closeButton.setText("OK");
            closeButton.setVisible(true);
            closeButton.setDisable(false);
            closeButton.setOnAction((var event) -> {
                popupDialogBox.close();
            });
            popupDialogBox.load();
        }
    }
    
    private void activeSlientTrackingHandler(long currentIdleTime) {
        try {
            CompanyConfiguration companyConfig = this.companyConfiguration.get();
            double maxAllowedIdleTime = Duration.minutes(companyConfig.getAppIdleTime())
                    .toSeconds();
            double _idleTimeDifference = maxAllowedIdleTime - currentIdleTime;
            
            this.idleTimeDifference.set(_idleTimeDifference);
            double signBit = Math.signum(_idleTimeDifference);
            
            if (signBit > 0.0) {
                return;
            }
            recordWorkStatusLogsOnSlientMode(WorkStatusLog.WorkStatus.BREAK);
            
            SlientModeWorkStatusActivator activatorService;
            activatorService = new SlientModeWorkStatusActivator();
            activatorService.setPeriod(IDLE_TIME_LISTENER_INTERVAL);
            
            activatorService.setOnSucceeded((var eventHandler) -> {
                Response response = activatorService.getValue();
                JsonObject resultData = response.getData().getAsJsonObject();
                boolean isUserWorking = resultData.get("isUserWorking").getAsBoolean();

                if (!isUserWorking) {
                    return;
                }

                ProjectTask previousWorkingTask 
                        = StateStorage.getPreviousState(StateName.RECENT_WORKING_TASK);

                if (previousWorkingTask != null) {
                    ProjectViewModel projectViewModel 
                            = StateStorage.getCurrentState(StateName.PROJECT_VIEWMODEL);
                    if (projectViewModel != null) {
                        projectViewModel.startRecentTaskOnSlientMode(previousWorkingTask);
                    }
                }

                try {
                    recordWorkStatusLogsOnSlientMode(WorkStatusLog.WorkStatus.START);
                    activatorService.cancel();
                } catch (Exception ex) {
                    String errorMsg = "SlientModeWorkStatusActivator setOnSuccess handler";
                    LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
                }
            });

            activatorService.start();
        } catch (Exception ex) {
            String errorMsg = "Unable to handle slient tracking";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }
    }
    
    private void inActiveSlientTrackingHandler(long currentIdleTime) {
        boolean didVisibleIdleTimeDialog = isDisplayedAppIdlePopup.get();
            
        if (didVisibleIdleTimeDialog) {
            long computedIdleTime 
                    = (long) (userIdlCountDownTimer.get() 
                    + IDLE_TIME_LISTENER_INTERVAL.toSeconds());
            userIdlCountDownTimer.set(computedIdleTime);
            currentIdleTime = computedIdleTime;
        } else {
            userIdlCountDownTimer.set(currentIdleTime);
        }

        CompanyConfiguration companyConfig = this.companyConfiguration.get();
        double appMaximumIdleTime = Duration.minutes(companyConfig.getAppIdleTime()).toSeconds();
        
        double _idleTimeDifference = appMaximumIdleTime - currentIdleTime;
        this.idleTimeDifference.set(_idleTimeDifference);
        double absoluteIdleTimeDifference = Math.abs(_idleTimeDifference);
        double signBit = Math.signum(_idleTimeDifference);

        boolean didVisibleAppIdleConfirmation = isDisplayedAppIdlePopup.getValue();

        if (!didVisibleAppIdleConfirmation && absoluteIdleTimeDifference <= 60 && signBit == 1.0) {
            isDisplayedAppIdlePopup.set(true);
            this.autoIdleCountDownTimer.set((long) absoluteIdleTimeDifference);
            initUserWorkingConfirmationDialogBox();
            this.userWorkingConfirmationDialogBox.load();

            if (!MainScreenController.isMainStatgeFocused()) {
                MainScreenController.setMainStageIconified(true);
            }
            MainScreenController.setMainStageIconified(false);
        }

        if (didVisibleAppIdleConfirmation) {
            setAutoIdleCountDownText();

            // hide user working confirmation and pop up force idle state
            if (signBit < 0.0) {
                this.userWorkingConfirmationDialogBox.close();
                isDisplayedAppIdlePopup.set(false);
                initForceIdleAlertDialogBox();
                this.userForceIdleAlertDialogBox.load();

                if (!MainScreenController.isMainStatgeFocused()) {
                    MainScreenController.setMainStageIconified(true);
                }
                MainScreenController.setMainStageIconified(false);

                Platform.runLater(() -> {
                    updateWorkingTaskAndRecordWorkStatusLog(WorkStatusLog.WorkStatus.BREAK, false);
                });
            }
        }
    }
    
    private void checkAppUpdateAvialability() {
        CheckAppUpdateService service = new CheckAppUpdateService();
        service.setOnSucceeded((var event) -> {
            Response result = service.getValue();
            
            if (result.isError()) {
                // error handling
                return;
            }
            
            JsonObject resultData = result.getData().getAsJsonObject();
            boolean availability = resultData.get("availability").getAsBoolean();
            HeaderNavigation.setAlertVisibility(StateName.SETTINGS_NAVIGATION_ITEM, availability);
        });
        service.start();
    }
    
    private void updateWorkingTaskAndRecordWorkStatusLog(WorkStatusLog.WorkStatus nextState, boolean isUserAction) {
        
        PopupDialogBox popupDialogBox = new PopupDialogBox();
        PopupDialogHeading popupDialogHeading = new PopupDialogHeading(
                PopupDialogHeading.PopupType.WARNING, "Update Task Error"
        );
        popupDialogBox.setHeadingComponent(popupDialogHeading);
       popupDialogBox.setDescription(
                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                "Unexpected error occurred while updaing working task, please try again"
        );
        JFXButton closeBtn = popupDialogBox.getDialogButton(
                PopupDialogBox.DialogButton.RIGHT_MOST
        );
        closeBtn.setVisible(true); closeBtn.setDisable(false);
        closeBtn.setText("Close");
        closeBtn.setOnAction((var event) -> {
            popupDialogBox.close();
        });
        
        ProjectViewModel projectViewModel = StateStorage.getCurrentState(
                StateName.PROJECT_VIEWMODEL
        );

        if (projectViewModel == null) {
            recordWorkStatusLog(nextState, isUserAction);
            return;
        }
        WorkingTaskUpdater workingTaskUpdater = new WorkingTaskUpdater(projectViewModel);
        workingTaskUpdater.setOnSucceeded((var stateEvent) -> {
            Response taskUpdate = workingTaskUpdater.getValue();

            if (taskUpdate.isError()) {
                popupDialogBox.load();
                return;
            }

            recordWorkStatusLog(nextState, isUserAction);
        });
        workingTaskUpdater.start();
    }
    
    private void recordWorkStatusLogsOnSlientMode(WorkStatusLog.WorkStatus nextStatus) throws Exception {
        
        if (nextStatus.equals(WorkStatusLog.WorkStatus.BREAK)) {
            SimpleObjectProperty<ProjectTask> taskProperty = ProjectViewModel.getCURRENT_WORKING_TASK();
            ProjectTask currentTask = taskProperty.get();
            StateStorage.set(StateName.RECENT_WORKING_TASK, ProjectTask.class, currentTask);
        }
        
        if (nextStatus.equals(WorkStatusLog.WorkStatus.START)) {
            ProjectTask projectTask = StateStorage.getCurrentState(StateName.RECENT_WORKING_TASK);
            ProjectViewModel projectViewModel = StateStorage.getCurrentState(StateName.PROJECT_VIEWMODEL);
            
            if (projectTask != null && projectViewModel != null) {
                projectViewModel.startRecentTaskOnSlientMode(projectTask);
            }
        }
        recordWorkStatusLog(nextStatus, false);
    }
    
    private void HourlyTask(ActionEvent event)  {
    
        
try {
            
            strjitter="0";
            struploadSpeed="0";
            strdownloadSpeed="0";
            updateNetworkSpeeds();
             startJitter();
             
          
             
             
             struploadSpeed= struploadSpeed.replace("Upload:", "");
             strdownloadSpeed= strdownloadSpeed.replace("Download:", "");
             strPing= strPing.replace("Ping:", "");
             strPing= strPing.replace("ms", "");
             
          AuthenticationModule authenticationModule = new AuthenticationModule();
                        JsonObject bandwidthDetails = new JsonObject();
                        bandwidthDetails.addProperty("Jitter", strjitter);
                        bandwidthDetails.addProperty("downloadSpeed", strdownloadSpeed);
                        bandwidthDetails.addProperty("uploadSpeed", struploadSpeed);
                        bandwidthDetails.addProperty("ping", strPing);
                       
                        
                       Response response = authenticationModule.syncBandwidth(bandwidthDetails);

             
                    
                } catch (URISyntaxException ex) {
                    Logger.getLogger(AuthenticatedMainWindowViewModel.class.getName()).log(Level.SEVERE, null, ex);
                }
      
    }
    private  void updateNetworkSpeeds() throws URISyntaxException {
        long currentBytesSent = 0;
        long currentBytesReceived = 0;
         String filePath="";
        try {
       //  URL resourceUrl = getClass().getResource("C://speedtest-cli.exe");

          //  if (resourceUrl != null) {
          //     filePath = new File(resourceUrl.toURI()).getAbsolutePath();
          //  } else {
          //      Platform.runLater(() -> filePathLabel.setText("File not found in the output directory."));
          //  }
            ProcessBuilder processBuilder = new ProcessBuilder("C://speedtest-cli.exe", "--simple");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Download:")) {
                    strdownloadSpeed=line;
                   // Platform.runLater(() -> downloadSpeedLabel=("Download Speed: " + line));
                } else if (line.startsWith("Upload:")) {
                    struploadSpeed=line;
                }
                else if (line.startsWith("Ping:")) {
                    strPing=line;
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Handle errors
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
   
    public void startJitter() {
        latList = new ArrayList<>();

        // Send 20 ping requests to "google.com" and collect round-trip times
        for (int i = 0; i < 20; i++) {
            try {
                InetAddress address = InetAddress.getByName("google.com"); // Replace with your target host
                long startTime = System.currentTimeMillis();
                boolean reachable = address.isReachable(1000); // Timeout set to 1000 ms (1 second)
                long endTime = System.currentTimeMillis();

                if (reachable) {
                    long roundtripTime = endTime - startTime;
                    latList.add((double) roundtripTime);
                }
                
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       calculateJitter(latList);
    }
    private void calculateJitter(List<Double> latList) {
        List<Double> latList2 = new ArrayList<>();
        for (int i = 1; i < latList.size(); i++) {
            double latVal = Math.abs(latList.get(i - 1) - latList.get(i));
            latList2.add(latVal);
        }

        List<Double> latList3 = new ArrayList<>();
        for (Double lat : latList2) {
            if (!lat.equals(0.0)) {
                latList3.add(lat);
            }
        }

        double latAverage = latList3.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        strjitter=String.valueOf(latAverage);
    }
   
  private static String getIPv6Address() throws IOException {
        URL url = new URL("https://api6.ipify.org");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return reader.readLine();
        }
    }

    private static String getGeoLocation(String ipAddress) throws IOException {
       URL url = new URL("https://ipapi.co/" + ipAddress + "/json/");

     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder jsonResponse = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
        }

        
        
        
        
        return jsonResponse.toString();
    }
    
    private static void getlocation() throws IOException{
     try {
        
           String ipAddress = getIPv6Address();
           long CurrentsunixTimestamp = Instant.now().getEpochSecond();

            String geoLocation = getGeoLocation(ipAddress);
            if (geoLocation != null) {
             AuthenticationModule authenticationModule = new AuthenticationModule();
             
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(geoLocation.toString(), JsonObject.class);
            
            String apiKey = "AIzaSyA5gEYIDrr7UL6StikpWE43CKfRWnB-ATU"; // Replace with your actual API key
            
           
           try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://www.googleapis.com/geolocation/v1/geolocate?key=" + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

           // JSONObject requestBody = new JSONObject();
            //requestBody.put("considerIp", true);

            //StringEntity requestEntity = new StringEntity(requestBody.toString());
            //httpPost.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String jsonString = EntityUtils.toString(entity);
                JSONObject jsonResponse = new JSONObject(jsonString);

                if (jsonResponse.has("location")) {
                    JSONObject location = jsonResponse.getJSONObject("location");
                    double latitude = location.getDouble("lat");
                    double longitude = location.getDouble("lng");

                //    System.out.println("Latitude: " + latitude);
                 //   System.out.println("Longitude: " + longitude);

                    // Use latitude and longitude as needed
                } else {
                    System.out.println("Location data not found in response.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

             JsonObject locdetails = new JsonObject();
             locdetails.addProperty("dataCreatedTime", CurrentsunixTimestamp);
             locdetails.addProperty("longitude", jsonObject.get("longitude").getAsString());
             locdetails.addProperty("latitude", jsonObject.get("latitude").getAsString());
             locdetails.addProperty("country", jsonObject.get("country_name").getAsString());
             locdetails.addProperty("state", jsonObject.get("region").getAsString());
             locdetails.addProperty("city", jsonObject.get("city").getAsString());
             locdetails.addProperty("timezone", jsonObject.get("timezone").getAsString());
            
             
             
           //  Response response = authenticationModule.syncGeoLocation(locdetails);

            } else {
                System.out.println("Failed to retrieve geolocation.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }
  
 
}

