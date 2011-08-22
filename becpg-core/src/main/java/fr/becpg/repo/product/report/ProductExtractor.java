/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;

import java.util.Map;

import org.dom4j.Element;

// TODO: Auto-generated Javadoc
/**
 * Interface used by classes that extract product data and product images to generate report.
 *
 * @author querephi
 */
public interface ProductExtractor{
	
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
