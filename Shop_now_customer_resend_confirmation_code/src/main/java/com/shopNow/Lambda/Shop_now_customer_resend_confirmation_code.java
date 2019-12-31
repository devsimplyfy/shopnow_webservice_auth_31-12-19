
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
import org.json.simple.JSONObject;

import java.util.Properties;

public class Shop_now_customer_resend_confirmation_code implements RequestHandler<JSONObject, JSONObject> {

	private String URL_DB;
	private String USERNAME;
	private String PASSWORD;

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
		Connection conn;
		Statement select_stmt = null;

		ResultSet select_resultSet;

		Properties prop_db = new Properties();
		try {

			prop_db.load(getClass().getResourceAsStream("/application.properties"));
			URL_DB = prop_db.getProperty("url");
			USERNAME = prop_db.getProperty("username");
			PASSWORD = prop_db.getProperty("password");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		CognitoHelper helper = new CognitoHelper();
		LambdaLogger logger = context.getLogger();
		String str_msg = null;
		JSONObject jo_change_psw = new JSONObject();

		String email = input.get("email").toString();
		if (email == "" || email == null) {
			str_msg = "Email-Id cannot be empty";
			jo_change_psw.put("status", "0");
			jo_change_psw.put("message", str_msg);
			return jo_change_psw;

		}

		try {
			conn = DriverManager.getConnection(URL_DB, USERNAME, PASSWORD);
			final String select_sql = "SELECT * FROM customers WHERE email ='" + email + "'";
			select_stmt = conn.createStatement();
			select_resultSet = select_stmt.executeQuery(select_sql);
			if (select_resultSet.next()) {

				
					// --RESEND CONFIRMATION CODE REQUESTED----//
					String result = helper.ResendConfirmationCode(email);
					if (result.equals("SUCCESSFUL")) {

						jo_change_psw.put("message", "We have sent a code by email to the registered email id.");

					} else {
						jo_change_psw.put("message",
								result.substring(result.indexOf(":") + 2, result.indexOf("(Service") - 1));
					}

					// ---END---//

			} else {
				str_msg = "No User Found";
				jo_change_psw.put("status", "0");
				jo_change_psw.put("message", str_msg);
				return jo_change_psw;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jo_change_psw;

	}

}