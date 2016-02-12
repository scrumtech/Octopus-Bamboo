package com.scrumtech.octopus;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClient {
	public static String executeRequest(HttpPost postRequest)
			throws IOException, ClientProtocolException {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() != 201) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		httpClient.getConnectionManager().shutdown();
		return json;
	}

	public static String executeRequest(HttpGet getRequest) throws IOException,
			ClientProtocolException {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpResponse response = httpClient.execute(getRequest);
		
		if (response.getStatusLine().getStatusCode() == 404) {
			return null;
		}

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException(
					"Failed : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		httpClient.getConnectionManager().shutdown();
		return json;
	}

	private static String getJsonFromResponse(HttpResponse response)
			throws IOException {
		Scanner scan = new Scanner(response.getEntity().getContent());
		String json = new String();
		while (scan.hasNext()) {
			json += scan.nextLine();
		}

		scan.close();
		return json;
	}
}
