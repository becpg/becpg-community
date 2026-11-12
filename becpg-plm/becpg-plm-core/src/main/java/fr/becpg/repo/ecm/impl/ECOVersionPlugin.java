package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>
 * ECOVersionPlugin class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ECOVersionPlugin implements EntityVersionPlugin {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private ECOService ecoService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	/**
	 * <p>deleteOnApply.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	private Boolean deleteOnApply() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.eco.automatic.deleteOnApply"));
	}

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	@Autowired
	private TenantAdminService tenantAdminService;

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ECOVersionPlugin.class);

	/** {@inheritDoc} */
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	/** {@inheritDoc} */
	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	/** {@inheritDoc} */
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The change order is generated in a thread started once the calling transaction is committed, so that it
	 * reads the merged entity and its new version, never the state before the merge.</p>
	 */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effectiveDate) {
		String ecoUserName = getEcoUserName();
		Runnable ecoGeneration = () -> AuthenticationUtil.runAs(() -> {
			generateAndApplyEco(entityNodeRef, versionType, description, effectiveDate);
			return null;
		}, ecoUserName);

		if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_NONE) {
			startEcoGeneration(ecoGeneration);
		} else {
			AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
				@Override
				public void afterCommit() {
					startEcoGeneration(ecoGeneration);
				}
			});
		}
	}

	/**
	 * @return the system user of the current tenant, who generates the change order
	 */
	private String getEcoUserName() {
		String userName = AuthenticationUtil.getSystemUserName();
		if (tenantAdminService.isEnabled()) {
			userName = tenantAdminService.getDomainUser(userName, tenantAdminService.getCurrentUserDomain());
		}
		return userName;
	}

	/**
	 * Runs the change order generation in its own thread, logging its failure instead of losing it.
	 *
	 * @param ecoGeneration the generation to run
	 */
	private void startEcoGeneration(Runnable ecoGeneration) {
		Thread asyncEcoGenerator = new Thread(() -> {
			try {
				ecoGeneration.run();
			} catch (RuntimeException e) {
				logger.error("Cannot generate the impactWUsed change order", e);
			}
		});
		asyncEcoGenerator.start();
	}

	/**
	 * Creates an automatic change order replacing the entity in its where-used, then applies it.
	 *
	 * @param entityNodeRef the merged entity
	 * @param versionType the version type of the merge
	 * @param description the description of the change order
	 * @param effectiveDate the effective date of the change order, null for none
	 */
	private void generateAndApplyEco(NodeRef entityNodeRef, VersionType versionType, String description, Date effectiveDate) {
		String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
		String versionLabel = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL);

		if (versionLabel == null) {
			versionLabel = RepoConsts.INITIAL_VERSION;
		}

		String ecoName = generateEcoName(name + "_v" + versionLabel);

		NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> createChangeOrder(entityNodeRef, ecoName, versionType, description, effectiveDate), false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> ecoService.apply(ecoNodeRef, deleteOnApply(), true, false), false,
				true);
	}

	/**
	 * Creates the change order under a name not yet used in the change order folder.
	 *
	 * @param entityNodeRef the merged entity
	 * @param ecoName the wanted name of the change order
	 * @param versionType the version type of the merge
	 * @param description the description of the change order
	 * @param effectiveDate the effective date of the change order, null for none
	 * @return the change order
	 */
	private NodeRef createChangeOrder(NodeRef entityNodeRef, String ecoName, VersionType versionType, String description, Date effectiveDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new impactWUsed change order");
		}
		NodeRef changeOrderFolder = getChangeOrderFolder();
		String availableEcoName = repoService.getAvailableName(changeOrderFolder, ecoName, false);
		ChangeOrderData changeOrderData = (ChangeOrderData) alfrescoRepository.create(changeOrderFolder,
				new ChangeOrderData(availableEcoName, ECOState.Automatic, ChangeOrderType.ImpactWUsed, null));

		changeOrderData.setDescription(description);
		changeOrderData.setEcoState(ECOState.InProgress);
		changeOrderData.setEffectiveDate(effectiveDate);

		List<ReplacementListDataItem> replacementList = changeOrderData.getReplacementList();

		if (replacementList == null) {
			replacementList = new ArrayList<>();
		}
		RevisionType revisionType = VersionType.MAJOR.equals(versionType) ? RevisionType.Major : RevisionType.Minor;

		replacementList.add(new ReplacementListDataItem(revisionType, Collections.singletonList(entityNodeRef), entityNodeRef, 100));

		if (logger.isDebugEnabled()) {
			logger.debug("Adding nodeRef " + entityNodeRef + " to automatic change order :" + changeOrderData.getName());
			logger.debug("Revision type : " + revisionType);
		}

		changeOrderData.setReplacementList(replacementList);
		alfrescoRepository.save(changeOrderData);

		return changeOrderData.getNodeRef();
	}

	/**
	 * <p>getChangeOrderFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef getChangeOrderFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
	}
	

	/**
	 * <p>generateEcoName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String generateEcoName(String name) {
		return name + "-" + I18NUtil.getMessage("plm.ecm.current.name", new Date());
	}

}
