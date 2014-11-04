package com.nib.octopus;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.google.gson.Gson;

public class NugetFeeds {

	private String baseUrl;
	private String apiKey;

	public NugetFeeds(String baseUrl, String apiKey) {
		this.baseUrl = baseUrl + "/api/feeds/";
		this.apiKey = apiKey;
	}
	
	public NugetPackage getLatestPackageVersion(String feedId, String packageId, BuildLogger buildLogger) throws ClientProtocolException, IOException, ParseException
	{
		String url = this.baseUrl + feedId + "/packages?packageId=" + packageId + "&partialMatch=False&includeMultipleVersions=False&take=1";
		buildLogger.addBuildLogEntry("URL for latest package version = " + url);
		HttpGet getRequest = new HttpGet(url);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);
		getRequest.addHeader("ContentType", "application/json");
		
		// TODO Try this request for up to 5 times since it seems to be a bit unstable.
		String json = HttpClient.executeRequest(getRequest); 
		
		// get the first result (we should only get one result since we only use one package ID)
		JSONParser parser = new JSONParser();

		JSONArray pckgs = (JSONArray) parser.parse(json);
		
		Gson gson = new Gson();
		NugetPackage pckg = gson.fromJson(((JSONObject)pckgs.get(0)).toJSONString(), NugetPackage.class);
				
		// return the package.
		return pckg;
	}
}
