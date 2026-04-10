package com.botree.csng.reports.currentstockweightreport;

public class CurrentStockWeightReportList {

    private String distrBrCode;
    private String prodCode;
    private String prodName;
    private String prodBatchCode;
    private String godownName;
    private int mrp;
    private int saleableStock;
    private int unsaleableStock;
    private int offerStock;
    private int inTransitSaleQty;
    private int inTransitUnsaleQty;
    private int inTransitofferQty;

    public CurrentStockWeightReportList(String distrBrCode, String prodCode, String prodName, String prodBatchCode,
            String godownName, int saleableStock, int unsaleableStock, int offerStock, int inTransitSaleQty,
            int inTransitUnsaleQty, int inTransitofferQty) {
        super();
        this.distrBrCode = distrBrCode;
        this.prodCode = prodCode;
        this.prodName = prodName;
        this.prodBatchCode = prodBatchCode;
        this.godownName = godownName;
        this.saleableStock = saleableStock;
        this.unsaleableStock = unsaleableStock;
        this.offerStock = offerStock;
        this.inTransitSaleQty = inTransitSaleQty;
        this.inTransitUnsaleQty = inTransitUnsaleQty;
        this.inTransitofferQty = inTransitofferQty;
    }

    public String getDistrBrCode() {
        return distrBrCode;
    }

    public void setDistrBrCode(String distrBrCode) {
        this.distrBrCode = distrBrCode;
    }

    public String getProdCode() {
        return prodCode;
    }

    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdBatchCode() {
        return prodBatchCode;
    }

    public void setProdBatchCode(String prodBatchCode) {
        this.prodBatchCode = prodBatchCode;
    }

    public String getGodownName() {
        return godownName;
    }

    public void setGodownName(String godownName) {
        this.godownName = godownName;
    }

    public int getMrp() {
        return mrp;
    }

    public void setMrp(int mrp) {
        this.mrp = mrp;
    }

    public int getSaleableStock() {
        return saleableStock;
    }

    public void setSaleableStock(int saleableStock) {
        this.saleableStock = saleableStock;
    }

    public int getUnsaleableStock() {
        return unsaleableStock;
    }

    public void setUnsaleableStock(int unsaleableStock) {
        this.unsaleableStock = unsaleableStock;
    }

    public int getOfferStock() {
        return offerStock;
    }

    public void setOfferStock(int offerStock) {
        this.offerStock = offerStock;
    }

    public int getInTransitSaleQty() {
        return inTransitSaleQty;
    }

    public void setInTransitSaleQty(int inTransitSaleQty) {
        this.inTransitSaleQty = inTransitSaleQty;
    }

    public int getInTransitUnsaleQty() {
        return inTransitUnsaleQty;
    }

    public void setInTransitUnsaleQty(int inTransitUnsaleQty) {
        this.inTransitUnsaleQty = inTransitUnsaleQty;
    }

    public int getInTransitofferQty() {
        return inTransitofferQty;
    }

    public void setInTransitofferQty(int inTransitofferQty) {
        this.inTransitofferQty = inTransitofferQty;
    }

}
