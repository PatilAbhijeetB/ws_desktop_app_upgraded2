/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.mainwindow.meeting;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;

/**
 *
 * @author jade_m
 */
public class ParticipantListItemModel implements ViewModel {
    
    private final SimpleStringProperty participantName;
    private final SimpleBooleanProperty itemSelected;
    
    private MeetingInfoModalViewModel parentViewModel;
    
    public ParticipantListItemModel() {    
        participantName = new SimpleStringProperty();
        itemSelected = new SimpleBooleanProperty();
    }

    public SimpleStringProperty getParticipantName() {
        return participantName;
    }
    
    public void setParticipantName(String participantName) {
        this.participantName.set(participantName);
        this.itemSelected.set(true);
    }

    public SimpleBooleanProperty getItemSelected() {
        return itemSelected;
    }

    public void setParentViewModel(MeetingInfoModalViewModel parentViewModel) {
        this.parentViewModel = parentViewModel;
    }
    
    public void onClickOnRadioBtn(ActionEvent event) {
        if (parentViewModel == null) {
            return;
        }
        parentViewModel.removeParticipantTagItem(this);
    }
    
    
    
}
