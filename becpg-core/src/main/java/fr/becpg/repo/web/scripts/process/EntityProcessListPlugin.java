package fr.becpg.repo.web.scripts.process;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.config.format.PropertyFormats;

public interface EntityProcessListPlugin {
	
	
	public static final String PROCESS_INSTANCE = "processInstance";
    public static final String PROCESS_INSTANCE_TYPE = "type";
    public static final String PROCESS_INSTANCE_ID = "id";
    public static final String PROCESS_INSTANCE_NODEREF = "nodeRef";
    public static final String PROCESS_INSTANCE_NAME = "name";
    public static final String PROCESS_INSTANCE_TITLE = "title";
    public static final String PROCESS_INSTANCE_IS_ACTIVE = "isActive";
    public static final String PROCESS_INSTANCE_STATE = "state";
    public static final String PROCESS_INSTANCE_START_DATE = "startDate";
    public static final String PROCESS_INSTANCE_MESSAGE = "message";
    public static final String PROCESS_INSTANCE_END_DATE = "endDate";
    public static final String PROCESS_INSTANCE_DUE_DATE = "dueDate";
    public static final String PROCESS_INSTANCE_INITIATOR = "initiator";
    
    public static final String PERSON_LAST_NAME = "lastName";
    public static final String PERSON_FIRST_NAME = "firstName";
    public static final String PERSON_USER_NAME = "userName";
    public static final String PERSON_AVATAR = "avatarUrl";
	

	List<Map<String, Object>> buildModel(NodeRef nodeRef);
	
	String getType();

	
	
	
}
