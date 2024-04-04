/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.common.model;

/**
 *
 * @author jade_m
 */
public class ProjectTaskType {
    
    private final String key;
    private final String value;
    private boolean visibility;
    
    private ProjectTaskType() {
        this.key = "";
        this.value = "";
        this.visibility = false;
    }
    
    public ProjectTaskType(String key, String value, boolean visibility) {
        this.key = key;
        this.value = value;
        this.visibility = visibility;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
