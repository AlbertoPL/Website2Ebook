package com.albertopl.com.website2ebookmaker.gui;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.validator.EmailValidator;

import com.albertopl.com.website2ebookmaker.data.CloudMineStorage;
import com.albertopl.website2ebookmaker.account.Account;
import com.vaadin.data.Validator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class LoginWindow extends Window {
	
	private PasswordField password;

	public LoginWindow() {
		super("User Account");
		this.setModal(true);
		this.setClosable(true);
		this.init();
	}
	
	private void init() {
		password = new PasswordField("Password:");
		password.setVisible(false);
		final VerticalLayout layout = (VerticalLayout) this.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        password.setInputPrompt("Enter your password");
		password.setSizeFull();
		password.setRequired(true);
		password.setRequiredError("Please enter your account password");
        
        Label message = new Label("<b>We need your email address so you can save your work! (You'll be able to come back to it later)");
        message.setContentMode(Label.CONTENT_XHTML);
        message.setSizeUndefined();
        this.addComponent(message);

        final TextField emailField = new TextField();
        emailField.setInputPrompt("Enter your email address here");
        emailField.setSizeFull();
        emailField.setRequired(true);
        emailField.setRequiredError("Please enter a valid email address");
        emailField.addValidator(new FormEmailValidator(
                "The email address entered is not valid! (It should look like account@service.com)"));
        layout.addComponent(emailField);
        layout.addComponent(password);
        
        HorizontalLayout bottombar = new HorizontalLayout();
        
        final Button okay = new Button("OK");
        okay.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
            	if (emailField.isValid()) {
            		if (!password.isVisible()) {
            			String[] returnValues = CloudMineStorage.userAccountStorage((String) emailField.getValue());
            			if (returnValues[0].equalsIgnoreCase("201")) {
    	            		//Account Created!
                			System.out.println("Account created and stored: " + emailField.getValue());
    	            		((Account)LoginWindow.this.getApplication().getUser()).setUsername((String) emailField.getValue());
    	            		sendEmail(returnValues[1]);
    	            		((Account)LoginWindow.this.getApplication().getUser()).setCloudMinePasswordHash(returnValues[1]);
    	            		((MainLayout)LoginWindow.this.getApplication().getMainWindow().getContent()).executeLastCommandBeforeLogin();
    	            		(LoginWindow.this.getParent()).removeWindow(LoginWindow.this);
                		}
                		else if (returnValues[0].equalsIgnoreCase("401")) {
                			//Account already existed! Prompt for credentials!
                			password.setVisible(true);
                			LoginWindow.this.setCaption("Your account exists! Please enter your account password!");
                		}
                		else if (returnValues[0].equalsIgnoreCase("400")) {
                			//Email address was invalid for some reason!
                			getWindow().showNotification(
                                    "Bad login attempt!",
                                    "The system had difficulty logging you in with your email address! " +
                                    "Click this box to try again.",
                                    Notification.TYPE_ERROR_MESSAGE);
                		}
            		}
            		else {
            			int responseCode = CloudMineStorage.userAccountStorage((String) emailField.getValue(), (String) password.getValue());
            			if (responseCode == 200) {
            				System.out.println(emailField.getValue() + " logged in successfully!");
    	            		((Account)LoginWindow.this.getApplication().getUser()).setUsername((String) emailField.getValue());
    	            		((Account)LoginWindow.this.getApplication().getUser()).setCloudMinePasswordHash((String) password.getValue());
    	            		((MainLayout)LoginWindow.this.getApplication().getMainWindow().getContent()).executeLastCommandBeforeLogin();
    	            		(LoginWindow.this.getParent()).removeWindow(LoginWindow.this);
            			}
            			else if (responseCode == 401) {
            				//Bad password!
            				getWindow().showNotification(
                                    "Bad login attempt!",
                                    "Your username (email address) and/or your password is incorrect! " +
                                    "Click this box to try again.",
                                    Notification.TYPE_ERROR_MESSAGE);
            			}
            			else if (responseCode == 400) {
            				//Email address was invalid for some reason!
            				getWindow().showNotification(
                                    "Bad login attempt!",
                                    "The system had difficulty logging you in with your email address! " +
                                    "Click this box to try again.",
                                    Notification.TYPE_ERROR_MESSAGE);
            			}
            			else {
            				System.out.println(responseCode);
            			}
            		}
            		
            	}
            }
        });
        
        final Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				(LoginWindow.this.getParent()).removeWindow(LoginWindow.this);
			}
		});
        // The components added to the window are actually added to the window's
        // layout; you can use either. Alignments are set using the layout
        bottombar.addComponent(okay);
        bottombar.addComponent(cancel);
        layout.addComponent(bottombar);
        layout.setComponentAlignment(bottombar, Alignment.BOTTOM_CENTER);
        layout.setSizeUndefined();
	}
	
	private void sendEmail(String password) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
 
		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("apareja@albertopl.com","Crowtche1");
					}
				});
		try {
			 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("admin@webpage2ebook.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(((Account)LoginWindow.this.getApplication().getUser()).getUsername()));
			message.setSubject("Your Webpage 2 Ebook Account");
			message.setText("Dear user," +
					"\n\n Thank you for registering! We have created an account for you" +
					"so you may always save your progress or even finalize a PDF of your" +
					"E-book! \n\n\n Username: " + ((Account)LoginWindow.this.getApplication().getUser()).getUsername() + "\n\n" +
					" Password: " + password);
 
			Transport.send(message);
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public class FormEmailValidator implements Validator {

        private String message;

        public FormEmailValidator(String message) {
            this.message = message;
        }

        public boolean isValid(Object value) {
        	return EmailValidator.getInstance().isValid((String) value);
        }

        public void validate(Object value) throws InvalidValueException {
        	if (!isValid(value)) {
                throw new InvalidValueException(message);
            }
        }
    }
}
