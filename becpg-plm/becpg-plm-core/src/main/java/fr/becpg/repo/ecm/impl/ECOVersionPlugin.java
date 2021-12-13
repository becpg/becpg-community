package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
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

	@Value("${beCPG.eco.automatic.deleteOnApply}")
	private Boolean deleteOnApply = false;

	@Autowired
	private BatchQueueService batchQueueService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

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

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description) {

		String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
		String versionLabel = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL);

		if (versionLabel == null) {
			versionLabel = RepoConsts.INITIAL_VERSION;
		}
		
		String ecoName = generateEcoName(name + "_v" + versionLabel);
		
		BatchInfo batchInfo = new BatchInfo(String.format("impactWUsed-%s-%s", entityNodeRef, versionLabel), "becpg.batch.eco.impactWUsed");
		batchInfo.setWorkerThreads(1);
		batchInfo.setBatchSize(1);
		batchInfo.setRunAsSystem(true);

		batchQueueService.queueBatch(batchInfo, new EntityListBatchProcessWorkProvider<>(Arrays.asList(entityNodeRef)),
				new AsyncECOGenerator(ecoName, versionType, description), null);

	}
	

	private String generateEcoName(String name) {
		return name + "-" + I18NUtil.getMessage("plm.ecm.current.name", new Date());
	}

	private class AsyncECOGenerator extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> implements BatchProcessWorker<NodeRef> {

		private final VersionType versionType;
		private final String description;
		private final String ecoName;

		public AsyncECOGenerator(String ecoName, VersionType versionType, String description) {
			super();
			this.ecoName = ecoName;
			this.versionType = versionType;
			this.description = description;
		}

		@Override
		public void process(NodeRef entityNodeRef) {

			NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {


				if (logger.isDebugEnabled()) {
					logger.debug("Creating new impactWUsed change order");
				}
				ChangeOrderData changeOrderData = (ChangeOrderData) alfrescoRepository.create(getChangeOrderFolder(),
						new ChangeOrderData(ecoName, ECOState.Automatic, ChangeOrderType.ImpactWUsed, null));

				changeOrderData.setDescription(description);
				changeOrderData.setEcoState(ECOState.InProgress);

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

			}, false, true);

			try {
				boolean success = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					ecoService.calculateWUsedList(ecoNodeRef, true);
					return true;
				}, false, true);

				if (success) {

					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						return ecoService.setInProgress(ecoNodeRef);
					}, false, true);

					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						if (ecoService.apply(ecoNodeRef) && Boolean.TRUE.equals(deleteOnApply)) {
							logger.debug("It's applied and deleteOnApply is set to true, deleting ECO with NR=" + ecoNodeRef);
							nodeService.deleteNode(ecoNodeRef);
						}

						return true;

					}, false, true);
				} else {
					logger.warn("Cannot calculate wused:" + ecoNodeRef);
				}

			} catch (Exception e) {
				if (nodeService.exists(ecoNodeRef)) {
					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						return ecoService.setInError(ecoNodeRef, e.getMessage());
					}, false, true);
				}
				logger.error("Unable to apply eco ", e);
			}

		}


		private NodeRef getChangeOrderFolder() {
			return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
		}

	}

}
