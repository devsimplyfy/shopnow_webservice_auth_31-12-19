
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

public class Shop_now_customer_forgot_password_confirm implements RequestHandler<JSONObject, JSONObject> {
	private String URL_DB;
	private String USERNAME;
	private String PASSWORD;

	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {

		Properties prop_db = new Properties();
		Connection conn;
		Statement stmt = null;
		Statement select_stmt = null;

		ResultSet select_resultSet;

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

		String email = input.get("email").toString();
		String password = input.get("new_password").toString();
		String code = input.get("code").toString();
		
		String str_msg = null;
		JSONObject jo_change_psw = new JSONObject();

		if (email == "" || email == null) {
			str_msg = "Email-Id cannot be empty";
			jo_change_psw.put("status", "0");
			jo_change_psw.put("message", str_msg);
			return jo_change_psw;

		}

		if (password == "" || password == null) {
			str_msg = "New Password cannot be empty";
			jo_change_psw.put("status", "0");
			jo_change_psw.put("message", str_msg);
			return jo_change_psw;

		}

		if (code == "" || code == null) {
			str_msg = "Code cannot be empty";
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

				if (select_resultSet.getString("status").equals("2")) {

					str_msg = "Confirm forgot password reset functionality cannot be executed as user is not yet confirmed";
					jo_change_psw.put("status", "0");
					jo_change_psw.put("message", str_msg);
					jo_change_psw.put("id", select_resultSet.getInt("id"));
					return jo_change_psw;
				} else {

					// --RESET CONFIRM REQUESTED----//

					String confirmation = helper.UpdatePassword(email, password, code);
					if (confirmation.equals("SUCCESSFUL")) {
						logger.log("Reset password confirmed: " + confirmation);

						stmt = conn.createStatement();
						
						final String sql_update = "update customers set password='" + password + "' where email='"
								+ email + "'";
						Statement stmt1 = conn.createStatement();
						int i = stmt1.executeUpdate(sql_update);
						if (i > 0) {
							str_msg = "Password Updated Successfully";
							jo_change_psw.put("status", "1");
							jo_change_psw.put("message", str_msg);
							return jo_change_psw;
						} else {
							str_msg = "Password Not Updated Successfully";
							jo_change_psw.put("status", "0");
							jo_change_psw.put("message", str_msg);
							return jo_change_psw;

						}
					} else {
						logger.log("Reset password procedure failed.");
						str_msg = "Either entered code is invalid or new password doesn't meet the necessary requirements";
						jo_change_psw.put("message", confirmation.substring(confirmation.indexOf(":")+2,confirmation.indexOf("(Service")-1 ));
						jo_change_psw.put("status", "0");
						jo_change_psw.put("httpStatus", 400);
						jo_change_psw.put("errorType", "BadRequest");
						throw new RuntimeException(jo_change_psw.toJSONString());
						//return jo_change_psw;
					}
				}
			} else {
				str_msg = "No user found with this Email Id";
				jo_change_psw.put("status", "0");
				jo_change_psw.put("message", str_msg);
				return jo_change_psw;
			}

		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// ---END---//
		return jo_change_psw;

	}

}