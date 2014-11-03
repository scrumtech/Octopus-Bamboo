package com.nib.octopus;

import com.atlassian.bamboo.build.logger.BuildLogger;

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
	private OctoRepository octoRepository;

	public DeployReleaseCommand(String environment, String project,
			String version, String url, String key, BuildLogger logger) {
		DeployToEnvironment = environment;
		ProjectName = project;
		VersionNumber = version;
		serverUrl = url;
		apiKey = key;
		buildLogger = logger;
		octoRepository = new OctoRepository(this.serverUrl, this.apiKey);
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
		Project project = this.octoRepository.getProjects().findByName(ProjectName, buildLogger);

		if (project == null)
			throw new Exception("Could not find a project named: "
					+ ProjectName);

		// Get the release to deploy
		buildLogger.addBuildLogEntry("Finding release: " + VersionNumber);
		Release releaseToPromote = this.octoRepository.getReleases().FindByProjectIdAndVersion(project.Id, VersionNumber, buildLogger);

		if (releaseToPromote == null)
			throw new Exception("Could not find a release: " + VersionNumber
					+ " for project: " + ProjectName);

		// get the environment to deploy to
		buildLogger.addBuildLogEntry("Finding environment: "
				+ DeployToEnvironment);
		Environment environment = this.octoRepository.getEnvironments().FindByName(DeployToEnvironment, buildLogger);

		if (environment == null)
			throw new Exception("Could not find an environment named: "
					+ DeployToEnvironment);

		buildLogger.addBuildLogEntry("Deploying " + this.ProjectName
				+ " version " + this.VersionNumber + " to "
				+ this.DeployToEnvironment);
		
		this.octoRepository.getDeployments().DeployRelease(project.Id, releaseToPromote.Id, environment.Id, buildLogger);
	}
}
