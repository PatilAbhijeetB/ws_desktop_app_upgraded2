/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.factory;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.sun.webkit.graphics.WCImage;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowView;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowViewModel;
import com.workshiftly.presentation.view.ForgetPasswordView;
import com.workshiftly.presentation.view.LoadingSplashView;
import com.workshiftly.presentation.view.LoginViewNew;
import com.workshiftly.presentation.view.TermsOfServiceView;
import com.workshiftly.presentation.view.ChangePasswordView;
import com.workshiftly.presentation.viewmodel.ForgetPasswordViewModel;
import com.workshiftly.presentation.viewmodel.LoadingSplashViewModel;
import com.workshiftly.presentation.viewmodel.LoginViewModelNew;
import com.workshiftly.presentation.viewmodel.TermsOfServiceViewModel;
import com.workshiftly.presentation.viewmodel.ChangePasswordViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.swing.Icon;



/**
 *
 * @author chamara
 */
public class MainScreenController {

    private static final InternalLogger LOGGER = LoggerService.getLogger(MainScreenController.class);

    public static final int WINDOW_WIDTH = 1063;
    public static final int WINDOW_HEIGHT = 672;

    // after refacor main screens as responsive manner delete this above window
    // height and weight
    public static final int AUTHENTICATED_MAIN_WINDOW_WIDTH = 1063;
    public static final int AUTHENTICATED_MAIN_WINDOW_HEIGHT = 672;

    private static final boolean IS_ANIMATED_SCREEN_CHANGES = DotEnvUtility.isAnimatedMainScreenChanges();

    private static final String WELCOME_SCREEN_NAME = StateName.MAIN_LOGIN_SCREEN;
    private static final String LOADING_SPLASH_SCREEN_NAME = StateName.LOADING_SPLASH_SCREEN;

    private static final Map<String, Node> SCREEN_COLLECTION = new HashMap<>();
    private static final StackPane ROOT_PANE = new StackPane();
    private static final StringProperty CURRENT_MAIN_SCREEN_NAME_PROPERTY = new SimpleStringProperty(
            StateName.NO_MAIN_SCREEN);

    private static boolean isAcitvatedSplashScreen = false;

    private static Stage mainStage;
    private static Scene mainScene;

    private static final String CLIENT_ID = "your_client_id"; // Replace with your client ID
    private static final String TENANT_ID = "your_tenant_id"; // Replace with your tenant ID
    private static final String CLIENT_SECRET = "your_client_secret"; // Replace with your client secret

    
    public static void initialize(Stage mainStage) throws IOException, UnknownSceneException, Exception {

        MainScreenController.mainStage = mainStage;
        MainScreenController.mainStage.setWidth(WINDOW_WIDTH);
        MainScreenController.mainStage.setHeight(WINDOW_HEIGHT);
        MainScreenController.mainStage.setResizable(false);

        String appName = DotEnvUtility.ApplicationName();
        String applicationVersion = DotEnvUtility.getApplicationVersion();
         String stageTitle = appName + " | Helping the World Track Time | v " +
         applicationVersion;
       //  String stageTitle = appName + " | Helping the World Track Time | v " +
       // applicationVersion + "_QA";
        //String stageTitle = appName + " | Helping the World Track Time | v " + applicationVersion + "_Staging";
       MainScreenController.mainStage.setTitle(stageTitle);

        registerMainScreenNameChangePropertyListner();
        activate(WELCOME_SCREEN_NAME, false);

        String workshiftlyThemeFile = "/css/workshiftly_theme.css";
        String COMPANY_LOGO_PATH1 = "/images/Frame12.ico";
        try {

            InputStream companyLogoStream = MainScreenController.class.getResourceAsStream(COMPANY_LOGO_PATH1);
            Image companyLogo = new Image(companyLogoStream);

            mainStage.getIcons().add(companyLogo);

        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE,
                    "Failed initialize MainScreenConroller",
                    ex);
        }
        try {
            String workStringThemeCSS = MainScreenController.class
                    .getResource(workshiftlyThemeFile).toExternalForm();
            mainScene.getStylesheets().add(workStringThemeCSS);
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE,
                    "Failed initialize MainScreenConroller",
                    ex);
        }

      
        
        MainScreenController.mainStage.show();
    }

    private MainScreenController() {
    }

    private MainScreenController(Stage mainStage) {
        MainScreenController.mainStage = mainStage;

    }

    public static final class UnknownSceneException extends Exception {

        public UnknownSceneException() {
        }

        public UnknownSceneException(String message) {
            super(message);
        }
    }

    private static Node screenRegistry(String screenName) throws UnknownSceneException {

        Node currentParent = null;
        switch (screenName) {
            case StateName.MAIN_LOGIN_SCREEN:

                ViewTuple<LoginViewNew, LoginViewModelNew> viewTuple = FluentViewLoader.fxmlView(LoginViewNew.class)
                        .load();
                currentParent = viewTuple.getView();
                break;

            case StateName.TERMS_OF_SERVICE_SCREEN:

                ViewTuple<TermsOfServiceView, TermsOfServiceViewModel> termsOfServiceViewTuple = FluentViewLoader
                        .fxmlView(TermsOfServiceView.class).load();
                currentParent = termsOfServiceViewTuple.getView();
                break;

            case StateName.FORGET_PASSWORD_SCREEN:

                ViewTuple<ForgetPasswordView, ForgetPasswordViewModel> forgetPwScreenViewTuple = FluentViewLoader
                        .fxmlView(ForgetPasswordView.class).load();
                currentParent = forgetPwScreenViewTuple.getView();
                break;

            case StateName.CHANGE_PASSWORD_SCREEN:
                ViewTuple<ChangePasswordView, ChangePasswordViewModel> changePasswordViewTuple = FluentViewLoader
                        .fxmlView(ChangePasswordView.class).load();
                currentParent = changePasswordViewTuple.getView();
                break;
            case StateName.AUTHENTICATED_MAIN_WINDOW:
                ViewTuple<AuthenticatedMainWindowView, AuthenticatedMainWindowViewModel> authenticatedMainWindow = FluentViewLoader
                        .fxmlView(AuthenticatedMainWindowView.class).load();
                currentParent = authenticatedMainWindow.getView();
                break;

            default:
                throw new UnknownSceneException("Unknown scene exception");
        }

        return currentParent;
    }

    public static void activate(String sceneName, boolean reuseIfExistingScene)
            throws IOException, UnknownSceneException, Exception {

        final boolean isBackwardTransition = false;
        activate(sceneName, reuseIfExistingScene, isBackwardTransition);
    }

    public static void activate(
            String screenName, boolean reuseIfExistingScene, boolean isBackwardTransition)
            throws IOException, UnknownSceneException, Exception {

        Node currentParent = null;

        if (reuseIfExistingScene) {
            currentParent = SCREEN_COLLECTION.get(screenName);

            if (currentParent != null) {
                ObservableList<Node> childrenNodes = ROOT_PANE.getChildren();
                childrenNodes.remove(currentParent);
            }
        }

        if (currentParent == null) {
            currentParent = screenRegistry(screenName);
        }

        if (IS_ANIMATED_SCREEN_CHANGES) {
            double translateXproperty = isBackwardTransition ? ROOT_PANE.getWidth() : -1 * ROOT_PANE.getWidth();
            currentParent.translateXProperty().set(translateXproperty);
        }

        CURRENT_MAIN_SCREEN_NAME_PROPERTY.set(screenName);
        ROOT_PANE.getChildren().add(currentParent);
        SCREEN_COLLECTION.put(screenName, currentParent);

        if (IS_ANIMATED_SCREEN_CHANGES) {
            KeyValue slideAnimationKeyValue = new KeyValue(currentParent.translateXProperty(), 0, Interpolator.EASE_IN);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100), slideAnimationKeyValue);
            Timeline timeline = new Timeline(keyFrame);
            timeline.play();
        }
    }

    public static void showLoadingSplashScreen() {

        if (MainScreenController.isAcitvatedSplashScreen) {
            hideLoadingSplashScreen();
        }

        Node splashScreenNode = SCREEN_COLLECTION.get(LOADING_SPLASH_SCREEN_NAME);

        if (splashScreenNode == null) {
            splashScreenNode = initailizeSplashLoadingScreen();
        }
        ROOT_PANE.getChildren().add(splashScreenNode);
        MainScreenController.isAcitvatedSplashScreen = true;
    }

    public static void hideLoadingSplashScreen() {
        boolean isActivatedSpalshScreen = MainScreenController.isAcitvatedSplashScreen;

        if (!isActivatedSpalshScreen) {
            return;
        }

        Node splashScreen = SCREEN_COLLECTION.get(LOADING_SPLASH_SCREEN_NAME);
        ROOT_PANE.getChildren().remove(splashScreen);
        MainScreenController.isAcitvatedSplashScreen = false;
    }

    private static Parent initailizeSplashLoadingScreen() {

        ViewTuple<LoadingSplashView, LoadingSplashViewModel> viewYuple = FluentViewLoader
                .fxmlView(LoadingSplashView.class).load();
        Parent spalshScreen = viewYuple.getView();
        SCREEN_COLLECTION.put(LOADING_SPLASH_SCREEN_NAME, spalshScreen);
        return spalshScreen;
    }

    public static void showJFXDialogPane(JFXDialogLayout dialogLayout, JFXButton closeButton) {

        JFXDialog jfxDialog = new JFXDialog(
                ROOT_PANE, dialogLayout, JFXDialog.DialogTransition.CENTER);
        closeButton.setOnAction((actionEvent) -> {
            jfxDialog.close();
        });
        dialogLayout.setActions(closeButton);
        jfxDialog.show();
    }

    public static void showJFXDialog(JFXDialog jfxDialog) {
        jfxDialog.setOverlayClose(false);
        jfxDialog.show(ROOT_PANE);
    }

    private static void registerMainScreenNameChangePropertyListner() {
        CURRENT_MAIN_SCREEN_NAME_PROPERTY.addListener((
                ObservableValue<? extends String> observableValue,
                String oldScreen,
                String newScreen) -> {
            try {
                StateStorage.set(StateName.CURRENT_MAIN_SCREEN, String.class, newScreen);
            } catch (Exception ex) {
                // need to heal this exception in application level not to send log to sentry
                // very rarely occurred exception risk - very low
                System.out.println("#### aaaaaaaa exception ex " + ex);
            }

            if (oldScreen.equals(StateName.NO_MAIN_SCREEN)
                    && newScreen.equals(StateName.MAIN_LOGIN_SCREEN)) {
                MainScreenController.changeRootPaneSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                mainScene = new Scene(ROOT_PANE, WINDOW_WIDTH, WINDOW_HEIGHT);
                mainScene.setRoot(ROOT_PANE);
                MainScreenController.mainStage.setScene(mainScene);
                return;
            }

            if (oldScreen.equals(StateName.MAIN_LOGIN_SCREEN)
                    && newScreen.equals(StateName.AUTHENTICATED_MAIN_WINDOW)) {
                MainScreenController.changeRootPaneSize(
                        AUTHENTICATED_MAIN_WINDOW_WIDTH,
                        AUTHENTICATED_MAIN_WINDOW_HEIGHT);
                MainScreenController.mainStage.setScene(mainScene);
            }

            if (oldScreen.equals(StateName.AUTHENTICATED_MAIN_WINDOW)
                    && newScreen.equals(StateName.MAIN_LOGIN_SCREEN)) {
                MainScreenController.changeRootPaneSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                MainScreenController.mainStage.setScene(mainScene);

                // remove authenticated_main_window from root stack pane
                Node authenticatedMainWindowNode = SCREEN_COLLECTION.get(
                        StateName.AUTHENTICATED_MAIN_WINDOW);
                ROOT_PANE.getChildren().remove(authenticatedMainWindowNode);

                // stop idletime listener
                ScheduledService<Long> idleTimerScheduleService = AuthenticatedMainWindowViewModel
                        .getIdleTimerScheduleService();
                idleTimerScheduleService.cancel();
            }
        });
    }

    private static void changeRootPaneSize(int width, int height) {

        ROOT_PANE.setPrefSize(width, height);
        ROOT_PANE.setMaxSize(width, height);
        ROOT_PANE.setMinSize(width, height);

        mainStage.setWidth(width);
        mainStage.setHeight(height);
    }

    public static void setMainStageIconified(boolean value) {
        if (mainStage != null) {
            mainStage.setIconified(value);
        }
    }

    public static boolean isMainStatgeFocused() {
        return mainStage.isFocused();
    }

  
}
