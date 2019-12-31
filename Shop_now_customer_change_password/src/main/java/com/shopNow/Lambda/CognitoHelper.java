package com.shopNow.Lambda;

/*
// Copyright 2013-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
 */

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The CognitoHelper class abstracts the functionality of connecting to the
 * Cognito user pool and Federated Identities.
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
	 * Start reset password procedure by sending reset code
	 *
	 * @param username
	 *            user to be reset
	 * @return returns code delivery details
	 */
	String ChangePassword(String access_token, String old_password, String new_password) {
		String msg = null;
		AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
		AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_2).build();

		ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
		changePasswordRequest.setAccessToken(access_token);
		changePasswordRequest.setPreviousPassword(old_password);
		changePasswordRequest.setProposedPassword(new_password);

		
		try {
			ChangePasswordResult changePasswordResult = cognitoIdentityProvider.changePassword(changePasswordRequest);
			msg = "SUCCESSFUL";
			System.out.println("\nChange Password Result"+changePasswordResult);
		} catch (Exception e) {
			// handle exception here
			System.out.println("Exception:"+e);
			msg=e.toString();
			//return false;
		}
		//return true;
		return msg;
	}

}
