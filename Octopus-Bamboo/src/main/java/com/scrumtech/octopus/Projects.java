package com.scrumtech.octopus;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.google.gson.Gson;

public class Projects {

	private String baseUrl;
	private String apiKey;

	public Projects(String baseUrl, String apiKey) {
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
	}
	
	/***
	 * Finds a project by name
	 * 
	 * @param projectName2
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Project findByName(String projectName, BuildLogger buildLogger)
			throws ClientProtocolException, IOException {
		String slug = createProjectSlug(projectName);

		HttpGet getRequest = new HttpGet(this.baseUrl + "/api/projects/"
				+ slug);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		String json = HttpClient.executeRequest(getRequest);
		
		Gson gson = new Gson();
		Project project = gson.fromJson(json, Project.class);

		buildLogger.addBuildLogEntry("Project found: " + project.Id);
		return project;
	}
	
	public DeploymentProcess getDeploymentProcess(String projectId, BuildLogger buildLogger) throws ClientProtocolException, IOException
	{
		String id = "deploymentprocess-" + projectId;
		
		HttpGet getRequest = new HttpGet(this.baseUrl + "/api/deploymentprocesses/" + id + "/template");
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);
		
		String json = HttpClient.executeRequest(getRequest);
		
		Gson gson = new Gson();
		DeploymentProcess template = gson.fromJson(json, DeploymentProcess.class);
		
		buildLogger.addBuildLogEntry("Build process template retrieved.");
		return template;
	}

	private String createProjectSlug(String projectName2) {
		// create project name slug
		String slug = projectName2.replace(" ", "-");
		return slug;
	}

}
