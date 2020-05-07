package com.apas.generic;

import com.apas.config.users;
import org.testng.annotations.DataProvider;

public class DataProviders {

    /**
     * Below function will be used to login to application with different users
     *
     * @return Return the user business admin and appraisal support in an array
     **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.BUSINESS_ADMIN }};
    }

}