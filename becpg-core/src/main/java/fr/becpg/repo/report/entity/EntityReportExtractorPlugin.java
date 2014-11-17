/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface used by classes that extract product data and product images to
 * generate report.
 *
 * @author querephi
 */
public interface EntityReportExtractorPlugin {

	public enum EntityReportExtractorPriority {
		HIGHT, NORMAL, LOW, NONE;

		public boolean isHigherPriority(EntityReportExtractorPriority compareTo) {
			if (LOW.equals(compareTo) && (NORMAL.equals(this) || HIGHT.equals(this)))
				return true;

			if (NORMAL.equals(compareTo) && HIGHT.equals(this))
				return true;

			return false;
		}
	}

	public EntityReportData extract(NodeRef entityNodeRef);

	public boolean shouldGenerateReport(NodeRef entityNodeRef);

	public EntityReportExtractorPriority getMatchPriority(QName type);

}
