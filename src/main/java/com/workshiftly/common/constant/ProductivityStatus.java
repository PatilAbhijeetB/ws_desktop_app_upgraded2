/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.constant;

/**
 *
 * @author Chanakya
 */
public enum ProductivityStatus {
    
    PRODUCTIVE("Productive"),
    UNPRODUCTIVE("Unproductive"),
    NEUTRAL("Neutral"),
    UNRATED("Unrated");
    
    String simpleName;
    
    private ProductivityStatus(String name) {
        simpleName = name;
    }
}