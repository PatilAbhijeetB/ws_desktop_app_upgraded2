/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.application.state;


public final class AppState<T> implements Cloneable {
    
    private Class<T> stateTypeClass;
    private String stateName;
    private T currentState;
    private T previouseState;
    
    private AppState() {};
    
    AppState(String stateName, Class<T> stateTypeClass) {
        this.stateTypeClass = stateTypeClass;
        this.stateName = stateName;
    }
    
    AppState(String stateName, Class<T> stateTypeClass, T newState) {
        
        this.stateName = stateName;
        this.currentState = newState;
        this.previouseState = null;
    }
    
    T getCurrentState() {
        return this.currentState;
    }

    Class<T> getStateTypeClass() {
        return stateTypeClass;
    }

    String getStateName() {
        return stateName;
    }

    T getPreviouseState() {
        return previouseState;
    }

    void setCurrentState(T currentState) {
        this.currentState = currentState;
    }

    void setPreviouseState(T previouseState) {
        this.previouseState = previouseState;
    }
    
    
    synchronized void setState(T newState) {
        this.previouseState = this.currentState;
        this.currentState = newState;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
