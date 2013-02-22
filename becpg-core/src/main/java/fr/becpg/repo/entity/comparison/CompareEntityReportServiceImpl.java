/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

// TODO: Auto-generated Javadoc
/**
 * The Class CompareEntityReportServiceImpl.
 *
 * @author querephi
 */
public class CompareEntityReportServiceImpl  implements CompareEntityReportService{
	
	//comparisonRows
	/** The Constant TAG_ENTITIES_COMPARISON. */
	private static final String TAG_ENTITIES_COMPARISON = "entitiesComparison";
	
	/** The Constant TAG_COMPARISON_ROWS. */
	private static final String TAG_COMPARISON_ROWS = "comparisonRows";
	
	/** The Constant TAG_COMPARISON_ROW. */
	private static final String TAG_COMPARISON_ROW = "comparisonRow";	
	
	/** The Constant ATTR_ENTITY_1. */
	private static final String ATTR_ENTITY_1 = "entity1";
	
	/** The Constant ATTR_ENTITY_2. */
	private static final String ATTR_ENTITY_2 = "entity2";
	
	/** The Constant ATTR_ENTITY_3. */
	private static final String ATTR_ENTITY_3 = "entity3";
	
	/** The Constant ATTR_ENTITY_4. */
	private static final String ATTR_ENTITY_4 = "entity4";		
	
	/** The Constant ATTR_ENTITYLIST. */
	private static final String ATTR_ENTITYLIST = "entityList";
	
	/** The Constant ATTR_CHARACTERISTIC. */
	private static final String ATTR_CHARACTERISTIC = "characteristic";
	
	/** The Constant ATTR_PROPERTY. */
	private static final String ATTR_PROPERTY = "property";
	
	private static final String ATTR_PROPERTY_QNAME = "propertyQName";
	
	/** The Constant ATTR_VALUE_1. */
	private static final String ATTR_VALUE_1 = "value1";
	
	/** The Constant ATTR_VALUE_2. */
	private static final String ATTR_VALUE_2 = "value2";	
	
	/** The Constant ATTR_VALUE_3. */
	private static final String ATTR_VALUE_3 = "value3";
	
	/** The Constant ATTR_VALUE_4. */
	private static final String ATTR_VALUE_4 = "value4";	
	
	//structComparisonRows
	/** The Constant TAG_STRUCT_COMPARISON_ROWS. */
	private static final String TAG_STRUCT_COMPARISON_ROWS = "structComparisonRows";
	
	/** The Constant TAG_STRUCT_COMPARISON_ROW. */
	private static final String TAG_STRUCT_COMPARISON_ROW = "structComparisonRow";
	
	/** The Constant ATTR_COMPARISON. */
	private static final String ATTR_COMPARISON = "comparison";
	
	/** The Constant ATTR_DEPTH_LEVEL. */
	private static final String ATTR_DEPTH_LEVEL = "depthLevel";
	
	/** The Constant ATTR_OPERATOR. */
	private static final String ATTR_OPERATOR = "operator";
	
	/** The Constant ATTR_ITEM1. */
	private static final String ATTR_ITEM1 = "item1";
	
	/** The Constant ATTR_ITEM2. */
	private static final String ATTR_ITEM2 = "item2";
	
	/** The Constant ATTR_PROPERTIES1. */
	private static final String ATTR_PROPERTIES1 = "properties1";
	
	/** The Constant ATTR_PROPERTIES2. */
	private static final String ATTR_PROPERTIES2 = "properties2";
	
	/** The Constant CHARACT_PATH_SEPARATOR. */
	private static final String CHARACT_PATH_SEPARATOR = " / ";
	
	/** The Constant COMPARISON_SEPARATOR. */
	private static final String COMPARISON_SEPARATOR = " - ";
	
	/** The Constant PROPERTY_SEPARATOR. */
	private static final String PROPERTY_SEPARATOR = " / ";
	
	/** The Constant PROPERTY_VALUE_SEPARATOR. */
	private static final String PROPERTY_VALUE_SEPARATOR = " : ";
	
	private static final String PARAM_VALUE_HIDE_STRUC_COMP = "StructComparisonHide";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareEntityServiceImpl.class);
	
	/** The compare entity service. */
	private CompareEntityService compareEntityService;
	
	
	/** The report engine. */
	private BeCPGReportEngine beCPGReportEngine;		
	
	private ReportTplService reportTplService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	
	private NamespaceService namespaceService;
		
	/**
	 * Sets the compare entity service.
	 *
	 * @param compareEntityService the new compare entity service
	 */
	public void setCompareEntityService(CompareEntityService compareEntityService) {
		this.compareEntityService = compareEntityService;
	}
	
	/**
	 * Sets the report engine.
	 *
	 * @param reportEngine the new report engine
	 */

	public void setBeCPGReportEngine(BeCPGReportEngine beCPGReportEngine) {
		this.beCPGReportEngine = beCPGReportEngine;
	}
	
	
	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}


	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.entity.comparison.CompareEntityReportService#getComparisonReport(org.alfresco.service.cmr.repository.NodeRef, java.util.List, java.io.OutputStream)
	 */
	@Override
	public void getComparisonReport(NodeRef entity1, List<NodeRef> entities, OutputStream out){				
		
		// look for template
		NodeRef templateNodeRef = reportTplService.getSystemReportTemplate(ReportType.System, 
											null, 
											TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS));
		
		if(templateNodeRef != null){
			
			// do comparison
			List<CompareResultDataItem> compareResult = compareEntityService.compare(entity1, entities);
			
			// do structural comparison
			Boolean hideStructComparison = true;
			Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<String, List<StructCompareResultDataItem>>();
			for(NodeRef entityNodeRef : entities){
				String comparison = (String)nodeService.getProperty(entity1, ContentModel.PROP_NAME) + COMPARISON_SEPARATOR + (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				List<StructCompareResultDataItem> scr = compareEntityService.compareStructDatalist(entity1, entityNodeRef, BeCPGModel.TYPE_COMPOLIST, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);				
				structCompareResults.put(comparison, scr);
				
				// display struct comparison if there is smth to show
				if(hideStructComparison && !scr.isEmpty()){
					hideStructComparison = false;
				}
			}
			
			//Prepare data source
			Document document = DocumentHelper.createDocument();
			Element entitiesCmpElt = document.addElement(TAG_ENTITIES_COMPARISON);
			entitiesCmpElt.add(renderComparisonAsXmlData(entity1, entities, compareResult));
			entitiesCmpElt.add(renderStructComparisonAsXmlData(structCompareResults, BeCPGModel.ASSOC_COMPOLIST_PRODUCT));			

			try{
				Map<String,Object> params = new HashMap<String, Object>();

				// hide struct comparison
				params.put(PARAM_VALUE_HIDE_STRUC_COMP, hideStructComparison);
				params.put(ReportParams.PARAM_FORMAT,ReportFormat.PDF);
				
				beCPGReportEngine.createReport(templateNodeRef, entitiesCmpElt, out , params);
				
					
			}
			catch(Exception e){
				logger.error("Failed to run comparison report: ",  e);
			}						
    	}
		
	}
	
	/**
	 * Render the comparison as xml data.
	 *
	 * @param entity1NodeRef the entity1 node ref
	 * @param entitiesNodeRef the entities node ref
	 * @param compareResult the compare result
	 * @return the element
	 */
	private Element renderComparisonAsXmlData(NodeRef entity1NodeRef, List<NodeRef> entitiesNodeRef, List<CompareResultDataItem> compareResult){
			
			Document document = DocumentHelper.createDocument();
			
			Element cmpRowsElt = document.addElement(TAG_COMPARISON_ROWS);
			
			String name = (String)nodeService.getProperty(entity1NodeRef, ContentModel.PROP_NAME);			
			cmpRowsElt.addAttribute(ATTR_ENTITY_1, name);
			
			name = (String)nodeService.getProperty(entitiesNodeRef.get(0), ContentModel.PROP_NAME);
			cmpRowsElt.addAttribute(ATTR_ENTITY_2, name);
			
			if(entitiesNodeRef.size() > 2){
				name = (String)nodeService.getProperty(entitiesNodeRef.get(1), ContentModel.PROP_NAME);
				cmpRowsElt.addAttribute(ATTR_ENTITY_3, name);
			}
				
			if(entitiesNodeRef.size() > 3){
				name = (String)nodeService.getProperty(entitiesNodeRef.get(2), ContentModel.PROP_NAME);
				cmpRowsElt.addAttribute(ATTR_ENTITY_4, name);
			}
				
			// compareResult
			for(CompareResultDataItem c : compareResult){
								
				String entityListTitle = "";
				if(c.getEntityList() != null){
					TypeDefinition typeDef = dictionaryService.getType(c.getEntityList());
					entityListTitle = typeDef.getTitle();
				}
				String charactPath = "";
				if(c.getCharactPath() != null){
					for(NodeRef nodeRef : c.getCharactPath()){						
						
						charactPath += (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
						
						if(!charactPath.isEmpty())
							charactPath += CHARACT_PATH_SEPARATOR;
					}
				}
				String charactName = c.getCharacteristic() == null ? "" : charactPath + (String)nodeService.getProperty(c.getCharacteristic(), ContentModel.PROP_NAME);				
				String propertyTitle = getClassAttributeTitle(c.getProperty());
								
				Element cmpRowElt = cmpRowsElt.addElement(TAG_COMPARISON_ROW);
				cmpRowElt.addAttribute(ATTR_ENTITYLIST, entityListTitle);
				cmpRowElt.addAttribute(ATTR_CHARACTERISTIC, charactName);							
				cmpRowElt.addAttribute(ATTR_PROPERTY, propertyTitle);
				cmpRowElt.addAttribute(ATTR_PROPERTY_QNAME, c.getProperty().toPrefixString(namespaceService));
				
				if(logger.isTraceEnabled()){
					String value1 = c.getValues().get(0);
					String value2 = c.getValues().get(1);
					logger.trace("compare prop: " + c.getProperty() + " - value1: " + value1 + " - value2: " + value2);
				}					
				
				if(c.getValues().size() > 0 && c.getValues().get(0) != null)
					cmpRowElt.addAttribute(ATTR_VALUE_1, c.getValues().get(0));
				if(c.getValues().size() > 1 && c.getValues().get(1) != null)
					cmpRowElt.addAttribute(ATTR_VALUE_2, c.getValues().get(1));
				if(c.getValues().size() > 2 && c.getValues().get(2) != null)
					cmpRowElt.addAttribute(ATTR_VALUE_3, c.getValues().get(2));
				if(c.getValues().size() > 3 && c.getValues().get(3) != null)
					cmpRowElt.addAttribute(ATTR_VALUE_4, c.getValues().get(3));
			}			
											
			return cmpRowsElt;
	}
	
	/**
	 * Render the comparison as xml data.
	 *
	 * @param structCompareResults the struct compare results
	 * @param pivotProperty the pivot property
	 * @return the element
	 */
	private Element renderStructComparisonAsXmlData(Map<String, List<StructCompareResultDataItem>> structCompareResults, QName pivotProperty ){
			
			Document document = DocumentHelper.createDocument();			
			
			// structCompareResult
			Element structCmpRowsElt = document.addElement(TAG_STRUCT_COMPARISON_ROWS);
			
			// each comparison
			for(String comparison : structCompareResults.keySet()){
				
				List<StructCompareResultDataItem> structCompareResult = structCompareResults.get(comparison);
				
				// each structCompareResultDataItem
				for(StructCompareResultDataItem c : structCompareResult){
					
					String entityListTitle = "";
					if(c.getEntityList() != null){
						TypeDefinition typeDef = dictionaryService.getType(c.getEntityList());
						entityListTitle = typeDef.getTitle();
					}
					
					String depthLevel = ((Integer)c.getDepthLevel()).toString();
					
					String entity1 = "";
					String properties1 = "";
					if(c.getCharacteristic1() != null){
						List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic1(), pivotProperty);
			    		NodeRef entityNodeRef = (compoAssocRefs.get(0)).getTargetRef();
			    		entity1 = (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
			    		
			    		// props
			    		for(QName property : c.getProperties1().keySet()){
							if(!properties1.isEmpty())
								properties1 += PROPERTY_SEPARATOR;
							
							String value = c.getProperties1().get(property);
							properties1 += getClassAttributeTitle(property) + PROPERTY_VALUE_SEPARATOR + value;
						}
					}
					
					String entity2 = "";
					String properties2 = "";
					if(c.getCharacteristic2() != null){
						List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic2(), pivotProperty);
			    		NodeRef entityNodeRef = (compoAssocRefs.get(0)).getTargetRef();
			    		entity2 = (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
			    		
			    		// props 
			    		for(QName property : c.getProperties2().keySet()){
							if(!properties2.isEmpty())
								properties2 += PROPERTY_SEPARATOR;
							
							String value = c.getProperties2().get(property);
							properties2 += getClassAttributeTitle(property) + PROPERTY_VALUE_SEPARATOR + value;
						}
					}		
					
					if(logger.isDebugEnabled()){
						logger.debug("renderStructComparisonAsXmlData: entity1: " + entity1 + " - entity2: " + entity2);
					}
					
					Element cmpRowElt = structCmpRowsElt.addElement(TAG_STRUCT_COMPARISON_ROW);				
					cmpRowElt.addAttribute(ATTR_COMPARISON, comparison);
					cmpRowElt.addAttribute(ATTR_ENTITYLIST, entityListTitle);
					cmpRowElt.addAttribute(ATTR_DEPTH_LEVEL, depthLevel);
					cmpRowElt.addAttribute(ATTR_OPERATOR, c.getOperator().toString());
					cmpRowElt.addAttribute(ATTR_ITEM1, entity1);
					cmpRowElt.addAttribute(ATTR_ITEM2, entity2);
					cmpRowElt.addAttribute(ATTR_PROPERTIES1, properties1);
					cmpRowElt.addAttribute(ATTR_PROPERTIES2, properties2);				
				}
			}
														
			return structCmpRowsElt;
	}
	
	/**
	 * Get the title of a property of association.
	 *
	 * @param qName the q name
	 * @return the class attribute title
	 */
	private String getClassAttributeTitle(QName qName){
		
		String title = "";
		
		PropertyDefinition propertyDef = dictionaryService.getProperty(qName);
		if(propertyDef != null){
			title = propertyDef.getTitle();
		}
		else{
			AssociationDefinition assocDef = dictionaryService.getAssociation(qName);
			if(assocDef != null)
				title = assocDef.getTitle();
		}
		
		return title;
	}	
}
