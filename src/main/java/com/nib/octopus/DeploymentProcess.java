package com.nib.octopus;

import java.util.List;

public class DeploymentProcess {
	public String DeploymentProcessId;
	public String LastReleaseVersion;
	public String NextVersionIncrement;
	public String VersioningPackageStepName;
	public List<com.nib.octopus.Package> Packages;
	
	public DeploymentProcess() {
		
	}
}
