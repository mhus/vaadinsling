package de.mhus.sling.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;

public abstract class VaadinApplication extends Application implements HttpServletRequestListener {

	private static final long serialVersionUID = 1L;
	protected Window window;
	protected SlingRequestInfo slingRequestInfo;

	@Override
	public void init() {
		
		window = new Window();
		setMainWindow(window);
		
		doInit();
	}

	protected abstract void doInit();

	@Override
	public void onRequestStart(HttpServletRequest request,
			HttpServletResponse response) {
		
		if (request instanceof SlingHttpServletRequest) {
			SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
//			log.debug("Request: " + slingRequest.getRequestPathInfo().getResourcePath());
			slingRequestInfo = new SlingRequestInfo(slingRequest);
			doSlingResourceChanged();
		}
		
	}

	@Override
	public void onRequestEnd(HttpServletRequest request,
			HttpServletResponse response) {
		
	}

	protected abstract void doSlingResourceChanged();
	
}
