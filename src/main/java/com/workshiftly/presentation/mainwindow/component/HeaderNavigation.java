/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.application.state.StateName;
import com.workshiftly.presentation.mainwindow.LogoutNavigationView;
import com.workshiftly.presentation.mainwindow.LogoutNavigationViewModel;
import com.workshiftly.presentation.mainwindow.dashboard.DashboardView;
import com.workshiftly.presentation.mainwindow.dashboard.DashboardViewModel;
import com.workshiftly.presentation.mainwindow.project.ProjectView;
import com.workshiftly.presentation.mainwindow.settings.SettingsView;
import com.workshiftly.presentation.mainwindow.settings.SettingsViewModel;
import com.workshiftly.presentation.mainwindow.project.ProjectViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author chamara
 */
public final class HeaderNavigation {
    private static final InternalLogger LOGGER = LoggerService.getLogger(HeaderNavigation.class);

    private static final SimpleStringProperty ACITVATED_TAB_NAME = new SimpleStringProperty();
    private static final String DEFAULT_ACTIVATED_TAB = StateName.DEFAULT_ACTIVATED_TAB;
    private static final ArrayList<HeaderNavigationItem> navigationItems = new ArrayList<>();
    private static final Map<String, Node> navigationMapper = new HashMap<>();
    
    private static boolean isInitialized = false;
    
    private StackPane contentContainer;
    private HBox breadcumbWrapper;
    
    private HeaderNavigation() {}
    
    public static void initialize(StackPane contentContainer, HBox breadcumbWrapper) 
            throws IOException {
        if (isInitialized ) {
            uninitailize();
        }
        HeaderNavigation headerNavigation = new HeaderNavigation();
        headerNavigation.initNavigationItems(contentContainer, breadcumbWrapper);
    }
    
    private static void uninitailize() {
        navigationItems.clear();
        navigationMapper.clear();
    }
    
    private ArrayList<HeaderNavigationItem> initNavigationItems(
            StackPane contentContainer, HBox breadcumbWrapper
    ) throws IOException {
        
        this.contentContainer = contentContainer;
        this.breadcumbWrapper = breadcumbWrapper;
        
        navigationItems.add(dashBoardNavigationItem());
        navigationItems.add(projectNavigatorItem());
        navigationItems.add(settingsNavigatorItem());
        navigationItems.add(logoutNavigationItem());
        
        this.breadcumbWrapper.getChildren().addAll(navigationItems);
        
        ACITVATED_TAB_NAME.addListener((
                ObservableValue<? extends String> bservableValue, 
                String oldValue, 
                String newValue
        ) -> {
            HeaderNavigationItem nextNavigationItem = null;
            
            for (HeaderNavigationItem currentItem : navigationItems) {
                String currentNavigationIdentifier = currentItem.getNavigationIdentifier();
                boolean isCurrentActivatedTabItem = currentNavigationIdentifier.equals(newValue);
                currentItem.setActiveState(isCurrentActivatedTabItem);
                
                if (isCurrentActivatedTabItem) {
                    nextNavigationItem = currentItem;
                }
            }
            
            switchNavigationContent(nextNavigationItem);
        });
        
        activateNavigationItem(DEFAULT_ACTIVATED_TAB);
        HeaderNavigation.isInitialized = true;
        return navigationItems;
    }

    public static boolean isInitialized() {
        return HeaderNavigation.isInitialized;
    }
    
    public static final void activateNavigationItem(String navigationIdentifier) {
        ACITVATED_TAB_NAME.set(navigationIdentifier);
    }
    
    private static HeaderNavigationItem logoutNavigationItem() throws IOException {
        
        HeaderNavigationItem navigationItem = HeaderNavigationItem.init(StateName.LOGOUT_NAVIGATION_ITEM);
        final String imagePath = "/images/icons/logout.png";
        navigationItem.setNavigationIcon(imagePath);
        navigationItem.setNavigationNameTxt("Logout");
        navigationItem.setReuseIfExisting(true);
       
        return navigationItem;
    }
    
    private static HeaderNavigationItem dashBoardNavigationItem() throws IOException {
        
        HeaderNavigationItem navigationItem = HeaderNavigationItem.init(StateName.DASHBOARD_NAVIGATION_ITEM);
        final String imagePath = "/images/icons/Home_filled.png";
        navigationItem.setNavigationIcon(imagePath);
        navigationItem.setNavigationNameTxt("Home");
        navigationItem.setReuseIfExisting(true);
        return navigationItem;
    }
    
    private static HeaderNavigationItem projectNavigatorItem() throws IOException {
        
        HeaderNavigationItem navigationItem = HeaderNavigationItem.init(StateName.PROJECT_NAVIGATION_ITEM);
        final String imagePath = "/images/icons/Insights_filled.png";
        navigationItem.setNavigationIcon(imagePath);
        navigationItem.setNavigationNameTxt("Insights");
        navigationItem.setReuseIfExisting(true);
        return navigationItem;
    }
    
    private static HeaderNavigationItem settingsNavigatorItem() throws IOException {
        
        HeaderNavigationItem navigationItem = HeaderNavigationItem.init(StateName.SETTINGS_NAVIGATION_ITEM);
        final String imagePath = "/images/icons/Settings_filled.png";
        navigationItem.setNavigationIcon(imagePath);
        navigationItem.setNavigationNameTxt("Settings");
        navigationItem.setReuseIfExisting(false);
        return navigationItem;
    }

    public static SimpleStringProperty getCurrentActivatedNavigationName() {
        return ACITVATED_TAB_NAME;
    }
    
    private void switchNavigationContent(HeaderNavigationItem nextNavigation) {
        try {
            String navigationIdentifier = nextNavigation.getNavigationIdentifier();
            boolean hasNodeInMapper = navigationMapper.containsKey(navigationIdentifier);
            ObservableList<Node> contentContainerChildren = contentContainer.getChildren();
            
            Node currentNavigationView;
            
            if (nextNavigation.isReuseIfExisting() && hasNodeInMapper) {
                currentNavigationView = navigationMapper.get(navigationIdentifier);
                contentContainerChildren.remove(currentNavigationView);
            } else {
                currentNavigationView = navigationRegistry(nextNavigation);
                navigationMapper.put(navigationIdentifier, currentNavigationView);
            }
            
            contentContainerChildren.add(currentNavigationView);
        } catch (Exception ex) {
            //TODO: need recheck it
//            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Switch Navigation Content Exception", ex);
        }
    }
    
    private Node navigationRegistry(HeaderNavigationItem nextNavigation) {
        
        String navigationIdentifier = nextNavigation.getNavigationIdentifier();
        Node currentNavigationView = null;
        
        switch (navigationIdentifier) {
            case StateName.LOGOUT_NAVIGATION_ITEM: {

                FluentViewLoader.FxmlViewStep<LogoutNavigationView, LogoutNavigationViewModel> fxmlView 
                        = FluentViewLoader.fxmlView(LogoutNavigationView.class);
                ViewTuple<LogoutNavigationView, LogoutNavigationViewModel> viewTuple = fxmlView.load();
                currentNavigationView = viewTuple.getView();
                contentContainer.getChildren().add(currentNavigationView);
                break;
            }
            case StateName.PROJECT_NAVIGATION_ITEM: {
               FluentViewLoader.FxmlViewStep<DashboardView, DashboardViewModel> fxmlView
                        = FluentViewLoader.fxmlView(DashboardView.class);
                ViewTuple<DashboardView, DashboardViewModel> viewTuple = fxmlView.load();
                currentNavigationView = viewTuple.getView();
                contentContainer.getChildren().add(currentNavigationView);
                break;
            }
            case StateName.DASHBOARD_NAVIGATION_ITEM: {
                 FluentViewLoader.FxmlViewStep<ProjectView, ProjectViewModel> fxmlView
                        = FluentViewLoader.fxmlView(ProjectView.class);
                ViewTuple<ProjectView, ProjectViewModel> viewTuple = fxmlView.load();
                currentNavigationView = viewTuple.getView();
                contentContainer.getChildren().add(currentNavigationView);
                break;
                
            }
          //  case StateName.DASHBOARD_NAVIGATION_ITEM: {
            //    FluentViewLoader.FxmlViewStep<DashboardView, DashboardViewModel> fxmlView
              //          = FluentViewLoader.fxmlView(DashboardView.class);
                //ViewTuple<DashboardView, DashboardViewModel> viewTuple = fxmlView.load();
                //currentNavigationView = viewTuple.getView();
                //contentContainer.getChildren().add(currentNavigationView);
                //break;
           // }
            case StateName.SETTINGS_NAVIGATION_ITEM: {
                FluentViewLoader.FxmlViewStep<SettingsView, SettingsViewModel> fxmlView
                        = FluentViewLoader.fxmlView(SettingsView.class);
                ViewTuple<SettingsView, SettingsViewModel> viewTuple = fxmlView.load();
                currentNavigationView = viewTuple.getView();
                contentContainer.getChildren().add(currentNavigationView);
                break;
            }
            default: {
                break;
            }
        }
        return currentNavigationView;
    }
    
    
    public static void setAlertVisibility(String navigationName, boolean visibility) {
        
        if (isInitialized) {
            HeaderNavigationItem navItemComponent = navigationItems.stream()
                    .filter((HeaderNavigationItem navItem) -> {
                        String navItemIdentifier = navItem.getNavigationIdentifier();
                        return navItemIdentifier.equals(navigationName);
                    }).findFirst().orElse(null);
            
            if (navItemComponent != null) {
                navItemComponent.setAlertVisibility(visibility);
            }
        }
    }
}
