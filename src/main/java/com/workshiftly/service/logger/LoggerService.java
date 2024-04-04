/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.service.logger;

import com.workshiftly.common.model.UserSession;
import io.sentry.Sentry;
import io.sentry.event.User;
import io.sentry.event.UserBuilder;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author chamara
 */
public class LoggerService {
    
    public static InternalLogger getLogger(Class loggerClass) {
        return new InternalLogger(loggerClass);
    }
    
    public static InternalLogger getLogger(Class loggerClass, InternalLogger.LOGGER_LEVEL level) {
        return new InternalLogger(loggerClass, level);
    }
    
    public final static void initializeUser(UserSession userSession) {
        
        UserBuilder userBuilder = new UserBuilder();
        
        try {
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ipAddress = socket.getLocalAddress().getHostAddress();
            
            userBuilder.setIpAddress(ipAddress);
        } catch (SocketException | UnknownHostException ex) {}
        
        if (userSession != null) {
            userBuilder.setUsername(userSession.getEmail());
            userBuilder.setId(userSession.getId());
            
            String firstName = userSession.getFirstName() != null ? userSession.getFirstName() : "";
            String lastName = userSession.getLastName() != null ? userSession.getLastName() : "";
            String fullName = firstName + (firstName != null ? " " : "") + lastName;
            
            if (!fullName.isEmpty()) {
                userBuilder.withData("full_name", fullName);
            }
            
            userBuilder.withData("companyId", userSession.getCompanyId());
        }
        
        User sentryUserObj = userBuilder.build();
        Sentry.getContext().setUser(sentryUserObj);
    }
    
    public static <T> void LogRecord(
            Class<T> loggerClass, 
            String message, 
            InternalLogger.LOGGER_LEVEL logLevel, 
            Throwable throwable
    ) {
        InternalLogger loggerObject = getLogger(loggerClass, logLevel);
        loggerObject.logRecord(logLevel, message, throwable);
    }
}
