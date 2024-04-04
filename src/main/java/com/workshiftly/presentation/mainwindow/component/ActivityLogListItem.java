/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 *
 * @author chamara
 */
public class ActivityLogListItem implements Initializable, FxmlView<ActivityLogWidgetViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ActivityLogListItem.class);

    @FXML
    private Pane rootWrapper;

    @FXML
    private Label timeLbl;

    @FXML
    private Label activityStatusLbl;
    
    @InjectViewModel
    private ActivityLogListItemViewModel viewModel;
    

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        timeLbl.textProperty().bind(viewModel.getActivityTimeProperty());
        activityStatusLbl.textProperty().bind(viewModel.getActivityTypeProperty());
        
    }
    
}
