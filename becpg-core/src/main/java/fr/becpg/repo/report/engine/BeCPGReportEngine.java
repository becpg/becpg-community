package fr.becpg.repo.report.engine;

import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface BeCPGReportEngine {


	
	
	public static String PARAM_FORMAT = "format";
	public static String PARAM_IMAGES = "images";

	public void createReport(NodeRef tplNodeRef, org.dom4j.Element nodeElt, OutputStream contentOutputStream, Map<String,Object> params);

}
