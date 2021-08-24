/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.report.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@Service
public class DefaultEntityReportExtractor implements EntityReportExtractorPlugin {

	private static final Log logger = LogFactory.getLog(DefaultEntityReportExtractor.class);

	protected static final String TAG_ENTITY = "entity";

	protected static final String TAG_DATALISTS = "dataLists";
	protected static final String TAG_ATTRIBUTES = "attributes";
	protected static final String TAG_ATTRIBUTE = "attribute";
	protected static final String TAG_VERSIONS = "versions";
	protected static final String TAG_VERSION = "version";
	protected static final String ATTR_SET = "set";
	protected static final String ATTR_NAME = "name";
	protected static final String ATTR_VALUE = "value";
	protected static final String ATTR_ITEM_TYPE = "itemType";
	protected static final String ATTR_ASPECTS = "aspects";
	protected static final String TAG_IMAGES = "images";
	protected static final String TAG_IMAGE = "image";
	protected static final String ATTR_ENTITY_NODEREF = "entityNodeRef";
	protected static final String ATTR_ENTITY_TYPE = "entityType";
	protected static final String PRODUCT_IMG_ID = "Img%d";
	protected static final String ATTR_IMAGE_ID = "id";
	protected static final String AVATAR_IMG_ID = "avatar";
	protected static final String REPORT_LOGO_ID = "report_logo";
	private static final String TAG_COMMENTS = "comments";
	private static final String TAG_COMMENT = "comment";

	protected static final String VALUE_NULL = "";

	private static final String REGEX_REMOVE_CHAR = "[^\\p{L}\\p{N}]";

	protected static final ArrayList<QName> hiddenNodeAttributes = new ArrayList<>(Arrays.asList(ContentModel.PROP_NODE_REF,
			ContentModel.PROP_NODE_UUID, ContentModel.PROP_STORE_IDENTIFIER, ContentModel.PROP_STORE_NAME, ContentModel.PROP_STORE_PROTOCOL,
			ContentModel.PROP_CONTENT, BeCPGModel.PROP_ENTITY_SCORE, ContentModel.PROP_PREFERENCE_VALUES, ContentModel.PROP_PERSONDESC));

	protected static final ArrayList<QName> hiddenDataListItemAttributes = new ArrayList<>(
			Arrays.asList(ContentModel.PROP_CREATED, ContentModel.PROP_CREATOR, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER));
	private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");

	@Value("${beCPG.entity.report.mltext.fields}")
	private String mlTextFields;

	@Value("${beCPG.product.report.assocsToExtractWithDataList}")
	protected String assocsToExtractWithDataList = "";

	@Value("${beCPG.product.report.assocsToExtractWithImage}")
	protected String assocsToExtractWithImage = "";

	@Value("${beCPG.product.report.assocsToExtract}")
	protected String assocsToExtract = "";

	@Value("${beCPG.product.report.assocsToExtractInDataList}")
	protected String assocsToExtractInDataList = "";

	@Value("${beCPG.product.report.multilineProperties}")
	protected String multilineProperties = "";

	@Autowired
	protected DictionaryService dictionaryService;

	@Autowired
	protected EntityDictionaryService entityDictionaryService;

	@Autowired
	protected NamespaceService namespaceService;

	@Autowired
	protected AttributeExtractorService attributeExtractorService;

	@Autowired
	protected NodeService nodeService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;

	@Autowired
	protected EntityService entityService;

	@Autowired
	protected VersionService versionService;

	@Autowired
	protected FileFolderService fileFolderService;

	@Autowired
	protected AssociationService associationService;

	@Autowired
	protected PersonService personService;

	@Autowired
	protected RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;

	@Autowired
	protected ContentService contentService;

	@Autowired
	protected AlfrescoRepository<BeCPGDataObject> alfrescoRepository;

	@Autowired
	protected EntityListDAO entityListDAO;

	@Autowired
	protected EntityReportService entityReportService;

	protected interface DefaultExtractorContextCallBack {
		public void run();
	}

	public class DefaultExtractorContext {

		boolean isInDataListContext = false;

		Map<String, String> preferences;
		Map<String, byte[]> images = new HashMap<>();
		Set<NodeRef> extractedNodes = new HashSet<>();

		public DefaultExtractorContext(Map<String, String> preferences) {
			super();
			this.preferences = preferences;
		}

		public Map<String, byte[]> getImages() {
			return images;
		}

		public Map<String, String> getPreferences() {
			return preferences;
		}

		public Set<NodeRef> getExtractedNodes() {
			return extractedNodes;
		}

		public boolean prefsContains(String key, String defaultValue, String query) {
			if (((defaultValue != null) && defaultValue.contains(query)) || (preferences.containsKey(key) && preferences.get(key).contains(query))) {
				return true;
			}

			return false;
		}

		public boolean multiPrefsEquals(String key, String defaultValue, String query) {
			if (((defaultValue != null) && Arrays.asList(defaultValue.split(",")).contains(query)) || (preferences.containsKey(key) && Arrays.asList(preferences.get(key).split(",")).contains(query))) {
				return true;
			}

			return false;
		}

		public boolean isPrefOn(String key, Boolean defaultValue) {
			if (Boolean.TRUE.equals(defaultValue) || (preferences.containsKey(key) && "true".equalsIgnoreCase(preferences.get(key)))) {
				return true;
			}

			return false;
		}

		public String getPrefValue(String key, String defaultValue) {

			if (preferences.containsKey(key)) {
				return preferences.get(key);
			}

			return defaultValue;
		}

		public boolean isNotEmptyPrefs(String key, String defaultValue) {
			if (((defaultValue != null) && !defaultValue.isEmpty()) || (preferences.containsKey(key) && !preferences.get(key).isEmpty())) {
				return true;
			}

			return false;
		}

		public boolean isInDataListContext() {
			return isInDataListContext;
		}

		public void doInDataListContext(DefaultExtractorContextCallBack callBack) {
			try {
				isInDataListContext = true;
				callBack.run();
			} finally {
				isInDataListContext = false;
			}

		}

	}

	@Override
	public EntityReportData extract(NodeRef entityNodeRef, Map<String, String> preferences) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		EntityReportData ret = new EntityReportData();

		DefaultExtractorContext context = new DefaultExtractorContext(preferences);

		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_ENTITY);

		extractEntity(entityNodeRef, entityElt, context);

		ret.setXmlDataSource(entityElt);
		ret.setDataObjects(context.getImages());

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("extract datasource in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
		}

		return ret;
	}

	public void extractEntity(NodeRef entityNodeRef, Element entityElt, DefaultExtractorContext context) {

		// load images
		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		extractEntityImages(entityNodeRef, imgsElt, context);

		// add attributes at <product/> tag
		loadNodeAttributes(entityNodeRef, entityElt, true, context);

		Element aspectsElt = entityElt.addElement(ATTR_ASPECTS);
		aspectsElt.addCDATA(extractAspects(entityNodeRef));

		Element itemTypeElt = entityElt.addElement(ATTR_ITEM_TYPE);
		itemTypeElt.addCDATA(nodeService.getType(entityNodeRef).toPrefixString(namespaceService));

		loadCreator(entityNodeRef, entityElt, imgsElt, context);

		// render data lists
		Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
		loadDataLists(entityNodeRef, dataListsElt, context);

		// render versions
		loadVersions(entityNodeRef, entityElt);
	}

	/**
	 * <p>extractEntityImages.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param imgsElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
	protected void extractEntityImages(NodeRef entityNodeRef, Element imgsElt, DefaultExtractorContext context) {

		int cnt = imgsElt.selectNodes(TAG_IMAGE) != null ? imgsElt.selectNodes(TAG_IMAGE).size() : 1;
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			for (FileInfo fileInfo : fileFolderService.listFiles(imagesFolderNodeRef)) {

				String imgId = String.format(PRODUCT_IMG_ID, cnt);

				if (fileInfo.getName().startsWith(REPORT_LOGO_ID)
						|| fileInfo.getName().startsWith(I18NUtil.getMessage("report.logo.fileName.prefix", Locale.getDefault()))) {
					imgId = REPORT_LOGO_ID;
				}

				extractImage(entityNodeRef, fileInfo.getNodeRef(), imgId, imgsElt, context);
				cnt++;
			}
		}
	}

	protected void extractImage(NodeRef entityNodeRef, NodeRef imgNodeRef, String imgId, Element imgsElt, DefaultExtractorContext context) {

		if (ApplicationModel.TYPE_FILELINK.equals(nodeService.getType(imgNodeRef))) {
			imgNodeRef = (NodeRef) nodeService.getProperty(imgNodeRef, ContentModel.PROP_LINK_DESTINATION);
		}

		byte[] imageBytes = entityService.getImage(imgNodeRef);
		if (imageBytes != null) {
			Element imgElt = imgsElt.addElement(TAG_IMAGE);
			imgElt.addAttribute(ATTR_ENTITY_NODEREF, entityNodeRef.toString());
			imgElt.addAttribute(ATTR_ENTITY_TYPE, nodeService.getType(entityNodeRef).getLocalName());
			imgElt.addAttribute(ATTR_IMAGE_ID, imgId);
			imgElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_NAME));
			imgElt.addAttribute(ContentModel.PROP_TITLE.getLocalName(), (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));
			addCDATA(imgElt, ContentModel.PROP_DESCRIPTION, (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_DESCRIPTION), null);
			context.getImages().put(imgId, imageBytes);
		}
	}

	// render target assocs (plants...special cases)
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, DefaultExtractorContext context) {
		boolean isExtracted = false;
		if ((assocDef != null) && (assocDef.getName() != null)) {
			boolean extractDataList = false;
			boolean extractAssoc = false;
			if (context.prefsContains("assocsToExtractWithDataList", assocsToExtractWithDataList,
					assocDef.getName().toPrefixString(namespaceService))) {
				extractDataList = true;
			}

			if (context.isInDataListContext()) {
				if (context.prefsContains("assocsToExtractInDataList", assocsToExtractInDataList,
						assocDef.getName().toPrefixString(namespaceService))) {
					extractAssoc = true;
				}
			} else {
				if (context.prefsContains("assocsToExtract", assocsToExtract, assocDef.getName().toPrefixString(namespaceService))) {
					extractAssoc = true;
				}
			}

			if (extractAssoc || extractDataList) {
				Element assocElt = entityElt.addElement(assocDef.getName().getLocalName());
				appendPrefix(assocDef.getName(), assocElt);
				extractTargetAssoc(entityNodeRef, assocDef, assocElt, context, extractDataList);
				isExtracted = true;
			}

			if (context.prefsContains("assocsToExtractWithImage", assocsToExtractWithImage, assocDef.getName().toPrefixString(namespaceService))) {
				List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());
				Element imgsElt = (Element) entityElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
				int cnt = imgsElt.selectNodes(TAG_IMAGE) != null ? imgsElt.selectNodes(TAG_IMAGE).size() : 1;

				for (NodeRef nodeRef : nodeRefs) {
					if (entityDictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						extractImage(nodeRef, nodeRef, assocDef.getName().getLocalName() + "_" + cnt, imgsElt, context);
					} else {
						extractEntityImages(nodeRef, imgsElt, context);
					}
					cnt++;
				}
			}
		}
		return isExtracted;
	}

	protected boolean isMultiLinesAttribute(QName attribute, DefaultExtractorContext context) {
		return context.prefsContains("multilineProperties", multilineProperties, attribute.toPrefixString());
	}

	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			List<NodeRef> listNodeRefs = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);

			for (NodeRef listNodeRef : listNodeRefs) {
				QName dataListQName = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
						namespaceService);

				Class<RepositoryEntity> entityClass = repositoryEntityDefReader.getEntityClass(dataListQName);
				if (entityClass != null) {
					List<BeCPGDataObject> dataListItems = alfrescoRepository.loadDataList(entityNodeRef, dataListQName, dataListQName);

					if ((dataListItems != null) && !dataListItems.isEmpty()) {
						Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");

						for (BeCPGDataObject dataListItem : dataListItems) {

							addDataListState(dataListElt, dataListItem.getParentNodeRef());
							Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
							loadDataListItemAttributes(dataListItem, nodeElt, context);
						}
					}
				} else {
					loadDataList(dataListsElt, listNodeRef, dataListQName, context);
				}
			}
		}
	}

	protected void addDataListState(Element xmlNode, NodeRef listNodeRef) {
		if (xmlNode.attributeValue(BeCPGModel.PROP_ENTITYLIST_STATE.getLocalName()) == null) {
			if ((listNodeRef != null) && nodeService.exists(listNodeRef)) {
				Serializable state = nodeService.getProperty(listNodeRef, BeCPGModel.PROP_ENTITYLIST_STATE);
				if (state != null) {
					xmlNode.addAttribute(BeCPGModel.PROP_ENTITYLIST_STATE.getLocalName(), (String) state);
				} else {
					xmlNode.addAttribute(BeCPGModel.PROP_ENTITYLIST_STATE.getLocalName(), SystemState.ToValidate.toString());
				}

			}
		}
	}

	protected void loadDataList(Element dataListsElt, NodeRef listNodeRef, QName dataListQName, DefaultExtractorContext context) {
		List<NodeRef> dataListItems = entityListDAO.getListItems(listNodeRef, dataListQName);
		if ((dataListItems != null) && !dataListItems.isEmpty()) {
			Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");

			for (NodeRef dataListItem : dataListItems) {

				addDataListState(dataListElt, dataListItem);
				Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
				loadDataListItemAttributes(dataListItem, nodeElt, context);
			}
		}
	}

	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt, DefaultExtractorContext context) {
		loadDataListItemAttributes(dataListItem, nodeElt, context, new ArrayList<>());
	}

	protected void loadNodeAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, DefaultExtractorContext context) {
		if ((nodeRef != null) && nodeService.exists(nodeRef)) {
			loadAttributes(nodeRef, nodeElt, useCData, hiddenNodeAttributes, context);
			loadComments(nodeRef, nodeElt, context);
		}
	}

	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt, DefaultExtractorContext context,
			List<QName> hiddentAttributes) {
		hiddentAttributes.addAll(hiddenNodeAttributes);
		hiddentAttributes.addAll(hiddenDataListItemAttributes);

		if ((dataListItem.getNodeRef() != null) && nodeService.exists(dataListItem.getNodeRef())) {
			loadAttributes(dataListItem.getNodeRef(), nodeElt, false, hiddentAttributes, context);

			// look for charact
			Map<QName, Serializable> identAttr = repositoryEntityDefReader.getIdentifierAttributes(dataListItem);
			for (Map.Entry<QName, Serializable> kv : identAttr.entrySet()) {
				if ((kv.getValue() instanceof NodeRef) && nodeService.hasAspect((NodeRef) kv.getValue(), BeCPGModel.ASPECT_LEGAL_NAME)) {
					nodeElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(),
							(String) nodeService.getProperty((NodeRef) kv.getValue(), BeCPGModel.PROP_LEGAL_NAME));
					addCDATA(nodeElt, ContentModel.PROP_DESCRIPTION,
							(String) nodeService.getProperty((NodeRef) kv.getValue(), ContentModel.PROP_DESCRIPTION), null);
					break;
				}
			}
			loadComments(dataListItem.getNodeRef(), nodeElt, context);
		}
	}

	protected void loadDataListItemAttributes(NodeRef nodeRef, Element nodeElt, DefaultExtractorContext context) {
		List<QName> hiddentAttributes = new ArrayList<>();
		hiddentAttributes.addAll(hiddenNodeAttributes);
		hiddentAttributes.addAll(hiddenDataListItemAttributes);

		if ((nodeRef != null) && nodeService.exists(nodeRef)) {
			loadAttributes(nodeRef, nodeElt, false, hiddentAttributes, context);
			loadComments(nodeRef, nodeElt, context);
		}
	}

	/**
	 * Load node attributes.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param elt
	 *            the elt
	 * @return the element
	 */
	protected void loadAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, List<QName> hiddenAttributes, DefaultExtractorContext context) {

		PropertyFormats propertyFormats = new PropertyFormats(false);

		// properties
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

			// do not display system properties
			if ((hiddenAttributes == null) || (!hiddenAttributes.contains(property.getKey())
					&& !NamespaceService.SYSTEM_MODEL_1_0_URI.equals(property.getKey().getNamespaceURI()))) {

				PropertyDefinition propertyDef = dictionaryService.getProperty(property.getKey());
				if (propertyDef == null) {
					logger.debug("This property doesn't exist. Name: " + property.getKey() + " nodeRef : " + nodeRef);
					continue;
				}

				String value = attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), propertyFormats, false);

				boolean isDyn = false;
				boolean isList = false;

				DynListConstraint dynListConstraint = null;

				if (DataTypeDefinition.TEXT.toString().equals(propertyDef.getDataType().toString())) {

					if (!propertyDef.getConstraints().isEmpty()) {
						for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
							if (constraint.getConstraint() instanceof DynListConstraint) {
								isDyn = true;
								dynListConstraint = (DynListConstraint) constraint.getConstraint();
								break;
							} else if ("LIST".equals(constraint.getConstraint().getType())) {
								isList = true;
								break;
							}
						}
					}
				}

				if (isDyn || isList) {
					String displayValue = attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), propertyFormats, true);
					if (useCData) {
						if (isList) {
							Element ret = addData(nodeElt, true, propertyDef.getName(), value, null, context);
							if (ret != null) {
								ret.addAttribute("translation", displayValue);
							}
						} else {
							Element ret = addData(nodeElt, true, propertyDef.getName(), displayValue, null, context);
							if (ret != null) {
								ret.addAttribute("code", value);
							}
						}
					} else {
						if (isList) {
							Element ret = addData(nodeElt, false, propertyDef.getName(), value, null, context);
							if (ret != null) {
								ret.addAttribute(propertyDef.getName().getLocalName() + "Translation", displayValue);
							}
						} else {
							Element ret = addData(nodeElt, false, propertyDef.getName(), displayValue, null, context);
							if (ret != null) {
								ret.addAttribute(propertyDef.getName().getLocalName() + "Code", value);
							}
						}
					}
				} else {
					addData(nodeElt, useCData, propertyDef.getName(), value, null, context);
				}

				if (context.prefsContains("mlTextFields", mlTextFields, propertyDef.getName().toPrefixString(namespaceService))) {

					MLText mlValues = null;

					if (DataTypeDefinition.MLTEXT.equals(propertyDef.getDataType().getName())) {
						mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDef.getName());

					} else if (DataTypeDefinition.TEXT.equals(propertyDef.getDataType().getName())) {
						if (dynListConstraint != null) {
							mlValues = dynListConstraint.getMLAwareAllowedValues().get(property.getValue());
						}
					}

					if (mlValues != null) {
						for (Map.Entry<Locale, String> mlEntry : mlValues.entrySet()) {
							String code = mlEntry.getKey().getLanguage();
							if ((mlEntry.getKey().getCountry() != null) && !mlEntry.getKey().getCountry().isEmpty()) {
								code += "_" + mlEntry.getKey().getCountry();
							}
							if ((code != null) && !code.isEmpty()) {
								Element ret = addData(nodeElt, useCData, propertyDef.getName(), mlEntry.getValue(), code, context);
								if (isDyn && (ret != null)) {
									if (useCData) {
										ret.addAttribute("code", value);
									}
								}

							}
						}
					}

				}

			}
		}

		Map<QName, AssociationDefinition> assocs = new HashMap<>(dictionaryService.getType(nodeService.getType(nodeRef)).getAssociations());
		for (QName aspect : nodeService.getAspects(nodeRef)) {
			if (dictionaryService.getAspect(aspect) != null) {
				assocs.putAll(dictionaryService.getAspect(aspect).getAssociations());
			} else {
				logger.warn("No definition for :" + aspect);
			}
		}

		for (AssociationDefinition associationDef : assocs.values()) {
			if (!associationDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
					&& !associationDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
					&& !associationDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER) && !associationDef.getName().equals(ContentModel.ASSOC_ORIGINAL)
					&& !associationDef.isChild()) {

				if (!loadTargetAssoc(nodeRef, associationDef, nodeElt, context) || !useCData) {

					List<NodeRef> assocNodes = associationService.getTargetAssocs(nodeRef, associationDef.getName());

					if ((assocNodes != null) && !assocNodes.isEmpty()) {
						String ret = assocNodes.stream().map(i -> extractName(associationDef.getTargetClass().getName(), i))
								.collect(Collectors.joining(RepoConsts.LABEL_SEPARATOR));
						addData(nodeElt, useCData, associationDef.getName(), ret, null, context);
					}

				}
			}
		}

	}

	private String extractName(QName targetClass, NodeRef nodeRef) {

		if (nodeService.exists(nodeRef)) {

			QName propNameOfType = getPropNameOfType(targetClass);

			if (propNameOfType != null) {
				return (String) nodeService.getProperty(nodeRef, propNameOfType);
			}

			if (ContentModel.TYPE_CMOBJECT.equals(targetClass)
					&& entityDictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_CHARACT)) {
				String name = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_LEGAL_NAME);

				if ((name == null) || name.isEmpty()) {
					name = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME);
				}
				return name;
			}

			return attributeExtractorService.extractPropName(targetClass, nodeRef);

		} else {
			logger.warn("Node doesn't exists : " + targetClass + "  " + nodeRef);
			return "#Error";
		}
	}

	private void loadComments(NodeRef nodeRef, Element nodeElt, DefaultExtractorContext context) {
		if (nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE)) {
			List<NodeRef> assocs = associationService.getChildAssocs(nodeRef, ForumModel.ASSOC_DISCUSSION);
			if (!assocs.isEmpty()) {
				NodeRef forumFolder = assocs.get(0);
				List<ChildAssociationRef> topics = nodeService.getChildAssocs(forumFolder, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME,
						true);
				if (!topics.isEmpty()) {
					NodeRef firstTopicNodeRef = topics.get(0).getChildRef();
					List<ChildAssociationRef> posts = nodeService.getChildAssocs(firstTopicNodeRef);
					if (!posts.isEmpty()) {
						Element commentsElt = nodeElt.addElement(TAG_COMMENTS);
						for (ChildAssociationRef post : posts) {
							Element commentElt = commentsElt.addElement(TAG_COMMENT);
							loadAttributes(post.getChildRef(), commentElt, true, hiddenNodeAttributes, context);
							ContentReader reader = contentService.getReader(post.getChildRef(), ContentModel.PROP_CONTENT);
							if (reader != null) {
								addData(commentElt, true, ContentModel.PROP_CONTENT, reader.getContentString(), null, context);
							}
						}
					}
				}
			}
		}

	}

	protected Element addData(Element nodeElt, boolean useCData, QName propertyQName, String value, String suffix, DefaultExtractorContext context) {
		if (useCData || isMultiLinesAttribute(propertyQName, context)) {
			return addCDATA(nodeElt, propertyQName, value, suffix);
		} else {
			String localName = propertyQName.getLocalName();
			if ((suffix != null) && !suffix.isEmpty()) {
				localName += "_" + suffix;
			}
			nodeElt.addAttribute(localName, value);
			return nodeElt;
		}
	}

	protected QName getPropNameOfType(QName type) {
		return null;
	}

	protected String generateKeyAttribute(String attributeName) {

		return StringUtils.stripAccents(attributeName.replaceAll(REGEX_REMOVE_CHAR, "").toLowerCase());
	}

	protected void loadVersions(NodeRef entityNodeRef, Element entityElt) {

		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
		Element versionsElt = entityElt.addElement(TAG_VERSIONS);

		if ((versionHistory != null) && (versionHistory.getAllVersions() != null)) {

			for (Version version : versionHistory.getAllVersions()) {
				Element versionElt = versionsElt.addElement(TAG_VERSION);
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_LABEL.getLocalName(), version.getVersionLabel());
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_DESCRIPTION.getLocalName(), version.getDescription());
				versionElt.addAttribute(ContentModel.PROP_CREATOR.getLocalName(),
						attributeExtractorService.getPersonDisplayName(version.getFrozenModifier()));
				versionElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(), ISO8601DateFormat.format(version.getFrozenModifiedDate()));

			}
		}
	}

	protected String extractNames(List<NodeRef> nodeRefs) {
		String value = VALUE_NULL;
		for (NodeRef nodeRef : nodeRefs) {
			if (!value.isEmpty()) {
				value += RepoConsts.LABEL_SEPARATOR;
			}
			value += (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		}
		return value;
	}

	protected String extractAspects(NodeRef nodeRef) {
		String value = VALUE_NULL;
		for (QName aspect : nodeService.getAspects(nodeRef)) {
			if (!value.isEmpty()) {
				value += RepoConsts.LABEL_SEPARATOR;
			}
			value += aspect.toPrefixString(namespaceService);
		}
		return value;
	}

	/**
	 * Extract target(s) association
	 */
	protected void extractTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element assocElt, DefaultExtractorContext context,
			boolean extractDataList) {

		List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());

		for (NodeRef nodeRef : nodeRefs) {
			if (!context.getExtractedNodes().contains(nodeRef)) {

				context.getExtractedNodes().add(nodeRef);
				QName qName = nodeService.getType(nodeRef);

				Element nodeElt = assocElt.addElement(qName.getLocalName());

				appendPrefix(qName, nodeElt);

				EntityReportExtractorPlugin extractor = entityReportService.retrieveExtractor(nodeRef);
				if (extractDataList && (extractor != null) && (extractor instanceof DefaultEntityReportExtractor)) {
					((DefaultEntityReportExtractor) extractor).extractEntity(nodeRef, nodeElt, context);
				} else {

					if (entityDictionaryService.isSubClass(qName, BeCPGModel.TYPE_CHARACT)) {
						List<QName> hiddentAttributes = new ArrayList<>();
						hiddentAttributes.addAll(hiddenNodeAttributes);
						hiddentAttributes.addAll(hiddenDataListItemAttributes);

						loadAttributes(nodeRef, nodeElt, true, hiddentAttributes, context);
					} else {
						loadNodeAttributes(nodeRef, nodeElt, true, context);
					}
					if (extractDataList) {

						Element dataListsElt = nodeElt.addElement(TAG_DATALISTS);
						loadDataLists(nodeRef, dataListsElt, new DefaultExtractorContext(context.getPreferences()));
					}
				}

				context.getExtractedNodes().remove(nodeRef);
			}
		}
	}

	protected Element addCDATA(Element nodeElt, QName propertyQName, String eltValue, String suffix) {
		if ((eltValue == null) || eltValue.isEmpty()) {
			return null;
		}

		String localName = propertyQName.getLocalName();
		if ((suffix != null) && !suffix.isEmpty()) {
			localName += "_" + suffix;
		}
		Element cDATAElt = nodeElt.addElement(localName);
		appendPrefix(propertyQName, cDATAElt);

		cDATAElt.addCDATA(eltValue);

		return cDATAElt;

	}

	protected void appendPrefix(QName propertyQName, Element cDATAElt) {
		Collection<String> prefixes = namespaceService.getPrefixes(propertyQName.getNamespaceURI());
		if (!prefixes.isEmpty()) {
			// TODO : manage prefix correctly
			cDATAElt.addAttribute("prefix", prefixes.iterator().next());
		}

	}

	// Check that images has not been update
	@Override
	public boolean shouldGenerateReport(NodeRef entityNodeRef, Date generatedReportDate) {
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			Date modified = (Date) nodeService.getProperty(imagesFolderNodeRef, ContentModel.PROP_MODIFIED);
			if ((modified == null) || (generatedReportDate == null) || (modified.getTime() > generatedReportDate.getTime())) {
				return true;
			}
			for (FileInfo fileInfo : fileFolderService.listFiles(imagesFolderNodeRef)) {
				modified = fileInfo.getModifiedDate();
				if ((modified == null) || (generatedReportDate == null) || (modified.getTime() > generatedReportDate.getTime())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return EntityReportExtractorPriority.LOW;
	}

	private void loadCreator(NodeRef entityNodeRef, Element entityElt, Element imgsElt, DefaultExtractorContext context) {
		String creator = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATOR);
		if ((creator != null) && personService.personExists(creator)) {
			Element creatorElt = (Element) entityElt.selectSingleNode(ContentModel.PROP_CREATOR.getLocalName());
			NodeRef creatorNodeRef = personService.getPerson(creator);
			loadNodeAttributes(creatorNodeRef, creatorElt, true, context);
			// extract avatar
			List<AssociationRef> avatorAssocs = nodeService.getTargetAssocs(creatorNodeRef, ContentModel.ASSOC_AVATAR);
			if (!avatorAssocs.isEmpty()) {
				extractImage(creatorNodeRef, avatorAssocs.get(0).getTargetRef(), AVATAR_IMG_ID, imgsElt, context);
			}
		}
	}
}
