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
package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * Helper used to manage a property
 * @author querephi
 *
 */
public interface AttributeExtractorService {

	public interface DataListCallBack {
		
		List<Map<String,Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field);


	}
	
	public enum AttributeExtractorMode {
		SEARCH,JSON,CSV;
	}

	
	
	
	public String getStringValue(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats);
	
	public String getPersonDisplayName(String userId);

	public String convertDateValue(Serializable value);

	public List<AttributeExtractorStructure> readExtractStructure(QName itemType, List<String> metadataFields);
	
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, AttributeExtractorMode mode );
	
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, Map<QName, Serializable> properties,  List<AttributeExtractorStructure> metadataFields, AttributeExtractorMode mode, DataListCallBack dataListCallBack);

	public String getDisplayPath(NodeRef nodeRef);

	public String[] getTags(NodeRef nodeRef);

	public String formatDate(Date date);

	public String extractMetadata(QName type, NodeRef nodeRef);

	public PropertyFormats getPropertyFormats();

	public String extractSiteId(NodeRef entityNodeRef);

	@Deprecated 
	//Use convertDateValue instead
	public Serializable getProperty(NodeRef nodeRef, QName propName);

	public String extractPropertyForReport(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats);

	public String extractAssociationForReport(AssociationRef assocRef);
}
