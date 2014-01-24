/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.data.BillType;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyErrorCheckingEjb;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.facade.BillFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;

/**
 *
 * @author ruhunu
 */
@Named
@SessionScoped
public class PharmacyErrorChecking implements Serializable {

    @EJB
    PharmacyErrorCheckingEjb ejb;

    List<BillItem> billItems;
    Date fromDate;
    Date toDate;
    Item item;
    List<Bill> mismatchPreBills;
    Department department;
    double calculatedStock;
    double calculatedSaleValue;
    double calculatedPurchaseValue;
    double currentStock;
    double currentSaleValue;
    double currentPurchaseValue;

    public void listMismatchPreBills() {
        mismatchPreBills = getEjb().errPreBills(department);
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public void listPharmacyMovement() {
        billItems = getEjb().allBillItems(item, department);

//        billItems = getEjb().allBillItems2(item, department);

        //  calculateTotals(getEjb().allBillItemsWithCreatedAt(item, department));
        calculateTotals2();
    }

    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmacyBean pharmacyBean;

    public double getItemStock() {
        return getPharmacyBean().getStockQty(item, department);
    }

    public void calculateTotals2() {
        calculatedStock = 0.0;
        calculatedSaleValue = 0.0;
        calculatedPurchaseValue = 0.0;
        currentStock = 0.0;
        currentSaleValue = 0.0;
        currentPurchaseValue = 0.0;

        calculatedStock += getEjb().getTotalQty(BillType.PharmacyGrnBill, new BilledBill(), department, item);
        calculatedStock -= getEjb().getTotalQty(BillType.PharmacyGrnBill, new CancelledBill(), department, item);

        calculatedStock -= getEjb().getTotalQty(BillType.PharmacyGrnReturn, new BilledBill(), department, item);
        calculatedStock += getEjb().getTotalQty(BillType.PharmacyGrnReturn, new CancelledBill(), department, item);

        calculatedStock += getEjb().getTotalQty(BillType.PharmacyPurchaseBill, new BilledBill(), department, item);
        calculatedStock -= getEjb().getTotalQty(BillType.PharmacyPurchaseBill, new CancelledBill(), department, item);

        calculatedStock -= getEjb().getTotalQty(BillType.PurchaseReturn, new BilledBill(), department, item);
        calculatedStock += getEjb().getTotalQty(BillType.PurchaseReturn, new CancelledBill(), department, item);
            
        calculatedStock -= getEjb().getTotalQty(BillType.PharmacyPre, new PreBill(), department, item);

        calculatedStock += getEjb().getTotalQty(BillType.PharmacyPre, new RefundBill(), department, item);

        calculatedStock -= getEjb().getTotalQty(BillType.PharmacyTransferIssue, new BilledBill(), department, item);
        calculatedStock += getEjb().getTotalQty(BillType.PharmacyTransferIssue, new CancelledBill(), department, item);

        calculatedStock += getEjb().getTotalQty(BillType.PharmacyTransferReceive, new BilledBill(), department, item);
        calculatedStock -= getEjb().getTotalQty(BillType.PharmacyTransferReceive, new CancelledBill(), department, item);

    }

    
    public void calculateTotals(List<BillItem> bis) {
        calculatedStock = 0.0;
        calculatedSaleValue = 0.0;
        calculatedPurchaseValue = 0.0;
        currentStock = 0.0;
        currentSaleValue = 0.0;
        currentPurchaseValue = 0.0;
        for (BillItem bi : bis) {

            if (bi.getBill() instanceof PreBill) {

                switch (bi.getBill().getBillType()) {
                    case PharmacySale:
                    case PharmacyIssue:
                    case PharmacyTransferIssue:
                        calculatedStock = calculatedStock - bi.getQty();
                        break;

                    case PharmacyTransferReceive:
                        calculatedStock = calculatedStock + bi.getQty();
                        break;

                }

            } else if (bi.getBill() instanceof CancelledBill || bi.getBill() instanceof RefundBill) {

                switch (bi.getBill().getBillType()) {
                    case PharmacySale:
                    case PharmacyIssue:
                    case PharmacyGrnReturn:
                    case PharmacyTransferIssue:
                        calculatedStock = calculatedStock + bi.getQty();
                        break;

                    case PharmacyGrnBill:
                    case PharmacyPurchaseBill:
                    case PharmacyTransferReceive:
                        calculatedStock = calculatedStock - bi.getQty();
                        break;

                }

            } else if (bi.getBill() instanceof BilledBill) {

                switch (bi.getBill().getBillType()) {
                    case PharmacyGrnReturn:
                        calculatedStock = calculatedStock - bi.getQty();
                        break;

                    case PharmacyGrnBill:
                    case PharmacyPurchaseBill:
                        calculatedStock = calculatedStock + bi.getQty();
                        break;

                }

            }

        }
    }

    /**
     * Creates a new instance of ItemMovementReportController
     */
    public PharmacyErrorChecking() {
    }

    public PharmacyErrorCheckingEjb getEjb() {
        return ejb;
    }

    public void setEjb(PharmacyErrorCheckingEjb ejb) {
        this.ejb = ejb;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Bill> getMismatchPreBills() {
        return mismatchPreBills;
    }

    public void setMismatchPreBills(List<Bill> mismatchPreBills) {
        this.mismatchPreBills = mismatchPreBills;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public double getCalculatedStock() {
        return calculatedStock;
    }

    public void setCalculatedStock(double calculatedStock) {
        this.calculatedStock = calculatedStock;
    }

    public double getCalculatedSaleValue() {
        return calculatedSaleValue;
    }

    public void setCalculatedSaleValue(double calculatedSaleValue) {
        this.calculatedSaleValue = calculatedSaleValue;
    }

    public double getCalculatedPurchaseValue() {
        return calculatedPurchaseValue;
    }

    public void setCalculatedPurchaseValue(double calculatedPurchaseValue) {
        this.calculatedPurchaseValue = calculatedPurchaseValue;
    }

    public double getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(double currentStock) {
        this.currentStock = currentStock;
    }

    public double getCurrentSaleValue() {
        return currentSaleValue;
    }

    public void setCurrentSaleValue(double currentSaleValue) {
        this.currentSaleValue = currentSaleValue;
    }

    public double getCurrentPurchaseValue() {
        return currentPurchaseValue;
    }

    public void setCurrentPurchaseValue(double currentPurchaseValue) {
        this.currentPurchaseValue = currentPurchaseValue;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

}
