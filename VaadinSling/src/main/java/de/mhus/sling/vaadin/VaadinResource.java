package de.mhus.sling.vaadin;

import java.net.MalformedURLException;
import java.net.URL;

public interface VaadinResource {

    /**
     * The service name to use when registering implementations of this
     * interface as services.
     */
	final String SERVICE_NAME = "de.mhus.sling.vaadin.VaadinResource";
		
	/**
	 * Query a path in this registered resource.
	 * 
	 * @param path The full path, including /VAADIN/ and the registered resource path.
	 * @return The resource or null if not exists
	 * @throws MalformedURLException
	 */
	URL getResource(String path) throws MalformedURLException;

	/**
	 * e.g. "widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet"
	 * 
	 * @return A list of resource pathes (exclude /VAADIN/) and never null
	 */
	String[] getVaadinResourcePathes();
	
}
