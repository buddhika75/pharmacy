/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.entity.PatientEncounter;
import com.divudi.entity.WebUser;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.RoomFacilityCharge;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.RoomFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author safrin
 */
@Stateless
public class InwardBean {

    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private RoomFacade roomFacade;
    @EJB
    private InwardCalculation inwardCalculation;

    public void savePatientRoom(RoomFacilityCharge newRoomFacilityCharge, double addLinenCharge, Date addmittedAt, PatientEncounter patientEncounter, WebUser webUser) {
        PatientRoom pr = new PatientRoom();

        pr.setCurrentLinenCharge(newRoomFacilityCharge.getLinenCharge());
        pr.setCurrentMaintananceCharge(newRoomFacilityCharge.getMaintananceCharge());
        pr.setCurrentMoCharge(newRoomFacilityCharge.getMoCharge());
        pr.setCurrentNursingCharge(newRoomFacilityCharge.getNursingCharge());
        pr.setCurrentRoomCharge(newRoomFacilityCharge.getRoomCharge());

        pr.setAddedLinenCharge(addLinenCharge);
        pr.setAdmittedAt(addmittedAt);

        PatientRoom currentPatientRoom = getInwardCalculation().getCurrentPatientRoom(patientEncounter);

        pr.setPreviousRoom(currentPatientRoom);
        pr.setCreatedAt(Calendar.getInstance().getTime());
        pr.setCreater(webUser);
        pr.setAddmittedBy(webUser);
        pr.setPatientEncounter(patientEncounter);
        pr.setRoomFacilityCharge(newRoomFacilityCharge);
        pr.setRoom(newRoomFacilityCharge.getRoom());

        getPatientRoomFacade().create(pr);

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(pr);
        }
    }

    public void savePatientRoom(PatientRoom patientRoom, PatientEncounter patientEncounter, Date admittedAt, WebUser webUser) {
        patientRoom.setCurrentLinenCharge(patientRoom.getRoomFacilityCharge().getLinenCharge());
        patientRoom.setCurrentMaintananceCharge(patientRoom.getRoomFacilityCharge().getMaintananceCharge());
        patientRoom.setCurrentMoCharge(patientRoom.getRoomFacilityCharge().getMoCharge());
        patientRoom.setCurrentNursingCharge(patientRoom.getRoomFacilityCharge().getNursingCharge());
        patientRoom.setCurrentRoomCharge(patientRoom.getRoomFacilityCharge().getRoomCharge());

        patientRoom.setAddmittedBy(webUser);
        patientRoom.setAdmittedAt(admittedAt);
        patientRoom.setCreatedAt(Calendar.getInstance().getTime());
        patientRoom.setCreater(webUser);
        patientRoom.setPatientEncounter(patientEncounter);
        patientRoom.setRoom(patientRoom.getRoomFacilityCharge().getRoom());

        if (patientRoom.getId() == null || patientRoom.getId() == 0) {
            getPatientRoomFacade().create(patientRoom);
        } else {
            getPatientRoomFacade().edit(patientRoom);
        }

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(patientRoom);
        }
    }

    public void makeRoomFilled(PatientRoom pr) {

        pr.getRoom().setFilled(true);
        getRoomFacade().edit(pr.getRoom());

    }

    public void makeRoomVacant(PatientEncounter patientEncounter) {
        if (!patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            return;
        }

        PatientRoom pr = getInwardCalculation().getCurrentPatientRoom(patientEncounter);
        pr.getRoom().setFilled(false);
        getRoomFacade().edit(pr.getRoom());
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public RoomFacade getRoomFacade() {
        return roomFacade;
    }

    public void setRoomFacade(RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }
}
