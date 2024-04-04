/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Paint;

/**
 *
 * @author jade_m
 */
public class ActivityDataLegendItemViewModel implements ViewModel {
    
    private final SimpleObjectProperty<Paint> legendCircleFillColor;
    private final SimpleStringProperty legendTitlte;
    private final SimpleStringProperty legendPercentage;
    
    public ActivityDataLegendItemViewModel() {
        legendCircleFillColor = new SimpleObjectProperty<>();
        legendTitlte = new SimpleStringProperty("");
        legendPercentage = new SimpleStringProperty("");
    }

    public SimpleObjectProperty<Paint> getLegendCircleFillColor() {
        return legendCircleFillColor;
    }
    
    public void setLegendCircleFill(Paint color) {
        legendCircleFillColor.set(color);
    }

    public SimpleStringProperty getLegendTitlte() {
        return legendTitlte;
    }
    
    public void setLegendTitle(String title) {
        legendTitlte.set(title);
    }

    public SimpleStringProperty getLegendPercentage() {
        return legendPercentage;
    }
    
    public void setLegendPercentage(String percentage) {
        legendPercentage.set(percentage);
    }
}
