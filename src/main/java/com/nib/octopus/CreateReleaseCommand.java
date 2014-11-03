package com.nib.octopus;

import com.atlassian.bamboo.build.logger.BuildLogger;

/**
 * @author wfalconer
 *
 */
public class CreateReleaseCommand {

	private String ProjectName;
	private String VersionNumber;
	private String serverUrl;
	private String apiKey;
	private BuildLogger buildLogger;
	private OctoRepository octoRepository;

	public CreateReleaseCommand(String project,
			String version, String url, String key, BuildLogger logger) {
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

		// create the release
		buildLogger.addBuildLogEntry("Creating release for " + this.ProjectName
				+ " version " + this.VersionNumber);
		
		this.octoRepository.getReleases().createRelease(project.Id, this.VersionNumber, buildLogger);
	}
}
