/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 *
 * @author jade_m
 */
public class TagItemView implements  Initializable, FxmlView<TagItemViewModel> {

    @FXML
    private Pane rootWrapper;

    @FXML
    private HBox secondryRootWrapper;

    @FXML
    private Label displayTxt;

    @FXML
    private Pane closeSignWrapper;
    
    @InjectViewModel
    private TagItemViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        displayTxt.textProperty().bind(viewModel.getTagDisplayText());
        
        closeSignWrapper.visibleProperty()
                .bind(viewModel.getcloseBtnWrapperVisiblity());
        
        closeSignWrapper.setOnMouseClicked(((MouseEvent event) -> {
            System.out.println("#### closeSignWrapper clicked");
        }));
        
    }
    
}
