/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * Helper used to manage a property
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface AttributeExtractorService {

	/** Constant <code>PROP_SUFFIX="prop_"</code> */
	public static final String PROP_SUFFIX = "prop_";
	/** Constant <code>ASSOC_SUFFIX="assoc_"</code> */
	public static final String ASSOC_SUFFIX = "assoc_";
	/** Constant <code>DT_SUFFIX="dt_"</code> */
	public static final String DT_SUFFIX = "dt_";
	
	
	interface DataListCallBack {
		
		List<Map<String,Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field, FormatMode mode);

	}
	
	interface AttributeExtractorPlugin {

		public static Integer LOW_PRIORITY = 0;
		public static Integer MEDIUM_PRIORITY = 1;
		public static Integer HIGH_PRIORITY = 2;
		
		/**
		 * Extracts the property name from a node based on its type.
		 * 
		 * @param type the type of the node
		 * @param nodeRef the reference to the node
		 * @return the extracted property name
		 */
		String extractPropName(QName type, NodeRef nodeRef);
		
		default String extractPropName(JSONObject jsonEntity) {
			return null;
		}

		String extractMetadata(QName type,NodeRef nodeRef);
		
		/**
		 * Gets the collection of QNames that match the criteria for this extractor.
		 * 
		 * @return a collection of matching QNames
		 */
		Collection<QName> getMatchingTypes();
		
		Integer getPriority(); 

		default boolean matchCriteria(NodeRef nodeRef, Map<String, String> criteriaMap) {
			return false;
		}
		
	}
	
	public static String extractPropName(PermissionService permissionService, NodeService nodeService, QName type,
			NodeRef nodeRef, String extractedPropName) {
		String value;

		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			if (extractedPropName != null) {
				value = extractedPropName;
			} else {
				value = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			}
		} else {
			value = I18NUtil.getMessage("message.becpg.access.denied");
		}

		return value;
	}

	/**
	 * <p>readExtractStructure.</p>
	 *
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	List<AttributeExtractorStructure> readExtractStructure(QName itemType, List<AttributeExtractorField> metadataFields);
	
	/**
	 * <p>extractNodeData.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<AttributeExtractorField> metadataFields, FormatMode mode);
	
	/**
	 * <p>extractNodeData.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @param dataListCallBack a {@link fr.becpg.repo.helper.AttributeExtractorService.DataListCallBack} object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, Map<QName, Serializable> properties, List<AttributeExtractorStructure> metadataFields, FormatMode mode, DataListCallBack dataListCallBack);
	
	/**
	 * <p>extractCommonNodeData.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, Object> extractCommonNodeData(NodeRef nodeRef);
	
    /**
     * <p>getStringValue.</p>
     *
     * @param propertyDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
     * @param value a {@link java.io.Serializable} object.
     * @param propertyFormats a {@link fr.becpg.config.format.PropertyFormats} object.
     * @return a {@link java.lang.String} object.
     */
    String getStringValue(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats);
	
	/**
	 * <p>getTags.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
	String[] getTags(NodeRef nodeRef);

	/**
	 * <p>extractPropertyForReport.</p>
	 *
	 * @param propertyDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
	 * @param value a {@link java.io.Serializable} object.
	 * @param formatData a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	String extractPropertyForReport(PropertyDefinition propertyDef, Serializable value, boolean formatData);
	
	/**
	 * <p>extractPropertyForReport.</p>
	 *
	 * @param propertyDef a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object.
	 * @param value a {@link java.io.Serializable} object.
	 * @param propertyFormats a {@link fr.becpg.config.format.PropertyFormats} object.
	 * @param formatData a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	@Deprecated
	String extractPropertyForReport(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats, boolean formatData);
	
	/**
	 * <p>extractMetadata.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String extractMetadata(QName type, NodeRef entityNodeRef);

	/**
	 * <p>getPersonDisplayName.</p>
	 *
	 * @param userId a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getPersonDisplayName(String userId);


	/**
	 * <p>extractPropName.</p>
	 *
	 * @param v a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String extractPropName(NodeRef v);
	
	/**
	 * <p>extractPropName.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @param v a {@link org.json.JSONObject} object
	 * @return a {@link java.lang.String} object
	 */
	String extractPropName(QName type, JSONObject v);

	/**
	 * <p>extractPropName.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String extractPropName(QName type, NodeRef nodeRef);
	
	/**
	 * <p>extractPropName.</p>
	 *
	 * @param format a {@link java.lang.String} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String extractExpr(String format, NodeRef nodeRef);

	/**
	 * <p>extractSiteId.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String extractSiteId(NodeRef entityNodeRef);

	/**
	 * <p>hasAttributeExtractorPlugin.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	boolean hasAttributeExtractorPlugin(NodeRef nodeRef);

	/**
	 * <p>matchCriteria.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param criteriaMap a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	boolean matchCriteria(NodeRef nodeRef, Map<String, String> criteriaMap);

	
	/**
	 * <p>getPropertyFormats.</p>
	 *
	 * @param json a {@link fr.becpg.config.format.FormatMode} object.
	 * @param useServerLocale a boolean.
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object.
	 */
	PropertyFormats getPropertyFormats(FormatMode json,
			boolean useServerLocale);

	

}
