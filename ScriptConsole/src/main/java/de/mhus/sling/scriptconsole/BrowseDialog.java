package de.mhus.sling.scriptconsole;

import java.util.Iterator;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class BrowseDialog extends Window {

	public static final Object PROPERTY_NAME = "name";
	private static final Object PROPERTY_TYPE = "type";
	
	private static Logger log = LoggerFactory.getLogger(BrowseDialog.class);

	public static void show(Window parent, String title, boolean canInput, ResourceResolver resolver, Action.Listener onClose) throws Exception {
		
		BrowseDialog dialog = new BrowseDialog(title, canInput, resolver);
		dialog.setCloseListener(onClose);
		parent.addWindow(dialog);
		
	}

	private Action.Listener closeListener;
	private TreeTable tree;
	private ResourceResolver resolver;
	private Button select;
	private TextField inputLine;
	
	public void setCloseListener(Action.Listener onClose) {
		this.closeListener = onClose;
	}

	public BrowseDialog(String title, boolean canInput, ResourceResolver resolverx) throws Exception {
		setModal(true);
		setCaption(title);
		this.resolver = resolverx.clone(null);
		
		VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        setWidth("650px");
        setHeight("600px");
        
        tree = new TreeTable("Repository");
        
        tree.setImmediate(true);
        tree.setWidth("600px");
        tree.setHeight("450px");
        tree.setPageLength(0);
        tree.setSelectable(true);
        tree.setMultiSelect(false);
        
        tree.addContainerProperty(PROPERTY_NAME, String.class, "");
        tree.addContainerProperty(PROPERTY_TYPE, String.class, "");
  
        tree.setColumnWidth(PROPERTY_NAME, 350);
        
        Resource rootResource = resolverx.getResource("/");
        Object rootItem = tree.addItem(new Object[] { "Root", rootResource, }, "/");
        
        tree.addListener(new Tree.ExpandListener() {
			
			@Override
			public void nodeExpand(ExpandEvent event) {
				Object openedItemId = event.getItemId();
                if (!tree.hasChildren(openedItemId)) {
                	Resource res = resolver.getResource((String)openedItemId);
                	for ( Iterator<Resource> iter = res.listChildren(); iter.hasNext();) {
                		Resource child = iter.next();
                		tree.addItem(new Object[] { child.getName(), child.getResourceType()}, child.getPath() );
                		if (child.getResourceType().equals("nt:file")) {
                			tree.setChildrenAllowed( child.getName(), false);
                		}
                		tree.setParent(child.getPath(), openedItemId);
                	}
                }
			}
		});

        tree.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				validateSelection();
			}
		});
        
        tree.addListener(new ItemClickEvent.ItemClickListener() {
			
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.isDoubleClick()) {
					doSelect();
				}
			}
		} );
        
//        tree.setContainerDataSource(new RepoDataSource());
        
        layout.addComponent(tree);
        
        if (canInput) {
        	inputLine = new TextField();
        	inputLine.setWidth("100%");
        	inputLine.addShortcutListener(new ShortcutListener("ENTER",KeyCode.ENTER, new int[] {}) {
    			
    			@Override
    			public void handleAction(Object sender, Object target) {
    				doSelect();
    			}
    		});
        	layout.addComponent(inputLine);
        }
        HorizontalLayout buttonBar = new HorizontalLayout();
        
        Button close = new Button("Close", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) {
                doClose();
            }
        });
        
        buttonBar.addComponent(close);
        
        select = new Button("Select", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) {
            	doSelect();
            }
        });
        
        buttonBar.addComponent(select);
        
        layout.addComponent(buttonBar);
	}

	protected void doClose() {
		// close the window by removing it from the parent window
        (getParent()).removeWindow(BrowseDialog.this);
        if (closeListener != null) closeListener.handleAction(BrowseDialog.this, null);
	}

	protected void doSelect() {
		validateSelection();
    	
    	if (!select.isEnabled()) return;
    	
    	String id = (String) tree.getValue();
    	if (id == null) return;
    	
    	Resource res = resolver.getResource(id);
    	if (res == null) return;
    	
    	// call the listener. If this caused an exception the window will not close.
    	try {
    		if (closeListener != null) closeListener.handleAction(BrowseDialog.this, res); // TODO selection
    	} catch (Throwable t) {
    		log.error("",t);
    		return;
    	}
    	// close the window by removing it from the parent window
        (getParent()).removeWindow(BrowseDialog.this);
	}

	protected void validateSelection() {
		
		select.setEnabled(false);
		if (inputLine!=null) inputLine.setEnabled(false);
		
    	String id = (String) tree.getValue();
    	if (id == null) return;
    	
    	Resource res = resolver.getResource(id);
    	if (res == null) return;
		
    	if (inputLine!=null && !res.getResourceType().equals("nt:file")) {
    		inputLine.setEnabled(true);
    	} else {
    		if (!id.endsWith(".bsh") ) return;
    	}
    	
    	select.setEnabled(true);
	}
	
	public String getInputLine() {
		return inputLine == null || !inputLine.isEnabled() ? null : (String) inputLine.getValue();
	}
	
//	private class RepoDataSource extends HierarchicalContainer {
//		private RepoDataSource() {
//			addContainerProperty(PROPERTY_NAME, String.class, null);
//		}
//	}
}
