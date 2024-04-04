/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import com.workshiftly.domain.DashboardDataModule;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 *
 * @author Hashan
 */
public class DashboardView implements Initializable, FxmlView<DashboardViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(DashboardView.class);

    @FXML
    private Pane productivityWidgetPane;

    @FXML
    private Pane attendanceWidgetPane;

    @FXML
    private Pane activitySummaryWidgetPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize Activity Summary Widget
        try {
            ViewTuple<ActivitySummaryWidget, ActivitySummaryWidgetModel> activitySummaryWidgetTuple
                    = FluentViewLoader.fxmlView(ActivitySummaryWidget.class).load();
            Node activitySummaryWidgetNode = activitySummaryWidgetTuple.getView();
            activitySummaryWidgetPane.getChildren().add(activitySummaryWidgetNode);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Initialize Activity Summary Widget Exception", ex);
        }

        // Initialize Attendance Widget
        try {
            ViewTuple<AttendanceWidget, AttendanceWidgetModel> attendanceWidgetTuple
                    = FluentViewLoader.fxmlView(AttendanceWidget.class).load();
            Node attendanceWidgetNode = attendanceWidgetTuple.getView();
            attendanceWidgetPane.getChildren().add(attendanceWidgetNode);
        } catch (Exception ex) {
            System.out.println("Initialize Attendance Widget >>> " + ex);
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Initialize Attendance Widget", ex);
        }

        // Initialize Productivity Widget
        try {
            ViewTuple<ProductivityWidget, ProductivityWidgetModel> productivityWidgetTuple
                    = FluentViewLoader.fxmlView(ProductivityWidget.class).load();
            Node productivityWidgetNode = productivityWidgetTuple.getView();
            productivityWidgetPane.getChildren().add(productivityWidgetNode);
        } catch (Exception ex) {
            System.out.println("Initialize Productivity Widget >>> " + ex);
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Initialize Productivity Widget", ex);
        }
    }
}
