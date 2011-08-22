package fr.becpg.repo.product.report;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemProductType;

/**
 * Class used to manage product report templates
 * @author querephi
 *
 */
public interface ProductReportTplService {

	/**
	 * Gets the system report templates.
	 *
	 * @param systemProductType
	 * @return the system report templates
	 */
	public List<NodeRef> getSystemReportTemplates(SystemProductType systemProductType);
	
	/**
	 * Gets the user report templates.
	 *
	 * @param systemProductType the system product type
	 * @param tplName the tpl name
	 * @return the user report templates
	 */
	public List<NodeRef> getUserReportTemplates(SystemProductType systemProductType, String tplName);
	
	/**
	 * Gets the report tpls to generate.
	 *
	 * @param productNodeRef the product node ref
	 * @return the report tpls to generate
	 */
	public List<NodeRef> getReportTplsToGenerate(NodeRef productNodeRef);
	
	/**
	 * Create a report template
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 * @param systemProductType
	 * @param isSystemTpl
	 * @param isDefaultTpl
	 * @return
	 * @throws IOException
	 */
	public NodeRef createTpl(NodeRef parentNodeRef, String tplName, String tplFilePath, SystemProductType systemProductType, boolean isSystemTpl, boolean isDefaultTpl) throws IOException;
	
	/**
	 * Check the default reports (return one default tpl)
	 * if there is a user default tpl, remove system default tpl and keep user one
	 * @param tplsNodeRef
	 * @return
	 */
	public List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef);
}
