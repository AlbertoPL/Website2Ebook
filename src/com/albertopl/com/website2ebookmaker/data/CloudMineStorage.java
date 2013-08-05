package com.albertopl.com.website2ebookmaker.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;

import com.Ostermiller.util.Base64;
import com.Ostermiller.util.RandPass;

public class CloudMineStorage {

	private final static String CLOUDMINE_API_ID = "0d75a2cbdf1e46efab985118e78c4c1b";
	private final static String CLOUDMINE_SECRET_API_KEY = "f0314a23ae7e41be9068a8193af93aa3";
	private final static String CLOUDMINE_ACCOUNT_POST = "https://api.cloudmine.me/v1/app/" + CLOUDMINE_API_ID + "/user/account";
	private final static String CLOUDMINE_ACCOUNT_TEXT_POST = "https://api.cloudmine.me/v1/app/" + CLOUDMINE_API_ID + "/user/text";
	private final static String CONTENT_TYPE_JSON = "application/json";
	
	//only return the status code, the user input the password so doesn't need to be returned
	public static int userAccountStorage(String username, String password) {
		String encoding = Base64.encode(username + ":" + password);
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(CLOUDMINE_ACCOUNT_POST);
		httppost.addHeader("X-CloudMine-ApiKey", CLOUDMINE_SECRET_API_KEY);
		httppost.addHeader("Authorization", "Basic " + encoding);
		try {
			HttpResponse response = httpclient.execute(httppost);
			return response.getStatusLine().getStatusCode();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	//return the status code and the generated password
	public static String[] userAccountStorage(String username) {
		String password = new RandPass().getPass(16);
		String encoding = Base64.encode(username + ":" + password);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(CLOUDMINE_ACCOUNT_POST);
		httppost.addHeader("X-CloudMine-ApiKey", CLOUDMINE_SECRET_API_KEY);
		httppost.addHeader("Authorization", "Basic " + encoding);
		try {
			HttpResponse response = httpclient.execute(httppost);
			return new String[]{String.valueOf(response.getStatusLine().getStatusCode()), password};
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new String[]{"-1", password};
	}
	
	//A webpage is the key to the key value pairs of pagename : content
	public static int updateWebpageData(final String content, String encoding) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut httpput = new HttpPut(CLOUDMINE_ACCOUNT_TEXT_POST);
		httpput.addHeader("Content-Type", CONTENT_TYPE_JSON);
		httpput.addHeader("X-CloudMine-ApiKey", CLOUDMINE_SECRET_API_KEY);
		httpput.addHeader("Authorization", "Basic " + encoding);
		
		ContentProducer cp = new ContentProducer() {
		    public void writeTo(OutputStream outstream) throws IOException {
		        Writer writer = new OutputStreamWriter(outstream, "UTF-8");
		        writer.write(content);
		        writer.flush();
		    }
		};
		HttpEntity entity = new EntityTemplate(cp);
		httpput.setEntity(entity);
		
		try {
			HttpResponse response = httpclient.execute(httpput);
			return response.getStatusLine().getStatusCode();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
}
