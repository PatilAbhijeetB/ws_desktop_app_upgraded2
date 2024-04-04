/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.project;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 *
 * @author hashan
 */
public class TaskView implements Initializable, FxmlView<TaskViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(TaskView.class);

    @FXML
    private Label titleLabel;

    @FXML
    private Label descLabel;
    
    @FXML
    private Label projNameLabel;

    @FXML
    private Label spendTimeLabel;

    
    @FXML
    private Button pauseBtn;

    @FXML
    private Button stopBtn;
    
    @FXML
    private Button expBtn;
    
    @FXML
    private Button lessBtn;
    
    @FXML
    private ImageView playBtnImageView;
    
    @FXML
    private ImageView lessBtnImageView;
    
    @FXML
    private ImageView stopBtnImageView;
    
     @FXML
    private ImageView ExpBtnImageView;
    
    @FXML
    private Pane btnWrapper;
    
    @FXML
    private Pane expBtnWrapper;
    
     @FXML
    private Button taskEditBtn;

    @FXML
    private ImageView taskEditBtnImg;
    
    @FXML
    private Pane descPanel;
    
    @FXML
    private Line sepLine;
    
    @FXML
    private VBox lstboxitem;
    
    @InjectViewModel
    private TaskViewModel viewModel;

    private double originalHeight;
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        titleLabel.textProperty().bind(viewModel.getTitleLabelTextProperty());
        descLabel.textProperty().bind(viewModel.getDescLabelTextProperty());
        projNameLabel.textProperty().bind(viewModel.getProjNameLabelTextProperty());
        //dueDateLabel.textProperty().bind(viewModel.getDueDateLabelTextProperty());
        //estimatedTimeLabel.textProperty().bind(viewModel.getEstimatedTimeLabelTextProperty());
        spendTimeLabel.textProperty().bind(viewModel.getSpendTimeLabelTextProperty());
        descPanel.setVisible(false);
        playBtnImageView.imageProperty().bind(viewModel.getPlayBtnImageProperty());
        stopBtnImageView.imageProperty().bind(viewModel.getStopBtnImageProperty());
        ExpBtnImageView.imageProperty().bind(viewModel.getExpBtnImageProperty());
        lessBtnImageView.imageProperty().bind(viewModel.getLessBtnImageProperty());
        pauseBtn.disableProperty().bind(viewModel.getPlayPuaseBtnDisableProperty());
        btnWrapper.visibleProperty().bind(viewModel.getListViewBtnVisibilityProperty());
        stopBtn.visibleProperty().bind(viewModel.getStopBtnDisableProperty().not());
        lessBtn.setVisible(false);
        sepLine.setVisible(false);
        originalHeight = lstboxitem.getHeight();
        pauseBtn.setOnAction((ActionEvent event) -> {
            viewModel.onClickStartPauseBtn(event);
        });
        stopBtn.setOnAction((ActionEvent event) -> {
            viewModel.onClickStopBtn(event);
        });
        
       
        expBtn.setOnAction((ActionEvent event)->{
        expBtn.setVisible(false);
        descPanel.setVisible(true);
        lessBtn.setVisible(true);
        sepLine.setVisible(true);
        //animateHeightChange(lstboxitem, true);
       // animateHeightIncrease(lstboxitem, calculateVBoxHeight());
        });
        
        lessBtn.setOnAction((ActionEvent event)->{
        lessBtn.setVisible(false);
        descPanel.setVisible(false);
        expBtn.setVisible(true);
        sepLine.setVisible(false);
     //   animateHeightChange(lstboxitem, false);
    // animateHeightDecrease(lstboxitem, originalHeight);
        });
        
        try {
            taskEditBtnImg.setImage(new Image(getClass()
                    .getResourceAsStream("/images/mainwindow/Task.png")));
            taskEditBtn.visibleProperty().bind(viewModel.getTaskEditBtnVisibility());
            taskEditBtn.setOnAction(viewModel::onClickTaskEditBtn);
        } catch (Exception ex) {
            System.out.println("#### TaskView >>> init >>> EX " + ex);
        }
       
        
    }
    
    
   
 
}
