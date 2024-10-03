package fr.becpg.repo.entity;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.BeCPGModel.EntityFormat;


/**
 * <p>EntityFormatService interface.</p>
 *
 * @author matthieu
 */
public interface EntityFormatService {
	
	/**
	 * <p>updateEntityFormat.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param format a {@link fr.becpg.model.BeCPGModel.EntityFormat} object
	 * @param data a {@link java.lang.String} object
	 */
	public void updateEntityFormat(NodeRef entityNodeRef, EntityFormat format, String data);
	
	/**
	 * <p>getEntityData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String getEntityData(NodeRef entityNodeRef);
	
	/**
	 * <p>getEntityFormat.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String getEntityFormat(NodeRef entityNodeRef);
	
	/**
	 * <p>convertToFormat.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toFormat a {@link fr.becpg.model.BeCPGModel.EntityFormat} object
	 */
	public void convertToFormat(NodeRef entityNodeRef, EntityFormat	toFormat);
	
	/**
	 * <p>generateEntityData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toFormat a {@link fr.becpg.model.BeCPGModel.EntityFormat} object
	 * @return a {@link java.lang.String} object
	 */
	public String generateEntityData(NodeRef entityNodeRef, EntityFormat toFormat);
	
	/**
	 * <p>generateEntityData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toFormat a {@link fr.becpg.model.BeCPGModel.EntityFormat} object
	 * @param extraParams a {@link java.util.Map} object
	 * @return a {@link java.lang.String} object
	 */
	public String generateEntityData(NodeRef entityNodeRef, EntityFormat toFormat, Map<String, Object> extraParams);

	/**
	 * <p>createOrUpdateEntityFromJson.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entityJson a {@link java.lang.String} object
	 */
	public void createOrUpdateEntityFromJson(NodeRef entityNodeRef, String entityJson);

}
