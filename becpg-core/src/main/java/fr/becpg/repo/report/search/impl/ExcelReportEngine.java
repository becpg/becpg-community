package fr.becpg.repo.report.search.impl;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

/**
 * <p>ExcelReportEngine class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ExcelReportEngine implements BeCPGReportEngine {

	@Autowired
	ExcelReportSearchRenderer renderer;

	/** {@inheritDoc} */
	@Override
	public void createReport(NodeRef tplNodeRef, EntityReportData reportData, OutputStream out, Map<String, Object> params) throws ReportException {

		renderer.renderReport(tplNodeRef, Arrays.asList((NodeRef) params.get(BeCPGReportEngine.PARAM_ENTITY_NODEREF)),
				(ReportFormat) params.get(ReportParams.PARAM_FORMAT), out);

	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return renderer.isApplicable(templateNodeRef, reportFormat);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isXmlEngine() {
		return false;
	}

}
