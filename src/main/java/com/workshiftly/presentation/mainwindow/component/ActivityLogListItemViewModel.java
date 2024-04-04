/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.time.Duration;
import javafx.beans.property.SimpleStringProperty;
import org.joda.time.DateTimeZone;

/**
 *
 * @author chamara
 */
public class ActivityLogListItemViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ActivityLogListItemViewModel.class);

    private static final String TIME_FORMATE_PATTERN = "HH:mm:ss";
    
    private final SimpleStringProperty activityTimeProperty;
    private final SimpleStringProperty activityTypeProperty;
 
    public ActivityLogListItemViewModel(WorkStatusLog workStatusLog) {
        
        long activityTimestamp = workStatusLog.getActionTimestamp();
        Duration activityDuration = Duration.ofSeconds(activityTimestamp);
        DateTimeZone userTimeZone = DateTimeZone.getDefault();
        String readableTime = TimeUtility.getHumanReadbleDateTime(activityDuration, userTimeZone, TIME_FORMATE_PATTERN);
        
        activityTimeProperty = new SimpleStringProperty(readableTime);
        activityTypeProperty = new SimpleStringProperty(workStatusLog.getWorkStatus().toString());
    }

    public SimpleStringProperty getActivityTimeProperty() {
        return activityTimeProperty;
    }

    public SimpleStringProperty getActivityTypeProperty() {
        return activityTypeProperty;
    }
    
    
}
