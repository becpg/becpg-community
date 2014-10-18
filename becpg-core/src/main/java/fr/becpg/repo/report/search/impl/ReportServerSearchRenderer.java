package fr.becpg.repo.report.search.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

@Service
public class ReportServerSearchRenderer implements SearchReportRenderer {

	private static Log logger = LogFactory.getLog(ReportServerSearchRenderer.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private BeCPGReportEngine beCPGReportEngine;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private EntityService entityService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	/** The Constant QUERY_XPATH_DATE_FORMAT. */
	protected static final String QUERY_XPATH_DATE_FORMAT = "settings/setting[@id='dateFormat']/@value";
	protected static final String QUERY_XPATH_DATETIME_FORMAT = "settings/setting[@id='datetimeFormat']/@value";
	protected static final String QUERY_XPATH_DECIMAL_PATTERN = "settings/setting[@id='decimalPattern']/@value";

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

	private static final String TAG_EXPORT = "export";
	private static final String TAG_NODES = "nodes";
	private static final String TAG_FILES = "files";
	private static final String TAG_NODE = "node";
	private static final String TAG_FILE = "file";
	private static final String ATTR_ID = "id";
	public static final String VALUE_NULL = "";

	public static final String KEY_IMAGE_NODE_IMG = "%s-%s";

	@Override
	public void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {

		try {
			ReportServerSearchContext exportSearchCtx = getQuery(templateNodeRef);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put(ReportParams.PARAM_FORMAT, reportFormat);
			params.put(ReportParams.PARAM_IMAGES, new HashMap<String, byte[]>());

			// Prepare data source
			logger.debug("Prepare data source");
			Document document = DocumentHelper.createDocument();
			Element exportElt = document.addElement(TAG_EXPORT);
			params = loadReportData(exportSearchCtx, exportElt, params, searchResults);

			if (logger.isDebugEnabled()) {
				logger.debug("Xml data: " + exportElt.asXML());
			}

			beCPGReportEngine.createReport(templateNodeRef, new ByteArrayInputStream(exportElt.asXML().getBytes()), outputStream, params);

		} catch (Exception e) {
			logger.error("Failed to run report: ", e);
		}

	}

	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME)).endsWith(ReportTplService.PARAM_VALUE_DESIGN_EXTENSION);
	}

	/**
	 * Generate Xml export data.
	 *
	 * @param queryElt
	 *            the query elt
	 * @param exportElt
	 *            the export elt
	 * @param task
	 *            the task
	 * @param nodeRefList
	 *            the node ref list
	 * @return the i run and render task
	 */
	private Map<String, Object> loadReportData(ReportServerSearchContext exportSearchCtx, Element exportElt, Map<String, Object> params,
			List<NodeRef> nodeRefList) {

		logger.debug("start loadReportData " + nodeRefList.size());
		Element nodesElt = exportElt.addElement(TAG_NODES);
		Element filesElt = exportElt.addElement(TAG_FILES);
		Integer z_idx = 1;

		for (NodeRef nodeRef : nodeRefList) {

			Element nodeElt = nodesElt.addElement(TAG_NODE);
			nodeElt.addAttribute(ATTR_ID, z_idx.toString());

			params = exportNode(exportSearchCtx, nodeElt, filesElt, params, nodeRef);

			z_idx++;
		}

		logger.debug("End loadReportData");

		return params;
	}

	/**
	 * Export properties and associations of a node.
	 *
	 * @param queryElt
	 *            the query elt
	 * @param nodeElt
	 *            the node elt
	 * @param task
	 *            the task
	 * @param nodeRef
	 *            the node ref
	 * @return the i run and render task
	 */
	private Map<String, Object> exportNode(ReportServerSearchContext exportSearchCtx, Element nodeElt, Element filesElt, Map<String, Object> params,
			NodeRef nodeRef) {

		// export class attributes
		for (AttributeMapping attributeMapping : exportSearchCtx.getAttributeColumns()) {

			String value = getColumnValue(exportSearchCtx, nodeRef, attributeMapping.getAttribute());
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

						String value = getColumnValue(exportSearchCtx, linkNodeRef, characteristicMapping.getAttribute());
						nodeElt.addAttribute(characteristicMapping.getId(), value);
					}
				}
			}
		}

		// export file
		Map<NodeRef, Map<String, String>> filesAttributes = new HashMap<NodeRef, Map<String, String>>();
		for (FileMapping fileMapping : exportSearchCtx.getFileColumns()) {

			NodeRef tempNodeRef = nodeRef;
			for (String p : fileMapping.getPath()) {

				if (tempNodeRef != null) {
					tempNodeRef = nodeService.getChildByName(tempNodeRef, ContentModel.ASSOC_CONTAINS, p);
				}
			}

			logger.debug("tempNodeRef: " + tempNodeRef);

			if (tempNodeRef != null) {

				Map<String, String> fileAttributes = filesAttributes.get(tempNodeRef);
				if (fileAttributes == null) {
					fileAttributes = new HashMap<String, String>();
					filesAttributes.put(tempNodeRef, fileAttributes);
				}

				String id = String.format(KEY_IMAGE_NODE_IMG, nodeElt.valueOf(QUERY_ATTR_GET_ID), fileMapping.getId());
				fileAttributes.put(ATTR_ID, id);

				// file content
				if (fileMapping.getAttribute().getName().isMatch(ContentModel.PROP_CONTENT)) {

					byte[] imageBytes = entityService.getImage(tempNodeRef);
					if (imageBytes != null) {

						@SuppressWarnings("unchecked")
						Map<String, byte[]> images = (Map<String, byte[]>) params.get(ReportParams.PARAM_IMAGES);
						images.put(id, imageBytes);
					}
				}
				// class attribute
				else {
					String value = getColumnValue(exportSearchCtx, nodeRef, fileMapping.getAttribute());
					fileAttributes.put(fileMapping.getId(), value);
				}
			}
		}

		if (!filesAttributes.isEmpty()) {
			for (Map<String, String> fileAttributes : filesAttributes.values()) {
				Element fileElt = filesElt.addElement(TAG_FILE);
				for (Map.Entry<String, String> kv : fileAttributes.entrySet()) {
					fileElt.addAttribute(kv.getKey(), kv.getValue());
				}
			}
		}

		return params;
	}

	/**
	 * Gets the column value.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param qName
	 *            the q name
	 * @return the column value
	 */
	private String getColumnValue(ReportServerSearchContext exportSearchCtx, NodeRef nodeRef, ClassAttributeDefinition attribute) {

		String value = VALUE_NULL;
		
		if (nodeService.exists(nodeRef)) {
			if (attribute instanceof PropertyDefinition) {
				Serializable serializable = nodeService.getProperty(nodeRef, attribute.getName());
				value = attributeExtractorService.extractPropertyForReport((PropertyDefinition) attribute, serializable,
						exportSearchCtx.getPropertyFormats(), false);

			} else if (attribute instanceof AssociationDefinition) {
				return attributeExtractorService.extractAssociationsForReport(nodeService.getTargetAssocs(nodeRef, attribute.getName()), ContentModel.PROP_NAME);
			}
		}

		return value;
	}

	/**
	 * Load the query file.
	 *
	 * @param templateNodeRef
	 *            the template node ref
	 * @param queryFileName
	 *            the query file name
	 * @return the query
	 * @throws MappingException
	 * @throws BeCPGException
	 */
	@SuppressWarnings("unchecked")
	private ReportServerSearchContext getQuery(NodeRef templateNodeRef) throws MappingException {

		Element queryElt = null;
		ReportServerSearchContext exportSearchCtx = new ReportServerSearchContext();

		NodeRef folderNodeRef = nodeService.getPrimaryParent(templateNodeRef).getParentRef();
		NodeRef queryNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, FILE_QUERY);

		if (queryNodeRef == null) {
			logger.error(String.format("The query file '%s' is not found", FILE_QUERY));
			return exportSearchCtx;
		}

		ContentReader reader = contentService.getReader(queryNodeRef, ContentModel.PROP_CONTENT);
		InputStream is = null;
		SAXReader saxReader = new SAXReader();

		try {
			is = reader.getContentInputStream();
			Document doc = saxReader.read(is);
			queryElt = doc.getRootElement();

			// date format
			Node dateFormat = queryElt.selectSingleNode(QUERY_XPATH_DATE_FORMAT);
			if (dateFormat != null) {
				exportSearchCtx.getPropertyFormats().setDateFormat(dateFormat.getStringValue());
			}

			// datetime format
			Node datetimeFormat = queryElt.selectSingleNode(QUERY_XPATH_DATETIME_FORMAT);
			if (datetimeFormat != null) {
				exportSearchCtx.getPropertyFormats().setDatetimeFormat(datetimeFormat.getStringValue());
			}

			// decimal format
			Node decimalFormatPattern = queryElt.selectSingleNode(QUERY_XPATH_DECIMAL_PATTERN);
			if (decimalFormatPattern != null) {
				exportSearchCtx.getPropertyFormats().setDecimalFormat(decimalFormatPattern.getStringValue());
			}

			// attributes
			List<Node> columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_ATTRIBUTE);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException("Failed to map the following attribute. TemplateNodeRef: " + templateNodeRef + " - Attribute: "
								+ attribute);
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
				NodeRef charactNodeRef = null;
				String charactNodeRefString = columnNode.valueOf(QUERY_ATTR_GET_CHARACT_NODE_REF);
				String charactName = columnNode.valueOf(QUERY_ATTR_GET_CHARACT_NAME);
				QName charactQName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_CHARACT_QNAME), namespaceService);

				// get characteristic nodeRef
				if (charactNodeRefString != null && !charactNodeRefString.isEmpty() && NodeRef.isNodeRef(charactNodeRefString)) {
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

				CharacteristicMapping attributeMapping = new CharacteristicMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef,
						dataListQName, charactQName, charactNodeRef);
				exportSearchCtx.getCharacteristicsColumns().add(attributeMapping);
			}

			// file import
			columnNodes = queryElt.selectNodes(QUERY_XPATH_COLUMNS_FILE);
			for (Node columnNode : columnNodes) {
				QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				String path = columnNode.valueOf(QUERY_ATTR_GET_PATH);
				List<String> paths = new ArrayList<String>();
				String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
				for (String p : arrPath)
					paths.add(p);

				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
				FileMapping attributeMapping = new FileMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), propertyDefinition, paths);
				exportSearchCtx.getFileColumns().add(attributeMapping);
			}

		} catch (DocumentException e) {
			logger.error("Failed to read the query file", e);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return exportSearchCtx;
	}

	private NodeRef getItemByTypeAndName(QName type, String name) {
		return BeCPGQueryBuilder.createQuery().ofType(type).andPropQuery(ContentModel.PROP_NAME, name).singleValue();
	}

}
