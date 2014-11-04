package com.nib.octopus;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.google.gson.Gson;

public class Releases {
	private String apiKey;
	private String baseUrl;

	public Releases(String baseUrl, String apiKey) {
		this.apiKey = apiKey;
		this.baseUrl = baseUrl;
	}

	/***
	 * Gets a Release ID for a project and version number.
	 * 
	 * @param projectID
	 * @param versionNumber
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Release FindByProjectIdAndVersion(String projectID,
			String versionNumber, BuildLogger buildLogger)
			throws ClientProtocolException, IOException {
		HttpGet getRequest = new HttpGet(this.baseUrl + "/api/projects/"
				+ projectID + "/releases/" + versionNumber);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		String json = HttpClient.executeRequest(getRequest);

		if (json != null) {

			// parse to a release object
			Gson gson = new Gson();
			Release release = gson.fromJson(json, Release.class);

			buildLogger.addBuildLogEntry("Release found: " + release.Id);
			return release;
		} else {
			return null;
		}
	}

	public void createRelease(String projectId, String versionNumber,
			List<SelectedPackage> selectedPackages, BuildLogger buildLogger) throws ClientProtocolException, IOException {
		buildLogger.addBuildLogEntry("Create release...");
		
		Gson gson = new Gson();
		String jsonSelectedPackages =gson.toJson(selectedPackages); 
		String jsonData = "{\"ProjectId\":\"" + projectId + "\","
				+ "\"ReleaseNotes\":\"\","
				+ "\"Version\":\"" + versionNumber + "\","
				+ "\"SelectedPackages\":" + jsonSelectedPackages + "}";
		
		HttpPost postRequest = new HttpPost(
				this.baseUrl + "/api/Releases");
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("X-Octopus-ApiKey", this.apiKey);
		StringEntity input = new StringEntity(jsonData);
		postRequest.setEntity(input);
		
		buildLogger.addBuildLogEntry("Creating release with payload: " + jsonData);
		
		HttpClient.executeRequest(postRequest);
	}

}
