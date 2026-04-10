package com.botree.csng.reports.currentstockweightreport;

public class CurrentStockWeightSubReportModel {

    private String prodCode;
    private String prodName;
    private Double mrp;
    private String prodBatchCode;
    private int saleableStock;
    private int unSaleableStock;
    private int offerStock;
    private Double stockValueSaleable;
    private Double stockValueUnsaleable;
    private Double totalStockValue;
    private Double purPrice;
    private String godownName;
    private String distrName;
    private String distrBrName;
    private Double listPriceWithTax;

    public CurrentStockWeightSubReportModel() {
        // Default Constructor

    }

    public CurrentStockWeightSubReportModel(String prodCode, String prodName, String prodBatchCode, Double mrp,
            Double purPrice, int saleableStock, int unSaleableStock, int offerStock, Double stockValueSaleable,
            Double stockValueUnsaleable, Double totalStockValue, String godownName, String distrName,
            String distrBrName) {
        this.prodCode = prodCode;
        this.prodName = prodName;
        this.prodBatchCode = prodBatchCode;
        this.mrp = mrp;
        this.purPrice = purPrice;
        this.saleableStock = saleableStock;
        this.unSaleableStock = unSaleableStock;
        this.offerStock = offerStock;
        this.stockValueSaleable = stockValueSaleable;
        this.stockValueUnsaleable = stockValueUnsaleable;
        this.totalStockValue = totalStockValue;
        this.godownName = godownName;
        this.distrName = distrName;
        this.distrBrName = distrBrName;
    }

    public CurrentStockWeightSubReportModel(String prodCode, String prodName, Double mrp, String prodBatchCode,
            int saleableStock, int unSaleableStock, int offerStock, Double stockValueSaleable,
            Double stockValueUnsaleable, Double totalStockValue) {
        this.prodCode = prodCode;
        this.prodName = prodName;
        this.mrp = mrp;
        this.prodBatchCode = prodBatchCode;
        this.saleableStock = saleableStock;
        this.unSaleableStock = unSaleableStock;
        this.offerStock = offerStock;
        this.stockValueSaleable = stockValueSaleable;
        this.stockValueUnsaleable = stockValueUnsaleable;
        this.totalStockValue = totalStockValue;
    }

    public CurrentStockWeightSubReportModel(String prodCode, String prodName, String prodBatchCode, Double mrp,
            Double listPriceWithTax, int saleableStock, int unSaleableStock, int offerStock, Double stockValueSaleable,
            Double stockValueUnsaleable, Double totalStockValue) {
        this.prodCode = prodCode;
        this.prodName = prodName;
        this.prodBatchCode = prodBatchCode;
        this.mrp = mrp;
        this.listPriceWithTax = listPriceWithTax;
        this.saleableStock = saleableStock;
        this.unSaleableStock = unSaleableStock;
        this.offerStock = offerStock;
        this.stockValueSaleable = stockValueSaleable;
        this.stockValueUnsaleable = stockValueUnsaleable;
        this.totalStockValue = totalStockValue;
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

    public Double getMrp() {
        return mrp;
    }

    public void setMrp(Double mrp) {
        this.mrp = mrp;
    }

    public String getProdBatchCode() {
        return prodBatchCode;
    }

    public void setProdBatchCode(String prodBatchCode) {
        this.prodBatchCode = prodBatchCode;
    }

    public int getSaleableStock() {
        return saleableStock;
    }

    public void setSaleableStock(int saleableStock) {
        this.saleableStock = saleableStock;
    }

    public int getUnSaleableStock() {
        return unSaleableStock;
    }

    public void setUnSaleableStock(int unSaleableStock) {
        this.unSaleableStock = unSaleableStock;
    }

    public int getOfferStock() {
        return offerStock;
    }

    public void setOfferStock(int offerStock) {
        this.offerStock = offerStock;
    }

    public Double getStockValueSaleable() {
        return stockValueSaleable;
    }

    public void setStockValueSaleable(Double stockValueSaleable) {
        this.stockValueSaleable = stockValueSaleable;
    }

    public Double getStockValueUnsaleable() {
        return stockValueUnsaleable;
    }

    public void setStockValueUnsaleable(Double stockValueUnsaleable) {
        this.stockValueUnsaleable = stockValueUnsaleable;
    }

    public Double getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(Double totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

    public Double getPurPrice() {
        return purPrice;
    }

    public void setPurPrice(Double purPrice) {
        this.purPrice = purPrice;
    }

    public String getGodownName() {
        return godownName;
    }

    public void setGodownName(String godownName) {
        this.godownName = godownName;
    }

    public String getDistrName() {
        return distrName;
    }

    public void setDistrName(String distrName) {
        this.distrName = distrName;
    }

    public String getDistrBrName() {
        return distrBrName;
    }

    public void setDistrBrName(String distrBrName) {
        this.distrBrName = distrBrName;
    }

    public Double getListPriceWithTax() {
        return listPriceWithTax;
    }

    public void setListPriceWithTax(Double listPriceWithTax) {
        this.listPriceWithTax = listPriceWithTax;
    }

}
