package com.shopNow.Lambda;

import com.shopNow.Lambda.CognitoHelper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.json.simple.JSONObject;

public class Shop_now_customer_signup_confirm implements RequestHandler<JSONObject, JSONObject> {

	private String URL_DB;
	private String USERNAME;
	private String PASSWORD;

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {

		Properties prop_db = new Properties();
		try {

			prop_db.load(getClass().getResourceAsStream("/application.properties"));
			URL_DB = prop_db.getProperty("url");
			USERNAME = prop_db.getProperty("username");
			PASSWORD = prop_db.getProperty("password");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		LambdaLogger logger = context.getLogger();

		// logger.log("\n " + URL_DB);

		CognitoHelper helper = new CognitoHelper();

		Connection conn;
		Statement stmt = null;
		Statement select_stmt = null;
		
		ResultSet select_resultSet;
		ResultSet resultSet1 = null;

		final String customermail = input.get("email").toString();
		final String code = input.get("code").toString();

		JSONObject jsonObject_Register_customer_result = new JSONObject();

		String strMsg;

		final String select_sql = "SELECT * FROM customers WHERE email ='" + customermail + "'";

		final String sql = "UPDATE customers SET status = '1' WHERE email ='" + customermail + "'";

		if (customermail == "" || customermail == null) {
			if (code == "" || code == null) {

				strMsg = "Email-id and Verification Code cannot be empty";
				jsonObject_Register_customer_result.put("status", "0");
				jsonObject_Register_customer_result.put("message", strMsg);
				return jsonObject_Register_customer_result;
			} else {
				strMsg = "Email-Id cannot be empty";
				jsonObject_Register_customer_result.put("status", "0");
				jsonObject_Register_customer_result.put("message", strMsg);
				return jsonObject_Register_customer_result;

			}
		} else if (code == "" || code == null) {

			strMsg = "Verification Code cannot be empty";
			jsonObject_Register_customer_result.put("status", "0");
			jsonObject_Register_customer_result.put("message", strMsg);
			return jsonObject_Register_customer_result;

		}

		else {
			try {
				conn = DriverManager.getConnection(URL_DB, USERNAME, PASSWORD);
				select_stmt = conn.createStatement();
				select_resultSet = select_stmt.executeQuery(select_sql);
				if (select_resultSet.next()) {

					if (select_resultSet.getString("status").equals("1")) {

						strMsg = "User is already confirmed";
						jsonObject_Register_customer_result.put("status", "0");
						jsonObject_Register_customer_result.put("message", strMsg);
						jsonObject_Register_customer_result.put("id", select_resultSet.getInt("id"));
						return jsonObject_Register_customer_result;
					} else {
						//boolean success = helper.VerifyAccessCode(customermail, code);
						String confirmation = helper.VerifyAccessCode(customermail, code);
						if(confirmation.equals("SUCCESSFUL")) {

							stmt = conn.createStatement();							
							try {
								int count = stmt.executeUpdate(sql);
								//logger.log("Number of record updated=" + count);
								resultSet1 = stmt.executeQuery("SELECT id FROM customers order by id DESC LIMIT 1");
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							int id1 = 0;
							if (resultSet1.first()) {
								id1 = resultSet1.getInt("id");

							}

							strMsg = "User Confirmed Successfully";
							jsonObject_Register_customer_result.put("status", "1");
							jsonObject_Register_customer_result.put("message", strMsg);
							jsonObject_Register_customer_result.put("id", id1);
						} else {
							//strMsg = "Invalid code provided, please request a code again.";
							jsonObject_Register_customer_result.put("status", "0");
							jsonObject_Register_customer_result.put("message", confirmation.substring(confirmation.indexOf(":")+2,confirmation.indexOf("(Service")-1 ));
							jsonObject_Register_customer_result.put("httpStatus", 400);
							jsonObject_Register_customer_result.put("errorType", "BadRequest");
							throw new RuntimeException(jsonObject_Register_customer_result.toJSONString());	
							//return jsonObject_Register_customer_result;

						}

					}

				} else {
					strMsg = "No User Found";
					jsonObject_Register_customer_result.put("status", "0");
					jsonObject_Register_customer_result.put("message", strMsg);
					return jsonObject_Register_customer_result;
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jsonObject_Register_customer_result.put("message", e.toString());
			}

		}
		return jsonObject_Register_customer_result;

	}
}