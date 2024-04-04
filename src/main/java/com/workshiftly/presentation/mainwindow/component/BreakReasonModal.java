/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.common.model.BreakReason;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

/**
 *
 * @author Hashan@Hirosh
 */
public class BreakReasonModal implements Initializable, FxmlView<BreakReasonModalModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(BreakReasonModal.class);

    private final Node otherBreakReasonComponentNode;
    private final OtherBreakReasonComponentModel otherBreakReasonComponentModel;

    @FXML
    private Pane breakReasonPane;

    @FXML
    private Label breakReasonLabel;

    @FXML
    private ComboBox<BreakReason> breakReasonComboBox;

    @FXML
    private Pane otherReasonPane;

    @FXML
    private Label breakReasonErrorLabel;

    @InjectViewModel
    private BreakReasonModalModel viewModel;

    public BreakReasonModal() {
        ViewTuple<OtherBreakReasonComponent, OtherBreakReasonComponentModel> otherBreakReasonComponentTuple
                = FluentViewLoader.fxmlView(OtherBreakReasonComponent.class).load();

        otherBreakReasonComponentModel = otherBreakReasonComponentTuple.getViewModel();
        otherBreakReasonComponentNode = otherBreakReasonComponentTuple.getView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        breakReasonComboBox.setItems(viewModel.getBreakReasonList());

        breakReasonComboBox.setConverter(new StringConverter<BreakReason>() {
            @Override
            public String toString(BreakReason breakReason) {
                return breakReason != null ? breakReason.getTitle() : null;
            }

            @Override
            public BreakReason fromString(String string) {
                return viewModel.getBreakReasonList().parallelStream()
                        .filter(el -> el.getTitle().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        breakReasonComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            viewModel.onChangeReason(newSelection);
        });

        viewModel.getOtherReasonTxtAreaVibilityProp().addListener((o) -> {
            if (viewModel.getOtherReasonTxtAreaVibilityProp().get()) {
                otherReasonPane.getChildren().add(otherBreakReasonComponentNode);
            } else {
                otherReasonPane.getChildren().clear();
            }
        });

        otherBreakReasonComponentModel.getOtherReasonTextInputProperty().addListener((o) -> {
            String otherReasonText = otherBreakReasonComponentModel.getOtherReasonTextInputProperty().get();

            try {
                viewModel.onChangeOtherReason(otherReasonText);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "On Change Other Reason Exception", ex);
            }
        });

        viewModel.setOtherReasonErrorLabelTextProperty(otherBreakReasonComponentModel.getOtherReasonErrorLabelTextProperty());
        breakReasonErrorLabel.textProperty().bind(viewModel.getBreakReasonErrorLabelTextProperty());
    }
}
