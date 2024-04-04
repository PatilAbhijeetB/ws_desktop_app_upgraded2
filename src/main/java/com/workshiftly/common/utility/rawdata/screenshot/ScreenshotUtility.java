/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.screenshot;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.AppDirectory;
import com.workshiftly.common.model.CompanyConfiguration;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.FileUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.rawdata.BaseRawDataHandler;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.concurrent.ThreadExecutorService;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javax.imageio.ImageIO;

/**
 *
 * @author chamara
 */
public class ScreenshotUtility implements BaseRawDataHandler {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ScreenshotUtility.class);

    private static final int DEFAULT_SCREENSHOTS_PER_HOUR = 10;
    private static final String SCREENSOHT_EXTENSION = "png";
    public static final String RECORDED_MIME_TYPE = "image/png"; 

    private static int SCREENSHOT_WIDTH;
    private static int SCREENSHOT_HEIGHT;
    
    private static ScreenshotUtility INSTANCE;
    private static TimeslotDefinition timeslotDefinition;
    
    private final Robot robot;
    private final Toolkit toolkit;
    private final int screenshotPerHour;
    
    private ScreenshotUtility() throws Exception {
        
        robot = new Robot();
        toolkit = Toolkit.getDefaultToolkit();

        GraphicsDevice graphicsDevice = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        SCREENSHOT_WIDTH = graphicsDevice.getDisplayMode().getWidth();
        SCREENSHOT_HEIGHT = graphicsDevice.getDisplayMode().getHeight();
        
        CompanyConfiguration companyConfig = StateStorage
                .getCurrentState(StateName.COMPANY_CONFIGURATION);
        int _screenshotsPerHour = companyConfig.getNumberOfScreenshotsPerHour();
        screenshotPerHour = _screenshotsPerHour > 0 ? _screenshotsPerHour 
                : DEFAULT_SCREENSHOTS_PER_HOUR;
        
        long currentTimestamp = TimeUtility.getCurrentTimestamp();
        Duration utcTimeDuration = Duration.ofSeconds(currentTimestamp);
        timeslotDefinition = new TimeslotDefinition(
                utcTimeDuration, screenshotPerHour
        );
    }
    
    public static ScreenshotUtility getInstance() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new ScreenshotUtility();
        }
        return INSTANCE;
    }
    
    private BufferedImage takeCurrentScreenshot() throws Exception {
        
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenArea = new Rectangle(screenSize);
        BufferedImage capturedImage = robot.createScreenCapture(screenArea);
        
        capturedImage = scaleDownImageTodefault(capturedImage);
        return capturedImage;
    }
    
    private Screenshot captureCurrentScreenshot() throws Exception {
        
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        AppValidator.validateUserSession(userSession);
        
        Screenshot screenshot = new Screenshot();
        screenshot.setUserId(userSession.getId());
        screenshot.setCompanyId(userSession.getCompanyId());
        screenshot.setMimeType(RECORDED_MIME_TYPE);
        
        Long currentTimestamp = TimeUtility.getCurrentTimestamp();
        screenshot.setTimestamp(currentTimestamp);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        Date todayDate = new Date();
        String formattedDate = dateFormat.format(todayDate);
        
        String fileName = String.format(
                "%s/%s/%s/%s", 
                userSession.getCompanyId(),
                userSession.getId(),
                formattedDate,
                currentTimestamp
        );
        System.out.println("#### screenshot fileName >>> " + fileName);
        screenshot.setFileName(fileName);
        
        BufferedImage currentBufferedImg = takeCurrentScreenshot();
        screenshot.setBufferedImage(currentBufferedImg);
        
        String base64EncodedImg = imageToBase64String(currentBufferedImg);
        screenshot.setData(base64EncodedImg);
        
        return screenshot;
    }

    @Override
    public boolean handlePeridociDatabaseWriting() {
        
        try {
            List<Screenshot> screenshots = StateStorage.getCurrentState(StateName.SCREENSHOT_LIST);
            
            if (screenshots.isEmpty()) {
                return true;
            }
            
            Database<Screenshot> database = DatabaseProxy.openConnection(Screenshot.class);
            Response response = database.create(screenshots);
            
            if (!response.isError()) {
                StateStorage.set(StateName.SCREENSHOT_LIST, ArrayList.class, new ArrayList());
            }
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to handle Peridoci Database Writing", ex);
            return false;
        } finally {
            DatabaseProxy.closeConnection(Screenshot.class);
        }
        return true;
    }
    
    public boolean persistCaputuredImagetoLocalDisk(BufferedImage screenshot) throws Exception {
        
        File imageDirPath = FileUtility.getApplicationResourceDirectory(AppDirectory.RAW_SCREENSHOTS);
        Long currentTimestamp = TimeUtility.getCurrentTimestamp();
        String iamgeFilePath = imageDirPath.getAbsolutePath() + FileUtility.FILE_SEPARATOR + currentTimestamp;
        File imageFile = new File(iamgeFilePath);
        return ImageIO.write(screenshot, "png", imageFile);
    }
    
    public BufferedImage scaleDownImageTodefault(BufferedImage image) {
        
        BufferedImage processedImage = new BufferedImage(
                SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, BufferedImage.TYPE_INT_RGB
        );
        AffineTransform tranformConfig = new AffineTransform();
        tranformConfig.setToScale(1, 1);
        AffineTransformOp operation = new AffineTransformOp(tranformConfig, AffineTransformOp.TYPE_BILINEAR);
        processedImage = operation.filter(image, processedImage);
        return processedImage;
    }
    
    public String imageToBase64String(final BufferedImage image) throws IOException {
        
        final ByteArrayOutputStream outputSream = new ByteArrayOutputStream();
        ImageIO.write(image, SCREENSOHT_EXTENSION, outputSream);
        return Base64.getEncoder().encodeToString(outputSream.toByteArray());
    }
    
    public ScheduledFuture<?> capture() {
        // Todo: thread interval calculation based on number of screenshots per hour
        Runnable runnalbe = getRunnable();
        return ThreadExecutorService.executePeriodicTask(runnalbe, 10, 5);
    }
    
    private Runnable getRunnable() {
        return () -> {
            try {
                long currentTimestamp = TimeUtility.getCurrentTimestamp();
                
                if (timeslotDefinition.isCompleted()) {
                    CompanyConfiguration companyConfig = 
                            StateStorage.getCurrentState(StateName.COMPANY_CONFIGURATION);
                    Integer _screenshotPerHour = companyConfig.getNumberOfScreenshotsPerHour();
                    _screenshotPerHour = _screenshotPerHour != null && _screenshotPerHour > 0
                            ? _screenshotPerHour : DEFAULT_SCREENSHOTS_PER_HOUR;
                    
                    Duration utcTimeDuration = Duration.ofSeconds(currentTimestamp);
                    timeslotDefinition = new TimeslotDefinition(utcTimeDuration, _screenshotPerHour);
                }
                
                CapturePoint nextCapturePoint = timeslotDefinition.getNextCapturePoint();
                long nextCapturePointTimestamp = nextCapturePoint.getCaptureTimestamp();
                
                if (nextCapturePointTimestamp <= currentTimestamp) {
                    Screenshot capturedScreenshot = captureCurrentScreenshot();
                    timeslotDefinition.commitScreenshotToCurrentCapturePoint(capturedScreenshot);
                }
            } catch (Exception ex) {
                LOGGER.logRecord(
                        InternalLogger.LOGGER_LEVEL.SEVERE, 
                        "failed to get Runnable at ScreenshotUtility", 
                        ex
                );
            }            
        };
    }
    
    public void forceCommit() throws Exception {
        timeslotDefinition.forceToFinalize();
    }
    
}
