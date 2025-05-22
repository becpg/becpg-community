package fr.becpg.repo.jscript;

import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.api.BeCPGPublicApi;

/**
 * <p>BeCPGStateHelper class.</p>
 *
 * @author matthieu
 */
@BeCPGPublicApi
public class BeCPGStateHelper extends BaseScopableProcessorExtension {

	/** Constant <code>ACTION_BRANCH_ENTITY="ACTION_BRANCH_ENTITY"</code> */
	public static final String ACTION_BRANCH_ENTITY = "ACTION_BRANCH_ENTITY";
	/** Constant <code>ACTION_COPY_ENTITY="ACTION_COPY_ENTITY"</code> */
	public static final String ACTION_COPY_ENTITY = "ACTION_COPY_ENTITY";
	/** Constant <code>ACTION_FORMULATE_ENTITY="ACTION_FORMULATE_ENTITY"</code> */
	public static final String ACTION_FORMULATE_ENTITY = "ACTION_FORMULATE_ENTITY";
	/** Constant <code>ACTION_CREATE_MINOR_VERSION="ACTION_CREATE_MINOR_VERSION"</code> */
	public static final String ACTION_CREATE_MINOR_VERSION = "ACTION_CREATE_MINOR_VERSION";
	/** Constant <code>ACTION_CREATE_MAJOR_VERSION="ACTION_CREATE_MAJOR_VERSION"</code> */
	public static final String ACTION_CREATE_MAJOR_VERSION = "ACTION_CREATE_MAJOR_VERSION";
	/** Constant <code>ACTION_CREATE_ENTITY="ACTION_CREATE_ENTITY"</code> */
	public static final String ACTION_CREATE_ENTITY = "ACTION_CREATE_ENTITY";
	
	private static Log logger = LogFactory.getLog(BeCPGStateHelper.class);
	

	public static class ActionStateContext implements AutoCloseable {

		private String state;

		public ActionStateContext(NodeRef entityNodeRef, String state) {
			super();
			this.state = state;
			if(logger.isDebugEnabled()) {
				logger.debug("Start action state: " + state);
			}
			TransactionalResourceHelper.incrementCount(getCounterState(state));
			if (entityNodeRef != null) {
				Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(state);
				nodeRefs.add(entityNodeRef);
			}
		}

		@Override
		public void close() {
			if(logger.isDebugEnabled()) {
				logger.debug("End action state: " + state);
			}
			TransactionalResourceHelper.decrementCount(getCounterState(state), false);
		}

		public void addToState(NodeRef entityNodeRef) {
			if (entityNodeRef != null) {
				Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(state);
				nodeRefs.add(entityNodeRef);
			}
			
		}

	}

	/**
	 * <p>onBranchEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.jscript.BeCPGStateHelper.ActionStateContext} object
	 */
	public static ActionStateContext onBranchEntity(NodeRef entityNodeRef) {
		return new ActionStateContext(entityNodeRef, ACTION_BRANCH_ENTITY);
	}

	/**
	 * <p>onCopyEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static void onCopyEntity(NodeRef entityNodeRef) {
		if (TransactionalResourceHelper.getCount(getCounterState(ACTION_BRANCH_ENTITY)) == 0 && TransactionalResourceHelper.getCount(getCounterState(ACTION_CREATE_MAJOR_VERSION)) == 0
				&& TransactionalResourceHelper.getCount(getCounterState(ACTION_CREATE_MINOR_VERSION)) == 0) {
			if (entityNodeRef != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Start action state: " + ACTION_COPY_ENTITY);
				}
				TransactionalResourceHelper.incrementCount(getCounterState(ACTION_COPY_ENTITY));
				Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(ACTION_COPY_ENTITY);
				nodeRefs.add(entityNodeRef);
			}
		}

	}
	

	/**
	 * <p>onCreateEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public static void onCreateEntity(NodeRef entityNodeRef) {
		if (TransactionalResourceHelper.getCount(getCounterState(ACTION_BRANCH_ENTITY)) == 0 && TransactionalResourceHelper.getCount(getCounterState(ACTION_CREATE_MAJOR_VERSION)) == 0
				&& TransactionalResourceHelper.getCount(getCounterState(ACTION_CREATE_MINOR_VERSION)) == 0
				&& TransactionalResourceHelper.getCount(getCounterState(ACTION_COPY_ENTITY)) == 0) {
			if (entityNodeRef != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Start action state: " + ACTION_CREATE_ENTITY);
				}
				Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(ACTION_CREATE_ENTITY);
				nodeRefs.add(entityNodeRef);
			}
		}
		
	}

	private static String getCounterState(String state) {
		return state+"_IDX";
	}

	/**
	 * <p>onMergeEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object
	 * @return a {@link fr.becpg.repo.jscript.BeCPGStateHelper.ActionStateContext} object
	 */
	public static ActionStateContext onMergeEntity(NodeRef entityNodeRef, VersionType versionType) {
		if (VersionType.MAJOR.equals(versionType)) {
			return new ActionStateContext(entityNodeRef, ACTION_CREATE_MAJOR_VERSION);
		}

		return new ActionStateContext(entityNodeRef, ACTION_CREATE_MINOR_VERSION);
	}

	/**
	 * <p>onFormulateEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.jscript.BeCPGStateHelper.ActionStateContext} object
	 */
	public static ActionStateContext onFormulateEntity(NodeRef entityNodeRef) {
		return new ActionStateContext(entityNodeRef, ACTION_FORMULATE_ENTITY);
	}

	/**
	 * <p>isOnMergeEntity.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnMergeEntity(ScriptNode entityNode) {
		return isOnMergeEntity(entityNode.getNodeRef());
	}
	
	/**
	 * <p>isOnCreateEntity.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnCreateEntity(ScriptNode entityNode) {
		return isOnCreateEntity(entityNode.getNodeRef());
	}

	/**
	 * <p>isOnMergeMajorVersion.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnMergeMajorVersion(ScriptNode entityNode) {
		return isOnMergeMajorVersion(entityNode.getNodeRef());
	}

	/**
	 * <p>isOnMergeMinorVersion.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnMergeMinorVersion(ScriptNode entityNode) {
		return isOnMergeMinorVersion(entityNode.getNodeRef());
	}

	/**
	 * <p>isOnFormulateEntity.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnFormulateEntity(ScriptNode entityNode) {
		return isOnFormulateEntity(entityNode.getNodeRef());
	}

	/**
	 * <p>isOnCopyEntity.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnCopyEntity(ScriptNode entityNode) {
		return isOnCopyEntity(entityNode.getNodeRef());
	}

	/**
	 * <p>isOnBranchEntity.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isOnBranchEntity(ScriptNode entityNode) {
		return isOnBranchEntity(entityNode.getNodeRef());
	}

	/**
	 * <p>isOnMergeEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnMergeEntity(NodeRef entityNodeRef) {
		return isOnMergeMajorVersion(entityNodeRef) || isOnMergeMinorVersion(entityNodeRef);
	}

	/**
	 * <p>isOnCreateEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnCreateEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_CREATE_ENTITY);
	}
	
	/**
	 * <p>isOnMergeMinorVersion.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnMergeMinorVersion(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_CREATE_MINOR_VERSION);
	}

	/**
	 * <p>isOnMergeMajorVersion.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnMergeMajorVersion(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_CREATE_MAJOR_VERSION);
	}

	/**
	 * <p>isOnCopyEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnCopyEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_COPY_ENTITY);
	}

	/**
	 * <p>isOnFormulateEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnFormulateEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_FORMULATE_ENTITY);
	}

	/**
	 * <p>isOnBranchEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isOnBranchEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_BRANCH_ENTITY);
	}

	private static boolean hasEntity(NodeRef entityNodeRef, String state) {
		Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(state);
		return nodeRefs.contains(entityNodeRef);
	}


}
