package fr.becpg.repo.listvalue;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO to store list value entry 
 * @author matthieu
 *
 */
public class ListValueEntry {


	private String value;
	private String name;
	private String cssClass;
	private Map<String,String> metadatas ;

	
	public ListValueEntry(String value, String name, String cssClass) {
		super();
		this.value = value;
		this.name = name;
		this.cssClass = cssClass;
		this.metadatas = new HashMap<String,String>();
	}
	
	public ListValueEntry(String value, String name, String cssClass, Map<String,String> metadatas) {
		super();
		this.value = value;
		this.name = name;
		this.cssClass = cssClass;
		this.metadatas = metadatas;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public Map<String, String> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(Map<String, String> metadatas) {
		this.metadatas = metadatas;
	}

	
	
	
	
}
