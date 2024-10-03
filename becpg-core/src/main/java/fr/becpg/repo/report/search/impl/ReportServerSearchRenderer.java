package fr.becpg.repo.report.search.impl;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.report.engine.impl.ReportServerEngine;
import fr.becpg.repo.report.entity.EntityImageInfo;
import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.repo.report.search.actions.AbstractExportSearchAction;
import fr.becpg.repo.report.search.actions.ReportSearchAction;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

/**
 * <p>
 * ReportServerSearchRenderer class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ReportServerSearchRenderer implements SearchReportRenderer {

	private static final Log logger = LogFactory.getLog(ReportServerSearchRenderer.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private ReportServerEngine reportServerEngine;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private ActionService actionService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	private static final String FILE_QUERY = "ExportSearchQuery.xml";

	private static final String QUERY_XPATH_COLUMNS_ATTRIBUTE = "/export/query/columns/column[@type='Attribute']";

	private static final String QUERY_XPATH_COLUMNS_DATALIST = "/export/query/columns/column[@type='Characteristic']";

	private static final String QUERY_XPATH_COLUMNS_FILE = "/export/query/columns/column[@type='File']";

	private static final String QUERY_ATTR_GET_ID = "@id";
	private static final String QUERY_ATTR_GET_ATTRIBUTE = "@attribute";
	private static final String QUERY_ATTR_GET_DATALIST_QNAME = "@dataListQName";
	private static final String QUERY_ATTR_GET_CHARACT_QNAME = "@charactQName";
	private static final String QUERY_ATTR_GET_CHARACT_NODE_REF = "@charactNodeRef";
	private static final String QUERY_ATTR_GET_CHARACT_NAME = "@charactName";
	private static final String QUERY_ATTR_GET_PATH = "@path";

	/** Constant <code>VALUE_NULL=""</code> */
	public static final String VALUE_NULL = "";

	/** Constant <code>KEY_IMAGE_NODE_IMG="%s-%s"</code> */
	public static final String KEY_IMAGE_NODE_IMG = "%s-%s";

	/** {@inheritDoc} */
	@Override
	public void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {

		try {
			ReportServerSearchContext exportSearchCtx = createContext(templateNodeRef);

			loadReportData(exportSearchCtx, searchResults);

			if (logger.isDebugEnabled()) {
				logger.debug("Xml data: " + exportSearchCtx.getReportData().getXmlDataSource());
			}

			createReport(templateNodeRef, exportSearchCtx, outputStream, reportFormat);

		} catch (ReportException | MappingException e) {

			logger.error("Failed to run report: ", e);
		}

	}

	/**
	 * <p>createReport.</p>
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param exportSearchCtx a {@link fr.becpg.repo.report.search.impl.ReportServerSearchContext} object
	 * @param outputStream a {@link java.io.OutputStream} object
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object
	 * @throws fr.becpg.report.client.ReportException if any.
	 */
	public void createReport(NodeRef templateNodeRef, ReportServerSearchContext exportSearchCtx, OutputStream outputStream, ReportFormat reportFormat)
			throws ReportException {
		Map<String, Object> params = new HashMap<>();
		params.put(ReportParams.PARAM_FORMAT, reportFormat);
		params.put(ReportParams.PARAM_LANG, MLTextHelper.localeKey(I18NUtil.getLocale()));
		params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES,
				associationService.getTargetAssocs(templateNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));

		reportServerEngine.createReport(templateNodeRef, exportSearchCtx.getReportData(), outputStream, params);

	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME)).endsWith(ReportTplService.PARAM_VALUE_DESIGN_EXTENSION);
	}

	/**
	 * Generate Xml export data.
	 *
	 * @throws InterruptedException
	 *
	 */
	private void loadReportData(ReportServerSearchContext exportSearchCtx, List<NodeRef> nodeRefList) {

		logger.debug("start loadReportData " + nodeRefList.size());
		Integer idx = 1;

		for (NodeRef nodeRef : nodeRefList) {
			exportNode(exportSearchCtx, nodeRef, idx);
			idx++;
		}

		logger.debug("End loadReportData");

	}

	/**
	 * Export properties and associations of a node.
	 *
	 * @param exportSearchCtx a {@link fr.becpg.repo.report.search.impl.ReportServerSearchContext} object
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param idx a long
	 */
	public void exportNode(ReportServerSearchContext exportSearchCtx, NodeRef nodeRef, long idx) {
		Element nodeElt = exportSearchCtx.createNodeElt(idx);

		// export class attributes
		for (AttributeMapping attributeMapping : exportSearchCtx.getAttributeColumns()) {

			String value = getColumnValue(nodeRef, attributeMapping.getAttribute());
			nodeElt.addAttribute(attributeMapping.getId(), value);
		}

		// export charact
		if (!exportSearchCtx.getCharacteristicsColumns().isEmpty()) {
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);

			if (listContainerNodeRef != null) {
				for (CharacteristicMapping characteristicMapping : exportSearchCtx.getCharacteristicsColumns()) {

					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, characteristicMapping.getDataListQName());
					NodeRef linkNodeRef = entityListDAO.getListItem(listNodeRef, characteristicMapping.getCharactQName(),
							characteristicMapping.getCharactNodeRef());

					if (linkNodeRef != null) {

						String value = getColumnValue(linkNodeRef, characteristicMapping.getAttribute());
						nodeElt.addAttribute(characteristicMapping.getId(), value);
					}
				}
			}
		}
		Path path = nodeService.getPath(nodeRef);
		// Extract site
		if (path != null) {
			String pathString = path.toPrefixString(namespaceService);
			String siteId = SiteHelper.extractSiteId(pathString);
			if (siteId != null) {
				nodeElt.addAttribute(ReportServerSearchContext.TAG_SITE, siteId);
			}
		}

		// export file
		Map<NodeRef, Map<String, String>> filesAttributes = new HashMap<>();
		for (FileMapping fileMapping : exportSearchCtx.getFileColumns()) {

			NodeRef tempNodeRef = nodeRef;
			if (tempNodeRef != null) {
				for (String p : fileMapping.getPath()) {

					if (tempNodeRef != null) {
						tempNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, p);
					} else {
						break;
					}
				}
			}

			if (tempNodeRef != null) {

				Map<String, String> fileAttributes = filesAttributes.get(tempNodeRef);
				if (fileAttributes == null) {
					fileAttributes = new HashMap<>();
					filesAttributes.put(tempNodeRef, fileAttributes);
				}

				String id = String.format(KEY_IMAGE_NODE_IMG, nodeElt.valueOf(QUERY_ATTR_GET_ID), fileMapping.getId());
				fileAttributes.put(ReportServerSearchContext.ATTR_ID, id);

				// file content
				if (fileMapping.getAttribute().getName().isMatch(ContentModel.PROP_CONTENT)) {

					if(tempNodeRef!=null) {
						exportSearchCtx.getReportData().getImages().add(new EntityImageInfo(id, tempNodeRef));
					}
					
				}
				// class attribute
				else {
					String value = getColumnValue(nodeRef, fileMapping.getAttribute());
					fileAttributes.put(fileMapping.getId(), value);
				}
			}
		}

		if (!filesAttributes.isEmpty()) {
			for (Map<String, String> fileAttributes : filesAttributes.values()) {
				Element fileElt = exportSearchCtx.createFileElt();

				for (Map.Entry<String, String> kv : fileAttributes.entrySet()) {
					fileElt.addAttribute(kv.getKey(), kv.getValue());
				}
			}
		}

	}

	/**
	 * Gets the column value.
	 */
	private String getColumnValue(NodeRef nodeRef, ClassAttributeDefinition attribute) {

		String value = VALUE_NULL;

		if (nodeService.exists(nodeRef)) {
			if (attribute instanceof PropertyDefinition) {
				Serializable serializable = nodeService.getProperty(nodeRef, attribute.getName());
				value = attributeExtractorService.extractPropertyForReport((PropertyDefinition) attribute, serializable, false);

			} else if (attribute instanceof AssociationDefinition) {

				List<NodeRef> assocNodes = associationService.getTargetAssocs(nodeRef, attribute.getName());

				if ((assocNodes != null) && !assocNodes.isEmpty()) {
					return assocNodes.stream().map(i -> attributeExtractorService.extractPropName(i))
							.collect(Collectors.joining(RepoConsts.LABEL_SEPARATOR));

				}

			}
		}

		return value;
	}

	/**
	 * Load the query file.
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.report.search.impl.ReportServerSearchContext} object
	 * @throws fr.becpg.config.mapping.MappingException if any.
	 */
	public ReportServerSearchContext createContext(NodeRef templateNodeRef) throws MappingException {

		ReportServerSearchContext exportSearchCtx = new ReportServerSearchContext();

		NodeRef folderNodeRef = nodeService.getPrimaryParent(templateNodeRef).getParentRef();
		NodeRef queryNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, FILE_QUERY);

		if (queryNodeRef == null) {
			logger.error(String.format("The query file '%s' is not found", FILE_QUERY));
			return exportSearchCtx;
		}

		ContentReader reader = contentService.getReader(queryNodeRef, ContentModel.PROP_CONTENT);

		try {
			SAXReader saxReader = new SAXReader();
			try {
				saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			} catch (SAXException e) {
				logger.error(e.getMessage(), e);
			}
			Document doc = saxReader.read(reader.getContentInputStream());
			Element queryElt = doc.getRootElement();

			// attributes
			List<Node> columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_ATTRIBUTE);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(
								"Failed to map the following attribute. TemplateNodeRef: " + templateNodeRef + " - Attribute: " + attribute);
					}
				}

				AttributeMapping attributeMapping = new AttributeMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef);
				exportSearchCtx.getAttributeColumns().add(attributeMapping);
			}

			// characteristics
			columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_DATALIST);
			for (Node columnNode : columnNodes) {
				QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				QName dataListQName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_DATALIST_QNAME), namespaceService);
				NodeRef charactNodeRef;
				String charactNodeRefString = columnNode.valueOf(QUERY_ATTR_GET_CHARACT_NODE_REF);
				String charactName = columnNode.valueOf(QUERY_ATTR_GET_CHARACT_NAME);
				QName charactQName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_CHARACT_QNAME), namespaceService);

				// get characteristic nodeRef
				if ((charactNodeRefString != null) && !charactNodeRefString.isEmpty() && NodeRef.isNodeRef(charactNodeRefString)) {
					charactNodeRef = new NodeRef(charactNodeRefString);
				} else if (!charactName.isEmpty()) {
					AssociationDefinition assocDef = dictionaryService.getAssociation(charactQName);
					charactNodeRef = getItemByTypeAndName(assocDef.getTargetClass().getName(), charactName);

					if (charactNodeRef == null) {
						throw new MappingException(String.format("ERROR : Failed to get the nodeRef of the characteristic. Type:%s - Name:%s",
								assocDef.getTargetClass().getName(), charactName));
					}
				} else {
					throw new MappingException("ERROR : Missing Characteristic nodeRef or name. trace: " + columnNode.asXML());
				}

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(qName);
				if (attributeDef == null) {
					attributeDef = dictionaryService.getAssociation(qName);
				}

				CharacteristicMapping attributeMapping = new CharacteristicMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef, dataListQName,
						charactQName, charactNodeRef);
				exportSearchCtx.getCharacteristicsColumns().add(attributeMapping);
			}

			// file import
			columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_FILE);
			for (Node columnNode : columnNodes) {
				QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				String path = columnNode.valueOf(QUERY_ATTR_GET_PATH);
				List<String> paths = new ArrayList<>();
				String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
				Collections.addAll(paths, arrPath);

				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
				FileMapping attributeMapping = new FileMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), propertyDefinition, paths);
				exportSearchCtx.getFileColumns().add(attributeMapping);
			}

		} catch (DocumentException | ContentIOException e) {
			logger.error("Failed to read the query file", e);
		}

		return exportSearchCtx;
	}

	private NodeRef getItemByTypeAndName(QName type, String name) {
		return BeCPGQueryBuilder.createQuery().ofType(type).andPropQuery(ContentModel.PROP_NAME, name).singleValue();
	}

	/** {@inheritDoc} */
	@Override
	public void executeAction(NodeRef templateNodeRef, NodeRef downloadNode, ReportFormat reportFormat) {
		Action action = actionService.createAction(ReportSearchAction.NAME);
		action.setExecuteAsynchronously(true);
		action.setParameterValue(AbstractExportSearchAction.PARAM_TPL_NODEREF, templateNodeRef);
		action.setParameterValue(AbstractExportSearchAction.PARAM_FORMAT, reportFormat.toString());
		actionService.executeAction(action, downloadNode);
	}

}
