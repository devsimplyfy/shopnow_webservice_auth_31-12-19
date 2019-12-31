package com.shopNow.Lambda;

import com.shopNow.Lambda.CognitoHelper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.json.simple.JSONObject;

public class Shop_now_customer_registration implements RequestHandler<JSONObject, JSONObject> {
  
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
		CognitoHelper helper = new CognitoHelper();
		
				
		Connection conn;
		Statement stmt = null;
		ResultSet resultSet;
		ResultSet resultSet1 = null;

		final String customermail = input.get("email").toString();
		final String cust_psw = input.get("password").toString();
		//final String phonenumber = input.get("phone_number").toString();
		
		
		JSONObject jsonObject_Register_customer_result = new JSONObject();
		String strMsg;
//db name from prop
		
		final String sql = "INSERT INTO customers(email,password,status)VALUES('" + customermail + "','" + cust_psw + "','2')";
		if (customermail == "" || customermail == null) {
			if (cust_psw == "" || cust_psw == null) {

				strMsg = "Email-id and Passowrd cannot be empty";
				jsonObject_Register_customer_result.put("status", "0");
				jsonObject_Register_customer_result.put("message", strMsg);
				System.out.println("null");
			} else {
				strMsg = "Email-Id cannot be empty";
				jsonObject_Register_customer_result.put("status", "0");
				jsonObject_Register_customer_result.put("message", strMsg);
				System.out.println(strMsg);

			}
		} else if (cust_psw == "" || cust_psw == null) {

			strMsg = "Password cannot be empty";
			jsonObject_Register_customer_result.put("status", "0");
			jsonObject_Register_customer_result.put("message", strMsg);
			System.out.println(strMsg);

		}

		else {
			 //boolean success = helper.SignUpUser(customermail, password, customermail, phonenumber);
			 //boolean success = helper.SignUpUser(customermail, cust_psw, customermail);
			 String confirmation = helper.SignUpUser(customermail, cust_psw, customermail);
				if(confirmation.equals("SUCCESSFUL")) {
			try {
				conn = DriverManager.getConnection(URL_DB, USERNAME, PASSWORD);
				stmt = conn.createStatement();
				resultSet = stmt.executeQuery("SELECT id FROM customers where email='" + customermail	+"'");

				if (resultSet.next()) {
					strMsg = "User already exists with this email-id";
					jsonObject_Register_customer_result.put("status", "0");
					jsonObject_Register_customer_result.put("message", strMsg);

				} else {

					try {
						int count = stmt.executeUpdate(sql);
						System.out.println("Number of records inserted=" + count);
						resultSet1 = stmt
								.executeQuery("SELECT id FROM customers order by id DESC LIMIT 1");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					int id1 = 0;
					if (resultSet1.first()) {
						id1 = resultSet1.getInt("id");

					}

					strMsg = "Registered User Successfully. A verification code has been sent to your email id using which you can confirm your status by calling sign up confirm service. Thank You !";
					jsonObject_Register_customer_result.put("status", "1");
					jsonObject_Register_customer_result.put("message", strMsg);
					jsonObject_Register_customer_result.put("id", id1);

				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
			 else {
				 jsonObject_Register_customer_result.put("message", confirmation.substring(confirmation.indexOf(":")+2,confirmation.indexOf("(Service")-1 ));
				 jsonObject_Register_customer_result.put("status", "0");
			 }
		}
		return jsonObject_Register_customer_result;
		
	}
}