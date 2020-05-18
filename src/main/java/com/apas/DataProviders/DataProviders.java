package com.apas.DataProviders;

import org.testng.annotations.DataProvider;

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
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 * @return Return the user business administrator and appraisal support in an array
	 **/
    @DataProvider(name = "loginBusinessAndAppraisalUsers")
    public Object[][] dpLoginBusinessAndAppraisalUsers() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.APPRAISAL_SUPPORT } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 * @returns: Return the user business administrator, principal user and Bpp Auditor in an array
	 **/
    @DataProvider(name = "loginBusinessAdminAndPrincipalUserAndBppAuditor")
    public Object[][] dpLoginBusinessAdminAndPrincipalUserAndBppAuditor() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER }, { users.BPP_AUDITOR } };
    }
    
<<<<<<< HEAD
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
        //return new Object[][] { { users.PRINCIPAL_USER }, { users.RP_APPRAISER }, { users.BPP_AUDITOR } };
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
    	//return new Object[][] { { users.PRINCIPAL_USER }, { users.RP_APPRAISER }, { users.BPP_AUDITOR } };
    	return new Object[][] { { users.PRINCIPAL_USER } };
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
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }

	/**
	 * Returns users allowed to access Download button
	 **/
    @DataProvider(name = "usersAllowedToDownloadPdfFile")
    public Object[][] dpUsersAllowedToDownloadPdfFile() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER } };
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
        return new Object[][] { { users.BUSINESS_ADMIN } };
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
        //return new Object[][] { { users.PRINCIPAL_USER }, { users.BPP_AUDITOR }, { users.BUSINESS_ADMIN } };
    	return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Returns users not allowed to access edit and delete option for maximum equipment index factor
	 **/
    @DataProvider(name = "usersRestrictedToModifyMaxEquipIndexFactor")
    public Object[][] dpUsersRestrictedToModifyMaxEquipIndexFactor() {
        //return new Object[][] { { users.PRINCIPAL_USER }, { users.RP_APPRAISER }, { users.BPP_AUDITOR } };
    	return new Object[][] { { users.PRINCIPAL_USER } };
    }
=======
    @DataProvider(name = "loginUsers")
    public Object[][] dpLoginUser() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }
>>>>>>> 66dc6865b76a397c2e503581b4d81c8239bc86b8
}
