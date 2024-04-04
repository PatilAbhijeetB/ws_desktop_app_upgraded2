/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 *
 * @author chamara
 */
public class PopupDialogHeadingView implements FxmlView<PopupDialogHeadingViewModel>, Initializable {
    
    @FXML
    private HBox baseWrapper;

    @FXML
    private Pane iconWrapper;

    @FXML
    private Pane headingWrapper;
    
    @FXML
    private ImageView headingImageView;

    @FXML
    private Label headingTxtLbl;
    
    @InjectViewModel
    private PopupDialogHeadingViewModel viewModel;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        headingImageView.imageProperty().bind(viewModel.getHeaderImageProperty());
        headingTxtLbl.textProperty().bind(viewModel.getHeadingTextProperty());
        headingTxtLbl.getStyleClass().add("dialog-modal-heading");
    }
    
}
