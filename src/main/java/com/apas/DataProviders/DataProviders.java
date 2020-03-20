package com.apas.DataProviders;

import org.testng.annotations.DataProvider;

import com.apas.config.users;

public class DataProviders {
	
	/**
	 * Below function will be used to login to application with principa user
	 *
	 * @returns: Returns the principal user
	 **/
    @DataProvider(name = "loginPrincipalUser")
    public Object[][] dpLoginPrincipalUser() {
        return new Object[][] { { users.PRINCIPAL_USER } };
    }
    
	/**
	 * Below function will be used to login to application with business admin user
	 *
	 * @returns: Return the user business admin
	 **/
    @DataProvider(name = "loginBusinessAdmin")
    public Object[][] dpLoginBusinessUser() {
        return new Object[][] { { users.BUSINESS_ADMIN } };
    }
    
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @returns: Return the user business admin and appraisal support in an array
	 **/
    @DataProvider(name = "loginBusinessAndPrincipalUsers")
    public Object[][] dpLoginBusinessAndPrincipalUsers() {
        return new Object[][] { { users.PRINCIPAL_USER }, { users.BUSINESS_ADMIN } };
    }
}
