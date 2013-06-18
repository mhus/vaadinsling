package de.mhus.sling.vaadin.servlet;

import org.apache.felix.http.api.ExtHttpService;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mhus.sling.vaadin.VaadinResource;


public class CoreActivator implements BundleActivator, ServiceListener {

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

	    ServiceReference sRef3 = context.getServiceReference(ResourceResolverFactory.class.getName());
	    if (sRef3 != null) {
	    	ResourceResolverFactory factory = (ResourceResolverFactory)context.getService(sRef3);
	    	if (factory != null && filter != null) {
	    		filter.setResourceResolverFactory(factory);
	    	}
	    }
	    
	    context.addServiceListener(this);
	    
	    // TODO register existing
	    
	}

	public void stop(BundleContext context) throws Exception {
		log.info("VAADIN-SLING STOP");
		
		context.removeServiceListener(this);
		
	    ServiceReference sRef2 = context.getServiceReference(ExtHttpService.class.getName());
	    if (sRef2 != null)
	    {
	      ExtHttpService service = (ExtHttpService) context.getService(sRef2);
	      service.unregisterFilter(filter);
	      filter.destroy();
	      filter = null;
	    }		
	}

	public ContextWrapper getCoreContext() {
		return filter.getCoreContext();
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		
		String[] objectClass = (String[])
	            event.getServiceReference().getProperty("objectClass");
		
		if (event.getType() == ServiceEvent.REGISTERED)
        {
			log.info(
                "Ex1: Service of type " + objectClass[0] + " registered.");
            
            if (VaadinResource.SERVICE_NAME.equals(objectClass[0]) && event.getServiceReference() instanceof VaadinResource) {
            	register((VaadinResource)event.getServiceReference());
            }
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING)
        {
        	log.info(
                "Ex1: Service of type " + objectClass[0] + " unregistered.");
            
            if (VaadinResource.SERVICE_NAME.equals(objectClass[0]) && event.getServiceReference() instanceof VaadinResource) {
            	unregister((VaadinResource)event.getServiceReference());
            }
        }
        else if (event.getType() == ServiceEvent.MODIFIED)
        {
        	log.info(
                "Ex1: Service of type " + objectClass[0] + " modified.");
 
            if (VaadinResource.SERVICE_NAME.equals(objectClass[0]) && event.getServiceReference() instanceof VaadinResource) {
            	unregister((VaadinResource)event.getServiceReference());
            	register((VaadinResource)event.getServiceReference());
            }
         }
	}

	public void unregister(VaadinResource resource) {
		filter.getCoreContext().unregister(resource);
	}

	public void register(VaadinResource resource) {
		filter.getCoreContext().register(resource);
	}
	
}
