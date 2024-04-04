/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

/**
 *
 * @author dmhashan
 */
public class OtherBreakReasonComponent implements Initializable, FxmlView<OtherBreakReasonComponentModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(OtherBreakReasonComponent.class);

    @FXML
    private Pane otherReasonPane;

    @FXML
    private Label otherReasonLabel;

    @FXML
    private Label otherReasonErrorLabel;

    @FXML
    private TextArea otherReasonTextArea;

    @InjectViewModel
    private OtherBreakReasonComponentModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        viewModel.getOtherReasonTextInputProperty().bind(otherReasonTextArea.textProperty());
        otherReasonErrorLabel.textProperty().bind(viewModel.getOtherReasonErrorLabelTextProperty());
    }
}
