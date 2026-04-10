package com.botree.csng.reports.currentstockweightreport;

import java.util.Date;

public class CurrentStockWeightModel {

    private String godownCode;
    private String godownName;
    private int prodHierLvlCode;
    private String prodHierLvlName;
    private String cmpCode;
    private String cmpName;

    private String distrBrCode;
    private String distrBrName;

    private String prodCode;
    private String prodName;
    private String prodWgtType;
    private String prodBatchCode;
    private Date expiryDt;
    private Double purPrice;
    private Double mrp;
    private String hsnCode;
    private Double saleableStockKg;
    private Double unSaleableStockKg;
    private Double offerStockKg;
    private Double totalKg;
    private Long saleableStock;
    private Long unSaleableStock;
    private Long offerStock;
    private Double stockValueSaleable;
    private Double stockValueUnSaleable;
    private Double totalStockValue;
    private String distrName;
    private String distrCode;
    private Integer cases;
    private Integer pieces;
    private String prodHierValName;
    private char gstprodType;
    private Integer ageingDays;

    public CurrentStockWeightModel() {
        super();
    }

    public CurrentStockWeightModel(String godownCode, String godownName) {
        super();
        this.godownCode = godownCode;
        this.godownName = godownName;
    }

    public CurrentStockWeightModel(int prodHierLvlCode, String prodHierLvlName) {
        super();
        this.prodHierLvlCode = prodHierLvlCode;
        this.prodHierLvlName = prodHierLvlName;
    }

    public CurrentStockWeightModel(String distrCode, String distrName,String distrBrCode,String distrBrName,
            String godownName, String hsnCode, String prodCode,String prodName,String prodBatchCode,Date expiryDt,Double mrp,
            Double purPrice,Integer cases , Integer pieces,Integer saleableStock, Integer unSaleableStock, Integer offerStock, Double stockValueSaleable,Double stockValueUnSaleable,
            Double totalStockValue, Double saleableStockKg, Double unSaleableStockKg, Double offerStockKg, Double totalKg,String prodWgtType, String prodHierValName, char gstprodType, Integer ageingDays ) {
        super();
        this.prodCode = prodCode;
        this.prodName = prodName;
        this.prodBatchCode = prodBatchCode;
        this.mrp = mrp;
        this.expiryDt = expiryDt;
        this.hsnCode = hsnCode;
        this.purPrice = purPrice;
        this.prodWgtType = prodWgtType;
        this.saleableStockKg = saleableStockKg;
        this.unSaleableStockKg = unSaleableStockKg;
        this.offerStockKg = offerStockKg;
        this.totalKg = totalKg;
        this.saleableStock = Long.valueOf(saleableStock);
        this.unSaleableStock = Long.valueOf(unSaleableStock);
        this.offerStock = Long.valueOf(offerStock);
        this.stockValueSaleable = stockValueSaleable;
        this.stockValueUnSaleable = stockValueUnSaleable;
        this.totalStockValue = totalStockValue;
        this.godownName = godownName;
        this.distrName = distrName;
        this.distrBrName = distrBrName;
        this.distrBrCode = distrBrCode;
        this.distrCode = distrCode;
        this.cases = cases;
        this.pieces = pieces;
        this.prodHierValName = prodHierValName;
        this.gstprodType = gstprodType;
        this.ageingDays = ageingDays;
    }


    public CurrentStockWeightModel(String distrCode, String distrName,String distrBrCode,String distrBrName,
            String godownName, String hsnCode, String prodCode,String prodName,String prodBatchCode,Date expiryDt,Double mrp,
            Double purPrice,Long saleableStock, Long unSaleableStock, Long offerStock, Double stockValueSaleable,Double stockValueUnSaleable,
            Double totalStockValue, Double saleableStockKg, Double unSaleableStockKg, Double offerStockKg, Double totalKg) {
        super();
        this.prodCode = prodCode;
        this.prodName = prodName;
        this.prodBatchCode = prodBatchCode;
        this.saleableStockKg = saleableStockKg;
        this.unSaleableStockKg = unSaleableStockKg;
        this.offerStockKg = offerStockKg;
        this.totalKg = totalKg;
        this.mrp = mrp;
        this.purPrice = purPrice;
        this.hsnCode = hsnCode;
        this.expiryDt = expiryDt;
        this.saleableStock = saleableStock;
        this.unSaleableStock = unSaleableStock;
        this.offerStock = offerStock;
        this.stockValueSaleable = stockValueSaleable;
        this.stockValueUnSaleable = stockValueUnSaleable;
        this.totalStockValue = totalStockValue;
        this.godownName = godownName;
        this.distrName = distrName;
        this.distrBrName = distrBrName;
        this.distrBrCode = distrBrCode;
        this.distrCode = distrCode;
    }

    /**
     * @return the godownCode
     */
    public String getGodownCode() {
        return godownCode;
    }

    /**
     * @param godownCode the godownCode to set
     */
    public void setGodownCode(String godownCode) {
        this.godownCode = godownCode;
    }

    /**
     * @return the godownName
     */
    public String getGodownName() {
        return godownName;
    }

    /**
     * @param godownName the godownName to set
     */
    public void setGodownName(String godownName) {
        this.godownName = godownName;
    }

    /**
     * @return the prodHierLvlCode
     */
    public int getProdHierLvlCode() {
        return prodHierLvlCode;
    }

    /**
     * @param prodHierLvlCode the prodHierLvlCode to set
     */
    public void setProdHierLvlCode(int prodHierLvlCode) {
        this.prodHierLvlCode = prodHierLvlCode;
    }

    /**
     * @return the prodHierLvlName
     */
    public String getProdHierLvlName() {
        return prodHierLvlName;
    }

    /**
     * @param prodHierLvlName the prodHierLvlName to set
     */
    public void setProdHierLvlName(String prodHierLvlName) {
        this.prodHierLvlName = prodHierLvlName;
    }

    /**
     * @return the cmpCode
     */
    public String getCmpCode() {
        return cmpCode;
    }

    /**
     * @param cmpCode the cmpCode to set
     */
    public void setCmpCode(String cmpCode) {
        this.cmpCode = cmpCode;
    }

    /**
     * @return the cmpName
     */
    public String getCmpName() {
        return cmpName;
    }

    /**
     * @param cmpName the cmpName to set
     */
    public void setCmpName(String cmpName) {
        this.cmpName = cmpName;
    }

    /**
     * @return the distrBrCode
     */
    public String getDistrBrCode() {
        return distrBrCode;
    }

    /**
     * @param distrBrCode the distrBrCode to set
     */
    public void setDistrBrCode(String distrBrCode) {
        this.distrBrCode = distrBrCode;
    }

    /**
     * @return the distrBrName
     */
    public String getDistrBrName() {
        return distrBrName;
    }

    /**
     * @param distrBrName the distrBrName to set
     */
    public void setDistrBrName(String distrBrName) {
        this.distrBrName = distrBrName;
    }

    /**
     * @return the prodCode
     */
    public String getProdCode() {
        return prodCode;
    }

    /**
     * @param prodCode the prodCode to set
     */
    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    /**
     * @return the prodName
     */
    public String getProdName() {
        return prodName;
    }

    /**
     * @param prodName the prodName to set
     */
    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    /**
     * @return the prodWgtType
     */
    public String getProdWgtType() {
        return prodWgtType;
    }

    /**
     * @param prodWgtType the prodWgtType to set
     */
    public void setProdWgtType(String prodWgtType) {
        this.prodWgtType = prodWgtType;
    }

    /**
     * @return the prodBatchCode
     */
    public String getProdBatchCode() {
        return prodBatchCode;
    }

    /**
     * @param prodBatchCode the prodBatchCode to set
     */
    public void setProdBatchCode(String prodBatchCode) {
        this.prodBatchCode = prodBatchCode;
    }

    /**
     * @return the expiryDt
     */
    public Date getExpiryDt() {
        return expiryDt;
    }

    /**
     * @param expiryDt the expiryDt to set
     */
    public void setExpiryDt(Date expiryDt) {
        this.expiryDt = expiryDt;
    }

    /**
     * @return the purPrice
     */
    public Double getPurPrice() {
        return purPrice;
    }

    /**
     * @param purPrice the purPrice to set
     */
    public void setPurPrice(Double purPrice) {
        this.purPrice = purPrice;
    }

    /**
     * @return the mrp
     */
    public Double getMrp() {
        return mrp;
    }

    /**
     * @param mrp the mrp to set
     */
    public void setMrp(Double mrp) {
        this.mrp = mrp;
    }

    /**
     * @return the hsnCode
     */
    public String getHsnCode() {
        return hsnCode;
    }

    /**
     * @param hsnCode the hsnCode to set
     */
    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    /**
     * @return the saleableStockKg
     */
    public Double getSaleableStockKg() {
        return saleableStockKg;
    }

    /**
     * @param saleableStockKg the saleableStockKg to set
     */
    public void setSaleableStockKg(Double saleableStockKg) {
        this.saleableStockKg = saleableStockKg;
    }

    /**
     * @return the unSaleableStockKg
     */
    public Double getUnSaleableStockKg() {
        return unSaleableStockKg;
    }

    /**
     * @param unSaleableStockKg the unSaleableStockKg to set
     */
    public void setUnSaleableStockKg(Double unSaleableStockKg) {
        this.unSaleableStockKg = unSaleableStockKg;
    }

    /**
     * @return the offerStockKg
     */
    public Double getOfferStockKg() {
        return offerStockKg;
    }

    /**
     * @param offerStockKg the offerStockKg to set
     */
    public void setOfferStockKg(Double offerStockKg) {
        this.offerStockKg = offerStockKg;
    }

    /**
     * @return the totalKg
     */
    public Double getTotalKg() {
        return totalKg;
    }

    /**
     * @param totalKg the totalKg to set
     */
    public void setTotalKg(Double totalKg) {
        this.totalKg = totalKg;
    }

    /**
     * @return the saleableStock
     */
    public Long getSaleableStock() {
        return saleableStock;
    }

    /**
     * @param saleableStock the saleableStock to set
     */
    public void setSaleableStock(Long saleableStock) {
        this.saleableStock = saleableStock;
    }

    /**
     * @return the unSaleableStock
     */
    public Long getUnSaleableStock() {
        return unSaleableStock;
    }

    /**
     * @param unSaleableStock the unSaleableStock to set
     */
    public void setUnSaleableStock(Long unSaleableStock) {
        this.unSaleableStock = unSaleableStock;
    }

    /**
     * @return the offerStock
     */
    public Long getOfferStock() {
        return offerStock;
    }

    /**
     * @param offerStock the offerStock to set
     */
    public void setOfferStock(Long offerStock) {
        this.offerStock = offerStock;
    }

    /**
     * @return the stockValueSaleable
     */
    public Double getStockValueSaleable() {
        return stockValueSaleable;
    }

    /**
     * @param stockValueSaleable the stockValueSaleable to set
     */
    public void setStockValueSaleable(Double stockValueSaleable) {
        this.stockValueSaleable = stockValueSaleable;
    }

    /**
     * @return the stockValueUnSaleable
     */
    public Double getStockValueUnSaleable() {
        return stockValueUnSaleable;
    }

    /**
     * @param stockValueUnSaleable the stockValueUnSaleable to set
     */
    public void setStockValueUnSaleable(Double stockValueUnSaleable) {
        this.stockValueUnSaleable = stockValueUnSaleable;
    }

    /**
     * @return the totalStockValue
     */
    public Double getTotalStockValue() {
        return totalStockValue;
    }

    /**
     * @param totalStockValue the totalStockValue to set
     */
    public void setTotalStockValue(Double totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

    /**
     * @return the distrName
     */
    public String getDistrName() {
        return distrName;
    }

    /**
     * @param distrName the distrName to set
     */
    public void setDistrName(String distrName) {
        this.distrName = distrName;
    }

    /**
     * @return the distrCode
     */
    public String getDistrCode() {
        return distrCode;
    }

    /**
     * @param distrCode the distrCode to set
     */
    public void setDistrCode(String distrCode) {
        this.distrCode = distrCode;
    }

	public Integer getCases() {
		return cases;
	}

	public void setCases(Integer cases) {
		this.cases = cases;
	}

	public Integer getPieces() {
		return pieces;
	}

	public void setPieces(Integer pieces) {
		this.pieces = pieces;
	}

	public String getProdHierValName() {
		return prodHierValName;
	}

	public void setProdHierValName(String prodHierValName) {
		this.prodHierValName = prodHierValName;
	}

	public char getGstprodType() {
		return gstprodType;
	}

	public void setGstprodType(char gstprodType) {
		this.gstprodType = gstprodType;
	}
	
	public Integer getAgeingDays() {
		return ageingDays;
	}

	public void setAgeingDays(Integer ageingDays) {
		this.ageingDays = ageingDays;
	}
	
}
