package com.albertopl.website2ebookmaker;

import com.albertopl.com.website2ebookmaker.gui.LoginWindow;
import com.albertopl.com.website2ebookmaker.gui.MainLayout;
import com.albertopl.com.website2ebookmaker.gui.StartModalWindow;
import com.albertopl.website2ebookmaker.account.Account;
import com.albertopl.website2ebookmaker.scraper.WebScraper;
import com.vaadin.Application;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class Website2EbookmakerApplication extends Application {
	
	private Window mainWindow;
	
	private MainLayout mainLayout;
	private WebScraper webScraper;
	
	@Override
	public void init() {
		mainWindow = new Window("Website 2 Ebook Maker Application");
		mainLayout = new MainLayout();
		mainLayout.setSizeFull();
		mainWindow.setContent(mainLayout);
		setMainWindow(mainWindow);
		
		webScraper = new WebScraper();
		
		StartModalWindow loginOrBeginWindow = new StartModalWindow();
		mainWindow.addWindow(loginOrBeginWindow);
	}
	
	public WebScraper getWebScraper() {
		return webScraper;
	}
	
	public MainLayout getMainLayout() {
		return mainLayout;
	}
	
	public void login() {
		LoginWindow loginWindow = new LoginWindow();
		mainWindow.addWindow(loginWindow);
	}

}
