package fr.becpg.repo.report.entity;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

public class EntityReportData {
	
	private Element xmlDataSource;
	
	private Map<String, byte[]> dataObjects = new HashMap<String, byte[]>();
	/**
	 * @return the xmlDataSource
	 */
	public Element getXmlDataSource() {
		return xmlDataSource;
	}
	/**
	 * @param xmlDataSource the xmlDataSource to set
	 */
	public void setXmlDataSource(Element xmlDataSource) {
		this.xmlDataSource = xmlDataSource;
	}
	/**
	 * @return the dataObjects
	 */
	public Map<String, byte[]> getDataObjects() {
		return dataObjects;
	}
	/**
	 * @param dataObjects the dataObjects to set
	 */
	public void setDataObjects(Map<String, byte[]> dataObjects) {
		this.dataObjects = dataObjects;
	}
	
	
}
