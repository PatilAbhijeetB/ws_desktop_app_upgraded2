/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author jade_m
 */
public class TagItemViewModel implements ViewModel {
    
    private final SimpleBooleanProperty closeBtnWrapperVisiblity;
    private final SimpleStringProperty tagDisplayText;
    
    public TagItemViewModel() {
        this.closeBtnWrapperVisiblity = new SimpleBooleanProperty(false);
        this.tagDisplayText = new SimpleStringProperty();
    }
    
    public void setCloseBtnWrapperVisiblity (boolean value) {
        this.closeBtnWrapperVisiblity.set(value);
    }
    
    SimpleBooleanProperty getcloseBtnWrapperVisiblity() {
        return this.closeBtnWrapperVisiblity;
    }
    
    public void setTagDisplayText(String value) {
        this.tagDisplayText.set(value);
    }
    
    SimpleStringProperty getTagDisplayText() {
        return this.tagDisplayText;
    }
}
