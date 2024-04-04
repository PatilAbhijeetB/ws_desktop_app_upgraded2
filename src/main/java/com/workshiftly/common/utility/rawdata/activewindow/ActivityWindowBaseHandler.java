/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.activewindow;

import com.workshiftly.common.model.ActiveWindow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author chamara
 */
abstract class ActivityWindowBaseHandler {

    public abstract ActiveWindow getCurrentActivityWindow();

    protected String getOutputFromCommandProcess(Process process) throws IOException {

        InputStreamReader errorReader = new InputStreamReader(process.getInputStream());
        BufferedReader outputReader = new BufferedReader(errorReader);
        StringBuilder stringBuilder = new StringBuilder();
        String currentLine;

        while ((currentLine = outputReader.readLine()) != null) {
            stringBuilder.append(currentLine);
        }

        return stringBuilder.toString();
    } 
}
