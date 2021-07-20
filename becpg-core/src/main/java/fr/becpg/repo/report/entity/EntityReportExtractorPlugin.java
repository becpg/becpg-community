/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.report.entity;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface used by classes that extract product data and product images to
 * generate report.
 *
 * @author querephi
 * @version $Id: $Id
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

	/**
	 * <p>extract.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param preferences a {@link java.util.Map} object.
	 * @return a {@link fr.becpg.repo.report.entity.EntityReportData} object.
	 */
	EntityReportData extract(NodeRef entityNodeRef, Map<String, String> preferences);

	/**
	 * <p>shouldGenerateReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param generatedReportDate a {@link java.util.Date} object.
	 * @return a boolean.
	 */
	boolean shouldGenerateReport(NodeRef entityNodeRef, Date generatedReportDate);

	/**
	 * <p>getMatchPriority.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.report.entity.EntityReportExtractorPlugin.EntityReportExtractorPriority} object.
	 */
	EntityReportExtractorPriority getMatchPriority(QName type);

	void handleReportLogInfos(NodeRef entityNodeRef, Set<ReportLogInfo> logInfos);

	void cleanTemplateLogInfos(NodeRef entityNodeRef, NodeRef tplNodeRef);


}
