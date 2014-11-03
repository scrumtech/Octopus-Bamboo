package com.nib.octopus;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.struts.DefaultTextProvider;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CreateReleaseTaskConfigurator extends AbstractTaskConfigurator {

    private DefaultTextProvider textProvider;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put("serverUrl", params.getString("serverUrl"));
        config.put("apiKey", params.getString("apiKey"));
        config.put("projectName", params.getString("projectName"));
        config.put("version", params.getString("version"));

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);

        context.put("serverUrl", "http://octopusdeploy/");
        context.put("apiKey", "API-KOTGRIJSZRNPTEPFGU1WLB8KVWE");
        context.put("projectName", "");
        context.put("version", "");
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);

        context.put("serverUrl", taskDefinition.getConfiguration().get("serverUrl"));
        context.put("apiKey", taskDefinition.getConfiguration().get("apiKey"));
        context.put("projectName", taskDefinition.getConfiguration().get("projectName"));
        context.put("version", taskDefinition.getConfiguration().get("version"));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        context.put("serverUrl", taskDefinition.getConfiguration().get("serverUrl"));
        context.put("apiKey", taskDefinition.getConfiguration().get("apiKey"));
        context.put("projectName", taskDefinition.getConfiguration().get("projectName"));
        context.put("version", taskDefinition.getConfiguration().get("version"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

        final String serverUrlValue = params.getString("serverUrl");
        if (StringUtils.isEmpty(serverUrlValue))
        {
            errorCollection.addError("serverUrl", textProvider.getText("com.nib.octopus.serverUrl.error"));
        }
        
        final String apiKeyValue = params.getString("apiKey");
        if (StringUtils.isEmpty(apiKeyValue))
        {
            errorCollection.addError("apiKey", textProvider.getText("com.nib.octopus.apiKey.error"));
        }
        
        final String projectNameValue = params.getString("projectName");
        if (StringUtils.isEmpty(projectNameValue))
        {
            errorCollection.addError("projectName", textProvider.getText("com.nib.octopus.projectName.error"));
        }
        
        final String versionValue = params.getString("version");
        if (StringUtils.isEmpty(versionValue))
        {
            errorCollection.addError("version", textProvider.getText("com.nib.octopus.version.error"));
        }
    }

    public void setTextProvider(final DefaultTextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}
