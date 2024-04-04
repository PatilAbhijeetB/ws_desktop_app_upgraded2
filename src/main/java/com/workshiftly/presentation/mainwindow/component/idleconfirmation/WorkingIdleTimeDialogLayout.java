/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component.idleconfirmation;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


/**
 *
 * @author chamara
 */
public class WorkingIdleTimeDialogLayout {
    
    private static final int CONTENT_PANE_WIDTH = 500; 
    private static final int CONTENT_PANE_HEIGHT = 250;
    
    
    private JFXDialogLayout dialogLayout;
    private String heading;
    private PopupDialogHeading dialogHeading;
    private StackPane contentWrapper;
    
    
    private enum DialogLayoutContent {
        WORK_IDLE_CONFIRMATION,
        FORCE_BREAK_STATE
    }
    
    public WorkingIdleTimeDialogLayout() {
        
        this.dialogHeading = new PopupDialogHeading(PopupDialogHeading.PopupType.WARNING);
        
        this.contentWrapper = new StackPane();
        this.contentWrapper.setPrefSize(CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT);
        
        this.dialogLayout = new JFXDialogLayout();
        this.dialogLayout.setHeading(this.dialogHeading.getViewNode());
        this.dialogLayout.setBody(contentWrapper);
        
        setContent(DialogLayoutContent.WORK_IDLE_CONFIRMATION);
        
    }
    
    private void setContent(DialogLayoutContent layoutContent) {
        
        switch (layoutContent) {
            
            case WORK_IDLE_CONFIRMATION:
                System.out.println("#### layout content: WORK_IDLE_CONFIRMATION");
                this.dialogHeading.setContent(PopupDialogHeading.PopupType.WARNING, "WORK_IDLE_CONFIRMATION");
                break;
                
            case FORCE_BREAK_STATE:
                System.out.println("#### layout content: FORCE_BREAK_STATE");
                this.dialogHeading.setContent(PopupDialogHeading.PopupType.WARNING, "FORCE_BREAK_STATE");
                break;
                
            default:
                System.out.println("#### layout content: default case");
                break;
        }
    }
    
    public void load() {
        
        JFXDialog dialongInstance = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.TOP);
        MainScreenController.showJFXDialog(dialongInstance);
    }
}
