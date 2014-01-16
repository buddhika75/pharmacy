/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.ejb.BillBean;
import com.divudi.entity.Department;
import com.divudi.entity.Fee;
import com.divudi.entity.Item;
import com.divudi.entity.PackageFee;
import com.divudi.entity.PackageItem;
import com.divudi.entity.Packege;
import com.divudi.entity.Staff;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.InvestigationFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PackageFeeFacade;
import com.divudi.facade.PackageItemFacade;
import com.divudi.facade.PackegeFacade;
import com.divudi.facade.StaffFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class PackageItemFeeController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private ItemFacade ejbFacade;
    @EJB
    private PackageFeeFacade packageFeeFacade;
    @EJB
    private PackageItemFacade packageItemFacade;
    @EJB
    private PackegeFacade packegeFacade;
    @EJB
    private InvestigationFacade investigationFacade;
    private Packege currentPackege;
    private List<PackageFee> fees;
    private Item currentIx;
    private PackageFee currentFee;
    private PackageFee removingPackageFee;
    private double total;
    List<PackageItem> items;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private BillBean billBean;

    public List<Staff> completeStaff(String query) {
        List<Staff> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Staff>();
        } else {
            if (getCurrentFee().getSpeciality() == null) {
                sql = "select p from Staff p where p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) order by p.person.name";
            } else {
                sql = "select p from Staff p where p.speciality.id=" + getCurrentFee().getSpeciality().getId() + " and p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) order by p.person.name";
            }
            System.out.println(sql);
            suggestions = getStaffFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public List<Department> getInstitutionDepatrments() {
        List<Department> d;
        System.out.println("gettin ins dep ");
        //if (getCurrentFee()==null && getCurrentFee().getInstitution() == null && getCurrentFee().getInstitution().getId() == null) {
        if (getCurrentFee().getInstitution() == null) {
            return new ArrayList<Department>();
        } else {
            String sql = "Select d From Department d where d.retired=false and d.institution.id=" + getCurrentFee().getInstitution().getId();
            d = getDepartmentFacade().findBySQL(sql);
        }

        return d;
    }

    public List<PackageItem> getItems() {
        String temSql;
        if (getCurrentPackege() != null) {
            temSql = "SELECT i FROM PackageItem i where i.retired=false and i.packege.id = " + getCurrentPackege().getId();
            items = getPackageItemFacade().findBySQL(temSql);
        } else {
            items = null;
        }

        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void saveCharge() {

        if (currentFee == null) {
            UtilityController.addErrorMessage("Please select a charge");
            return;
        }
        if (currentIx == null) {
            UtilityController.addErrorMessage("Please select a Investigation");
            return;
        }

        currentFee.setPackege(currentPackege);
        currentFee.setItem(currentIx);
        if (currentFee.getId() == null || currentFee.getId() == 0) {
            currentFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            currentFee.setCreater(sessionController.getLoggedUser());

            getPackageFeeFacade().create(currentFee);
            UtilityController.addSuccessMessage("Fee Added");
        } else {
            getPackageFeeFacade().edit(currentFee);
            UtilityController.addSuccessMessage("Fee Saved");
        }

        setPakageTotal();

        setCharges(null);
        currentFee = null;

    }

    private void setPakageTotal() {
        total = 0.0;
        List<Item> itemList = getBillBean().itemFromPackage(currentPackege);
        for (Item i : itemList) {
            List<Fee> tmp = getBillBean().getPackageFee(currentPackege, i);
            for (Fee ff : tmp) {
                total += ff.getFee();
            }
        }
        currentPackege.setTotal(total);
        getPackegeFacade().edit(currentPackege);
    }

    public ItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(ItemFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PackageItemFeeController() {
    }

    public Item getCurrentIx() {
        return currentIx;
    }

    public void setCurrentIx(Item ix) {
        this.currentIx = ix;

    }

    public void removeFee() {
        if (getRemovedPackageFee().getId() == null || getRemovedPackageFee().getId() == 0) {
            UtilityController.addErrorMessage("Nothing to remove");
            return;
        } else {
            getRemovedPackageFee().setRetired(true);
            getRemovedPackageFee().setRetirer(sessionController.getLoggedUser());
            getRemovedPackageFee().setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getPackageFeeFacade().edit(getRemovedPackageFee()); // Flag as retired, so that will never appearing when calling from database

            setCharges(null);
            getCharges();
            getCurrentIx().setTotal(getTotal());

        }
    }

    public void delete() {

        if (currentIx != null) {
            currentIx.setRetired(true);
            currentIx.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            currentIx.setRetirer(sessionController.getLoggedUser());
            getFacade().edit(currentIx);
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }

        currentIx = null;
        setCharges(null);
    }

    private ItemFacade getFacade() {
        return ejbFacade;
    }

    public List<PackageFee> getCharges() {

        if (currentIx != null && currentIx.getId() != null) {
            setCharges(getPackageFeeFacade().findBySQL("select c from PackageFee c where c.retired = false and c.packege.id=" + getCurrentPackege().getId() + " and c.item.id = " + currentIx.getId()));

        } else {
            setCharges(new ArrayList<PackageFee>());

        }
        if (fees == null) {
            fees = new ArrayList<>();

        }
        return fees;
    }

    public void setCharges(List<PackageFee> charges) {
        this.fees = charges;
    }

    public PackageFee getCurrentFee() {
        if (currentFee == null) {
            currentFee = new PackageFee();
        }
        return currentFee;
    }

    public void setCurrentFee(PackageFee currentFee) {
        this.currentFee = currentFee;
    }

    public double getTotal() {
        double d = 0.0;
        for (PackageFee f : getCharges()) {
            d = d + f.getFee();
        }
        total = d;
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public PackageFeeFacade getPackageFeeFacade() {
        return packageFeeFacade;
    }

    public void setPackageFeeFacade(PackageFeeFacade packageFeeFacade) {
        this.packageFeeFacade = packageFeeFacade;

    }

    public PackageFee getRemovedPackageFee() {
        return removingPackageFee;
    }

    public void setRemovedPackageFee(PackageFee removedPackageFee) {
        this.removingPackageFee = removedPackageFee;
    }

    public Packege getCurrentPackege() {
        return currentPackege;
    }

    public void setCurrentPackege(Packege currentPackege) {
        this.currentPackege = currentPackege;
        getCharges();
        getTotal();
    }

    public List<PackageFee> getFees() {
        return fees;
    }

    public void setFees(List<PackageFee> fees) {
        this.fees = fees;
    }

    public PackageFee getRemovingPackageFee() {
        return removingPackageFee;
    }

    public void setRemovingPackageFee(PackageFee removingPackageFee) {
        this.removingPackageFee = removingPackageFee;
    }

    public PackageItemFacade getPackageItemFacade() {
        return packageItemFacade;
    }

    public void setPackageItemFacade(PackageItemFacade packageItemFacade) {
        this.packageItemFacade = packageItemFacade;
    }

    public PackegeFacade getPackegeFacade() {
        return packegeFacade;
    }

    public void setPackegeFacade(PackegeFacade packegeFacade) {
        this.packegeFacade = packegeFacade;
    }

    public InvestigationFacade getInvestigationFacade() {
        return investigationFacade;
    }

    public void setInvestigationFacade(InvestigationFacade investigationFacade) {
        this.investigationFacade = investigationFacade;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    /**
     *
     */
    @FacesConverter("conPackageFee")
    public static class PackageFeeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PackageFeeController controller = (PackageFeeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "packageItemFeeController");
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
            if (object instanceof PackageFee) {
                PackageFee o = (PackageFee) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PackageFeeController.class.getName());
            }
        }
    }
}
