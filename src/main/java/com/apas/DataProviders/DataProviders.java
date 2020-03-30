package com.apas.DataProviders;

import org.testng.annotations.DataProvider;

import com.apas.config.users;

public class DataProviders {
	
	/**
	 * Below function will be used to login to application with principal user
	 *
	 * @returns: Returns the principal user
	 **/
    @DataProvider(name = "loginPrincipalUser")
    public Object[][] dpLoginPrincipalUser() {
        return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with business administrator user
	 *
	 * @returns: Return the user business administrator
	 **/
    @DataProvider(name = "loginBusinessAdmin")
    public Object[][] dpLoginBusinessUser() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }

	/**
	 * Below function will be used to login to application with business administrator user
	 *
	 * @returns: Return the user appraisal support
	 **/
    @DataProvider(name = "loginApraisalUser")
    public Object[][] dpLoginApraisalUser() {
        return new Object[][] { { users.APPRAISAL_SUPPORT } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @returns: Return the user business administrator and principal user in an array
	 **/
    @DataProvider(name = "loginBusinessAndPrincipalUsers")
    public Object[][] dpLoginBusinessAndPrincipalUsers() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business administrator and appraisal support in an array
	 **/
    @DataProvider(name = "loginBusinessAndAppraisalUsers")
    public Object[][] dpLoginBusinessAndAppraisalUsers() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.APPRAISAL_SUPPORT } };
    }
}
