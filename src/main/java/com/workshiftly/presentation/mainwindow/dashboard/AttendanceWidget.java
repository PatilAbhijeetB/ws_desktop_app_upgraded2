/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 *
 * @author Hashan
 */
public class AttendanceWidget implements Initializable, FxmlView<AttendanceWidgetModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(AttendanceWidget.class);

    private static final String LOADING_SPINNER_IMAGE_PATH = "/images/loading_spinner.gif";
   
    @FXML
    private Pane rootWrapper;
    
    @FXML
    private DatePicker datePicker;

    @FXML
    private Label shiftInLabel;

    @FXML
    private Label actualInLabel;

    @FXML
    private Label shiftOutLabel;

    @FXML
    private Label actualOutLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Pane loadingSpinner;
    
  

    @FXML
    private ImageView loadingSpinnerImg;
    
    @FXML
    private Text strCurrentDay;
    
    @FXML
    private Text strCurrentDaym1;
    
    @FXML
    private Text strCurrentDayp1;
    
    @FXML
    private Text strCurrentDayp2;
    
    @FXML
    private Text strCurrentDayp3;
    
    @FXML
    private Text strCurrentDayp4;
    
    @FXML
    private Text strCurrentDayp5;
   
    @FXML
    private Text strCurrentDate;
    
    @FXML
    private Text strCurrentDatem1;
    
    @FXML
    private Text strCurrentDatep1;
    
    @FXML
    private Text strCurrentDatep2;
    
    @FXML
    private Text strCurrentDatep3;
    
    @FXML
    private Text strCurrentDatep4;
    
    @FXML
    private Text strCurrentDatep5;
    
    @FXML
    private Text strMonthName;
   
    @InjectViewModel
    private AttendanceWidgetModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
       
        try {
            InputStream inputStream = getClass().getResourceAsStream(LOADING_SPINNER_IMAGE_PATH);
            Image loadingSpinnerImage = new Image(inputStream);
            loadingSpinnerImg.setImage(loadingSpinnerImage);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Attendance Widget Initialization Exception", ex);
        }

        loadingSpinner.visibleProperty().bind(viewModel.getLoadingProperty());
        shiftInLabel.textProperty().bind(viewModel.getShiftInLabelTextProperty());
        shiftOutLabel.textProperty().bind(viewModel.getShiftOutLabelTextProperty());
        actualInLabel.textProperty().bind(viewModel.getActualInLabelTextProperty());
        actualOutLabel.textProperty().bind(viewModel.getActualOutLabelTextProperty());
        statusLabel.textProperty().bind(viewModel.getStatusLabelTextProperty());
        statusLabel.backgroundProperty().bind(viewModel.getStatusLabelColorProperty());
        
       
        
        
        datePicker.valueProperty().bindBidirectional(viewModel.getDatePickerDate());
        
        strCurrentDay.textProperty().bind(viewModel.getDayNameProperty());
        strCurrentDaym1.textProperty().bind(viewModel.getDayNamem1Property());
        strCurrentDayp1.textProperty().bind(viewModel.getDayNamep1Property());
        strCurrentDayp2.textProperty().bind(viewModel.getDayNamep2Property());
        strCurrentDayp3.textProperty().bind(viewModel.getDayNamep3Property());
        strCurrentDayp4.textProperty().bind(viewModel.getDayNamep4Property());
        strCurrentDayp5.textProperty().bind(viewModel.getDayNamep5Property());
        
        strCurrentDate.textProperty().bind(viewModel.getDayNumProperty());
        strCurrentDatem1.textProperty().bind(viewModel.getDayNumm1Property());
        strCurrentDatep1.textProperty().bind(viewModel.getDayNump1Property());
        strCurrentDatep2.textProperty().bind(viewModel.getDayNump2Property());
        strCurrentDatep3.textProperty().bind(viewModel.getDayNump3Property());
        strCurrentDatep4.textProperty().bind(viewModel.getDayNump4Property());
        strCurrentDatep5.textProperty().bind(viewModel.getDayNump5Property());
        
        strMonthName.textProperty().bind(viewModel.getMonthNameProperty());
        
        
        
        
        datePicker.setOnAction((ActionEvent actionEvent) -> {
         viewModel.onChangeDate();
        });
    }
    
}
