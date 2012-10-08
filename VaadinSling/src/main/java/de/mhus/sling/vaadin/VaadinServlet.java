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
import com.vaadin.terminal.gwt.server.SessionExpiredException;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

import de.mhus.sling.vaadin.servlet.ConfigWrapper;
import de.mhus.sling.vaadin.servlet.SlingWebApplicationContext;

public class VaadinServlet<A extends VaadinApplication> extends ApplicationServlet {

	private static Logger log = LoggerFactory.getLogger(VaadinServlet.class);
	private static final long serialVersionUID = 1L;
	private WeakHashMap<A, String> register = new WeakHashMap<A, String>();
	private Class<? extends A> clazz;

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
    	
//    	if (request.getPathInfo().startsWith("/UIDL")) {
//    		return new URL( request.getHeader("Referer") );
//    	}
        final URL reqURL = new URL(
                (request.isSecure() ? "https://" : "http://")
                        + request.getServerName()
                        + ((request.isSecure() && request.getServerPort() == 443)
                                || (!request.isSecure() && request
                                        .getServerPort() == 80) ? "" : ":"
                                + request.getServerPort())
                        + request.getRequestURI());
//        String servletPath = "";
//        if (request.getAttribute("javax.servlet.include.servlet_path") != null) {
//            // this is an include request
//            servletPath = request.getAttribute(
//                    "javax.servlet.include.context_path").toString()
//                    + request
//                            .getAttribute("javax.servlet.include.servlet_path");
//
//        } else {
//            servletPath = request.getContextPath() + request.getServletPath();
//        }
//
//        if (servletPath.length() == 0
//                || servletPath.charAt(servletPath.length() - 1) != '/') {
//            servletPath = servletPath + "/";
//        }
//        URL u = new URL(reqURL, servletPath);
//        return u;
        return reqURL;
    }
    
//    @Override
//    protected Application getExistingApplication(HttpServletRequest request,
//            boolean allowSessionCreation) throws MalformedURLException,
//            SessionExpiredException {
//
//    	if (request.getPathInfo().startsWith("/UIDL") ) {
//
//            // Ensures that the session is still valid
//            final HttpSession session = request.getSession(allowSessionCreation);
//            if (session == null) {
//                throw new SessionExpiredException();
//            }
//
//            WebApplicationContext context = getApplicationContext(session);
//
//            // Gets application list for the session.
//            final Collection<Application> applications = context.getApplications();
//
//            // Search for the application (using the application URI) from the list
//            for (final Iterator<Application> i = applications.iterator(); i.hasNext();) {
//                final Application sessionApplication = i.next();
//                final String sessionApplicationPath = sessionApplication.getURL()
//                        .getPath();
//                String requestApplicationPath = getApplicationUrl(request)
//                        .getPath();
//
//                if (requestApplicationPath.equals(sessionApplicationPath)) {
//                    // Found a running application
//                    if (sessionApplication.isRunning()) {
//                        return sessionApplication;
//                    }
//                    break;
//                }
//            }
//
//            // Existing application not found
//            return null;
//    	} else {
//    		return super.getExistingApplication(request, allowSessionCreation);
//    	}
//    	
//    }    
}
