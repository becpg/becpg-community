package fr.becpg.repo.entity.version;

import java.util.Calendar;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>VersionCleanerJob class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class VersionCleanerJob extends AbstractScheduledLockedJob implements Job {

	private static final Log logger = LogFactory.getLog(VersionCleanerJob.class);

	private EntityFormatService entityFormatService;

	private TransactionService transactionService;

	private NodeService nodeService;

	private TenantAdminService tenantAdminService;

	private AssociationService associationService;

	private static final int MAX_PROCESSED_NDOES = 10;

	public VersionCleanerJob() {
		super();
	}

	@Override
	public void executeJob(JobExecutionContext context) throws JobExecutionException {

		logger.info("Start of Version cleaner Job.");

		JobDataMap jobData = context.getJobDetail().getJobDataMap();

		entityFormatService = (EntityFormatService) jobData.get("entityFormatService");
		transactionService = (TransactionService) jobData.get("transactionService");
		nodeService = (NodeService) jobData.get("nodeService");
		tenantAdminService = (TenantAdminService) jobData.get("tenantAdminService");
		associationService = (AssociationService) jobData.get("associationService");

		AuthenticationUtil.runAsSystem(this::cleanVersions);

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(() -> {
					cleanVersions();
					return null;
				}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		}

		logger.info("End of Version cleaner Job.");
	}

	private boolean cleanVersions() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);

		List<NodeRef> temporaryNodes = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)
				.withAspect(ContentModel.ASPECT_TEMPORARY).inDB().ftsLanguage().maxResults(MAX_PROCESSED_NDOES)
				.andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'")
				.list();

		String tenantName = "default";

		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())) {
			tenantName = tenantAdminService.getTenant(tenantAdminService.getCurrentUserDomain()).getTenantDomain();
		}

		for (NodeRef temporaryNode : temporaryNodes) {
			deleteNode(tenantName, temporaryNode);
		}

		int processedNodes = 0;
		int skippedNodes = 0;

		List<NodeRef> notConvertedNodes;
		
		do {
			notConvertedNodes = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)
					.excludeAspect(BeCPGModel.ASPECT_ENTITY_FORMAT).excludeAspect(ContentModel.ASPECT_TEMPORARY).inDB().ftsLanguage()
					.maxResults(MAX_PROCESSED_NDOES - processedNodes + skippedNodes)
					.andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'")
					.list();

			for (NodeRef notConvertedNode : notConvertedNodes) {

				long start = System.currentTimeMillis();

				String name = (String) nodeService.getProperty(notConvertedNode, ContentModel.PROP_NAME);

				boolean convertNode = true;
				
				for (NodeRef source : associationService.getSourcesAssocs(notConvertedNode, QName.createQName(BeCPGModel.BECPG_URI, "compoListProduct"))) {
					NodeRef datalistFolder = nodeService.getPrimaryParent(source).getParentRef();
					NodeRef entitylistFolder = nodeService.getPrimaryParent(datalistFolder).getParentRef();
					NodeRef parentProduct = nodeService.getPrimaryParent(entitylistFolder).getParentRef();

					if (nodeService.hasAspect(parentProduct, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& !nodeService.hasAspect(parentProduct, BeCPGModel.ASPECT_ENTITY_FORMAT)
							&& !nodeService.hasAspect(parentProduct, ContentModel.ASPECT_TEMPORARY)) {
						
						String parentName = (String) nodeService.getProperty(parentProduct, ContentModel.PROP_NAME);
						logger.info("Couldn't convert entity '" + name + "' because it is used by entity '" + parentName + "' which needs to be converted first.");
					}
					
					convertNode = false;
					skippedNodes++;
				}
				
				NodeRef parentNode = nodeService.getPrimaryParent(notConvertedNode).getParentRef();
				
				String parentName = (String) nodeService.getProperty(parentNode, ContentModel.PROP_NAME);
				
				NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);

				if (!nodeService.exists(originalNode)) {
					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						nodeService.deleteNode(notConvertedNode);
						return null;
					}, false, false);
					
					processedNodes++;
					convertNode = false;
					logger.info("deleted node : " + notConvertedNode + " because the reference node doesn't exist");
				}
				
				if (convertNode) {
					NodeRef convertedNode = entityFormatService.convertVersionHistoryNodeRef(notConvertedNode);
					if (convertedNode != null) {
						processedNodes++;
						long timeElapsed = System.currentTimeMillis() - start;
						logger.info("Converted entity '" + name + "', from " + notConvertedNode + " to " + convertedNode + ", tenant : " + tenantName
								+ ", time elapsed : " + timeElapsed + " ms");
					}
				}
				
			}
		} while (processedNodes < MAX_PROCESSED_NDOES && !notConvertedNodes.isEmpty());

		return true;
	}

	private void deleteNode(String tenantName, NodeRef temporaryNode) {
		long start = System.currentTimeMillis();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			if (nodeService.exists(temporaryNode)) {
				NodeRef parentNode = nodeService.getPrimaryParent(temporaryNode).getParentRef();
				
				if (parentNode != null && nodeService.exists(parentNode)) {
					nodeService.deleteNode(parentNode);
					long timeElapsed = System.currentTimeMillis() - start;
					
					logger.info("deleted node : " + parentNode + ", tenant : " + tenantName + ", time elapsed : " + timeElapsed + " ms");
				}
			}

			
			return null;
		}, false, false);

	}

}
