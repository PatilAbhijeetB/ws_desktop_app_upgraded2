/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;



import com.azure.core.credential.TokenRequestContext;
import com.workshiftly.presentation.viewmodel.LoginViewModelNew;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import io.sentry.event.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import com.azure.identity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IPublicClientApplication;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import json;



/*
 * @author chamara
 */
public final class LoginViewNew implements Initializable, FxmlView<LoginViewModelNew> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LoginViewNew.class);

    private static final String AUTH_SCRREN_IMAGE_PATH = "/images/Background.png";
    private static final String COMPANY_LOGO_PATH = "/images/logowork.png";
     private static final String EYE_LOGO_PATH = "/images/eye.png";
      private static final String EYEOPEN_LOGO_PATH = "/images/eyeOpen.png";
    private String actualPassword = "";
    
    @FXML
    private Pane loginWindoeRootPane;
  

    @FXML
    private Pane loginWindowSvgWrapper;

    @FXML
    private SVGPath footerSVGWave;

    @FXML
    private Pane authScImageWrapper;

    @FXML
    private ImageView authScreenImage;
    
    @FXML 
    private ImageView eyeLogo;
    
    @FXML
    private Button eyeButton;

    @FXML
    private Pane formContentWrapper;

    @FXML
    private ImageView authScreenAppLogo;

    @FXML
    private Label emailErrorTxt;

    @FXML
    private Label formTitle;
  

    @FXML
    private TextField emailTextInput;
    
    @FXML
    private TextField passwordshow;

    @FXML
    private Label emailTextInputLbl;

    @FXML
    private PasswordField passwordTextInout;

    @FXML
    private Label passwordErrorTxt;

    @FXML
    private Button loginFormButton;

    @FXML
    private Hyperlink forgetPasswordLink;

    @FXML
    private Hyperlink signupLink;

    private static final String CLIENT_ID = "your_client_id"; // Replace with your client ID
    private static final String TENANT_ID = "your_tenant_id"; // Replace with your tenant ID
    private static final String CLIENT_SECRET = "your_client_secret"; // Replace with your client secret
   // private static final String[] SCOPES = {"User.Read"};
    
    @InjectViewModel
    private LoginViewModelNew viewModel;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
       
         try{
        String tenantId = "7701d9e4-fc17-44a7-b421-77f3cac32795"; // Replace with actual Tenant ID
        String clientId = "afe25613-2a4d-4e47-9419-7dc9cddf77e6"; // Replace with actual Client ID
        String clientSecret = "8fn8Q~WwZZ6tdzr5eVwBAmNJOt1ruMOeV3UOOb2x"; // Replace with actual Client Secret

        //String accessToken = getAccessToken(tenantId, clientId, clientSecret);
       // if (accessToken != null) {
           // String userEmail = getUserEmail(accessToken);
      //       String userEmail = getEmailAddress(accessToken);
      //      System.out.println("Logged-in Microsoft account email: " + userEmail);
      //  } else {
      //      System.out.println("Failed to obtain access token.");
      //  }
        
        
         }catch(Exception es){
         
         }
        // set up left image
     //   InputStream inputStream = getClass().getResourceAsStream(COMPANY_LOGO_PATH);
      //  Image authScImage = new Image(inputStream);
     //   authScreenImage.setImage(authScImage);
        
        //set up company logo
        
       String userDefPath = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\source.xml";

       File xmlFile = new File(userDefPath);
        if(xmlFile.exists()){
         try {
             Boolean IsLogout= StateStorage.getCurrentState(StateName.IsLogout); 
             if(IsLogout!=null && IsLogout==true)
             {
             logininitiate();
             }
             else
             {
                       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Authontication");
           // List<LogAppData> list = new ArrayList<>();
              String authToken="";
              String userId="";
              String userName = "";
              String email = "";
              String deviceId = "";
              for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    authToken = element.getElementsByTagName("AUTH_TOKEN").item(0).getTextContent();
                    //list.add(insert("AUTH_TOKEN", authToken));

                    String userSession = element.getElementsByTagName("USER_SESSION").item(0).getTextContent();
                    JsonElement jsonElement = JsonParser.parseString(userSession);
                     if (jsonElement.isJsonObject()) {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();

                            userId = jsonObject.get("id").getAsString();
                            userName = jsonObject.get("firstName").getAsString();
                            email = jsonObject.get("email").getAsString();
                            deviceId = jsonObject.get("deviceId").getAsString();

                         } 
                     
                   
                  
                }
                
                
                
            }
              viewModel.onSkipLogin(userId, authToken, deviceId);
             }
             

              
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        }
        else{
        
        logininitiate();
        
       
        }
        
    }
    
    private void logininitiate(){
    
     InputStream companyLogoStream = getClass().getResourceAsStream(COMPANY_LOGO_PATH);
        Image companyLogo = new Image(companyLogoStream);
        authScreenAppLogo = new ImageView(companyLogo);
        
        InputStream eyeLogoStream = getClass().getResourceAsStream(EYE_LOGO_PATH);
        Image eLogo = new Image(eyeLogoStream);
        //eyeLogo = new ImageView(eLogo);
        
        InputStream eyeOpenLogoStream = getClass().getResourceAsStream(EYEOPEN_LOGO_PATH);
        Image eyeOpenLogo = new Image(eyeOpenLogoStream);
        
        
        passwordshow.setVisible(false);
       
        //view model bindings
        emailTextInput.textProperty().bindBidirectional(viewModel.emailTxtInput());
        emailErrorTxt.textProperty().bind(viewModel.emailErrorTxt());
        
        passwordTextInout.textProperty().bindBidirectional(viewModel.passwordFieldInput());
        passwordErrorTxt.textProperty().bind(viewModel.passwordErrorTxt());
        
        passwordshow.textProperty().bindBidirectional(viewModel.passwordShowFieldInput());
       
        
        loginFormButton.disableProperty().bind(viewModel.loginBtnDisability());
        
        emailTextInput.textProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.onChanageEmailTxtInput(newValue);
        });
        emailTextInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                viewModel.isEmailTxtInputDirty().set(true);
            }
        });
        
        passwordTextInout.textProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.onChangePasswordTxtInput();
        });
        passwordTextInout.focusedProperty().addListener((obsservale, oldValue, newValue) -> {
            if (!newValue) {
                viewModel.isPasswordlTxtInputDirty().set(true);
            }
           
            
        });
        
         passwordshow.textProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.onChangePasswordTxtInput();
        });
        passwordshow.focusedProperty().addListener((obsservale, oldValue, newValue) -> {
            if (!newValue) {
                viewModel.isPasswordlTxtInputDirty().set(true);
            }
        });
   
        
        loginFormButton.setOnAction((ActionEvent actionEvent) -> {
            viewModel.onSubmitLoginBtn();
        });
        
        forgetPasswordLink.setOnAction((ActionEvent actionEvent) -> {
            viewModel.onClickForgetPasswordHyperLink();
        });
       
        
        eyeButton.setOnAction(e -> {
            if (passwordshow.isVisible()) {
                passwordTextInout.setText(passwordshow.getText());
                passwordTextInout.setVisible(true);
                passwordshow.setVisible(false);
                eyeLogo.setImage(eyeOpenLogo);
                passwordErrorTxt.textProperty().bind(viewModel.passwordErrorTxt());
                
            } else {
                passwordshow.setText(passwordTextInout.getText());
                passwordshow.setVisible(true);
                passwordTextInout.setVisible(false);
                eyeLogo.setImage(eLogo);
                passwordErrorTxt.textProperty().bind(viewModel.passwordErrorTxt());
            }
        });
        
       

        // hide signup link due to implementations were not started
        signupLink.setVisible(false);
        
    }
    
   private static String getAccessToken(String tenantId, String clientId, String clientSecret) throws IOException {
       
       try{
       String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
        String requestBody = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret +
                             "&scope=https://graph.microsoft.com/.default";
        
        HttpURLConnection connection = (HttpURLConnection) new URL(tokenUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        connection.getOutputStream().write(requestBody.getBytes());
        
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        // Extract access token from JSON response
        String jsonResponse = response.toString();
        String accessToken = jsonResponse.contains("access_token") ? jsonResponse.split("\"access_token\"")[1].split("\"")[1] : null;
        return accessToken;
        }
       catch(Exception ed){
       return ed.getMessage().toString();
       }
    }

   
   private static final String GRAPH_API_ENDPOINT = "https://graph.microsoft.com/v1.0/users";
   // private static final String ACCESS_TOKEN = "YOUR_ACCESS_TOKEN"; // Replace with your access token
    
private static String getEmailAddress(String accessToken) throws IOException {
    
String UPN=getUPN(accessToken);
 CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(GRAPH_API_ENDPOINT);
    httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
   
    try {
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Status Code: " + statusCode);
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JSONObject objJson=new JSONObject(jsonNode.toString());
          // parsed_response = json.loads(response)
            String respValue = objJson.getJSONArray("value").toString();
            
            
            System.out.println("Response Body: " + responseBody);
             List<String> UserName =   filterUsers(respValue,UPN);
            // Parse the JSON response to extract the email address
            // Update this logic based on the actual JSON structure
            String email = responseBody.contains("mail") ? responseBody.split("\"mail\":")[1].split(",")[0].replace("\"", "") : "";
            //return email;
        } else {
            System.err.println("Error: " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
            // Print response body for debugging purposes
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseBody = EntityUtils.toString(entity);
               // System.err.println("Response Body: " + responseBody);
            
            }
            return null;
        }
    } finally {
        httpClient.close();
    }
        return null;

    
    
    

    
    
   
}
 private static String getUPN(String AccessTok) throws MalformedURLException{
     
   String upn="";
        try {
            JWT jwt = JWTParser.parse(AccessTok);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            upn = claimsSet.getSubject(); // Extract UPN from token's subject claim
            System.out.println("UPN: " + upn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return upn;
 }
   private static List<String> filterUsers(String response, String UPN) {
        List<String> loggedInUserInfo = new ArrayList<>();
       try{
       
        // Parse JSON response and filter users based on userPrincipalName
        // Implement your logic to parse the JSON response and filter users
        // For simplicity, let's assume the response is a JSON array of user objects
        // and we filter users based on their userPrincipalName
        // You may need to use a JSON parsing library like Jackson or Gson for proper parsing
        // Here's a simplified example assuming the response is in JSON format:
        //JSONArray usersArray = new JSONObject (response).getJSONArray("value");
         JSONArray usersArray = new JSONArray(response);
       //  for (int i = 0; i < usersArray.length(); i++) {
      //       JSONObject userObject = usersArray.getJSONObject(i);
      //       String UserID = userObject.getString("id");
      //       if (UserID.equals(UPN)) {
     //            loggedInUserInfo.add(userObject.toString());
     //        }
    //     }
           for (int i = 0; i < usersArray.length(); i++) {
        JSONObject userObject = usersArray.getJSONObject(i);
        String userId = userObject.getString("id");
        String userPrincipalName = userObject.getString("userPrincipalName");
        // Check if the identifier matches the user's ID or UPN
        if (UPN.equals(userId) || UPN.equals(userPrincipalName)) {
            //return userObject; // Return the matched user object
            loggedInUserInfo.add(userObject.toString());
            
        }
    }
       }catch (Exception ex){
       
       }
        return loggedInUserInfo;
    }
   
 }
