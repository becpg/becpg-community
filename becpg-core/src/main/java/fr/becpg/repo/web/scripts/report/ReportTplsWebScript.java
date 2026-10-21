/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

/**
 * The Class ExportSearchTplsWebScript.
 *
 * @author querephi, matthieu
 * @version $Id: $Id
 */
public class ReportTplsWebScript extends DeclarativeWebScript {

	/** Constant <code>PARAM_ENTITY_NODEREF="entityNodeRef"</code> */
	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";
	/** Constant <code>PARAM_DATATYPE="datatype"</code> */
	private static final String PARAM_DATATYPE = "datatype";
	/** Constant <code>PARAM_REPORT_TYPE="type"</code> */
	private static final String PARAM_REPORT_TYPE = "type";
	/** Constant <code>MODEL_KEY_NAME_REPORT_TEMPLATES="reportTpls"</code> */
	private static final String MODEL_KEY_NAME_REPORT_TEMPLATES = "reportTpls";

	private NamespaceService namespaceService;

	private ReportTplService reportTplService;

	private AssociationService associationService;

	private DictionaryService dictionaryService;

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>reportTplService</code>.</p>
	 *
	 * @param reportTplService a {@link fr.becpg.repo.report.template.ReportTplService} object.
	 */
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		// get datatype
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String datatype = templateArgs.get(PARAM_DATATYPE);
		String reportType = templateArgs.get(PARAM_REPORT_TYPE);
		ReportType type = ReportType.ExportSearch;

		if ((reportType != null) && !reportType.isEmpty()) {
			for (ReportType tmp : ReportType.values()) {
				if (reportType.equalsIgnoreCase(tmp.toString())) {
					type = tmp;
					break;
				}
			}
		}

		QName datatypeQName = null;
		if ((datatype != null) && (datatype.length() > 0)) {
			datatypeQName = QName.createQName(datatype, namespaceService);
		}

		List<NodeRef> reportTpls = ReportType.ExportSearch.equals(type) ? getExportSearchTemplates(datatypeQName)
				: reportTplService.getUserReportTemplates(type, datatypeQName, null);
		Map<String, Object> model = new HashMap<>();
		model.put(MODEL_KEY_NAME_REPORT_TEMPLATES, reportTpls);

		if (ReportType.Compare.equals(type) && (req.getParameter(PARAM_ENTITY_NODEREF) != null)) {
			List<NodeRef> entityNodeRefs = associationService.getTargetAssocs(new NodeRef(req.getParameter(PARAM_ENTITY_NODEREF)),
					BeCPGModel.ASSOC_COMPARE_WITH_ENTITIES);
			if ((entityNodeRefs != null) && !entityNodeRefs.isEmpty()) {
				model.put("entities", entityNodeRefs.stream().map(NodeRef::toString).collect(Collectors.joining(",")));
			}

		}

		return model;
	}

	/**
	 * Returns the export search templates of a type and of its parent types, the most specific
	 * first: a template declared on <code>bcpg:product</code> also exports its sub types.
	 *
	 * @param datatype the searched type, or <code>null</code> for the templates bound to no type
	 * @return the templates, without duplicates
	 */
	List<NodeRef> getExportSearchTemplates(QName datatype) {
		Set<NodeRef> reportTpls = new LinkedHashSet<>(reportTplService.getUserReportTemplates(ReportType.ExportSearch, datatype, null));
		QName parentType = getParentType(datatype);
		while (parentType != null) {
			reportTpls.addAll(reportTplService.getUserReportTemplates(ReportType.ExportSearch, parentType, null));
			parentType = getParentType(parentType);
		}
		return new ArrayList<>(reportTpls);
	}

	private QName getParentType(QName type) {
		ClassDefinition classDefinition = type != null ? dictionaryService.getClass(type) : null;
		return classDefinition != null ? classDefinition.getParentName() : null;
	}
}
