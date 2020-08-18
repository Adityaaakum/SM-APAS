package com.apas.config;

public interface BPPTablesData {
	
	//List of tables on BPP Trends Module
	public static final String COMPOSITE_TABLES_API_NAMES = "Commercial_Trends_Status__c,Const_Mobile_Equipment_Trends_Status__c,Industrial_Trend_Status__c,Ag_Mobile_Equipment_Trends_Status__c,Const_Trends_Status__c,Ag_Trends_Status__c,Prop_13_Factor_Status_New__c";
	public static final String VALUATION_TABLES_API_NAMES = "Computer_Trends_Status__c,Biopharmaceutical_Trends_Status__c,Copier_Trends_Status__c,Semiconductor_Trends_Status__c,Litho_Trends_Status__c,Mechanical_Slot_Machine_Trends_Status__c,Set_Top_Box_Trends_Status__c,Electronic_Slot_Machine_Trends_Status__c";

	public static final String EFILE_IMPORT_PAGE_BOE_INDEX_TABLES_NAMES = "Commercial Equipment Index,Machinery and Equipment Index,Agricultural Index,Construction Index,M&E Good Factors,Construction ME Good Factors,Agricultural ME Good Factors";
	public static final String EFILE_IMPORT_PAGE_BOE_VAL_TABLES_NAMES = "Computer Val Factors,Semiconductor Val Factors,Biopharmaceutical Val Factors,Copier Val Factors,Litho Val Factors";
	public static final String EFILE_IMPORT_PAGE_CAA_VAL_TABLES_NAMES = "Set-Top Box Val Factors,Elec. Slot Machines Val Factors,Mech. Slot Machines Val Factors";
	
	String COMPOSITE_TABLES = "Commercial Composite Factors,Industrial Composite Factors,Agricultural Composite Factors,Construction Composite Factors,Agricultural Mobile Equipment Composite Factors,Construction Mobile Equipment Composite Factors,BPP Prop 13 Factors";
	String VALUATION_TABLES = "Computer Valuation Factors,Semiconductor Valuation Factors,Biopharmaceutical Valuation Factors,Copier Valuation Factors,Litho Valuation Factors,Set-Top Box Valuation Factors,Electrical Slot Machines Valuation Factors,Mechanical Slot Machines Valuation Factors";
	String BPP_TREND_TABLES = COMPOSITE_TABLES + "," + VALUATION_TABLES;

	String BPP_TREND_SETUP_FACTOR_STATUS_FIELDS = "Commercial Trends Status,Industrial Trend Status,Const. Trends Status,Const. Mobile Equipment Trends Status,Ag. Mobile Equipment Trends Status,Ag. Trends Status,Prop 13 Factor Status,Computer Trends Status,Biopharmaceutical Trends Status,Copier Trends Status,Semiconductor Trends Status,Litho Trends Status,Mechanical Slot Machine Trends Status,Set-Top Box Trends Status,Electronic Slot Machine Trends Status";

}