package com.scrumtech.octopus;

import java.util.List;

public class DeploymentProcess {
	public String DeploymentProcessId;
	public String LastReleaseVersion;
	public String NextVersionIncrement;
	public String VersioningPackageStepName;
	public List<com.scrumtech.octopus.Package> Packages;
	
	public DeploymentProcess() {
		
	}
}
