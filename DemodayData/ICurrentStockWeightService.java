package com.botree.csng.reports.currentstockweightreport;

import java.util.List;
import java.util.Map;

import com.botree.common.bean.ProductHierarchyLevelPopup;
import com.botree.common.bean.ProductHierarchyLevelValuePopup;
import com.botree.common.bean.ProductPopUp;

public interface ICurrentStockWeightService {

    public void generateReportPDF(String reportName, CurrentStockWeightReportForm form);

    public Map<String, String> getCompanies(String[] distrBranches);

    public Map<Integer, String> getProductHierLevel(String godownCode);

    public Map<Integer, String> getProductHierValue(Integer prodHierLvlCode);

    public Map<String, String> getGodown(String[] distBranches);

    public List<CurrentStockWeightReportList> search(CurrentStockWeightReportForm form);

    public String getDistributorName(String distrCode);

    public List<ProductHierarchyLevelPopup> getProductLevels(String cmpCode);

    public List<ProductHierarchyLevelValuePopup> getProductValue(List<ProductHierarchyLevelPopup> selectedLevels);

    public List<ProductPopUp> getProducts(List<ProductHierarchyLevelValuePopup> selectedValues);

    public List<CurrentStockWeightModel> getCSVData(Map<String, Object> map);

    public List<CurrentStockWeightModel> getCSVDataTotal(Map<String, Object> map);

}
