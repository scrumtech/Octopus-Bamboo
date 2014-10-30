package com.nib.octopus;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.CommonTaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.CommonTaskType;

import org.jetbrains.annotations.NotNull;

public class DeployTask implements CommonTaskType 
{
	@Override
	@NotNull
	public TaskResult execute(@NotNull final CommonTaskContext taskContext)
			throws TaskException 
	{
		final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String serverUrl = taskContext.getConfigurationMap().get("serverUrl");
        final String apiKey = taskContext.getConfigurationMap().get("apiKey");
        final String projectName = taskContext.getConfigurationMap().get("projectName");
        final String version = taskContext.getConfigurationMap().get("version");
        final String environment = taskContext.getConfigurationMap().get("environment");
        
        TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext);

        buildLogger.addBuildLogEntry("Server URL: " + serverUrl);
        buildLogger.addBuildLogEntry("API Key: " + apiKey);
        buildLogger.addBuildLogEntry("Project Name: " + projectName);
        buildLogger.addBuildLogEntry("Version: " + version);
        buildLogger.addBuildLogEntry("Environment: " + environment);
        
        try {
        	DeployReleaseCommand command = new DeployReleaseCommand(environment, projectName, version, serverUrl, apiKey, buildLogger);
        	command.Execute();
        }
        catch (Exception e) {
        	buildLogger.addErrorLogEntry("Error occured during deployment", e);
        	return builder.failed().build();
        }

        return builder.success().build();
	}
}
