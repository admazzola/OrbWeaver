package com.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
	
	

	public static String performGet(URL url) throws Exception  
	{
	    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	    connection.setRequestMethod("GET");

	    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

	    StringBuilder response = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	    }

	    reader.close();
	    return response.toString();
	 }

}
