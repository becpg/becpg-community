package fr.becpg.repo.quality;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.QualityControlData;

public interface QualityControlDAO {

	public NodeRef create(NodeRef parentNodeRef, QualityControlData qcData);	
	public void update(NodeRef qcNodeRef, QualityControlData qcData);		
	public QualityControlData find(NodeRef qcNodeRef);
	public void delete(NodeRef qcNodeRef);
}
