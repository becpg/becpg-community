/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@Service
public class DefaultEntityReportExtractor implements EntityReportExtractorPlugin {

	private static final Log logger = LogFactory.getLog(DefaultEntityReportExtractor.class);

	/** The Constant TAG_ENTITY. */
	protected static final String TAG_ENTITY = "entity";

	/** The Constant TAG_DATALISTS. */
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
	private static final String TAG_COMMENTS = "comments";
	private static final String TAG_COMMENT = "comment";

	/** The Constant VALUE_NULL. */
	protected static final String VALUE_NULL = "";

	private static final String REGEX_REMOVE_CHAR = "[^\\p{L}\\p{N}]";

	protected static final ArrayList<QName> hiddenNodeAttributes = new ArrayList<>(Arrays.asList(ContentModel.PROP_NODE_REF,
			ContentModel.PROP_NODE_UUID, ContentModel.PROP_STORE_IDENTIFIER, ContentModel.PROP_STORE_NAME,
			ContentModel.PROP_STORE_PROTOCOL, ContentModel.PROP_CONTENT));

	protected static final ArrayList<QName> hiddenDataListItemAttributes = new ArrayList<>(Arrays.asList(ContentModel.PROP_CREATED, ContentModel.PROP_CREATOR, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER));
	private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");

	@Autowired
	protected DictionaryService dictionaryService;

	@Autowired
	protected NamespaceService namespaceService;

	@Autowired
	protected AttributeExtractorService attributeExtractorService;

	@Autowired
	protected NodeService nodeService;

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

	@Override
	public EntityReportData extract(NodeRef entityNodeRef) {

		EntityReportData ret = new EntityReportData();

		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_ENTITY);
		Map<String, byte[]> images = new HashMap<>();

		// add attributes at <product/> tag
		loadNodeAttributes(entityNodeRef, entityElt, true);

		Element aspectsElt = entityElt.addElement(ATTR_ASPECTS);
		aspectsElt.addCDATA(extractAspects(entityNodeRef));

		Element itemTypeElt = entityElt.addElement(ATTR_ITEM_TYPE);
		itemTypeElt.addCDATA(nodeService.getType(entityNodeRef).getPrefixString());

		// load images
		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		extractEntityImages(entityNodeRef, imgsElt, images);
		
		loadCreator(entityNodeRef, entityElt, imgsElt, images);

		// render data lists
		Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
		loadDataLists(entityNodeRef, dataListsElt, images);

		// render versions
		loadVersions(entityNodeRef, entityElt);

		ret.setXmlDataSource(entityElt);
		ret.setDataObjects(images);

		return ret;
	}

	protected void extractEntityImages(NodeRef entityNodeRef, Element imgsElt, Map<String, byte[]> images) {

		int cnt = imgsElt.selectNodes(TAG_IMAGE) != null ? imgsElt.selectNodes(TAG_IMAGE).size() : 1;
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			for (FileInfo fileInfo : fileFolderService.listFiles(imagesFolderNodeRef)) {
				extractImage(fileInfo.getNodeRef(), String.format(PRODUCT_IMG_ID, cnt), imgsElt, images);
				cnt++;
			}
		}				
	}

	protected void extractImage(NodeRef imgNodeRef, String imgId, Element imgsElt, Map<String, byte[]> images) {
		
		if(ApplicationModel.TYPE_FILELINK.equals(nodeService.getType(imgNodeRef))){
			imgNodeRef = (NodeRef)nodeService.getProperty(imgNodeRef, ContentModel.PROP_LINK_DESTINATION);
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
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt) {
		return false;
	}

	protected boolean isMultiLinesAttribute(QName attribute) {
		return false;
	}

	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images) {
	}

	protected void loadNodeAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData) {
		loadAttributes(nodeRef, nodeElt, useCData, hiddenNodeAttributes);
		loadComments(nodeRef, nodeElt);
	}

	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt) {
		List<QName> hiddentAttributes = new ArrayList<>();
		hiddentAttributes.addAll(hiddenNodeAttributes);
		hiddentAttributes.addAll(hiddenDataListItemAttributes);
		loadAttributes(dataListItem.getNodeRef(), nodeElt, false, hiddentAttributes);

		// look for charact
		Map<QName, Serializable> identAttr = repositoryEntityDefReader.getIdentifierAttributes(dataListItem);
		for (Map.Entry<QName, Serializable> kv : identAttr.entrySet()) {
			if (kv.getValue() instanceof NodeRef && nodeService.hasAspect((NodeRef) kv.getValue(), BeCPGModel.ASPECT_LEGAL_NAME)) {
				nodeElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(),
						(String) nodeService.getProperty((NodeRef) kv.getValue(), BeCPGModel.PROP_LEGAL_NAME));
				addCDATA(nodeElt, ContentModel.PROP_DESCRIPTION, (String)nodeService.getProperty((NodeRef) kv.getValue(), ContentModel.PROP_DESCRIPTION));
				break;
			}
		}
		loadComments(dataListItem.getNodeRef(), nodeElt);
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
	protected void loadAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, List<QName> hiddenAttributes) {

		PropertyFormats propertyFormats = new PropertyFormats(true);

		if (nodeRef != null && nodeService.exists(nodeRef)) {
			// properties
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
			for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

				// do not display system properties
				if (hiddenAttributes == null || !hiddenAttributes.contains(property.getKey())) {

					PropertyDefinition propertyDef = dictionaryService.getProperty(property.getKey());
					if (propertyDef == null) {
						logger.error("This property doesn't exist. Name: " + property.getKey() + " nodeRef : " + nodeRef);
						continue;
					}
					addData(nodeElt, useCData, propertyDef.getName(),
							attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), propertyFormats, false));
				}
			}

			// associations
			Map<QName, List<AssociationRef>> tempHashMap = new HashMap<>();
			List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

			for (AssociationRef assocRef : associations) {
				QName qName = assocRef.getTypeQName();
				List<AssociationRef> assocRefs = tempHashMap.get(qName);
				if (assocRefs == null) {
					assocRefs = new ArrayList<>();
					tempHashMap.put(qName, assocRefs);
				}
				assocRefs.add(assocRef);
			}

			for (Map.Entry<QName, List<AssociationRef>> tempValue : tempHashMap.entrySet()) {
				AssociationDefinition associationDef = dictionaryService.getAssociation(tempValue.getKey());
				if (associationDef == null) {
					logger.error("This association doesn't exist. Name: " + tempValue.getKey());
					continue;
				} else if (!loadTargetAssoc(nodeRef, associationDef, nodeElt)) {
					addData(nodeElt, useCData, associationDef.getName(), attributeExtractorService.extractAssociationsForReport(tempValue.getValue(),
							getPropNameOfType(associationDef.getTargetClass().getName())));
				}
			}
		}
	}
	
	protected void loadComments(NodeRef nodeRef, Element nodeElt){
		if (nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE)) {
			List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ForumModel.ASSOC_DISCUSSION, ForumModel.ASSOC_DISCUSSION, true);
	        if (!assocs.isEmpty()){
	            NodeRef forumFolder = assocs.get(0).getChildRef();
	            List<ChildAssociationRef> topics = nodeService.getChildAssocs(forumFolder, ContentModel.ASSOC_CONTAINS,
						FORUM_TO_TOPIC_ASSOC_QNAME, true);
	            if (!topics.isEmpty()) {
					NodeRef firstTopicNodeRef = topics.get(0).getChildRef();
					List<ChildAssociationRef> posts = nodeService.getChildAssocs(firstTopicNodeRef);
					if(!posts.isEmpty()){
						Element commentsElt = (Element) nodeElt.addElement(TAG_COMMENTS);
						for(ChildAssociationRef post : posts){        	            		
    	            		Element commentElt = (Element) commentsElt.addElement(TAG_COMMENT);
    	            		loadAttributes(post.getChildRef(), commentElt, true, hiddenNodeAttributes);	            		
    	            		ContentReader reader = contentService.getReader(post.getChildRef(), ContentModel.PROP_CONTENT);	            		
    	            		if(reader != null){
    	            			addData(commentElt, true, ContentModel.PROP_CONTENT, reader.getContentString());
    	            		}	            		
    		            }
					}				
				}	            
	        }
		}
	}

	protected void addData(Element nodeElt, boolean useCData, QName qName, String value) {
		if (useCData || isMultiLinesAttribute(qName)) {
			addCDATA(nodeElt, qName, value);
		} else {
			nodeElt.addAttribute(qName.getLocalName(), value);
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

		PropertyFormats propertyFormats = new PropertyFormats(false);

		if (versionHistory != null && versionHistory.getAllVersions() != null) {

			for (Version version : versionHistory.getAllVersions()) {
				Element versionElt = versionsElt.addElement(TAG_VERSION);
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_LABEL.getLocalName(), version.getVersionLabel());
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_DESCRIPTION.getLocalName(), version.getDescription());
				versionElt.addAttribute(ContentModel.PROP_CREATOR.getLocalName(),
						attributeExtractorService.getPersonDisplayName(version.getFrozenModifier()));
				versionElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(), propertyFormats.formatDate(version.getFrozenModifiedDate()));
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
	protected void extractTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt) {

		Element rootElt = assocDef.isTargetMany() ? entityElt.addElement(assocDef.getName().getLocalName()) : entityElt;
		List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());

		for (NodeRef nodeRef : nodeRefs) {

			QName qName = nodeService.getType(nodeRef);
			Element nodeElt = rootElt.addElement(qName.getLocalName());
			loadNodeAttributes(nodeRef, nodeElt, true);
		}
	}

	protected void addCDATA(Element nodeElt, QName propertyQName, String eltValue) {
		Element cDATAElt = nodeElt.addElement(propertyQName.getLocalName());
		cDATAElt.addCDATA(eltValue);

		Collection<String> prefixes = namespaceService.getPrefixes(propertyQName.getNamespaceURI());
		if (prefixes.size() != 0) {

			// TODO : manage prefix correctly
			cDATAElt.addAttribute("prefix", prefixes.iterator().next());
		}
	}

	// Check that images has not been update
	public boolean shouldGenerateReport(NodeRef entityNodeRef) {
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			Date modified = (Date) nodeService.getProperty(imagesFolderNodeRef, ContentModel.PROP_MODIFIED);
			Date generatedReportDate = (Date) nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
			if (modified == null || generatedReportDate == null || modified.getTime() > generatedReportDate.getTime()) {
				return true;
			}
			for (FileInfo fileInfo : fileFolderService.listFiles(imagesFolderNodeRef)) {
				modified = fileInfo.getModifiedDate();
				if (modified == null || generatedReportDate == null || modified.getTime() > generatedReportDate.getTime()) {
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

	private void loadCreator(NodeRef entityNodeRef, Element entityElt, Element imgsElt, Map<String, byte[]> images){		
		String creator = (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATOR);
		if(creator != null){					
			Element creatorElt = (Element) entityElt.selectSingleNode(ContentModel.PROP_CREATOR.getLocalName());
			NodeRef creatorNodeRef = personService.getPerson(creator);
			loadNodeAttributes(creatorNodeRef, creatorElt, true);
			// extract avatar			
			List<AssociationRef> avatorAssocs = nodeService.getTargetAssocs(creatorNodeRef, ContentModel.ASSOC_AVATAR);
			if(!avatorAssocs.isEmpty()){
				extractImage(avatorAssocs.get(0).getTargetRef(), AVATAR_IMG_ID, imgsElt, images);
			}
		}
	}
}
