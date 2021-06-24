/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
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
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityImageInfo;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>DefaultEntityReportExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DefaultEntityReportExtractor implements EntityReportExtractorPlugin {

	private static final Log logger = LogFactory.getLog(DefaultEntityReportExtractor.class);

	/** Constant <code>TAG_ENTITY="entity"</code> */
	protected static final String TAG_ENTITY = "entity";

	/** Constant <code>TAG_DATALISTS="dataLists"</code> */
	protected static final String TAG_DATALISTS = "dataLists";
	/** Constant <code>TAG_ATTRIBUTES="attributes"</code> */
	protected static final String TAG_ATTRIBUTES = "attributes";
	/** Constant <code>TAG_ATTRIBUTE="attribute"</code> */
	protected static final String TAG_ATTRIBUTE = "attribute";
	/** Constant <code>TAG_VERSIONS="versions"</code> */
	protected static final String TAG_VERSIONS = "versions";
	/** Constant <code>TAG_VERSION="version"</code> */
	protected static final String TAG_VERSION = "version";
	/** Constant <code>ATTR_SET="set"</code> */
	protected static final String ATTR_SET = "set";
	/** Constant <code>ATTR_NAME="name"</code> */
	protected static final String ATTR_NAME = "name";
	/** Constant <code>ATTR_VALUE="value"</code> */
	protected static final String ATTR_VALUE = "value";
	/** Constant <code>ATTR_ITEM_TYPE="itemType"</code> */
	protected static final String ATTR_ITEM_TYPE = "itemType";
	/** Constant <code>ATTR_ASPECTS="aspects"</code> */
	protected static final String ATTR_ASPECTS = "aspects";
	/** Constant <code>TAG_IMAGES="images"</code> */
	protected static final String TAG_IMAGES = "images";
	/** Constant <code>TAG_IMAGE="image"</code> */
	protected static final String TAG_IMAGE = "image";
	/** Constant <code>ATTR_ENTITY_NODEREF="entityNodeRef"</code> */
	protected static final String ATTR_ENTITY_NODEREF = "entityNodeRef";
	/** Constant <code>ATTR_ENTITY_TYPE="entityType"</code> */
	protected static final String ATTR_ENTITY_TYPE = "entityType";
	/** Constant <code>PRODUCT_IMG_ID="Img%d"</code> */
	protected static final String PRODUCT_IMG_ID = "Img%d";
	/** Constant <code>ATTR_IMAGE_ID="id"</code> */
	protected static final String ATTR_IMAGE_ID = "id";
	/** Constant <code>AVATAR_IMG_ID="avatar"</code> */
	protected static final String AVATAR_IMG_ID = "avatar";
	/** Constant <code>REPORT_LOGO_ID="report_logo"</code> */
	protected static final String REPORT_LOGO_ID = "report_logo";
	private static final String TAG_COMMENTS = "comments";
	private static final String TAG_COMMENT = "comment";
	private static final String ATTR_ENTITY_CODE ="entityCode";
	private static final String ATTR_ENTITY_NAME ="entityName";

	/** Constant <code>VALUE_NULL=""</code> */
	protected static final String VALUE_NULL = "";

	private static final String REGEX_REMOVE_CHAR = "[^\\p{L}\\p{N}]";

	/** Constant <code>hiddenNodeAttributes</code> */
	protected static final ArrayList<QName> hiddenNodeAttributes = new ArrayList<>(Arrays.asList(ContentModel.PROP_NODE_REF,
			ContentModel.PROP_NODE_UUID, ContentModel.PROP_STORE_IDENTIFIER, ContentModel.PROP_STORE_NAME, ContentModel.PROP_STORE_PROTOCOL,
			ContentModel.PROP_CONTENT, BeCPGModel.PROP_ENTITY_SCORE, ContentModel.PROP_PREFERENCE_VALUES, ContentModel.PROP_PERSONDESC));

	/** Constant <code>hiddenDataListItemAttributes</code> */
	protected static final ArrayList<QName> hiddenDataListItemAttributes = new ArrayList<>(
			Arrays.asList(ContentModel.PROP_CREATED, ContentModel.PROP_CREATOR, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER));
	private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");

	@Value("${beCPG.entity.report.mltext.fields}")
	private String mlTextFields;

	@Value("${beCPG.entity.report.mltext.locales}")
	private String mlTextLocales;

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
		Set<NodeRef> extractedNodes = new HashSet<>();

		EntityReportData reportData = new EntityReportData();
		
		public DefaultExtractorContext(Map<String, String> preferences) {
			super();
			this.preferences = preferences;
		}


		public Map<String, String> getPreferences() {
			return preferences;
		}

		public Set<NodeRef> getExtractedNodes() {
			return extractedNodes;
		}
		

		public EntityReportData getReportData() {
			return reportData;
		}

		public boolean prefsContains(String key, String defaultValue, String query) {
			if ((defaultValue != null) && defaultValue.contains(query)) {
				return true;
			}

			if (preferences.containsKey(key) && preferences.get(key).contains(query)) {
				return true;
			}

			return false;
		}

		public boolean multiPrefsEquals(String key, String defaultValue, String query) {
			if ((defaultValue != null) && Arrays.asList(defaultValue.split(",")).contains(query)) {
				return true;
			}

			if (preferences.containsKey(key) && Arrays.asList(preferences.get(key).split(",")).contains(query)) {
				return true;
			}

			return false;
		}

		public boolean isPrefOn(String key, Boolean defaultValue) {
			if (Boolean.TRUE.equals(defaultValue)) {
				return true;
			}

			if (preferences.containsKey(key) && "true".equalsIgnoreCase(preferences.get(key))) {
				return true;
			}

			return false;
		}
		
		public String getPrefValue(String key, String defaultValue) {
			

			if (preferences.containsKey(key) ) {
				return preferences.get(key);
			}

			return defaultValue;
		}

		public boolean isNotEmptyPrefs(String key, String defaultValue) {
			if ((defaultValue != null) && !defaultValue.isEmpty()) {
				return true;
			}

			if (preferences.containsKey(key) && !preferences.get(key).isEmpty()) {
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

	/** {@inheritDoc} */
	@Override
	public EntityReportData extract(NodeRef entityNodeRef, Map<String, String> preferences) {

			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}
			
			
			DefaultExtractorContext context = new DefaultExtractorContext(preferences);
			
			Document document = DocumentHelper.createDocument();
			Element entityElt = document.addElement(TAG_ENTITY);
			
			extractEntity(entityNodeRef, entityElt, context);
			
			context.getReportData().setXmlDataSource(entityElt);
			
			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("extract datasource in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
			}
			
			return context.getReportData();

	}

	/**
	 * <p>extractEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
	public void extractEntity(NodeRef entityNodeRef, Element entityElt, DefaultExtractorContext context) {

		// load images
		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		extractEntityImages(entityNodeRef, imgsElt, context);

		// extract site info
		extractSiteInfo(entityNodeRef, entityElt);
		
		// add attributes at <product/> tag
		loadNodeAttributes(entityNodeRef, entityElt, true, context);

		Element aspectsElt = entityElt.addElement(ATTR_ASPECTS);
		aspectsElt.addCDATA(extractAspects(entityNodeRef));

		Element itemTypeElt = entityElt.addElement(ATTR_ITEM_TYPE);
		itemTypeElt.addCDATA(entityDictionaryService.toPrefixString(nodeService.getType(entityNodeRef)));

		loadCreator(entityNodeRef, entityElt, imgsElt, context);

		// render data lists
		Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
		loadDataLists(entityNodeRef, dataListsElt, context);

		// render versions
		loadVersions(entityNodeRef, entityElt);
	}

	private void extractSiteInfo(NodeRef entityNodeRef, Element entityElt) {
		Element siteElt = entityElt.addElement("site");
		Path path = nodeService.getPath(entityNodeRef);
		siteElt.addAttribute("id", SiteHelper.extractSiteId(path.toPrefixString(namespaceService)));
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
			for (NodeRef imgNodeRef : associationService.getChildAssocs(imagesFolderNodeRef,  ContentModel.ASSOC_CONTAINS) ) {

				String imgId = String.format(PRODUCT_IMG_ID, cnt);
				String name = (String) nodeService.getProperty(imagesFolderNodeRef,  ContentModel.PROP_NAME);
				if (name.startsWith(REPORT_LOGO_ID)
						||name.startsWith(I18NUtil.getMessage("report.logo.fileName.prefix", Locale.getDefault()))) {
					imgId = REPORT_LOGO_ID;
				}

				extractImage(entityNodeRef, imgNodeRef, imgId, imgsElt, context);
				cnt++;
			}
		}
	}

	/**
	 * <p>extractImage.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param imgNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param imgId a {@link java.lang.String} object.
	 * @param imgsElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
	protected void extractImage(NodeRef entityNodeRef, NodeRef imgNodeRef, String imgId, Element imgsElt, DefaultExtractorContext context) {

		if (ApplicationModel.TYPE_FILELINK.equals(nodeService.getType(imgNodeRef))) {
			imgNodeRef = (NodeRef) nodeService.getProperty(imgNodeRef, ContentModel.PROP_LINK_DESTINATION);
		}

		if (imgNodeRef != null) {
			EntityImageInfo imgInfo = new EntityImageInfo(imgId,imgNodeRef);
			
			
			imgInfo.setName((String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_NAME));
			imgInfo.setTitle((String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));
			imgInfo.setDescription((String)nodeService.getProperty(imgNodeRef, ContentModel.PROP_DESCRIPTION));
			
			

			Element imgElt = imgsElt.addElement(TAG_IMAGE);
			if(entityNodeRef!=null) {
				imgElt.addAttribute(ATTR_ENTITY_NODEREF, entityNodeRef.toString());
				imgElt.addAttribute(ATTR_ENTITY_TYPE, nodeService.getType(entityNodeRef).getLocalName());
				imgElt.addAttribute(ATTR_ENTITY_NAME, (String) nodeService.getProperty(entityNodeRef, 
						ContentModel.PROP_NAME));
				if(nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_CODE)) {
					imgElt.addAttribute(ATTR_ENTITY_CODE, (String) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE));
				}
			}
			imgElt.addAttribute(ATTR_IMAGE_ID, imgId);
			imgElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), imgInfo.getName());
			imgElt.addAttribute(ContentModel.PROP_TITLE.getLocalName(), imgInfo.getTitle());
			addCDATA(imgElt, ContentModel.PROP_DESCRIPTION, imgInfo.getDescription(), null);
			context.getReportData().getImages().add(imgInfo);
		}
	}

	// render target assocs (plants...special cases)
	/**
	 * <p>loadTargetAssoc.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocDef a {@link org.alfresco.service.cmr.dictionary.AssociationDefinition} object.
	 * @param entityElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 * @return a boolean.
	 */
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, DefaultExtractorContext context) {
		boolean isExtracted = false;
		if ((assocDef != null) && (assocDef.getName() != null)) {
			boolean extractDataList = false;
			boolean extractAssoc = false;
			String prefixedAssocName = entityDictionaryService.toPrefixString(assocDef.getName());
			
			if (context.prefsContains("assocsToExtractWithDataList", assocsToExtractWithDataList,
					prefixedAssocName)) {
				extractDataList = true;
			}

			if (context.isInDataListContext()) {
				if (context.prefsContains("assocsToExtractInDataList", assocsToExtractInDataList,
						prefixedAssocName)) {
					extractAssoc = true;
				}
			} else {
				if (context.prefsContains("assocsToExtract", assocsToExtract,prefixedAssocName)) {
					extractAssoc = true;
				}
			}

			if (extractAssoc || extractDataList) {
				Element assocElt = entityElt.addElement(assocDef.getName().getLocalName());
				appendPrefix(assocDef.getName(), assocElt);
				extractTargetAssoc(entityNodeRef, assocDef, assocElt, context, extractDataList);
				isExtracted = true;
			}

			if (context.prefsContains("assocsToExtractWithImage", assocsToExtractWithImage, prefixedAssocName)) {
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

	/**
	 * <p>isMultiLinesAttribute.</p>
	 *
	 * @param attribute a {@link org.alfresco.service.namespace.QName} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 * @return a boolean.
	 */
	protected boolean isMultiLinesAttribute(QName attribute, DefaultExtractorContext context) {
		return context.prefsContains("multilineProperties", multilineProperties,  entityDictionaryService.toPrefixString(attribute));
	}

	/**
	 * <p>loadDataLists.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListsElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
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

	/**
	 * <p>addDataListState.</p>
	 *
	 * @param xmlNode a {@link org.dom4j.Element} object.
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
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

	/**
	 * <p>loadDataList.</p>
	 *
	 * @param dataListsElt a {@link org.dom4j.Element} object.
	 * @param listNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
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

	/**
	 * <p>loadDataListItemAttributes.</p>
	 *
	 * @param dataListItem a {@link fr.becpg.repo.repository.model.BeCPGDataObject} object.
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt, DefaultExtractorContext context) {
		loadDataListItemAttributes(dataListItem, nodeElt, context, new ArrayList<>());
	}

	/**
	 * <p>loadNodeAttributes.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param useCData a boolean.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
	protected void loadNodeAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, DefaultExtractorContext context) {
		if ((nodeRef != null) && nodeService.exists(nodeRef)) {
			loadAttributes(nodeRef, nodeElt, useCData, hiddenNodeAttributes, context);
			loadComments(nodeRef, nodeElt, context);
		}
	}

	/**
	 * <p>loadDataListItemAttributes.</p>
	 *
	 * @param dataListItem a {@link fr.becpg.repo.repository.model.BeCPGDataObject} object.
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 * @param hiddentAttributes a {@link java.util.List} object.
	 */
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

	/**
	 * <p>loadDataListItemAttributes.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
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
	 * <p>loadAttributes.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param useCData a boolean.
	 * @param hiddenAttributes a {@link java.util.List} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 */
	protected void loadAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, List<QName> hiddenAttributes, DefaultExtractorContext context) {

		// properties
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

		// versionLabel
		String versionLabelDisplayValue = null;
		QName versionLabelQName = null;
		if (properties.containsKey(BeCPGModel.PROP_VERSION_LABEL) && (properties.get(BeCPGModel.PROP_VERSION_LABEL) != null)
				&& !"".equals(properties.get(BeCPGModel.PROP_VERSION_LABEL))) {
			versionLabelQName = BeCPGModel.PROP_VERSION_LABEL;
		} else if (properties.containsKey(ContentModel.PROP_VERSION_LABEL) && (properties.get(ContentModel.PROP_VERSION_LABEL) != null)
				&& !"".equals(properties.get(ContentModel.PROP_VERSION_LABEL))) {
			versionLabelQName = ContentModel.PROP_VERSION_LABEL;
		}
		if (versionLabelQName != null) {
			PropertyDefinition propertyDef = dictionaryService.getProperty(versionLabelQName);
			if (propertyDef == null) {
				logger.debug("This property doesn't exist. Name: " + versionLabelQName + " nodeRef : " + nodeRef);
			}
			versionLabelDisplayValue = attributeExtractorService.extractPropertyForReport(propertyDef, properties.get(versionLabelQName), false);
		}

		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

			// do not display system properties
			if ((hiddenAttributes == null) || (!hiddenAttributes.contains(property.getKey())
					&& !NamespaceService.SYSTEM_MODEL_1_0_URI.equals(property.getKey().getNamespaceURI()))) {

				PropertyDefinition propertyDef = dictionaryService.getProperty(property.getKey());
				if (propertyDef == null) {
					logger.debug("This property doesn't exist. Name: " + property.getKey() + " nodeRef : " + nodeRef);
					continue;
				}
				
				boolean isNodeRefProp = false;
				
				if (DataTypeDefinition.NODE_REF.toString().equals(propertyDef.getDataType().toString()) && context.prefsContains("assocsToExtract", assocsToExtract, propertyDef.getName().toPrefixString(namespaceService))) {
					
					isNodeRefProp = true;
					
					NodeRef dNodeRef = (NodeRef) property.getValue();
					
					if (dNodeRef != null) {
						Element newElement = nodeElt.addElement(propertyDef.getName().getLocalName());
						
						appendPrefix(propertyDef.getName(), newElement);
						
						if (entityDictionaryService.isSubClass(propertyDef.getName(), BeCPGModel.TYPE_CHARACT)) {
							List<QName> hiddentAttributes = new ArrayList<>();
							hiddentAttributes.addAll(hiddenNodeAttributes);
							hiddentAttributes.addAll(hiddenDataListItemAttributes);
							
							loadAttributes(dNodeRef, newElement, true, hiddentAttributes, context);
						} else {
							loadNodeAttributes(dNodeRef, newElement, true, context);
						}
					}
				}

				if (isNodeRefProp) {
					continue;
				}
				
				String value = attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), false);

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
					String displayValue = attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), true);
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
					if ((versionLabelDisplayValue != null) && (property.getKey().equals(BeCPGModel.PROP_VERSION_LABEL)
							|| property.getKey().equals(ContentModel.PROP_VERSION_LABEL))) {
						addData(nodeElt, useCData, propertyDef.getName(), versionLabelDisplayValue, null, context);
					} else {
						addData(nodeElt, useCData, propertyDef.getName(), value, null, context);
					}
				}

				if (context.prefsContains("mlTextFields", mlTextFields,  entityDictionaryService.toPrefixString(propertyDef.getName()))) {
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
							if ((((mlTextLocales == null) || "".equals(mlTextLocales)) && ((context.getPreferences() == null)
									|| ((context.getPreferences() != null) && !context.getPreferences().containsKey("mlTextLocales"))))
									|| context.prefsContains("mlTextLocales", mlTextLocales, code)) {
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
			if (hiddenAttributes==null || !hiddenAttributes.contains(associationDef.getName())) {

				if (!associationDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !associationDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !associationDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER)
						&& !associationDef.getName().equals(ContentModel.ASSOC_ORIGINAL) && !associationDef.isChild()) {

					if (!loadTargetAssoc(nodeRef, associationDef, nodeElt, context) || (useCData == false)) {

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

	/**
	 * <p>addData.</p>
	 *
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param useCData a boolean.
	 * @param propertyQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param value a {@link java.lang.String} object.
	 * @param suffix a {@link java.lang.String} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 * @return a {@link org.dom4j.Element} object.
	 */
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

	/**
	 * <p>getPropNameOfType.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	protected QName getPropNameOfType(QName type) {
		return null;
	}

	/**
	 * <p>generateKeyAttribute.</p>
	 *
	 * @param attributeName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String generateKeyAttribute(String attributeName) {

		return StringUtils.stripAccents(attributeName.replaceAll(REGEX_REMOVE_CHAR, "").toLowerCase());
	}

	/**
	 * <p>loadVersions.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityElt a {@link org.dom4j.Element} object.
	 */
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

	/**
	 * <p>extractNames.</p>
	 *
	 * @param nodeRefs a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String extractNames(List<NodeRef> nodeRefs) {
		StringBuilder value = null;
		for (NodeRef nodeRef : nodeRefs) {
			if (value!=null) {
				value.append(RepoConsts.LABEL_SEPARATOR);
			} else  {
				value = new StringBuilder();
			}
			value.append((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
		}
		return value == null ? VALUE_NULL : value.toString();
	}

	/**
	 * <p>extractAspects.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String extractAspects(NodeRef nodeRef) {
		StringBuilder value = null;
		for (QName aspect : nodeService.getAspects(nodeRef)) {
			if (value!=null) {
				value.append(RepoConsts.LABEL_SEPARATOR);
			} else  {
				value = new StringBuilder();
			}
			
			value.append( entityDictionaryService.toPrefixString(aspect));
		}
		
		return value == null ? VALUE_NULL : value.toString();
	}

	/**
	 * Extract target(s) association
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocDef a {@link org.alfresco.service.cmr.dictionary.AssociationDefinition} object.
	 * @param assocElt a {@link org.dom4j.Element} object.
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContext} object.
	 * @param extractDataList a boolean.
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

	/**
	 * <p>addCDATA.</p>
	 *
	 * @param nodeElt a {@link org.dom4j.Element} object.
	 * @param propertyQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param eltValue a {@link java.lang.String} object.
	 * @param suffix a {@link java.lang.String} object.
	 * @return a {@link org.dom4j.Element} object.
	 */
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

	/**
	 * <p>appendPrefix.</p>
	 *
	 * @param propertyQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param cDATAElt a {@link org.dom4j.Element} object.
	 */
	protected void appendPrefix(QName propertyQName, Element cDATAElt) {
		cDATAElt.addAttribute("prefix",  entityDictionaryService.toPrefixString(propertyQName).split(":")[0]);
	}

	// Check that images has not been update
	/** {@inheritDoc} */
	@Override
	public boolean shouldGenerateReport(NodeRef entityNodeRef, Date generatedReportDate) {
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			Date modified = (Date) nodeService.getProperty(imagesFolderNodeRef, ContentModel.PROP_MODIFIED);
			if ((modified == null) || (generatedReportDate == null) || (modified.getTime() > generatedReportDate.getTime())) {
				return true;
			}
			
		}
		return false;
	}

	/** {@inheritDoc} */
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
