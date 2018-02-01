package fr.becpg.repo.entity.simulation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

@Service("simulationService")
public class EntitySimulationServiceImpl implements EntitySimulationService {

	@Autowired
	@Qualifier("defaultAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private EntitySimulationPlugin[] entitySimulationPlugins;

	private static Log logger = LogFactory.getLog(EntitySimulationServiceImpl.class);

	private class AsyncCreateSimulationNodeRefsCommand implements Runnable {

		private final NodeRef destNodeRef;
		private final List<NodeRef> nodeRefs;
		private final String mode;
		private final String userName;

		public AsyncCreateSimulationNodeRefsCommand(NodeRef destNodeRef, List<NodeRef> nodeRefs, String mode) {
			super();
			this.destNodeRef = destNodeRef;
			this.nodeRefs = nodeRefs;
			this.mode = mode;
			this.userName = AuthenticationUtil.getFullyAuthenticatedUser();
		}

		@Override
		public void run() {
			try {

				AuthenticationUtil.runAs(() -> {

					EntitySimulationPlugin entitySimulationPlugin = findPlugin(mode);

					if (entitySimulationPlugin != null) {
						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							return entitySimulationPlugin.simulateNodeRefs(destNodeRef, nodeRefs);

						}, false, true);

						return true;
					}
					return false;

				}, userName);

			} catch (Exception e) {
				if (e instanceof ConcurrencyFailureException) {
					throw (ConcurrencyFailureException) e;
				}
				logger.error("Unable to simulate entities ", e);
			}

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			result = (prime * result) + ((destNodeRef == null) ? 0 : destNodeRef.hashCode());
			result = (prime * result) + ((mode == null) ? 0 : mode.hashCode());
			result = (prime * result) + ((nodeRefs == null) ? 0 : nodeRefs.hashCode());
			result = (prime * result) + ((userName == null) ? 0 : userName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AsyncCreateSimulationNodeRefsCommand other = (AsyncCreateSimulationNodeRefsCommand) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (destNodeRef == null) {
				if (other.destNodeRef != null) {
					return false;
				}
			} else if (!destNodeRef.equals(other.destNodeRef)) {
				return false;
			}
			if (mode == null) {
				if (other.mode != null) {
					return false;
				}
			} else if (!mode.equals(other.mode)) {
				return false;
			}
			if (nodeRefs == null) {
				if (other.nodeRefs != null) {
					return false;
				}
			} else if (!nodeRefs.equals(other.nodeRefs)) {
				return false;
			}
			if (userName == null) {
				if (other.userName != null) {
					return false;
				}
			} else if (!userName.equals(other.userName)) {
				return false;
			}
			return true;
		}

		private AsyncCreateSimulationNodeRefsCommand getOuterType() {
			return AsyncCreateSimulationNodeRefsCommand.this;
		}

	}

	private EntitySimulationPlugin findPlugin(String mode) {
		for (EntitySimulationPlugin entitySimulationPlugin : entitySimulationPlugins) {
			if (entitySimulationPlugin.accept(mode)) {
				return entitySimulationPlugin;
			}
		}
		logger.warn("No EntitySimulationPlugin found for:" + mode);
		return null;
	}

	@Override
	public NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef destNodeRef) {
		EntitySimulationPlugin entitySimulationPlugin = findPlugin(EntitySimulationPlugin.SIMPLE_MODE);

		List<NodeRef> ret = null;

		if (entitySimulationPlugin != null) {
			ret = entitySimulationPlugin.simulateNodeRefs(destNodeRef, Arrays.asList(entityNodeRef));

		}

		return (ret != null) && !ret.isEmpty() ? ret.get(0) : null;

	}

	@Override
	public void simuleDataListItems(NodeRef entityNodeRef, List<NodeRef> dataListItemsNodeRefs) {

		EntitySimulationPlugin entitySimulationPlugin = findPlugin(EntitySimulationPlugin.DATALIST_MODE);
		if (entitySimulationPlugin != null) {
			entitySimulationPlugin.simulateNodeRefs(entityNodeRef, dataListItemsNodeRefs);
		}

	}

	@Override
	public NodeRef createSimulationNodeRefs(List<NodeRef> nodeRefs, NodeRef destNodeRef, String mode) {
		Runnable command = new AsyncCreateSimulationNodeRefsCommand(destNodeRef, nodeRefs, mode);
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			logger.warn("AsyncCreateSimulationNodeRefsCommand job already in queue for " + destNodeRef);
			logger.info("AsyncCreateSimulationNodeRefsCommand active task size " + threadExecuter.getActiveCount());
			logger.info("AsyncCreateSimulationNodeRefsCommand queue size " + threadExecuter.getTaskCount());
		}
		return destNodeRef;
	}

}
