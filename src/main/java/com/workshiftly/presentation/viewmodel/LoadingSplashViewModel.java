/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author chamara
 */
public class LoadingSplashViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LoadingSplashViewModel.class);

    private final SimpleDoubleProperty FADE_TRANSTION_FROM_VALUE = new SimpleDoubleProperty(0.7);
    private final SimpleDoubleProperty FADE_TRANSTION_TO_VALUE = new SimpleDoubleProperty(0.9);
    private final SimpleIntegerProperty TRANSITION_TIMEOUT = new SimpleIntegerProperty(1);
    
    public SimpleDoubleProperty fadeTransitionFromValue() {
        return FADE_TRANSTION_FROM_VALUE;
    }
    
    public SimpleDoubleProperty fadeTransitionToValue() {
        return FADE_TRANSTION_TO_VALUE;
    }
    
    public SimpleIntegerProperty fadeTranstiitonTimeout() {
        return TRANSITION_TIMEOUT;
    }
    
}
