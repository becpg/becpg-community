package fr.becpg.repo.report.entity;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Element;

public interface EntityReportService {

	/**
	 * Load node attributes.
	 * @param nodeRef
	 * @return
	 */
	public Map<ClassAttributeDefinition, String> loadNodeAttributes(NodeRef nodeRef);
	
	/**
	 * Gets the image.
	 *
	 * @param nodeRef the node ref
	 * @return the image
	 */
	public byte[] getImage(NodeRef nodeRef);
	

	/**
	 * Checks if is report up to date.
	 *
	 * @param nodeRef the  node ref
	 * @return true, if is report up to date
	 */
	public boolean isReportUpToDate(NodeRef nodeRef);	
	
	/**
	 * Gets the document content writer.
	 *
	 * @param nodeRef the node ref
	 * @param tplNodeRef the tpl node ref
	 * @return the document content writer
	 */
	public ContentWriter getDocumentContentWriter(NodeRef nodeRef, NodeRef tplNodeRef);
	
	/**
	 * Generate reports.
	 *
	 * @param nodeRef the node ref
	 * @param tplsNodeRef the tpls node ref
	 * @param nodeElt the node elt
	 * @param images the images
	 */
	public void generateReports(NodeRef nodeRef, List<NodeRef> tplsNodeRef, Element nodeElt, Map<String, byte[]> images);
		
	/**
	 * Get the fields organized by sets
	 * @param nodeRef
	 * @param reportFormConfigPath
	 * @return
	 */
	public Map<String, List<String>> getFieldsBySets(NodeRef nodeRef, String reportFormConfigPath);
	
	/**
	 * Gets the report tpls to generate.
	 *
	 * @param nodeRef the node ref
	 * @return the report tpls to generate
	 */
	public List<NodeRef> getReportTplsToGenerate(NodeRef nodeRef);
		
}
