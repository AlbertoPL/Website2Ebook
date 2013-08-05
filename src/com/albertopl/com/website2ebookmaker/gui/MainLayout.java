package com.albertopl.com.website2ebookmaker.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.albertopl.com.website2ebookmaker.data.CloudMineStorage;
import com.albertopl.website2ebookmaker.Website2EbookmakerApplication;
import com.albertopl.website2ebookmaker.account.Account;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MainLayout extends VerticalLayout {

	private HorizontalSplitPanel splitPanel;
	private Tree webpageTree;
	private String websitename;
	private RichTextArea webpageText;
	private String currentPage;
	private String lastCommandBeforeLogin; //command to call when we are done logging in
	private List<Object> lastCommandParams; //parameters of the command
	
	private MenuBar menubar;
	
	public MainLayout() {
		super();
		this.setSizeFull();
		this.init();
		currentPage = "";
		lastCommandBeforeLogin = null;
		lastCommandParams = new ArrayList<Object>();
	}
	
	private void buildMenu() {
		menubar = new MenuBar();
		menubar.setWidth(100, Sizeable.UNITS_PERCENTAGE);

		final MenuBar.MenuItem file = menubar.addItem("File", null);
        final MenuBar.MenuItem newItem = file.addItem("New", null);
        file.addItem("Open file...", null);
        file.addSeparator();

        newItem.addItem("File", null);
        newItem.addItem("Folder", null);
        newItem.addItem("Project...", null);

        file.addItem("Close", null);
        file.addItem("Close All", null);
        file.addSeparator();

        file.addItem("Save", saveCommand);
        file.addItem("Save As...", null);
        file.addItem("Save All", null);
        file.addSeparator();
        file.addItem("Log Out", null);

        final MenuBar.MenuItem edit = menubar.addItem("Edit", null);
        edit.addItem("Undo", null);
        edit.addItem("Redo", null).setEnabled(false);
        edit.addSeparator();

        edit.addItem("Cut", null);
        edit.addItem("Copy", null);
        edit.addItem("Paste", null);
        edit.addSeparator();

        final MenuBar.MenuItem find = edit.addItem("Find/Replace", null);

        find.addSeparator();
        find.addItem("Find/Replace...", null);
        find.addItem("Find Next", null);
        find.addItem("Find Previous", null);

        final MenuBar.MenuItem view = menubar.addItem("View", null);
        view.addItem("Show/Hide Status Bar", null);
        view.addItem("Customize Toolbar...", null);

        this.addComponent(menubar);
	}
	
	public void init() {
		buildMenu();
		
		//make a split panel
		splitPanel = new HorizontalSplitPanel();
		splitPanel.setSizeFull();
		splitPanel.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);
		splitPanel.setLocked(false);
		splitPanel.setVisible(true);
	
		//make a tree as the first component
		webpageTree = new Tree("Your Site's Web Pages");
		webpageTree.setSizeUndefined();
		webpageTree.addItem("Your Website");
		webpageTree.setImmediate(true);
		
		splitPanel.setFirstComponent(webpageTree);
		
		//make a text area as the second component
		webpageText = new RichTextArea();
		webpageText.setSizeFull();
		splitPanel.setSecondComponent(webpageText);
		
		webpageTree.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					
					Website2EbookmakerApplication app = ((Website2EbookmakerApplication)MainLayout.this.getApplication());
		            if (currentPage.equals(websitename)) {     	
						app.getWebScraper().saveFormattedPageText(currentPage, (String) webpageText.getValue());
		            }
		            else if (!currentPage.isEmpty()) {
		            	if (currentPage.startsWith(websitename)) {
							app.getWebScraper().saveFormattedPageText(currentPage, (String) webpageText.getValue());
		            	}
		            	else {
							app.getWebScraper().saveFormattedPageText(websitename + "/" + currentPage, (String) webpageText.getValue());
		            	}
		            }
					
					if (!((String)event.getProperty().getValue()).startsWith(websitename)) {
		            	webpageText.setValue(app.getWebScraper().getPageText(websitename + "/" + (String) event.getProperty().getValue()));
		            }
		            else {
		     
		            	webpageText.setValue(app.getWebScraper().getPageText((String) event.getProperty().getValue()));
		            }
		            currentPage = (String) event.getProperty().getValue();
		            webpageText.requestRepaint();
		        } else {
		        	webpageText.setValue("");
		        }				
			}
		});
		
		this.addComponent(splitPanel);
		this.setExpandRatio(splitPanel, 0.9f);
	}
	
	public void renameTree(String websitename) {
		webpageTree.removeAllItems();
		webpageTree.addItem(websitename);
		this.websitename = websitename;
	}
	
	public void addTreeItem(String parent, String url) {
		webpageTree.addItem(url);
		webpageTree.setParent(url, parent);
	}
	
	public void openTree(String websitename) {
		webpageTree.expandItemsRecursively(websitename);
	}
	
	//sometimes we want to do things that require logging in. However, the log in process is
	//asynchronous and code you don't want to execute goes ahead anyway. This means the process
	//you wanted to have authentication for goes on ahead well before the user has a chance to
	//log in. The answer is to store our command and its parameters temporarily and then have
	//the successful login call the command that was happening.
	public void executeLastCommandBeforeLogin() {
		try {
			this.getClass().getMethod(this.lastCommandBeforeLogin, new Class[] {MenuItem.class}).invoke(this, this.lastCommandParams.toArray());
			lastCommandBeforeLogin = null;
			lastCommandParams.clear();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	//MUST be public for invocation via reflection
	public void save(MenuItem selectedItem) {
		if (selectedItem.getText().equalsIgnoreCase("Save")) {
    		//check if logged in...
    		Website2EbookmakerApplication app = ((Website2EbookmakerApplication)MainLayout.this.getApplication());
    		if (((Account) app.getUser()).isGuest()) {
    			app.login();
    			MainLayout.this.lastCommandBeforeLogin = "save";
    			MainLayout.this.lastCommandParams.add(selectedItem);
    		}
    		else {
    			StringBuilder jsonString = new StringBuilder();
				jsonString.append('{');
				jsonString.append("\"" + "Key" + "\": {");
					String websitenameString = websitename.replace("http://www.", "");
    			for (String page: app.getWebScraper().getPages()) {
    				jsonString.append("\"" + websitenameString + "\": \"" + app.getWebScraper().getPageText(page).replace(websitename, "").substring(1) + "\",");
    			}
    			jsonString.setCharAt(jsonString.length()-1, ' ');
    			jsonString.append("}}");
				int responseCode = CloudMineStorage.updateWebpageData(jsonString.toString(), ((Account)app.getUser()).getPasswordHash());
    			if (responseCode == 200 || responseCode == 201) {
    				System.out.println("Saved!");
    			}
    			else {
    				//System.out.println(jsonString.toString());
    				System.out.println("Save failed! Error code: " + responseCode);
    			}
    		}
    	}
	}
	
	private Command saveCommand = new Command() {
        public void menuSelected(MenuItem selectedItem) {
        	save(selectedItem);
        }
    };

	
}
