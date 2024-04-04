/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author dmhashan
 */
public class OtherBreakReasonComponentModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(OtherBreakReasonComponentModel.class);

    private final SimpleStringProperty otherReasonTextInputProperty = new SimpleStringProperty();
    private final SimpleStringProperty otherReasonErrorLabelTextProperty = new SimpleStringProperty();

    public SimpleStringProperty getOtherReasonTextInputProperty() {
        return otherReasonTextInputProperty;
    }

    public SimpleStringProperty getOtherReasonErrorLabelTextProperty() {
        return otherReasonErrorLabelTextProperty;
    }
}
