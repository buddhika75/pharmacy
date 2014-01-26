/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import java.io.Serializable;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author buddhika
 */
@Entity
public class Bill implements Serializable {

    @ManyToOne
    BatchBill batchBill;

    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    ////////////////////////////////////////////////////
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Payment> payments = new ArrayList<>();
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<BillFee> billFees = new ArrayList<>();
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<BillItem> billItems;
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<BillComponent> billComponents = new ArrayList<>();

    @Transient
    List<BillItem> transBillItems;

    @Transient
    private List<BillItem> transActiveBillItem;
    @ManyToOne
    private Bill forwardReferenceBill;
    @ManyToOne
    private Bill backwardReferenceBill;

    public List<BillItem> getTransBillItems() {
        if (billItems != null) {
            transBillItems = new ArrayList<>();
            for (BillItem b : billItems) {
                if (!b.isRetired()) {
                    transBillItems.add(b);
                }
            }

            transBillItems = sort(transBillItems, on(BillItem.class).getSearialNo());
        } else {
            transBillItems = new ArrayList<>();
        }
        return transBillItems;
    }

    public void invertValue(Bill bill) {
        staffFee = 0 - bill.getStaffFee();
        performInstitutionFee = 0 - bill.getPerformInstitutionFee();
        billerFee = 0 - bill.getBillerFee();
        discount = 0 - bill.getDiscount();
        netTotal = 0 - bill.getNetTotal();
        total = 0 - bill.getTotal();

    }

    public void copy(Bill bill) {
        billType = bill.getBillType();
        collectingCentre = bill.getCollectingCentre();
        catId = bill.getCatId();
        creditCompany = bill.getCreditCompany();
        staff = bill.getStaff();
        toDepartment = bill.getToDepartment();
        toInstitution = bill.getToInstitution();
        fromDepartment = bill.getFromDepartment();
        fromInstitution = bill.getFromInstitution();
        discountPercent = bill.getDiscountPercent();
        patient = bill.getPatient();
        patientEncounter = bill.getPatientEncounter();
        referredBy = bill.getReferredBy();
        referringDepartment = bill.getReferringDepartment();
        //      referenceBill=bill.getReferenceBill();
        //  paymentScheme=bill.getPaymentScheme();
        //   paymentMethod=bill.getPaymentMethod();
        comments = bill.getComments();

    }

    public List<BillComponent> getBillComponents() {
        return billComponents;
    }

    public void setBillComponents(List<BillComponent> billComponents) {
        this.billComponents = billComponents;
    }
    ////////////////////////////////////////////////   
    @Lob
    String comments;
    // Bank Detail
    String creditCardRefNo;
    String chequeRefNo;
    @ManyToOne
    Institution bank;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date chequeDate;
    //Approve
    @ManyToOne
    WebUser approveUser;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date approveAt;
    //Pharmacy
    @Temporal(javax.persistence.TemporalType.DATE)
    Date invoiceDate;
    //Enum
    @Enumerated(EnumType.STRING)
    BillType billType;
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;
    //Values
    double total = 0.0;
    double discount = 0.0;
    double discountPercent;
    double netTotal = 0.0;
    double paidAmount = 0.0;
    double balance = 0.0;
    Double tax = 0.0;
    Double cashPaid = 0.0;
    Double cashBalance = 0.0;
    double saleValue = 0.0;
    double freeValue = 0.0;
    double performInstitutionFee;
    double staffFee;
    double billerFee;
    double grantTotal = 0.0;
    //Institution
    @ManyToOne
    Institution paymentSchemeInstitution;
    @ManyToOne
    Institution collectingCentre;
    @ManyToOne
    Institution institution;
    @ManyToOne
    Institution fromInstitution;
    @ManyToOne
    Institution toInstitution;
    @ManyToOne
    Institution creditCompany;
    //Departments
    @ManyToOne
    Department referringDepartment;
    @ManyToOne
    Department department;
    @ManyToOne
    Department fromDepartment;
    @ManyToOne
    Department toDepartment;
    //Bill
    @ManyToOne
    Bill billedBill;
    @ManyToOne
    Bill cancelledBill;
    @ManyToOne
    Bill refundedBill;
    @ManyToOne
    Bill reactivatedBill;
    @ManyToOne
    Bill referenceBill;
    //Id's
    String deptId;
    String insId;
    String catId;
    String sessionId;
    String bookingId;
    String invoiceNumber;
    @Transient
    int intInvoiceNumber;
    //Staff
    @ManyToOne
    Staff staff;
    @ManyToOne
    Staff fromStaff;
    @ManyToOne
    Staff toStaff;
    //Booleans
    boolean cancelled;
    boolean refunded;
    boolean reactivated;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    ////////////////
    @ManyToOne
    PaymentScheme paymentScheme;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date billDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date billTime;
    @Transient
    String billClass;
    @ManyToOne
    Item billPackege;//BILLPACKEGE_ID
    @ManyToOne
    Person person;
    @ManyToOne
    Patient patient;
    @ManyToOne
    Doctor referredBy;
    @ManyToOne
    PatientEncounter patientEncounter;
    @Transient
    List<Bill> listOfBill;

    public Field getField(String name) {
        try {
            System.err.println("ss : " + name);
            for (Field f : this.getClass().getFields()) {
                System.err.println(f.getName());
            }
            return this.getClass().getField(name);
        } catch (NoSuchFieldException e) {
            System.err.println("Ex no " + e.getMessage());
            return null;
        } catch (SecurityException e) {
            System.err.println("Ex " + e.getMessage());
            return null;
        }
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getNetTotal() {

        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getBalance() {

        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Bill> getListOfBill() {
        if (listOfBill == null) {
            listOfBill = new ArrayList<>();
        }
        return listOfBill;
    }

    public void setListOfBill(List<Bill> listOfBill) {
        this.listOfBill = listOfBill;
    }

    public int getIntInvoiceNumber() {
        return intInvoiceNumber;
    }

    public void setIntInvoiceNumber(int intInvoiceNumber) {
        this.intInvoiceNumber = intInvoiceNumber;
        invoiceNumber = intInvoiceNumber + "";
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public PaymentMethod getPaymentMethod() {
        if (paymentScheme != null) {
            paymentMethod = paymentScheme.getPaymentMethod();
        }
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Item getBillPackege() {
        return billPackege;
    }

    public void setBillPackege(Item billPackege) {
        this.billPackege = billPackege;
    }

    public String getBillClass() {
        return this.getClass().toString();
    }

    public void setBillClass(String billClass) {
        this.billClass = billClass;
    }

    public double getPerformInstitutionFee() {
        return performInstitutionFee;
    }

    public void setPerformInstitutionFee(double performInstitutionFee) {
        this.performInstitutionFee = performInstitutionFee;
    }

    public double getStaffFee() {
        return staffFee;
    }

    public void setStaffFee(double staffFee) {
        this.staffFee = staffFee;
    }

    public double getBillerFee() {
        return billerFee;
    }

    public void setBillerFee(double billerFee) {
        this.billerFee = billerFee;
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

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public Date getBillTime() {
        return billTime;
    }

    public void setBillTime(Date billTime) {
        this.billTime = billTime;
    }

    public Double getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(Double cashPaid) {
        this.cashPaid = cashPaid;

        cashBalance = netTotal - cashPaid;

    }

    public Double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(Double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public PaymentScheme getPaymentScheme() {
        System.out.println("");
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
        if (paymentScheme != null) {
            setPaymentMethod(paymentScheme.getPaymentMethod());
        }
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public Department getFromDepartment() {
        return fromDepartment;
    }

    public void setFromDepartment(Department fromDepartment) {
        this.fromDepartment = fromDepartment;
    }

    public Department getToDepartment() {
        return toDepartment;
    }

    public void setToDepartment(Department toDepartment) {
        this.toDepartment = toDepartment;
    }

    public Staff getFromStaff() {
        return fromStaff;
    }

    public void setFromStaff(Staff fromStaff) {
        this.fromStaff = fromStaff;
    }

    public Staff getToStaff() {
        return toStaff;
    }

    public void setToStaff(Staff toStaff) {
        this.toStaff = toStaff;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public Institution getPaymentSchemeInstitution() {
        return paymentSchemeInstitution;
    }

    public void setPaymentSchemeInstitution(Institution paymentSchemeInstitution) {
        this.paymentSchemeInstitution = paymentSchemeInstitution;
    }

    public PatientEncounter getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(PatientEncounter patientEncounter) {
        this.patientEncounter = patientEncounter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Bill)) {
            return false;
        }
        Bill other = (Bill) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.Bill[ id=" + id + " ]";
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public Patient getPatient() {
        if (patientEncounter != null) {
            patient = patientEncounter.getPatient();
        }
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(Doctor referredBy) {
        this.referredBy = referredBy;
    }

    public Department getReferringDepartment() {
        return referringDepartment;
    }

    public void setReferringDepartment(Department referringDepartment) {
        this.referringDepartment = referringDepartment;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setNetTotal(Double netTotal) {
        this.netTotal = netTotal;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getInsId() {
        return insId;
    }

    public void setInsId(String insId) {
        this.insId = insId;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Institution getCollectingCentre() {
        return collectingCentre;
    }

    public void setCollectingCentre(Institution collectingCentre) {
        this.collectingCentre = collectingCentre;
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public Bill getCancelledBill() {
        return cancelledBill;
    }

    public void setCancelledBill(Bill cancelledBill) {
        this.cancelledBill = cancelledBill;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public boolean isReactivated() {
        return reactivated;
    }

    public void setReactivated(boolean reactivated) {
        this.reactivated = reactivated;
    }

    public Bill getRefundedBill() {
        return refundedBill;
    }

    public void setRefundedBill(Bill refundedBill) {
        this.refundedBill = refundedBill;
    }

    public Bill getReactivatedBill() {
        return reactivatedBill;
    }

    public void setReactivatedBill(Bill reactivatedBill) {
        this.reactivatedBill = reactivatedBill;
    }

    public String getCreditCardRefNo() {
        return creditCardRefNo;
    }

    public void setCreditCardRefNo(String creditCardRefNo) {
        this.creditCardRefNo = creditCardRefNo;
    }

    public String getChequeRefNo() {
        return chequeRefNo;
    }

    public void setChequeRefNo(String chequeRefNo) {
        this.chequeRefNo = chequeRefNo;
    }

    public Institution getBank() {
        return bank;
    }

    public void setBank(Institution bank) {
        this.bank = bank;
    }

    public WebUser getApproveUser() {
        return approveUser;
    }

    public void setApproveUser(WebUser approveUser) {
        this.approveUser = approveUser;
    }

    public Date getApproveAt() {
        return approveAt;
    }

    public void setApproveAt(Date approveAt) {
        this.approveAt = approveAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Bill getReferenceBill() {
        return referenceBill;
    }

    public void setReferenceBill(Bill referenceBill) {
        this.referenceBill = referenceBill;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public double getSaleValue() {
        return saleValue;
    }

    public void setSaleValue(double saleValue) {
        this.saleValue = saleValue;
    }

    public double getFreeValue() {
        return freeValue;
    }

    public void setFreeValue(double freeValue) {
        this.freeValue = freeValue;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public double getGrantTotal() {
        return grantTotal;
    }

    public void setGrantTotal(double grantTotal) {
        this.grantTotal = grantTotal;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public List<BillFee> getBillFees() {
        return billFees;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }
    @Transient
    private double hospitalFee;
    @Transient
    private double professionalFee;
    @Transient
    private double tmpReturnTotal;

    public double getHospitalFee() {
        return hospitalFee;
    }

    public void setHospitalFee(double hospitalFee) {
        this.hospitalFee = hospitalFee;
    }

    public double getProfessionalFee() {
        return professionalFee;
    }

    public void setProfessionalFee(double professionalFee) {
        this.professionalFee = professionalFee;
    }

    public Bill getBilledBill() {
        return billedBill;
    }

    public void setBilledBill(Bill billedBill) {
        this.billedBill = billedBill;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public BatchBill getBatchBill() {
        return batchBill;
    }

    public void setBatchBill(BatchBill batchBill) {
        this.batchBill = batchBill;
    }

    @Transient
    private Bill tmpRefBill;

    public Bill getTmpRefBill() {
        return tmpRefBill;
    }

    public void setTmpRefBill(Bill refBill) {
        this.tmpRefBill = refBill;
    }

    public double getTmpReturnTotal() {
        return tmpReturnTotal;
    }

    public void setTmpReturnTotal(double tmpReturnTotal) {
        this.tmpReturnTotal = tmpReturnTotal;
    }

    public Bill getForwardReferenceBill() {
        return forwardReferenceBill;
    }

    public void setForwardReferenceBill(Bill forwardReferenceBill) {
        this.forwardReferenceBill = forwardReferenceBill;
    }

    public Bill getBackwardReferenceBill() {
        return backwardReferenceBill;
    }

    public void setBackwardReferenceBill(Bill backwardReferenceBill) {
        this.backwardReferenceBill = backwardReferenceBill;
    }

    public List<BillItem> getTransActiveBillItem() {
        if (billItems != null) {
            transActiveBillItem = new ArrayList<>();
            for (BillItem b : billItems) {
                if (!b.isRetired()) {
                    transActiveBillItem.add(b);
                }
            }
        } else {
            transActiveBillItem = new ArrayList<>();
        }
        return transActiveBillItem;
    }

    public void setTransActiveBillItem(List<BillItem> transActiveBillItem) {
        this.transActiveBillItem = transActiveBillItem;
    }

}
