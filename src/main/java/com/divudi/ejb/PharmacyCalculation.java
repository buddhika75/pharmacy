/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Category;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.ItemBatch;
import com.divudi.entity.pharmacy.ItemsDistributors;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.PharmaceuticalItem;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.CategoryFacade;
import com.divudi.facade.ItemBatchFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemsDistributorsFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class PharmacyCalculation {

    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private AmpFacade ampFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private ItemBatchFacade itemBatchFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private ItemsDistributorsFacade itemsDistributorsFacade;
    @EJB
    private CategoryFacade categoryFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillNumberBean billNumberBean;

//    public void editBill(Bill bill, Bill ref, SessionController sc) {
//
//       
//    }
    public List<Item> getSuggessionOnly(Item item) {
        List<Item> suggessions = new ArrayList<>();

        if (item instanceof Amp) {
            suggessions = findPack((Amp) item);
            suggessions.add(item);
        } else if (item instanceof Ampp) {
            Amp amp = ((Ampp) item).getAmp();
            suggessions = findPack(amp);
            suggessions.add(amp);
        }

        return suggessions;
    }

    public List<Item> getItemsForDealor(Institution i) {
        String temSql;
        List<Item> tmp;
        if (i != null) {
            temSql = "SELECT i.item FROM ItemsDistributors i where i.retired=false and i.institution.id = " + i.getId();
            tmp = getItemFacade().findBySQL(temSql);
        } else {
            tmp = null;
        }

        if (tmp == null) {
            tmp = new ArrayList<>();
        }
        return tmp;
    }

    public boolean checkItem(Institution ins, Item i) {
        String sql = "Select i from ItemsDistributors i where i.retired=false and i.institution.id= " + ins.getId() + " and i.item.id=" + i.getId();
        ItemsDistributors tmp = getItemsDistributorsFacade().findFirstBySQL(sql);
        if (tmp != null) {
            return true;
        }

        return false;
    }

    public double calRetailRate(PharmaceuticalBillItem ph) {

        PharmaceuticalItem i = (PharmaceuticalItem) ph.getBillItem().getItem();
        double margin = 0.0;
        String sql;
        Category cat;

        if (i instanceof Amp) {
            sql = "Select p.category from Vmp p where p.retired=false and p.id in (Select a.vmp.id from Amp a where a.retired=false and a.id=" + i.getId() + ")";
        } else if (i instanceof Ampp) {
            sql = "Select p.category from Vmp p where p.retired=false and p.id in (Select a.amp.vmp.id from Ampp a where a.retired=false and a.id=" + i.getId() + ")";

        } else {
            return 0.0f;
        }

        cat = getCategoryFacade().findFirstBySQL(sql);

        if (cat != null) {
            margin = cat.getSaleMargin();
        }

        double retailPrice = ph.getPurchaseRate() + (ph.getPurchaseRate() * (margin / 100));

        return (double) retailPrice;
    }

    public double getTotalQty(BillItem b, BillType billType, Bill bill) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  type(p.bill)=:class and p.creater is not null and"
                + " p.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("btp", billType);
        hm.put("class", bill.getClass());

        double value = getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

        ////System.err.println("GETTING TOTAL QTY " + value);
        return value;
    }

    public double getTotalQty(BillItem b, BillType billType) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  p.creater is not null and"
                + " p.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("btp", billType);

        double value = getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

        ////System.err.println("GETTING TOTAL QTY " + value);
        return value;
    }

    public double getBilledIssuedByRequestedItem(BillItem b, BillType billType) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  p.creater is not null and type(p.bill)=:class and "
                + " p.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("class", BilledBill.class);
        hm.put("btp", billType);

        double value = getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

        //System.err.println("GETTING Billed QTY " + value);
        return value;
    }
    
    public double getCancelledIssuedByRequestedItem(BillItem b, BillType billType) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  p.creater is not null and type(p.bill)=:class and "
                + " p.referanceBillItem.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("class", CancelledBill.class);
        hm.put("btp", billType);

        double value = getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

        //System.err.println("GETTING Cancelled TOTAL QTY " + value);
        return value;
    }

    public double getTotalQty(BillItem b, BillType billType, Bill refund, Bill cancel) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  (type(p.bill)=:class or type(p.bill)=:class2)and p.creater is not null and"
                + " p.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("btp", billType);
        hm.put("class", refund.getClass());
        hm.put("class2", cancel.getClass());
        return getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

    }

//    public double getTotalQtyInSingleSql(BillItem b, BillType billType) {
//        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
//                + "   p.creater is not null and"
//                + " p.referanceBillItem=:bt and p.bill.billType=:btp";
//
//        HashMap hm = new HashMap();
//        hm.put("bt", b);
//        hm.put("btp", billType);
////        hm.put("class", refund.getClass());
////        hm.put("class2", cancel.getClass());
//        return getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);
//
//    }
    public double getReturnedTotalQty(BillItem b, BillType billType, Bill bill) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  type(p.bill)=:class and p.bill.creater is not null and"
                + " p.referanceBillItem.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("btp", billType);
        hm.put("class", bill.getClass());

        return getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

    }

    public double getReturnedTotalQty(BillItem b, BillType billType) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "  p.bill.creater is not null and"
                + " p.referanceBillItem.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", b);
        hm.put("btp", billType);

        return getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);

    }

    public double calQty(PharmaceuticalBillItem po) {

        double billed = getTotalQty(po.getBillItem(), BillType.PharmacyGrnBill, new BilledBill());
        double cancelled = getTotalQty(po.getBillItem(), BillType.PharmacyGrnBill, new CancelledBill());;
        double returnedB = getReturnedTotalQty(po.getBillItem(), BillType.PharmacyGrnReturn, new BilledBill());
        double returnedC = getReturnedTotalQty(po.getBillItem(), BillType.PharmacyGrnReturn, new CancelledBill());

        double recieveNet = Math.abs(billed) - Math.abs(cancelled);
        double retuernedNet = Math.abs(returnedB) - Math.abs(returnedC);
        ////System.err.println("BILLED " + billed);
        ////System.err.println("Cancelled " + cancelled);
        ////System.err.println("recieveNet " + recieveNet);
        ////System.err.println("Refunded Bill " + returnedB);
        ////System.err.println("Refunded Cancelld " + returnedC);
        ////System.err.println("retuernedNet " + retuernedNet);
        ////System.err.println("Cal Qty " + (Math.abs(recieveNet) - Math.abs(retuernedNet)));

        return (Math.abs(recieveNet) - Math.abs(retuernedNet));
    }

    public double calQtyInTwoSql(PharmaceuticalBillItem po) {

        double grns = getTotalQty(po.getBillItem(), BillType.PharmacyGrnBill);
        double grnReturn = getReturnedTotalQty(po.getBillItem(), BillType.PharmacyGrnReturn);

        double netQty = grns - grnReturn;

        //System.err.println("GRN " + grns);
        //System.err.println("GRN Return " + grnReturn);
        //System.err.println("Net " + netQty);

        return netQty;
    }

    public double calQty2(BillItem bil) {

        double returnBill = getTotalQty(bil, BillType.PharmacySale, new RefundBill());

        ////System.err.println("RETURN " + returnBill);
        return bil.getQty() - returnBill;
    }

    public double calQty3(BillItem bil) {

        double returnBill = getTotalQty(bil, BillType.PharmacyPre, new RefundBill());

        ////System.err.println("RETURN " + returnBill);
        return bil.getQty() - returnBill;
    }

//     public double calQty(PharmaceuticalBillItem po) {
//       
//        double billed =getTotalQty(po.getBillItem(),BillType.PharmacyGrnBill,new BilledBill());
//        double cancelled = getTotalQty(po.getBillItem(),BillType.PharmacyGrnBill,new CancelledBill());;
//        double returned = getTotalQty(po.getBillItem(),BillType.PharmacyGrnReturn,new BilledBill());
//
//        ////System.err.println("BILLED " + billed);
//        ////System.err.println("Cancelled " + cancelled);
//        ////System.err.println("Refunded " + returned);
//        ////System.err.println("Cal Qty " + (billed - (cancelled + returned)));
//
//        return billed - (cancelled + returned);
//    }
//    public double checkQty(PharmaceuticalBillItem ph) {
//        if (ph.getQty() == 0.0) {
//            return 0.0;
//        }
//
//        double adjustedGrnQty;
//        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id=" + ph.getBillItem().getReferanceBillItem().getId();
//        PharmaceuticalBillItem po = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);
//
//        Item poItem = po.getBillItem().getItem();
//        Item grnItem = ph.getBillItem().getItem();
//
//        if ((poItem instanceof Amp || poItem instanceof Vmp) && grnItem instanceof Amp) {
//            if (po.getQty() < ph.getQty()) {
//                return po.getQty();
//            } else {
//                return ph.getQty();
//            }
//        } else if ((poItem instanceof Ampp || poItem instanceof Vmpp) && grnItem instanceof Ampp) {
//            adjustedGrnQty = ph.getQty() * grnItem.getDblValue();
//            double adjustedPoQty = po.getQty() * poItem.getDblValue();
//            if (adjustedPoQty < adjustedGrnQty) {
//                return adjustedPoQty / grnItem.getDblValue();
//            } else {
//                return ph.getQty();
//            }
//
//        } else {
//            if ((poItem instanceof Amp || poItem instanceof Vmp) && grnItem instanceof Ampp) {
//                adjustedGrnQty = ph.getQty() * grnItem.getDblValue();
//                if (po.getQty() < adjustedGrnQty) {
//                    return po.getQty() / grnItem.getDblValue();
//                } else {
//                    return ph.getQty();
//                }
//
//            } else {
//                adjustedGrnQty = ph.getQty() / poItem.getDblValue();
//                if (po.getQty() < adjustedGrnQty) {
//                    return po.getQty() * poItem.getDblValue();
//                } else {
//                    return ph.getQty();
//                }
//            }
//        }
//    }
    public double getRemainingQty(PharmaceuticalBillItem ph) {

        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id = " + ph.getBillItem().getReferanceBillItem().getId();
        PharmaceuticalBillItem po = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);

        //    Item poItem = po.getBillItem().getItem();
        //    Item grnItem = ph.getBillItem().getItem();
        double poQty, grnQty, remains;
        poQty = po.getQtyInUnit();
        remains = poQty - calQty(po);

        //System.err.println("REMAIN " + remains);
        return remains;

    }

    public boolean checkQty(PharmaceuticalBillItem ph) {

        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id=" + ph.getBillItem().getReferanceBillItem().getId();
        PharmaceuticalBillItem po = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);

        //    Item poItem = po.getBillItem().getItem();
        //    Item grnItem = ph.getBillItem().getItem();
        double poQty, grnQty, remains;
        poQty = Math.abs(po.getQtyInUnit());
        remains = Math.abs(poQty) - calQty(po);
        grnQty = Math.abs(ph.getQtyInUnit());

        ////System.err.println("poQty : " + poQty);
        ////System.err.println("grnQty : " + grnQty);
        ////System.err.println("remain : " + remains);
        if (remains < grnQty) {
            return true;
        } else {
            return false;
        }

    }

    public boolean checkPurchasePrice(PharmaceuticalBillItem i) {
        double oldPrice, newPrice = 0.0;

        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id=" + i.getBillItem().getReferanceBillItem().getId();
        PharmaceuticalBillItem tmp = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);

        oldPrice = tmp.getPurchaseRate();
        newPrice = i.getPurchaseRate();

        double max = oldPrice + (oldPrice * (getPharmacyBean().getMaximumPurchasePriceChange() / 100.0));
        ////System.err.println("Old Pur Price : " + oldPrice);
        ////System.err.println("New Pur Price : " + newPrice);
        ////System.err.println("MAX Price : " + max);

        if (max < newPrice) {
            return true;
        } else {
            return false;
        }
    }

//    public boolean checkRetailPrice(PharmaceuticalBillItem i) {
//        double oldPrice, newPrice = 0.0;
//
//        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id=" + i.getBillItem().getReferanceBillItem().getId();
//        PharmaceuticalBillItem tmp = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);
//
//        oldPrice = tmp.getRetailRate();
//        newPrice = i.getRetailRate();
//
////        ////System.err.println("Old Ret Price : " + oldPrice);
////        ////System.err.println("New Ret Price : " + newPrice);
//        double max = oldPrice + (oldPrice * (getPharmacyBean().getMaximumRetailPriceChange() / 100));
//
//        ////System.err.println("Old Ret Price : " + oldPrice);
//        ////System.err.println("New Ret Price : " + newPrice);
//        ////System.err.println("MAX Price : " + max);
//
//        if (max < newPrice) {
//            return true;
//        } else {
//            return false;
//        }
//    }
    public boolean checkRetailPrice(PharmaceuticalBillItem i) {

        double max = i.getPurchaseRate() + (i.getPurchaseRate() * (getPharmacyBean().getMaximumRetailPriceChange() / 100));

        ////System.err.println("Purchase Price : " + i.getPurchaseRate());
        ////System.err.println("Retail Price : " + i.getRetailRate());
        ////System.err.println("MAX Price : " + max);
        if (max < i.getRetailRate()) {
            return true;
        } else {
            return false;
        }
    }

    public ItemBatch saveItemBatch(BillItem tmp) {
        ////System.err.println("Save Item Batch");
        ItemBatch itemBatch = new ItemBatch();
        Item itm = tmp.getItem();

        if (itm instanceof Ampp) {
            itm = ((Ampp) itm).getAmp();
        }

        double purchase = tmp.getPharmaceuticalBillItem().getPurchaseRateInUnit();
        double retail = tmp.getPharmaceuticalBillItem().getRetailRateInUnit();

        ////System.err.println("Puchase :  " + purchase);
        ////System.err.println("Puchase :  " + retail);
        itemBatch.setDateOfExpire(tmp.getPharmaceuticalBillItem().getDoe());
        itemBatch.setBatchNo(tmp.getPharmaceuticalBillItem().getStringValue());
        itemBatch.setPurcahseRate(purchase);
        itemBatch.setRetailsaleRate(retail);

        HashMap hash = new HashMap();
        String sql;

        itemBatch.setItem(itm);
        sql = "Select p from ItemBatch p where  p.item=:itm "
                + " and p.dateOfExpire= :doe and p.retailsaleRate=:ret "
                + " and p.purcahseRate=:pur";

        hash.put("doe", itemBatch.getDateOfExpire());
        hash.put("itm", itemBatch.getItem());
        hash.put("ret", itemBatch.getRetailsaleRate());
        hash.put("pur", itemBatch.getPurcahseRate());
        List<ItemBatch> i = getItemBatchFacade().findBySQL(sql, hash, TemporalType.TIMESTAMP);
        ////System.err.println("Size " + i.size());
        if (i.size() > 0) {
//            ////System.err.println("Edit");
//            i.get(0).setBatchNo(i.get(0).getBatchNo());
//            i.get(0).setDateOfExpire(i.get(0).getDateOfExpire());
            return i.get(0);
        } else {
            ////System.err.println("Create");
            getItemBatchFacade().create(itemBatch);
        }

        ////System.err.println("ItemBatc Id " + itemBatch.getId());
        return itemBatch;
    }

    public List<Item> findItem(Amp tmp, List<Item> items) {

        String sql;

        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }

        return items;
    }

    public List<Item> findPack(Amp amp) {

        String sql;
        HashMap hm = new HashMap();
        sql = "SELECT i from Ampp i where i.retired=false and "
                + " i.amp=:am";

        hm.put("am", amp);

        return getItemFacade().findBySQL(sql, hm);

    }

    public List<Item> findItem(Ampp tmp, List<Item> items) {

        String sql;
        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getAmp().getVmp().getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getAmp().getVmp().getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }
        return items;
    }

    public List<Item> findItem(Vmp tmp, List<Item> items) {

        String sql;
        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }
        return items;
    }

    public List<Item> findItem(Vmpp tmp, List<Item> items) {

        String sql;
        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }
        return items;
    }

    public void editBillItem(PharmaceuticalBillItem i, WebUser w) {

        i.getBillItem().setNetValue(0 - (i.getQty() * i.getPurchaseRate()));

        i.getBillItem().setCreatedAt(Calendar.getInstance().getTime());
        i.getBillItem().setCreater(w);
        i.getBillItem().setPharmaceuticalBillItem(i);

        getBillItemFacade().edit(i.getBillItem());
//
//        double consumed = calQty(i.getBillItem().getReferanceBillItem().getPharmaceuticalBillItem());
//        i.getBillItem().setRemainingQty(i.getBillItem().getReferanceBillItem().getPharmaceuticalBillItem().getQty() - consumed);
//        getBillItemFacade().edit(i.getBillItem());
    }

//    public BillItem saveBillItem(Item i, Bill b, SessionController s) {
//        BillItem tmp = new BillItem();
//        tmp.setBill(b);
//        tmp.setItem(i);
//        //  tmp.setQty(getPharmacyBean().getOrderingQty(i, s.getDepartment()));
//        // tmp.setRate(getPharmacyBean().getPurchaseRate(i, s.getDepartment()));
//        // tmp.setNetValue(tmp.getQty() * tmp.getRate());
//
//        getBillItemFacade().create(tmp);
//
//        return tmp;
//    }
    public String errorCheck(Bill b, List<BillItem> billItems) {
        String msg = "";

        if (b.getInvoiceNumber() == null || "".equals(b.getInvoiceNumber().trim())) {
            msg = "Please Fill invoice number";
        }

        if (b.getPaymentMethod() != null && b.getPaymentMethod() == PaymentMethod.Cheque) {
            if (b.getBank().getId() == null || b.getChequeRefNo() == null) {
                msg = "Please select Cheque Number and Bank";
            }
        }

        if (b.getPaymentMethod() != null && b.getPaymentMethod() == PaymentMethod.Slip) {
            if (b.getBank().getId() == null || b.getComments() == null) {
                msg = "Please Fill Memo and Bank";
            }
        }

        if (billItems.isEmpty()) {
            msg = "There is no Item to receive";
        }

        if (checkItemBatch(billItems)) {
            msg = "Please Fill Batch deatail and Sale Price to All Item";
        }

        return msg;
    }

    public void calSaleFreeValue(Bill b, List<BillItem> bis) {
    //    System.out.println("cal sale free value");
        double sale = 0.0;
        double free = 0.0;
        for (BillItem i : bis) {
        //    System.out.println("i.getPharmaceuticalBillItem().getQty() = " + i.getPharmaceuticalBillItem().getQty());
        //    System.out.println("i.getPharmaceuticalBillItem().getFreeQty() = " + i.getPharmaceuticalBillItem().getFreeQty());
        //    System.out.println("i.getPharmaceuticalBillItem().getPurchaseRate() = " + i.getPharmaceuticalBillItem().getPurchaseRate());
            sale += i.getPharmaceuticalBillItem().getQty() * i.getPharmaceuticalBillItem().getPurchaseRate();
            free += i.getPharmaceuticalBillItem().getFreeQty() * i.getPharmaceuticalBillItem().getPurchaseRate();
        //    System.out.println("sale = " + sale);
        //    System.out.println("free = " + free);
        }
        b.setSaleValue(sale);
        b.setFreeValue(free);
    }

    public boolean checkItemBatch(List<BillItem> list) {

        for (BillItem i : list) {
            if (i.getPharmaceuticalBillItem().getQty() != 0.0) {
                if (i.getPharmaceuticalBillItem().getDoe() == null || i.getPharmaceuticalBillItem().getStringValue().trim().equals("")) {
                    return true;
                }
                if (i.getPharmaceuticalBillItem().getPurchaseRate() > i.getPharmaceuticalBillItem().getRetailRate()) {
                    return true;
                }

            }

        }
        return false;
    }

//    public void preCalForAddToStock(PharmacyItemData ph, ItemBatch itb, Department d) {
//
//        Item item = ph.getPharmaceuticalBillItem().getBillItem().getItem();
//        Double qty = 0.0;
//
//        if (item instanceof Amp) {
//            qty = ph.getPharmaceuticalBillItem().getQty() + ph.getPharmaceuticalBillItem().getFreeQty();
//
//        } else {
//            qty = (ph.getPharmaceuticalBillItem().getQty() + ph.getPharmaceuticalBillItem().getFreeQty()) * item.getDblValue();
//            //      ////System.out.println("sssssss " + qty);
//        }
//
//        if (itb.getId() != null) {
//           
//        }
//    }
    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public ItemBatchFacade getItemBatchFacade() {
        return itemBatchFacade;
    }

    public void setItemBatchFacade(ItemBatchFacade itemBatchFacade) {
        this.itemBatchFacade = itemBatchFacade;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public ItemsDistributorsFacade getItemsDistributorsFacade() {
        return itemsDistributorsFacade;
    }

    public void setItemsDistributorsFacade(ItemsDistributorsFacade itemsDistributorsFacade) {
        this.itemsDistributorsFacade = itemsDistributorsFacade;
    }

    public CategoryFacade getCategoryFacade() {
        return categoryFacade;
    }

    public void setCategoryFacade(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
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
}
