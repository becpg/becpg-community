package fr.becpg.repo.web.scripts.process;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EntityProcessListPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityProcessListPlugin {
	
	
	/** Constant <code>PROCESS_INSTANCE="processInstance"</code> */
	public static final String PROCESS_INSTANCE = "processInstance";
    /** Constant <code>PROCESS_INSTANCE_TYPE="type"</code> */
    public static final String PROCESS_INSTANCE_TYPE = "type";
    /** Constant <code>PROCESS_INSTANCE_ID="id"</code> */
    public static final String PROCESS_INSTANCE_ID = "id";
    /** Constant <code>PROCESS_INSTANCE_NODEREF="nodeRef"</code> */
    public static final String PROCESS_INSTANCE_NODEREF = "nodeRef";
    /** Constant <code>PROCESS_INSTANCE_NAME="name"</code> */
    public static final String PROCESS_INSTANCE_NAME = "name";
    /** Constant <code>PROCESS_INSTANCE_TITLE="title"</code> */
    public static final String PROCESS_INSTANCE_TITLE = "title";
    /** Constant <code>PROCESS_INSTANCE_IS_ACTIVE="isActive"</code> */
    public static final String PROCESS_INSTANCE_IS_ACTIVE = "isActive";
    /** Constant <code>PROCESS_INSTANCE_STATE="state"</code> */
    public static final String PROCESS_INSTANCE_STATE = "state";
    /** Constant <code>PROCESS_INSTANCE_START_DATE="startDate"</code> */
    public static final String PROCESS_INSTANCE_START_DATE = "startDate";
    /** Constant <code>PROCESS_INSTANCE_MESSAGE="message"</code> */
    public static final String PROCESS_INSTANCE_MESSAGE = "message";
    /** Constant <code>PROCESS_INSTANCE_END_DATE="endDate"</code> */
    public static final String PROCESS_INSTANCE_END_DATE = "endDate";
    /** Constant <code>PROCESS_INSTANCE_DUE_DATE="dueDate"</code> */
    public static final String PROCESS_INSTANCE_DUE_DATE = "dueDate";
    /** Constant <code>PROCESS_INSTANCE_INITIATOR="initiator"</code> */
    public static final String PROCESS_INSTANCE_INITIATOR = "initiator";
    
    /** Constant <code>PERSON_LAST_NAME="lastName"</code> */
    public static final String PERSON_LAST_NAME = "lastName";
    /** Constant <code>PERSON_FIRST_NAME="firstName"</code> */
    public static final String PERSON_FIRST_NAME = "firstName";
    /** Constant <code>PERSON_USER_NAME="userName"</code> */
    public static final String PERSON_USER_NAME = "userName";
    /** Constant <code>PERSON_AVATAR="avatarUrl"</code> */
    public static final String PERSON_AVATAR = "avatarUrl";
	

	/**
	 * <p>buildModel.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<Map<String, Object>> buildModel(NodeRef nodeRef);
	
	/**
	 * <p>getType.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getType();

	
	
	
}
