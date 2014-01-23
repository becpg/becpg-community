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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

/**
 * The Class CompareEntityReportServiceImpl.
 *
 * @author querephi
 */
@Service("compareEntityReportService")
public class CompareEntityReportServiceImpl  implements CompareEntityReportService{
	
	private static final String TAG_ENTITIES_COMPARISON = "entitiesComparison";
	private static final String TAG_COMPARISON_ROWS = "comparisonRows";
	private static final String TAG_COMPARISON_ROW = "comparisonRow";	
	
	private static final String ATTR_ENTITY = "entity";
	
	private static final String ATTR_ENTITYLIST = "entityList";
	
	private static final String ATTR_CHARACTERISTIC = "characteristic";
	
	private static final String ATTR_PROPERTY = "property";
	
	private static final String ATTR_PROPERTY_QNAME = "propertyQName";
	
	private static final String ATTR_VALUE = "value";
	
	private static final String TAG_STRUCT_COMPARISON_ROWS = "structComparisonRows";
	
	private static final String TAG_STRUCT_COMPARISON_ROW = "structComparisonRow";
	
	private static final String ATTR_COMPARISON = "comparison";
	
	private static final String ATTR_DEPTH_LEVEL = "depthLevel";
	
	private static final String ATTR_OPERATOR = "operator";
	
	private static final String ATTR_ITEM1 = "item1";
	
	private static final String ATTR_ITEM2 = "item2";
	
	private static final String ATTR_PROPERTIES1 = "properties1";
	
	private static final String ATTR_PROPERTIES2 = "properties2";
	
	private static final String CHARACT_PATH_SEPARATOR = " / ";
	
	private static final String COMPARISON_SEPARATOR = " - ";
	
	private static final String PROPERTY_SEPARATOR = " / ";
	
	private static final String PROPERTY_VALUE_SEPARATOR = " : ";
	
	private static final String PARAM_VALUE_HIDE_STRUC_COMP = "StructComparisonHide";
	
	private static Log logger = LogFactory.getLog(CompareEntityServiceImpl.class);
	
	@Autowired
	private CompareEntityService compareEntityService;
	
	@Autowired
	private BeCPGReportEngine beCPGReportEngine;		
	
	@Autowired
	private ReportTplService reportTplService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private DictionaryService dictionaryService;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	@Autowired
	private NamespaceService namespaceService;
	

	@Override
	public void getComparisonReport(NodeRef entity1, QName dataListTypeQName, List<NodeRef> entities, OutputStream out){				
		
		// look for template
		NodeRef templateNodeRef = reportTplService.getSystemReportTemplate(ReportType.System, 
											null, 
											TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_ENTITIES));
		
		if(templateNodeRef != null){
			
			QName pivoAssoc = entityDictionaryService.getDefaultPivotAssoc(dataListTypeQName);
			
			// do comparison
			List<CompareResultDataItem> compareResult = compareEntityService.compare(entity1, entities);
			
			// do structural comparison
			Boolean hideStructComparison = true;
			Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<String, List<StructCompareResultDataItem>>();
			for(NodeRef entityNodeRef : entities){
				String comparison = (String)nodeService.getProperty(entity1, ContentModel.PROP_NAME) + COMPARISON_SEPARATOR + (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				List<StructCompareResultDataItem> scr = compareEntityService.compareStructDatalist(entity1, entityNodeRef, dataListTypeQName, pivoAssoc);				
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
			entitiesCmpElt.add(renderStructComparisonAsXmlData(structCompareResults, pivoAssoc));		
			
			if(logger.isDebugEnabled()){
				logger.debug("comparison XML " + entitiesCmpElt);
			}

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
	private Element renderComparisonAsXmlData(NodeRef entity1NodeRef, List<NodeRef> entityNodeRefs, List<CompareResultDataItem> compareResult){
			
			Document document = DocumentHelper.createDocument();
			
			Element cmpRowsElt = document.addElement(TAG_COMPARISON_ROWS);
			
			String name = (String)nodeService.getProperty(entity1NodeRef, ContentModel.PROP_NAME);			
			cmpRowsElt.addAttribute(ATTR_ENTITY + "1", name);
			
			int i = 2;
			for(NodeRef entityNodeRef : entityNodeRefs){
				name = (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				cmpRowsElt.addAttribute(ATTR_ENTITY + i, name);
				i++;
			}
			
			// compareResult
			for(CompareResultDataItem c : compareResult){
								
				String entityListTitle = "";
				if(c.getEntityList() != null){
					TypeDefinition typeDef = dictionaryService.getType(c.getEntityList());
					entityListTitle = typeDef.getTitle(dictionaryService);
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
				
				i = 1;
				for(String value : c.getValues()){
					if(logger.isTraceEnabled()){
						logger.trace("compare prop: " + c.getProperty() + " - " + ATTR_VALUE + i + " " + value);
					}
					cmpRowElt.addAttribute(ATTR_VALUE + i, value);
					i++;
				}				
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
						entityListTitle = typeDef.getTitle(dictionaryService);
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
			title = propertyDef.getTitle(dictionaryService);
		}
		else{
			AssociationDefinition assocDef = dictionaryService.getAssociation(qName);
			if(assocDef != null)
				title = assocDef.getTitle(dictionaryService);
		}
		
		return title;
	}	
}
