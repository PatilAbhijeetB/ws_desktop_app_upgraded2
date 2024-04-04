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
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author chamara
 */
public class PopupDialogLayoutView implements FxmlView<PopupDialogLayoutViewModel>, Initializable {
    
    @FXML
    private StackPane rootWrapper;
    
    @FXML
    private Label mainContentDescription;

    @FXML
    private Label mainContentSubDescription;
    
    @InjectViewModel
    private PopupDialogLayoutViewModel viewModel;
    

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        rootWrapper.setManaged(true);
        viewModel.setMainContentDescriptionEntity(mainContentDescription);
        viewModel.setMainContentSubDescriptionEntity(mainContentSubDescription);
        
        mainContentDescription.visibleProperty().bind(viewModel.getMainContentDescriptionVisibility());
        mainContentSubDescription.visibleProperty().bind(viewModel.getMainContentSubDescriptionVisibility());
        
        mainContentDescription.textProperty().bind(viewModel.getMainContentDescriptionText());
        mainContentSubDescription.textProperty().bind(viewModel.getMainContentSubDescriptionText());
        
       mainContentDescription.setTextAlignment(TextAlignment.CENTER);
        mainContentSubDescription.setTextAlignment(TextAlignment.CENTER);
        
         mainContentDescription.getStyleClass().add("dialog-modal-bodytext");
        mainContentDescription.setTextAlignment(TextAlignment.CENTER);
    }
    
}
