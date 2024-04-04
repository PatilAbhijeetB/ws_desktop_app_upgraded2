/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import com.jfoenix.controls.JFXButton;

/**
 *
 * @author chamara
 */
public class PopupDialogButtonLayout implements FxmlView<PopupDialogButtonLayoutViewModel>, Initializable {

    @FXML
    private HBox rootWrapper;

    @FXML
    private HBox leftWrapper;

    @FXML
    private JFXButton dialogBtn3;

    @FXML
    private HBox rightWrapper;

    @FXML
    private JFXButton dialogBtn2;

    @FXML
    private JFXButton dialogBtn1;
    
    @InjectViewModel
    private PopupDialogButtonLayoutViewModel viewModel;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        viewModel.setRightMostButton(dialogBtn1);
        viewModel.setRightCenterBtn(dialogBtn2);
        viewModel.setLeftMostBtn(dialogBtn3);
        
        dialogBtn1.setVisible(false);
        dialogBtn2.setVisible(false);
        dialogBtn3.setVisible(false);
        
        
    }
    
}
