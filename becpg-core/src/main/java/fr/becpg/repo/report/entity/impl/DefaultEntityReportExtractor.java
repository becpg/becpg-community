/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
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
	protected static final String PRODUCT_IMG_ID = "Img%d";
	protected static final String ATTR_IMAGE_ID = "id";
	protected static final String AVATAR_IMG_ID = "avatar";
	protected static final String REPORT_LOGO_ID = "report_logo";
	private static final String TAG_COMMENTS = "comments";
	private static final String TAG_COMMENT = "comment";

	protected static final String VALUE_NULL = "";

	private static final String REGEX_REMOVE_CHAR = "[^\\p{L}\\p{N}]";

	protected static final ArrayList<QName> hiddenNodeAttributes = new ArrayList<>(
			Arrays.asList(ContentModel.PROP_NODE_REF, ContentModel.PROP_NODE_UUID, ContentModel.PROP_STORE_IDENTIFIER, ContentModel.PROP_STORE_NAME,
					ContentModel.PROP_STORE_PROTOCOL, ContentModel.PROP_CONTENT, BeCPGModel.PROP_ENTITY_SCORE));

	protected static final ArrayList<QName> hiddenDataListItemAttributes = new ArrayList<>(
			Arrays.asList(ContentModel.PROP_CREATED, ContentModel.PROP_CREATOR, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER));
	private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");

	@Value("${beCPG.entity.report.mltext.fields}")
	private String mlTextFields;

	@Autowired
	protected DictionaryService dictionaryService;

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
	protected EntityListDAO entityListDAO;
	
	@Autowired
	protected AlfrescoRepository<BeCPGDataObject> alfrescoRepository;

	@Override
	public EntityReportData extract(NodeRef entityNodeRef) {

		EntityReportData ret = new EntityReportData();

		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_ENTITY);
		Map<String, byte[]> images = new HashMap<>();

		extractEntity(entityNodeRef, entityElt, images);

		ret.setXmlDataSource(entityElt);
		ret.setDataObjects(images);

		return ret;
	}

	public void extractEntity(NodeRef entityNodeRef, Element entityElt, Map<String, byte[]> images) {

		// load images
		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		extractEntityImages(entityNodeRef, imgsElt, images);

		// add attributes at <product/> tag
		loadNodeAttributes(entityNodeRef, entityElt, true, images);

		Element aspectsElt = entityElt.addElement(ATTR_ASPECTS);
		aspectsElt.addCDATA(extractAspects(entityNodeRef));

		Element itemTypeElt = entityElt.addElement(ATTR_ITEM_TYPE);
		itemTypeElt.addCDATA(nodeService.getType(entityNodeRef).getPrefixString());

		loadCreator(entityNodeRef, entityElt, imgsElt, images);

		// render data lists
		Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
		loadDataLists(entityNodeRef, dataListsElt, images);

		// render versions
		loadVersions(entityNodeRef, entityElt);
	}

	protected void extractEntityImages(NodeRef entityNodeRef, Element imgsElt, Map<String, byte[]> images) {

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

				extractImage(fileInfo.getNodeRef(), imgId, imgsElt, images);
				cnt++;
			}
		}
	}

	protected void extractImage(NodeRef imgNodeRef, String imgId, Element imgsElt, Map<String, byte[]> images) {

		if (ApplicationModel.TYPE_FILELINK.equals(nodeService.getType(imgNodeRef))) {
			imgNodeRef = (NodeRef) nodeService.getProperty(imgNodeRef, ContentModel.PROP_LINK_DESTINATION);
		}

		byte[] imageBytes = entityService.getImage(imgNodeRef);
		if (imageBytes != null) {
			Element imgElt = imgsElt.addElement(TAG_IMAGE);
			imgElt.addAttribute(ATTR_IMAGE_ID, imgId);
			imgElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_NAME));
			imgElt.addAttribute(ContentModel.PROP_TITLE.getLocalName(), (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));

			images.put(imgId, imageBytes);
		}
	}

	// render target assocs (plants...special cases)
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, Map<String, byte[]> images) {
		return false;
	}

	protected boolean isMultiLinesAttribute(QName attribute) {
		return false;
	}

	@SuppressWarnings("deprecation")
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images) {
		
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if(listContainerNodeRef != null){
			List<NodeRef> listNodeRefs = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);
			
			for(NodeRef listNodeRef : listNodeRefs){
				QName dataListQName = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE), namespaceService);
				
				Class<RepositoryEntity> entityClass = repositoryEntityDefReader.getEntityClass(dataListQName);
				if (entityClass != null) {
					List<BeCPGDataObject> dataListItems = alfrescoRepository.loadDataList(entityNodeRef, dataListQName, dataListQName);
					
					if ((dataListItems != null) && !dataListItems.isEmpty()) {
						Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");

						for (BeCPGDataObject dataListItem : dataListItems) {

							addDataListState(dataListElt, dataListItem.getParentNodeRef());
							Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
							loadDataListItemAttributes(dataListItem, nodeElt, images);
						}
					}
				}
				else{
					List<NodeRef> dataListItems = entityListDAO.getListItems(listNodeRef, dataListQName);
					if ((dataListItems != null) && !dataListItems.isEmpty()) {
						Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");

						for (NodeRef dataListItem : dataListItems) {

							addDataListState(dataListElt, dataListItem);
							Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
							loadDataListItemAttributes(dataListItem, nodeElt, images);
						}
					}
				}
			}
		}
	}
	
	protected void addDataListState(Element xmlNode, NodeRef listNodeRef) {

		if (xmlNode.valueOf("@" + BeCPGModel.PROP_ENTITYLIST_STATE.getLocalName()).isEmpty()) {
			Serializable state = nodeService.getProperty(listNodeRef, BeCPGModel.PROP_ENTITYLIST_STATE);
			if (state != null) {
				xmlNode.addAttribute(BeCPGModel.PROP_ENTITYLIST_STATE.getLocalName(), (String) state);
			} else {
				xmlNode.addAttribute(BeCPGModel.PROP_ENTITYLIST_STATE.getLocalName(), SystemState.ToValidate.toString());
			}

		}
	}

	protected void loadNodeAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, Map<String, byte[]> images) {
		if ((nodeRef != null) && nodeService.exists(nodeRef)) {
			loadAttributes(nodeRef, nodeElt, useCData, hiddenNodeAttributes, images);
			loadComments(nodeRef, nodeElt, images);
		}
	}
	
	protected void loadDataListItemAttributes(NodeRef nodeRef, Element nodeElt, Map<String, byte[]> images) {
		List<QName> hiddentAttributes = new ArrayList<>();
		hiddentAttributes.addAll(hiddenNodeAttributes);
		hiddentAttributes.addAll(hiddenDataListItemAttributes);

		if ((nodeRef != null) && nodeService.exists(nodeRef)) {
			loadAttributes(nodeRef, nodeElt, false, hiddentAttributes, images);
			loadComments(nodeRef, nodeElt, images);
		}
	}

	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt, Map<String, byte[]> images) {
		List<QName> hiddentAttributes = new ArrayList<>();
		hiddentAttributes.addAll(hiddenNodeAttributes);
		hiddentAttributes.addAll(hiddenDataListItemAttributes);

		if ((dataListItem.getNodeRef() != null) && nodeService.exists(dataListItem.getNodeRef())) {
			loadAttributes(dataListItem.getNodeRef(), nodeElt, false, hiddentAttributes, images);

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
			loadComments(dataListItem.getNodeRef(), nodeElt, images);
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
	protected void loadAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, List<QName> hiddenAttributes, Map<String, byte[]> images) {

		PropertyFormats propertyFormats = new PropertyFormats(true);

		// properties
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

			// do not display system properties
			if ((hiddenAttributes == null) || !hiddenAttributes.contains(property.getKey())) {

				PropertyDefinition propertyDef = dictionaryService.getProperty(property.getKey());
				if (propertyDef == null) {
					logger.error("This property doesn't exist. Name: " + property.getKey() + " nodeRef : " + nodeRef);
					continue;
				}
				addData(nodeElt, useCData, propertyDef.getName(),
						attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), propertyFormats, false), null);

				if ((mlTextFields != null) && !mlTextFields.isEmpty()
						&& mlTextFields.contains(propertyDef.getName().toPrefixString(namespaceService))) {

					MLText mlValues = null;

					if (DataTypeDefinition.MLTEXT.equals(propertyDef.getDataType().getName())) {
						mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDef.getName());

					} else if (DataTypeDefinition.TEXT.equals(propertyDef.getDataType().getName())) {

						DynListConstraint dynListConstraint = null;

						if (!propertyDef.getConstraints().isEmpty()) {

							for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
								if (constraint.getConstraint() instanceof DynListConstraint) {
									dynListConstraint = (DynListConstraint) constraint.getConstraint();
									break;

								}
							}

						}

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
								addData(nodeElt, useCData, propertyDef.getName(), mlEntry.getValue(), code);
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

				if (!loadTargetAssoc(nodeRef, associationDef, nodeElt, images)) {

					List<NodeRef> assocNodes = associationService.getTargetAssocs(nodeRef, associationDef.getName());

					if ((assocNodes != null) && !assocNodes.isEmpty()) {
						String ret = assocNodes.stream().map(i -> extractName(associationDef.getTargetClass().getName(), i))
								.collect(Collectors.joining(RepoConsts.LABEL_SEPARATOR));

						addData(nodeElt, useCData, associationDef.getName(), ret, null);

					}

				}
			}
		}

	}

	private String extractName(QName targetClass, NodeRef nodeRef) {
		
		if(!nodeService.exists(nodeRef)){
			logger.info("Extract name : "+targetClass);
		}
		
		QName propNameOfType = getPropNameOfType(targetClass);

		if (propNameOfType != null) {
			return (String) nodeService.getProperty(nodeRef, propNameOfType);
		}

		return attributeExtractorService.extractPropName(targetClass, nodeRef);
	}

	private void loadComments(NodeRef nodeRef, Element nodeElt, Map<String, byte[]> images) {
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
							loadAttributes(post.getChildRef(), commentElt, true, hiddenNodeAttributes, images);
							ContentReader reader = contentService.getReader(post.getChildRef(), ContentModel.PROP_CONTENT);
							if (reader != null) {
								addData(commentElt, true, ContentModel.PROP_CONTENT, reader.getContentString(), null);
							}
						}
					}
				}
			}
		}

	}

	protected void addData(Element nodeElt, boolean useCData, QName propertyQName, String value, String suffix) {
		if (useCData || isMultiLinesAttribute(propertyQName)) {
			addCDATA(nodeElt, propertyQName, value, suffix);
		} else {
			String localName = propertyQName.getLocalName();
			if ((suffix != null) && !suffix.isEmpty()) {
				localName += "_" + suffix;
			}
			nodeElt.addAttribute(localName, value);
		}
	}

	protected QName getPropNameOfType(QName type) {
		return null;
	}

	protected String generateKeyAttribute(String attributeName) {

		return attributeName.replaceAll(REGEX_REMOVE_CHAR, "").toLowerCase();
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
				versionElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(), 
						ISO8601DateFormat.format((Date) version.getFrozenModifiedDate()));
				
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
			value += aspect.toPrefixString();
		}
		return value;
	}

	/**
	 * Extract target(s) association
	 */
	protected void extractTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element assocElt, Map<String, byte[]> images,
			boolean extractDataList) {

		List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());

		for (NodeRef nodeRef : nodeRefs) {

			QName qName = nodeService.getType(nodeRef);
			Element nodeElt = assocElt.addElement(qName.getLocalName());

			appendPrefix(qName, nodeElt);

			loadNodeAttributes(nodeRef, nodeElt, true, images);

			if (extractDataList) {
				Element dataListsElt = nodeElt.addElement(TAG_DATALISTS);
				loadDataLists(nodeRef, dataListsElt, new HashMap<String, byte[]>());
			}
		}
	}

	protected void addCDATA(Element nodeElt, QName propertyQName, String eltValue, String suffix) {
		String localName = propertyQName.getLocalName();
		if ((suffix != null) && !suffix.isEmpty()) {
			localName += "_" + suffix;
		}
		Element cDATAElt = nodeElt.addElement(localName);
		appendPrefix(propertyQName, cDATAElt);

		cDATAElt.addCDATA(eltValue);

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

	private void loadCreator(NodeRef entityNodeRef, Element entityElt, Element imgsElt, Map<String, byte[]> images) {
		String creator = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATOR);
		if (creator != null) {
			Element creatorElt = (Element) entityElt.selectSingleNode(ContentModel.PROP_CREATOR.getLocalName());
			NodeRef creatorNodeRef = personService.getPerson(creator);
			loadNodeAttributes(creatorNodeRef, creatorElt, true, images);
			// extract avatar
			List<AssociationRef> avatorAssocs = nodeService.getTargetAssocs(creatorNodeRef, ContentModel.ASSOC_AVATAR);
			if (!avatorAssocs.isEmpty()) {
				extractImage(avatorAssocs.get(0).getTargetRef(), AVATAR_IMG_ID, imgsElt, images);
			}
		}
	}
}
