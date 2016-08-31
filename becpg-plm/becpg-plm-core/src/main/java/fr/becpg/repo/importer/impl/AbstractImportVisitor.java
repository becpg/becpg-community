/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.FormulaMapping;
import fr.becpg.config.mapping.HierarchyMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.extractor.RemoteHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Abstract class used to import a node with its attributes and files.
 *
 * @author querephi
 */
public class AbstractImportVisitor implements ImportVisitor, ApplicationContextAware {

	protected static final String QUERY_XPATH_MAPPING = "mapping";

	protected static final String QUERY_XPATH_DATE_FORMAT = "settings/setting[@id='dateFormat']/@value";

	protected static final String QUERY_XPATH_DATETIME_FORMAT = "settings/setting[@id='datetimeFormat']/@value";

	protected static final String QUERY_XPATH_DECIMAL_PATTERN = "settings/setting[@id='decimalPattern']/@value";

	protected static final String QUERY_XPATH_NODE_COLUMN_KEY = "nodeColumnKeys/nodeColumnKey";

	protected static final String QUERY_XPATH_DATALIST_COLUMN_KEY = "dataListColumnKeys/dataListColumnKey";

	protected static final String QUERY_XPATH_COLUMNS_ATTRIBUTE = "columns/column[@type='Attribute']";

	protected static final String QUERY_XPATH_COLUMNS_FORMULA = "columns/column[@type='Formula']";

	protected static final String QUERY_XPATH_COLUMNS_DATALIST = "columns/column[@type='Characteristic']";

	protected static final String QUERY_XPATH_COLUMNS_FILE = "columns/column[@type='File']";

	protected static final String QUERY_ATTR_GET_ID = "@id";

	protected static final String QUERY_ATTR_GET_ATTRIBUTE = "@attribute";

	protected static final String QUERY_ATTR_GET_TARGET_CLASS = "@targetClass";

	protected static final String QUERY_ATTR_GET_NAME = "@name";

	protected static final String QUERY_ATTR_GET_DATALIST_QNAME = "@dataListQName";

	protected static final String QUERY_ATTR_GET_PATH = "@path";

	protected static final String QUERY_ATTR_GET_CHARACT_QNAME = "@charactQName";

	protected static final String QUERY_ATTR_GET_CHARACT_NODE_REF = "@charactNodeRef";

	protected static final String QUERY_ATTR_GET_CHARACT_NAME = "@charactName";

	private static final String QUERY_XPATH_COLUMNS_HIERARCHY = "columns/column[@type='Hierarchy']";

	private static final String QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE = "@parentLevelAttribute";

	private static final String QUERY_ATTR_GET_PARENT_LEVEL = "@parentLevel";

	protected static final String CACHE_KEY = "cKey%s-%s";

	protected static final String MSG_ERROR_LOAD_FILE = "import_service.error.err_load_file";
	protected static final String MSG_ERROR_FILE_NOT_FOUND = "import_service.error.err_file_not_found";
	protected static final String MSG_ERROR_MAPPING_ATTR_FAILED = "import_service.error.err_mapping_attr_failed";
	protected static final String MSG_ERROR_GET_OR_CREATE_NODEREF = "import_service.error.err_get_or_create_noderef";
	protected static final String MSG_ERROR_GET_NODEREF_CHARACT = "import_service.error.err_get_noderef_charact";
	protected static final String MSG_ERROR_UNDEFINED_CHARACT = "import_service.error.err_undefined_charact";
	protected static final String MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING = "import_service.error.err_columns_do_not_respect_mapping";
	protected static final String MSG_ERROR_TARGET_ASSOC_NOT_FOUND = "import_service.error.err_target_assoc_not_found";
	protected static final String MSG_ERROR_TARGET_ASSOC_SEVERAL = "import_service.error.err_target_assoc_several";
	protected static final String MSG_ERROR_GET_ASSOC_TARGET = "import_service.error.err_get_assoc_target";
	protected static final String MSG_ERROR_NO_DOCS_BASE_PATH_SET = "import_service.error.err_no_docs_base_path_set";

	private static final Log logger = LogFactory.getLog(AbstractImportVisitor.class);

	protected NodeService nodeService;

	protected DictionaryService dictionaryService;

	protected RepoService repoService;

	protected ContentService contentService;

	protected MimetypeService mimetypeService;

	protected NamespaceService namespaceService;

	protected ApplicationContext applicationContext;

	protected EntityListDAO entityListDAO;

	protected AutoNumService autoNumService;

	protected HierarchyService hierarchyService;

	protected Repository repositoryHelper;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException {

		logger.debug("ImportNode. type: " + importContext.getType());

		// import properties
		Map<QName, Serializable> properties = getNodePropertiesToImport(importContext, values);

		// work around since CodePolicy is asynchronous, so we need to generate
		// code if empty
		String code = (String) properties.get(BeCPGModel.PROP_CODE);
		if ((code != null) && code.isEmpty()) {
			code = autoNumService.getAutoNumValue(importContext.getType(), BeCPGModel.PROP_CODE);
			properties.put(BeCPGModel.PROP_CODE, code);
		}

		NodeRef nodeRef = findNode(importContext, importContext.getType(), properties);

		if (nodeRef == null) {
			String name = getName(importContext.getType(), properties);

			if (logger.isDebugEnabled()) {
				logger.debug("create node. Type: " + importContext.getType() + " - Properties: " + properties);
			}
			QName assocName = ContentModel.ASSOC_CHILDREN;
			if ((name != null) && (name.length() > 0)) {
				assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));
			}

			nodeRef = nodeService.createNode(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, assocName, importContext.getType(),
					ImportHelper.cleanProperties(properties)).getChildRef();
		} else if (importContext.isDoUpdate()) {

			if (logger.isDebugEnabled()) {
				logger.debug("update node. Properties: " + properties);
			}
			nodeService.setType(nodeRef, importContext.getType());

			for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
				if ((entry.getValue() != null) && ImportHelper.NULL_VALUE.equals(entry.getValue())) {
					nodeService.removeProperty(nodeRef, entry.getKey());
				} else {
					nodeService.setProperty(nodeRef, entry.getKey(), entry.getValue());
				}

			}
		} else {
			logger.info("Update mode is not enabled so no update is done.");
		}

		// import associations
		logger.debug("Import Assocs");
		importAssociations(importContext, values, nodeRef);

		// import files
		logger.debug("Import Files");
		importFiles(importContext, values, nodeRef);

		logger.debug("Node Imported");

		return nodeRef;
	}

	private String getName(QName type, Map<QName, Serializable> properties) {
		String name = null;
		QName propName = RemoteHelper.getPropName(type, dictionaryService);
		if (properties.get(propName) != null) {
			if (properties.get(propName) instanceof MLText) {
				name = ((MLText) properties.get(propName)).getDefaultValue();
			} else {
				name = (String) properties.get(propName);
			}
		}
		return name;
	}

	/**
	 * Calculate the properties of the node import
	 *
	 * @param importContext
	 * @param values
	 * @return
	 * @throws ParseException
	 * @throws ImporterException
	 */
	@SuppressWarnings("unchecked")
	protected Map<QName, Serializable> getNodePropertiesToImport(ImportContext importContext, List<String> values)
			throws ParseException, ImporterException {

		Map<QName, Serializable> properties = new HashMap<>();

		for (int z_idx = 0; (z_idx < values.size()) && (z_idx < importContext.getColumns().size()); z_idx++) {

			AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);

			if ((attributeMapping instanceof AttributeMapping) || (attributeMapping instanceof HierarchyMapping)
					|| (attributeMapping instanceof FormulaMapping)) {
				ClassAttributeDefinition column = attributeMapping.getAttribute();

				if (column instanceof PropertyDefinition) {
					PropertyDefinition propDef = (PropertyDefinition) column;
					Serializable value = null;
					QName dataType = propDef.getDataType().getName();

					if (ImportHelper.NULL_VALUE.equalsIgnoreCase(values.get(z_idx))) {
						value = ImportHelper.NULL_VALUE;
					} else {

						if (dataType.isMatch(DataTypeDefinition.NODE_REF)) {
							if (propDef.isMultiValued()) {
								String[] arrValue = split(values.get(z_idx));

								for (String v : arrValue) {
									if (!v.isEmpty()) {
										NodeRef nodeRef = findPropertyTargetNodeByValue(importContext, propDef, attributeMapping, v, properties);
										if (nodeRef != null) {
											if (value == null) {
												value = new ArrayList<NodeRef>();
											}
											((List<NodeRef>) value).add(nodeRef);
										}
									}
								}

							} else {
								if(values.get(z_idx)!=null && ! values.get(z_idx).isEmpty()){
									value = findPropertyTargetNodeByValue(importContext, propDef, attributeMapping, values.get(z_idx), properties);
								}
							}
						} else {

							value = ImportHelper.loadPropertyValue(importContext, values, z_idx);

							if ((value instanceof String) && (attributeMapping instanceof FormulaMapping)) {
								value = parseFormula((String) value);
							}

						}

					}

					if (value != null) {
						properties.put(column.getName(), value);
					}
				}
			}

		}

		return properties;
	}

	private String[] split(String value) {
		String[] ret = null;
		if (value != null) {
			ret = value.replace("\\,", "@ML@").split(RepoConsts.MULTI_VALUES_SEPARATOR);
			for (int i = 0; i < ret.length; i++) {
				ret[i] = ret[i].replace("@ML@", ",");
			}
		}
		return ret;

	}

	private String parseFormula(String formula) throws ImporterException {
		try {
			ExpressionParser parser = new SpelExpressionParser();
			StandardEvaluationContext context = new StandardEvaluationContext(this);

			return parser.parseExpression(formula, new ParserContext() {

				@Override
				public String getExpressionPrefix() {
					return "${";
				}

				@Override
				public String getExpressionSuffix() {
					return "}";
				}

				@Override
				public boolean isTemplate() {
					return true;
				}
			}).getValue(context, String.class);
		} catch (Exception e) {
			logger.error("Cannot parse formula :" + formula, e);
			throw new ImporterException("Cannot parse formula :" + formula, e);
		}
	}

	// DO NOT REMOVED USED in FORMULA
	public String findCharact(String type, String name) {
		NodeRef ret = findCharact(QName.createQName(type, namespaceService), name);
		if (ret == null) {
			logger.error("Cannot find (" + type + "," + name + ")");
			return null;
		}

		return ret.toString();

	}

	private NodeRef findCharact(QName type, String name) {

		for (NodeRef tmpNodeRef : BeCPGQueryBuilder.createQuery().ofType(type).andPropEquals(BeCPGModel.PROP_CHARACT_NAME, name).inDB().ftsLanguage()
				.list()) {
			if (!nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
					&& !nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
				return tmpNodeRef;
			}
		}
		return null;
	}

	/**
	 * Import the associations of the node
	 *
	 * @param importContext
	 * @param values
	 * @param nodeRef
	 * @throws InvalidTargetNodeException
	 * @throws ImporterException
	 */
	protected void importAssociations(ImportContext importContext, List<String> values, NodeRef nodeRef) throws ImporterException {

		for (int z_idx = 0; (z_idx < values.size()) && (z_idx < importContext.getColumns().size()); z_idx++) {

			AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);

			if (attributeMapping instanceof AttributeMapping) {
				ClassAttributeDefinition column = attributeMapping.getAttribute();

				if (column instanceof AssociationDefinition) {
					AssociationDefinition assocDef = (AssociationDefinition) column;
					String value = values.get(z_idx);

					if (value != null && !value.isEmpty()) {
						QName targetClass = ((AttributeMapping) attributeMapping).getTargetClass();
						logger.debug("importAssociations targetClass" + targetClass);
						List<NodeRef> targetRefs = findTargetNodesByValue(importContext, assocDef.isTargetMany(),
								targetClass != null ? targetClass : assocDef.getTargetClass().getName(), value);

						// mandatory target not found
						if (targetRefs.isEmpty()) {
							throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_ASSOC_TARGET, assocDef.getName(), value));
						}

						// remove associations if needed
						List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
						for (AssociationRef assocRef : assocRefs) {
							NodeRef targetRef = assocRef.getTargetRef();
							if (targetRefs.contains(targetRef)) {
								logger.debug("Assoc already present");
								targetRefs.remove(targetRef);
							} else {
								logger.debug("Remove assocs :" + assocDef.getName());
								nodeService.removeAssociation(nodeRef, targetRef, assocDef.getName());
							}
						}

						// add new associations, the rest
						for (NodeRef targetRef : targetRefs) {
							logger.debug("Add assocs :" + assocDef.getName());
							nodeService.createAssociation(nodeRef, targetRef, assocDef.getName());
						}
					} else if(assocDef.isTargetMandatory()){
						throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_ASSOC_TARGET, assocDef.getName(), value));
					}
				}
			}
		}
	}

	/**
	 * Import the files of the node
	 *
	 * @param importContext
	 * @param values
	 * @param nodeRef
	 * @throws ImporterException
	 */
	protected void importFiles(ImportContext importContext, List<String> values, NodeRef nodeRef) throws ParseException, ImporterException {

		/*
		 * import files
		 *
		 * - get the targetFolder where files will be stored
		 */
		NodeRef targetFolderNodeRef = nodeRef;
		String fileName = "";
		List<String> path = new ArrayList<>();
		for (int z_idx = 0; (z_idx < values.size()) && (z_idx < importContext.getColumns().size()); z_idx++) {

			AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);

			if (attributeMapping instanceof FileMapping) {

				// look for parent
				FileMapping fileMapping = (FileMapping) attributeMapping;
				QName contentQName = (fileMapping.getAttribute() instanceof PropertyDefinition) ? fileMapping.getAttribute().getName()
						: ContentModel.PROP_CONTENT;

				// do not reload the same folder several times
				if (!path.equals(fileMapping.getPath())) {

					path = fileMapping.getPath();

					if (fileMapping.getPath().size() > 1) {

						fileName = fileMapping.getPath().get(fileMapping.getPath().size() - 1);

						// remove the last path since it is the fileName
						List<String> pathFolders = new ArrayList<>();
						for (int cntPath = 0; cntPath < (fileMapping.getPath().size() - 1); cntPath++) {
							pathFolders.add(fileMapping.getPath().get(cntPath));
						}

						logger.debug("creates folders" + pathFolders);
						targetFolderNodeRef = repoService.getOrCreateFolderByPaths(nodeRef, pathFolders);
					} else {
						targetFolderNodeRef = nodeRef;
					}
				}

				String value = values.get(z_idx);

				if ((value != null) && value.startsWith("reg:")) {
					File docsFolder = new File(importContext.getDocsBasePath());

					if ((importContext.getDocsBasePath() != null) && docsFolder.isDirectory()) {

						Collection<File> files = FileUtils.listFiles(docsFolder, null, true);

						for (File file : files) {
							String regexp = value.replace("reg:", "");
							if (Pattern.matches(regexp, file.getName())) {

								logger.debug(file.getName() + " match regexp " + regexp);

								NodeRef fileNodeRef = createFile(targetFolderNodeRef, file.getName(), file.getName());
								String mimetype = mimetypeService.guessMimetype(file.getName());
								FileInputStream in = null;
								try {
									in = new FileInputStream(file);

									ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
									Charset charset = charsetFinder.getCharset(in, mimetype);
									String encoding = charset.name();

									ContentWriter writer = contentService.getWriter(fileNodeRef, contentQName, true);
									writer.setMimetype(mimetype);
									writer.setEncoding(encoding);
									writer.putContent(in);
								} catch (FileNotFoundException e) {
									throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_LOAD_FILE, value));

								} finally {
									IOUtils.closeQuietly(in);
								}
							}
						}

					} else {
						throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_NO_DOCS_BASE_PATH_SET, value));
					}
				} else if ((value != null) && !value.isEmpty()) {

					// add file content
					if (fileMapping.getAttribute().getName().equals(ContentModel.PROP_CONTENT)) {

						if (value.contains(",")) {
							int count = 0;
							for (String fileNameValue : split(value)) {
								importFileContent(fileNameValue, targetFolderNodeRef, fixFileNameExtension(fileName, fileNameValue, count),
										fileMapping.getId() + (count > 0 ? "-" + count : ""));
								count++;
							}

						} else {

							importFileContent(value, targetFolderNodeRef, fixFileNameExtension(fileName, value, 0), fileMapping.getId());

						}
					}
					// manage only properties
					else if (fileMapping.getAttribute() instanceof PropertyDefinition) {

						NodeRef fileNodeRef = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, fileName);

						if (fileNodeRef != null) {

							PropertyDefinition propertyDefinition = (PropertyDefinition) fileMapping.getAttribute();

							nodeService.setProperty(fileNodeRef, propertyDefinition.getName(),
									ImportHelper.loadPropertyValue(importContext, values, z_idx));
						}
					}
				}
			}
		}
	}

	private String fixFileNameExtension(String fileName, String value, int count) {
		String[] tokens = value.split("\\.(?=[^\\.]+$)");

		if (tokens.length > 1) {
			return fileName.split("\\.(?=[^\\.]+$)")[0] + (count > 0 ? "-" + count : "") + "." + tokens[1];
		}

		return fileName;

	}

	private void importFileContent(String value, NodeRef targetFolderNodeRef, String mappingFileName, String mappingId) throws ImporterException {
		InputStream in = null;
		try {
			if ((value.startsWith("classpath:") || value.startsWith("file:") || value.startsWith("http:") || value.startsWith("ftp:"))) {

				try {
					Resource resource = applicationContext.getResource(value);
					in = resource.getInputStream();
				} catch (IOException e) {
					logger.error("No resource found in path " + value);
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_LOAD_FILE, value));
				}

			} else if (new File(value).exists()) {
				try {
					in = new FileInputStream(value);
				} catch (FileNotFoundException e) {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_LOAD_FILE, value));
				}
			} else {
				logger.warn(I18NUtil.getMessage(MSG_ERROR_FILE_NOT_FOUND, value));
			}

			if (in != null) {
				// create file if it doesn't exist
				NodeRef fileNodeRef = createFile(targetFolderNodeRef, mappingFileName, mappingId);

				String mimetype = mimetypeService.guessMimetype(value);
				ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
				Charset charset = charsetFinder.getCharset(in, mimetype);
				String encoding = charset.name();

				ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
				writer.setMimetype(mimetype);
				writer.setEncoding(encoding);
				writer.putContent(in);
			}

		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	private NodeRef createFile(NodeRef targetFolderNodeRef, String fileName, String localName) {
		NodeRef fileNodeRef = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
		if (fileNodeRef == null) {
			Map<QName, Serializable> fileProperties = new HashMap<>();
			fileProperties.put(ContentModel.PROP_NAME, fileName);
			fileNodeRef = nodeService.createNode(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(localName)), ContentModel.TYPE_CONTENT,
					fileProperties).getChildRef();
		}
		return fileNodeRef;
	}

	/**
	 * Load the mapping of each class.
	 *
	 * @param mappingsElt
	 *            the mappings elt
	 * @param importContext
	 *            the import context
	 * @return the import context
	 * @throws ImporterException
	 *             the be cpg exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ImportContext loadClassMapping(Element mappingsElt, ImportContext importContext) throws MappingException {

		List<Node> mappingNodes = mappingsElt.selectNodes(QUERY_XPATH_MAPPING);

		Node dateFormat = mappingsElt.selectSingleNode(QUERY_XPATH_DATE_FORMAT);
		if (dateFormat != null) {
			importContext.getPropertyFormats().setDateFormat(dateFormat.getStringValue());
		}

		Node datetimeFormat = mappingsElt.selectSingleNode(QUERY_XPATH_DATETIME_FORMAT);
		if (datetimeFormat != null) {
			importContext.getPropertyFormats().setDateFormat(datetimeFormat.getStringValue());
		}

		Node decimalFormatPattern = mappingsElt.selectSingleNode(QUERY_XPATH_DECIMAL_PATTERN);
		if (decimalFormatPattern != null) {
			importContext.getPropertyFormats().setDecimalFormat(decimalFormatPattern.getStringValue());
		}

		for (Node mappingNode : mappingNodes) {

			QName typeQName = QName.createQName(mappingNode.valueOf(QUERY_ATTR_GET_NAME), namespaceService);
			ClassMapping classMapping = new ClassMapping();
			classMapping.setType(typeQName);
			
			logger.debug("Register mapping for : "+typeQName);
			
			importContext.getClassMappings().put(typeQName, classMapping);

			// node keys
			List<Node> nodeColumnKeyNodes = mappingNode.selectNodes(QUERY_XPATH_NODE_COLUMN_KEY);
			for (Node columnNode : nodeColumnKeyNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				// classMapping.getNodeColumnKeys().add(new
				// KeyAttributeMapping(id, attributeDef, classQName));
				classMapping.getNodeColumnKeys().add(attribute);
			}

			// productlist keys
			List<Node> dataListColumnKeyNodes = mappingNode.selectNodes(QUERY_XPATH_DATALIST_COLUMN_KEY);
			for (Node columnNode : dataListColumnKeyNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				// classMapping.getDataListColumnKeys().add(new
				// KeyAttributeMapping(id, attributeDef, classQName));
				classMapping.getDataListColumnKeys().add(attribute);
			}

			// attributes
			List<Node> columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_ATTRIBUTE);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				AttributeMapping attributeMapping = new AttributeMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef);
				String targetClass = columnNode.valueOf(QUERY_ATTR_GET_TARGET_CLASS);
				logger.debug("targetClass: " + targetClass);
				if ((targetClass != null) && !targetClass.isEmpty()) {
					attributeMapping.setTargetClass(QName.createQName(targetClass, namespaceService));
				}
				classMapping.getColumns().add(attributeMapping);
			}

			// Formula
			columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_FORMULA);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				AbstractAttributeMapping attributeMapping = new FormulaMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef);
				classMapping.getColumns().add(attributeMapping);
			}

			// characteristics
			columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_DATALIST);
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
					charactNodeRef = findCharact(assocDef.getTargetClass().getName(), charactName);

					if (charactNodeRef == null) {
						String error = I18NUtil.getMessage(MSG_ERROR_GET_NODEREF_CHARACT, assocDef.getTargetClass().getName(), charactName);
						logger.error(error);
						throw new MappingException(error);
					}
				} else {
					throw new MappingException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_CHARACT, columnNode.asXML()));
				}

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(qName);
				if (attributeDef == null) {
					attributeDef = dictionaryService.getAssociation(qName);
				}

				CharacteristicMapping attributeMapping = new CharacteristicMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef, dataListQName,
						charactQName, charactNodeRef);
				classMapping.getColumns().add(attributeMapping);
			}

			// file import
			columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_FILE);
			for (Node columnNode : columnNodes) {
				QName qName = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				String path = columnNode.valueOf(QUERY_ATTR_GET_PATH);
				List<String> paths = new ArrayList<>();
				String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
				Collections.addAll(paths, arrPath);

				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
				FileMapping attributeMapping = new FileMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), propertyDefinition, paths);
				classMapping.getColumns().add(attributeMapping);
			}

			// hierachies
			columnNodes = mappingNode.selectNodes(QUERY_XPATH_COLUMNS_HIERARCHY);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				ClassAttributeDefinition parentLevelAttributeDef = null;

				if ((columnNode.valueOf(QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE) != null)
						&& !columnNode.valueOf(QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE).isEmpty()) {
					attribute = QName.createQName(columnNode.valueOf(QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE), namespaceService);

					parentLevelAttributeDef = dictionaryService.getProperty(attribute);
					if (parentLevelAttributeDef == null) {

						parentLevelAttributeDef = dictionaryService.getAssociation(attribute);
						if (parentLevelAttributeDef == null) {
							throw new MappingException(I18NUtil.getMessage(MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
						}
					}
				}

				AbstractAttributeMapping attributeMapping = new HierarchyMapping(columnNode.valueOf(QUERY_ATTR_GET_ID), attributeDef,
						(columnNode.valueOf(QUERY_ATTR_GET_PARENT_LEVEL) != null) && !columnNode.valueOf(QUERY_ATTR_GET_PARENT_LEVEL).isEmpty()
								? columnNode.valueOf(QUERY_ATTR_GET_PARENT_LEVEL) : null,
						columnNode.valueOf(QUERY_ATTR_GET_PATH), parentLevelAttributeDef);
				classMapping.getColumns().add(attributeMapping);
			}

		}

		return importContext;
	}

	/**
	 * Load the columns of the type and check the import file respects the
	 * mapping file.
	 *
	 * @param mappingElt
	 *            the mapping elt
	 * @param columns
	 *            the columns
	 * @param importContext
	 *            the import context
	 * @return the import context
	 * @throws ImporterException
	 *             the be cpg exception
	 * @throws MappingException
	 */
	@Override
	public ImportContext loadMappingColumns(Element mappingElt, List<String> columns, ImportContext importContext) throws MappingException {

		ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());
		logger.debug("Type: " + importContext.getType() + ", find matching class mapping: " + classMapping!=null);

		// check COLUMNS respects the mapping and the class attributes
		List<AbstractAttributeMapping> columnsAttributeMapping = new ArrayList<>();
		List<String> unknownColumns = new ArrayList<>();
		boolean isMLPropertyDef = false;
		for (String column : columns) {

			boolean isAttributeMapped = false;
			String columnId = column;

			if (classMapping != null) {
				// columns
				for (AbstractAttributeMapping attrMapping : classMapping.getColumns()) {
					if (attrMapping.getId().equals(columnId)) {
						logger.debug("Find matching attribute mapping columnId : " + columnId );
						columnsAttributeMapping.add(attrMapping);
						isAttributeMapped = true;
						break;
					}
				}
			}

			// columnId not mapped, is it a property or an association ?
			if (!isAttributeMapped) {
				QName qName;
				if (columnId.indexOf(QName.NAMESPACE_BEGIN) != -1) {
					qName = QName.createQName(columnId);
				} else {
					qName = QName.createQName(columnId, namespaceService);
				}

				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);

				if (propertyDefinition != null) {
					logger.debug("Create mapping column for property, id: " + columnId + " - name: " + propertyDefinition.getName());
					AbstractAttributeMapping attributeMapping = new AttributeMapping(columnId, propertyDefinition);
					columnsAttributeMapping.add(attributeMapping);

					// MLText : we store that we got an MLText Property, so the
					// next COLUMNS may be propertyName_Locale
					// (bcpg:ingMLName_en)
					// so they won't be defined in the dictionary
					if (propertyDefinition.getDataType().toString().equals(DataTypeDefinition.MLTEXT.toString())) {
						isMLPropertyDef = true;
					} else {
						isMLPropertyDef = false;
					}
				} else {
					AssociationDefinition assocDefinition = dictionaryService.getAssociation(qName);
					if (assocDefinition != null) {
						logger.debug("Create mapping column for assoc, id: " + columnId + " - name: " + assocDefinition.getName());
						AbstractAttributeMapping attributeMapping = new AttributeMapping(columnId, assocDefinition);
						columnsAttributeMapping.add(attributeMapping);
					} else if (isMLPropertyDef) {
						// not defined in dictionary but, it is an mltext
						logger.debug("Create mapping column for MLText translation, id: " + columnId);
						columnsAttributeMapping.add(new AttributeMapping(columnId, null));
					} else {
						unknownColumns.add(columnId);
					}
				}
			}
		}

		if (!unknownColumns.isEmpty()) {

			// calculate mappedColumns
			List<String> mappedColumns = new ArrayList<>();
			if (classMapping != null) {
				for (AbstractAttributeMapping attrMapping : classMapping.getColumns()) {
					mappedColumns.add(attrMapping.getId());
				}
			}

			String error = I18NUtil.getMessage(MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING, importContext.getType(), (classMapping != null),
					unknownColumns, mappedColumns);
			logger.error(error);
			throw new MappingException(error);
		}

		importContext.setColumns(columnsAttributeMapping);

		if (logger.isDebugEnabled()) {
			logger.debug("importContext.getColumns() " + importContext.getColumns());
		}

		return importContext;
	}

	/**
	 * Check if the node exists, according to : - keys or code - Path and name.
	 *
	 * @param importContext
	 *            the import context
	 * @param type
	 *            the type
	 * @param properties
	 *            the properties
	 * @return the node ref
	 */
	protected NodeRef findNode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException {

		NodeRef nodeRef = findNodeByKeyOrCode(importContext, type, properties);

		if (nodeRef == null) {

			String name = (String) properties.get(ContentModel.PROP_NAME);
			if ((name != null) && !name.isEmpty()) {

				// look in import folder
				nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, name);
			} else if (!dictionaryService.isSubClass(type, BeCPGModel.TYPE_LINKED_VALUE)
					&& !dictionaryService.isSubClass(type, BeCPGModel.TYPE_LIST_VALUE)
					&& !dictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)) {

				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_OR_CREATE_NODEREF));
			}
		}

		return nodeRef;
	}

	/**
	 * find the node by key properties, according to : - nodeColumnKey - code.
	 *
	 * @param importContext
	 *            the import context
	 * @param type
	 *            the type
	 * @param codeQName
	 *            the code q name
	 * @param properties
	 *            the properties
	 * @return the node ref
	 * @throws ImporterException
	 */
	protected NodeRef findNodeByKeyOrCode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException {

		NodeRef nodeRef = null;

		ClassMapping classMapping = importContext.getClassMappings().get(type);

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(type);

		boolean doQuery = false;

		// nodeColumnKeys
		if ((classMapping != null) && !classMapping.getNodeColumnKeys().isEmpty()) {

			for (QName attribute : classMapping.getNodeColumnKeys()) {

				if (logger.isDebugEnabled()) {
					logger.debug("attribute: " + attribute + " value: " + properties.get(attribute));
				}

				if (ContentModel.ASSOC_CONTAINS.isMatch(attribute)
						|| BeCPGModel.PROP_LV_VALUE.isMatch(attribute)) {
					// query by path
					NodeRef folderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
							importContext.getPath());
					queryBuilder.parent(folderNodeRef);
					
					if(BeCPGModel.PROP_LV_VALUE.isMatch(attribute)){
						if(properties.get(attribute)!=null && properties.get(attribute) instanceof MLText){
							queryBuilder.andPropEquals(BeCPGModel.PROP_LV_VALUE, ((MLText) properties.get(attribute)).getDefaultValue());
						} else if(properties.get(attribute)!=null ){
							queryBuilder.andPropEquals(BeCPGModel.PROP_LV_VALUE,properties.get(attribute).toString());
						}
					}
					
					doQuery = true;
				} else if (properties.get(attribute) != null) {

					String value = null;
					if (properties.get(attribute) instanceof MLText) {
						value = ((MLText) properties.get(attribute)).getDefaultValue();
					} else {
						value = properties.get(attribute).toString();
					}

					if (ImportHelper.NULL_VALUE.equals(value)) {
						if (attribute.equals(BeCPGModel.PROP_PARENT_LEVEL)) {
							queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, Integer.toString(RepoConsts.DEFAULT_LEVEL));
						} else {
							throw new ImporterException("NodeColumnKey cannot be null. NodeColumnKey: " + attribute);
						}
					} else if ((properties.get(attribute) != null) && NodeRef.isNodeRef(value) && (classMapping.getNodeColumnKeys().size() == 1)) {
						return new NodeRef(value);
					} else {
						queryBuilder.andPropEquals(attribute, value);
					}
					doQuery = true;
				} else {
					logger.warn("Value of NodeColumnKey " + attribute + " is null (or it is not a property).");
				}
			}
		} else {
			logger.debug("nodeColumnKeys is empty type: " + type);

			// look for codeAspect
			if (dictionaryService.getType(type).getDefaultAspects() != null) {
				for (AspectDefinition aspectDef : dictionaryService.getType(type).getDefaultAspects()) {
					if (aspectDef.getName().equals(BeCPGModel.ASPECT_CODE) && (properties.get(BeCPGModel.PROP_CODE) != null)) {
						if (NodeRef.isNodeRef(properties.get(BeCPGModel.PROP_CODE).toString())) {
							return new NodeRef(properties.get(BeCPGModel.PROP_CODE).toString());
						} else {
							queryBuilder.andPropEquals(BeCPGModel.PROP_CODE, (String) properties.get(BeCPGModel.PROP_CODE));
							doQuery = true;
							break;
						}
					}
				}
			}

			QName propName = RemoteHelper.getPropName(type, dictionaryService);

			// look by name
			if (!doQuery && !dictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2) && (properties.get(propName) != null)) {

				String name = getName(type, properties);

				if (NodeRef.isNodeRef(name.toString())) {
					return new NodeRef(name.toString());
				} else {
					queryBuilder.andPropEquals(propName, name);
					doQuery = true;
				}
			} else if ((properties.get(ContentModel.PROP_NAME) != null) && NodeRef.isNodeRef(properties.get(ContentModel.PROP_NAME).toString())) {
				return new NodeRef(properties.get(ContentModel.PROP_NAME).toString());
			}

			if (!doQuery) {
				logger.warn("No keys defined in mapping, neither code property. Type: " + type + " Properties: " + properties);
			}
		}

		if (doQuery) {
			logger.debug("findNodeByKeyOrCode: " + queryBuilder.toString());

			// TODO Refactor used to lookup for parent in the same list
			if (dictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM) && !dictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)
					&& !dictionaryService.isSubClass(type, BeCPGModel.TYPE_LINKED_VALUE)
					&& !dictionaryService.isSubClass(type, BeCPGModel.TYPE_LIST_VALUE) && !dictionaryService.isSubClass(type, PLMModel.TYPE_PLANT)
					&& !dictionaryService.isSubClass(type, PLMModel.TYPE_TRADEMARK)) {
				for (NodeRef tmpNodeRef : queryBuilder.inDB().ftsLanguage().list()) {
					if (nodeService.getPrimaryParent(tmpNodeRef).getParentRef().equals(importContext.getParentNodeRef())
							&& !nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& !nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
						return tmpNodeRef;
					}
				}
			} else {
				// TODO #ALF-21197 excludeDefault instead
				for (NodeRef tmpNodeRef : queryBuilder.inDB().ftsLanguage().list()) {
					if (!nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& !nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
						return tmpNodeRef;
					}
				}
			}

		}

		return nodeRef;
	}

	/**
	 * find the nodes by value (multi-value or single value)
	 *
	 * @param importContext
	 * @param assocDef
	 * @param value
	 * @return
	 * @throws InvalidTargetNodeException
	 * @throws ImporterException
	 */
	protected List<NodeRef> findTargetNodesByValue(ImportContext importContext, boolean isTargetMany, QName targetClass, String value)
			throws ImporterException {

		List<NodeRef> targetRefs = new ArrayList<>();

		if (!value.isEmpty()) {

			if (isTargetMany) {
				String[] arrValue = split(value);

				for (String v : arrValue) {
					if (!v.isEmpty()) {
						NodeRef targetNodeRef = findTargetNodeByValue(importContext, targetClass, v);
						if (targetNodeRef != null) {
							targetRefs.add(targetNodeRef);
						}
					}
				}
			} else {
				NodeRef targetNodeRef = findTargetNodeByValue(importContext, targetClass, value);
				if (targetNodeRef != null) {
					targetRefs.add(targetNodeRef);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("assoc, targetClass: " + targetClass + " - value: " + value + "- targetRefs: " + targetRefs);
		}

		return targetRefs;
	}

	/**
	 * Find the target node according to the property (no target type associated
	 * to a property of type nodeRef
	 *
	 * @param importContext
	 * @param attributeMapping
	 * @param propDef
	 * @param attributeMapping
	 * @param value
	 * @param properties
	 * @return
	 * @throws ImporterException
	 */
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef,
			AbstractAttributeMapping attributeMapping, String value, Map<QName, Serializable> properties) throws ImporterException {

		if (attributeMapping instanceof HierarchyMapping) {

			String path = importContext.getPath();
			if ((((HierarchyMapping) attributeMapping).getPath() != null) && !((HierarchyMapping) attributeMapping).getPath().isEmpty()) {
				path = ((HierarchyMapping) attributeMapping).getPath();
			}

			logger.debug("Case hierarchy mapping");
			NodeRef hierarchyNodeRef;
			if ((((HierarchyMapping) attributeMapping).getParentLevelColumn() != null)
					&& !((HierarchyMapping) attributeMapping).getParentLevelColumn().isEmpty()) {
				NodeRef parentHierachyNodeRef = (NodeRef) properties
						.get(QName.createQName(((HierarchyMapping) attributeMapping).getParentLevelColumn(), namespaceService));
				if (parentHierachyNodeRef != null) {
					hierarchyNodeRef = hierarchyService.getHierarchyByPath(path, parentHierachyNodeRef, value);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("No parent for column " + attributeMapping.getAttribute().getName() + " prop "
								+ ((HierarchyMapping) attributeMapping).getParentLevelColumn());
					}
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_ASSOC_TARGET, propDef.getName(), value));
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Look for hierarchy " + attributeMapping.getAttribute().getName() + ": " + value + " at path " + path);
				}
				hierarchyNodeRef = hierarchyService.getHierarchyByPath(path, null, value);
			}

			if (hierarchyNodeRef != null) {

				return hierarchyNodeRef;
			} else {
				logger.error("No hierarchy found in path " + importContext.getPath() + " with value " + value);
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_ASSOC_TARGET, propDef.getName(), value));
			}
		}

		QName targetClass = propDef.getDataType().getName();
		if ((attributeMapping instanceof AttributeMapping) && (((AttributeMapping) attributeMapping).getTargetClass() != null)) {
			targetClass = ((AttributeMapping) attributeMapping).getTargetClass();
		}
		return findTargetNodeByValue(importContext, targetClass, value);

	}

	/**
	 * find the node by value, according to : - nodeColumnKey, take the first -
	 * - code
	 *
	 * @param importContext
	 *            the import context
	 * @param type
	 *            the type
	 * @param value
	 *            the value
	 * @return the node ref
	 * @throws InvalidTargetNodeException
	 * @throws ImporterException
	 */
	protected NodeRef findTargetNodeByValue(ImportContext importContext, QName type, String value) throws ImporterException {

		NodeRef nodeRef = null;

		Map<QName, Serializable> properties = new HashMap<>();

		ClassMapping classMapping = importContext.getClassMappings().get(type);
		boolean doQuery = false;

		// look in the cache
		String key = String.format(CACHE_KEY, type, value);

		if (importContext.getCacheNodes().containsKey(key)) {
			nodeRef = importContext.getCacheNodes().get(key);
		} else {

			// nodeColumnKeys, take the first
			if ((classMapping != null) && (classMapping.getNodeColumnKeys() != null) && !classMapping.getNodeColumnKeys().isEmpty()) {

				for (QName attribute : classMapping.getNodeColumnKeys()) {
					properties.put(attribute, value);
					doQuery = true;
					break;
				}
			} else {

				// look for codeAspect
				if ((dictionaryService.getType(type) != null) && (dictionaryService.getType(type).getDefaultAspects() != null)) {
					for (AspectDefinition aspectDef : dictionaryService.getType(type).getDefaultAspects()) {
						if (aspectDef.getName().equals(BeCPGModel.ASPECT_CODE)) {
							properties.put(BeCPGModel.PROP_CODE, value);
							doQuery = true;
							break;
						}
					}
				}

				// we try with the name
				if (!doQuery) {
					properties.put(RemoteHelper.getPropName(type, dictionaryService), value);
					doQuery = true;
				}
			}

			if (doQuery) {

				nodeRef = findNodeByKeyOrCode(importContext, type, properties);

				if (nodeRef == null) {
					String typeTitle = type.toString();
					TypeDefinition typeDef = dictionaryService.getType(type);
					if ((typeDef != null) && (typeDef.getTitle(dictionaryService) != null) && !typeDef.getTitle(dictionaryService).isEmpty()) {
						typeTitle = typeDef.getTitle(dictionaryService);
					}

					logger.error(I18NUtil.getMessage(MSG_ERROR_TARGET_ASSOC_NOT_FOUND, typeTitle, value));
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_TARGET_ASSOC_NOT_FOUND, typeTitle, value));
				}
			}

			// add in the cache
			importContext.getCacheNodes().put(key, nodeRef);
		}

		return nodeRef;
	}

}
