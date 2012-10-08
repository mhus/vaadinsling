package de.mhus.sling.vaadin.servlet;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ConfigWrapper implements ServletConfig, FilterConfig {

	private ServletConfig sc;
	private FilterConfig fc;
	private String application;

	public String getServletName() {
		return sc.getServletName();
	}

	public ServletContext getServletContext() {
		if (fc!= null) return fc.getServletContext();
		return sc.getServletContext();
	}

	public String getInitParameter(String name) {
		if (fc!= null) return fc.getInitParameter(name);
		if (application != null && "application".equals(name))
			return application;
		return sc.getInitParameter(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		if (fc!= null) return fc.getInitParameterNames();
		return sc.getInitParameterNames();
	}

	public ConfigWrapper(ServletConfig servletConfig, String app) {
		sc = servletConfig;
		application = app;
	}

	public ConfigWrapper(FilterConfig filterConfig, String app) {
		fc = filterConfig;
		application = app;
	}

	public String getFilterName() {
		return "VAADIN";
	}

}
