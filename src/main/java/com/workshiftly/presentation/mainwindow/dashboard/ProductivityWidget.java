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
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 * @author Chanakya
 */
public class ProductivityWidget implements Initializable, FxmlView<ProductivityWidgetModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ProductivityWidget.class);

    private LocalDate selectedWeekStartingLocalDate;
    private LocalDate selectedWeekEndingLocalDate;
    
    @FXML
    private DatePicker weekPicker;

    @FXML
    private Label productivityPercentageLabel;

    @FXML
    private Label unProductivityPercentageLabel;

    @FXML
    private Label neutralPercentageLabel;

    @FXML
    private Label unratedPercentageLabel;

    @FXML
    private BarChart productivityBarChart;

    @FXML
    private CategoryAxis dateAxis;

    @FXML
    private NumberAxis percentageAxis;

    @InjectViewModel
    private ProductivityWidgetModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        percentageAxis.setAutoRanging(false);
        percentageAxis.setLowerBound(0);
        percentageAxis.setUpperBound(100);
        percentageAxis.setTickUnit(10);
        
        productivityBarChart.setAnimated(false);
        productivityBarChart.setLegendVisible(false);
        productivityBarChart.setData(viewModel.getProductivityDataSet());
        
        dateAxis.setCategories(viewModel.getProductivityLabelMap());
        weekPicker.valueProperty().bindBidirectional(viewModel.getWeekPickerDate());
        
        // labels textPropertyBindings
        productivityPercentageLabel.textProperty().bind(viewModel.getProductivityPercentageLabelProperty());
        unProductivityPercentageLabel.textProperty().bind(viewModel.getUnProductivityPercentageLabelProperty());
        neutralPercentageLabel.textProperty().bind(viewModel.getNeutralPercentageLabelProperty());
        unratedPercentageLabel.textProperty().bind(viewModel.getUnratedPercentageLabelProperty());
        
        // date picker onChange Action
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
