package de.mhus.sling.scriptconsole;

import java.util.Map;

import javax.script.ScriptContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.engine.BshScriptEngine;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window.Notification;

import de.mhus.sling.vaadin.VaadinApplication;

public class BshApplication extends VaadinApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(BshApplication.class);
	
	private BshScriptEngine engine = new BshScriptEngine();
	private TextArea output;
	private TextArea inputLine;
	private Table bindings;
	private BindingsDataSource bindingsDataSource;
	private CheckBox cbCleanLine;
	
	@Override
	public void doInit() {

		window.setCaption("Script Console");
		
		VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");

		HorizontalLayout topLine = new HorizontalLayout();
		mainLayout.addComponent(topLine);
		
		
		HorizontalSplitPanel mainLR = new HorizontalSplitPanel();
		mainLR.setSplitPosition(400, Sizeable.UNITS_PIXELS);
        mainLR.setWidth("100%");
		mainLayout.addComponent(mainLR);
		
		
		VerticalSplitPanel vert = new VerticalSplitPanel();
		mainLR.addComponent(vert);
		
        vert.setHeight("600px");
        vert.setWidth("100%");
        vert.setSplitPosition(150, Sizeable.UNITS_PIXELS);
        
        output = new TextArea();
        output.setHeight("100%");
        output.setWidth("100%");
        
        vert.addComponent(output);
        
        inputLine = new TextArea();
        inputLine.setHeight("100%");
        inputLine.setWidth("100%");
//		inputLine.setValue("return ");

        Button button = new Button("Execute");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doExecute();
			}
		});
//        TextArea inputArea = new TextArea();
        
        
        inputLine.addShortcutListener(new ShortcutListener("ALT+ENTER",KeyCode.ENTER, new int[] {ShortcutAction.ModifierKey.ALT}) {
			
			@Override
			public void handleAction(Object sender, Object target) {
				doExecute();
			}
		});
        
        cbCleanLine = new CheckBox("Clean Line");
        cbCleanLine.setValue(true);
        
        mainLayout.addComponent(button);
        mainLayout.addComponent(cbCleanLine);

//        output.setValue("");
//        output.setReadOnly(true);
        
        vert.addComponent(inputLine);

        engine.put("vaadin", topLine);

        
        bindings = new Table("Bindings");
        bindings.setWidth("100%");
        bindings.setHeight("100%");
        
        bindingsDataSource = new BindingsDataSource();
        bindings.setContainerDataSource( bindingsDataSource );
        bindings.setColumnReorderingAllowed(true);
        bindings.setColumnCollapsingAllowed(true);
        
        bindings.setColumnHeaders(new String[] { "Name", "Type", "Value" });

        mainLR.addComponent(bindings);
        
        
		window.addComponent(mainLayout);
		
		try {
			engine.eval("import com.vaadin.ui;");
			engine.eval("import org.apache.sling.api.resource;");
			engine.eval("import org.apache.sling.api;");
		} catch (Throwable t) {
			log.error("",t);
		}
		
	}

	protected void doExecute() {
		String text = (String)output.getValue();
		String cmd = (String)inputLine.getValue();
		text = text + "> " + cmd + "\n";
		try {
			Object ret = engine.eval( cmd );
			text = text + ret + "\n";
			if (ret != null) window.showNotification(ret.toString());
		} catch (Throwable e) {
			window.showNotification(e.toString(),Notification.TYPE_ERROR_MESSAGE );
			text = text + e.toString() + "\n";
		}
		
		output.setValue( text );
		
		if (((Boolean)cbCleanLine.getValue()).booleanValue()) inputLine.setValue("");
		
		bindingsDataSource.update();
	}

	private class BindingsDataSource extends IndexedContainer {
		
		private static final long serialVersionUID = 1L;

		BindingsDataSource() {
			
			addContainerProperty("Name", String.class,null);
			addContainerProperty("Type", String.class,null);
			addContainerProperty("Value", String.class,null);
			
			update();
		}
		
		void update() {
			// removeAllItems(); // TODO merge
			
			for ( Map.Entry<String, Object> bind : engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet() ) {
				
				Item item = addItem(bind.getKey());
				item.getItemProperty("Name").setValue(bind.getKey());
				item.getItemProperty("Type").setValue(bind.getValue().getClass().getCanonicalName());
				item.getItemProperty("Value").setValue(shorten(bind.getValue()));
				
			}
			
		}

		private String shorten(Object value) {
			if (value == null) return "[null]";
			String ret = value.toString();
			if (ret.length() > 100 ) return ret.substring(0,100) + " ...";
			return ret;
		}
		
	}

	@Override
	protected void doSlingResourceChanged() {
		if (slingRequestInfo == null || engine == null) return;
		engine.put("resource", slingRequestInfo.getResource());
	}
	
//	public static final Object iso3166_PROPERTY_NAME = "name";
//    public static final Object iso3166_PROPERTY_SHORT = "short";
//    public static final Object iso3166_PROPERTY_FLAG = "flag";
//    
//	private static void fillIso3166Container(IndexedContainer container) {
//        container.addContainerProperty(iso3166_PROPERTY_NAME, String.class,
//                null);
//        container.addContainerProperty(iso3166_PROPERTY_SHORT, String.class,
//                null);
//        container.addContainerProperty(iso3166_PROPERTY_FLAG, Resource.class,
//                null);
//	}
	
}
