package com.apas.DataProviders;

import org.testng.annotations.DataProvider;

import com.apas.PageObjects.CIOTransferPage;
import com.apas.config.testdata;
import com.apas.config.users;

public class DataProviders {
	
	/**
	 * Below function will be used to login to application with principal user
	 * @returns: Returns the principal user
	 **/
    @DataProvider(name = "loginPrincipalUser")
    public Object[][] dpLoginPrincipalUser() {
        return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with business administrator user
	 * @returns: Return the user business administrator
	 **/
    @DataProvider(name = "loginBusinessAdmin")
    public Object[][] dpLoginBusinessUser() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }


    /**
     * Below function will be used to login to application with BPP business administrator user
     * @returns: Return the user BPP business administrator
     **/
    @DataProvider(name = "loginBPPBusinessAdmin")
    public Object[][] dpLoginBPPBusinessAdminUser() {
        return new Object[][] { { users.BPP_BUSINESS_ADMIN } };
    }

    /**
	 * Below function will be used to login to application with appraisal support user
	 * @returns: Return the user appraisal support
	 **/
    @DataProvider(name = "loginApraisalUser")
    public Object[][] dpLoginApraisalUser() {
        return new Object[][] { { users.APPRAISAL_SUPPORT } };
    }

	/**
	 * Below function will be used to login to application with system administrator user
	 *
	 * @returns: Return the user system administrator
	 **/
    @DataProvider(name = "loginSystemAdmin")
    public Object[][] dpLoginSystemAdmin() {
        return new Object[][] { { users.SYSTEM_ADMIN } };
    }

	/**
	 * Below function will be used to login to application with bpp auditor / appraiser user
	 * @returns: Return the user bpp auditor / appraiser
	 **/
    @DataProvider(name = "loginBppAuditor")
    public Object[][] dpLoginBppAuditor() {
        return new Object[][] { { users.BPP_AUDITOR } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user business administrator and principal user in an array
	 **/
    @DataProvider(name = "loginBusinessAndPrincipalUsers")
    public Object[][] dpLoginBusinessAndPrincipalUsers() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.PRINCIPAL_USER } };
        else
            return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 * @return Return the user business administrator and appraisal support in an array
	 **/
    @DataProvider(name = "loginBusinessAndAppraisalUsers")
    public Object[][] dpLoginBusinessAndAppraisalUsers() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.APPRAISAL_SUPPORT } };
        else
            return new Object[][] { { users.BUSINESS_ADMIN }, { users.APPRAISAL_SUPPORT } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user business administrator, principal user and Bpp Auditor in an array
	 **/
    @DataProvider(name = "loginBusinessAdminAndPrincipalUserAndBppAuditor")
    public Object[][] dpLoginBusinessAdminAndPrincipalUserAndBppAuditor() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.BPP_AUDITOR } };
        else
            return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER }, { users.BPP_AUDITOR } };

    }
    
	/**
	 * Below function will be used to upload files with different invalid formats
	 * @returns: Returns the names of invalid files
	 **/
    @DataProvider(name = "invalidFileTypes")
    public Object[][] dpInvalidFieTypes() {
        return new Object[][] { { testdata.BPP_TREND_BOE_INDEX_FACTORS_CSV }, 
        						{ testdata.BPP_TREND_BOE_INDEX_FACTORS_TXT }, 
        						{ testdata.BPP_TREND_BOE_INDEX_FACTORS_XLS } };
    }
    
    // ************** Below Users Are For Security And Sharing Of BPP Trends *************
    
	/**
	 * Returns users allowed to access Calculate and Calculate All button
	 **/
    @DataProvider(name = "usersAllowedToCalculate")
    public Object[][] dpUsersAllowedToCalculate() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }
    
	/**
	 * Returns users not allowed to access Calculate and Calculate All button
	 **/

    @DataProvider(name = "usersRestrictedToCalculate")
    public Object[][] dpUsersRestrictedToCalculate() {
    	return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Returns users allowed to access ReCalculate and ReCalculate All button
	 **/
    @DataProvider(name = "usersAllowedToReCalculate")
    public Object[][] dpUsersAllowedToReCalculate() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }
    
	/**
	 * Returns users not allowed to access ReCalculate and ReCalculate All button
	 **/
    @DataProvider(name = "usersRestrictedToReCalculate")
    public Object[][] dpUsersRestrictedToReCalculate() {
    	return new Object[][] { { users.PRINCIPAL_USER }};
    }
    
	/**
	 * Returns users allowed to access Submit Factor and Submit All Factors For Approval button
	 **/
    @DataProvider(name = "usersAllowedToSubmitAllFactors")
    public Object[][] dpUsersAllowedToSubmitAllFactors() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }
    
	/**
	 * Returns users not allowed to access Submit Factor and Submit All Factors For Approval button
	 **/
    @DataProvider(name = "usersRestrictedToSubmitAllFactors")
    public Object[][] dpUsersRestrictedToSubmitAllFactors() {
    	//return new Object[][] { { users.PRINCIPAL_USER }, { users.RP_APPRAISER }, { users.BPP_AUDITOR } };
    	return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Returns users allowed to access Approve and Approve All button
	 **/
    @DataProvider(name = "usersAllowedToApprove")
    public Object[][] dpUsersAllowedToApprove() {
        return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Returns users not allowed to access Approve and Approve All button
	 **/
    @DataProvider(name = "usersRestrictedToApprove")
    public Object[][] dpUsersRestrictedToApprove() {
        return new Object[][] { { users.BUSINESS_ADMIN }};
    }

	/**
	 * Returns users allowed to access Download button
	 **/
    @DataProvider(name = "usersAllowedToDownloadPdfFile")
    public Object[][] dpUsersAllowedToDownloadPdfFile() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.BUSINESS_ADMIN }};
        else
            return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER }  };
    }
    
	/**
	 * Returns users not allowed to access Download button
	 **/
    @DataProvider(name = "usersRestrictedToDownloadPdfFile")
    public Object[][] dpUsersRestrictedToDownloadPdfFile() {
        return new Object[][] {  };
    }
    
	/**
	 * Returns users allowed to access Export Composite Factors button
	 **/
    @DataProvider(name = "usersAllowedToExportCompFactorsFile")
    public Object[][] dpUsersAllowedToExportCompFactorsFile() {
        return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Returns users not allowed to access Export Composite Factors button
	 **/
    @DataProvider(name = "usersRestrictedToExportCompFactorsFile")
    public Object[][] dpUsersRestrictedToExportCompFactorsFile() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.RP_APPRAISER }, { users.BPP_AUDITOR } };
    }
    
	/**
	 * Returns users allowed to access Export Valuation Factors button
	 **/
    @DataProvider(name = "usersAllowedToExportValFactorsFile")
    public Object[][] dpUsersAllowedToExportValFactorsFile() {
        return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Returns users not allowed to access Export Valuation Factors button
	 **/
    @DataProvider(name = "usersRestrictedToExportValFactorsFile")
    public Object[][] dpUsersRestrictedToExportValFactorsFile() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }

	/**
	 * Returns users not allowed to access edit pencil icon on BPP Trend Setup page
	 **/
    @DataProvider(name = "usersRestrictedEditTableStatusOnBppTrendPage")
    public Object[][] dpUsersRestrictedEditTableStatusOnBppTrendPage() {
        return new Object[][] { { users.PRINCIPAL_USER }, { users.BUSINESS_ADMIN } };
    }
    
	/**
	 * Returns users not allowed to access edit and delete option for maximum equipment index factor
	 **/
    @DataProvider(name = "usersRestrictedToModifyMaxEquipIndexFactor")
    public Object[][] dpUsersRestrictedToModifyMaxEquipIndexFactor() {
    	return new Object[][] { { users.PRINCIPAL_USER } };
    }

    @DataProvider(name = "loginUsers")
    public Object[][] dpLoginUser() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }
    
    @DataProvider(name = "loginExemptionSupportStaff")
	public Object[][] dataProviderLoginUserMethod() {
		return new Object[][] { { users.EXEMPTION_SUPPORT_STAFF } };
	}
    
    
	@DataProvider(name = "rpApprasierAndBPPAuditor")
	public Object[][] dataProviderLoginUserMethodForUser() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
		    return new Object[][] { { users.BPP_AUDITOR } };
        else
            return new Object[][] { { users.RP_APPRAISER }, { users.BPP_AUDITOR } };
	}

    /**
     * Return RP Appraiser User
     */
    @DataProvider(name = "RPAppraiser")
    public Object[][] dataProviderLoginRPAppraiser() {
        return new Object[][] { { users.RP_APPRAISER }};
    }
    /**
     * Return RP Principal User
     */
    @DataProvider(name = "rpPrincipal")
    public Object[][] dataProviderLoginRPPrincipal() {
        return new Object[][] { { users.RP_PRINCIPAL }};
    }
    /**
	 * Return different status of composite and valuation factor tables
	 */
    @DataProvider(name = "variousStatusOfCompositeAndValuationTables")
    public Object[][] dpVariousStatusOfCompositeAndValuationTables() {
        return new Object[][]{{"Not Calculated"}, {"Calculated"}, {"Needs Recalculation"}, {"Submitted for Approval"}, {"Approved"}};
    }

    @DataProvider(name = "BPPAuditorAndPrincipal")
    public Object[][] dataBPPAuditorAndPricipalUser() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.PRINCIPAL_USER }};
        else
            return new Object[][] { { users.PRINCIPAL_USER }, { users.BPP_AUDITOR }};
    }
    
    
	/**
	 * Return different status of composite and valuation factor tables post submitting calculations
	 */
    @DataProvider(name = "variousStatusOfTablesPostSubmittingCalculations")
    public Object[][] dpVariousStatusOfTablesPostSubmittingCalculations() {
    	return new Object[][] { { "Submitted for Approval" }, { "Approved" } };
    }
    
    @DataProvider(name = "variousStatusOfCompositeTablesBeforeSubmitting")
    public Object[][] dpVariousStatusOfCompositeTables() {
    	return new Object[][] { { "Not Calculated" }, { "Calculated" }, {"Needs Recalculation"} };
    }

    
    @DataProvider(name = "variousStatusOfValuationTablesBeforeSubmitting")
    public Object[][] dpVariousStatusOfValuationTables() {
    	return new Object[][] { { "Yet to submit for Approval" }, { "Import Approved" } };
    }
           
    
	/**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user BPP business administrator and RP business administrator users in an array
	 **/
    @DataProvider(name = "loginBppAndRpBusinessAdminUsers")
    public Object[][] dpLoginBppAndRpBusinessAdminUsers() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.RP_BUSINESS_ADMIN }};
        else
            return new Object[][] { { users.RP_BUSINESS_ADMIN }, { users.BUSINESS_ADMIN } };
    }
 
	/**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user BPP business administrator, RP business administrator and Principal users in an array
	 **/
    @DataProvider(name = "loginBppAndRpBusinessAdminAndPrincipalUsers")
    public Object[][] dpLoginBppAndRpBusinessAdminAndPrincipalUsers() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.PRINCIPAL_USER } };
        else
            return new Object[][] { { users.RP_BUSINESS_ADMIN }, { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user BPP RP business administrator and Principal users in an array
	 **/
    @DataProvider(name = "loginRpBusinessAdminAndPrincipalUsers")
    public Object[][] dpLoginRpBusinessAdminAndPrincipalUsers() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.RP_BUSINESS_ADMIN }};
        else
            return new Object[][] { { users.RP_BUSINESS_ADMIN }, { users.PRINCIPAL_USER } };
    }
    
    /**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user RP Business Admin and Exemption Support Staff users in an array
	 **/
    @DataProvider(name = "loginRpBusinessAdminAndExemptionSupportUsers")
    public Object[][] dpLoginRpBusinessAdminAndExemptionSupportUsers() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.EXEMPTION_SUPPORT_STAFF } };
        else
            return new Object[][] { { users.RP_BUSINESS_ADMIN }, { users.EXEMPTION_SUPPORT_STAFF } };
    }
    
    
    @DataProvider(name = "loginRPBusinessAdmin")
    public Object[][] rpBusinessAdmin() {
        return new Object[][] {{ users.RP_BUSINESS_ADMIN }};
    }
    
    
    @DataProvider(name = "DVworkPoolSuperviosrUser")
    public Object[][] workPoolSupervisor() {
        return new Object[][] {{ users.DATA_ADMIN }};
    }
    
    @DataProvider(name = "loginMappingUser")
    public Object[][] mappingUser() {
        return new Object[][] {{ users.MAPPING_STAFF }};
    }
    
    @DataProvider(name = "Condo_MobileHome_Parcels")
    public Object[][] condoMobileHomeParcels() {
		return new Object[][]
		{
            { users.MAPPING_STAFF , "Condo_Parcel"},
            { users.MAPPING_STAFF , "Mobile_Home_Parcel" }
        };
    }
    
    /**
   	 * Below function will be used to login to application with CIO Supervisor
   	 * @returns: Return the user CIO Supervisor
   	 **/
    @DataProvider(name = "loginCIOSupervisor")
    public Object[][] dpCIOSupervisor() {
        return new Object[][] { { users.CIO_SUPERVISOR } };
    }
    
    /**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user business administrator, principal user and Bpp Auditor in an array
	 **/
    @DataProvider(name = "loginSystemAdminAndMappingStaffAndMappingSupervisor")
    public Object[][] dpLoginSystemAdminAndMappingstaffuser() {
        if (System.getProperty("testSuite") != null && System.getProperty("testSuite").equals("smoke"))
            return new Object[][] { { users.MAPPING_STAFF } };
        else
            return new Object[][] { { users.MAPPING_STAFF}, { users.SYSTEM_ADMIN }, { users.MAPPING_SUPERVISOR } };

    }
       
    /**
   	 * Below function will be used to login to application with CIO Staff
   	 * @returns: Return the user CIO Staff
   	 **/
       @DataProvider(name = "loginCIOStaff")
       public Object[][] dpCIOStaffUser() {
           return new Object[][] { { users.CIO_STAFF } };
       }
       
		@DataProvider(name = "dpForCioAutoConfirm")
		public Object[][] dpTestCioAutoConfirm() {
			return new Object[][] { { CIOTransferPage.CIO_EVENT_CODE_COPAL, CIOTransferPage.CIO_EVENT_CODE_PART, CIOTransferPage.CIO_RESPONSE_NoChangeRequired },
					{ CIOTransferPage.CIO_EVENT_CODE_COPAL, CIOTransferPage.CIO_EVENT_CODE_PART, CIOTransferPage.CIO_RESPONS_EventCodeChangeRequired }

			};
		}
       
		@DataProvider(name = "dpForCioAutoConfirmUsingBatchJob")
		public Object[][] dpTestCioAutoConfirmUsingBatchJob() {
			return new Object[][] { { CIOTransferPage.CIO_EVENT_CODE_COPAL, CIOTransferPage.CIO_EVENT_CODE_PART },
					{ CIOTransferPage.CIO_EVENT_CODE_BASE_YEAR_TRANSFER, CIOTransferPage.CIO_EVENT_CODE_BASE_YEAR_AUTOCONFIRM_CODE}

			};
		}
		@DataProvider(name = "usersRestrictedToNewandEditButtonOnParcelMailto")
	    public Object[][] validateUsersToNotHaveEditpermissionsOrCreateNewonMailToParcel() {
	        return new Object[][] { { users.BPP_BUSINESS_ADMIN}, { users.RP_APPRAISER },{ users.CIO_STAFF }, { users.MAPPING_STAFF},{ users.SYSTEM_ADMIN} 
	        };
	        
		}

       
}
       

