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
            input = getClass().getClassLoader().getResourceAsStream("/config.properties");        	
            // load a properties file
            prop.load(getClass().getResourceAsStream("/config.properties"));
           // prop.load(input);

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
     * Helper method to validate the user
     *
     * @param username represents the username in the cognito user pool
     * @param password represents the password in the cognito user pool
     * @return returns the JWT token after the validation
     * @throws Exception 
     */
    JSONObject ValidateUser(String username, String password) throws Exception {
        AuthenticationHelper helper = new AuthenticationHelper(POOL_ID, CLIENTAPP_ID, "");
        return helper.PerformSRPAuthentication(username, password);
    }

    /**
     * Returns the AWS credentials
     *
     * @param idprovider the IDP provider for the login map
     * @param id         the username for the login map.
     * @return returns the credentials based on the access token returned from the user pool.
     */
    Credentials GetCredentials(String idprovider, String id) {
        AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
        AmazonCognitoIdentity provider = AmazonCognitoIdentityClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.US_EAST_2)
                .build();

        GetIdRequest idrequest = new GetIdRequest();
        idrequest.setIdentityPoolId(FED_POOL_ID);
        idrequest.addLoginsEntry(idprovider, id);
        GetIdResult idResult = provider.getId(idrequest);

        GetCredentialsForIdentityRequest request = new GetCredentialsForIdentityRequest();
        request.setIdentityId(idResult.getIdentityId());
        request.addLoginsEntry(idprovider, id);

        GetCredentialsForIdentityResult result = provider.getCredentialsForIdentity(request);
        return result.getCredentials();
    }

    /**
     * Returns the AWS credentials
     *
     * @param accesscode access code
     * @return returns the credentials based on the access token returned from the user pool.
     */
    Credentials GetCredentials(String accesscode) {
        Credentials credentials = null;

        try {
            Map<String, String> httpBodyParams = new HashMap<String, String>();
            httpBodyParams.put(Constants.TOKEN_GRANT_TYPE, Constants.TOKEN_GRANT_TYPE_AUTH_CODE);
            httpBodyParams.put(Constants.DOMAIN_QUERY_PARAM_CLIENT_ID, CLIENTAPP_ID);
            httpBodyParams.put(Constants.DOMAIN_QUERY_PARAM_REDIRECT_URI, Constants.REDIRECT_URL);
            httpBodyParams.put(Constants.TOKEN_AUTH_TYPE_CODE, accesscode);

            AuthHttpClient httpClient = new AuthHttpClient();
            URL url = new URL(GetTokenURL());
            String result = httpClient.httpPost(url, httpBodyParams);
            System.out.println(result);

            JSONObject payload = CognitoJWTParser.getPayload(result);
            String provider = payload.get("iss").toString().replace("https://", "");
            credentials = GetCredentials(provider, result);

            return credentials;
        } catch (Exception exp) {
            System.out.println(exp);
        }
        return credentials;
    }

    }
