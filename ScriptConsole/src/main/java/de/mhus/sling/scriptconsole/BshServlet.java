package de.mhus.sling.scriptconsole;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import de.mhus.sling.vaadin.VaadinServlet;


@Component(immediate = true, metatype = true, label = "Bean Shell")
@Service(javax.servlet.Servlet.class)
@Properties({
    @Property(name = "sling.servlet.paths", value = "/scriptconsole")
})
public class BshServlet extends  VaadinServlet<BshApplication> {

	  public BshServlet() {
		super(BshApplication.class);
	}

	private static final long serialVersionUID = 1L;


}
