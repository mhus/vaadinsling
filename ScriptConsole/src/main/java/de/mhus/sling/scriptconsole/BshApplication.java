package de.mhus.sling.scriptconsole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.script.ScriptContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.engine.BshScriptEngine;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
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
	private TextArea bindingInfo;
	private CheckBox cbCleanOutput;
	private CheckBox cbBindingsUpdate;
	private StringBuffer text;
	
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
        vert.setSplitPosition(400, Sizeable.UNITS_PIXELS);
        
        output = new TextArea();
        output.setHeight("100%");
        output.setWidth("100%");
        
        vert.addComponent(output);
        
        inputLine = new TextArea();
        inputLine.setHeight("100%");
        inputLine.setWidth("100%");
//		inputLine.setValue("return ");

        HorizontalLayout buttonBar = new HorizontalLayout();
        
        Button button = new Button("Execute (Alt+Enter)");
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
                
        buttonBar.addComponent(button);
        
        button = new Button("Delete Binding");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doDeleteBinding();
			}
		});
        buttonBar.addComponent(button);

        button = new Button("Refresh Binding");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doRefreshBinding();
			}
		});
        buttonBar.addComponent(button);
        
        button = new Button("Load Script");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doLoadScript();
			}
		});
        buttonBar.addComponent(button);
        
        button = new Button("Save Script");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				doSaveScript();
			}
		});
        buttonBar.addComponent(button);

        button = new Button("Destroy Application");
        button.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				BshApplication.this.close();
			}
		});
        buttonBar.addComponent(button);

//        output.setValue("");
//        output.setReadOnly(true);
        
        mainLayout.addComponent(buttonBar);
        
        HorizontalLayout configBar = new HorizontalLayout();
        
        cbCleanLine = new CheckBox("Clean Line");
        cbCleanLine.setValue(false);
        configBar.addComponent(cbCleanLine);
        
        cbCleanOutput = new CheckBox("Clean Output");
        cbCleanOutput.setValue(true);
        configBar.addComponent(cbCleanOutput);

        cbBindingsUpdate = new CheckBox("Bindings Update");
        cbBindingsUpdate.setValue(true);
        configBar.addComponent(cbBindingsUpdate);
        
        mainLayout.addComponent(configBar);
        
        vert.addComponent(inputLine);

        engine.put("vaadin", topLine);

        
        bindings = new Table("Bindings");
        bindings.setWidth("100%");
        bindings.setHeight("100%");
        
        bindingsDataSource = new BindingsDataSource();
        bindings.setContainerDataSource( bindingsDataSource );
        bindings.setColumnReorderingAllowed(true);
        bindings.setColumnCollapsingAllowed(true);
        bindings.setMultiSelect(false);
        bindings.setSelectable(true);
        bindings.setImmediate(true);
        bindings.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				doShowBindingInfos();
			}
        	
        });
        
        bindings.setColumnHeaders(new String[] { "Name", "Type", "Value" });

        VerticalSplitPanel rightSplit = new VerticalSplitPanel();
        rightSplit.addComponent(bindings);
        rightSplit.setHeight("600px");
        rightSplit.setWidth("100%");
        rightSplit.setSplitPosition(150, Sizeable.UNITS_PIXELS);
        
        bindingInfo = new TextArea();
        bindingInfo.setWidth("100%");
        bindingInfo.setHeight("100%");

        rightSplit.addComponent(bindingInfo);
        
        mainLR.addComponent(rightSplit);
        
        
		window.addComponent(mainLayout);
		
		try {
			MyWriter myWriter = new MyWriter();
			engine.getContext().setErrorWriter(myWriter);
			engine.getContext().setWriter(myWriter);
			
			engine.eval("import com.vaadin.ui.*;");
			engine.eval("import org.apache.sling.api.resource.*;");
			engine.eval("import org.apache.sling.api.*;");
			engine.eval("import java.util.*;");
			engine.eval("import java.io.*;");
			
		} catch (Throwable t) {
			log.error("",t);
		}
		
	}

	protected void doRefreshBinding() {
		bindingsDataSource.removeAllItems();
		bindingsDataSource.update();
	}

	@SuppressWarnings("serial")
	protected void doSaveScript() {
		try {
			BrowseDialog.show(getMainWindow(), "Save", true, resourceResolver, new Action.Listener() {
				
				@Override
				public void handleAction(Object sender, Object target) {
					if (target == null) return;
					try {
						Resource res = (Resource)target;
						BrowseDialog dlg = (BrowseDialog) sender;
						String il = dlg.getInputLine();
						System.err.println(il);
						if (il != null) {
							
							if (!il.endsWith(".bsh")) il = il + ".bsh";
							
							// create
							// do not have the cool CRUD in the current release ... use old style
//							ModifyingResourceProvider mRes = (ModifyingResourceProvider)res;
							
							String c = (String) inputLine.getValue();
							ByteArrayInputStream dataStream = new ByteArrayInputStream(c.getBytes());

							Session jcrSession = res.getResourceResolver().adaptTo(Session.class);
							Node dir = res.adaptTo(Node.class);
							
							Node file = dir.addNode(il, "nt:file");
							Node content = file.addNode("jcr:content","nt:unstructured");
							content.setProperty("jcr:data", new org.apache.jackrabbit.value.BinaryImpl( dataStream ));
							
							jcrSession.save();
							
						} else {
							// overwrite
//							OutputStream os = res.adaptTo(OutputStream.class);
//							String c = (String) inputLine.getValue();
//							for (int i = 0; i < c.length(); i++)
//								os.write(c.charAt(i)); // no UTF-8
//							os.close();

							String c = (String) inputLine.getValue();
							ByteArrayInputStream dataStream = new ByteArrayInputStream(c.getBytes());
							
							Session jcrSession = res.getResourceResolver().adaptTo(Session.class);
							Node file = res.adaptTo(Node.class);
							Node content = file.getNode("jcr:content");
							content.setProperty("jcr:data", new org.apache.jackrabbit.value.BinaryImpl( dataStream ));
							
							jcrSession.save();
							
						}
					} catch (Exception e) {
						log.error("save fails",e);
						throw new RuntimeException("Save",e);
					}
				}
			});
		} catch (Exception e) {
			log.error("save no dialog",e);
			window.showNotification(e.toString(),Notification.TYPE_ERROR_MESSAGE );
		}
	}

	protected void doLoadScript() {
		try {
			BrowseDialog.show(getMainWindow(), "Open", false, resourceResolver, new Action.Listener() {
				
				@Override
				public void handleAction(Object sender, Object target) {
					if (target == null) return;
					Resource res = (Resource)target;
					InputStream reader = res.adaptTo(InputStream.class);
					int rc = 0;
					StringBuffer sb = new StringBuffer();
					try {
						while ((rc=reader.read()) >= 0) {
							sb.append((char)rc); // no UTF-8 !!!
						}
						reader.close();
					} catch (IOException e) {
						log.error("",e);
						window.showNotification(e.toString(),Notification.TYPE_ERROR_MESSAGE );
					}
					
					inputLine.setValue(sb.toString());
				}
			});
		} catch (Exception e) {
			log.error("",e);
			window.showNotification(e.toString(),Notification.TYPE_ERROR_MESSAGE );
		}
	}

	protected void doDeleteBinding() {
		try {
			Item item = bindings.getItem(bindings.getValue());
			String name = (String)item.getItemProperty("Name").getValue();
//			Object obj = engine.get(name);
			engine.put(name, null);
			bindingsDataSource.removeItem(name);
			bindingInfo.setValue("");
		} catch (Throwable t) {
			bindingInfo.setValue(t.toString());
			log.error("",t);
		}
	}

	protected void doShowBindingInfos() {
		bindingInfo.setValue("");
		try {
			Item item = bindings.getItem(bindings.getValue());
			String name = (String)item.getItemProperty("Name").getValue();
			Object obj = engine.get(name);
			StringBuffer out = new StringBuffer();
			for (Method m : obj.getClass().getMethods()) {
					out.append(m.getReturnType().getSimpleName()).append(" ");
					out.append(m.getName()).append("(");
					int nr = 0;
					for (Class<?> pt : m.getParameterTypes()) {
						if (nr != 0) out.append(", ");
						out.append(pt.getSimpleName()).append(" nr" + nr);
						nr++;
					}
					out.append(")\n");
			}
			bindingInfo.setValue(out.toString());
		} catch (Throwable t) {
			bindingInfo.setValue(t.toString());
			log.error("",t);
		}
	}

	protected void doExecute() {
		if (((Boolean)cbCleanOutput.getValue()).booleanValue()) output.setValue("");
		String cmd = (String)inputLine.getValue();
		text = new StringBuffer();
		text.append( (String)output.getValue() );
		text.append( "> " + cmd + "\n");
		try {
			Object ret = engine.eval( cmd );
			text.append( "< " + ret + "\n" );
			if (ret != null) window.showNotification(ret.toString());
		} catch (Throwable e) {
			log.error("",e);
			window.showNotification(e.toString(),Notification.TYPE_ERROR_MESSAGE );
			text.append(e.toString() + "\n");
		}
		
		output.setValue( text.toString() );
		text = null;
		if (((Boolean)cbCleanLine.getValue()).booleanValue()) inputLine.setValue("");
		
		if (((Boolean)cbBindingsUpdate.getValue()).booleanValue()) doRefreshBinding();
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
		engine.put("resolver", resourceResolver);
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
	
	private class MyWriter extends Writer {

		@Override
		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] buf, int off, int size) throws IOException {
			StringBuffer sb = text == null ? new StringBuffer() : text;
			sb.append(output.getValue());
			for (int i = 0; i < size; i++) {
				sb.append(buf[off+i]);
			}
			if (text == null) output.setValue(sb.toString());
		}
		
	}
}
