/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory.popdialogheading;

import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading.PopupType;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.io.InputStream;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

/**
 *
 * @author chamara
 */
public class PopupDialogHeadingViewModel implements ViewModel {
    
    private static final String COMPUTED_IMAGE_PATH = "/images/icons/popup_dialogbox/information.png";
    private static final PopupDialogHeading.PopupType DEFAULT_POPUP_TYPE = PopupType.INFORMATION;
    
    private SimpleObjectProperty<PopupDialogHeading.PopupType> popupTypeProperty;
    private SimpleStringProperty popupHeadingTxtProperty;
    private SimpleObjectProperty<Image> popHeadingImageProperty;
    
    public PopupDialogHeadingViewModel() {
        
        this.popupTypeProperty = new SimpleObjectProperty<>(DEFAULT_POPUP_TYPE);
        this.popupHeadingTxtProperty = new SimpleStringProperty();
        this.popHeadingImageProperty = new SimpleObjectProperty<>();
        
        setHeaderImageProperty(DEFAULT_POPUP_TYPE);
    }
    
    public void setHeadingTextPropertyValue(String value) {
        this.popupHeadingTxtProperty.set(value);
    }
    
    public SimpleStringProperty getHeadingTextProperty() {
        return this.popupHeadingTxtProperty;
    }
    
    public void setPopupTypeProperty(PopupType popupType) {
        this.popupTypeProperty.set(popupType);
        this.setHeaderImageProperty(popupType);
    }

    public SimpleObjectProperty<PopupType> getPopupTypeProperty() {
        return this.popupTypeProperty;
    }
    
    private void setHeaderImageProperty(PopupDialogHeading.PopupType popupType) {
        
        try {
            String popupTypeName = popupType.name().toLowerCase();
            String formattedImagePath = String.format(COMPUTED_IMAGE_PATH, popupTypeName);
            InputStream imageStream = getClass().getResourceAsStream(formattedImagePath);
            
            Image popupImage = new Image(imageStream);
            this.popHeadingImageProperty.set(popupImage);
            
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    this.getClass(), 
                    "failed to attach popup image", 
                    InternalLogger.LOGGER_LEVEL.ALL,
                    ex
            );
        }
    }
    
    SimpleObjectProperty<Image> getHeaderImageProperty() {
        return this.popHeadingImageProperty;
    }
}
