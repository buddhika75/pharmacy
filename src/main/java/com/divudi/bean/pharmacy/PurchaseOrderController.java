/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.LazyBill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PurchaseOrderController implements Serializable {

    @Inject
    private SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;
    ///////////////
    private Bill requestedBill;
    private Bill aprovedBill;
    private Date fromDate;
    Date toDate;
    private boolean printPreview;
    private String txtSearch;
    /////////////
//    private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    private List<PharmaceuticalBillItem> filteredValue;
    private List<BillItem> billItems;
    private List<BillItem> selectedItems;
    private List<Bill> billsToApprove;
    // private List<BillItem> billItems;
    // List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    //////////
    @EJB
    private CommonFunctions commonFunctions;
    private LazyDataModel<Bill> searchBills;

    public void removeSelected() {
        //  System.err.println("1");
        if (selectedItems == null) {
            //   System.err.println("2");
            return;
        }

        System.err.println("3");
        for (BillItem b : selectedItems) {
            //  System.err.println("4");
            getBillItems().remove(b.getSearialNo());
            calTotal();
        }

        selectedItems = null;
    }

    public void removeItem(BillItem billItem) {
        getBillItems().remove(billItem.getSearialNo());
        calTotal();
    }

    public void createAll() {
        searchBills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null  "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null  "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        searchBills = new LazyBill(lst);

    }

    public void createNotApproved() {
        searchBills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill is null  "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill is null "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst1 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is null "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is null "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        List<Bill> lst2 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is not null and b.referenceBill.cancelled=true "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is not null and b.referenceBill.cancelled=true "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        List<Bill> lst3 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        lst1.addAll(lst2);
        lst1.addAll(lst3);

        searchBills = new LazyBill(lst1);

    }

    public void createApproved() {
        searchBills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill.creater is not null and b.referenceBill.cancelled=false "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill.creater is not null and b.referenceBill.cancelled=false  "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        searchBills = new LazyBill(lst);

    }

    public void clearList() {
        filteredValue = null;
        billsToApprove = null;
        printPreview = false;
        billItems = null;
        aprovedBill = null;
        requestedBill = null;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public String approve() {
        if (getAprovedBill().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Select Paymentmethod");
            return "";
        }

        calTotal();

        saveBill();
        saveBillComponent();

        //Update Requested Bill Reference
        getRequestedBill().setReferenceBill(getAprovedBill());
        getBillFacade().edit(getRequestedBill());

        clearList();

        return viewRequestedList();
        //   printPreview = true;

    }

    public String viewRequestedList() {
        clearList();
        return "pharmacy_purhcase_order_list_to_approve";
    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(BillItem tmp) {
        tmp.setNetValue(tmp.getPharmaceuticalBillItem().getQty() * tmp.getPharmaceuticalBillItem().getPurchaseRate());
        calTotal();
    }

    public void onFocus(PharmaceuticalBillItem ph) {
        getPharmacyController().setPharmacyItem(ph.getBillItem().getItem());
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Bill getRequestedBill() {
        if (requestedBill == null) {
            requestedBill = new BilledBill();
        }
        return requestedBill;
    }

    public void saveBill() {

        getAprovedBill().setPaymentMethod(getRequestedBill().getPaymentMethod());
        getAprovedBill().setFromDepartment(getRequestedBill().getDepartment());
        getAprovedBill().setFromInstitution(getRequestedBill().getInstitution());
        getAprovedBill().setReferenceBill(getRequestedBill());
        getAprovedBill().setBackwardReferenceBill(getRequestedBill());

        getAprovedBill().setDeptId(getBillNumberBean().institutionBillNumberGeneratorWithReference(getRequestedBill().getDepartment(), getAprovedBill(), BillType.PharmacyOrder, BillNumberSuffix.PO));
        getAprovedBill().setInsId(getBillNumberBean().institutionBillNumberGeneratorWithReference(getRequestedBill().getInstitution(), getAprovedBill(), BillType.PharmacyOrder, BillNumberSuffix.PO));

        getAprovedBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getAprovedBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getAprovedBill().setCreater(getSessionController().getLoggedUser());
        getAprovedBill().setCreatedAt(Calendar.getInstance().getTime());

        getBillFacade().create(getAprovedBill());

    }

    public void saveBillComponent() {
        for (BillItem i : getBillItems()) {
            i.setBill(getAprovedBill());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setNetValue(i.getPharmaceuticalBillItem().getQty() * i.getPharmaceuticalBillItem().getPurchaseRate());

            PharmaceuticalBillItem phItem = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            getBillItemFacade().create(i);

            phItem.setBillItem(i);
            getPharmaceuticalBillItemFacade().create(phItem);

            i.setPharmaceuticalBillItem(phItem);
            getBillItemFacade().edit(i);

            getAprovedBill().getBillItems().add(i);
        }

        getBillFacade().edit(getAprovedBill());
    }

    public void generateBillComponent() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getRequestedBill())) {
            BillItem bi = new BillItem();
            bi.copy(i.getBillItem());

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.setBillItem(bi);
            ph.setQtyInUnit(i.getQtyInUnit());
            ph.setPurchaseRateInUnit(i.getPurchaseRateInUnit());
            ph.setRetailRateInUnit(i.getRetailRateInUnit());
            bi.setPharmaceuticalBillItem(ph);

            getBillItems().add(bi);
        }

        calTotal();

    }

    public void setRequestedBill(Bill requestedBill) {
        clearList();
        this.requestedBill = requestedBill;
        getAprovedBill().setPaymentMethod(getRequestedBill().getPaymentMethod());
        getAprovedBill().setToInstitution(getRequestedBill().getToInstitution());
        generateBillComponent();
    }

    public Bill getAprovedBill() {
        if (aprovedBill == null) {
            aprovedBill = new BilledBill();
            aprovedBill.setBillType(BillType.PharmacyOrderApprove);
        }
        return aprovedBill;
    }

    public void setAprovedBill(Bill aprovedBill) {
        this.aprovedBill = aprovedBill;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public void calTotal() {
        double tmp = 0;
        int serialNo = 0;
        for (BillItem bi : getBillItems()) {
            tmp += bi.getPharmaceuticalBillItem().getQty() * bi.getPharmaceuticalBillItem().getPurchaseRate();
            bi.setSearialNo(serialNo++);
        }
        getAprovedBill().setTotal(tmp);
        getAprovedBill().setNetTotal(tmp);
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public List<PharmaceuticalBillItem> getFilteredValue() {
        return filteredValue;
    }

    public void setFilteredValue(List<PharmaceuticalBillItem> filteredValue) {
        this.filteredValue = filteredValue;
    }

    public List<Bill> getBillsToApprove() {
        return billsToApprove;
    }

    public void setBillsToApprove(List<Bill> billsToApprove) {
        this.billsToApprove = billsToApprove;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public boolean getPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public void makeListNull() {
//        pharmaceuticalBillItems = null;
        filteredValue = null;
        billsToApprove = null;
        searchBills = null;
        billItems = null;

    }

    public LazyDataModel<Bill> getSearchBills() {
        return searchBills;
    }

    public void setSearchBills(LazyDataModel<Bill> searchBills) {
        this.searchBills = searchBills;
    }

    public String getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(String txtSearch) {
        this.txtSearch = txtSearch;
    }

    public List<BillItem> getBillItems() {
        if (billItems == null) {
            billItems = new ArrayList<>();
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public List<BillItem> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<BillItem> selectedItems) {
        this.selectedItems = selectedItems;
    }
}
