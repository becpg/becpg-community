package fr.becpg.repo.entity.comparison;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.form.BecpgFormService;
import fr.becpg.repo.form.impl.BecpgFormDefinition;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.report.engine.impl.ReportServerEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

/**
 * The Class CompareEntityReportServiceImpl.
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("compareEntityReportService")
public class CompareEntityReportServiceImpl implements CompareEntityReportService {

	private static final String TAG_ENTITIES_COMPARISON = "entitiesComparison";
	private static final String TAG_COMPARISON_ROWS = "comparisonRows";
	private static final String TAG_COMPARISON_ROW = "comparisonRow";

	private static final String ATTR_ENTITY = "entity";

	private static final String ATTR_ENTITYLIST = "entityList";
	private static final String ATTR_ENTITYLIST_QNAME = "entityListQName";

	private static final String ATTR_CHARACTERISTIC = "characteristic";

	private static final String ATTR_PROPERTY = "property";

	private static final String ATTR_PROPERTY_QNAME = "propertyQName";

	private static final String ATTR_IS_DIFFERENT = "isDifferent";

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

	private static final String PROPERTY_SEPARATOR = " ; ";

	private static final String PROPERTY_VALUE_SEPARATOR = " : ";

	private static final Log logger = LogFactory.getLog(CompareEntityReportServiceImpl.class);

	@Autowired
	private CompareEntityService compareEntityService;

	@Autowired
	private ReportServerEngine reportServerEngine;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService dictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private BecpgFormService becpgFormService;

	/** {@inheritDoc} */
	@Override
	public void getComparisonReport(NodeRef entity1, List<NodeRef> entities, NodeRef templateNodeRef, OutputStream out) {

		if (templateNodeRef != null) {
			String reportFormat = (String) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT); 

			List<CompareResultDataItem> compareResult = new ArrayList<>();
			Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
			
			compareEntityService.compare(entity1, entities, compareResult, structCompareResults);

			// Prepare data source
			Document document = DocumentHelper.createDocument();
			Element entitiesCmpElt = document.addElement(TAG_ENTITIES_COMPARISON);
			
			entitiesCmpElt.add(renderComparisonAsXmlData(entity1, entities, compareResult));
			entitiesCmpElt.add(renderStructComparisonAsXmlData(nodeService.getType(entity1), structCompareResults));
			
			if (logger.isDebugEnabled()) {
				logger.debug("comparison XML " + entitiesCmpElt.asXML());
			}

			try {
				Map<String, Object> params = new HashMap<>();
				params.put(ReportParams.PARAM_FORMAT, ReportFormat.valueOf(reportFormat));
				params.put(ReportParams.PARAM_LANG, MLTextHelper.localeKey(I18NUtil.getLocale()));
				params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES,
						associationService.getTargetAssocs(templateNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));
				
				EntityReportData reportData = new EntityReportData();
				reportData.setXmlDataSource(entitiesCmpElt);
				
				reportServerEngine.createReport(templateNodeRef, reportData, out, params);

			} catch (Exception e) {
				logger.error("Failed to run comparison report: ", e);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getReportFileName(NodeRef tplNodeRef, String defaultName) {
		String documentName = defaultName;
		if (documentName == null) {
			documentName = (String) nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME);
		}

		String extension = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
		if ((extension != null)) {
			if (documentName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT)) {
				documentName = documentName.replace(RepoConsts.REPORT_EXTENSION_BIRT, extension.toLowerCase());
			} else if (!documentName.endsWith(extension.toLowerCase())) {
				documentName += "." + extension.toLowerCase();
			}
		}
		
		return documentName;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getXmlReportDataSource(NodeRef entity, List<NodeRef> entities) {
		List<CompareResultDataItem> compareResult = new ArrayList<>();
		Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
		
		compareEntityService.compare(entity, entities, compareResult, structCompareResults);
		
		// Prepare data source
		Document document = DocumentHelper.createDocument();
		Element entitiesCmpElt = document.addElement(TAG_ENTITIES_COMPARISON);
		
		entitiesCmpElt.add(renderComparisonAsXmlData(entity, entities, compareResult));
		entitiesCmpElt.add(renderStructComparisonAsXmlData(nodeService.getType(entity), structCompareResults));
		
		if (logger.isDebugEnabled()) {
			logger.debug("comparison XML " + entitiesCmpElt.asXML());
		}
		
		return entitiesCmpElt.asXML();
	}

	/**
	 * Render the comparison as xml data.
	 */
	private  Element renderComparisonAsXmlData(NodeRef entity1NodeRef, List<NodeRef> entityNodeRefs, List<CompareResultDataItem> compareResult) {
		Document document = DocumentHelper.createDocument();

		Element cmpRowsElt = document.addElement(TAG_COMPARISON_ROWS);

		String name = (String) nodeService.getProperty(entity1NodeRef, ContentModel.PROP_NAME);
		cmpRowsElt.addAttribute(ATTR_ENTITY + "1", name);

		int i = 2;
		for (NodeRef entityNodeRef : entityNodeRefs) {
			name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
			cmpRowsElt.addAttribute(ATTR_ENTITY + i, name); 
			i++;
		}
		
		Map<String, BecpgFormDefinition> defs = new HashMap<>();
		
		// compareResult
		for (CompareResultDataItem c : compareResult) {
			
			BecpgFormDefinition def = null;
			
			if (c.getEntityList() != null) {
				String entityListName = c.getEntityList().toPrefixString();
				
				TypeDefinition typeDef = dictionaryService.getType(c.getEntityList());
				String entityListTitle = typeDef.getTitle(dictionaryService);
				
				try {
					def = getFormDef(defs, c.getProperty(), entityListName, entityListTitle, entity1NodeRef);
				} catch (FormNotFoundException e) {}
				
			}
			
			String title =  getClassAttributeTitle(nodeService.getType(entity1NodeRef), def, c.getProperty());
			if(title!=null) {
				Element cmpRowElt = cmpRowsElt.addElement(TAG_COMPARISON_ROW);
				
				if (c.getEntityList() != null) {
					TypeDefinition typeDef = dictionaryService.getType(c.getEntityList());
					String entityListTitle = typeDef.getTitle(dictionaryService);
					
				
					cmpRowElt.addAttribute(ATTR_ENTITYLIST, entityListTitle);
					cmpRowElt.addAttribute(ATTR_ENTITYLIST_QNAME, c.getEntityList().toPrefixString(namespaceService));
				}
		
			
				cmpRowElt.addAttribute(ATTR_CHARACTERISTIC, c.getCharactName());
				cmpRowElt.addAttribute(ATTR_PROPERTY,title);
				cmpRowElt.addAttribute(ATTR_PROPERTY_QNAME, c.getProperty().toPrefixString(namespaceService));
				cmpRowElt.addAttribute(ATTR_IS_DIFFERENT, Boolean.toString(c.isDifferent()));
	
				i = 1;
				for (String value : c.getValues()) {
					// Escaping text to replace invalid chars in rendered XML output (e.g: 'property="Name "value""')
					value = StringEscapeUtils.escapeXml11(value);
					if (logger.isDebugEnabled()) {
						logger.debug("compare prop: " + c.getProperty() + " - " + ATTR_VALUE + i + " " + value);
					}
					cmpRowElt.addAttribute(ATTR_VALUE + i, value);
					i++;
				}
			}
		}

		return cmpRowsElt;
	}

	/**
	 * Render the comparison as xml data.
	 *
	 * @param structCompareResults a {@link java.util.Map} object.
	 * @return a {@link org.dom4j.Element} object.
	 */
	public  Element renderStructComparisonAsXmlData(QName nodeType, Map<String, List<StructCompareResultDataItem>> structCompareResults) {
		Document document = DocumentHelper.createDocument();

		// structCompareResult
		Element structCmpRowsElt = document.addElement(TAG_STRUCT_COMPARISON_ROWS);

		// each comparison
		for (String comparison : structCompareResults.keySet()) {
			List<StructCompareResultDataItem> structCompareResult = structCompareResults.get(comparison);
			
			
			// each structCompareResultDataItem
			for (StructCompareResultDataItem c : structCompareResult) {
				
				String entityListTitle = "";
				if (c.getEntityList() != null) {
					TypeDefinition typeDef = dictionaryService.getType(c.getEntityList());
					entityListTitle = typeDef.getTitle(dictionaryService);
				}

				String depthLevel = ((Integer) c.getDepthLevel()).toString();
				
				String entity1 = "";
				String properties1 = "";
				if (c.getCharacteristic1() != null) {
					List<AssociationRef> compoAssocRefs = null;
					if(c.getPivotProperty() != null) {
						compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic1(), c.getPivotProperty());
					}
					// If it's a document
					NodeRef entityNodeRef = null;
					if((compoAssocRefs == null )|| (compoAssocRefs.isEmpty())){
						entityNodeRef = c.getCharacteristic1();
					} else if(compoAssocRefs!= null) {
						entityNodeRef = (compoAssocRefs.get(0)).getTargetRef();
					}
					
					entity1 = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

					// props
					for (QName property : c.getProperties1().keySet()) {
						if (!properties1.isEmpty()) {
							properties1 += PROPERTY_SEPARATOR;
						}
						
						String value = c.getProperties1().get(property);
						properties1 += getClassAttributeTitle(nodeType, null, property) + PROPERTY_VALUE_SEPARATOR + value;
					}
				}

				String entity2 = "";
				String properties2 = "";
				if (c.getCharacteristic2() != null) {
					List<AssociationRef> compoAssocRefs= null;
					if(c.getPivotProperty() != null) {//There isn't pivotProperty for documents
						compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic2(), c.getPivotProperty());
					}
					// If it's a document
					NodeRef entityNodeRef = null;
					if((compoAssocRefs == null )|| (compoAssocRefs.isEmpty())){
						entityNodeRef = c.getCharacteristic2();
					} else if(compoAssocRefs!= null)  {
						entityNodeRef = (compoAssocRefs.get(0)).getTargetRef();
					}
					
					entity2 = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

					// props
					for (QName property : c.getProperties2().keySet()) {
						if (!properties2.isEmpty()) {
							properties2 += PROPERTY_SEPARATOR;
						}
						
						String value = c.getProperties2().get(property);
						properties2 += getClassAttributeTitle(nodeType, null, property) + PROPERTY_VALUE_SEPARATOR + value;
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("renderStructComparisonAsXmlData: entity1: " + entity1 + " - entity2: " + entity2);
				}

				Element cmpRowElt = structCmpRowsElt.addElement(TAG_STRUCT_COMPARISON_ROW);
				cmpRowElt.addAttribute(ATTR_COMPARISON, comparison);
				cmpRowElt.addAttribute(ATTR_ENTITYLIST, entityListTitle);
				cmpRowElt.addAttribute(ATTR_ENTITYLIST_QNAME, c.getEntityList().toPrefixString(namespaceService));
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

	
	private String getClassAttributeTitle(QName nodeType, BecpgFormDefinition definition, QName qName) {

		String title = (definition != null) ? definition.getTitle(qName.toPrefixString(namespaceService)) : null;
		
		if (title == null) {
			PropertyDefinition propertyDef = dictionaryService.getProperty(qName);
			if (propertyDef != null) {
				title = dictionaryService.getTitle(propertyDef, nodeType);
			} else {
				AssociationDefinition assocDef = dictionaryService.getAssociation(qName);
				if (assocDef != null) {
					title = assocDef.getTitle(dictionaryService);
				}
			}
		}

		// Escaping text to replace invalid chars in rendered XML output (e.g: 'property="Name "value""')
		return (title != null && !title.isBlank()) ? StringEscapeUtils.escapeXml11(title) :  null;
	}

	private BecpgFormDefinition getFormDef(Map<String, BecpgFormDefinition> defs, QName property, 
		String entityListName, String entityListTitle, NodeRef entityNodeRef) {
		
		List<String> fields = Arrays.asList(property.toPrefixString());
		
		String itemId = (entityListName != null) ? entityListName : entityListTitle;
		BecpgFormDefinition def = null;
		
		if (defs.containsKey(itemId)) {
			def = defs.get(itemId);
		} else {
			def = becpgFormService.getForm("type", itemId,
				(entityListName != null) ? "datagrid" : "default", null, fields, fields, entityNodeRef);
			defs.put(itemId, def);
		}
		
		return def;
	}
	
}
