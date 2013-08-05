package com.albertopl.website2ebookmaker.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebScraper {

	private String root;
	private TreeNode pageRoot;
	private Map<String, Document> pages; //url to Document
	private Map<String, String> pageText; //url to Text of document (initially unformatted)
	private List<String> nodesAdded;
	
	public WebScraper() {
		pages = new HashMap<String, Document>();
		pageText = new HashMap<String, String>();
		pageRoot = null;
		nodesAdded = new ArrayList<String>();
	}
	
	public WebScraper(String root) {
		this();
		setRoot(root);
	}

	public void setRoot(String root) {
		if (!root.startsWith("http://")) {
			root = "http://" + root;
		}
		this.root = root;
	}
	
	public void scrape() {
		pageRoot = new TreeNode(root);
		scrape(pageRoot);
	}
	
	private TreeNode scrape(TreeNode node) {
		//only scrape if we haven't before
		String url = node.getName();
		if (!pages.containsKey(url)) {
			Document doc = null;
			boolean success = false;
			try {
				if (url.startsWith("http://")) {
					doc = Jsoup.connect(url).get();
				}
				else {
					doc = Jsoup.connect("http://" + url).get();
				}
				success = true;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (success) {
				pages.put(url, doc);
				pageText.put(url, doc.body().text());
				List<TreeNode> currentChildren = new ArrayList<TreeNode>();
				for (int x = 0; x < doc.select("a").size(); ++x) {
					Element link = doc.select("a").get(x);
					String linkHref = link.attr("href");
					if (linkHref.startsWith(root) && !nodesAdded.contains(linkHref)) {
						TreeNode child = new TreeNode(linkHref);
						node.addNode(child);
						child.setParent(node);
						nodesAdded.add(linkHref);
						currentChildren.add(child);
					}
				}
				
				//go through each link and call scrape if its in our domain
				for (TreeNode n: currentChildren) {
						scrape(n);
				}
				return node;
			}
		}
		return null;
	}
	
	public String getPageText(String url) {
		return pageText.get(url);
	}
	
	public void saveFormattedPageText(String url, String text) {
		pageText.put(url, text);
	}
	
	public Set<String> getPages() {
		return pages.keySet();
	}
	
	public TreeNode getRootPageNode() {
		return pageRoot;
	}
}
