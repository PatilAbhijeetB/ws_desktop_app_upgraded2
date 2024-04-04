/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.IOException;
import java.io.InputStream;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author chamara
 */
public final class HeaderNavigationItem extends Pane {
    private static final InternalLogger LOGGER = LoggerService.getLogger(HeaderNavigationItem.class);

    private static final String FXML_PATH = "/com/workshiftly/presentation/mainwindow/components/HeaderNavigationItem.fxml";
    private static final Paint ICON_COLOR_PAINT = Color.WHITE ;
    private static final String ACTIVATED_CSS_CLASS = "nav-bar-item-activated";
            
    @FXML
    private Pane rootWrapper;
    
    @FXML
    private Pane contentWrapper;

    @FXML
    private Pane iconWrapper;
    
    @FXML
    private ImageView iconImageView;

    @FXML
    private Pane txtWrapper;
    
    @FXML
    private Label navigationName;
    
    
    @FXML
    private ImageView alertIconImgView;
    
    private String navigationIdentifier;
    SimpleBooleanProperty didActivatedProperty = new SimpleBooleanProperty(false);
    final SimpleBooleanProperty shouldViewAlertIcon = new SimpleBooleanProperty(false);
    private boolean reuseIfExisting;
    
    void setAlertVisibility(boolean value) {
        shouldViewAlertIcon.set(value);
    }
    
    private HeaderNavigationItem() {};
    
    /**
     * 
     * @param navigationIdentifier
     * @throws IOException 
     */
    private HeaderNavigationItem(String navigationIdentifier) throws IOException {
        
        this.navigationIdentifier = navigationIdentifier;
        
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        InputStream stream = getClass().getResourceAsStream(FXML_PATH);
        fxmlLoader.load(stream);
        setId(navigationIdentifier);
        
        didActivatedProperty.addListener((
                ObservableValue<? extends Boolean> observable, 
                Boolean oldValue, 
                Boolean newValue
        ) -> {
            if (newValue) {
                contentWrapper.getStyleClass().add(ACTIVATED_CSS_CLASS);
            } else {
                ObservableList<String> cssStyles = contentWrapper.getStyleClass();
                
                if (cssStyles.contains(ACTIVATED_CSS_CLASS)) {
                    cssStyles.remove(ACTIVATED_CSS_CLASS);
                }
            }
        });
        
        addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            boolean didActivated = didActivatedProperty.get();
            if (mouseEvent.isPrimaryButtonDown() && !didActivated) {
                HeaderNavigation.activateNavigationItem(navigationIdentifier);
            }
        });
        
        try {
            String alertIconPath = "/images/mainwindow/main_window_tab_alert.png";
            InputStream inStream = getClass().getResourceAsStream(alertIconPath);
            alertIconImgView.setImage(new Image(inStream));
            alertIconImgView.visibleProperty().bind(shouldViewAlertIcon);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "unable to load", ex);
        }
    }
    
    /**
     * 
     * @param navigationIdentifier
     * @return
     * @throws IOException 
     */
    static HeaderNavigationItem init(String navigationIdentifier) 
            throws IOException {
        
        HeaderNavigationItem navigationItem 
                = new HeaderNavigationItem(navigationIdentifier);
        return navigationItem;
    }
    
    public void setNavigationIcon(final String imagePath) {
        
       InputStream imageStream = getClass().getResourceAsStream(imagePath);
       Image iconImage = new Image(imageStream);
       iconImageView.setImage(iconImage);
    }
    
    public void setNavigationNameTxt(String nameTxt) {
        navigationName.setText(nameTxt);
        Font font = Font.font("Rubik", FontWeight.findByWeight(500), 12.0); // 300 is equivalent to FontWeight.LIGHT
          navigationName.setFont(font);
    }
    
    public void setActivatedCSSClass() {
        contentWrapper.getStyleClass().add(ACTIVATED_CSS_CLASS);
    }
    
    public void setActiveState(boolean value) {
        didActivatedProperty.set(value);
    }

    public String getNavigationIdentifier() {
        return navigationIdentifier;
    }

    public boolean isReuseIfExisting() {
        return reuseIfExisting;
    }

    public void setReuseIfExisting(boolean reuseIfExisting) {
        this.reuseIfExisting = reuseIfExisting;
    }
}
