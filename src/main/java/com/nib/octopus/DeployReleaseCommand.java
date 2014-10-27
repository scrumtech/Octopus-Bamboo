package com.nib.octopus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.extras.common.log.Logger;

public class DeployReleaseCommand {

	private String ProjectName;
	private String DeployToEnvironment;
	private String VersionNumber;
	private String serverUrl;
	private String apiKey;
	private BuildLogger buildLogger;
	private Object DeploymentStatusCheckSleepCycle;
	private Object DeploymentTimeout;
	
	public DeployReleaseCommand(String environment, String project, String version, String url, String key, BuildLogger logger)
	{
	    DeployToEnvironment = environment;
	    ProjectName = project;
	    VersionNumber = version;
	    serverUrl = url;
	    apiKey = key;
	    buildLogger = logger;
//	    DeploymentStatusCheckSleepCycle = TimeSpan.FromSeconds(10);
//	    DeploymentTimeout = TimeSpan.FromMinutes(10);
	}

	public void Execute() throws Exception
	{
	    if (ProjectName == null) throw new Exception("Please specify a project name");
	    if (DeployToEnvironment == null) throw new Exception("Please specify an environment.");
	    if (VersionNumber == null) throw new Exception("Please specify a release version.");
	    if (serverUrl == null) throw new Exception("Please specify a server URL.");
	    if (apiKey == null) throw new Exception("Please specify an API Key.");
	
	    // Get the project to deploy
	    buildLogger.addBuildLogEntry("Finding project: " + ProjectName);
	    String project = GetProjectId(ProjectName);
	    
	    if (project == null)
	        throw new Exception("Could not find a project named: " + ProjectName);

	    // Get the release to deploy
        buildLogger.addBuildLogEntry("Finding release: " + VersionNumber);
        String releaseToPromote = GetReleaseId(project, VersionNumber);
        
        if (releaseToPromote == null)
	        throw new Exception("Could not find a release: " + VersionNumber + " for project: " + ProjectName);
        
        // get the environment to deploy to
        buildLogger.addBuildLogEntry("Finding environment: " + DeployToEnvironment);
        String environment = GetEnvironmentId(DeployToEnvironment);
        
        if (environment == null)
	        throw new Exception("Could not find an environment named: " + DeployToEnvironment);

	    DeployRelease(project, releaseToPromote, environment);
	}

	private String GetEnvironmentId(String deployToEnvironment2) {
		// TODO Auto-generated method stub
		return null;
	}

	private String GetReleaseId(String project, String versionNumber2) throws ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(
				"http://localhost:8080/RESTfulExample/json/product/get");
			getRequest.addHeader("accept", "application/json");
	 
			HttpResponse response = httpClient.execute(getRequest);
	 
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				   + response.getStatusLine().getStatusCode());
			}
	 
			BufferedReader br = new BufferedReader(
	                         new InputStreamReader((response.getEntity().getContent())));
	 
			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
	 
			httpClient.getConnectionManager().shutdown();
	 
		  
		return null;
	}

	private String GetProjectId(String projectName2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void DeployRelease(String project, String release, String environment)
	{
	    // create the deployment resource
        DeploymentResource deployment = Repository.Deployments.Create(new DeploymentResource
        {
            EnvironmentId = promote.Id,
            ReleaseId = release.Id,
            ForcePackageDownload = false,
            UseGuidedFailure = false,
            ForcePackageRedeployment = false,
        });

        buildLogger.addBuildLogEntry("Deploying {0} {1} to: {2} (Guided Failure: {3})", project.Name, release.Version, environment.Name, deployment.UseGuidedFailure ? "Enabled" : "Not Enabled");
        
        try {
        	 
    		DefaultHttpClient httpClient = new DefaultHttpClient();
    		HttpPost postRequest = new HttpPost(
    			"http://localhost:8080/RESTfulExample/json/product/post");
     
    		StringEntity input = new StringEntity("{\"qty\":100,\"name\":\"iPad 4\"}");
    		input.setContentType("application/json");
    		postRequest.setEntity(input);
     
    		HttpResponse response = httpClient.execute(postRequest);
     
    		if (response.getStatusLine().getStatusCode() != 201) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ response.getStatusLine().getStatusCode());
    		}
     
    		BufferedReader br = new BufferedReader(
                            new InputStreamReader((response.getEntity().getContent())));
     
    		String output;
    		System.out.println("Output from Server .... \n");
    		while ((output = br.readLine()) != null) {
    			System.out.println(output);
    		}
     
    		httpClient.getConnectionManager().shutdown();
     
    	  } catch (MalformedURLException e) {
     
    		e.printStackTrace();
     
    	  } catch (IOException e) {
     
    		e.printStackTrace();
     
    	  }
	
	    WaitForDeploymentToComplete(deploymentTask);
	}

	public void WaitForDeploymentToComplete(List<TaskResource> deploymentTasks, List<DeploymentResource> deployments, ProjectResource project, ReleaseResource release)
	{
	    try
	    {
	        buildLogger.addBuildLogEntry("Waiting for deployment to complete....");
	        Repository.Tasks.WaitForCompletion(deploymentTasks.ToArray(), DeploymentStatusCheckSleepCycle.Seconds, DeploymentTimeout.Minutes, PrintTaskOutput);
	        var failed = false;
	        foreach (var deploymentTask in deploymentTasks)
	        {
	            var updated = Repository.Tasks.Get(deploymentTask.Id);
	            if (updated.FinishedSuccessfully)
	            {
	                Log.InfoFormat("{0}: {1}", updated.Description, updated.State);
	            }
	            else
	            {
	                Log.ErrorFormat("{0}: {1}, {2}", updated.Description, updated.State, updated.ErrorMessage);
	                failed = true;
	
	                if (!noRawLog)
	                {
	                    try
	                    {
	                        var raw = Repository.Tasks.GetRawOutputLog(updated);
	                        if (!string.IsNullOrEmpty(rawLogFile))
	                        {
	                            File.WriteAllText(rawLogFile, raw);
	                        }
	                        else
	                        {
	                            Log.Error(raw);
	                        }
	                    }
	                    catch (Exception ex)
	                    {
	                        Log.Error("Could not retrieve the raw task log for the failed task.", ex);
	                    }
	                }
	            }
	        }
	        if (failed)
	        {
	            throw new CommandException("One or more deployment tasks failed.");
	        }
	
	        Log.Info("Done!");
	    }
	    catch (TimeoutException e)
	    {
	        Log.Error(e.Message);
	        var guidedFailureDeployments =
	            from d in deployments
	            where d.UseGuidedFailure
	            select d;
	        if (guidedFailureDeployments.Any())
	        {
	            Log.Warn("One or more deployments are using Guided Failure. Use the links below to check if intervention is required:");
	            foreach (var guidedFailureDeployment in guidedFailureDeployments)
	            {
	                var environment = Repository.Environments.Get(guidedFailureDeployment.Link("Environment"));
	                Log.WarnFormat("  - {0}: {1}", environment.Name, GetPortalUrl(string.Format("/app#/projects/{0}/releases/{1}/deployments/{2}", project.Slug, release.Version, guidedFailureDeployment.Id)));
	            }
	        }
	        throw new CommandException(e.Message);
	    }
	}
}

