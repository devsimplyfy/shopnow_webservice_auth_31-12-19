package com.shopNow.Lambda;

/*
// Copyright 2013-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
 */

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClientBuilder;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import org.json.JSONObject;
import com.shopNow.Lambda.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * The CognitoHelper class abstracts the functionality of connecting to the Cognito user pool and Federated Identities.
 */
class CognitoHelper {
    private String POOL_ID;
    private String CLIENTAPP_ID;
    private String FED_POOL_ID;
    private String CUSTOMDOMAIN;
    private String REGION;

    public CognitoHelper() {

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = getClass().getClassLoader().getResourceAsStream("config.properties");

            // load a properties file
            prop.load(getClass().getResourceAsStream("/config.properties"));
            //prop.load(input);

            // Read the property values
            POOL_ID = prop.getProperty("POOL_ID");
            CLIENTAPP_ID = prop.getProperty("CLIENTAPP_ID");
            FED_POOL_ID = prop.getProperty("FED_POOL_ID");
            CUSTOMDOMAIN = prop.getProperty("CUSTOMDOMAIN");
            REGION = prop.getProperty("REGION");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String GetHostedSignInURL() {
        String customurl = "https://%s.auth.%s.amazoncognito.com/login?response_type=code&client_id=%s&redirect_uri=%s";

        return String.format(customurl, CUSTOMDOMAIN, REGION, CLIENTAPP_ID, Constants.REDIRECT_URL);
    }

    String GetTokenURL() {
        String customurl = "https://%s.auth.%s.amazoncognito.com/oauth2/token";

        return String.format(customurl, CUSTOMDOMAIN, REGION);
    }


    /**
     * Verify the verification code sent on the user phone.
     *
     * @param username User for which we are submitting the verification code.
     * @param code     Verification code delivered to the user.
     * @return if the verification is successful.
     */
    String VerifyAccessCode(String username, String code) {
        String msg =null;
    	
    	AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
        AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(REGION))
                .build();

        ConfirmSignUpRequest confirmSignUpRequest = new ConfirmSignUpRequest();
        confirmSignUpRequest.setUsername(username);
        confirmSignUpRequest.setConfirmationCode(code);
        confirmSignUpRequest.setClientId(CLIENTAPP_ID);

        /*System.out.println("username=" + username);
        System.out.println("code=" + code);
        System.out.println("clientid=" + CLIENTAPP_ID);*/

        try {
            ConfirmSignUpResult confirmSignUpResult = cognitoIdentityProvider.confirmSignUp(confirmSignUpRequest);
            msg = "SUCCESSFUL";
            System.out.println("confirmSignupResult=" + confirmSignUpResult.toString());
        } catch (Exception ex) {
            System.out.println(ex);
            msg = ex.toString();
            //return false;
        }
        return msg;
    }

      
}
