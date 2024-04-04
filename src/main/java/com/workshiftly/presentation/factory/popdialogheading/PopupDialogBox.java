/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

/**
 *
 * @author chamara
 */
public class PopupDialogBox {
    
    private PopupDialogHeading headingComponent;
    private JFXDialogLayout jFXDialogLayout;
    private JFXDialog jFXDialog;
    private Node parentNode;
    private PopupDialogButtonLayoutViewModel buttonLayoutViewModel;
    private PopupDialogLayoutViewModel dialogLayoutViewModel;
    
    public enum ButtonWrapperContainer {
        LEFT,
        RIGHT;
    }
    
    public enum DialogButton {
        LEFT_MOST,
        RIGHT_CENTER,
        RIGHT_MOST;
    }
    
    public enum MainContentDescriotion {
        MAIN_DESCRIPTION,
        SUB_DESCRIPTION;
    }

    public void setjFXDialog(JFXDialog jFXDialog) {
      //  jFXDialog.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        this.jFXDialog = jFXDialog;
    }

    public JFXDialog getjFXDialog() {
        return jFXDialog;
    }
    
    
    public PopupDialogBox() {
        
        try {
            this.jFXDialogLayout = new JFXDialogLayout();
            this.jFXDialog = new JFXDialog(null, this.jFXDialogLayout, JFXDialog.DialogTransition.TOP);
            //this.jFXDialog.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            this.loadDialoyContentLayout();
            this.loadButtonLayout();
            
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    this.getClass(), 
                    "Error occurred while initializing PopupDialogBox instance", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }    
    }
    
    public PopupDialogBox(PopupDialogHeading headingComponent) {
        
        this();
        setHeadingComponent(headingComponent);
    }
    
    private void loadButtonLayout() {
        
        try {
            FluentViewLoader.FxmlViewStep<PopupDialogButtonLayout, PopupDialogButtonLayoutViewModel> fxmlView
                    = FluentViewLoader.fxmlView(PopupDialogButtonLayout.class);
            ViewTuple<PopupDialogButtonLayout, PopupDialogButtonLayoutViewModel> viewTuple = fxmlView.load();
            
            this.buttonLayoutViewModel =  viewTuple.getViewModel();
            
            Parent view = viewTuple.getView();
            this.jFXDialogLayout.setActions(view);
            
            this.buttonLayoutViewModel.getRightMostButton().setText("Are you working?");
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    this.getClass(),
                    "Error occurred while initializing Popup Dialog Box button layout", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
    
    private void loadDialoyContentLayout() {
        try {
            FluentViewLoader.FxmlViewStep<PopupDialogLayoutView, PopupDialogLayoutViewModel> fxmlView =
                    FluentViewLoader.fxmlView(PopupDialogLayoutView.class);
            ViewTuple<PopupDialogLayoutView, PopupDialogLayoutViewModel> viewTuple = fxmlView.load();
            
            this.parentNode = viewTuple.getView();
            this.dialogLayoutViewModel = viewTuple.getViewModel();
            
            this.jFXDialogLayout.setBody(this.parentNode);
            
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    this.getClass(), 
                    "Error occurred while loading Popup Dialog Box content layout", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
    
    public final void setHeadingComponent(PopupDialogHeading popupDialogHeading) throws NullPointerException {
        
        if (popupDialogHeading == null) {
            throw new NullPointerException("Popup Dialog Heading should not be null");
        }
        
        this.headingComponent = popupDialogHeading;
        ObservableList<Node> headingComponents = this.jFXDialogLayout.getHeading();
        headingComponents.setAll(popupDialogHeading.getViewNode());
    }
    
    public JFXButton getDialogButton(DialogButton dialogButton) {
        
        JFXButton button = null;
        switch (dialogButton) {
            case LEFT_MOST:
                button = buttonLayoutViewModel.getLeftMostButton();
                break;
            case RIGHT_CENTER:
                button = buttonLayoutViewModel.getRightCenterButton();
                break;
            case RIGHT_MOST:
                button = buttonLayoutViewModel.getRightMostButton();
                break;
        }
        return button;
    }
    
    public void close() {
        this.jFXDialog.close();
    }
    
    public void load() {
      //  jFXDialog.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        MainScreenController.showJFXDialog(jFXDialog);
    }
    
    public void setDescription(MainContentDescriotion contentType, String input) {
        
      //  dialogLayoutViewModel.setMainContentDescription(contentType, input);
        
    }
}
