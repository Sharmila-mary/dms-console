package com.botree.csng.reports.currentstockweightreport;

import java.util.Date;

public class CurrentStockWeightReportForm {

    private String cmpCode;
    private String cmpName;
    private String location;
    private int prodHierLvlCode;
    private String prodHierLvlName;
    private int prodHierValCode;
    private String prodHierValName;
    private String displayBatch;
    private String productStatus;
    private String batchStatus;
    private String distrCode;
    private String distrBrCode;
    private String[] distrBrCodes;
    private String distrName;
    private Date fromDate = new Date();
    private Date toDate = new Date();
	private String subDistrCode;

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

    public String getCmpCode() {
        return cmpCode;
    }

    public void setCmpCode(String cmpCode) {
        this.cmpCode = cmpCode;
    }

    public String getCmpName() {
        return cmpName;
    }

    public void setCmpName(String cmpName) {
        this.cmpName = cmpName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getProdHierLvlCode() {
        return prodHierLvlCode;
    }

    public void setProdHierLvlCode(int prodHierLvlCode) {
        this.prodHierLvlCode = prodHierLvlCode;
    }

    public String getProdHierLvlName() {
        return prodHierLvlName;
    }

    public void setProdHierLvlName(String prodHierLvlName) {
        this.prodHierLvlName = prodHierLvlName;
    }

    public int getProdHierValCode() {
        return prodHierValCode;
    }

    public void setProdHierValCode(int prodHierValCode) {
        this.prodHierValCode = prodHierValCode;
    }

    public String getProdHierValName() {
        return prodHierValName;
    }

    public void setProdHierValName(String prodHierValName) {
        this.prodHierValName = prodHierValName;
    }

    public String getDisplayBatch() {
        return displayBatch;
    }

    public void setDisplayBatch(String displayBatch) {
        this.displayBatch = displayBatch;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus) {
        this.batchStatus = batchStatus;
    }

    public String getDistrCode() {
        return distrCode;
    }

    public void setDistrCode(String distrCode) {
        this.distrCode = distrCode;
    }

    public String getDistrBrCode() {
        return distrBrCode;
    }

    public void setDistrBrCode(String distrBrCode) {
        this.distrBrCode = distrBrCode;
    }

    public String[] getDistrBrCodes() {
        return distrBrCodes;
    }

    public void setDistrBrCodes(String[] distrBrCodes) {
        this.distrBrCodes = distrBrCodes;
    }

    public String getDistrName() {
        return distrName;
    }

    public void setDistrName(String distrName) {
        this.distrName = distrName;
    }

	public String getSubDistrCode() {
		return subDistrCode;
	}

	public void setSubDistrCode(String subDistrCode) {
		this.subDistrCode = subDistrCode;
	}

}
