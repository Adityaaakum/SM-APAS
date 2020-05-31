package com.apas.DataProviders;

import org.testng.annotations.DataProvider;

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
		return new Object[][] { { users.RP_APPRAISER }, { users.BPP_AUDITOR }};
	}
    
}
