package com.nib.octopus;

import java.util.regex.Pattern;

public class OctoRepository {
	private String baseUrl;
	private String apiKey;
	
	public OctoRepository(String url, String apiKey)
	{
		this.baseUrl = SanitiseUrl(url);
		this.apiKey = apiKey;
	}
	
	public Deployments getDeployments()
	{
		return new Deployments(this.baseUrl, this.apiKey);
	}
	
	public Environments getEnvironments()
	{
		return new Environments(this.baseUrl, this.apiKey);
	}
	
	public Releases getReleases()
	{
		return new Releases(this.baseUrl, this.apiKey);
	}
	
	public Projects getProjects()
	{
		return new Projects(this.baseUrl, this.apiKey);
	}

	private String SanitiseUrl(String url) {
		// remove any leading slashes
		if (url.startsWith("/")) {
			url = url.replaceAll("^/+", "");
		}
		
		// check starts with http://
		if (!Pattern.compile("^(?i)http[sS]?://{1}").matcher(url).find()) {
			url = "http://" + url;
		}
		
		// check ends with /
		if (url.endsWith("/")) {
			url = url.replaceFirst("/$", "");
		}
		
		return url;
	}
}
