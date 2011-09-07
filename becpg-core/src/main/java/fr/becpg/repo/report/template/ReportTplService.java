package fr.becpg.repo.report.template;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface ReportTplService {

	public static final String PARAM_VALUE_DESIGN_EXTENSION = ".rptdesign";
	
	/**
	 * Gets the system report templates.
	 *
	 * @param nodeType
	 * @return the system report templates
	 */
	public List<NodeRef> getSystemReportTemplates(ReportType reportType, QName nodeType);
	
	/**
	 * Gets the user report templates.
	 *
	 * @param nodeType the node type
	 * @param tplName the tpl name
	 * @return the user report templates
	 */
	public List<NodeRef> suggestUserReportTemplates(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Create a report template
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 * @param nodeType
	 * @param isSystemTpl
	 * @param isDefaultTpl
	 * @return
	 * @throws IOException
	 */
	public NodeRef createTpl(NodeRef parentNodeRef, String tplName, String tplFilePath, ReportType reportType, QName nodeType, boolean isSystemTpl, boolean isDefaultTpl) throws IOException;
	
	/**
	 * Check the default reports (return one default tpl)
	 * if there is a user default tpl, remove system default tpl and keep user one
	 * @param tplsNodeRef
	 * @return
	 */
	public List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef);
	
	/**
	 * Get the report format
	 * @param tplNodeRef
	 * @return
	 */
	public ReportFormat getReportFormat(NodeRef tplNodeRef);
}
