package fr.becpg.repo.report.engine;

import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface BeCPGReportEngine {

	public void createReport(NodeRef tplNodeRef, org.dom4j.Element nodeElt, OutputStream out, Map<String,Object> params);

}
