package fr.becpg.repo.jscript;

import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeCPGStateHelper extends BaseScopableProcessorExtension {

	public static final String ACTION_BRANCH_ENTITY = "ACTION_BRANCH_ENTITY";
	public static final String ACTION_COPY_ENTITY = "ACTION_COPY_ENTITY";
	public static final String ACTION_FORMULATE_ENTITY = "ACTION_FORMULATE_ENTITY";
	public static final String ACTION_CREATE_MINOR_VERSION = "ACTION_CREATE_MINOR_VERSION";
	public static final String ACTION_CREATE_MAJOR_VERSION = "ACTION_CREATE_MAJOR_VERSION";
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

	public static ActionStateContext onBranchEntity(NodeRef entityNodeRef) {
		return new ActionStateContext(entityNodeRef, ACTION_BRANCH_ENTITY);
	}

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

	public static ActionStateContext onMergeEntity(NodeRef entityNodeRef, VersionType versionType) {
		if (VersionType.MAJOR.equals(versionType)) {
			return new ActionStateContext(entityNodeRef, ACTION_CREATE_MAJOR_VERSION);
		}

		return new ActionStateContext(entityNodeRef, ACTION_CREATE_MINOR_VERSION);
	}

	public static ActionStateContext onFormulateEntity(NodeRef entityNodeRef) {
		return new ActionStateContext(entityNodeRef, ACTION_FORMULATE_ENTITY);
	}

	public boolean isOnMergeEntity(ScriptNode entityNode) {
		return isOnMergeEntity(entityNode.getNodeRef());
	}
	
	public boolean isOnCreateEntity(ScriptNode entityNode) {
		return isOnCreateEntity(entityNode.getNodeRef());
	}

	public boolean isOnMergeMajorVersion(ScriptNode entityNode) {
		return isOnMergeMajorVersion(entityNode.getNodeRef());
	}

	public boolean isOnMergeMinorVersion(ScriptNode entityNode) {
		return isOnMergeMinorVersion(entityNode.getNodeRef());
	}

	public boolean isOnFormulateEntity(ScriptNode entityNode) {
		return isOnFormulateEntity(entityNode.getNodeRef());
	}

	public boolean isOnCopyEntity(ScriptNode entityNode) {
		return isOnCopyEntity(entityNode.getNodeRef());
	}

	public boolean isOnBranchEntity(ScriptNode entityNode) {
		return isOnBranchEntity(entityNode.getNodeRef());
	}

	public boolean isOnMergeEntity(NodeRef entityNodeRef) {
		return isOnMergeMajorVersion(entityNodeRef) || isOnMergeMinorVersion(entityNodeRef);
	}

	public boolean isOnCreateEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_CREATE_ENTITY);
	}
	
	public boolean isOnMergeMinorVersion(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_CREATE_MINOR_VERSION);
	}

	public boolean isOnMergeMajorVersion(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_CREATE_MAJOR_VERSION);
	}

	public boolean isOnCopyEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_COPY_ENTITY);
	}

	public boolean isOnFormulateEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_FORMULATE_ENTITY);
	}

	public boolean isOnBranchEntity(NodeRef entityNodeRef) {
		return hasEntity(entityNodeRef, ACTION_BRANCH_ENTITY);
	}

	private boolean hasEntity(NodeRef entityNodeRef, String state) {
		Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(state);
		return nodeRefs.contains(entityNodeRef);
	}


}
