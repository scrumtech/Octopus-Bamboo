package com.scrumtech.octopus;

import java.util.ArrayList;
import java.util.List;

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
		
		// check if a release already exists for this version number and skip if it does
		Release rel = this.octoRepository.getReleases().FindByProjectIdAndVersion(project.Id, this.VersionNumber, this.buildLogger);
		if (rel != null) {
			buildLogger.addBuildLogEntry("Release " + this.VersionNumber + " for project " + project.Name + " already exists. Skipping.");
			return;
		}
		
		// get the deployment process for the project.
		DeploymentProcess template = this.octoRepository.getProjects().getDeploymentProcess(project.Id, buildLogger);
		if (template == null)
			throw new Exception("Could not find deployment process for project:  "
					+ ProjectName);
		
		List<SelectedPackage> selectedPackages = new ArrayList<SelectedPackage>();
		
		// get the latest package version for each package in the deployment
		for (Package pkg : template.Packages)
		{
			buildLogger.addBuildLogEntry("Getting latest package version for " + pkg.NuGetPackageId + " from feed " + pkg.NugetFeedName);
			// get the latest version for the package from the feed
			NugetPackage version = this.octoRepository.getNugetFeeds().getLatestPackageVersion(pkg.NuGetFeedId, pkg.NuGetPackageId, buildLogger);
			
			if (version == null)
			{
				buildLogger.addErrorLogEntry("Unable to get the latest version of package " + pkg.NuGetPackageId + " from feed " + pkg.NugetFeedName);
				return;
			}
			
			// add package and version to selected packages list.
			buildLogger.addBuildLogEntry("Adding " + pkg.NuGetPackageId + " version " + version.Version + " to the list of selected packages.");
			SelectedPackage selected = new SelectedPackage();
			selected.StepName = pkg.StepName;
			selected.Version = version.Version;
			
			selectedPackages.add(selected);
		}

		// create the release
		buildLogger.addBuildLogEntry("Creating release for " + this.ProjectName
				+ " version " + this.VersionNumber);
		
		this.octoRepository.getReleases().createRelease(project.Id, this.VersionNumber, selectedPackages, buildLogger);
	}
}
