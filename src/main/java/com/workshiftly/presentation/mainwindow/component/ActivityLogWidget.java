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
import de.saxsys.mvvmfx.utils.viewlist.CachedViewModelCellFactory;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

/**
 *
 * @author chamara
 */
public class ActivityLogWidget implements Initializable, FxmlView<ActivityLogWidgetViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ActivityLogWidget.class);

    private final String FXML_PATH = "/com/workshiftly/presentation/mainwindow/components/ActivityLogWidget.fxml";
    private static ActivityLogWidget activityLogWidget;
       
    @FXML
    private Pane rootWrapper;
    
    @FXML
    private ListView<ActivityLogListItemViewModel> activityListView;
    
    @InjectViewModel
    private ActivityLogWidgetViewModel viewModel;
    
    
    public ActivityLogWidget() {
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        activityListView.setItems(viewModel.getActivityLogListItems());
        
        CachedViewModelCellFactory activityListCellFactory  
                = CachedViewModelCellFactory.createForFxmlView(ActivityLogListItem.class);
        activityListView.setCellFactory(activityListCellFactory);
    }
    
    
    
}
