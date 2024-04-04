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
import com.workshiftly.common.constant.ProductivityStatus;
import com.workshiftly.common.model.ProductivityWidgetData;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.domain.DashboardDataModule;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Chanakya
 */
public class ProductivityWidgetModel implements ViewModel {

    private static final InternalLogger LOGGER = LoggerService.getLogger(ProductivityWidgetModel.class);
    Gson GSON = new Gson();

    DashboardDataModule dashboardDataModule;
    DateTimeZone companyDateTimeZone;
    ZoneId companyDateTimeZoneId;
    WorkDateTime workDateTime;

    private final SimpleObjectProperty<LocalDate> weekPickerDate = new SimpleObjectProperty();
    private final SimpleStringProperty productivityPercentageLabelProperty = new SimpleStringProperty();
    private final SimpleStringProperty unProductivityPercentageLabelProperty = new SimpleStringProperty();
    private final SimpleStringProperty neutralPercentageLabelProperty = new SimpleStringProperty();
    private final SimpleStringProperty unratedPercentageLabelProperty = new SimpleStringProperty();

    private final ObservableList<BarChart.Series> productivityDataSet = FXCollections.observableArrayList();
    private final ObservableList<String> productivityLabelMap = FXCollections.observableArrayList();

    public ProductivityWidgetModel() {
        try {

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

        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.WARNING, "Error occurred when initializing Productivity Widget Widget Model constructor", ex);
        }
    }

    public SimpleObjectProperty<LocalDate> getWeekPickerDate() {
        return weekPickerDate;
    }

    public SimpleStringProperty getProductivityPercentageLabelProperty() {
        return productivityPercentageLabelProperty;
    }

    public SimpleStringProperty getUnProductivityPercentageLabelProperty() {
        return unProductivityPercentageLabelProperty;
    }

    public SimpleStringProperty getNeutralPercentageLabelProperty() {
        return neutralPercentageLabelProperty;
    }

    public SimpleStringProperty getUnratedPercentageLabelProperty() {
        return unratedPercentageLabelProperty;
    }

    public ObservableList<BarChart.Series> getProductivityDataSet() {
        return productivityDataSet;
    }

    public ObservableList<String> getProductivityLabelMap() {
        return productivityLabelMap;
    }

    public final void onChangeWeek() {
        Service<Response> getUserProductivityDataService;
        getUserProductivityDataService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        TemporalField fieldUS = WeekFields.of(Locale.US).dayOfWeek();
                        LocalDate weekStartingLocalDate = weekPickerDate.get().with(fieldUS, 1);
                        long weekStartingTimestamp = weekStartingLocalDate
                                .atStartOfDay(companyDateTimeZoneId)
                                .toEpochSecond();

                        Response response = dashboardDataModule.getUserProductivityData(weekStartingTimestamp);
                        
                        return response;
                    }
                };
            }
        };

        getUserProductivityDataService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
//            loadingProperty.set(true);
//            loadingProperty.set(true);
//            noDataProperty.set(false);
//            hideChartProperty.set(true);
        });

        getUserProductivityDataService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            Response result = getUserProductivityDataService.getValue();

            JsonElement resultObj = result.getData();
            Type productivityWidgetDataType = new TypeToken<ProductivityWidgetData>() {
            }.getType();
            ProductivityWidgetData productivityWidgetData = GSON.fromJson(resultObj, productivityWidgetDataType);

            productivityDataSet.clear();
            productivityLabelMap.clear();

            if (productivityWidgetData != null && productivityWidgetData.getSummary() != null) {
                XYChart.Series productiveSeries = new XYChart.Series();
                XYChart.Series unProductiveSeries = new XYChart.Series();
                XYChart.Series neutralSeries = new XYChart.Series();
                XYChart.Series UnratedSeries = new XYChart.Series();
                
                productiveSeries.setName(ProductivityStatus.PRODUCTIVE.name());
                unProductiveSeries.setName(ProductivityStatus.UNPRODUCTIVE.name());
                neutralSeries.setName(ProductivityStatus.NEUTRAL.name());
                UnratedSeries.setName(ProductivityStatus.UNRATED.name());
                
                double[] totalPercentage = new double[1];
                double[] totalproductivePercentage = new double[1];
                double[] totalunProductivePercentage = new double[1];
                double[] totalneutralPercentage = new double[1];
                double[] totalunratedPercentage = new double[1];
                
                totalPercentage[0] = 0.0;
                totalproductivePercentage[0] = 0.0;
                totalunProductivePercentage[0] = 0.0;
                totalneutralPercentage[0] = 0.0;
                totalunratedPercentage[0] = 0.0;
                
                HashMap<String, HashMap<String, Double>> processedActivitySummaryDataMap = productivityWidgetData.getSummary();
                TreeMap<String, HashMap<String, Double>> sortedProcessedActivitySummaryDataMap = new TreeMap<>();
                sortedProcessedActivitySummaryDataMap.putAll(processedActivitySummaryDataMap);
                sortedProcessedActivitySummaryDataMap.forEach((label, value) -> {

                    double productivePercentage = value.get(ProductivityStatus.PRODUCTIVE.name());
                    double unProductivePercentage = value.get(ProductivityStatus.UNPRODUCTIVE.name());
                    double neutralPercentage = value.get(ProductivityStatus.NEUTRAL.name());
                    double unratedPercentage = value.get(ProductivityStatus.UNRATED.name());
                    
                    XYChart.Data productiveSeriesData = new XYChart.Data<String, Number>(label, productivePercentage);
                    XYChart.Data unProductiveSeriesData = new XYChart.Data<String, Number>(label, unProductivePercentage);
                    XYChart.Data neutralSeriesData = new XYChart.Data<String, Number>(label, neutralPercentage);
                    XYChart.Data UnratedSeriesData = new XYChart.Data<String, Number>(label, unratedPercentage);
                    
                    productiveSeriesData.nodeProperty().addListener(new ChangeListener<Node>() {
                        @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                          if (node != null) {
                                productiveSeriesData.getNode().setStyle("-fx-bar-fill: #0CC075;");
                          } 
                        }
                      });
                    unProductiveSeriesData.nodeProperty().addListener(new ChangeListener<Node>() {
                        @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                          if (node != null) {
                            unProductiveSeriesData.getNode().setStyle("-fx-bar-fill: #FC657C;");
                          } 
                        }
                      });
                    neutralSeriesData.nodeProperty().addListener(new ChangeListener<Node>() {
                        @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                          if (node != null) {
                            neutralSeriesData.getNode().setStyle("-fx-bar-fill: #319CF9;");
                          } 
                        }
                      });
                    UnratedSeriesData.nodeProperty().addListener(new ChangeListener<Node>() {
                        @Override public void changed(ObservableValue<? extends Node> ov, Node oldNode, final Node node) {
                          if (node != null) {
                            UnratedSeriesData.getNode().setStyle("-fx-bar-fill: gray;");
                          } 
                        }
                      });
                    
                    productiveSeries.getData().add(productiveSeriesData);
                    unProductiveSeries.getData().add(unProductiveSeriesData);
                    neutralSeries.getData().add(neutralSeriesData);
                    UnratedSeries.getData().add(UnratedSeriesData);

                    totalPercentage[0] += (productivePercentage + unProductivePercentage + neutralPercentage + unratedPercentage);
                    totalproductivePercentage[0] += productivePercentage;
                    totalunProductivePercentage[0] += unProductivePercentage;
                    totalneutralPercentage[0] += neutralPercentage;
                    totalunratedPercentage[0] += unratedPercentage;
                    
                    productivityLabelMap.add(label);
                });
                productivityDataSet.addAll(productiveSeries, unProductiveSeries, neutralSeries, UnratedSeries);
                productivityPercentageLabelProperty.set(String.valueOf(Math.round(totalproductivePercentage[0] / totalPercentage[0] * 100)) + "%");
                unProductivityPercentageLabelProperty.set(String.valueOf(Math.round(totalunProductivePercentage[0] / totalPercentage[0] * 100)) + "%");
                neutralPercentageLabelProperty.set(String.valueOf(Math.round(totalneutralPercentage[0] / totalPercentage[0] * 100)) + "%");
                unratedPercentageLabelProperty.set(String.valueOf(Math.round(totalunratedPercentage[0] / totalPercentage[0] * 100)) + "%");
            } else {
//                noDataProperty.set(true);
//                hideChartProperty.set(false);
            }

//            loadingProperty.set(false);
        });

        getUserProductivityDataService.start();
    }

}
