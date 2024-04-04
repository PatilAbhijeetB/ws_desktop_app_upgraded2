package com.workshiftly.presentation.mainwindow.meeting;

import com.jfoenix.controls.JFXButton;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.ProjectTask;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 * @author jade_m
 */
public class MeetingInfoModalView implements Initializable, FxmlView<MeetingInfoModalViewModel> {
    
    @FXML
    private VBox rootWrapper;

    @FXML
    private Pane formElementWrapper;

    @FXML
    private Label formErrorTxt;

    @FXML
    private Pane subFormContentWrapper;

    @FXML
    private ComboBox<CompanyProject> projectSelectInput;

    @FXML
    private Label meetingPurposeCharCount;

    @FXML
    private TextArea meetingSummaryTxtInput;

    @FXML
    private Pane participantMainWrapper;

    @FXML
    private Pane formInputParticipantWrapper;

    @FXML
    private Pane participantTxtInputWrapper;

    @FXML
    private TextField participantTxtInput;

    @FXML
    private Label participantSlotCountLbl;

    @FXML
    private Pane participantLblWrapper;

    @FXML
    private ImageView addParticipantPlusMark;

    @FXML
    private HBox participantListViewHbox;

    @FXML
    private ListView<Parent> participantLeftList;

    @FXML
    private ListView<Parent> participantRightList;

    @FXML
    private HBox meetingBtnWrapper;

    @FXML
    private VBox meetingTimeWrapper;

    @FXML
    private Label elapsedTimeCounter;

    @FXML
    private JFXButton startMeetingBtn;

    @FXML
    private JFXButton cancelMeetingBtn;
    @InjectViewModel
    private MeetingInfoModalViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            participantTxtInput.setPromptText("Enter participant name");
            participantTxtInput.setOnKeyPressed((var keyEvent) -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    onAddMeetingParticipant();
                }
            });
            
            viewModel.setProjectSelectInput(projectSelectInput);
            projectSelectInput.setItems(viewModel.getCompanyProjects());
            projectSelectInput.setConverter(new StringConverter<CompanyProject>() {
                @Override
                public String toString(CompanyProject project) {
                    //return project != null ? project.getName().toUpperCase() : null;
                     return project != null ? project.getName() : null;
                }

                @Override
                public CompanyProject fromString(String value) {
                    ObservableList<CompanyProject> companyProjects 
                            = viewModel.getCompanyProjects();
                   return companyProjects.parallelStream().filter((CompanyProject project) -> {
                       String projectName = project.getName();
                       return projectName.equals(value);
                   }).findFirst().orElse(null); 
                }
            });
            
            viewModel.getCompanyProjects().addListener((Observable change) -> {
                if (!viewModel.getCompanyProjects().isEmpty()) {
                    ProjectTask meetingTask = viewModel.getMeetingTask().get();
                
                    if (meetingTask != null) {
                        CompanyProject project = viewModel.getCompanyProjects()
                                .parallelStream()
                                .filter((CompanyProject curProject) -> {
                                    String projectId = meetingTask.getProjectId(); 
                                    return curProject != null 
                                            && projectId.equals(curProject.getId());
                                }).findFirst().orElse(null);
//                        projectSelectInput.setValue(project);
                    }
                }
            });
            
            SimpleBooleanProperty isMeetingStartedProp = viewModel.getDidStartMeeting();
            isMeetingStartedProp.addListener((var ov, var oldValue, var newValue) -> {
                Platform.runLater(() -> {
                    boolean didStarted = newValue;
                    projectSelectInput.setDisable(didStarted);
                    participantTxtInput.setDisable(didStarted);
                    meetingSummaryTxtInput.setDisable(didStarted);
                    addParticipantPlusMark.setVisible(!didStarted);
                    participantSlotCountLbl.setVisible(!didStarted);
                    meetingPurposeCharCount.setVisible(!didStarted);
                    startMeetingBtn.setVisible(!didStarted);
                    participantLeftList.setDisable(didStarted);
                    participantRightList.setDisable(didStarted);
                    
                    cancelMeetingBtn.setText("End Meeting");
                    cancelMeetingBtn.setStyle("-fx-background-color: #f9a825");
                    cancelMeetingBtn.setOnAction(viewModel::onClickMeetingButton);
                });
                
            });
            
            formErrorTxt.textProperty().bind(viewModel.getFormErrorTxt());
            meetingTimeWrapper.visibleProperty().bind(viewModel.getDidStartMeeting());
            
            startMeetingBtn.textProperty().bind(viewModel.getMeetingBtnTxt());
            startMeetingBtn.setOnAction(viewModel::onClickMeetingButton);
            
            elapsedTimeCounter.textProperty().bind(viewModel.getMeetingElapsedTime());
            meetingSummaryTxtInput.textProperty()
                    .bindBidirectional(viewModel.getMeetingSummary());
            
            viewModel.setMeetingBtnWrapper(meetingBtnWrapper);
            
            // Cancel Meeting button action
            cancelMeetingBtn.setOnAction((var event) -> {
                viewModel.onClickMeetingCloseButton();
            });
            
            // setting up plus image into addParticipantPlusMark imageView
            final String pluseImgResource = "/images/icons/meeting_info_modal/plus.png";
            InputStream plusImgStream = getClass().getResourceAsStream(pluseImgResource);
            Image pluseIconImage = new Image(plusImgStream);
            addParticipantPlusMark.setImage(pluseIconImage);
            
            // addParticipantPlusMark Mouse click action
            addParticipantPlusMark.setOnMouseClicked((var event) -> {
                onAddMeetingParticipant();
            });
            
            
            // participantSlotCountLbl configurations
            participantSlotCountLbl.setText("Available slots:10");
            participantSlotCountLbl.textProperty().bind(viewModel.getParticipantSlotCountLbl());
            
            
            // meetingPurposeCharCount configurations
            meetingPurposeCharCount.setText("Characters: 200");
            meetingSummaryTxtInput.textProperty().addListener((
                    var obValue, var oldInput, var newInput
            ) -> {
                if (newInput != null) {
                    int maxCharCount = 200;
                    int currentCount = newInput.length();
                    
                    if (currentCount > maxCharCount) {
                        meetingSummaryTxtInput.textProperty().set(oldInput);
                        return;
                    }
                    
                    int remainCharCount = maxCharCount - currentCount;
                    String newTxtValue = String.format("Characters: %d", remainCharCount);
                    meetingPurposeCharCount.textProperty().set(newTxtValue);
                }
            });
            
            participantLeftList.setItems(viewModel.getLeftParticipantListItems());
            participantRightList.setItems(viewModel.getRightParticipantListItems());
        } catch (Exception ex) {
            System.out.println("#### Exception ex >>>>> " + ex);
        }
    }
    
    private void onAddMeetingParticipant() {
        String participant = participantTxtInput.getText();
        boolean isAdded = viewModel.addMeetingParticipant(participant);

        if (isAdded) {
            participantTxtInput.setText("");
        }
    }
}
