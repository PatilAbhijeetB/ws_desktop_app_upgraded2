/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.constant;

/**
 *
 * @author chamara
 */
public enum ColorCode {
    
    YELLOW("#ffff00"),
    VIVID_ORANGE("#f29f05"),
    SOFT_ORANGE("#fdd766"),
    VERY_LIGHT_GRAY("#e4e4e4"),
    VERY_DARK_GRAY("#3f3f3f");
    
    
    public String hexCode;
    
    private ColorCode(String hexCode) {
        this.hexCode = hexCode;
    }
    
}
