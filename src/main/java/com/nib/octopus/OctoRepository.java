package com.nib.octopus;

import java.util.regex.Pattern;

public class OctoRepository {
	private String baseUrl;
	private String apiKey;

	private Deployments deployments;
	private Environments environments;
	private Projects projects;
	private Releases releases;
	private NugetFeeds nugetFeeds;

	public OctoRepository(String url, String apiKey) {
		this.baseUrl = SanitiseUrl(url);
		this.apiKey = apiKey;
	}

	public Deployments getDeployments() {
		if (deployments == null) {
			deployments = new Deployments(this.baseUrl, this.apiKey);
		}
		return deployments;
	}

	public Environments getEnvironments() {
		if (environments == null) {
			environments = new Environments(this.baseUrl, this.apiKey);
		}
		return environments;
	}

	public Releases getReleases() {
		if (releases == null) {
			releases = new Releases(this.baseUrl, this.apiKey);
		}
		return releases;
	}

	public Projects getProjects() {
		if (projects == null) {
			projects = new Projects(this.baseUrl, this.apiKey);
		}
		return projects;
	}
	
	public NugetFeeds getNugetFeeds()
	{
		if (nugetFeeds == null)
			nugetFeeds = new NugetFeeds(this.baseUrl, this.apiKey);
		return nugetFeeds;
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
