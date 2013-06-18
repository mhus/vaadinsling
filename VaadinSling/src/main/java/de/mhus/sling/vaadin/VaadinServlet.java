package de.mhus.sling.vaadin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;

import de.mhus.sling.vaadin.servlet.ConfigWrapper;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;

@Component
public class VaadinServlet<A extends VaadinApplication> extends ApplicationServlet {

	private static Logger log = LoggerFactory.getLogger(VaadinServlet.class);
	private static final long serialVersionUID = 1L;
	private WeakHashMap<A, String> register = new WeakHashMap<A, String>();
	private Class<? extends A> clazz;

	@Reference
    protected ResourceResolverFactory resourceResolverFactory;

	
	public VaadinServlet(Class<? extends A> clazz) {
		this.clazz = clazz;
	}
	
	public Class<? extends A> getTypeParameterClass()
    {
        return clazz;
    }
	
    public void init(javax.servlet.ServletConfig servletConfig) throws ServletException {
    	log.debug("VAADIN START " + getTypeParameterClass());
    	super.init(new ConfigWrapper(servletConfig, getTypeParameterClass().getCanonicalName()));
    }

    protected Application getNewApplication(HttpServletRequest request)
            throws ServletException {

    	//if (request.getPathInfo().startsWith("/UIDL")) return null;
    	
    	try {
			A app = getTypeParameterClass().newInstance();
			if (app instanceof SlingInitialize) {
				((SlingInitialize)app).slingInitialize(this);
			}
			app.servlet = this;
			register.put(app, ""); // remember date ?
			return app;
		} catch (Exception e) {
			throw new ServletException(e);
		}
    }
    
    public void destroy() {
    	log.debug("VAADIN STOP " + getTypeParameterClass());
		for (A app : register.keySet()) {
			if (app != null && app.isRunning())
				app.close();
		}
		register.clear();
		super.destroy();
	}

    protected URL getApplicationUrl(HttpServletRequest request)
            throws MalformedURLException {
    	
        final URL reqURL = new URL(
                (request.isSecure() ? "https://" : "http://")
                        + request.getServerName()
                        + ((request.isSecure() && request.getServerPort() == 443)
                                || (!request.isSecure() && request
                                        .getServerPort() == 80) ? "" : ":"
                                + request.getServerPort())
                        + request.getRequestURI());

        return reqURL;
    }
    
    public ResourceResolverFactory getResourceResolverFactory() {
    	return resourceResolverFactory;
    }
    
}
