package de.mhus.sling.vaadin.servlet;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreActivator implements BundleActivator {

	private static Logger log = LoggerFactory.getLogger(CoreActivator.class);
	private CoreFilter filter;
	
	public void start(BundleContext context) throws Exception {
		log.info("VAADIN-SLING START");
		
	    ServiceReference sRef2 = context.getServiceReference(ExtHttpService.class.getName());
	    if (sRef2 != null)
	    {
	      ExtHttpService service = (ExtHttpService) context.getService(sRef2);
	      filter = new CoreFilter();
	      service.registerFilter(filter, "/VAADIN.*|/UIDL.*", null, 0, null);
	    }
	    
	}

	public void stop(BundleContext context) throws Exception {
		log.info("VAADIN-SLING STOP");
	    ServiceReference sRef2 = context.getServiceReference(ExtHttpService.class.getName());
	    if (sRef2 != null)
	    {
	      ExtHttpService service = (ExtHttpService) context.getService(sRef2);
	      service.unregisterFilter(filter);
	      filter.destroy();
	      filter = null;
	    }		
	}

}
