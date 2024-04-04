/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers;

import com.workshiftly.common.model.WebBrowserLog;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chanakya
 */
public abstract class UrlCaptureBrowserBaseHandler {

    private static final InternalLogger LOGGER = LoggerService.getLogger(UrlCaptureBrowserBaseHandler.class);
    private Connection conn;
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    private Connection connect(String url) {
        // SQLite connection string
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(UrlCaptureBrowserBaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (conn == null) {
            Logger.getLogger(UrlCaptureBrowserBaseHandler.class.getName()).log(Level.SEVERE, "Temp Browser DB SQLite Connection failed");
        }
        return conn;
    }

    protected static void dbDuplicate(String sourcestr, String targetstr) throws IOException, NullPointerException, NoSuchFileException {
        Path source = Paths.get(sourcestr);
        Path target = Paths.get(targetstr);
        Files.copy(source, target, REPLACE_EXISTING);
    }

    protected boolean isDirExists(String dirPathstr) {
        Path dirPath = Paths.get(dirPathstr);
        return dirPath != null && Files.exists(dirPath) && Files.isDirectory(dirPath);
    }

    protected ResultSet getDataFromDB(String BrowserDBURL, String Quary) throws SQLException {
        this.conn = this.connect("jdbc:sqlite:/" + BrowserDBURL);
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(Quary);
        } catch (SQLException ex) {
            Logger.getLogger(UrlCaptureBrowserBaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return rs;
        }
    }

    protected void closeDBConnection() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    protected String getDomainName(String url) throws URISyntaxException {
        url = url.startsWith("http://") || url.startsWith("https://") ? url : "http://" + url;
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain : "www." + domain;
    }

    public abstract WebBrowserLog getURL();

    public abstract boolean isBrowserFileExist();
}
