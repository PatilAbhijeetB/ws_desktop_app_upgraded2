/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.domain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.AppData;
import com.workshiftly.common.model.BreakReason;
import com.workshiftly.common.model.Company;
import com.workshiftly.common.model.CompanyConfiguration;
import com.workshiftly.common.model.CompanySetting;
import com.workshiftly.common.model.DeviceSetting;
import com.workshiftly.common.model.LoggedInDevice;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.CommonUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.projectandtask.ProjectAndTaskUtility;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import javax.imageio.ImageIO;
import org.joda.time.DateTimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;

public class AuthenticationModule {
    private static final InternalLogger LOGGER = LoggerService.getLogger(AuthenticationModule.class);
    
    /**
     * Method Name: handleChangeInitailPassword
     * 
     * Description: changing logged user's initial password field where initial password
     * is meant randomly generated password of use while creation of that user at API
     * 
     * Valid InputData object:
     * 
     *      type: import com.google.gson.JsonObject;
     *      inputData: {
     *          currentPassword: "Zxer!3skd4^",
     *          newPassword: "Abcd@1234",
     *          confirmPassword: "Abcd@1234"
     *      }
     * 
     * @param inputData
     * @return 
     */
     public synchronized Response handleUserLogin(JsonObject inputData) {
        
        try {
            Gson gson = new Gson();
            
            AppValidator appValidator = new AppValidator();
            Response response = appValidator.validateLoginDetails(inputData);
            if (response.isError()) { return response; }
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            response = httpRequestCaller.callUserLogin(inputData);
            
            if (response.isError()) {
                return response;
            }
            
            List<AppData> appDataList = new ArrayList<>();
            
            JsonObject responseData = response.getData().getAsJsonObject();
            
            String userAuthToken = responseData.get("token").getAsString();
            AppData appDataAuthToken = new AppData(StateName.AUTH_TOKEN, userAuthToken);
            appDataList.add(appDataAuthToken);
            StateStorage.set(StateName.AUTH_TOKEN, String.class, userAuthToken);
            
            JsonObject userObject = responseData.get("user").getAsJsonObject();
            UserSession userSession = gson.fromJson(userObject, UserSession.class);
            userSession.setAuthToken(userAuthToken);
            
            boolean isClientActived = userSession.isIsClientActive();
            if (!isClientActived) {
                String password = inputData.get("password").getAsString();
                StateStorage.set(StateName.USER_INITIAL_PASSWORD, String.class, password);
            }

            
            String profileImageBase64String = userObject.get("profileImage").getAsString();
            BufferedImage bufferedProfileImage = initProfileImage(profileImageBase64String);
            StateStorage.set(StateName.USER_PROFILE_PICTURE, BufferedImage.class, bufferedProfileImage);

            AppData appDataUserSession = new AppData(StateName.USER_SESSION, gson.toJson(userSession));
            appDataList.add(appDataUserSession);
            StateStorage.set(StateName.USER_SESSION, UserSession.class, userSession);
            
            CompanySetting companySettings = gson.fromJson(userObject.get("company"), CompanySetting.class);
            AppData appDataCompany = new AppData(StateName.COMPANY_SETTINGS, gson.toJson(companySettings));
            appDataList.add(appDataCompany);
            StateStorage.set(StateName.COMPANY_SETTINGS, CompanySetting.class, companySettings);
            
            Response companyApiResponse = httpRequestCaller.callGetCompany(userSession.getCompanyId());
            if (companyApiResponse.isError()) {
                return companyApiResponse;
            }
            
            JsonElement companyApiResponseData = companyApiResponse.getData();
            Company companyObject = gson.fromJson(companyApiResponseData, Company.class);
            AppData appDataCompanyObj = new AppData(StateName.USER_COMPANY_INSTANCE, gson.toJson(companyObject));
            appDataList.add(appDataCompanyObj);
            StateStorage.set(StateName.USER_COMPANY_INSTANCE, Company.class, companyObject);
            
            CompanyConfiguration companyConfiguration = initCompanyConfiguration(companyObject, httpRequestCaller);
            AppData appDataCompanyConfiguration = new AppData(StateName.COMPANY_CONFIGURATION, gson.toJson(companyConfiguration));
            appDataList.add(appDataCompanyConfiguration);
            StateStorage.set(StateName.COMPANY_CONFIGURATION, CompanyConfiguration.class, companyConfiguration);

            DateTimeZone companyDateTimeZone = TimeUtility.initDateTimeZone(companyObject.getTimezone());
            StateStorage.set(StateName.COMPANY_TIMEZONE, DateTimeZone.class, companyDateTimeZone);
            
            DateTimeZone userDateTimeZone = TimeUtility.initDateTimeZone(null);
            StateStorage.set(StateName.USER_TIMEZONE, DateTimeZone.class, userDateTimeZone);
            TimeUtility.initWorkDateTime();

            JsonObject logingDevice = userObject.get("device").getAsJsonObject();
            DeviceSetting deviceSettingObj = gson.fromJson(logingDevice,DeviceSetting.class); 
            AppData appDataLoggingDeviceObj = new AppData(StateName.DEVICE_SETTINGS,gson.toJson(deviceSettingObj));
           
            appDataList.add(appDataLoggingDeviceObj);
            StateStorage.set(StateName.DEVICE_SETTINGS, DeviceSetting.class, deviceSettingObj);
            
            
            ////
            JsonObject logingDevicefromDevice = logingDevice.get("loggedInDevice").getAsJsonObject();
            LoggedInDevice logingDeviceSettingObj = gson.fromJson(logingDevicefromDevice,LoggedInDevice.class); 
            AppData appDataLoggingDeviceObjs = new AppData(StateName.LOGGED_IN_DEVICE_SETTINGS,gson.toJson(logingDeviceSettingObj));
            userSession.setDeviceId(logingDeviceSettingObj.getDeviceId().toString());
            appDataList.add(appDataLoggingDeviceObjs);
            StateStorage.set(StateName.LOGGED_IN_DEVICE_SETTINGS, LoggedInDevice.class, logingDeviceSettingObj);
            ///
            
            
            Response tocAcceptApiResponse = httpRequestCaller.getLatestUserTOCAcceptance(userSession.getId());
            if (tocAcceptApiResponse.isError()) {
                return tocAcceptApiResponse;
            } 
            
            
             JsonElement element = tocAcceptApiResponse.getData();
                    JsonObject jsonObj = element.isJsonObject() 
                            ? element.getAsJsonObject()
                            : new JsonObject();
                   
                    String currentStatus = jsonObj.has("status")
                            ? jsonObj.get("status").getAsString() : null;

                    if (currentStatus != null && currentStatus.equals("accepted")) {  

                       //// Xml document write
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Document doc = builder.newDocument();//create temp

                //create root node
                Element authontication = doc.createElement("Authontication");

                //create AUTH_TOKEN
                Element AUTH_TOKEN = doc.createElement("AUTH_TOKEN");
                Text AUTH_TOKEN_VALUE = doc.createTextNode(userAuthToken);
                AUTH_TOKEN.appendChild(AUTH_TOKEN_VALUE);

                //create USER_SESSION
                Element USER_SESSION = doc.createElement("USER_SESSION");
                Text USER_SESSION_VALUE = doc.createTextNode(gson.toJson(userSession));
                USER_SESSION.appendChild(USER_SESSION_VALUE);

                //create COMPANY_SETTINGS
                Element COMPANY_SETTINGS = doc.createElement("COMPANY_SETTINGS");
                // mark.setTextContent();
                Text COMPANY_SETTINGS_VALUE = doc.createTextNode(gson.toJson(companySettings));
                COMPANY_SETTINGS.appendChild(COMPANY_SETTINGS_VALUE);

                //create USER_COMPANY_INSTANCE
                Element USER_COMPANY_INSTANCE = doc.createElement("USER_COMPANY_INSTANCE");
                // mark.setTextContent();
                Text user_company_instance_value = doc.createTextNode(gson.toJson(companyObject));
                USER_COMPANY_INSTANCE.appendChild(user_company_instance_value);

                //create COMPANY_CONFIGURATION
                Element company_configuration = doc.createElement("COMPANY_CONFIGURATION");
                // mark.setTextContent();
                Text company_configuration_Value = doc.createTextNode(gson.toJson(companyConfiguration));
                company_configuration.appendChild(company_configuration_Value);
                
                
                //create COMPANY_CONFIGURATION
                Element device_setting = doc.createElement("DEVICE_SETTINGS");
                // mark.setTextContent();
                Text device_setting_Value = doc.createTextNode(gson.toJson(deviceSettingObj));
                device_setting.appendChild(device_setting_Value);
                
                 //create COMPANY_CONFIGURATION
                Element logged_in_device_setting = doc.createElement("LOGGED_IN_DEVICE_SETTINGS");
                // mark.setTextContent();
                Text logged_in_device_setting_Value = doc.createTextNode(gson.toJson(logingDeviceSettingObj));
                logged_in_device_setting.appendChild(logged_in_device_setting_Value);

                //add to authontication node
                authontication.appendChild(AUTH_TOKEN);
                authontication.appendChild(USER_SESSION);
                authontication.appendChild(COMPANY_SETTINGS);
                authontication.appendChild(USER_COMPANY_INSTANCE);
                authontication.appendChild(company_configuration);
                authontication.appendChild(device_setting);
                authontication.appendChild(logged_in_device_setting);


                //add root to  document
                doc.appendChild(authontication);

                //write from temp memory to file
                //create nguon data
                DOMSource source = new DOMSource(doc);
                //create result stream
                 String userDefPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Documents\\source.xml";
                 String userDefPath1 = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\source.xml";
      //  String path = "C:\\Users\\Niroshan\\Desktop\\My\\HelloWorld\\src\\com\\emageia\\source.xml";

                File f = new File(userDefPath);
                File f1 = new File(userDefPath1);
                Result result = new StreamResult(f);
                Result result1 = new StreamResult(f1);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
                transformer.transform(source, result1);

            } catch (ParserConfigurationException ex) {
                //   Logger.getLogger(DemoDOMCreate.class.getName()).log(Level.SEVERE,null,ex);
                // Logger.getLogger(DemoDoMCreate);
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

            //// Xml document write End
                    } else {
                       
                    }
            
            try {
                Database<AppData> database = DatabaseProxy.openConnection(AppData.class);
                database.flushData();
                Response dbResponse = database.create(appDataList);
                
                if (dbResponse.isError()) {
                    return dbResponse;
                }
                
            } finally {
                DatabaseProxy.closeConnection(AppData.class);
            }
            
            ProjectTaskModule projectTaskModule = new ProjectTaskModule();
            projectTaskModule.getAsyncRemoteProjectTasks(userSession.getId());
            
            RawDataModule rawDataModule = new RawDataModule();
            rawDataModule.deleteSyncedRowData();
            
            return new Response(false, StatusCode.SUCCESS, "User login in successful");
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while login", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while login");
        }
    }
        
    /**
     * Method Name: handleChangeInitailPassword
     * 
     * Description: changing logged user's initial password field where initial password
     * is meant randomly generated password of use while creation of that user at API
     * 
     * Valid InputData object:
     * 
     *      type: import com.google.gson.JsonObject;
     *      inputData: {
     *          currentPassword: "Zxer!3skd4^",
     *          newPassword: "Abcd@1234",
     *          confirmPassword: "Abcd@1234"
     *      }
     * 
     * @param inputData
     * @return 
     */
    public synchronized Response handleChangeInitailPassword(JsonObject inputData) {
        try {
            Gson gson = new Gson();
            
            AppValidator appValidator = new AppValidator();
            Response validationResult = appValidator.validateChangePasswordDetails(inputData);
            
            if (validationResult.isError()) {
                return validationResult;
            }
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            
            inputData.addProperty("id", userSession.getId());
            inputData.addProperty("token", userSession.getAuthToken());
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            Response response = httpRequestCaller.callUserChanagePassword(inputData);
            
            if (response.isError()) {
                return response;
            }
            
            userSession.setIsClientActive(true);
            StateStorage.set(StateName.USER_SESSION, UserSession.class, userSession);
            
            try {
                Database<AppData> database = DatabaseProxy.openConnection(AppData.class);
                UpdateBuilder<AppData, Long> updateBuilder = database.getUpdateBuilder();
                
                updateBuilder.where().eq(AppData.TYPE_FIELD_NAME, StateName.USER_SESSION);
                updateBuilder.updateColumnValue(AppData.DATA_FIELD_NAME, gson.toJson(userSession));
                updateBuilder.update();
            } finally {
                DatabaseProxy.closeConnection(AppData.class);
            }
            
            return new Response(false, StatusCode.SUCCESS, "Successfully changed password");
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while change initial password", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while change initial password");
        }
    }
    
    /**
     * Method Name: handleChangeInitailPassword
     * Description: reset user's password for forget password scenario
     * Valid InputData object:
     *      type: import com.google.gson.JsonObject;
     *      inputData: {
     *          "user": {
     *              "email": "johnSmith@abc.com"
     *          }
     *      }
     * @param inputData
     * @return 
     */
    public synchronized Response handleForgetPassword(JsonObject inputData) {
        
        try {
            AppValidator appValidator = new AppValidator();
            Response validationResult = appValidator.validateForgetPasswordInputData(inputData);
            if (validationResult.isError()) {
                return validationResult;
            }
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            Response apiResponse = httpRequestCaller.callForgetPasswordRequest(inputData);
            
            return apiResponse;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while reset password", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while reset password");
        }
    }
    
    /**
     * Method Name: initCompanyConfiguration
     * Description: fetch company configurations from API and initialize CompanyConfiguration object 
     * Valid InputData object:
     *      type: import com.google.gson.JsonObject;
     *      inputData: {
     *           "id": "d9162c85-5f9c-4824-9ddc-0972201e88a4",
     *           "appIdleTime": 3,
     *           "isScreenCapturing": 1,
     *           "screenCapturingInterval": 15,
     *           "isTrackWithinShift": 0,
     *           "allowUsersToAddBreakReasons": 1
     *          ...
     *      }
     *      
     * @param inputData
     * @return 
     */
    private CompanyConfiguration initCompanyConfiguration(Company company, HttpRequestCaller httpRequestCaller) throws Exception {
        
        Gson gson = new Gson();
        String companyId = company.getId();
        String configurationId = company.getConfigurationId();
        
        Response apiResponse = httpRequestCaller.callGetCompanyConfigurations(companyId, configurationId);
        if (apiResponse.isError()) {
            throw new Exception(
                    "Error occurred while retrieving company configutations " + apiResponse.getMessage()
            );
        }
        
        JsonObject responseData = apiResponse.getData().getAsJsonObject();
        CompanyConfiguration companyConfiguration = new CompanyConfiguration();
        
        String id = responseData.get("id").getAsString();
        companyConfiguration.setId(id);
        
        Integer appIdleTime = 5;
        if (responseData.has("appIdleTime")) {
            appIdleTime = responseData.get("appIdleTime").isJsonNull() 
                    ?  appIdleTime
                    : responseData.get("appIdleTime").getAsInt();
        }
        companyConfiguration.setAppIdleTime(appIdleTime);
        
        Boolean isScreenCapturing = false;
        if (responseData.has("isScreenCapturing")) {
            isScreenCapturing = responseData.get("isScreenCapturing").isJsonNull() 
                    ? isScreenCapturing
                    : CommonUtility.parseToBoolean(responseData.get("isScreenCapturing").getAsInt());
        }
        companyConfiguration.setIsScreenCapturing(isScreenCapturing);
        
        Integer numberOfScreenshotsPerHour = 5;
        if (responseData.has("screenShotsPerHour")) {
            numberOfScreenshotsPerHour = responseData.get("screenShotsPerHour").isJsonNull()
                    ? numberOfScreenshotsPerHour
                    : responseData.get("screenShotsPerHour").getAsInt();
        }
        companyConfiguration.setNumberOfScreenshotsPerHour(numberOfScreenshotsPerHour);
        
        boolean isTrackWithinShift = false;
        if (responseData.has("isTrackWithinShift")) {
            isTrackWithinShift = responseData.get("isTrackWithinShift").isJsonNull() 
                    ? isTrackWithinShift
                    : CommonUtility.parseToBoolean(responseData.get("isTrackWithinShift").getAsInt());
        }
        companyConfiguration.setIsTrackWithinShift(isTrackWithinShift);
        
        boolean allowUsersToAddBreakReasons = true;
        if (responseData.has("allowUsersToAddBreakReasons")) {
            allowUsersToAddBreakReasons = responseData.get("allowUsersToAddBreakReasons").isJsonNull()
                    ? allowUsersToAddBreakReasons
                    : CommonUtility.parseToBoolean(responseData.get("allowUsersToAddBreakReasons").getAsInt());
        }
        companyConfiguration.setAllowUsersToAddBreakReasons(allowUsersToAddBreakReasons);
        
        boolean allowNotification = false;
        if (responseData.has("allowNotification")) {
            allowNotification = responseData.get("allowNotification").isJsonNull()
                    ? allowNotification
                    : CommonUtility.parseToBoolean(responseData.get("allowNotification").getAsInt());
        }
        companyConfiguration.setAllowNotification(allowNotification);
        
        Type breakReasonListType = new TypeToken<ArrayList<BreakReason>>(){}.getType();
        ArrayList<BreakReason> breakReasons = new ArrayList<>();
        if (responseData.has("breakReasons")) {
            breakReasons = responseData.get("breakReasons").isJsonNull()
                    ? breakReasons
                    : gson.fromJson(responseData.get("breakReasons"), breakReasonListType);
        }
        companyConfiguration.setBreakReasons(breakReasons);
        
        boolean activeSilentTracking = false;
        if (responseData.has("activeSilentTracking")) {
            activeSilentTracking = responseData.get("activeSilentTracking").isJsonNull()
                    ? activeSilentTracking
                    : CommonUtility.parseToBoolean(responseData.get("activeSilentTracking").getAsInt());
        }
        companyConfiguration.setActiveSilentTracking(activeSilentTracking);
        
        long createdAt = responseData.get("createdAt").getAsLong();
        companyConfiguration.setCreatedAt(createdAt);
        
        long updatedAt = responseData.get("updatedAt").getAsLong();
        companyConfiguration.setUpdatedAt(updatedAt);
        
        return companyConfiguration;
    }
    
    /**
     * Method Name: handleLogout
     * Description: handle user logout and stop raw data capturing services 
     * Valid InputData object:
     *      none
     *      
     * @param inputData
     * @return 
     */
    public synchronized Response handleLogout() {
        try {
            
            
            ProjectAndTaskUtility patu=new ProjectAndTaskUtility();
            Response response = patu.updateDirtyTaskList();
            RawDataModule rawDataModule = new RawDataModule();
            rawDataModule.captureWorkStatusLog(WorkStatusLog.WorkStatus.STOP, null, true);
            rawDataModule.cancelCapturingServices(true);

            Map<String, ScheduledFuture<?>> cancelableRawThreadTasks = rawDataModule.getCancelableRawThreadTasks();
            boolean isCancelAllThreakTasks = false;

            while (!isCancelAllThreakTasks) {
                isCancelAllThreakTasks = cancelableRawThreadTasks
                        .values()
                        .stream()
                        .allMatch((ScheduledFuture<?> future) -> future.isDone());
            }

            Future<?> syncOnetimeFuture = SyncDataModule.invokeSyncDataAtOnce();
            boolean isDoneSyncOnetimeTask = syncOnetimeFuture.isDone();
            while (isDoneSyncOnetimeTask) {
                isDoneSyncOnetimeTask = syncOnetimeFuture.isDone();
            }
            
            return new Response(false, StatusCode.SUCCESS, "Successfully logged out user session");
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "unable to logout", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while logout");
        }
        
    }
    
    private BufferedImage initProfileImage(String imageBase64String) throws IOException {
        if (imageBase64String != null && !"".equals(imageBase64String)) {
            String imageDataBytes = imageBase64String.substring(imageBase64String.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(imageDataBytes);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            return bufferedImage;
        }

        return null;
    }
    
   

    public Response syncBandwidth(JsonObject bandwidthDetails) {
        
         UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
         String userId = userSession.getId();
        //  String deviceId = userSession.
         HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
         
         Response response = httpRequestCaller.insertBandwidthDetails(userId, bandwidthDetails); 
       
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public Response syncUserStatus(String uStatus)
    {
        try{
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
       String userId = userSession.getId();
        //  String deviceId = userSession.
       HttpRequestCaller httpRequestCaller = new HttpRequestCaller();  
        
       Response response = httpRequestCaller.insertUserStatus(userId, uStatus); 
       return response;
        }catch(Exception ec){
        throw ec;
        }
       
//       throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  
    }
    
     public Response syncGeoLocation(JsonObject locationdetails) {
        
         UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
         String userId = userSession.getId();
          String deviceId = userSession.getDeviceId();
        //  String deviceId = userSession.
         HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
         
         Response response = httpRequestCaller.insertLocationDetails(userId, deviceId,locationdetails); 
       
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
     
       public synchronized Response handleSkipUserLogin(JsonObject inputData) {
        
        try {
            Gson gson = new Gson();
            
         
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            Response response = httpRequestCaller.callUserSkipLogin(inputData);
            
            if (response.isError()) {
                return response;
            }
            
            List<AppData> appDataList = new ArrayList<>();
            
            JsonObject responseData = response.getData().getAsJsonObject();
            
            String userAuthToken = responseData.get("token").getAsString();
            AppData appDataAuthToken = new AppData(StateName.AUTH_TOKEN, userAuthToken);
            appDataList.add(appDataAuthToken);
            StateStorage.set(StateName.AUTH_TOKEN, String.class, userAuthToken);
            
            JsonObject userObject = responseData.get("user").getAsJsonObject();
            UserSession userSession = gson.fromJson(userObject, UserSession.class);
            userSession.setAuthToken(userAuthToken);
            
            boolean isClientActived = userSession.isIsClientActive();
            if (!isClientActived) {
                String password = inputData.get("password").getAsString();
                StateStorage.set(StateName.USER_INITIAL_PASSWORD, String.class, password);
            }

            
            String profileImageBase64String = userObject.get("profileImage").getAsString();
            BufferedImage bufferedProfileImage = initProfileImage(profileImageBase64String);
            StateStorage.set(StateName.USER_PROFILE_PICTURE, BufferedImage.class, bufferedProfileImage);

            AppData appDataUserSession = new AppData(StateName.USER_SESSION, gson.toJson(userSession));
            appDataList.add(appDataUserSession);
            StateStorage.set(StateName.USER_SESSION, UserSession.class, userSession);
            
            CompanySetting companySettings = gson.fromJson(userObject.get("company"), CompanySetting.class);
            AppData appDataCompany = new AppData(StateName.COMPANY_SETTINGS, gson.toJson(companySettings));
            appDataList.add(appDataCompany);
            StateStorage.set(StateName.COMPANY_SETTINGS, CompanySetting.class, companySettings);
            
            Response companyApiResponse = httpRequestCaller.callGetCompany(userSession.getCompanyId());
            if (companyApiResponse.isError()) {
                return companyApiResponse;
            }
            
            JsonElement companyApiResponseData = companyApiResponse.getData();
            Company companyObject = gson.fromJson(companyApiResponseData, Company.class);
            AppData appDataCompanyObj = new AppData(StateName.USER_COMPANY_INSTANCE, gson.toJson(companyObject));
            appDataList.add(appDataCompanyObj);
            StateStorage.set(StateName.USER_COMPANY_INSTANCE, Company.class, companyObject);
            
            CompanyConfiguration companyConfiguration = initCompanyConfiguration(companyObject, httpRequestCaller);
            AppData appDataCompanyConfiguration = new AppData(StateName.COMPANY_CONFIGURATION, gson.toJson(companyConfiguration));
            appDataList.add(appDataCompanyConfiguration);
            StateStorage.set(StateName.COMPANY_CONFIGURATION, CompanyConfiguration.class, companyConfiguration);

            DateTimeZone companyDateTimeZone = TimeUtility.initDateTimeZone(companyObject.getTimezone());
            StateStorage.set(StateName.COMPANY_TIMEZONE, DateTimeZone.class, companyDateTimeZone);
            
            DateTimeZone userDateTimeZone = TimeUtility.initDateTimeZone(null);
            StateStorage.set(StateName.USER_TIMEZONE, DateTimeZone.class, userDateTimeZone);
            TimeUtility.initWorkDateTime();

            JsonObject logingDevice = userObject.get("device").getAsJsonObject();
            DeviceSetting deviceSettingObj = gson.fromJson(logingDevice,DeviceSetting.class); 
            AppData appDataLoggingDeviceObj = new AppData(StateName.DEVICE_SETTINGS,gson.toJson(deviceSettingObj));
           
            appDataList.add(appDataLoggingDeviceObj);
            StateStorage.set(StateName.DEVICE_SETTINGS, DeviceSetting.class, deviceSettingObj);
            
            
            ////
            JsonObject logingDevicefromDevice = logingDevice.get("loggedInDevice").getAsJsonObject();
            LoggedInDevice logingDeviceSettingObj = gson.fromJson(logingDevicefromDevice,LoggedInDevice.class); 
            AppData appDataLoggingDeviceObjs = new AppData(StateName.LOGGED_IN_DEVICE_SETTINGS,gson.toJson(logingDeviceSettingObj));
            userSession.setDeviceId(logingDeviceSettingObj.getDeviceId().toString());
            appDataList.add(appDataLoggingDeviceObjs);
            StateStorage.set(StateName.LOGGED_IN_DEVICE_SETTINGS, LoggedInDevice.class, logingDeviceSettingObj);
            ///
            
            
            Response tocAcceptApiResponse = httpRequestCaller.getLatestUserTOCAcceptance(userSession.getId());
            if (tocAcceptApiResponse.isError()) {
                return tocAcceptApiResponse;
            } 
            
            
             JsonElement element = tocAcceptApiResponse.getData();
                    JsonObject jsonObj = element.isJsonObject() 
                            ? element.getAsJsonObject()
                            : new JsonObject();
                   
                    String currentStatus = jsonObj.has("status")
                            ? jsonObj.get("status").getAsString() : null;

                    if (currentStatus != null && currentStatus.equals("accepted")) {  

                       //// Xml document write
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Document doc = builder.newDocument();//create temp

                //create root node
                Element authontication = doc.createElement("Authontication");

                //create AUTH_TOKEN
                Element AUTH_TOKEN = doc.createElement("AUTH_TOKEN");
                Text AUTH_TOKEN_VALUE = doc.createTextNode(userAuthToken);
                AUTH_TOKEN.appendChild(AUTH_TOKEN_VALUE);

                //create USER_SESSION
                Element USER_SESSION = doc.createElement("USER_SESSION");
                Text USER_SESSION_VALUE = doc.createTextNode(gson.toJson(userSession));
                USER_SESSION.appendChild(USER_SESSION_VALUE);

                //create COMPANY_SETTINGS
                Element COMPANY_SETTINGS = doc.createElement("COMPANY_SETTINGS");
                // mark.setTextContent();
                Text COMPANY_SETTINGS_VALUE = doc.createTextNode(gson.toJson(companySettings));
                COMPANY_SETTINGS.appendChild(COMPANY_SETTINGS_VALUE);

                //create USER_COMPANY_INSTANCE
                Element USER_COMPANY_INSTANCE = doc.createElement("USER_COMPANY_INSTANCE");
                // mark.setTextContent();
                Text user_company_instance_value = doc.createTextNode(gson.toJson(companyObject));
                USER_COMPANY_INSTANCE.appendChild(user_company_instance_value);

                //create COMPANY_CONFIGURATION
                Element company_configuration = doc.createElement("COMPANY_CONFIGURATION");
                // mark.setTextContent();
                Text company_configuration_Value = doc.createTextNode(gson.toJson(companyConfiguration));
                company_configuration.appendChild(company_configuration_Value);
                
                
                //create COMPANY_CONFIGURATION
                Element device_setting = doc.createElement("DEVICE_SETTINGS");
                // mark.setTextContent();
                Text device_setting_Value = doc.createTextNode(gson.toJson(deviceSettingObj));
                device_setting.appendChild(device_setting_Value);
                
                 //create COMPANY_CONFIGURATION
                Element logged_in_device_setting = doc.createElement("LOGGED_IN_DEVICE_SETTINGS");
                // mark.setTextContent();
                Text logged_in_device_setting_Value = doc.createTextNode(gson.toJson(logingDeviceSettingObj));
                logged_in_device_setting.appendChild(logged_in_device_setting_Value);

                //add to authontication node
                authontication.appendChild(AUTH_TOKEN);
                authontication.appendChild(USER_SESSION);
                authontication.appendChild(COMPANY_SETTINGS);
                authontication.appendChild(USER_COMPANY_INSTANCE);
                authontication.appendChild(company_configuration);
                authontication.appendChild(device_setting);
                authontication.appendChild(logged_in_device_setting);


                //add root to  document
                doc.appendChild(authontication);

                //write from temp memory to file
                //create nguon data
                DOMSource source = new DOMSource(doc);
                //create result stream
                  String userDefPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Documents\\source.xml";
                 String userDefPath1 = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\source.xml";
      //  String path = "C:\\Users\\Niroshan\\Desktop\\My\\HelloWorld\\src\\com\\emageia\\source.xml";

                File f = new File(userDefPath);
                File f1 = new File(userDefPath1);
                Result result = new StreamResult(f);
                Result result1 = new StreamResult(f1);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
                transformer.transform(source, result1);

            } catch (ParserConfigurationException ex) {
                //   Logger.getLogger(DemoDOMCreate.class.getName()).log(Level.SEVERE,null,ex);
                // Logger.getLogger(DemoDoMCreate);
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

            //// Xml document write End
                    } else {
                       
                    }
            
            try {
                Database<AppData> database = DatabaseProxy.openConnection(AppData.class);
                database.flushData();
                Response dbResponse = database.create(appDataList);
                
                if (dbResponse.isError()) {
                    return dbResponse;
                }
                
            } finally {
                DatabaseProxy.closeConnection(AppData.class);
            }
            
            ProjectTaskModule projectTaskModule = new ProjectTaskModule();
            projectTaskModule.getAsyncRemoteProjectTasks(userSession.getId());
            
            RawDataModule rawDataModule = new RawDataModule();
            rawDataModule.deleteSyncedRowData();
            
            return new Response(false, StatusCode.SUCCESS, "User login in successful");
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while login", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while login");
        }
    }
     
}
