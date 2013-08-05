package com.albertopl.com.website2ebookmaker.gui;

import java.util.ArrayList;
import java.util.List;

import com.albertopl.website2ebookmaker.Website2EbookmakerApplication;
import com.albertopl.website2ebookmaker.account.Account;
import com.albertopl.website2ebookmaker.scraper.TreeNode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class StartModalWindow extends Window {

	private String url;
    final ProgressIndicator progressBar = new ProgressIndicator();
	
	public StartModalWindow() {
		super("Welcome!");
		this.setModal(true);
		this.setClosable(false);
		url = "";
		this.init();
	}
	
	private void init() {
		VerticalLayout layout = (VerticalLayout) this.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        
        Label message = new Label("<b>Please type your website URL to begin making your E-book! (e.g. www.example.com)</b>");
        message.setContentMode(Label.CONTENT_XHTML);
        message.setSizeUndefined();
        this.addComponent(message);

        final TextField urlField = new TextField();
        urlField.setInputPrompt("Enter your website URL here");
        urlField.setSizeFull();
        layout.addComponent(urlField);
        
        HorizontalLayout bottombar = new HorizontalLayout();
        
        progressBar.setIndeterminate(true);
        progressBar.setPollingInterval(5000);
        progressBar.setEnabled(false);
        bottombar.addComponent(progressBar);
        
        final Button okay = new Button("Okay!");
        okay.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
            	if (!(((String)urlField.getValue()).startsWith("www.") || ((String)urlField.getValue()).startsWith("http://"))) {
            		getWindow().showNotification(
                            "I couldn't recognize the website name!",
                            "Did you make sure to type www. or http:// in front of your website url? " +
                            "Click this box to try again.",
                            Notification.TYPE_ERROR_MESSAGE);
            	}
            	else {
                	url = (String) urlField.getValue();
                	StartModalWindow.this.getApplication().setUser(new Account());
                	
                	//wait until website is scraped
                	Worker worker = new Worker();
                	worker.start();
                	progressBar.setEnabled(true);
                	progressBar.setVisible(true);
                    urlField.setEnabled(false);
                	okay.setCaption("Please wait while we grab your site...");
                	okay.setEnabled(false);
            	}
            }
        });
        // The components added to the window are actually added to the window's
        // layout; you can use either. Alignments are set using the layout
        bottombar.addComponent(okay);
        layout.addComponent(bottombar);
        layout.setComponentAlignment(bottombar, Alignment.BOTTOM_CENTER);
        layout.setSizeUndefined();
	}
	
	public void done(String url) {
		progressBar.setEnabled(false);
    	progressBar.setVisible(false);
    	((Account)StartModalWindow.this.getApplication().getUser()).addWebsite(url);

		(StartModalWindow.this.getParent()).removeWindow(StartModalWindow.this);
	}
	
	public class Worker extends Thread {
        @Override
        public void run() {
            Website2EbookmakerApplication app = ((Website2EbookmakerApplication)StartModalWindow.this.getApplication());
            app.getWebScraper().setRoot(url);
            app.getWebScraper().scrape();
            if (!url.startsWith("http://")) {
            	url = "http://" + url;
    		}
            app.getMainLayout().renameTree(url);
            TreeNode node = app.getWebScraper().getRootPageNode();
            List<TreeNode> children = new ArrayList<TreeNode>();
            children.addAll(node.getChildren());
            while (!children.isEmpty()) {
            	TreeNode child = children.remove(0);
            	if (child.getParent().getName().equals(url)) {
            		app.getMainLayout().addTreeItem(child.getParent().getName(), child.getName().replace(url + "/", ""));
            	}
            	else {
            		app.getMainLayout().addTreeItem(child.getParent().getName().replace(url + "/", ""), child.getName().replace(url + "/", ""));
            	}
            	children.addAll(child.getChildren());
            }
            app.getMainLayout().openTree(url);
            // synchronize changes over application
            synchronized (getApplication()) {
                done(url);
            }
        }
    }

}
