package fr.becpg.repo.entity;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.BeCPGModel.EntityFormat;


public interface EntityFormatService {
	
	public void updateEntityFormat(NodeRef entityNodeRef, EntityFormat format, String data);
	
	public String getEntityData(NodeRef entityNodeRef);
	
	public String getEntityFormat(NodeRef entityNodeRef);
	
	public void convertToFormat(NodeRef entityNodeRef, EntityFormat	toFormat);
	
	public String generateEntityData(NodeRef entityNodeRef, EntityFormat toFormat);
	
	public String generateEntityData(NodeRef entityNodeRef, EntityFormat toFormat, Map<String, Object> extraParams);

	public void createOrUpdateEntityFromJson(NodeRef entityNodeRef, String entityJson);

}
