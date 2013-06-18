package de.mhus.sling.vaadin.servlet;

import java.io.File;

import de.mhus.sling.vaadin.VaadinResource;
import de.mhus.sling.vaadin.VaadinServlet;

public class CoreServlet extends VaadinServlet<CoreApplication> {

	public CoreServlet() {
		super(CoreApplication.class);
	}

	private static final long serialVersionUID = 1L;
    	
	public ContextWrapper getCoreContext() {
		return (ContextWrapper)getServletContext();
	}
	
	
}
