package com.botree.csng.reports.currentstockweightreport;

import java.util.ArrayList;
import java.util.List;

public class CurrentStockWeightReportModel {

    private String distrBrCode;
    private String distrBrName;

    private List<CurrentStockWeightSubReportModel> subreportList = new ArrayList<>();

    public String getDistrBrCode() {
        return distrBrCode;
    }

    public void setDistrBrCode(String distrBrCode) {
        this.distrBrCode = distrBrCode;
    }

    public String getDistrBrName() {
        return distrBrName;
    }

    public void setDistrBrName(String distrBrName) {
        this.distrBrName = distrBrName;
    }

    public List<CurrentStockWeightSubReportModel> getSubreportList() {
        return subreportList;
    }

    public void setSubreportList(List<CurrentStockWeightSubReportModel> subreportList) {
        this.subreportList = subreportList;
    }

}
