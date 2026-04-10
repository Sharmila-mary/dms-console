package com.botree.csng.reports.currentstockweightreport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.botree.common.bean.ProductHierarchyLevelPopup;
import com.botree.common.bean.ProductHierarchyLevelValuePopup;
import com.botree.common.bean.ProductPopUp;
import com.botree.csng.constants.StringConstants;
import com.botree.csng.domain.Distributor;
import com.botree.csng.domain.Product;
import com.botree.csng.domain.ProductHierLevel;
import com.botree.csng.domain.ProductHierValue;
import com.botree.csng.reports.service.db.IGenericReportDAOService;
import com.botree.csng.service.IReportService;

@Component("currentStockWeightService")
public class CurrentStockWeightService implements ICurrentStockWeightService {

    @Autowired
    IReportService reportService;
    @Autowired
    IGenericReportDAOService reportDaoService;

    private static final Logger LOG = LoggerFactory.getLogger(CurrentStockWeightService.class);

    @Override
    public void generateReportPDF(String reportName, CurrentStockWeightReportForm form) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(" Inside generateReport() Method ");
        }

        Map<String, Object> parameters = new HashMap<>();

        parameters.put(StringConstants.DISTRNAME, form.getDistrName());

        reportService.downloadReportUsingSQL(reportName, parameters);
    }

    @Override
    public Map<String, String> getCompanies(String[] distBranches) {

        int branchSize = distBranches.length;
        List<String> distrBranches = new ArrayList<>(branchSize);
        for (String distBranche : distBranches) {
            distrBranches.add(distBranche);
        }

        Map<String, String> companyMap = new HashMap<>();

        List<CurrentStockWeightModel> companies = reportDaoService.find("CurrentStockWeight.findCompanies",
                StringConstants.DISTRIBUTOR_BRANCHES, distrBranches);
        for (CurrentStockWeightModel company : companies) {
            companyMap.put(company.getGodownCode(), company.getGodownName());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Company map : {}", companyMap.entrySet());
        }
        return companyMap;
    }

    @Override
    public Map<Integer, String> getProductHierLevel(String godownCode) {

        Map<Integer, String> prodHierLevelMap = new HashMap<>();

        List<CurrentStockWeightModel> prodHierLevels = reportDaoService.find("CurrentStockWeight.findProductHierLevel",
                StringConstants.GODOWN_CODE, godownCode);
        for (CurrentStockWeightModel prodHierLvl : prodHierLevels) {
            prodHierLevelMap.put(prodHierLvl.getProdHierLvlCode(),
                    prodHierLvl.getProdHierLvlName() + "-" + prodHierLvl.getProdHierLvlCode());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Product Hier Level: {}", prodHierLevelMap.entrySet());
        }
        return prodHierLevelMap;
    }

    @Override
    public Map<Integer, String> getProductHierValue(Integer prodHierLvlCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("product hier value");
        }
        Map<Integer, String> prodHierValueMap = new HashMap<>();

        Map<String, Object> productHierLvlCode = new HashMap<>();

        productHierLvlCode.put(StringConstants.PRODUCT_HIER_LEVEL_CODE, prodHierLvlCode);

        List<ProductHierValue> prodHierValues = reportDaoService.find("CurrentStockWeight.findProductHierValue",
                productHierLvlCode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("size:" + prodHierValues.size());
        }
        if (prodHierValues != null && !prodHierValues.isEmpty()) {
            for (ProductHierValue prodHierVal : prodHierValues) {
                prodHierValueMap.put(Integer.parseInt(prodHierVal.getId().getProdHierValCode()),
                        prodHierVal.getProdHierValName() + "-" + prodHierVal.getId().getProdHierValCode());
            }
        } else {
            LOG.error("Product Hier Values list should not be empty.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Product Hier Value: {}", prodHierValueMap.entrySet());
        }
        return prodHierValueMap;

    }

    @Override
    public Map<String, String> getGodown(String[] distBranches) {

        int branchSize = distBranches.length;
        List<String> distrBranches = new ArrayList<>(branchSize);
        for (String distBranche : distBranches) {
            distrBranches.add(distBranche);
        }

        Map<String, String> godownMap = new HashMap<>();
        Map<String, Object> companyMap = new HashMap<>();
        companyMap.put(StringConstants.DISTRIBUTOR_BRANCHES, distrBranches);

        List<CurrentStockWeightModel> models = reportDaoService.find("CurrentStockWeight.findGodowns",
                StringConstants.DISTRIBUTOR_BRANCHES, distrBranches);
        if (LOG.isDebugEnabled()) {
            LOG.debug("list: " + models.size());
        }
        if (models.isEmpty() || !models.isEmpty()) {
            for (CurrentStockWeightModel godown : models) {
                godownMap.put(godown.getGodownCode(), godown.getGodownName());
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("This company has no godown.");
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Godown Map: {}", godownMap.entrySet());
        }
        return godownMap;
    }

    @Override
    public List<CurrentStockWeightReportList> search(CurrentStockWeightReportForm form) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("search method is fired.");
        }
        Map<String, Object> reportsMap = new HashMap<>();

        reportsMap.put(StringConstants.CMP_CODE, form.getCmpCode());
        reportsMap.put("GODOWN_CODE", form.getLocation());
        reportsMap.put(StringConstants.PRODUCT_HIER_LEVEL_CODE, form.getProdHierLvlCode());
        reportsMap.put("prodHierValCode", form.getProdHierValCode());

        String productStatus = form.getProductStatus();
        Character prodStatus = productStatus.charAt(0);
        String batchStatus = form.getBatchStatus();
        Character batStatus = batchStatus.charAt(0);

        if (prodStatus.equals('Y')) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Product status is {}", form.getProductStatus());
            }
            reportsMap.put(StringConstants.PRODUCT_IS_ACTIVE, prodStatus);
            reportsMap.put(StringConstants.PRODUCT_IS_ACTIVE1, prodStatus);
        } else if (prodStatus.equals('N')) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Product status is {}", form.getProductStatus());
            }
            reportsMap.put(StringConstants.PRODUCT_IS_ACTIVE, prodStatus);
            reportsMap.put(StringConstants.PRODUCT_IS_ACTIVE1, prodStatus);
        } else if (prodStatus.equals('A')) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Product status is {}", form.getProductStatus());
            }
            reportsMap.put(StringConstants.PRODUCT_IS_ACTIVE, 'Y');
            reportsMap.put(StringConstants.PRODUCT_IS_ACTIVE1, 'N');
        }

        if (batStatus.equals('Y')) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Batch status is  {}", form.getBatchStatus());
            }
            reportsMap.put(StringConstants.BATCH_IS_ACTIVE, batStatus);
            reportsMap.put(StringConstants.BATCH_IS_ACTIVE1, batStatus);
        } else if (batStatus.equals('N')) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Batch status is {}", form.getBatchStatus());
            }
            reportsMap.put(StringConstants.BATCH_IS_ACTIVE, batStatus);
            reportsMap.put(StringConstants.BATCH_IS_ACTIVE1, batStatus);
        } else if (batStatus.equals('A')) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Batch status is {}", form.getBatchStatus());
            }
            reportsMap.put(StringConstants.BATCH_IS_ACTIVE, 'Y');
            reportsMap.put(StringConstants.BATCH_IS_ACTIVE1, 'N');
        }

        return reportDaoService.find("CurrentStockVolume.findReports", reportsMap);
    }

    @Override
    public String getDistributorName(String distrCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("distributor name method");
        }
        Map<String, String> distributorMap = new HashMap<>();
        String distrName;
        List<Distributor> distributors = reportDaoService.find("CurrentStockWeight.findDistributorName", StringConstants.DISTR_CODE,
                distrCode);
        for (Distributor distributor : distributors) {
            distributorMap.put(distributor.getId().getDistrCode(), distributor.getDistrName());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("name:" + distributorMap.get(distrCode));
        }
        distrName = distributorMap.get(distrCode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("size of distributor:" + distributorMap.size() + "-" + distributorMap.entrySet());
        }
        return distrName;
    }

    @Override
    public List<ProductHierarchyLevelPopup> getProductLevels(String cmpCode) {
        List<ProductHierLevel> list;

        String[] params = { StringConstants.CMP_CODE };
        Object[] values = { cmpCode };
        list = reportDaoService.find("CurrentStockWeight.findProductHierLevel", params, values);

        List<ProductHierarchyLevelPopup> productList = new ArrayList<>();
        for (ProductHierLevel productHierLevel : list) {
            productList.add(new ProductHierarchyLevelPopup(
                    new Integer(productHierLevel.getId().getProdHierLvlCode()).toString(),
                    productHierLevel.getProdHierLvlName()));
        }
        return productList;
    }

    @Override
    public List<ProductHierarchyLevelValuePopup> getProductValue(List<ProductHierarchyLevelPopup> selectedLevels) {
        List<Integer> levelList = new ArrayList<>();
        for (ProductHierarchyLevelPopup model : selectedLevels) {
            levelList.add(Integer.parseInt(model.getCode()));
        }
        List<ProductHierValue> list = new ArrayList<>();
        if (!levelList.isEmpty()) {
            list = reportDaoService.find("CurrentStockWeight.findProductHierValue", "prodHierLvlCodes", levelList);
        }
        List<ProductHierarchyLevelValuePopup> productList = new ArrayList<>();
        for (ProductHierValue productHierLevel : list) {
            productList.add(new ProductHierarchyLevelValuePopup(
                    new Integer(productHierLevel.getId().getProdHierValCode()).toString(),
                    productHierLevel.getProdHierValName()));
        }
        return productList;
    }

    @Override
    public List<ProductPopUp> getProducts(List<ProductHierarchyLevelValuePopup> selectedValues) {
        List<Integer> valueLists = new ArrayList<>();
        for (ProductHierarchyLevelValuePopup model : selectedValues) {
            valueLists.add(Integer.parseInt(model.getCode()));
        }
        List<Product> products = new ArrayList<>();
        if (!valueLists.isEmpty()) {
            products = reportDaoService.find("CurrentStockWeight.findProducts", "prodHierValCodes", valueLists);

        }
        List<ProductPopUp> productPopupLists = new ArrayList<>();
        for (Product product : products) {
            productPopupLists.add(new ProductPopUp(product.getId().getProdCode(), product.getProdName()));
        }

        return productPopupLists;
    }

    @Override
    public List<CurrentStockWeightModel> getCSVData(Map<String, Object> map) {
        return reportDaoService.find("CurrentStockWeight.fetchCSVData", map);
    }

    @Override
    public List<CurrentStockWeightModel> getCSVDataTotal(Map<String, Object> map) {
        return reportDaoService.find("CurrentStockWeight.fetchCSVDataTotal", map);
    }

	
}
