/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.ActivitySummaryWidgetData;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.domain.DashboardDataModule;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Hashan
 */
public class ActivitySummaryWidgetModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ActivitySummaryWidgetModel.class);
    Gson GSON = new Gson();
    private String[] PIE_CHART_DATA_COLORS = new String[] {
        "#FC657C",
        "#319CF9",
        "#F79D12",
        "#664EFF",
        "#BE52F2",
        "#FDD664",
        "#0CC075"
    };

    DashboardDataModule dashboardDataModule;
    DateTimeZone companyDateTimeZone;
    ZoneId companyDateTimeZoneId;
    WorkDateTime workDateTime;

    private final SimpleBooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty noDataProperty = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty hideChartProperty = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<LocalDate> weekPickerDate = new SimpleObjectProperty();

    private final ObservableList<PieChart.Data> activitySummaryDataSet = FXCollections.observableArrayList();
    private final ObservableList<Node> legendItems = FXCollections.observableArrayList();
    
    private PieChart boundPieChart;

    public ActivitySummaryWidgetModel() {
        try {
            loadingProperty.set(true);

            dashboardDataModule = new DashboardDataModule();
            companyDateTimeZone = StateStorage.getCurrentState(StateName.COMPANY_TIMEZONE);
            companyDateTimeZoneId = ZoneId.of(companyDateTimeZone.getID());

            while (true) {
                workDateTime = StateStorage.getCurrentState(StateName.CURRENT_WORKDATETIME_INSTANCE);
                if (workDateTime != null) {
                    break;
                }
            }
            long epochDay = workDateTime.getDateStartedTimestamp();
            Instant instant = Instant.ofEpochSecond(epochDay);
            LocalDate localDate = LocalDate.ofInstant(instant, this.companyDateTimeZoneId);
            weekPickerDate.set(localDate);

            onChangeWeek();

            loadingProperty.set(false);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.WARNING, "Error occurred when initializing Activity Summary Widget Model constructor", ex);
        }
    }

    public SimpleBooleanProperty getLoadingProperty() {
        return loadingProperty;
    }

    public SimpleBooleanProperty getNoDataProperty() {
        return noDataProperty;
    }

    public SimpleBooleanProperty getHideChartProperty() {
        return hideChartProperty;
    }

    public SimpleObjectProperty<LocalDate> getWeekPickerDate() {
        return weekPickerDate;
    }

    public ObservableList<PieChart.Data> getActivitySummaryDataSet() {
        return activitySummaryDataSet;
    }

    public void setBoundPieChart(PieChart boundPieChart) {
        this.boundPieChart = boundPieChart;
    }

    public ObservableList<Node> getLegendItems() {
        return legendItems;
    }
    
    
    public final void onChangeWeek() {
        Service<Response> getUserActivitySummaryDataService;
        getUserActivitySummaryDataService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        LocalDate weekStartDate = weekPickerDate.get();
                        Response response = dashboardDataModule
                                .getUserActivitySummaryData(weekStartDate);
                        return response;
                    }
                };
            }
        };

        getUserActivitySummaryDataService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
            loadingProperty.set(true);
            noDataProperty.set(false);
            hideChartProperty.set(true);
        });

        getUserActivitySummaryDataService.setOnSucceeded((var workerStateEvent) -> {
            Response result = getUserActivitySummaryDataService.getValue();

            JsonElement resultObj = result.getData();
            Type activitySummaryWidgetDataType = new TypeToken<ActivitySummaryWidgetData>() {}.getType();
            ActivitySummaryWidgetData activitySummaryWidgetData = GSON.fromJson(resultObj, activitySummaryWidgetDataType);

            activitySummaryDataSet.clear();
            loadingProperty.set(false);
            
            HashMap<String, Double> widgetData = activitySummaryWidgetData != null 
                    ? activitySummaryWidgetData.getSummary() : null;
            
            if (widgetData != null && !widgetData.entrySet().isEmpty()) {
                ArrayList<Map.Entry<String, Double>> dataEntries = new ArrayList<>();
                widgetData.entrySet().forEach((Map.Entry<String, Double> curObj) -> {
                    dataEntries.add(curObj);
                });
                Collections.sort(dataEntries, (
                        Map.Entry<String, Double> obj1, Map.Entry<String, Double> obj2) -> 
                            Double.compare(obj1.getValue(), obj2.getValue())
                );
                
                Collections.reverse(dataEntries);
                String colorStyleFormat = "-fx-pie-color: %s; -fx-border-color: %s;";
                colorStyleFormat +=  " -fx-border-width: 1px";
                
                if (boundPieChart != null) {
                    boundPieChart.legendVisibleProperty().set(false);
                }
                
                legendItems.clear();
                
                for (int idx = 0; idx < dataEntries.size(); idx++) {
                    Map.Entry<String, Double> curEntry = dataEntries.get(idx);
                    String label = curEntry.getKey();
                    Double value = curEntry.getValue();
                    
                    PieChart.Data data = new PieChart.Data(label, value);
                    activitySummaryDataSet.add(data);
                    
                    PieChart.Data curChartData = activitySummaryDataSet.get(idx);
                    Node curNode = curChartData.getNode();

                    if (idx < PIE_CHART_DATA_COLORS.length && curNode != null) {
                        String color = PIE_CHART_DATA_COLORS[idx];
                        String replaceStyle = String.format(colorStyleFormat, color, color);
                        curNode.setStyle(replaceStyle);
                        
                        try {
                            // initialize legend list item
                            FluentViewLoader.FxmlViewStep<ActivityDataLegendItem, ActivityDataLegendItemViewModel> fxmlView;
                            fxmlView = FluentViewLoader.fxmlView(ActivityDataLegendItem.class);
                            ViewTuple<ActivityDataLegendItem, ActivityDataLegendItemViewModel> viewTuple = fxmlView.load();
                            
                            Parent legendItem = viewTuple.getView();
                            ActivityDataLegendItemViewModel legendItemViewModel = viewTuple.getViewModel();
                            
                            legendItemViewModel.setLegendCircleFill(Color.web(color));
                            legendItemViewModel.setLegendTitle(label);
                            
                            String percentage = Math.round(value) + "%";
                            legendItemViewModel.setLegendPercentage(percentage);
                            
                            legendItems.add(legendItem);
                        } catch (Exception ex) {
                            String errorMsg = "Unable to create legend list item";
                            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
                        }
                    }
                }
            } else {
                noDataProperty.set(true);
                hideChartProperty.set(false);
                  legendItems.clear();
            }
        });

        getUserActivitySummaryDataService.start();
    }
}
