/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.workshiftly.presentation.viewmodel.AppDownloadAlertViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;


/**
 *
 * @author jade_m
 */
public class AppDownloadAlertView extends Region implements Initializable, FxmlView<AppDownloadAlertViewModel> {
    
    @FXML
    private Pane rootWrapper;

    @FXML
    private Button DownlaodNowBtn;

    @FXML
    private Button RemindLaterBtn;

   

    @InjectViewModel
    private AppDownloadAlertViewModel viewModel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        DownlaodNowBtn.setOnAction((var actionEvent) -> {
            System.out.println("##### download now btn >>>>> actionEvent >>>> " + actionEvent);
            viewModel.onClickDownloadNowBtn();
        });
        
        RemindLaterBtn.setOnAction((var actionEvent) -> {
            System.out.println("##### RemindLater Btn >>>> actionEvent >>>> " + actionEvent);
            viewModel.onClickRemindMeLater();
        });
        
    }
    
}
