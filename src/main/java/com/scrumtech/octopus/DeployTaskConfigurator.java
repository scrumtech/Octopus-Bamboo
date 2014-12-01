package com.scrumtech.octopus;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.struts.DefaultTextProvider;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DeployTaskConfigurator extends AbstractTaskConfigurator {

    private DefaultTextProvider textProvider;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put("serverUrl", params.getString("serverUrl"));
        config.put("apiKey", params.getString("apiKey"));
        config.put("projectName", params.getString("projectName"));
        config.put("octopusVersion", params.getString("octopusVersion"));
        config.put("octopusEnvironment", params.getString("octopusEnvironment"));

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);

        context.put("serverUrl", "http://octopusdeploy/");
        context.put("apiKey", "API-EOJEBOQKHX5DT70M1RAPFFTDPM");
        context.put("projectName", "");
        context.put("octopusVersion", "");
        context.put("octopusEnvironment", "");
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);

        context.put("serverUrl", taskDefinition.getConfiguration().get("serverUrl"));
        context.put("apiKey", taskDefinition.getConfiguration().get("apiKey"));
        context.put("projectName", taskDefinition.getConfiguration().get("projectName"));
        context.put("octopusVersion", taskDefinition.getConfiguration().get("octopusVersion"));
        context.put("octopusEnvironment", taskDefinition.getConfiguration().get("octopusEnvironment"));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        context.put("serverUrl", taskDefinition.getConfiguration().get("serverUrl"));
        context.put("apiKey", taskDefinition.getConfiguration().get("apiKey"));
        context.put("projectName", taskDefinition.getConfiguration().get("projectName"));
        context.put("octopusVersion", taskDefinition.getConfiguration().get("octopusVersion"));
        context.put("octopusEnvironment", taskDefinition.getConfiguration().get("octopusEnvironment"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        final String serverUrlValue = params.getString("serverUrl");
        if (StringUtils.isEmpty(serverUrlValue))
        {
            errorCollection.addError("serverUrl", textProvider.getText("com.scrumtech.octopus.serverUrl.error"));
        }
        
        final String apiKeyValue = params.getString("apiKey");
        if (StringUtils.isEmpty(apiKeyValue))
        {
            errorCollection.addError("apiKey", textProvider.getText("com.scrumtech.octopus.apiKey.error"));
        }
        
        final String projectNameValue = params.getString("projectName");
        if (StringUtils.isEmpty(projectNameValue))
        {
            errorCollection.addError("projectName", textProvider.getText("com.scrumtech.octopus.projectName.error"));
        }
        
        final String versionValue = params.getString("octopusVersion");
        if (StringUtils.isEmpty(versionValue))
        {
            errorCollection.addError("octopusVersion", textProvider.getText("com.scrumtech.octopus.version.error"));
        }
        
        final String environmentValue = params.getString("octopusEnvironment");
        if (StringUtils.isEmpty(environmentValue))
        {
            errorCollection.addError("octopusEnvironment", textProvider.getText("com.scrumtech.octopus.environment.error"));
        }
    }

    public void setTextProvider(final DefaultTextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}
