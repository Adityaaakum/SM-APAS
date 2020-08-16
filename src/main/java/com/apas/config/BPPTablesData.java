package com.apas.config;

public interface BPPTablesData {
	
	//List of tables on BPP Trends Module
	public static final String COMPOSITE_TABLES_API_NAMES = "Commercial_Trends_Status__c,Const_Mobile_Equipment_Trends_Status__c,Industrial_Trend_Status__c,Ag_Mobile_Equipment_Trends_Status__c,Const_Trends_Status__c,Ag_Trends_Status__c,Prop_13_Factor_Status_New__c";
	public static final String VALUATION_TABLES_API_NAMES = "Computer_Trends_Status__c,Biopharmaceutical_Trends_Status__c,Copier_Trends_Status__c,Semiconductor_Trends_Status__c,Litho_Trends_Status__c,Mechanical_Slot_Machine_Trends_Status__c,Set_Top_Box_Trends_Status__c,Electronic_Slot_Machine_Trends_Status__c";
	public static final String EFILE_IMPORT_PAGE_BOE_INDEX_TABLES_NAMES = "Commercial Equipment Index,Machinery and Equipment Index,Agricultural Index,Construction Index,M&E Good Factors,Construction ME Good Factors,Agricultural ME Good Factors";
	public static final String EFILE_IMPORT_PAGE_BOE_VAL_TABLES_NAMES = "Computer Val Factors,Semiconductor Val Factors,Biopharmaceutical Val Factors,Copier Val Factors,Litho Val Factors";
	public static final String EFILE_IMPORT_PAGE_CAA_VAL_TABLES_NAMES = "Set-Top Box Val Factors,Elec. Slot Machines Val Factors,Mech. Slot Machines Val Factors";
}