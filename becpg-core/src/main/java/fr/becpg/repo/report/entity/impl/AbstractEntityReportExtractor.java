/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractor;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public abstract class AbstractEntityReportExtractor implements EntityReportExtractor {

	private static Log logger = LogFactory.getLog(AbstractEntityReportExtractor.class);

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

	/** The Constant VALUE_NULL. */
	protected static final String VALUE_NULL = "";
	
	private static final String REGEX_REMOVE_CHAR = "[^\\p{L}\\p{N}]";

	protected static final ArrayList<QName> hiddenNodeAttributes = new ArrayList<QName>(Arrays.asList(ContentModel.PROP_NODE_REF, ContentModel.PROP_NODE_DBID,
			ContentModel.PROP_NODE_UUID, ContentModel.PROP_STORE_IDENTIFIER, ContentModel.PROP_STORE_NAME, ContentModel.PROP_STORE_PROTOCOL, ContentModel.PROP_CONTENT));

	protected static final ArrayList<QName> hiddenDataListItemAttributes = new ArrayList<QName>(Arrays.asList(ContentModel.PROP_NAME, ContentModel.PROP_CREATED,
			ContentModel.PROP_CREATOR, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER));

	protected DictionaryService dictionaryService;

	protected NamespaceService namespaceService;

	protected AttributeExtractorService attributeExtractorService;

	protected NodeService nodeService;

	protected EntityService entityService;

	protected VersionService versionService;

	protected FileFolderService fileFolderService;

	protected AssociationService associationService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public EntityReportData extract(NodeRef entityNodeRef) {

		EntityReportData ret = new EntityReportData();

		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_ENTITY);
		Map<String, byte[]> images = new HashMap<String, byte[]>();

		// add attributes at <product/> tag
		loadNodeAttributes(entityNodeRef, entityElt, true);

		Element aspectsElt = entityElt.addElement(ATTR_ASPECTS);
		aspectsElt.addCDATA(extractAspects(entityNodeRef));

		Element itemTypeElt = entityElt.addElement(ATTR_ITEM_TYPE);
		itemTypeElt.addCDATA(nodeService.getType(entityNodeRef).getPrefixString());

		// load images
		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		extractEntityImages(entityNodeRef, imgsElt, images);

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
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			for (FileInfo fileInfo : fileFolderService.listFiles(imagesFolderNodeRef)) {

				String imgName = fileInfo.getName().toLowerCase();
				NodeRef imgNodeRef = fileInfo.getNodeRef();
				String imgId = String.format(PRODUCT_IMG_ID, cnt);
				byte[] imageBytes = entityService.getImage(imgNodeRef);
				if (imageBytes != null) {
					Element imgElt = imgsElt.addElement(TAG_IMAGE);
					imgElt.addAttribute(ATTR_IMAGE_ID, imgId);
					imgElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), imgName);
					imgElt.addAttribute(ContentModel.PROP_TITLE.getLocalName(), (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));

					images.put(imgId, imageBytes);
				}
				cnt++;
			}
		}
	}

	// render target assocs (plants...special cases)
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt) {
		return false;
	}
	
	protected abstract boolean isMultiLinesAttribute(QName attribute);

	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images) {
	}

	protected void loadNodeAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData) {
		loadAttributes(nodeRef, nodeElt, useCData, hiddenNodeAttributes);
	}

	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt) {
		List<QName> hiddentAttributes = new ArrayList<>();
		hiddentAttributes.addAll(hiddenNodeAttributes);
		hiddentAttributes.addAll(hiddenDataListItemAttributes);
		loadAttributes(dataListItem.getNodeRef(), nodeElt, false, hiddentAttributes);
		// extract charact properties (legalname,...)
		if(dataListItem instanceof SimpleCharactDataItem){
			nodeElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(), 
					(String)nodeService.getProperty(((SimpleCharactDataItem) dataListItem).getCharactNodeRef(), BeCPGModel.PROP_LEGAL_NAME));
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
	protected void loadAttributes(NodeRef nodeRef, Element nodeElt, boolean useCData, List<QName> hiddenAttributes) {

		PropertyFormats propertyFormats = new PropertyFormats(true);
		Map<ClassAttributeDefinition, String> values = new HashMap<ClassAttributeDefinition, String>();

		// properties
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

			// do not display system properties
			if (hiddenAttributes == null || !hiddenAttributes.contains(property.getKey())) {

				PropertyDefinition propertyDef = dictionaryService.getProperty(property.getKey());
				if (propertyDef == null) {
					logger.error("This property doesn't exist. Name: " + property.getKey());
					continue;
				}
				values.put(propertyDef, attributeExtractorService.extractPropertyForReport(propertyDef, property.getValue(), propertyFormats));
			}
		}

		// associations
		Map<QName, String> tempValues = new HashMap<QName, String>();
		List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

		for (AssociationRef assocRef : associations) {

			QName qName = assocRef.getTypeQName();
			String name = attributeExtractorService.extractAssociationForReport(assocRef);

			if (tempValues.containsKey(qName)) {
				String names = tempValues.get(qName);
				names += RepoConsts.LABEL_SEPARATOR;
				names += name;
				tempValues.put(qName, names);
			} else {
				tempValues.put(qName, name);
			}
		}

		for (Map.Entry<QName, String> tempValue : tempValues.entrySet()) {
			AssociationDefinition associationDef = dictionaryService.getAssociation(tempValue.getKey());
			if (associationDef == null) {
				logger.error("This association doesn't exist. Name: " + tempValue.getKey());
				continue;
			}
			values.put(associationDef, tempValue.getValue());
		}

		for (Map.Entry<ClassAttributeDefinition, String> attrKV : values.entrySet()) {

			if (attrKV.getKey() instanceof PropertyDefinition || !loadTargetAssoc(nodeRef, (AssociationDefinition) attrKV.getKey(), nodeElt)) {

				if (useCData || isMultiLinesAttribute(attrKV.getKey().getName())) {
					addCDATA(nodeElt, attrKV.getKey().getName(), attrKV.getValue());
				} else {
					nodeElt.addAttribute(attrKV.getKey().getName().getLocalName(), attrKV.getValue());
				}
			}
		}
	}

	protected String generateKeyAttribute(String attributeName) {

		return attributeName.replaceAll(REGEX_REMOVE_CHAR, "").toLowerCase();
	}

	protected void loadVersions(NodeRef entityNodeRef, Element entityElt) {

		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
		Element versionsElt = entityElt.addElement(TAG_VERSIONS);

		if (versionHistory != null && versionHistory.getAllVersions() != null) {

			for (Version version : versionHistory.getAllVersions()) {
				Element versionElt = versionsElt.addElement(TAG_VERSION);
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_LABEL.getLocalName(), version.getVersionLabel());
				versionElt.addAttribute(Version2Model.PROP_QNAME_VERSION_DESCRIPTION.getLocalName(), version.getDescription());
				versionElt.addAttribute(ContentModel.PROP_CREATOR.getLocalName(), attributeExtractorService.getPersonDisplayName(version.getFrozenModifier()));
				versionElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(),
						attributeExtractorService.getPropertyFormats().getDateFormat().format(version.getFrozenModifiedDate()));
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
	
	//Check that images has not been update
	public boolean shouldGenerateReport(NodeRef entityNodeRef) {
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
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
}
