/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.Node;
import javafx.scene.Parent;


/**
 *
 * @author chamara
 */
public class PopupDialogHeading {
    
    private Node parentNode;
    private PopupType headingType;
    private String headingTxt;
    private PopupDialogHeadingViewModel viewModel;
    
    public enum PopupType {
        WARNING,
        INFORMATION,
        SUCCESS,
        ERROR;
    }
    
    private PopupDialogHeading() {
        
    }
    
    public PopupDialogHeading(PopupType type) {
        
        FluentViewLoader.FxmlViewStep<PopupDialogHeadingView, PopupDialogHeadingViewModel> fxmlView 
                = FluentViewLoader.fxmlView(PopupDialogHeadingView.class);
        ViewTuple<PopupDialogHeadingView, PopupDialogHeadingViewModel> viewTuple = fxmlView.load();
        
        this.viewModel  = viewTuple.getViewModel();
        this.headingType = type;
        this.parentNode = viewTuple.getView();       
    }
    
    public PopupDialogHeading(PopupType popupType, String heading) {
        
        this(popupType);
        this.headingTxt = heading;
        this.viewModel.setHeadingTextPropertyValue(heading);
    }

    public PopupType getHeadingType() {
        return headingType;
    }

    public void setHeadingType(PopupType popupType) {
        
        this.headingType = popupType;
        this.viewModel.setPopupTypeProperty(popupType);
    }

    public String getHeadingTxt() {
        return headingTxt;
    }

    public void setHeadingTxt(String headingTxt) {
        
        this.headingTxt = headingTxt;
        this.viewModel.setHeadingTextPropertyValue(headingTxt);
    }
    
    public void setContent(PopupType popupType, String headingTxt) {
        
        this.setHeadingType(popupType);
        this.setHeadingTxt(headingTxt);
    }
    
    public Node getViewNode() {
        return parentNode;
    }
}
