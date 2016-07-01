/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.report.entity;

import java.io.OutputStream;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.report.client.ReportFormat;

public interface EntityReportService {

	void generateReport(NodeRef entityNodeRef);

	String getXmlReportDataSource(NodeRef entityNodeRef);

	void setPermissions(NodeRef tplNodeRef, NodeRef documentNodeRef);

	boolean shouldGenerateReport(NodeRef entityNodeRef);

	NodeRef getSelectedReport(NodeRef entityNodeRef);

	String getSelectedReportName(NodeRef entityNodeRef);

	void generateReport(NodeRef entityNodeRef, NodeRef documentNodeRef, ReportFormat reportFormat, OutputStream outputStream);

	EntityReportExtractorPlugin retrieveExtractor(NodeRef entityNodeRef);
}
