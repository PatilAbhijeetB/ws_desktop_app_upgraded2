/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.mainwindow.meeting;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;

/**
 *
 * @author jade_m
 */
public class ParticipantListItem implements Initializable, FxmlView<ParticipantListItemModel> {
    
    @FXML
    private HBox rootWrapper;

    @FXML
    private RadioButton participantRadioBtn;
    
    @InjectViewModel
    private ParticipantListItemModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        participantRadioBtn.textProperty().bind(viewModel.getParticipantName());
        participantRadioBtn.selectedProperty().bindBidirectional(viewModel.getItemSelected());
        participantRadioBtn.setOnAction(viewModel::onClickOnRadioBtn);
    }
    
    String getParticipantName() {
        return participantRadioBtn.getText();
    }
}
