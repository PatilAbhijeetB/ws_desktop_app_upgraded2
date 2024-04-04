/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.workshiftly.application.state.StateName;
import com.workshiftly.common.model.BreakReason;
import com.workshiftly.common.model.Response;
import com.workshiftly.domain.RawDataModule;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.lang.reflect.Type;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

/**
 *
 * @author Hashan@Hirosh
 */
public class BreakReasonModalModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(BreakReasonModalModel.class);
            
    private final SimpleObjectProperty<BreakReason> selectedBreakReasonProperty;
    private final ObservableList<BreakReason> breakReasonList;
    private final SimpleStringProperty breakReasonErrorLabelTextProperty;

    private final SimpleBooleanProperty otherReasonTxtAreaVibilityProp;
    private final SimpleStringProperty otherReasonTextInputProperty;
    private SimpleStringProperty otherReasonErrorLabelTextProperty;

    public BreakReasonModalModel() throws Exception {
        selectedBreakReasonProperty = new SimpleObjectProperty<>();
        otherReasonTxtAreaVibilityProp = new SimpleBooleanProperty(false);
        otherReasonTextInputProperty = new SimpleStringProperty();
        breakReasonList = FXCollections.observableArrayList();
        breakReasonErrorLabelTextProperty = new SimpleStringProperty();

        callGetBreakReasonService();
    }

    public void setOtherReasonErrorLabelTextProperty(SimpleStringProperty otherReasonErrorLabelTextProperty) {
        this.otherReasonErrorLabelTextProperty = otherReasonErrorLabelTextProperty;
    }

    public SimpleBooleanProperty getOtherReasonTxtAreaVibilityProp() {
        return otherReasonTxtAreaVibilityProp;
    }

    public SimpleObjectProperty<BreakReason> getSelectedBreakReasonProperty() {
        return selectedBreakReasonProperty;
    }

    public SimpleStringProperty getOtherReasonTextInputProperty() {
        return otherReasonTextInputProperty;
    }

    public ObservableList<BreakReason> getBreakReasonList() {
        return breakReasonList;
    }

    public SimpleStringProperty getBreakReasonErrorLabelTextProperty() {
        return breakReasonErrorLabelTextProperty;
    }

    public SimpleStringProperty getOtherReasonErrorLabelTextProperty() {
        return otherReasonErrorLabelTextProperty;
    }

    public void onChangeReason(BreakReason breakReason) {
        selectedBreakReasonProperty.set(breakReason);
        
        String currentBreakReasonTitle = breakReason.getTitle();
        boolean isOtherReasonTxtAreaVisible = currentBreakReasonTitle.equals(StateName.OTHER_BREAK_REASON_TITLE);
        otherReasonTxtAreaVibilityProp.set(isOtherReasonTxtAreaVisible);
        
        if (!isOtherReasonTxtAreaVisible) {
            otherReasonTextInputProperty.set(null);
        }
    }

    public void onChangeOtherReason(String otherReasonText) throws Exception {
        otherReasonTextInputProperty.set(otherReasonText);

        BreakReason selectedBreakReason = selectedBreakReasonProperty.get();
        String currentBreakReasonTitle = selectedBreakReason.getTitle();
        boolean isOtherReasonItemSelected = currentBreakReasonTitle.equals(StateName.OTHER_BREAK_REASON_TITLE);

        if (!isOtherReasonItemSelected) {
            return;
        }

        selectedBreakReason.setOtherReason(otherReasonText);
    }
    
    public final void callGetBreakReasonService() {
        
        Service<Response> service = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        RawDataModule rawDataModule = new RawDataModule();
                        Response response = rawDataModule.getBreakingReasons(true);
                        return response;
                    };
                };
            }
        };
        
        service.setOnSucceeded((WorkerStateEvent event) -> {
            Response response = service.getValue();
            if (response.isError()) {
                // error handling code should be included here
                return;
            }
            
            Gson gson = new Gson();
            Type listOfBreakReasonType = new TypeToken<List<BreakReason>>(){}.getType();
            List<BreakReason> breakReasons = gson.fromJson(response.getData(), listOfBreakReasonType);
            breakReasonList.addAll(breakReasons);
        });
        
        service.start();
    }
}
