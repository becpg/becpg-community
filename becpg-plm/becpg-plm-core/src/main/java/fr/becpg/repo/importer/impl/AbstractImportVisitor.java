/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.FormulaMapping;
import fr.becpg.config.mapping.HierarchyMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.extractor.RemoteHelper;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.importer.MappingLoader;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Abstract class used to import a node with its attributes and files.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AbstractImportVisitor implements ImportVisitor, ApplicationContextAware {

	/** Constant <code>CACHE_KEY="cKey%s-%s"</code> */
	protected static final String CACHE_KEY = "cKey%s-%s";

	private static final Log logger = LogFactory.getLog(AbstractImportVisitor.class);

	protected NodeService nodeService;

	protected EntityDictionaryService entityDictionaryService;

	protected RepoService repoService;

	protected ContentService contentService;

	protected MimetypeService mimetypeService;

	protected NamespaceService namespaceService;

	protected ApplicationContext applicationContext;

	protected EntityListDAO entityListDAO;

	protected AutoNumService autoNumService;

	protected HierarchyService hierarchyService;

	protected Repository repositoryHelper;

	protected AssociationService associationService;
	
	protected PermissionService permissionService;
	
	private SpelFormulaService formulaService;
	
	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param spelFormulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object
	 */
	public void setFormulaService(SpelFormulaService spelFormulaService) {
		this.formulaService = spelFormulaService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object.
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>mimetypeService</code>.</p>
	 *
	 * @param mimetypeService a {@link org.alfresco.service.cmr.repository.MimetypeService} object.
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>autoNumService</code>.</p>
	 *
	 * @param autoNumService a {@link fr.becpg.repo.entity.AutoNumService} object.
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	/**
	 * <p>Setter for the field <code>hierarchyService</code>.</p>
	 *
	 * @param hierarchyService a {@link fr.becpg.repo.hierarchy.HierarchyService} object.
	 */
	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	/**
	 * <p>Setter for the field <code>repositoryHelper</code>.</p>
	 *
	 * @param repositoryHelper a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	/**
	 * <p>Getter for the field <code>associationService</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public AssociationService getAssociationService() {
		return associationService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/** {@inheritDoc} */
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

			
			QName assocName = ContentModel.ASSOC_CHILDREN;
			if ((name != null) && (name.length() > 0)) {
				assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(PropertiesHelper.cleanName(name)));
			}

			if (importContext.getParentNodeRef() == null) {
				throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_NO_PARENT));
			}

			nodeRef = nodeService.createNode(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, assocName, importContext.getType(),
					ImportHelper.cleanProperties(properties)).getChildRef();
			
			if (logger.isDebugEnabled()) {
				logger.debug("create node ("+nodeRef+"). Type: " + importContext.getType() + " - Properties: " + properties);
			}
		} else if (importContext.isDoUpdate()) {

			if (!AccessStatus.ALLOWED.equals(permissionService.hasPermission(nodeRef, PermissionService.WRITE_CONTENT))) {
				throw new AccessDeniedException("permissions.err_access_denied");
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("update node. Properties: " + properties);
			}
			if(entityDictionaryService.isSubClass(importContext.getType(), nodeService.getType(nodeRef))) {
				nodeService.setType(nodeRef, importContext.getType());
			}

			for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
				if ((entry.getValue() != null) && ImportHelper.NULL_VALUE.equals(entry.getValue())) {
					nodeService.removeProperty(nodeRef, entry.getKey());
				} else {

					if ((entry.getValue() instanceof MLText)) {
						boolean mlAware = MLPropertyInterceptor.isMLAware();
						try {
							MLPropertyInterceptor.setMLAware(true);
							nodeService.setProperty(nodeRef, entry.getKey(),
									ImportHelper.mergeMLText((MLText) entry.getValue(), (MLText) nodeService.getProperty(nodeRef, entry.getKey())));
						} finally {
							MLPropertyInterceptor.setMLAware(mlAware);
						}
					} else if (ContentModel.PROP_CONTENT.equals(entry.getKey())) {
						
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)){
							oos.writeObject(entry.getValue());
							InputStream in = new ByteArrayInputStream(baos.toByteArray());
							
							String mimetype = mimetypeService.guessMimetype(entry.getValue() != null ? entry.getValue().toString() : null);
							ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
							Charset charset = charsetFinder.getCharset(in, mimetype);
							String encoding = charset.name();
							
							ContentWriter writer = contentService.getWriter(nodeRef, entry.getKey(), true);
							writer.setEncoding(encoding);
							writer.setMimetype(mimetype);
							writer.putContent(in);
						} catch (IOException e) {
							throw new ImporterException(e.getMessage());
						}

					} else {
						nodeService.setProperty(nodeRef, entry.getKey(), entry.getValue());
					}
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
		QName propName = RemoteHelper.getPropName(type, entityDictionaryService);
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
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param values a {@link java.util.List} object.
	 * @return a {@link java.util.Map} object.
	 * @throws java.text.ParseException if any.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
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
								if ((values.get(z_idx) != null) && !values.get(z_idx).isEmpty()) {
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
			ExpressionParser parser = formulaService.getSpelParser();
			StandardEvaluationContext context = formulaService.createSpelContext(this);

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
	/**
	 * <p>findCharact.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String findCharact(String type, String name) {
		NodeRef ret = findCharact(QName.createQName(type, namespaceService), BeCPGModel.PROP_CHARACT_NAME, name);
		if (ret == null) {
			logger.error("Cannot find (" + type + "," + name + ")");
			return null;
		}

		return ret.toString();
	}

	// DO NOT REMOVED USED in FORMULA
	/**
	 * <p>findNut.</p>
	 *
	 * @param nutCode a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String findNut(String nutCode) {
		NodeRef ret = findCharact(PLMModel.TYPE_NUT, GS1Model.PROP_NUTRIENT_TYPE_CODE, nutCode);
		if (ret == null) {
			logger.error("Cannot find nut (" + nutCode + ")");
			return null;
		}

		return ret.toString();
	}
	
	// DO NOT REMOVED USED in FORMULA
		/**
		 * <p>findIng.</p>
		 *
		 * @param ingName a {@link java.lang.String} object.
		 * @return a {@link java.lang.String} object.
		 */
		public String findIng(String ingName) {
			NodeRef ret = findCharact(PLMModel.TYPE_ING, BeCPGModel.PROP_CHARACT_NAME, ingName);
			if (ret == null) {
				logger.error("Cannot find ing (" + ingName + ")");
				return null;
			}

			return ret.toString();
		}

	// DO NOT REMOVED USED in FORMULA
	/**
	 * <p>findLabelClaim.</p>
	 *
	 * @param labelClaimCode a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String findLabelClaim(String labelClaimCode) {
		NodeRef ret = findCharact(PLMModel.TYPE_LABEL_CLAIM, PLMModel.PROP_LABEL_CLAIM_CODE, labelClaimCode);
		if (ret == null) {
			logger.error("Cannot find labelClaim (" + labelClaimCode + ")");
			return null;
		}

		return ret.toString();
	}

	// DO NOT REMOVED USED in FORMULA
	/**
	 * <p>findAllergen.</p>
	 *
	 * @param allergenCode a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String findAllergen(String allergenCode) {
		NodeRef ret = findCharact(PLMModel.TYPE_ALLERGEN, PLMModel.PROP_ALLERGEN_CODE, allergenCode);
		if (ret == null) {
			logger.error("Cannot find allergen (" + allergenCode + ")");
			return null;
		}

		return ret.toString();
	}

	// DO NOT REMOVED USED in FORMULA
	/**
	 * <p>findCharactByProperty.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @param property a {@link java.lang.String} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String findCharactByProperty(String type, String property, String name) {
		NodeRef ret = findCharact(QName.createQName(type, namespaceService), QName.createQName(property, namespaceService), name);
		if (ret == null) {
			logger.error("Cannot find (" + type + "," + name + ")");
			return null;
		}

		return ret.toString();
	}

	private NodeRef findCharact(QName type, QName property, String name) {
		return ImportHelper.findCharact(type, property, name, nodeService);
	}

	/**
	 * Import the associations of the node
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param values a {@link java.util.List} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	protected void importAssociations(ImportContext importContext, List<String> values, NodeRef nodeRef) throws ImporterException {

		for (int z_idx = 0; (z_idx < values.size()) && (z_idx < importContext.getColumns().size()); z_idx++) {

			AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);

			if (attributeMapping instanceof AttributeMapping) {
				ClassAttributeDefinition column = attributeMapping.getAttribute();

				if (column instanceof AssociationDefinition) {
					AssociationDefinition assocDef = (AssociationDefinition) column;
					String value = values.get(z_idx);

					if ((value != null) && !value.isEmpty() && !ImportHelper.NULL_VALUE.equals(value)) {
						QName targetClass = ((AttributeMapping) attributeMapping).getTargetClass();
						logger.debug("importAssociations targetClass" + targetClass);
						List<NodeRef> targetRefs = findTargetNodesByValue(importContext, assocDef.isTargetMany(),
								targetClass != null ? targetClass : assocDef.getTargetClass().getName(), value, assocDef.getName());

						// mandatory target not found
						if (targetRefs.isEmpty()) {
							throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_ASSOC_TARGET, assocDef.getName(), value));
						}

						associationService.update(nodeRef, assocDef.getName(), targetRefs);

					} else if ((value != null) && value.isEmpty()) {

						associationService.update(nodeRef, assocDef.getName(), new ArrayList<>());

					} else if (assocDef.isTargetMandatory()) {

						throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_ASSOC_TARGET, assocDef.getName(), value));
					}
				}
			}
		}
	}

	/**
	 * Import the files of the node
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param values a {@link java.util.List} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws java.text.ParseException if any.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
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
				NodeRef fileNodeRef = null;

				if ((value != null) && value.startsWith("reg:")) {
					File docsFolder = new File(importContext.getDocsBasePath());

					if ((importContext.getDocsBasePath() != null) && docsFolder.isDirectory()) {

						Collection<File> files = FileUtils.listFiles(docsFolder, null, true);

						for (File file : files) {
							String regexp = value.replace("reg:", "");
							if (Pattern.matches(regexp, file.getName())) {

								logger.debug(file.getName() + " match regexp " + regexp);

							 fileNodeRef = createFile(targetFolderNodeRef, file.getName(), file.getName());
								String mimetype = mimetypeService.guessMimetype(file.getName());
								try (FileInputStream in = new FileInputStream(file);) {

									ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
									Charset charset = charsetFinder.getCharset(in, mimetype);
									String encoding = charset.name();

									ContentWriter writer = contentService.getWriter(fileNodeRef, contentQName, true);
									writer.setMimetype(mimetype);
									writer.setEncoding(encoding);
									writer.putContent(in);
								} catch (IOException e) {
									throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_LOAD_FILE, value));

								}
							}
						}

					} else {
						throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_NO_DOCS_BASE_PATH_SET, value));
					}
				} else if ((value != null) && !value.isEmpty()) {

					// add file content
					if (fileMapping.getAttribute().getName().equals(ContentModel.PROP_CONTENT)) {

						if (value.contains(",")) {
							int count = 0;
							for (String fileNameValue : split(value)) {
								fileNodeRef = importFileContent(fileNameValue, targetFolderNodeRef, fixFileNameExtension(fileName, fileNameValue, count),
										fileMapping.getId() + (count > 0 ? "-" + count : ""));
								count++;
							}

						} else {

							fileNodeRef = importFileContent(value, targetFolderNodeRef, fixFileNameExtension(fileName, value, 0), fileMapping.getId());

						}
					}
					// manage only properties
					else if (fileMapping.getAttribute() instanceof PropertyDefinition) {

						
						if (value.contains(",")) {
							int count = 0;
							for (String fileNameValue : split(value)) {
								fileNodeRef = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, fixFileNameExtension(fileName, fileNameValue, count));
								count++;
								
								if (fileNodeRef != null) {
									
									PropertyDefinition propertyDefinition = (PropertyDefinition) fileMapping.getAttribute();
									
									nodeService.setProperty(fileNodeRef, propertyDefinition.getName(),
											ImportHelper.loadPropertyValue(importContext, values, z_idx));
								}
							}

						} else {

							if(fileNodeRef == null) {
								fileNodeRef = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, fixFileNameExtension(fileName, value, 0));
							}
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
	}

	private String fixFileNameExtension(String fileName, String value, int count) {
		String[] tokens = value.split("\\.(?=[^\\.]+$)");

		if (tokens.length > 1) {
			return fileName.split("\\.(?=[^\\.]+$)")[0] + (count > 0 ? "-" + count : "") + "." + tokens[1];
		}

		return fileName;

	}

	private NodeRef importFileContent(String value, NodeRef targetFolderNodeRef, String mappingFileName, String mappingId) throws ImporterException {
		InputStream in = null;
		try {
			if ((value.startsWith("classpath:") || value.startsWith("file:") || value.startsWith("url:") || value.startsWith("http:") || value.startsWith("https:")
					|| value.startsWith("ftp:"))) {

				try {
					Resource resource = applicationContext.getResource(value);
					in = resource.getInputStream();
				} catch (IOException e) {
					logger.error("No resource found in path " + value, e);
					throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_LOAD_FILE, value));
				}

			} else if (new File(value).exists()) {
				try {
					in = new FileInputStream(value);
				} catch (FileNotFoundException e) {
					throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_LOAD_FILE, value));
				}
			} else {
				logger.error(I18NUtil.getMessage(ImportHelper.MSG_ERROR_FILE_BAD_PREFIX, value));
				throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_FILE_BAD_PREFIX, value));
			}

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
				
				return fileNodeRef;

		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (final IOException ioe) {
				// ignore
			}
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

	/** {@inheritDoc} */
	@Override
	public ImportContext loadClassMapping(Object mapping, ImportContext importContext, MappingLoader mappingLoader) throws MappingException {

		return mappingLoader.loadClassMapping(mapping, importContext);
	}

	/** {@inheritDoc} */
	@Override
	public ImportContext loadMappingColumns(Element mappingElt, List<String> columns, ImportContext importContext) throws MappingException {

		ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());
		
		// check COLUMNS respects the mapping and the class attributes
		List<AbstractAttributeMapping> columnsAttributeMapping = new LinkedList<>();
		List<String> unknownColumns = new ArrayList<>();
		boolean isMLPropertyDef = false;
		for (String column : columns) {

			boolean isAttributeMapped = false;
			String columnId = column;

			if (classMapping != null) {
				// columns
				for (AbstractAttributeMapping attrMapping : classMapping.getColumns()) {
					if (attrMapping.getId().equals(columnId)) {
						logger.debug("Find matching attribute mapping columnId : " + columnId);
						columnsAttributeMapping.add(attrMapping);
						isAttributeMapped = true;
						if ((attrMapping instanceof AttributeMapping) && ((AttributeMapping) attrMapping).isMLText()) {
							isMLPropertyDef = true;
						}
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

				PropertyDefinition propertyDefinition = entityDictionaryService.getProperty(qName);

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
					AssociationDefinition assocDefinition = entityDictionaryService.getAssociation(qName);
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

			String error = I18NUtil.getMessage(ImportHelper.MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING, importContext.getType(), (classMapping != null),
					unknownColumns, mappedColumns);
			logger.error(error);
			throw new MappingException(error);
		}

		importContext.setColumns(columnsAttributeMapping);

		if (logger.isTraceEnabled()) {
			logger.trace("importContext.getColumns() " + columnsAttributeMapping.size() + " / "+ importContext.getColumns().size() + importContext.getColumns().toString());
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
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	protected NodeRef findNode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException {

		NodeRef nodeRef = findNodeByKeyOrCode(importContext, null, type, properties, null, true);

		if (nodeRef == null) {

			String name = (String) properties.get(ContentModel.PROP_NAME);
			if ((name != null) && !name.isEmpty()) {
				if (importContext.getParentNodeRef() == null) {
					throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_NO_PARENT));
				}
				// look in import folder
				nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, name);
			} else if (!entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_LINKED_VALUE)
					&& !entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_LIST_VALUE)
					&& !entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)) {

				throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_OR_CREATE_NODEREF));
			}
		}

		return nodeRef;
	}

	/**
	 * find the node by key properties, according to : - nodeColumnKey - code.
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param propDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param parentRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 * @param useContextPath a boolean
	 */
	protected NodeRef findNodeByKeyOrCode(ImportContext importContext, PropertyDefinition propDef, QName type, Map<QName, Serializable> properties,
			NodeRef parentRef, boolean useContextPath) throws ImporterException {

		NodeRef nodeRef = null;

		ClassMapping classMapping = importContext.getClassMappings().get(type);

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(type);

		boolean doQuery = false;
		boolean hasParent = false;

		// nodeColumnKeys
		if ((classMapping != null) && !classMapping.getNodeColumnKeys().isEmpty()) {
			
			Set<QName> nullAttributes = new HashSet<>();

			for (QName attribute : classMapping.getNodeColumnKeys()) {

				if (logger.isDebugEnabled()) {
					logger.debug("attribute: " + attribute + " value: " + properties.get(attribute));
				}

				if (ContentModel.ASSOC_CONTAINS.isMatch(attribute) || BeCPGModel.PROP_LV_VALUE.isMatch(attribute)  || BeCPGModel.PROP_LV_CODE.isMatch(attribute)
						|| BeCPGModel.PROP_LKV_VALUE.isMatch(attribute)) {

					if (parentRef == null && useContextPath) {
						// query by path
						NodeRef folderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
								AbstractBeCPGQueryBuilder.encodePath(importContext.getPath()));
						if(folderNodeRef == null) {
							logger.warn("No folder found for :"+importContext.getPath());
						} else if(!hasParent){
							queryBuilder.parent(folderNodeRef);
							hasParent = true;
						}
					}

					if (BeCPGModel.PROP_LV_VALUE.isMatch(attribute) || BeCPGModel.PROP_LKV_VALUE.isMatch(attribute) || BeCPGModel.PROP_LV_CODE.isMatch(attribute)) {
						if ( (properties.get(attribute) instanceof MLText)) {
							queryBuilder.andPropEquals(attribute, ((MLText) properties.get(attribute)).getDefaultValue());
						} else if (properties.get(attribute) != null) {
							queryBuilder.andPropEquals(attribute, properties.get(attribute).toString());
						} else {
							logger.debug("Value of NodeColumnKey " + attribute + " is null (or it is not a property).");
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
					nullAttributes.add(attribute);
				}
			}
			
			 if (!doQuery && !nullAttributes.isEmpty()) {
				logger.warn("Value of NodeColumnKeys " + nullAttributes.toString() + " is null (or it is not a property). For "+ type);
			}

		} else {
			logger.debug("No key is define for type: " + type);

			// look for codeAspect
			if ((entityDictionaryService.getType(type) != null) && (entityDictionaryService.getType(type).getDefaultAspects() != null)) {
				for (AspectDefinition aspectDef : entityDictionaryService.getType(type).getDefaultAspects()) {
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

			QName propName = RemoteHelper.getPropName(type, entityDictionaryService);

			// look by name
			if (!doQuery && !entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITY_V2) && (properties.get(propName) != null)) {

				String name = getName(type, properties);

				if (NodeRef.isNodeRef(name)) {
					return new NodeRef(name);
				} else {
					queryBuilder.andPropEquals(propName, name);
					// #3433 by default look by path or provide mapping
					if (ContentModel.PROP_NAME.equals(propName) && !((propDef != null) && BeCPGModel.PROP_PARENT_LEVEL.equals(propDef.getName()))) {
						queryBuilder.parent(importContext.getParentNodeRef());
						hasParent = true;
					}
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

			// #3433
			if (!hasParent && entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM) && (propDef != null)
					&& BeCPGModel.PROP_PARENT_LEVEL.equals(propDef.getName())) {
				queryBuilder.parent(importContext.getParentNodeRef());
			} else if (parentRef != null && ! hasParent) {
				queryBuilder.inParent(parentRef);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("findNodeByKeyOrCode: " + queryBuilder.toString());
			}
			
			for (NodeRef tmpNodeRef : queryBuilder.inDB().ftsLanguage().list()) {
				if (!nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
						&& !nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
					return tmpNodeRef;
				}
			}

		}

		return nodeRef;
	}

	/**
	 * find the nodes by value (multi-value or single value)
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param isTargetMany a boolean.
	 * @param targetClass a {@link org.alfresco.service.namespace.QName} object.
	 * @param value a {@link java.lang.String} object.
	 * @param assoc a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	protected List<NodeRef> findTargetNodesByValue(ImportContext importContext, boolean isTargetMany, QName targetClass, String value, QName assoc)
			throws ImporterException {

		List<NodeRef> targetRefs = new ArrayList<>();

		if (!value.isEmpty()) {

			if (isTargetMany) {
				String[] arrValue = split(value);

				for (String v : arrValue) {
					if (!v.isEmpty()) {
						NodeRef targetNodeRef = findTargetNodeByValue(importContext, null, targetClass, v, assoc);
						if (targetNodeRef != null) {
							targetRefs.add(targetNodeRef);
						}
					}
				}
			} else {
				NodeRef targetNodeRef = findTargetNodeByValue(importContext, null, targetClass, value, assoc);
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
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param propDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
	 * @param attributeMapping a {@link fr.becpg.config.mapping.AbstractAttributeMapping} object.
	 * @param value a {@link java.lang.String} object.
	 * @param properties a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef,
			AbstractAttributeMapping attributeMapping, String value, Map<QName, Serializable> properties) throws ImporterException {

		if (attributeMapping instanceof HierarchyMapping) {

			String path = importContext.getPath();
			if ((((HierarchyMapping) attributeMapping).getPath() != null) && !((HierarchyMapping) attributeMapping).getPath().isEmpty()) {
				path = ((HierarchyMapping) attributeMapping).getPath();
			}
			
			QName key = null;
			if( ((HierarchyMapping) attributeMapping).getKey()!=null) {
				key =  QName.createQName( ((HierarchyMapping) attributeMapping).getKey(), namespaceService);
			}

			logger.debug("Case hierarchy mapping");
			NodeRef hierarchyNodeRef;
			if ((((HierarchyMapping) attributeMapping).getParentLevelColumn() != null)
					&& !((HierarchyMapping) attributeMapping).getParentLevelColumn().isEmpty()) {
				NodeRef parentHierachyNodeRef = (NodeRef) properties
						.get(QName.createQName(((HierarchyMapping) attributeMapping).getParentLevelColumn(), namespaceService));
			
				if (parentHierachyNodeRef != null) {
					hierarchyNodeRef = hierarchyService.getHierarchyByPath(path, parentHierachyNodeRef, key , value);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("No parent for column " + attributeMapping.getId() + " prop "
								+ ((HierarchyMapping) attributeMapping).getParentLevelColumn());
					}
					throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_ASSOC_TARGET, propDef.getName(), value));
				}
			} else {
				
				hierarchyNodeRef = hierarchyService.getHierarchyByPath(path, null, key , value);
				
				if (logger.isDebugEnabled()) {
					logger.debug("Look for hierarchy " + value + " at path " + path+ " for attribute : "+ attributeMapping.getId()+" "+hierarchyNodeRef  );
				}
			}

			if (hierarchyNodeRef != null) {

				return hierarchyNodeRef;
			} else {
				logger.error("No hierarchy found in path " + importContext.getPath() + " with value " + value);
				throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_ASSOC_TARGET, propDef.getName(), value));
			}
		}

		QName targetClass = propDef.getDataType().getName();
		if ((attributeMapping instanceof AttributeMapping) && (((AttributeMapping) attributeMapping).getTargetClass() != null)) {
			targetClass = ((AttributeMapping) attributeMapping).getTargetClass();
		} else if ( BeCPGModel.PROP_PARENT_LEVEL.equals(propDef.getName())) {
			targetClass = importContext.getType();
		}
		return findTargetNodeByValue(importContext, propDef, targetClass, value, null);

	}

	/**
	 * find the node by value, according to : - nodeColumnKey, take the first -
	 * - code
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param propDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param value a {@link java.lang.String} object.
	 * @param assoc a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	protected NodeRef findTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef, QName type, String value, QName assoc)
			throws ImporterException {
		NodeRef nodeRef = null;
		NodeRef parentRef = null;
		String assocPath = null;

		Map<QName, Serializable> properties = new HashMap<>();

		ClassMapping classMapping = importContext.getClassMappings().get(type);
		boolean doQuery = false;

		if (classMapping != null) {
			if (assoc != null) {
				assocPath = classMapping.getPaths().get(assoc);
			} else if(propDef!=null) {
				assocPath = classMapping.getPaths().get(propDef.getName());
			}
		}
		
		// look in the cache
		String key = String.format(CACHE_KEY, type, value);
		key = StringUtils.isEmpty(assocPath) ? key : key + assocPath;

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
				if ((entityDictionaryService.getType(type) != null) && (entityDictionaryService.getType(type).getDefaultAspects() != null)) {
					for (AspectDefinition aspectDef : entityDictionaryService.getType(type).getDefaultAspects()) {
						if (aspectDef.getName().equals(BeCPGModel.ASPECT_CODE)) {
							properties.put(BeCPGModel.PROP_CODE, value);
							doQuery = true;
							break;
						}
					}
				}

				// we try with the name
				if (!doQuery) {
					properties.put(RemoteHelper.getPropName(type, entityDictionaryService), value);
					doQuery = true;
				}
			}

			if (doQuery) {

				if ((classMapping != null) && StringUtils.isNotEmpty(assocPath)) {
					parentRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
							AbstractBeCPGQueryBuilder.encodePath(assocPath));
				}

				nodeRef = findNodeByKeyOrCode(importContext, propDef, type, properties, parentRef, false);

				if (nodeRef == null) {
					String typeTitle = type.toString();
					TypeDefinition typeDef = entityDictionaryService.getType(type);
					if ((typeDef != null) && (typeDef.getTitle(entityDictionaryService) != null)
							&& !typeDef.getTitle(entityDictionaryService).isEmpty()) {
						typeTitle = typeDef.getTitle(entityDictionaryService);
					}

					logger.error(I18NUtil.getMessage(ImportHelper.MSG_ERROR_TARGET_ASSOC_NOT_FOUND, typeTitle, value));
					throw new ImporterException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_TARGET_ASSOC_NOT_FOUND, typeTitle, value));
				}
			}

			// add in the cache
			importContext.getCacheNodes().put(key, nodeRef);
		}

		return nodeRef;
	}

}
