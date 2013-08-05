package com.albertopl.website2ebookmaker.scraper;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;


public class TreeNode {
	private TreeNode parent;
	private List<TreeNode> children;
	private String name;
	private Document value;
	
	public TreeNode(String name) {
		this.name = name;
		children = new ArrayList<TreeNode>();
		value = null;
		parent = null;
	}
	
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	
	public TreeNode getParent() {
		return parent;
	}
	
	public void addNode(TreeNode n) {
		children.add(n);
	}
	
	public TreeNode getChild(int pos) {
		return children.get(pos);
	}
	
	public List<TreeNode> getChildren() {
		return children;
	}
	
	public String getName() {
		return name;
	}
	
	public Document getValue() {
		return value;
	}
	
	public void setValue(Document doc) {
		this.value = doc;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
}
