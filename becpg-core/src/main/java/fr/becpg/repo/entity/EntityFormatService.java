package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel.EntityFormat;


public interface EntityFormatService {
	
	public void setDatalistFormat(NodeRef entityNodeRef, QName dataListType, EntityFormat format);
	
	public void setDatalistFormat(NodeRef listNodeRef, EntityFormat format);
	
	public String getDatalistFormat(NodeRef entityNodeRef, QName dataListType);

	public String getDatalistFormat(NodeRef listNodeRef);

	public void setDataListData(NodeRef entityNodeRef, QName dataListType, String data);
	
	public void setDataListData(NodeRef listNodeRef, String data);

	public String getDataListData(NodeRef listNodeRef);
	
	public String getDataListData(NodeRef entityNodeRef, QName dataListType);

	public void convertDataListFormat(NodeRef entityNodeRef, QName dataListType, EntityFormat toFormat);
	
	public void convertDataListFormat(NodeRef listNodeRef, EntityFormat toFormat);
	
	public void setEntityData(NodeRef entityNodeRef, String data);
	
	public String getEntityData(NodeRef entityNodeRef);
	
	public void setEntityFormat(NodeRef entityNodeRef, EntityFormat format);
	
	public String getEntityFormat(NodeRef entityNodeRef);
	
	public void convert(NodeRef entityNodeRef, EntityFormat	toFormat);
	
	public void convert(NodeRef from, NodeRef to, EntityFormat toFormat);
	
	public String extractEntityData(NodeRef entityNodeRef, EntityFormat toFormat);

	public void createOrUpdateEntityFromJson(NodeRef entityNodeRef, String entityJson);

	public NodeRef convertVersionHistoryNodeRef(NodeRef from);


}
