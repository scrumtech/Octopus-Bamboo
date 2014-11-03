package com.nib.octopus;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlassian.bamboo.build.logger.BuildLogger;

public class Deployments {

	private String baseUrl;
	private String apiKey;
	private int DeploymentStatusCheckSleepCycleSeconds;
	private int DeploymentTimeoutMinutes;
	
	public Deployments(String baseUrl, String apiKey) {
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
		DeploymentStatusCheckSleepCycleSeconds = 10;
		DeploymentTimeoutMinutes = 10;
	}
	
	public void DeployRelease(String project, String release, String environment, BuildLogger buildLogger)
			throws IllegalStateException, IOException, ParseException {
		
		HttpPost postRequest = new HttpPost(
				this.baseUrl + "/api/Deployments");
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("X-Octopus-ApiKey", this.apiKey);
		StringEntity input = new StringEntity("{\"EnvironmentId\":\""
				+ environment + "\",\"ReleaseId\":\"" + release + "\"}");
		postRequest.setEntity(input);
		
		String json = HttpClient.executeRequest(postRequest);

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

		WaitForDeploymentToComplete(deploymentId, buildLogger);
	}

	private void WaitForDeploymentToComplete(String deploymentId, BuildLogger buildLogger)
			throws IOException, ParseException {
		int totalRunTime = 0;
		// get the deployment server task ID
		HttpGet getRequest = new HttpGet(
				this.baseUrl + "/api/Deployments/" + deploymentId);
		getRequest.addHeader("accept", "application/json");
		getRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

		String json = HttpClient.executeRequest(getRequest);

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
			HttpGet taskGetRequest = new HttpGet(this.baseUrl + serverTaskUrl);
			taskGetRequest.addHeader("accept", "application/json");
			taskGetRequest.addHeader("X-Octopus-ApiKey", this.apiKey);

			String taskJson = HttpClient.executeRequest(taskGetRequest);

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

}
