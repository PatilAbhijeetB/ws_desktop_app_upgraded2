/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

/**
 *
 * @author jade_m
 */
public class ActivityDataLegendItem implements Initializable, FxmlView<ActivityDataLegendItemViewModel>{

    @FXML
    private HBox rootContainer;

    @FXML
    private Pane shapeContainer;

    @FXML
    private Label legendTitle;

    @FXML
    private Label percentageLbl;
    
    @FXML
    private Circle legendCircle;
    
    @InjectViewModel
    private ActivityDataLegendItemViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        legendCircle.fillProperty().bind(viewModel.getLegendCircleFillColor());
        legendCircle.strokeProperty().bind(viewModel.getLegendCircleFillColor());
        
        legendTitle.textProperty().bind(viewModel.getLegendTitlte());
        percentageLbl.textProperty().bind(viewModel.getLegendPercentage());
    }
    
}

