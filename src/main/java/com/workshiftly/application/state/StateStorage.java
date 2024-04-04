/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.application.state;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author chamara
 */
public final class StateStorage {
    
    private static final Map<String, AppState> stateMapper = new HashMap<>();
    
    private StateStorage() {}
    
    public static <T> void set(String stateName, Class<T> typeClass, T newValue) throws Exception {
        
        boolean containStateInMapper = stateMapper.containsKey(stateName);
        
        if (containStateInMapper) {
            AppState<T> appState = stateMapper.get(stateName);
            synchronized (appState) {
                appState.setState(newValue);
            }
            return;
        }
            
        AppState<T> appState = new AppState<>(stateName, typeClass, newValue);
        stateMapper.put(stateName, appState);
    }
    
    private static <T> AppState<T> getState(String stateName) {
        AppState<T> storedState = stateMapper.get(stateName);
        
        if (storedState == null) {
            return null;
        }
        
        AppState<T> readableState;
        try {
            readableState = (AppState<T>) storedState.clone(); 
        } catch (CloneNotSupportedException ex) {
            readableState = new AppState<>(storedState.getStateName(), storedState.getStateTypeClass());
            readableState.setCurrentState(storedState.getCurrentState());
            readableState.setPreviouseState(storedState.getPreviouseState()); 
        }
        
        return readableState;
    }
    
    public static <T> T getCurrentState(String stateName) {
        AppState<T> appState = getState(stateName);
        T currentState = appState != null ? appState.getCurrentState() : null;
        return currentState;
    }
    
    public static <T> T getPreviousState(String stateName) {
        AppState<T> appState = getState(stateName);
        T previouseState = appState != null ? appState.getPreviouseState() : null;
        return previouseState;
    }
    
    public static boolean isPersistedState(String stateName) {
        return stateMapper.containsKey(stateName);
    }
}
