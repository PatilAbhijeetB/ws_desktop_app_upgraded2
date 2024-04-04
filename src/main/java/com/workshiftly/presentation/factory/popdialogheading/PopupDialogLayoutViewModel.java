/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author chamara
 */
public class PopupDialogLayoutViewModel implements ViewModel {
    
    private SimpleObjectProperty<PopupDialogHeading> headingObjectProperty;
    
    private SimpleBooleanProperty mainContentDescriptionVisibility;
    private SimpleBooleanProperty mainContentSubDescriptionVisibility;
    private SimpleStringProperty mainContentDescriptionText;
    private SimpleStringProperty mainContentSubDescriptionText;
    
    private Label mainContentDescription;
    private Label mainContentSubDescription;
    
    public PopupDialogLayoutViewModel() {
        
        this.headingObjectProperty = new SimpleObjectProperty<>(null);
        
        mainContentDescriptionVisibility = new SimpleBooleanProperty(false);
        mainContentSubDescriptionVisibility = new SimpleBooleanProperty(false);
        
        mainContentDescriptionText = new SimpleStringProperty(null);
        mainContentSubDescriptionText = new SimpleStringProperty(null);
       
    }
    
    public void setDialogHeading(PopupDialogHeading headingComponent) {
        
        this.headingObjectProperty.set(headingComponent);
    }
    
    public SimpleObjectProperty<PopupDialogHeading> getHeadingComponentProperty() {
        return this.headingObjectProperty;
    }
    
    void setMainContentDescriptionEntity(Label labelEntity) {
        this.mainContentDescription = labelEntity;
    }
    
    void setMainContentSubDescriptionEntity(Label labelEntity) {
        this.mainContentSubDescription = labelEntity;
    }

    SimpleBooleanProperty getMainContentDescriptionVisibility() {
        return mainContentDescriptionVisibility;
    }

    SimpleBooleanProperty getMainContentSubDescriptionVisibility() {
        return mainContentSubDescriptionVisibility;
    }

    SimpleStringProperty getMainContentDescriptionText() {
        return mainContentDescriptionText;
    }

    SimpleStringProperty getMainContentSubDescriptionText() {
        return mainContentSubDescriptionText;
    }
    
    void setMainContentDescription(PopupDialogBox.MainContentDescriotion descriptionType, String input) 
    {
        
        SimpleStringProperty stringProperty = descriptionType == PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION ?
                this.mainContentDescriptionText : this.mainContentSubDescriptionText;
        SimpleBooleanProperty visibilityProperty = descriptionType == PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION ?
                this.mainContentDescriptionVisibility : this.mainContentSubDescriptionVisibility;
        
        boolean wrapperVisibility = !(input == null || input.isBlank());
        String text = wrapperVisibility ? input : "";
        visibilityProperty.set(wrapperVisibility);
        stringProperty.set(text);
    }
}
