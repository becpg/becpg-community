/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.entity;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface used by classes that extract product data and product images to
 * generate report.
 *
 * @author querephi
 */
public interface EntityReportExtractorPlugin {

	enum EntityReportExtractorPriority {
		HIGHT, NORMAL, LOW, NONE;

		public boolean isHigherPriority(EntityReportExtractorPriority compareTo) {
			if (LOW.equals(compareTo) && (NORMAL.equals(this) || HIGHT.equals(this)))
				return true;

			if (NORMAL.equals(compareTo) && HIGHT.equals(this))
				return true;

			return false;
		}
	}

	EntityReportData extract(NodeRef entityNodeRef);

	boolean shouldGenerateReport(NodeRef entityNodeRef, Date generatedReportDate);

	EntityReportExtractorPriority getMatchPriority(QName type);

}
