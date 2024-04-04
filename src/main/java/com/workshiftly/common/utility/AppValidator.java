/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.EmailValidator;

/**
 *
 * @author chamara
 */
public final class AppValidator {
    private static final InternalLogger LOGGER = LoggerService.getLogger(AppValidator.class);

    public Response validateLoginDetails(JsonObject loginDetails) {
        
        JsonElement userNameField = loginDetails.get("username");
        JsonElement passwordField = loginDetails.get("password");
        
        if (userNameField == null) {
            return new Response(true, StatusCode.VALIDATION_ERROR, "username field has not set in login object");
        }
        
        if (passwordField == null) {
            return new Response(true, StatusCode.VALIDATION_ERROR, "password field has not set in login object");
        }
        
        String username, password = null;
        try {
            username = userNameField.getAsString();
            password = userNameField.getAsString();
        } catch (ClassCastException ex) {
            String message = "Either username or password value is not valid type of String";
            return new Response(true, StatusCode.VALIDATION_ERROR, message);
        } catch (IllegalStateException ex) {
            String message = "Either username or password value  is not in legal state to parse";
            return new Response(true, StatusCode.VALIDATION_ERROR, message);
        }
        
        if (username != null && username.isBlank()) {
            return new Response(true, StatusCode.VALIDATION_ERROR,  "username should not be empty");
        }
        
        boolean isValidEmail = validateEmailPattern(username);
        if (!isValidEmail) {
            return new Response(true, StatusCode.VALIDATION_ERROR, "Email should be a val");
        }
        
        if (password == null && password.isBlank()) {
            return new Response(true, StatusCode.VALIDATION_ERROR,  "username should not be empty");
        }
        
        return new Response(false, StatusCode.SUCCESS, "login details are valid");
    }
    
    public boolean validateEmailPattern(String input) {
        
        EmailValidator emailValidator = EmailValidator.getInstance(true);
        return emailValidator.isValid(input);
    }
    
    /**
     * Method Name: validateChangePasswordDetails
     * 
     * Description: validate forget password functionalities user inputs data passed from
     * presentation layer to domain layer function
     * 
     * Valid Input format example: 
     *  inputData class type: com.google.gson.JsonObject;
     * 
     *  inputData =  {
     *      "currentPassword": "Zxer!3skd4^",
     *      "newPassword": "Abcd@1234",
     *      "confirmPassword": "Abcd@1234"
     * }
     * 
     * @param inputData
     * @return 
     */
    public Response validateChangePasswordDetails(JsonObject inputData) {
        
        Map<String, JsonElement> fieldMap = new HashMap<>();
        fieldMap.put("currentPassword", inputData.get("currentPassword"));
        fieldMap.put("newPassword", inputData.get("password"));
        fieldMap.put("confirmPassword", inputData.get("confirmPassword"));
        
        JsonObject validationResult = new JsonObject();
        
        for (Entry<String, JsonElement> entry : fieldMap.entrySet()) {
            String entryName = entry.getKey(); JsonElement entryValue = entry.getValue();
            
            if (entryValue.isJsonNull()) {
                validationResult.addProperty(entryName, "is a null value or not exist in validation input data");
                return new Response(true, StatusCode.VALIDATION_ERROR, null, validationResult);
            }
            
            if (!entryValue.isJsonPrimitive()) {
                validationResult.addProperty(entryName, "not a valid json primitive");
                return new Response(true, StatusCode.VALIDATION_ERROR, null, validationResult);
            }
            
            try {
                String parsedValue = entryValue.getAsString();
                
                if (parsedValue.isBlank()) {
                    validationResult.addProperty(entryName, "should not be empty or blank value");
                    return new Response(true, StatusCode.VALIDATION_ERROR, null, validationResult);
                }
            } catch (ClassCastException ex) {
                validationResult.addProperty(entryName, "should be string value");
                return new Response(true, StatusCode.VALIDATION_ERROR, null, validationResult);
            } 
        }
        
        return new Response(false, StatusCode.SUCCESS, null);
    }
    
    public Response validateForgetPasswordInputData(JsonObject inputData) {
        
        try {
            JsonElement emailElement = inputData.get("email");
            if (emailElement.isJsonNull()) {
                return new Response(true, StatusCode.VALIDATION_ERROR, "Email should not be a null value");
            }
            
            String emailValue = emailElement.getAsString();
            if (emailValue.isEmpty() || emailValue.isBlank()) {
                return new Response(true, StatusCode.VALIDATION_ERROR, "Email should not be an empty or a blank value");
            }
            
            boolean isValidEmail = validateEmailPattern(emailValue);
            if (!isValidEmail) {
                return new Response(true, StatusCode.VALIDATION_ERROR, "Invalid email address");
            }
            return new Response(false, StatusCode.VALIDATION_ERROR, null);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while validating forget password input data", ex);
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occurred while validating forget password input data"
            );
        }
    }

    public static boolean isNullOrEmptyOrBlank (String string) {
        return string == null || string.isEmpty() || string.isBlank();
    }
    
    public static void validateUserSession(UserSession userSession) throws AuthenticationException {
        
        if (userSession == null) {
            throw new AuthenticationException("UserSession is null");
        }

        String userId = userSession.getId();
        if (userId == null || userId.isEmpty() || userId.isBlank()) {
            throw new AuthenticationException("UserId should not be null, empty or blank. Authentication is failed");
        }
    }

    public Response validatePassword(String input) {
        List<String> errorList = new ArrayList<>();

        if (input.length() < 8) {
            errorList.add("8 characters");
        }

        if (!containsLowerCase(input)) {
            errorList.add("one lowercase");
        }

        if (!containsUpperCase(input)) {
            errorList.add("one uppercase");
        }

        if (!containsSpecialCharacter(input)) {
            errorList.add("one special character");
        }

        if (!containsNumber(input)) {
            errorList.add("one digit");
        }

        String errorString;
        if (!errorList.isEmpty() && errorList.size() > 1) {
            String lastError = errorList.get(errorList.size() - 1);
            errorList.remove(errorList.size() - 1);
            errorString = String.join(", ", errorList);
            errorString = errorString + " and " + lastError;
        } else if (!errorList.isEmpty()) {
            errorString = errorList.get(0);
        } else {
            return new Response(false, StatusCode.SUCCESS, "Given password is a valid password");
        }

        errorString = "At least " + errorString;
        return new Response(true, StatusCode.BAD_REQUEST, errorString);
    }

    public static boolean containsLowerCase(String value) {
        return contains(value, i -> Character.isLetter(i) && Character.isLowerCase(i));
    }

    public static boolean containsUpperCase(String value) {
        return contains(value, i -> Character.isLetter(i) && Character.isUpperCase(i));
    }

    public static boolean containsNumber(String value) {
        return contains(value, Character::isDigit);
    }

    public static boolean contains(String value, IntPredicate predicate) {
        return value.chars().anyMatch(predicate);
    }

    public static boolean containsSpecialCharacter(String value) {
        return Pattern
                .compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
                .matcher(value).find();
    }
}
