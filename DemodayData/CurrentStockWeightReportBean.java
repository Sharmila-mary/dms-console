package com.botree.csng.reports.currentstockweightreport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import com.botree.common.bean.CompanyPopUp;
import com.botree.common.bean.DistributorBranchPopUp;
import com.botree.common.bean.GodownPopUp;
import com.botree.common.bean.ProductHierarchyLevelPopup;
import com.botree.common.bean.ProductHierarchyLevelValuePopup;
import com.botree.common.bean.ProductPopUp;
import com.botree.common.exception.CoreStockyException;
import com.botree.csng.common.AbstractBean;
import com.botree.csng.common.AppConfig;
import com.botree.csng.common.CSVFileExporter;
import com.botree.csng.common.component.distributorselection.DistributorSelectionBean;
import com.botree.csng.common.lobpopup.LOBPopupBean;
import com.botree.csng.common.lobpopup.LobValuePopup;
import com.botree.csng.constants.StringConstants;
import com.botree.csng.reports.grnlistingreport.IGRNListingReportService;
import com.botree.csng.reports.popup.IPopupsBeanService;
import com.botree.csng.reports.popup.PopUpsBean;
import com.botree.csng.reports.salesreportbillwise.ISalesReportBillWiseService;

@Component("currentStockWeightReportBean")
@Scope("session")
public class CurrentStockWeightReportBean extends AbstractBean {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentStockWeightReportBean.class);

    @Autowired
    private ICurrentStockWeightService service;

    @Autowired
    private ISalesReportBillWiseService salService;

    @Autowired
    IGRNListingReportService grnService;

    @Autowired
    PopUpsBean popUpsBean;

    @Autowired
    LOBPopupBean lobPopupBean;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    IPopupsBeanService popupsBeanService;

    

    @Autowired
    private DistributorSelectionBean distrSelectionBean;

    private CurrentStockWeightReportForm form = new CurrentStockWeightReportForm();
    private Map<String, String> companies;
    private Map<String, String> godowns;
    private Map<Integer, String> productHierLevels;
    private Map<Integer, String> productHierValues;
    private Map<String, String> distrBranches;
	private List<CurrentStockWeightReportList> searchList;
    private Date currentDate = new Date();
    private boolean generateReportFlag;
    private boolean exportToExcel;
    private boolean lobFlag;
	private Map<String, String> subDistrCodes = new HashMap<>();
	private String selectedDistr;
	private String distrCode;

    public boolean isGenerateReportFlag() {
        if (form.getFromDate() == null || form.getToDate() == null || popUpsBean.getSelectedBranches() == null) {
            generateReportFlag = true;
        } else if (form.getFromDate().compareTo(form.getToDate()) > 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(StringConstants.CONTENT_POPUP_FROM_DATE, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "From date should less then to date", "From date should less then to date"));
            generateReportFlag = true;
        } else {
            generateReportFlag = false;
        }

        return generateReportFlag;
    }

    @Override
    public void onPageDisplay() {
        lobPopupBean.resetAllFlags();
        lobPopupBean.setReportFlag(true);
        lobPopupBean.setLobValue("");
        setLobFlag(userSession.isLobEnabledFlag());
        lobPopupBean.loadDefaultLobValues();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside the init() method");
        }
        if('Y' == appConfig.getEnableSubStockiestReportConfig() && popUpsBean.isEnableSubDistrReportData()) {
			setSelectedDistr(userSession.getUserCode());
        }
		subDistrCodes.clear();
		subDistrCodes =popUpsBean.loadSubDistr();
		popUpsBean.setDistrName(userSession.getUserCode());
		popUpsBean.setSubDistrReportFalg(true);
		popUpsBean.setSelectedDistr(userSession.getDistrCode());
		distrCode=userSession.getDistrCode();
		popUpsBean.loadDistrBranchesForSubDistrs(distrCode, distrCode);
    }

	/**
	 * @param distrCde
	 * Below method loads subdistr branch based on the selected distr in drop down.
	 */
	public void loadSubDistrData(String distrCde) {
		distrCode=distrCde;
		popUpsBean.setDistrName(subDistrCodes.get(distrCde));
		popUpsBean.setSubDistrReportFalg(true);
		popUpsBean.setSelectedDistr(distrCde);
		popUpsBean.loadDistrBranchesForSubDistrs(distrCode, distrCode);
		popUpsBean.setDistrBrCode("");
		popUpsBean.setGodown("");
		popUpsBean.setSelectedDistrBranchBackUp(null);
		popUpsBean.setSelectedBranch(null);
		popUpsBean.setSubDistrCode(distrCde);
		lobPopupBean.setLobValue("");
		popUpsBean.setProductLevel("");
		popUpsBean.setProductValue("");
	}

    public Map<String, Object> getParameters() {
        Map<String, Object> map = new HashMap<>();
        List<String> distrList=new ArrayList<>();
        //If this config is enabled then selected distr code from drop down is sent.
		if ('Y' == appConfig.getEnableSubStockiestReportConfig() && !userSession.isCmpUser() && popUpsBean.isEnableSubDistrReportData()) {
			distrList.add(popUpsBean.getSelectedDistr());
			map.put("DISTRIBUTOR_NAME_STR", subDistrCodes.get(popUpsBean.getSelectedDistr()));
		} else {
			distrList = popUpsBean.getDistributorList();
			  map.put("DISTRIBUTOR_NAME_STR", popUpsBean.getDistrName());
		}
        map.put("DISTR_BR_LST", popUpsBean.getBranchCodeList());
        map.put("DISTR_BR_STR", popUpsBean.getDistrBrCode());
		
		map.put("DISTRIBUTOR_LST", distrList);
        map.put("GODOWN_LST", popUpsBean.getGodownList());
        map.put("GODOWN_AVL", popUpsBean.getGodownFlag());

        map.put("P_ProductCode", appConfig.isShowSKU() ? StringConstants.SKU_CODE : StringConstants.PRODUCT_CODE);
        map.put("P_ProductName", appConfig.isShowSKU() ? StringConstants.SKU_NAME : StringConstants.PRODUCT_NAME);
        map.put("P_Product", appConfig.isShowSKU() ? StringConstants.SKU : StringConstants.PRODUCT);

        if (StringConstants.N.equals(popUpsBean.getGodownFlag())) {
            map.put("GODOWN_STR", StringConstants.ALL);
        } else {
            map.put("GODOWN_STR", popUpsBean.getGodown());
        }

        map.put("PROD_HIER_LVL_AVL", popUpsBean.getProductLevelFlag());
        if (StringConstants.N.equals(popUpsBean.getProductLevelFlag())) {
            map.put("PROD_HIER_LVL_STR", StringConstants.ALL);
        } else {
            map.put("PROD_HIER_LVL_STR", popUpsBean.getProductLevel());
        }
        map.put("PROD_HIER_VAL_AVL", popUpsBean.getPrdValueFlag());
        if (StringConstants.N.equals(popUpsBean.getPrdValueFlag())) {
            map.put("PROD_HIER_VAL_STR", StringConstants.ALL);
        } else {
            map.put("PROD_HIER_VAL_STR", popUpsBean.getProductValue());
        }

        if ("Y".equalsIgnoreCase(popUpsBean.getProductStatus())) {
            map.put(StringConstants.PRODUCT_STATUS_ACTIVE_STR, popUpsBean.getProductStatus());
            map.put(StringConstants.PRODUCT_STATUS_INACTIVE_STR, popUpsBean.getProductStatus());
            map.put(StringConstants.PRODUCT_STATUS_STR, StringConstants.ACTIVE);
        } else if (StringConstants.N.equalsIgnoreCase(popUpsBean.getProductStatus())) {
            map.put(StringConstants.PRODUCT_STATUS_ACTIVE_STR, popUpsBean.getProductStatus());
            map.put(StringConstants.PRODUCT_STATUS_INACTIVE_STR, popUpsBean.getProductStatus());
            map.put(StringConstants.PRODUCT_STATUS_STR, StringConstants.INACTIVE);
        } else if ("ALL".equalsIgnoreCase(popUpsBean.getProductStatus())) {

            map.put("PRODUCT_STATUS_ACTIVE_STR", "Y");
            map.put("PRODUCT_STATUS_INACTIVE_STR", StringConstants.N);
            map.put("PRODUCT_STATUS_STR", "<All>");
        }

        if ("Y".equalsIgnoreCase(popUpsBean.getBatchStatus())) {
            map.put(StringConstants.BATCH_STATUS_ACTIVE_STR, popUpsBean.getBatchStatus());
            map.put(StringConstants.BATCH_STATUS_INACTIVE_STR, popUpsBean.getBatchStatus());
            map.put(StringConstants.BATCH_STATUS_STR, "<Active>");
        } else if (StringConstants.N.equalsIgnoreCase(popUpsBean.getBatchStatus())) {
            map.put(StringConstants.BATCH_STATUS_ACTIVE_STR, popUpsBean.getBatchStatus());
            map.put(StringConstants.BATCH_STATUS_INACTIVE_STR, popUpsBean.getBatchStatus());
            map.put(StringConstants.BATCH_STATUS_STR, "<Inactive>");
        } else if ("ALL".equalsIgnoreCase(popUpsBean.getBatchStatus())) {

            map.put(StringConstants.BATCH_STATUS_ACTIVE_STR, "Y");
            map.put(StringConstants.BATCH_STATUS_INACTIVE_STR, StringConstants.N);
            map.put(StringConstants.BATCH_STATUS_STR, "<All>");
        }

        if ("MRP".equals(popUpsBean.getStockValueasPer())) {
            map.put(StringConstants.RATE_DISPLAYED_TITLE, "MRP");
            map.put(StringConstants.RATE_TOBE_DISPLAYED, "PB.MRP");
        } else if (StringConstants.PURCHASE_RATE.equals(popUpsBean.getStockValueasPer())) {
            map.put(StringConstants.RATE_DISPLAYED_TITLE, "List Price With out Tax");
            map.put(StringConstants.RATE_TOBE_DISPLAYED, "PB.PurPrice");
        } else if (StringConstants.SELLING_RATE.equals(popUpsBean.getStockValueasPer())) {
            map.put(StringConstants.RATE_DISPLAYED_TITLE, "Selling Rate Without Tax");
            map.put(StringConstants.RATE_TOBE_DISPLAYED, "PB.SellPrice");
        } else if ("Selling Rate With Tax".equals(popUpsBean.getStockValueasPer())) {
            map.put(StringConstants.RATE_DISPLAYED_TITLE, "Selling Rate With Tax");
            map.put(StringConstants.RATE_TOBE_DISPLAYED,
                    "PB.SellPrice + (PB.SellPrice * CAST((ISNULL(ST.TaxRate1,0) + ISNULL(ST.TaxRate2,0) +(ISNULL(ST.TaxRate1,0)*ISNULL(ST.TaxRate3,0)/100)+(ISNULL(ST.TaxRate1,0)*ISNULL(ST.TaxRate4,0)/100)) as decimal(22,6))/100)");
        } else {
            map.put(StringConstants.RATE_DISPLAYED_TITLE, "List Price With Tax");
            map.put(StringConstants.RATE_TOBE_DISPLAYED,
                    "PB.PurPrice + (PB.PurPrice * CAST((ISNULL(ST.TaxRate1,0) + ISNULL(ST.TaxRate2,0) +(ISNULL(ST.TaxRate1,0)*ISNULL(ST.TaxRate3,0)/100)+(ISNULL(ST.TaxRate1,0)*ISNULL(ST.TaxRate4,0)/100)) as decimal(22,6))/100)");

        }
        List<String> productHireValueList = getDefaultProducts();
        map.put("PRODUCT_HIER_VALUE_LIST", productHireValueList);

        getLobCodeList(map);

        map.put("PRODUCT_AVL", popUpsBean.getProdFlagForProdHierValues());

        boolean geoProdFlag = false;
        if ('D' == appConfig.getProdBatchConfig()) {
            geoProdFlag = true;
        }

        if (!popUpsBean.isCmpuser() && 'Y' != appConfig.getEnableSubStockiestReportConfig() ){

            if (geoProdFlag) {
                List<String> distrs = new ArrayList<>();
                distrs.add(userSession.getDistrCode());
                map.put(StringConstants.GEOPROD_LST, distrs);
            } else {
                map.put(StringConstants.GEOPROD_LST, userSession.getGeoPathList());
            }
        } else if(!popUpsBean.isCmpuser() && 'Y' == appConfig.getEnableSubStockiestReportConfig() && popUpsBean.isEnableSubDistrReportData() ){
        	 if (geoProdFlag) {
                 map.put(StringConstants.GEOPROD_LST, distrList);
             } else {
                 List<String> getGeoPathList = popUpsBean
                         .getGeoPathListForSelectedDistr(distrList);
                 map.put(StringConstants.GEOPROD_LST, getGeoPathList);
             }
        }else {
            if (geoProdFlag) {
                map.put(StringConstants.GEOPROD_LST, popUpsBean.getDistributorList());
            } else {
                List<String> getGeoPathList = popUpsBean
                        .getGeoPathListForSelectedDistr(popUpsBean.getDistributorList());
                map.put(StringConstants.GEOPROD_LST, getGeoPathList);
            }
           
        }
        map.put("PRODUCT_TO_DISPLAY",
                "and P.ProdCode in (select GPB.prodCode from GeoProductBatch GPB where $X{IN,GPB.geoCode,GEOPROD_LST})"
                        + "and PB.prodBatchCode in (select GPB.prodBatchCode from GeoProductBatch GPB where $X{IN,GPB.geoCode,GEOPROD_LST})");

        return map;
    }

    public void reset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside Reset method ");
        }
        popUpsBean.reset();
        this.exportToExcel = false;
        popUpsBean.setDisplayBatchStatutsFlag("Y");
        onPageDisplay();
    }

    public List<String> getDefaultProducts() {
        List<String> prodHierValueCodeList = new ArrayList<>();
        List<String> levelCodes = new ArrayList<>();
        if (!"".equals(popUpsBean.getProductValue()) && popUpsBean.getProductValue() != null) {
            List<ProductPopUp> list = popupsBeanService.getCompanyByHierLevelValues(
                    popUpsBean.getSelectedProdLevelPopup(), popUpsBean.getSelctedprodLevelValuePopUps());
            if (list != null && !list.isEmpty()) {
                for (ProductHierarchyLevelValuePopup model : popUpsBean.getSelctedprodLevelValuePopUps()) {
                    List<String> tempList;
                    List<ProductHierarchyLevelPopup> levelList = popupsBeanService
                            .getHierLevelCodeByValues(model.getCode(), list.get(0).getCmpCode());
                    if(levelList != null && !levelList.isEmpty()) {
                    	String levelCode = levelList.get(0).getCode();
                        if (userSession.isCmpUser()) {
                            List<String> lobCodes = new ArrayList<>();
                            for (LobValuePopup lob : distrSelectionBean.getLobPopupBean().getSelectedLobValueList()) {
                                lobCodes.add(lob.getCode());
                            }

                            tempList = salService.getAllProducts(list.get(0).getCmpCode(), model.getCode(), levelCode,
                                    lobCodes);
                            levelCodes.addAll(tempList);
                        } else {
                            List<String> lobCodes = new ArrayList<>();
                            for (LobValuePopup lob : lobPopupBean.getSelectedLobValueList()) {
                                lobCodes.add(lob.getCode());
                            }
                            tempList = salService.getProductsBasedUponLoginDistributor(list.get(0).getCmpCode(),
                                    model.getCode(), levelCode, userSession.getGeoPathList(), userSession.getUserCode(),
                                    lobCodes, appConfig.getProdBatchConfig());
                            levelCodes.addAll(tempList);
                        }
                    }
                }
            }
        } else {
            String prodLevel = popupsBeanService.findLObProductLevel();
            List<ProductHierarchyLevelValuePopup> valueList = popupsBeanService.getProductHierValue(
                    userSession.getCmpCode(), prodLevel,
                    distrSelectionBean.getLobPopupBean().getSelectedLobValueList());

            for (ProductHierarchyLevelValuePopup model : valueList) {
                List<String> tempList;
                if (userSession.isCmpUser()) {
                    List<String> lobCodes = new ArrayList<>();
                    for (LobValuePopup lob : distrSelectionBean.getLobPopupBean().getSelectedLobValueList()) {
                        lobCodes.add(lob.getCode());
                    }
                    tempList = salService.getAllProducts(userSession.getCmpCode(), model.getCode(), prodLevel,
                            lobCodes);
                    levelCodes.addAll(tempList);
                } else {

                    List<String> lobCodes = new ArrayList<>();
                    for (LobValuePopup lob : lobPopupBean.getSelectedLobValueList()) {
                        lobCodes.add(lob.getCode());
                    }
                    tempList = salService.getProductsBasedUponLoginDistributor(userSession.getCmpCode(),
                            model.getCode(), prodLevel, userSession.getGeoPathList(), userSession.getUserCode(),
                            lobCodes, appConfig.getProdBatchConfig());
                    levelCodes.addAll(tempList);
                }
            }
        }
        Set<String> levelCodeSet = new HashSet<>(levelCodes);
        prodHierValueCodeList.addAll(levelCodeSet);
        return prodHierValueCodeList;
    }

    public void getGenerateReportForPDF() {
        boolean flag = validate();
        if (flag) {
            popUpsBean.setDisplayBatchStatutsFlag("Y");
            if ("Y".equals(popUpsBean.getDisplayBatchStatutsFlag())) {
                if (!userSession.getLobCodeList().contains("CHOC")) {
                    if ("MRP".equals(popUpsBean.getStockValueasPer())) {
                        String reportName;
                        reportName = "CurrentStockReportmrp";
                        Map<String, Object> map = getParameters();
                        salService.generateReportPDF(reportName, map);
                    } else if (StringConstants.PURCHASE_RATE.equals(popUpsBean.getStockValueasPer())) {
                        String reportName = "CurrentStockWeightReport";
                        Map<String, Object> map = getParameters();
                        salService.generateReportPDF(reportName, map);
                    } else if (StringConstants.PURCHASE_RATE_WITH_TAX.equals(popUpsBean.getStockValueasPer())) {
                        String reportName = "CurrentStockReportLPWBWT";
                        Map<String, Object> map = getParameters();
                        salService.generateReportPDF(reportName, map);
                    } else if (StringConstants.SELLING_RATE.equals(popUpsBean.getStockValueasPer())) {
                        String reportName = "CurrentStockReportSRWBWOT";
                        Map<String, Object> map = getParameters();
                        salService.generateReportPDF(reportName, map);
                    } else {
                        String reportName = "CurrentStockReportSRWBWT";
                        Map<String, Object> map = getParameters();
                        salService.generateReportPDF(reportName, map);
                    }
                } else {
                    String reportName = "CurrentStockReportMARS";
                    Map<String, Object> map = getParameters();
                    salService.generateReportPDF(reportName, map);
                }
            } else {

                if ("MRP".equals(popUpsBean.getStockValueasPer())) {
                    String reportName;
                    reportName = "CurrentStockReportmrp-";
                    Map<String, Object> map = getParameters();
                    salService.generateReportPDF(reportName, map);
                } else if (StringConstants.PURCHASE_RATE.equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockReport-";
                    Map<String, Object> map = getParameters();
                    salService.generateReportPDF(reportName, map);
                } else if (StringConstants.PURCHASE_RATE_WITH_TAX.equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockReportLPWOBWT";
                    Map<String, Object> map = getParameters();
                    salService.generateReportPDF(reportName, map);
                } else if (StringConstants.SELLING_RATE.equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockReportSRWOBWOT";
                    Map<String, Object> map = getParameters();
                    salService.generateReportPDF(reportName, map);
                } else {
                    String reportName = "CurrentStockReportSRWOBWT";
                    Map<String, Object> map = getParameters();
                    salService.generateReportPDF(reportName, map);
                }
            }
            this.setExportToExcel(false);
        }
    }

    public void getProductLevelForLocation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("inside product level for location");
        }
        this.productHierLevels = new HashMap<>();
        if (form.getLocation() != null && "".equalsIgnoreCase(form.getLocation())) {
            this.productHierLevels = service.getProductHierLevel(form.getLocation());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("This company has no godowns.");
            }
        }
    }

    public void getProdHierValueForHierLevel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get Product hier value for hier level called {}", form.getProdHierLvlCode());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("cmp code: " + form.getCmpCode());
        }
        this.productHierValues = new HashMap<>();
        if (form.getProdHierLvlCode() != 0) {
            this.productHierValues = service.getProductHierValue(form.getProdHierLvlCode());
        }
    }

    public void getGodownLocation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("godown method");
        }
        this.godowns = new HashMap<>();
        this.productHierLevels = new HashMap<>();
        this.productHierValues = new HashMap<>();
        if (form.getDistrBrCodes() != null && form.getDistrBrCodes().length != 0) {
            this.godowns = service.getGodown(form.getDistrBrCodes());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("This company has no godowns.");
            }
        }
    }

    public void getCompany() {

        this.companies = new HashMap<>();
        if (form.getDistrBrCodes().length != 0) {
            this.companies = service.getCompanies(form.getDistrBrCodes());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("This branch has no company.");
            }
        }

    }

    public void search() {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("search method fire.");
            }

            searchList = service.search(form);
            if (LOG.isDebugEnabled()) {
                LOG.debug("size:" + searchList.size());
            }
        } catch (CoreStockyException e) {
            LOG.error("CurrentStockVolumeBean:search()", e);
        }
    }

    public boolean validate() {
        boolean flag = true;
        if (popUpsBean.getGodown() == null || "".equals(popUpsBean.getGodown())) {
            flag = false;
            PrimeFaces.current().ajax().addCallbackParam(StringConstants.SHOW_DIALOG, false);
            setErrorMessage("content:selectGodown", "E_MISSING_GODOWN");
        }

        if (popUpsBean.isCmpuser() && popUpsBean.getDistrType() == null && "".equals(popUpsBean.getDistrType())) {
            flag = false;
            PrimeFaces.current().ajax().addCallbackParam(StringConstants.SHOW_DIALOG, false);
            setErrorMessage("content:distrType", "E_MISSING_DISTRIBUTOR");

        }

        if (popUpsBean.getDistrBrCode() == null || "".equals(popUpsBean.getDistrBrCode())) {
            flag = false;
            PrimeFaces.current().ajax().addCallbackParam(StringConstants.SHOW_DIALOG, false);
            setErrorMessage("content:distrBranch", "E_REQ_DISTRBRANCH");
        }
        if (flag) {
        	PrimeFaces.current().ajax().addCallbackParam(StringConstants.SHOW_DIALOG, true);
        }
        return flag;
    }

    @Override
    public String getHeader() {

        return "Current Stock Report";
    }

    @Override
    public void delete() {
        // Overridden from Abstract Bean
    }

    @Override
    public void setSearchPage() {
        // Overridden from Abstract Bean

    }

    public void fromdateValidator(SelectEvent event) {
        Date date1 = (Date) event.getObject();
        Date date2 = form.getToDate();

        if (date1.compareTo(date2) > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(StringConstants.DATE1_IS_AFTER_DATE2);
            }
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("content:popupFromDate", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    StringConstants.DATE1_IS_AFTER_DATE2, StringConstants.DATE1_IS_AFTER_DATE2));
        }
    }

    public void todateValidator(SelectEvent event) {
        Date date1 = (Date) event.getObject();
        Date date2 = form.getFromDate();
        if (date2.compareTo(date1) > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Date1 is after Date2");
            }
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("content:popupFromDate", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    StringConstants.DATE1_IS_AFTER_DATE2, StringConstants.DATE1_IS_AFTER_DATE2));
        }
    }

    public void loadDistrBranches() {

        String distrCode = userSession.getDistrCode();
        popUpsBean.setDistrBrCodes(grnService.getDistrBrCodes(distrCode));
        List<DistributorBranchPopUp> selectedBranches = new ArrayList<>();
        if (popUpsBean.getSelectedBranches() == null || !popUpsBean.getSelectedBranches().isEmpty()) {
            popUpsBean.setSelectedBranches(new ArrayList<>());
            for (DistributorBranchPopUp distrBrPopup : popUpsBean.getDistrBrCodes()) {
                if (distrBrPopup.getCode().equalsIgnoreCase(userSession.getDistrBrCode())) {
                    selectedBranches.add(distrBrPopup);
                    popUpsBean.setSelectedBranches(selectedBranches);
                    form.setDistrBrCode(userSession.getDistrBrCode());
                }
            }
        }
        loadCompanies();
    }

    public void addCompanies() {
        loadCompanies();
    }

    public void loadCompanies() {
        List<CompanyPopUp> companyLists;
        companyLists = grnService.getCompany(popUpsBean.getSelectedBranches());
        popUpsBean.setCompanyPopUpList(companyLists);
        if (companyLists.size() == 1) {
            popUpsBean.setSelectedCompanies(companyLists);
        } else if (companyLists.isEmpty()) {
            form.setCmpCode("");
        }
        loadProductHierarchyLevel();
    }

    public void loadGodowns() {
        List<GodownPopUp> godownLists = grnService.getGodowns(popUpsBean.getSelectedBranches());
        popUpsBean.setGodownLists(godownLists);
    }

    public void loadProductHierarchyLevel() {
        List<ProductHierarchyLevelPopup> prodLevelMap = service.getProductLevels(userSession.getCmpCode());
        popUpsBean.setProdLevelPopUpList(prodLevelMap);
    }

    public void loadProductHierarchyValue() {
        if (popUpsBean.getSelctedprodLevelPopups() != null) {
            List<ProductHierarchyLevelValuePopup> prodValueMap = service
                    .getProductValue(popUpsBean.getSelctedprodLevelPopups());
            popUpsBean.setProdLevelValuePopUpList(prodValueMap);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("content:btnProductValue",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "select product level", "select product level"));
        }
    }

    public boolean getLobFlag() {
        return lobFlag;
    }

    public void setLobFlag(boolean lobFlag) {
        this.lobFlag = lobFlag;
    }

    public boolean isExportToExcel() {
        return exportToExcel;
    }

    public void setExportToExcel(boolean exportToExcel) {
        this.exportToExcel = exportToExcel;
    }

    public void setGenerateReportFlag(boolean generateReportFlag) {
        this.generateReportFlag = generateReportFlag;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public ICurrentStockWeightService getService() {
        return service;
    }

    public void setService(ICurrentStockWeightService service) {
        this.service = service;
    }

    public CurrentStockWeightReportForm getForm() {
        return form;
    }

    public void setForm(CurrentStockWeightReportForm form) {
        this.form = form;
    }

    public Map<String, String> getCompanies() {
        return companies;
    }

    public void setCompanies(Map<String, String> companies) {
        this.companies = companies;
    }

    public Map<String, String> getGodowns() {
        return godowns;
    }

    public void setGodowns(Map<String, String> godowns) {
        this.godowns = godowns;
    }

    public Map<Integer, String> getProductHierLevels() {
        return productHierLevels;
    }

    public void setProductHierLevels(Map<Integer, String> productHierLevels) {
        this.productHierLevels = productHierLevels;
    }

    public Map<Integer, String> getProductHierValues() {
        return productHierValues;
    }

    public void setProductHierValues(Map<Integer, String> productHierValues) {
        this.productHierValues = productHierValues;
    }

    public Map<String, String> getDistrBranches() {
        return distrBranches;
    }

    public void setDistrBranches(Map<String, String> distrBranches) {
        this.distrBranches = distrBranches;
    }

    public List<CurrentStockWeightReportList> getSearchList() {
        return searchList;
    }

    public void setSearchList(List<CurrentStockWeightReportList> searchList) {
        this.searchList = searchList;
    }

    /**
     * Generating the Reports in a CSV File Formats.
     */
    public void getCSVDownload() {
        if (validate()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Inside the generateReportForExcel() method");
            }
            popUpsBean.setDisplayBatchStatutsFlag("Y");

            if ("Y".equals(popUpsBean.getDisplayBatchStatutsFlag())) {
                if ("MRP".equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockReportmrpXLS";
                    Map<String, Object> map = getParameters();
                    salService.generateReportCSV(reportName, map);
                } else if (StringConstants.PURCHASE_RATE.equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockWeightReportXLS";
                    Map<String, Object> map = getParameters();
                    salService.generateReportCSV(reportName, map);

                } else if (StringConstants.PURCHASE_RATE_WITH_TAX.equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockReportXLSLPWBWT";
                    Map<String, Object> map = getParameters();
                    salService.generateReportCSV(reportName, map);
                } else if (StringConstants.SELLING_RATE.equals(popUpsBean.getStockValueasPer())) {
                    String reportName = "CurrentStockReportXLSSRWBWOT";
                    Map<String, Object> map = getParameters();
                    salService.generateReportCSV(reportName, map);
                } else {
                    String reportName = "CurrentStockReportXLSSRWBWT";
                    Map<String, Object> map = getParameters();
                    salService.generateReportCSV(reportName, map);
                }
            }
            this.setExportToExcel(false);
        }

    }

    public void getExternalCSVDownload() {
        if (validate()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Inside the generateReportForExcel() method");
            }
            popUpsBean.setDisplayBatchStatutsFlag("Y");

            if ("Y".equals(popUpsBean.getDisplayBatchStatutsFlag())) {
                LOG.info("Inside the generate Report For CSV method");
                Map<String, Object> map = new HashMap<>();
                List<String> distrList=new ArrayList<>();
                if ('Y' == appConfig.getEnableSubStockiestReportConfig() && !userSession.isCmpUser() && popUpsBean.isEnableSubDistrReportData()) {
        			distrList.add(popUpsBean.getSelectedDistr());
        		} else {
        			distrList = popUpsBean.getDistributorList();
        		}
                map.put("DISTR_BR_LST", popUpsBean.getBranchCodeList());
                map.put("DISTRIBUTOR_LST", distrList);
                map.put("GODOWN_LST", popUpsBean.getGodownList());
                map.put("GODOWN_AVL", popUpsBean.getGodownFlag());
                List<String> productHireValueList = getDefaultProducts();
                map.put("PRODUCT_HIER_VALUE_LIST", productHireValueList);
                map.put("PRODUCT_AVL", popUpsBean.getProdFlagForProdHierValues());
                if ("Y".equalsIgnoreCase(popUpsBean.getBatchStatus())) {
                    map.put(StringConstants.BATCH_STATUS_ACTIVE_STR, popUpsBean.getBatchStatus().charAt(0));
                    map.put(StringConstants.BATCH_STATUS_INACTIVE_STR, popUpsBean.getBatchStatus().charAt(0));
                } else if (StringConstants.N.equalsIgnoreCase(popUpsBean.getBatchStatus())) {
                    map.put(StringConstants.BATCH_STATUS_ACTIVE_STR, popUpsBean.getBatchStatus().charAt(0));
                    map.put(StringConstants.BATCH_STATUS_INACTIVE_STR, popUpsBean.getBatchStatus().charAt(0));
                } else if ("ALL".equalsIgnoreCase(popUpsBean.getBatchStatus())) {

                    map.put(StringConstants.BATCH_STATUS_ACTIVE_STR, 'Y');
                    map.put(StringConstants.BATCH_STATUS_INACTIVE_STR, 'N');
                }
                if ("Y".equalsIgnoreCase(popUpsBean.getProductStatus())) {
                    map.put(StringConstants.PRODUCT_STATUS_ACTIVE_STR, popUpsBean.getProductStatus().charAt(0));
                    map.put(StringConstants.PRODUCT_STATUS_INACTIVE_STR, popUpsBean.getProductStatus().charAt(0));
                } else if (StringConstants.N.equalsIgnoreCase(popUpsBean.getProductStatus())) {
                    map.put(StringConstants.PRODUCT_STATUS_ACTIVE_STR, popUpsBean.getProductStatus().charAt(0));
                    map.put(StringConstants.PRODUCT_STATUS_INACTIVE_STR, popUpsBean.getProductStatus().charAt(0));
                } else if ("ALL".equalsIgnoreCase(popUpsBean.getProductStatus())) {

                    map.put("PRODUCT_STATUS_ACTIVE_STR", 'Y');
                    map.put("PRODUCT_STATUS_INACTIVE_STR", 'N');
                }
                getLobCodeList(map);
                boolean geoProdFlag = false;
                if ('D' == appConfig.getProdBatchConfig()) {
                    geoProdFlag = true;
                }

                getGeoProdList(map, geoProdFlag,distrList);



                    // Setting this variables for the header column of the sheet
                    String[] headerNames = new String[] { StringConstants.EM_DISTRIBUTOR_CODE, "Distributor Name", "Distributor Br Code",
                            "Distributor Br Name", "Godown Name","HSN Code", "Product Code", "Product Description", "Batch","Expiry Date", "MRP",
                            "PurchPrice Without Tax","Cases" , "Pieces","Saleable Stock", "UnSaleable Stock", "Offer Stock",
                            "SaleableStockValue", "UnSaleableStockValue", "TotalStockValue","SaleableStock KG","UnSaleableStock KG","OfferStock KG","Total KG",
                            "Pack Size Name", "Product Type", "Ageing Days"};

                    // It get the values from the list.
                    String[] getterFields = new String[] { StringConstants.DISTR_CODE, "distrName", StringConstants.DISTRBRCODE, "distrBrName",
                            "godownName",StringConstants.HSNCODE, StringConstants.PROD_CODE, "prodName", "prodBatchCode","expiryDt", "mrp", "purPrice","cases","pieces", "saleableStock",
                            "unSaleableStock", "offerStock", "stockValueSaleable", "stockValueUnSaleable","totalStockValue",
                             "saleableStockKg","unSaleableStockKg","offerStockKg","totalKg", "prodHierValName", "gstprodType", "ageingDays"};

                    List<CurrentStockWeightModel> weightReportList = service.getCSVData(map);

                    CellProcessor[] processors = new CellProcessor[] {
                            new Optional(), // distrCode
                            new Optional(), //distrName
                            new Optional(), // distrBrCode
                            new Optional(), // distrBrName
                            new Optional(), // godownName
                            new Optional(), // hsnCode
                            new Optional(), // prodCode
                            new Optional(), // prodName
                            new Optional(), // prodBatchCode
                            new Optional(new FmtDate("dd/MM/yyyy")), // expiryDt
                            new Optional(), // mrp
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // purPrice
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // cases
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // pieces
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // saleableStock
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // unSaleableStock
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // offerStock
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // stockValueSaleable
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // stockValueUnSaleable
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // totalStockValue
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // saleableStockKg
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // unSaleableStockKg
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // offerStockKg
                            new Optional(new FmtNumber(new DecimalFormat("##0.00"))), // totalKg
                            new Optional(), // prodHierValName
                            new Optional(), // gstprodType
                            new Optional(), // ageingDays
                    };

                    List<Object> list = new ArrayList<>(weightReportList);
                    
                    long salesableStock = 0;
                    long unSalesableStock = 0;
                    long offerStock = 0;
                    double stockValueSaleable = 0;
                    double stockValueUnSaleable= 0;
                    double totalStockValue = 0;

                    for (CurrentStockWeightModel object : weightReportList) {                    	
                    	salesableStock = salesableStock + object.getSaleableStock();
                    	unSalesableStock = unSalesableStock + object.getUnSaleableStock();
                    	offerStock = offerStock + object.getOfferStock();
                    	stockValueSaleable = stockValueSaleable + object.getStockValueSaleable();
                    	stockValueUnSaleable = stockValueUnSaleable + object.getStockValueUnSaleable();
                    	totalStockValue = totalStockValue + object.getTotalStockValue();                        
                    }
                    
                    list.add(new CurrentStockWeightModel("Total","","","","","","","","",null,null,null,salesableStock,unSalesableStock,
                    		offerStock,stockValueSaleable,stockValueUnSaleable,totalStockValue,null,null,null,null));

                    CSVFileExporter.writeSuperCSVFile(headerNames, getterFields, "CurrentStockWeightReport", list, processors);
            }

            this.setExportToExcel(false);
        }
    }

    private void getGeoProdList(Map<String, Object> map, boolean geoProdFlag,List<String> distrList) {
        if (!popUpsBean.isCmpuser() && 'Y' != appConfig.getEnableSubStockiestReportConfig() ){

            if (geoProdFlag) {
                List<String> distrs = new ArrayList<>();
                distrs.add(userSession.getDistrCode());
                map.put(StringConstants.GEOPROD_LST, distrs);
            } else {
                map.put(StringConstants.GEOPROD_LST, userSession.getGeoPathList());
            }
        } else if(!popUpsBean.isCmpuser() && 'Y' == appConfig.getEnableSubStockiestReportConfig() && popUpsBean.isEnableSubDistrReportData() ){
        	 if (geoProdFlag) {
                 map.put(StringConstants.GEOPROD_LST, distrList);
             } else {
                 List<String> getGeoPathList = popUpsBean
                         .getGeoPathListForSelectedDistr(distrList);
                 map.put(StringConstants.GEOPROD_LST, getGeoPathList);
             }
        }else {
            if (geoProdFlag) {
                map.put(StringConstants.GEOPROD_LST, popUpsBean.getDistributorList());
            } else {
                List<String> getGeoPathList = popUpsBean
                        .getGeoPathListForSelectedDistr(popUpsBean.getDistributorList());
                map.put(StringConstants.GEOPROD_LST, getGeoPathList);
            }
           
        }
    }

    private void getLobCodeList(Map<String, Object> map) {
        if (userSession.isCmpUser()) {
            List<String> lobCodes = new ArrayList<>();
            for (LobValuePopup lob : distrSelectionBean.getLobPopupBean().getSelectedLobValueList()) {
                lobCodes.add(lob.getCode());
            }
            map.put("LOBCODE_LIST", lobCodes);
        } else {
            List<String> lobCodes = new ArrayList<>();
            for (LobValuePopup lob : lobPopupBean.getSelectedLobValueList()) {
                lobCodes.add(lob.getCode());
            }
            map.put("LOBCODE_LIST", lobCodes);
        }
    }


	public Map<String, String> getSubDistrCodes() {
		return subDistrCodes;
	}

	public void setSubDistrCodes(Map<String, String> subDistrCodes) {
		this.subDistrCodes = subDistrCodes;
	}

	public String getSelectedDistr() {
		return selectedDistr;
	}

	public void setSelectedDistr(String selectedDistr) {
		this.selectedDistr = selectedDistr;
	}

	public String getDistrCode() {
		return distrCode;
	}

	public void setDistrCode(String distrCode) {
		this.distrCode = distrCode;
	}

}
