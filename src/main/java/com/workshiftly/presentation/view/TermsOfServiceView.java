/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.google.gson.JsonObject;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.presentation.viewmodel.TermsOfServiceViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

/**
 *
 * @author chamara
 */
public class TermsOfServiceView implements Initializable, FxmlView<TermsOfServiceViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(TermsOfServiceView.class);

    @FXML
    private Pane termsOfServiceWrapper;

    @FXML
    private WebView webView;

    @FXML
    private ButtonBar btnWrapper;

    @FXML
    private Button acceptBtn;

    @FXML
    private Button declineBtn;
    
     @FXML
    private Pane acceptRadioBtnWrapper;

    @FXML
    private CheckBox acceptRadioBtn;

    
    @InjectViewModel
    private TermsOfServiceViewModel viewModel;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        JsonObject latestUserTNCRecord = viewModel.getLatestTNCUserRecord();
        JsonObject TNCDocumentMeta = latestUserTNCRecord.getAsJsonObject("documentMeta");
        String documentRemoteLink = TNCDocumentMeta.get("link").getAsString();
        webView.getEngine().load(documentRemoteLink);
        
        acceptBtn.disableProperty().set(true);
        acceptRadioBtn.selectedProperty().addListener(((var observable, var oldValue, var newValue) -> {
            this.acceptBtn.disableProperty().set(!newValue);
        }));
        
        acceptBtn.setOnMouseClicked((var mouseEvent) -> {
            String action = "accepted";
            viewModel.handleAcceptance(action);
        });
        
        declineBtn.setOnMouseClicked((var mouseEvent) -> {
            String action = "declined";
            viewModel.handleAcceptance(action);
        });
    }
    
    
}
