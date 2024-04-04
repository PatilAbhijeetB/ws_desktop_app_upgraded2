/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox.ButtonWrapperContainer;
import de.saxsys.mvvmfx.ViewModel;
import com.jfoenix.controls.JFXButton;

/**
 *
 * @author chamara
 */
public class PopupDialogButtonLayoutViewModel implements ViewModel {
    
    // button 01 is named as rightMostBtn
    private JFXButton rightMostBtn;
    
    // button 02 is named as rightCenterBtn
    private JFXButton rightCenterBtn;
    
    // button 03 is named as leftMostBtn
    private JFXButton leftMostBtn;
    
    public PopupDialogButtonLayoutViewModel() {
        
    }
    
    void setRightMostButton(JFXButton button) {
        this.rightMostBtn = button;
    }
    
    public JFXButton getRightMostButton() {
        return this.rightMostBtn;
    }
    
    void setRightCenterBtn(JFXButton button) {
        this.rightCenterBtn = button;
    }
    
    public JFXButton getRightCenterButton() {
        return this.rightCenterBtn;
    }
    
    void setLeftMostBtn(JFXButton button) {
        this.leftMostBtn = button;
    }
    
    public JFXButton getLeftMostButton() {
        return this.leftMostBtn;
    }
    
    
    
}
