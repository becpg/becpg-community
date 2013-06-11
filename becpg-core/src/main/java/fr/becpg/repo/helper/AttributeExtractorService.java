package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.config.format.PropertyFormats;

/**
 * Helper used to manage a property
 * @author querephi
 *
 */
public interface AttributeExtractorService {

	public interface DataListCallBack {

		List<Map<String,Object>> extractDataListField(NodeRef entityNodeRef, QName dataListQname, List<String> metadataFields);

		Map<String, Object> extractEntityField(NodeRef entityListNodeRef, QName entityTypeQname, List<String> metadataFields);

	}

	public String getStringValue(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats);

	public String getPersonDisplayName(String userId);

	public Serializable getProperty(NodeRef nodeRef, QName propQname);
	
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, boolean isSearch);

	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, boolean isSearch, DataListCallBack callback);

	public String getDisplayPath(NodeRef nodeRef);

	public String[] getTags(NodeRef nodeRef);

	public String formatDate(Date date);

	public String extractMetadata(QName type, NodeRef nodeRef);

	public PropertyFormats getPropertyFormats();

	public String extractSiteId(NodeRef entityNodeRef);

	
}
