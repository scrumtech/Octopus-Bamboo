package com.nib.octopus;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.atlassian.bamboo.build.logger.BuildLogger;

import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * @author wfalconer
 *
 */
public class DeployReleaseCommand {

	private String ProjectName;
	private String DeployToEnvironment;
	private String VersionNumber;
	private String serverUrl;
	private String apiKey;
	private BuildLogger buildLogger;
	private int DeploymentStatusCheckSleepCycleSeconds;
	private int DeploymentTimeoutMinutes;

	public DeployReleaseCommand(String environment, String project,
			String version, String url, String key, BuildLogger logger) {
		DeployToEnvironment = environment;
		ProjectName = project;
		VersionNumber = version;
		serverUrl = url;
		apiKey = key;
		buildLogger = logger;
		DeploymentStatusCheckSleepCycleSeconds = 10;
		DeploymentTimeoutMinutes = 10;
	}

	public void Execute() throws Exception {
		if (ProjectName == null)
			throw new Exception("Please specify a project name");
		if (DeployToEnvironment == null)
			throw new Exception("Please specify an environment.");
		if (VersionNumber == null)
			throw new Exception("Please specify a release version.");
		if (serverUrl == null)
			throw new Exception("Please specify a server URL.");
		if (apiKey == null)
			throw new Exception("Please specify an API Key.");

		// Get the project to deploy
		buildLogger.addBuildLogEntry("Finding project: " + ProjectName);
		String project = GetProjectId(ProjectName);

		if (project == null)
			throw new Exception("Could not find a project named: "
					+ ProjectName);

		// Get the release to deploy
		buildLogger.addBuildLogEntry("Finding release: " + VersionNumber);
		String releaseToPromote = GetReleaseId(project, VersionNumber);

		if (releaseToPromote == null)
			throw new Exception("Could not find a release: " + VersionNumber
					+ " for project: " + ProjectName);

		// get the environment to deploy to
		buildLogger.addBuildLogEntry("Finding environment: "
				+ DeployToEnvironment);
		String environment = GetEnvironmentId(DeployToEnvironment);

		if (environment == null)
			throw new Exception("Could not find an environment named: "
					+ DeployToEnvironment);

		DeployRelease(project, releaseToPromote, environment);
	}

	private String GetEnvironmentId(String deployToEnvironment)
			throws IllegalStateException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(
				"http://octopusdeploy/api/environments/all");
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		httpClient.getConnectionManager().shutdown();

		// find the environment we want.
		String environmentId = null;
		JSONParser parser = new JSONParser();

		try {
			JSONArray environments = (JSONArray) parser.parse(json);
			for (Object obj : environments) {
				JSONObject env = (JSONObject) obj;
				if (env.get("Name").equals(deployToEnvironment)) {
					environmentId = (String) env.get("Id");
					break;
				}
			}
		} catch (ParseException pe) {
			buildLogger.addErrorLogEntry(
					"Error parsing response from octopus server.", pe);
			return null;
		}

		buildLogger.addBuildLogEntry("Environment found: " + environmentId);
		return environmentId;
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
	private String GetReleaseId(String projectID, String versionNumber)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet("http://octopusdeploy/api/projects/"
				+ projectID + "/releases/" + versionNumber);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		httpClient.getConnectionManager().shutdown();

		// get the release ID
		String releaseId = null;
		JSONParser parser = new JSONParser();

		try {
			JSONObject obj = (JSONObject) (parser.parse(json));
			releaseId = (String) obj.get("Id");
		} catch (ParseException pe) {
			buildLogger.addErrorLogEntry(
					"Error parsing response from octopus server.", pe);
			return null;
		}

		buildLogger.addBuildLogEntry("Release found: " + releaseId);
		return releaseId;
	}

	/***
	 * Gets the ID of a project for project name
	 * 
	 * @param projectName2
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String GetProjectId(String projectName2)
			throws ClientProtocolException, IOException {
		String slug = createProjectSlug(projectName2);

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet("http://octopusdeploy/api/projects/"
				+ slug);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		httpClient.getConnectionManager().shutdown();

		// get the release ID
		String projectId = null;
		JSONParser parser = new JSONParser();

		try {
			JSONObject obj = (JSONObject) (parser.parse(json));
			projectId = (String) obj.get("Id");
		} catch (ParseException pe) {
			buildLogger.addErrorLogEntry(
					"Error parsing response from octopus server.", pe);
			return null;
		}

		buildLogger.addBuildLogEntry("Project found: " + projectId);
		return projectId;
	}

	private String createProjectSlug(String projectName2) {
		// create project name slug
		String slug = projectName2.replace(" ", "-");
		return slug;
	}

	public void DeployRelease(String project, String release, String environment)
			throws IllegalStateException, IOException, ParseException {
		buildLogger.addBuildLogEntry("Deploying " + this.ProjectName
				+ " version " + this.VersionNumber + " to "
				+ this.DeployToEnvironment);

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(
				"http://octopusdeploy/api/Deployments");
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		StringEntity input = new StringEntity("{\"EnvironmentId\":\""
				+ environment + "\",\"ReleaseId\":\"" + release + "\"}");
		postRequest.setEntity(input);

		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() != 201) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		httpClient.getConnectionManager().shutdown();

		String deploymentId = null;
		JSONParser parser = new JSONParser();

		try {
			JSONObject obj = (JSONObject) (parser.parse(json));
			deploymentId = (String) obj.get("Id");
		} catch (ParseException pe) {
			buildLogger.addErrorLogEntry(
					"Error parsing response from octopus server.", pe);
			return;
		}

		WaitForDeploymentToComplete(deploymentId);
	}

	public void WaitForDeploymentToComplete(String deploymentId)
			throws IOException, ParseException {
		int totalRunTime = 0;
		// get the deployment server task ID
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(
				"http://octopusdeploy/api/Deployments/" + deploymentId);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException(
					"Retrieving the deployment failed : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
		}

		String json = getJsonFromResponse(response);
		// httpClient.getConnectionManager().shutdown();

		buildLogger.addBuildLogEntry("Waiting for deployment to complete....");

		// get the task URL
		String serverTaskUrl = null;
		JSONParser parser = new JSONParser();

		JSONObject obj = (JSONObject) (parser.parse(json));
		JSONObject linksObj = (JSONObject) obj.get("Links");
		serverTaskUrl = (String) linksObj.get("Task");
		
		buildLogger.addBuildLogEntry("Server task URL: " + serverTaskUrl);

		// wait for the deployment to complete
		while (true) {
			// get the task
			HttpGet taskGetRequest = new HttpGet("http://octopusdeploy" + serverTaskUrl);
			taskGetRequest.addHeader("accept", "application/json");
			taskGetRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

			HttpResponse taskResponse = httpClient.execute(taskGetRequest);

			if (taskResponse.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(
						"Retrieving the deployment server task failed : HTTP error code : "
								+ taskResponse.getStatusLine().getStatusCode());
			}

			String taskJson = getJsonFromResponse(taskResponse);

			// check the status of the task
			JSONObject task = (JSONObject) parser.parse(taskJson);
			boolean isTaskComplete = (Boolean) task.get("IsCompleted");

			if (isTaskComplete) {
				// check the state of the task
				boolean finishedSuccessfully = (Boolean) task
						.get("FinishedSuccessfully");
				String taskDescription = (String) task.get("Description");
				String taskState = (String) task.get("State");
				String taskErrorMessage = (String) task.get("ErrorMessage");
				if (finishedSuccessfully) {
					buildLogger.addBuildLogEntry(taskDescription + ": "
							+ taskState);
				} else {
					buildLogger.addErrorLogEntry(taskDescription + ": "
							+ taskState + ", " + taskErrorMessage);
				}

				// output raw log
				break;

			} else {
				// check the deployment timeout
				if ((totalRunTime / 60) >= this.DeploymentTimeoutMinutes) {
					throw new RuntimeException(
							"Deployment has not finished within time out period of "
									+ this.DeploymentTimeoutMinutes
									+ " minutes.");
				}

				// sleep
				totalRunTime += this.DeploymentStatusCheckSleepCycleSeconds;
				try {
					Thread.sleep(this.DeploymentStatusCheckSleepCycleSeconds * 1000);
				} catch (InterruptedException e) {
					// TODO do something here.
					break;
				}
				buildLogger.addBuildLogEntry("Waiting for deployment "
						+ totalRunTime + "s");
			}
		}
	}

	private String getJsonFromResponse(HttpResponse response)
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
