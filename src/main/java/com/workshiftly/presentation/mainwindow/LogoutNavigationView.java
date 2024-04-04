/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow;

import com.workshiftly.application.state.StateName;
import com.workshiftly.presentation.mainwindow.component.HeaderNavigation;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 *
 * @author chamara
 */
public class LogoutNavigationView implements Initializable, FxmlView<LogoutNavigationViewModel> {
    
    @FXML
    private Pane rootWrapperPane;

    @FXML
    private Pane logoutSubContainer;

    @FXML
    private Button logoutBtn;
    
    @FXML
    private Button noBtn;

    @InjectViewModel
    LogoutNavigationViewModel viewModel;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        logoutBtn.disableProperty().bind(viewModel.getDisableLogoutBtnProperty());
        logoutBtn.setOnAction((ActionEvent actionEvent) -> {
            viewModel.handleLogout(actionEvent);
        });
        noBtn.setOnAction((ActionEvent actionEvent) -> {
            HeaderNavigation.activateNavigationItem(StateName.DEFAULT_ACTIVATED_TAB);
        });
    }
    
}
