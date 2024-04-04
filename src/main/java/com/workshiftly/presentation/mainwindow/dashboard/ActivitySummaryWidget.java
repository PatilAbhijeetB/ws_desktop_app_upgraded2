/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 * @author Hashan
 */
public class ActivitySummaryWidget implements Initializable, FxmlView<ActivitySummaryWidgetModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ActivitySummaryWidget.class);

    private static final String LOADING_SPINNER_IMAGE_PATH = "/images/loading_spinner.gif";

    private LocalDate selectedWeekStartingLocalDate;
    private LocalDate selectedWeekEndingLocalDate;

    @FXML
    private DatePicker weekPicker;

    @FXML
    private PieChart summaryChart;

    @FXML
    private Pane noDataPane;

    @FXML
    private Pane loadingSpinner;

    @FXML
    private ImageView loadingSpinnerImg;
    
    @FXML
    private VBox legendContainer;

    @InjectViewModel
    private ActivitySummaryWidgetModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            viewModel.setBoundPieChart(summaryChart);
            
            Bindings.bindContent(legendContainer.getChildren(), viewModel.getLegendItems());
        } catch (Exception ex) {
            String errorMsg = "Unable to load activity data widget legend items";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }
        
        try {
            InputStream inputStream = getClass().getResourceAsStream(LOADING_SPINNER_IMAGE_PATH);
            Image loadingSpinnerImage = new Image(inputStream);
            loadingSpinnerImg.setImage(loadingSpinnerImage);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Activity Summary Widget Initialization Exception", ex);
        }

        summaryChart.setData(viewModel.getActivitySummaryDataSet());
        weekPicker.valueProperty().bindBidirectional(viewModel.getWeekPickerDate());
        loadingSpinner.visibleProperty().bind(viewModel.getLoadingProperty());
        noDataPane.visibleProperty().bind(viewModel.getNoDataProperty());
        summaryChart.visibleProperty().bind(viewModel.getHideChartProperty());

        weekPicker.setOnAction((ActionEvent actionEvent) -> {
            LocalDate selectedLocalDate = weekPicker.getValue();
            TemporalField fieldUS = WeekFields.of(Locale.US).dayOfWeek();
            LocalDate weekStartingLocalDate = selectedLocalDate.with(fieldUS, 1);
            selectedWeekStartingLocalDate = weekStartingLocalDate.minus(1, ChronoUnit.DAYS);
            selectedWeekEndingLocalDate = weekStartingLocalDate.plus(7, ChronoUnit.DAYS);

            viewModel.onChangeWeek();
        });

        weekPicker.setDayCellFactory((DatePicker param) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                boolean isWeekDateRangeExist = selectedWeekStartingLocalDate != null && selectedWeekEndingLocalDate != null;
                boolean isItemInWeekRange = isWeekDateRangeExist && selectedWeekStartingLocalDate.isBefore(item) && selectedWeekEndingLocalDate.isAfter(item);

                if (isItemInWeekRange) {
                    setStyle("-fx-background-color: rgba(3, 169, 244, 0.7);");
                }
            }
        });

        weekPicker.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLL dd yyyy");

            @Override
            public String toString(LocalDate date) {
                return date == null
                        ? "Please select a week."
                        : "Week of " + date.format(formatter);
            }

            @Override
            public LocalDate fromString(String string) {
                return (string == null || string.isEmpty())
                        ? null
                        : LocalDate.parse(
                                string.replace("Week of ", ""),
                                formatter);
            }
        });
    }
}
