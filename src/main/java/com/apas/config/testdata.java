package com.apas.config;

public interface testdata {

	String DOWNLOAD_FOLDER = System.getProperty("user.dir") + "\\downloads";

	String userDirectory = System.getProperty("user.dir");

	//List of test data files used in different test cases
	String BUILDING_PERMIT_ATHERTON = "\\data\\BuildingPermit\\AthertonBuildingPermits\\";
	String BUILDING_PERMIT_SAN_MATEO = "\\data\\BuildingPermit\\SanMateoBuildingPermits\\";
	String BUILDING_PERMIT_BURLINGAME = "\\data\\BuildingPermit\\BurlingamePermits\\";
	String BUILDING_PERMIT_BELMONT = "\\data\\BuildingPermit\\BelmontPermits\\";
	String BUILDING_PERMIT_MANUAL = "\\data\\BuildingPermit\\ManualBuildingPermits\\";
	String BUILDING_PERMIT_UNINCORPORATED = "\\data\\BuildingPermit\\UnicorporatedBuildingPermits\\";
	String BPP_TREND_TABLES_DATA = "\\data\\BppTrend\\2020_Trend_Factors_Calculator.xlsx";
	
	String RPSL_ENTRY_DATA = "\\data\\DisabledVeteran\\RPSL\\RealPropertySettings.json";
	String ROLL_YEAR_DATA = "\\data\\RollYear\\RollYear_DataToCreateRollYearRecord.json";
	String EXEMPTION_MANDATORY_FIELDS_ENTRY_DATA = "\\data\\DisabledVeteran\\DisabledVeteran_DataToCreateExemptionRecord.json";

	String BUILDING_PERMIT_NEW_MANUAL_ENTRY_DATA = "\\data\\BuildingPermit\\SanMateoBuildingPermits\\BuildingPermit_DataToCreateManualEntry.txt";
	String BUILDING_PERMIT_MANUAL_ENTRY_DATA = "\\data\\BuildingPermit\\SanMateoBuildingPermits\\BuildingPermit_ManualEntryData.json";
	String BPP_TREND_CALCULATOR = "\\data\\BppTrend\\Valid_Import_Files\\";
	String BPP_TREND_BOE_INDEX_FACTORS = "\\data\\BppTrend\\Error_Record_Files\\";
	String BPP_TREND_CAA_VALUATION_FACTORS = "\\data\\BppTrend\\Error_Record_Files\\";
	String BPP_TREND_BOE_VALUATION_FACTORS = "\\data\\BppTrend\\Error_Record_Files\\";
	String BPP_TREND_DATA = "\\data\\BppTrend\\BppTrendSetupAndBppSettingData.json";
	String REMAP_MAPPING_ACTION = "\\data\\MappingActions\\RemapMappingAction.json";
	
	String BPP_TREND_BOE_INDEX_FACTORS_CSV = "\\data\\BppTrend\\Invalid_Format_Files\\BOE Equipment Index Factors and Percent Good Factors Sample_CSV_Format.csv";
	String BPP_TREND_BOE_INDEX_FACTORS_TXT = "\\data\\BppTrend\\Invalid_Format_Files\\BOE Equipment Index Factors and Percent Good Factors Sample_TXT_Format.txt";
	String BPP_TREND_BOE_INDEX_FACTORS_XLS = "\\data\\BppTrend\\Invalid_Format_Files\\BOE Equipment Index Factors and Percent Good Factors Sample_XLS_Format.xls";
	
	String BPP_TREND_BOE_INDEX_FACTORS_TRANSFORMATION_RULES= "\\data\\BppTrend\\Transformation_Validation_Files\\";
	String BPP_TREND_BOE_VALUATION_FACTORS_TRANSFORMATION_RULES= "\\data\\BppTrend\\Transformation_Validation_Files\\BOE Valuation Factors 2020_Transformation Rules.xlsx";
	String BPP_TREND_CAA_VALUATION_FACTORS_TRANSFORMATION_RULES= "\\data\\BppTrend\\Transformation_Validation_Files\\CAA Valuation Factors 2020_Transformation Rules.xlsx";
	String BPP_TREND_CALCULATOR_WITH_UPDATED_MIN_EQIP_INDEX_FACTOR = "\\data\\BppTrend\\Updated_Trend_Calculator_For_ReCalculation\\";
    
    String EXEMPTION_DATA = "\\data\\Exemptions\\ExemptionData.json";
    String ANNUAL_PROCESS_DATA = "\\data\\DisabledVeteran\\AnnualProcess\\DisabledVeteran_AnnualProcessData.json";

    String EXEMPTION_REPORT_DATA = "\\data\\DisabledVeteran\\DVExemptionReport\\DisabledVeteran_DVExemptionReport.json";


    String EFILEIMPORT_INVALIDDATA = "\\data\\EFileImportInvalidFiles\\";
    String EFILEIMPORT_BPPTRENDSDATA = "\\data\\EFileImportBppTrend\\";
    String EFILEIMPORT_BPDATA = "\\data\\EFileImportBP\\";

    String COUNTY_STRAT_CODES = "\\data\\CountyStratCodes";
    String BOE_ERRORREOCRDS_COUNT = "\\data\\EFileImportBppTrend\\";
    String BPP_TREND_BOE_VALUATION_FACTORS_VALID_DATA = "\\data\\EFileImportBppTrend\\";
    String LOAD_Test = "\\data\\LoadTest";
    String BPP_TREND_BOE_INDEX_FACTORS_VALID = "\\data\\BppTrend\\Valid_Import_Files\\";
    String BPP_TREND_BOE_VAL_FACTORS_VALID = "\\data\\BppTrend\\Valid_Import_Files\\";
    String BPP_TREND_CAA_VAL_FACTORS_VALID = "\\data\\BppTrend\\Valid_Import_Files\\";
    String MANUAL_WORK_ITEMS = "\\data\\WorkItems\\ManualWorkItem.json";
	String MANUAL_WORK_ITEMS_BPP_ACCOUNTS = userDirectory + "\\data\\WorkItems\\ManualWorkItemBPPAccount.json";
    String BPP_TREND_COMPOSITE_FACTORS_DATA = "\\data\\BppTrend\\BPPTrends_CompositeFactorsData.json";
	String WORK_ITEMS_ROUTING_SETUP = userDirectory + "\\data\\WorkItems\\WorkItemsRoutingSetup.json";
    String ONE_TO_ONE_MAPPING_ACTION = "\\data\\MappingActions\\OneToOneMapping.json";
    String RETIRE_ACTION = userDirectory + "\\data\\MappingActions\\RetireAction.json";

}