package fr.becpg.repo.entity.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.mail.BeCPGMailService;

@Service("simulationService")
public class EntitySimulationServiceImpl implements EntitySimulationService {

	@Autowired
	@Qualifier("defaultAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;

	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private BeCPGMailService beCPGMailService;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EntitySimulationPlugin[] entitySimulationPlugins;

	private static Log logger = LogFactory.getLog(EntitySimulationServiceImpl.class);

	private class AsyncCreateSimulationNodeRefsCommand implements Runnable {
		
		public static final String MAIL_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:asynchrone-actions-email.html.ftl";
		private static final String ARG_ACTION_TYPE = "actionType";
		private static final String ARG_ACTION_STATE = "actionState";
		private static final String ARG_ACTION_DESTINATION = "destination";
		private static final String ARG_ACTION_DESTINATION_PATH = "path";
		private static final String ARG_ACTION_RUN_TIME = "runTime";
		private String subject = "[Notification]" + I18NUtil.getMessage("message.simulate-entity.subject");
		
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
			StopWatch watch = new StopWatch();
			watch.start();
			boolean runState = false;
			
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
				
				runState = true;

			} catch (Exception e) {
				if (e instanceof ConcurrencyFailureException) {
					throw (ConcurrencyFailureException) e;
				}
				logger.error("Unable to simulate entities ", e);
				
			} finally {
				// Send Mail after simulation 
				watch.stop();
				Path folderPath = nodeService.getPath(destNodeRef);
				String destinationPath = folderPath.subPath(2, folderPath.size()-1).toDisplayPath(nodeService, permissionService) + "/"
						+ nodeService.getProperty(destNodeRef, ContentModel.PROP_NAME);
								
				Map<String, Object> templateArgs = new HashMap<>();
				templateArgs.put(ARG_ACTION_TYPE, "Simulate");
				templateArgs.put(ARG_ACTION_STATE, runState);
				templateArgs.put(ARG_ACTION_DESTINATION, destNodeRef);
				templateArgs.put(ARG_ACTION_DESTINATION_PATH, destinationPath);
				templateArgs.put(ARG_ACTION_RUN_TIME, watch.getTotalTimeSeconds());
				
				List<NodeRef> recipientNodeRefs = new ArrayList<>();
				recipientNodeRefs.add(personService.getPerson(userName));
				Map<String, Object> templateModel = new HashMap<>();
				templateModel.put("args", templateArgs);
				
				
				AuthenticationUtil.runAsSystem(()->{
					beCPGMailService.sendMail(recipientNodeRefs, subject, MAIL_TEMPLATE, templateModel, false);
					return null;
				});
				
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
