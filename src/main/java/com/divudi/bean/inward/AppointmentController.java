    /*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.inward.Admission;
import com.divudi.entity.Patient;
import com.divudi.entity.Person;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.facade.AdmissionFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.RoomFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.TemporalType;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class AppointmentController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    ////////////
    @EJB
    private AdmissionFacade ejbFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private RoomFacade roomFacade;
    ////////////////////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private InwardCalculation inwardCalculation;    
    ///////////////////////
    List<Admission> selectedItems;
    private Admission current;
    private PatientRoom patientRoom;
    private List<Admission> items = null;
    private List<Patient> patientList;
    ///////////////////////////
    String selectText = "";
    private String ageText = "";
    private String bhtText = "";
    private String patientTabId = "tabNewPt";
    private Patient newPatient;
    private YearMonthDay yearMonthDay;
    private Bill currentBill;
    
    
    
    

    public void dateChangeListen() {
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(yearMonthDay));

    }

    public PaymentMethod[] getPaymentMethods() {
        PaymentMethod[] tmp = {PaymentMethod.Cash, PaymentMethod.Credit};
        return tmp;
    }

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public List<Admission> completePatientBht(String query) {
        List<Admission> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Admission>();
        } else {
            sql = "select c from Admission c where c.retired=false and c.discharged=false and (upper(c.bhtNo) like '%" + query.toUpperCase() + "%' or upper(c.patient.person.name) like '%" + query.toUpperCase() + "%') order by c.bhtNo";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public List<Admission> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from Admission c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

    }

    public List<Admission> completePatient(String query) {
        List<Admission> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Admission>();
        } else {
            sql = "select c from Admission c where c.retired=false and c.discharged=false and (upper(c.bhtNo) like '%" + query.toUpperCase() + "%' or upper(c.patient.person.name) like '%" + query.toUpperCase() + "%') order by c.bhtNo";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public List<Admission> completePatientCredit(String query) {
        List<Admission> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Admission c where c.retired=false and c.paymentMethod=:pm  and (upper(c.bhtNo) like '%" + query.toUpperCase() + "%' or upper(c.patient.person.name) like '%" + query.toUpperCase() + "%') order by c.bhtNo";
            hm.put("pm", PaymentMethod.Credit);
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, hm, TemporalType.TIME);
        }
        return suggestions;
    }

    public void prepareAdd() {
        current = new Admission();
    }

    public List<Admission> completeBht(String query) {
        List<Admission> suggestions;
        String sql;
        if (query == null || query.trim().equals("")) {
            suggestions = new ArrayList<Admission>();
        } else {
            sql = "select p from Admission p where p.retired=false and upper(p.bhtNo) like '%" + query.toUpperCase() + "%'";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        if (suggestions == null) {
            suggestions = new ArrayList<Admission>();
        }
        return suggestions;
    }

    public void delete() {

        if (getCurrent() != null) {
            getCurrent().setRetired(true);
            getCurrent().setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getCurrent().setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(getCurrent());
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        makeNull();
        getItems();
        current = null;
        getCurrent();
    }

    public void setSelectedItems(List<Admission> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    public void makeNull() {
        current = null;
        patientRoom = null;
        items = null;
        bhtText = "";
        ageText = null;
        patientList = null;
        patientTabId = "tabNewPt";
        selectText = "";
        selectedItems = null;
        newPatient = null;
        yearMonthDay=null;

    }

    public void discharge() {
        if (getCurrent().getId() == null || getCurrent().getId() == 0) {
            UtilityController.addSuccessMessage("No Patient Data Found");
        } else {
            getCurrent().setDischarged(Boolean.TRUE);
            getCurrent().setDateOfDischarge(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getEjbFacade().edit(current);
        }

    }

    private void savePatient() {
        getNewPatient().getPerson().setCreatedAt(Calendar.getInstance().getTime());
        getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
        getPersonFacade().create(getNewPatient().getPerson());

        getNewPatient().setCreatedAt(Calendar.getInstance().getTime());
        getNewPatient().setCreater(getSessionController().getLoggedUser());
        getPatientFacade().create(getNewPatient());
    }

    private void saveGuardian() {
        Person temG = getCurrent().getGuardian();
        temG.setCreatedAt(Calendar.getInstance().getTime());
        temG.setCreater(getSessionController().getLoggedUser());

        if (temG.getId() == null || temG.getId() == 0) {
            getPersonFacade().create(temG);
        } else {
            getPersonFacade().edit(temG);
        }

    }

//        private boolean checkBht() {
//            String sql = "SELECT a FROM Admission a Where a.retired=false";
//            List<Admission> lst = getFacade().findBySQL(sql);
//
//            for (Admission an : lst) {
//                if (an.getBhtNo()!=null && getBhtText()!=null && an.getBhtNo().trim().toUpperCase().equals(getBhtText().trim().toUpperCase())) {
//                    UtilityController.addErrorMessage("Bht Number Already Exist");
//                    return true;
//                }
//
//            }
//
//            return false;
//
//        }
    private boolean errorCheck() {

        if (getCurrent().getAdmissionType() == null) {
            UtilityController.addErrorMessage("Please select Admission Type");
            return true;
        }

        if (getCurrent().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Select Paymentmethod");
            return true;
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Credit) {
            if (getCurrent().getCreditCompany() == null) {
                UtilityController.addErrorMessage("Select Credit Company");
                return true;
            }
            if (getCurrent().getCreditLimit() == 0.0) {
                UtilityController.addErrorMessage("Set Credit Limit");
                return true;
            }
        }

        if (!getCurrent().getAdmissionType().isOneDay()) {
            if (getPatientRoom().getRoomFacilityCharge() == null) {
                UtilityController.addErrorMessage("Select Room");
                return true;
            }
            if (getPatientRoom().getRoomFacilityCharge().getRoom().isFilled()) {
                UtilityController.addErrorMessage("Select Empty Room");
                return true;
            }
        }

        if (getPatientTabId().toString().equals("tabNewPt")) {
            if ("".equals(getAgeText())) {
                UtilityController.addErrorMessage("Patient Age Should be Typed");
                return true;
            }
            if (getNewPatient().getPerson().getName() == null || getNewPatient().getPerson().getName().trim().equals("") || getNewPatient().getPerson().getSex() == null || getAgeText() == null) {
                UtilityController.addErrorMessage("Can not admit without Patient Name, Age or Sex.");
                return true;
            }
        }

        if (getPatientTabId().toString().trim().equals("tabSearchPt")) {
            if (getCurrent().getPatient() == null) {
                UtilityController.addErrorMessage("Select Patient");
                return true;
            }
        }

        return false;
    }

    public void saveSelected() {

        if (errorCheck()) {
            return;
        }

        if (getPatientTabId().toString().equals("tabNewPt")) {
            savePatient();
            getCurrent().setPatient(getNewPatient());
        }

        saveGuardian();
        getCurrent().setBhtNo(getBhtText());

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("savedOldSuccessfully");
        } else {
            current.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setCreater(getSessionController().getLoggedUser());
            //      getCurrent().setDateOfAdmission(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Patient Admitted Succesfully");
        }

        if (!getCurrent().getAdmissionType().isOneDay()) {
            savePatientRoom();
        }

        makeNull();
        getItems();
    }

    private void makeRoomFilled(PatientRoom pr) {

        pr.getRoom().setFilled(true);
        getRoomFacade().edit(pr.getRoom());

    }

    private void savePatientRoom() {
        getPatientRoom().setCurrentLinenCharge(getPatientRoom().getRoomFacilityCharge().getLinenCharge());
        getPatientRoom().setCurrentMaintananceCharge(getPatientRoom().getRoomFacilityCharge().getMaintananceCharge());
        getPatientRoom().setCurrentMoCharge(getPatientRoom().getRoomFacilityCharge().getMoCharge());
        getPatientRoom().setCurrentNursingCharge(getPatientRoom().getRoomFacilityCharge().getNursingCharge());
        //   getPatientRoom().setCurrentRoomCharge(getPatientRoom().getRoomFacilityCharge().getRoomCharge());

        getPatientRoom().setAddmittedBy(getSessionController().getLoggedUser());
        getPatientRoom().setAdmittedAt(getCurrent().getDateOfAdmission());
        getPatientRoom().setCreatedAt(Calendar.getInstance().getTime());
        getPatientRoom().setCreater(getSessionController().getLoggedUser());
        getPatientRoom().setPatientEncounter(getCurrent());
        getPatientRoom().setRoom(getPatientRoom().getRoomFacilityCharge().getRoom());

        if (getPatientRoom().getId() == null || getPatientRoom().getId() == 0) {
            getPatientRoomFacade().create(getPatientRoom());
        } else {
            getPatientRoomFacade().edit(getPatientRoom());
        }

        makeRoomFilled(getPatientRoom());
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public AdmissionFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(AdmissionFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public AppointmentController() {
    }

    public Admission getCurrent() {
        if (current == null) {
            current = new Admission();
            current.setDateOfAdmission(new Date());
        }
        return current;
    }

    public void setCurrent(Admission current) {
        this.current = current;
    }

    private AdmissionFacade getFacade() {
        return ejbFacade;
    }

    public List<Admission> getItems() {
        if (items == null) {
            String temSql;
            temSql = "SELECT i FROM Admission i where i.retired=false and i.discharged=false order by i.bhtNo";
            items = getFacade().findBySQL(temSql);
            if (items == null) {
                items = new ArrayList<Admission>();
            }
        }

        return items;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public void setPatientFacade(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    public List<Patient> getPatientList() {
        if (patientList == null) {
            String temSql;
            temSql = "SELECT i FROM Patient i where i.retired=false ";
            patientList = getPatientFacade().findBySQL(temSql);
        }
        return patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public PatientRoom getPatientRoom() {
        if (patientRoom == null) {
            patientRoom = new PatientRoom();
        }
        return patientRoom;
    }

    public void setPatientRoom(PatientRoom patientRoom) {
        this.patientRoom = patientRoom;
    }

    public String getAgeText() {
        ageText = getNewPatient().getAge();
        return ageText;
    }

    public void setAgeText(String ageText) {
        this.ageText = ageText;
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(ageText));
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public String getBhtText() {
        if (getCurrent().getAdmissionType() != null) {
            bhtText = getInwardCalculation().getBhtText(getCurrent().getAdmissionType());
        } else {
            bhtText = "";
        }
        return bhtText;
    }

    public void setBhtText(String bhtText) {
        this.bhtText = bhtText;
    }

    public RoomFacade getRoomFacade() {
        return roomFacade;
    }

    public void setRoomFacade(RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    public String getPatientTabId() {
        return patientTabId;
    }

    public void setPatientTabId(String patientTabId) {
        this.patientTabId = patientTabId;
    }

    public Patient getNewPatient() {
        if (newPatient == null) {
            Person p = new Person();
            newPatient = new Patient();
            newPatient.setPerson(p);
        }
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }  

    public Bill getCurrentBill() {
        return currentBill;
    }

    public void setCurrentBill(Bill currentBill) {
        this.currentBill = currentBill;
    }

    /**
     *
     */
    @FacesConverter("admCon")
    public static class AdmissionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AppointmentController controller = (AppointmentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "admissionController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Admission) {
                Admission o = (Admission) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + AppointmentController.class.getName());
            }
        }
    }
}