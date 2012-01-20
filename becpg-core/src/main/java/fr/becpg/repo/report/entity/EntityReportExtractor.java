/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface used by classes that extract product data and product images to generate report.
 *
 * @author querephi
 */
public interface EntityReportExtractor{
	
	
	public EntityReportData extract(NodeRef entityNodeRef);
	
	
}
