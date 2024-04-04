/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/**
 *
 * @author chamara
 */
public class ActivityLogWidgetViewModel implements ViewModel {
    
    private final ObservableList<ActivityLogListItemViewModel> activityLogListItems;
    
    public ActivityLogWidgetViewModel() {
        
        activityLogListItems = FXCollections.observableArrayList();
        
        registerActivityLogDataProviderChangeListener();
    }
    
    private void registerActivityLogDataProviderChangeListener() {
        
        ObservableList<WorkStatusLog> dataProvider = AuthenticatedMainWindowViewModel.getWORK_STATUS_LOGS();
        dataProvider.addListener((Change<? extends WorkStatusLog> listChange) -> {
            while (listChange.next()) {
                List<WorkStatusLog>  newlyAddedChanges = (List<WorkStatusLog>) listChange.getAddedSubList();
                
                newlyAddedChanges.iterator().forEachRemaining((WorkStatusLog workStatusLog) -> {
                    ActivityLogListItemViewModel viewModelListItem = new ActivityLogListItemViewModel(workStatusLog);
                    activityLogListItems.add(viewModelListItem);
                });  
            }
        });
    }

    public ObservableList<ActivityLogListItemViewModel> getActivityLogListItems() {
        return activityLogListItems;
    }
}
