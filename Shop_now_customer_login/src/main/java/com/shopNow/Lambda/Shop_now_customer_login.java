package com.shopNow.Lambda;

import com.shopNow.Lambda.CognitoHelper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

import org.json.simple.JSONObject;

public class Shop_now_customer_login implements RequestHandler<JSONObject, JSONObject> {
	private String URL_DB;
	private String USERNAME;
	private String PASSWORD;

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		
		
		
		Connection conn;
			Properties prop_db = new Properties();
			try {

				prop_db.load(getClass().getResourceAsStream("/application.properties"));
				URL_DB = prop_db.getProperty("url");
				USERNAME = prop_db.getProperty("username");
				PASSWORD = prop_db.getProperty("password");

			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		
		
		JSONObject jsonObject_login_result = new JSONObject();
		CognitoHelper helper = new CognitoHelper();
		LambdaLogger logger = context.getLogger();
		logger.log("Invoked JDBCSample.getCurrentTime");

		final String email = input.get("email").toString();
		final String psw = input.get("password").toString();

		// --USER POOL LOGIN--//

		try {
			org.json.JSONObject result = helper.ValidateUser(email, psw);
			if (!(result.has("Exception"))) {
				//logger.log("User is authenticated: " + result);

				String strMsg;

				if (email == "" || email == null) {
					if (psw == "" || psw == null) {

						strMsg = "Email-Id and Password cannot be empty.";
						jsonObject_login_result.put("status", "0");
						jsonObject_login_result.put("message", strMsg);

					} else {

						strMsg = "Email-Id cannot be empty";
						jsonObject_login_result.put("status", "0");
						jsonObject_login_result.put("message", strMsg);

					}

				} else if (psw == "" || psw == null) {

					strMsg = "Password cannot be empty";
					jsonObject_login_result.put("status", "0");
					jsonObject_login_result.put("message", strMsg);

				} else {

					// Get time from DB server
					try {
						
						conn = DriverManager.getConnection(URL_DB, USERNAME, PASSWORD);

						Statement stmt = conn.createStatement();
						ResultSet resultSet = stmt
								.executeQuery("SELECT id FROM customers where email='" + email
										+ "' and password='" + psw + "'");

						if (resultSet.next() && result.has("id_token")) {
							logger.log("\nINSIDE IF\n");
							logger.log("User is authenticated: " + result);
							strMsg = "Login Successfull";
		
							jsonObject_login_result.put("status", "1");
							jsonObject_login_result.put("message", strMsg);
							//jsonObject_login_result.put("id_token", result);
							jsonObject_login_result.put("id_token", result.get("id_token"));
							jsonObject_login_result.put("access_token", result.get("access_token"));
							jsonObject_login_result.put("refresh_token", result.get("refresh_token"));
							jsonObject_login_result.put("id", resultSet.getInt("id"));
						} else {
							strMsg = "Incorrect Email-Id or Password";
							jsonObject_login_result.put("status", "0");
							jsonObject_login_result.put("message", strMsg);
						}

					} catch (Exception e) {
						e.printStackTrace();
						logger.log("Caught exception: " + e.getMessage());
					}

				}

			} else {
				logger.log("User is not authenticated");
				String res = (String) result.get("Exception");
				jsonObject_login_result.put("message", res.substring(res.indexOf(":") + 2, res.indexOf("(Service") - 1));
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			jsonObject_login_result.put("Exception", e1.toString());
		}

		// ---END---//

		return jsonObject_login_result;
	}
}
