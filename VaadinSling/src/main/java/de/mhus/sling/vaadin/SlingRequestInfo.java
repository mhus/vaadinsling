package de.mhus.sling.vaadin;

import java.security.Principal;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;

public class SlingRequestInfo {

	private Resource resource;
	private RequestPathInfo pathInfo;
	private RequestParameterMap parameters;
	private Principal principal;

	public SlingRequestInfo(SlingHttpServletRequest slingRequest) {
		resource = slingRequest.getResource();
		pathInfo = slingRequest.getRequestPathInfo();
		parameters = slingRequest.getRequestParameterMap();
		principal = slingRequest.getUserPrincipal();
	}

	public Resource getResource() {
		return resource;
	}

	public RequestPathInfo getPathInfo() {
		return pathInfo;
	}

	public RequestParameterMap getParameters() {
		return parameters;
	}

	public Principal getPrincipal() {
		return principal;
	}

}
