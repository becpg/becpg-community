package fr.becpg.repo.admin.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author matthieu
 * 
 */
public class PLMUndeployWorkflowPatch extends AbstractPatch implements ApplicationContextAware {
	private static final String MSG_SUCCESS = "patch.bcpg.plm.undeployWorkflowPatch";

	private static Log logger = LogFactory.getLog(PLMUndeployWorkflowPatch.class);

	private WorkflowService workflowService;

	private ApplicationContext applicationContext;

	public static final String PROJECT_OLD_NPD_URI = "http://www.alfresco.org/model/npd-workflow/1.0";

	public static final String PROJECT_OLD_WF_URI = "http://www.alfresco.org/model/project-workflow/1.0";

	public static final String PROJECT_NPD_URI = "http://www.becpg.fr/model/npd-workflow/1.0";

	public static final String PROJECT_WF_URI = "http://www.becpg.fr/model/project-workflow/1.0";

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public WorkflowService getWorkflowService() {
		return workflowService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	protected String applyInternal() throws Exception {

		// NamespaceDAO namespaceDAO = (NamespaceDAO)
		// applicationContext.getBean("namespaceDAO");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() {

			@Override
			public Object execute() throws Throwable {

				QNameDAO qNameDAO = (QNameDAO) applicationContext.getBean("qnameDAO");

				if (qNameDAO.getNamespace(PROJECT_WF_URI) == null) {

					qNameDAO.getOrCreateNamespace(PROJECT_OLD_WF_URI);
					qNameDAO.updateNamespace(PROJECT_OLD_WF_URI, PROJECT_WF_URI);

				}

				// if (namespaceDAO.getPrefixes(PROJECT_WF_URI)==null ||
				// namespaceDAO.getPrefixes(PROJECT_WF_URI).isEmpty()){
				// namespaceDAO.addURI(PROJECT_WF_URI);
				// namespaceDAO.removePrefix("pjtwf");
				// namespaceDAO.addPrefix("pjtwf", PROJECT_WF_URI);
				// }

				if (qNameDAO.getNamespace(PROJECT_NPD_URI) == null) {
					qNameDAO.getOrCreateNamespace(PROJECT_OLD_NPD_URI);
					qNameDAO.updateNamespace(PROJECT_OLD_NPD_URI, PROJECT_NPD_URI);
				}

				// if (namespaceDAO.getPrefixes(PROJECT_NPD_URI)==null ||
				// namespaceDAO.getPrefixes(PROJECT_NPD_URI).isEmpty()){
				// namespaceDAO.addURI(PROJECT_NPD_URI);
				// namespaceDAO.removePrefix("npdwf");
				// namespaceDAO.addPrefix("npdwf", PROJECT_NPD_URI);
				// }

				return null;
			}

		}, false, false);

		try {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() {

				@Override
				public Object execute() throws Throwable {
					WorkflowDeployer deployer = (WorkflowDeployer) applicationContext.getBean("bcpg.project.workflowDeployer");
					deployer.init();
					deployer = (WorkflowDeployer) applicationContext.getBean("bcpg.plm.workflowDeployer");
					deployer.init();
					return null;
				}

			}, false, false);

		} catch (Throwable e) {
			logger.error(e, e);
		}

		logger.info("Apply for tenant : " + tenantAdminService.getCurrentUserDomain());

		//

		for (final String toRemove : new String[] { "jbpm$npdwf:newProductDevelopmentWF", "jbpm$bcpgwf:productValidationWF", "jbpm$bcpgwf:adhoc" }) {

			try {
				logger.info("Getting def for : " + toRemove);
				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() {

					@Override
					public Object execute() throws Throwable {
						for (WorkflowDefinition def : workflowService.getAllDefinitionsByName(toRemove)) {
							logger.info("Undeploy : " + def.getId() + " " + def.getName() + " " + def.getTitle());
							workflowService.undeployDefinition(def.getId());
						}
						return null;
					}

				}, false, false);

			} catch (Throwable e) {
				logger.error(e, e);
			}
		}

		// try {
		// transactionService.getRetryingTransactionHelper().doInTransaction(new
		// RetryingTransactionCallback<Object>() {
		//
		// @Override
		// public Object execute() throws Throwable {
		//
		// for (WorkflowDefinition def : workflowService.getAllDefinitions()) {
		// if (def.getId().contains("projectNewProduct")) {
		// logger.info("Undeploy : " + def.getId() + " " + def.getName() + " " +
		// def.getTitle());
		// workflowService.undeployDefinition(def.getId());
		// }
		// }
		// return null;
		// }
		//
		// }, false, false);
		//
		// } catch (Throwable e) {
		// logger.error(e, e);
		// }

		// namespaceDAO.removeURI(PROJECT_NPD_URI);
		// namespaceDAO.removeURI(PROJECT_WF_URI);

		// try {
		// WorkflowDeployer deployer = (WorkflowDeployer)
		// applicationContext.getBean("bcpg.project.workflowDeployer");
		// deployer.init();
		// deployer = (WorkflowDeployer)
		// applicationContext.getBean("bcpg.plm.workflowDeployer");
		// deployer.init();
		// } catch (Throwable e) {
		// logger.error(e, e);
		// }

		logger.warn("PLEASE SET UNDER alfresco-global.properties");
		logger.warn("system.workflow.engine.jbpm.definitions.visible=false");
		logger.warn("system.workflow.engine.jbpm.enabled=false");

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
