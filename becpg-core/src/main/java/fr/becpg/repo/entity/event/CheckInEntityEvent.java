package fr.becpg.repo.entity.event;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.context.ApplicationEvent;

/**
 * CheckIn entity event
 * @author quere
 *
 */
public class CheckInEntityEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6872600105098661778L;	
	private NodeRef entityNodeRef;
	
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public CheckInEntityEvent(Object source, NodeRef entityNodeRef) {
		super(source);
		
		this.entityNodeRef = entityNodeRef;
	}

}
