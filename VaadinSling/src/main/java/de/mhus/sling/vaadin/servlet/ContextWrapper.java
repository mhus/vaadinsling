package de.mhus.sling.vaadin.servlet;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import de.mhus.sling.vaadin.VaadinResource;

public class ContextWrapper implements ServletContext {

	private static final String PREFIX = "/VAADIN/";
	private ServletContext org;
	private File root = null;
	private HashMap<String, VaadinResource> resources = new HashMap<String, VaadinResource>();
	
	public ContextWrapper(ServletContext servletContext) {
		
		String prop = System.getProperty("vaadin-root");
		if (prop == null) prop = "./vaadin";
		root = new File(prop);
		
		org = servletContext;
	}

	public File getVaadinRoot() {
		return root;
	}
	
	public ServletContext getContext(String uripath) {
		return org.getContext(uripath);
	}

	public int getMajorVersion() {
		return org.getMajorVersion();
	}

	public int getMinorVersion() {
		return org.getMinorVersion();
	}

	public String getMimeType(String file) {
		return org.getMimeType(file);
	}

	@SuppressWarnings("rawtypes")
	public Set getResourcePaths(String path) {
		return org.getResourcePaths(path);
	}

	@SuppressWarnings("deprecation")
	public URL getResource(String path) throws MalformedURLException {
		// look for static VAADIN resources
		if (path.startsWith(PREFIX)) {
			int pos = path.indexOf('/', 8);
			if (pos > 0) {
				pos = path.indexOf('/',pos+1);
			}
			if (pos > 0) {
				String prefix = path.substring(0,pos);
				VaadinResource resource = null;
				synchronized (resources) {
					resource = resources.get(prefix);
				}
				if (resource != null) {
					return resource.getResource(path);
				}
			}
			// TODO trace a log
			// fallback
			if (path.startsWith("/VAADIN/widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet")) {
				// switch to local resource
				return new File(root,path).toURL();
			}
		}
		
		return org.getResource(path);
	}

	public InputStream getResourceAsStream(String path) {
		return org.getResourceAsStream(path);
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return org.getRequestDispatcher(path);
	}

	public RequestDispatcher getNamedDispatcher(String name) {
		return org.getNamedDispatcher(name);
	}

	@SuppressWarnings("deprecation")
	public Servlet getServlet(String name) throws ServletException {
		return org.getServlet(name);
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public Enumeration getServlets() {
		return org.getServlets();
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	public Enumeration getServletNames() {
		return org.getServletNames();
	}

	public void log(String msg) {
		org.log(msg);
	}

	@SuppressWarnings("deprecation")
	public void log(Exception exception, String msg) {
		org.log(exception, msg);
	}

	public void log(String message, Throwable throwable) {
		org.log(message, throwable);
	}

	public String getRealPath(String path) {
		return org.getRealPath(path);
	}

	public String getServerInfo() {
		return org.getServerInfo();
	}

	public String getInitParameter(String name) {
		return org.getInitParameter(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		return org.getInitParameterNames();
	}

	public Object getAttribute(String name) {
		return org.getAttribute(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return org.getAttributeNames();
	}

	public void setAttribute(String name, Object object) {
		org.setAttribute(name, object);
	}

	public void removeAttribute(String name) {
		org.removeAttribute(name);
	}

	public String getServletContextName() {
		return org.getServletContextName();
	}

	/**
	 * Remove the resource key and all resource instances from the registry.
	 * 
	 * @param resource
	 */
	public void unregister(VaadinResource resource) {
		synchronized (resources) {
			for (Object obj : resources.entrySet().toArray()) {
				@SuppressWarnings("unchecked")
				Entry<String, VaadinResource> set = (Map.Entry<String, VaadinResource>)obj;
				if (set.getValue() == resource)
					resources.remove(set.getKey());
			}
			for (String path : resource.getVaadinResourcePathes() ) {
				resources.remove(PREFIX + path);
			}
		}
	}

	public void register(VaadinResource resource) {
		synchronized (resources) {
			for (String path : resource.getVaadinResourcePathes() ) {
				resources.put(PREFIX + path, resource);
			}
		}
	}

}
