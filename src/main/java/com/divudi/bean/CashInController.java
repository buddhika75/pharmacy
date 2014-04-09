/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CashTransaction;
import com.divudi.entity.Drawer;
import com.divudi.facade.BillFacade;
import com.divudi.facade.CashTransactionFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class CashInController implements Serializable {

    private boolean printPreview;
    Bill bill;
    private Bill referenceBill;
    @EJB
    private CashTransactionBean cashTransactionBean;
    @EJB
    CashTransactionFacade cashTransactionFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @Inject
    private SessionController sessionController;
    private Drawer drawer;
    private boolean disableInput;

    public CashTransactionFacade getCashTransactionFacade() {
        return cashTransactionFacade;
    }

    public void setCashTransactionFacade(CashTransactionFacade cashTransactionFacade) {
        this.cashTransactionFacade = cashTransactionFacade;
    }

    private boolean errorCheck() {

        return false;
    }

    public Bill getBill() {
        if (bill == null) {
            bill = new BilledBill();
            bill.setCashTransaction(new CashTransaction());
        }

        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    private void saveBill() {
        double netTotal = Math.abs(getBill().getCashTransaction().getCashValue())
                + Math.abs(getBill().getCashTransaction().getChequeValue())
                + Math.abs(getBill().getCashTransaction().getCreditCardValue())
                + Math.abs(getBill().getCashTransaction().getSlipValue());

        getBill().setNetTotal(netTotal);
        getBill().setBillType(BillType.CashIn);
        getBill().setCreatedAt(new Date());
        getBill().setCreater(getSessionController().getLoggedUser());
        getBill().setDepartment(getSessionController().getDepartment());
        getBill().setInstitution(getSessionController().getInstitution());
        getBill().setToWebUser(getSessionController().getLoggedUser());
        getBill().setBackwardReferenceBill(getReferenceBill());

        getBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getBill(), getBill().getBillType(), BillNumberSuffix.CSIN));
        getBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getBill(), getBill().getBillType(), BillNumberSuffix.CSIN));

        getBillFacade().create(getBill());

    }

    public void settle() {
        if (errorCheck()) {
            return;
        }

        CashTransaction ct = getBill().getCashTransaction();
        getBill().setCashTransaction(null);
        saveBill();

        getCashTransactionBean().saveCashInTransaction(ct, getBill(), getSessionController().getLoggedUser());

        getBill().setCashTransaction(ct);
        getBillFacade().edit(getBill());

        if (getReferenceBill() != null) {
            getReferenceBill().getForwardReferenceBills().add(getBill());
            getBillFacade().edit(getReferenceBill());

        }

//        if (getBill().getFromWebUser() != null) {
//            getCashTransactionBean().deductFromBallance(getBill().getFromWebUser().getDrawer(), dbl, ct);
//        }
        getCashTransactionBean().addToBallance(getSessionController().getLoggedUser().getDrawer(), ct);

        UtilityController.addSuccessMessage("Succesfully Cash Inned ");
        printPreview = true;

    }

    public void makeNull() {
        printPreview = false;
        bill = null;
        drawer = null;
    }

    public void calTotal() {
        double dbl = getCashTransactionBean().calTotal(getBill().getCashTransaction());

        getBill().getCashTransaction().setCashValue(dbl);
    }

    /**
     * Creates a new instance of BulkCashierController
     */
    public CashInController() {
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public boolean getPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public Drawer getDrawer() {
        if (drawer == null) {
            drawer = getCashTransactionBean().getDrawer(getSessionController().getLoggedUser());
        }
        return drawer;
    }

    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    public boolean isDisableInput() {
        return disableInput;
    }

    public void setDisableInput(boolean disableInput) {
        this.disableInput = disableInput;
    }

    public Bill getReferenceBill() {
        return referenceBill;
    }

    public void setReferenceBill(Bill referenceBill) {
        this.referenceBill = referenceBill;
        makeNull();
        getBill().getCashTransaction().copyQty(referenceBill.getCashTransaction());
        getBill().setFromWebUser(referenceBill.getFromWebUser());
        getBill().setNetTotal(referenceBill.getNetTotal());
    }

}
