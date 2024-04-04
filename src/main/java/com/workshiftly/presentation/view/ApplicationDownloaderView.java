/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.workshiftly.presentation.viewmodel.ApplicationDownloaderViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 *
 * @author jade_m
 */
public class ApplicationDownloaderView  implements Initializable, FxmlView<ApplicationDownloaderViewModel> {

    @FXML
    private Label downloadStatusTxt;

    @FXML
    private ProgressBar downloadProgressBar;

    @FXML
    private Label progressTxtLbl;
    
    @FXML
    private Button completeBtn;

    @InjectViewModel
    public ApplicationDownloaderViewModel viewModel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        downloadStatusTxt.textProperty().bind(viewModel.getStatusTxtProp());
        progressTxtLbl.textProperty().bind(viewModel.getProgressPrecentageProp());
        downloadProgressBar.progressProperty().bind(viewModel.getProgressBarValueProp());
        
        completeBtn.disableProperty().bind(viewModel.getCompleteBtnVisibility().not());
        completeBtn.setOnAction(viewModel::onClickCompleteBtn);
    }
    
}
