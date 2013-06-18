package de.mhus.sling.vaadin.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.mhus.sling.vaadin.SlingInitialize;
import de.mhus.sling.vaadin.VaadinApplication;
import de.mhus.sling.vaadin.VaadinServlet;

public class CoreApplication extends VaadinApplication implements SlingInitialize {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private VerticalLayout vertical;
	private File vaadinRoot;

	@Override
	protected void doInit() {
		vertical = new VerticalLayout();
		
		Label l = null;
		l = new Label( "Root Path: " + new File(".").getAbsolutePath() );
		vertical.addComponent(l);

		l = new Label( "Vaadin Path: " + (vaadinRoot == null ? "?" : vaadinRoot.getAbsolutePath() ) );
		vertical.addComponent(l);
		
		// TODO add list of resources
		
		l = new Label("Resource Resolver Factory: " + servlet.getResourceResolverFactory() );
		vertical.addComponent(l);
		
		window.addComponent(vertical);
	//	doSlingResourceChanged();
	}

	/**
	 * Override because in the core application a filtered http request will be given, not a sling request.
	 */
	@Override
	public void onRequestStart(HttpServletRequest request,
			HttpServletResponse response) {
		
	//	if (vertical == null) return; // not initialized
	
		
	}
	
	@Override
	protected void doSlingResourceChanged() {
		
	}

	@Override
	public void slingInitialize(VaadinServlet<?> servlet) {
		vaadinRoot = ((CoreServlet)servlet).getCoreContext().getVaadinRoot();
	}

}
