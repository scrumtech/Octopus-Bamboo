package com.scrumtech.octopus;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.google.gson.Gson;

public class Environments {

	private String apiKey;
	private String baseUrl;

	public Environments(String baseUrl, String apiKey) {
		this.apiKey = apiKey;
		this.baseUrl = baseUrl;
	}

	public Environment FindByName(String environmentName, BuildLogger buildLogger)
			throws IllegalStateException, IOException 
	{
		HttpGet getRequest = new HttpGet(
				this.baseUrl + "/api/environments/all");
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		String json = HttpClient.executeRequest(getRequest);

		// find the environment we want.
		Environment target = null;
		JSONParser parser = new JSONParser();

		try {
			JSONArray environments = (JSONArray) parser.parse(json);
			for (Object obj : environments) {
				JSONObject env = (JSONObject) obj;
				if (env.get("Name").equals(environmentName)) {
					Gson gson = new Gson();
					target = gson.fromJson(env.toJSONString(),  Environment.class);
					break;
				}
			}
		} catch (ParseException pe) {
			buildLogger.addErrorLogEntry(
					"Error parsing response from octopus server.", pe);
			return null;
		}

		buildLogger.addBuildLogEntry("Environment found: " + target.Id);
		return target;
	}
}
