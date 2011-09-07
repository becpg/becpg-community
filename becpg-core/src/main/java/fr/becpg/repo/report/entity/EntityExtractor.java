/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.entity;

import java.util.Map;

import org.dom4j.Element;

/**
 * Interface used by classes that extract product data and product images to generate report.
 *
 * @author querephi
 */
public interface EntityExtractor{
	
	/**
	 * Get extracted data.
	 *
	 * @return the xml data
	 */
	public Element getXmlData();
	
	/**
	 * Get extracted images.
	 *
	 * @return the images
	 */
	public Map<String, byte[]> getImages();
	
	/**
	 * Extract data and images needed by report.
	 */
	public void extract();
}
