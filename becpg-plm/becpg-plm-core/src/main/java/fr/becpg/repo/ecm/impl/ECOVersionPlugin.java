package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.AsyncECOService;
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

@Service
public class ECOVersionPlugin implements EntityVersionPlugin {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private RepoService repoService;

	@Autowired
	AsyncECOService asyncECOService;

	@Autowired
	ECOService ecoService;

	@Autowired
	@Qualifier("ecoAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;

	@Value("${beCPG.eco.automatic.deleteOnApply}")
	private Boolean deleteOnApply = false;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private static final Log logger = LogFactory.getLog(ECOVersionPlugin.class);

	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description) {

		Runnable command = new AsyncECOGenerator(entityNodeRef, versionType, description, AuthenticationUtil.getFullyAuthenticatedUser());
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			logger.warn("AsyncECOGenerator job already in queue for " + entityNodeRef);
			logger.info("AsyncECOGenerator active task size " + threadExecuter.getActiveCount());
			logger.info("AsyncECOGenerator queue size " + threadExecuter.getTaskCount());
		}

	}

	private String generateEcoName(String name) {
		return name + "-" + I18NUtil.getMessage("plm.ecm.current.name", new Date());
	}

	private NodeRef getChangeOrderFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
	}

	private class AsyncECOGenerator implements Runnable {

		private final NodeRef entityNodeRef;
		private final VersionType versionType;
		private final String description;
		private final String userName;

		public AsyncECOGenerator(NodeRef entityNodeRef, VersionType versionType, String description, String userName) {
			super();
			this.entityNodeRef = entityNodeRef;
			this.versionType = versionType;
			this.description = description;
			this.userName = userName;
		}

		@Override
		public void run() {
			StopWatch watch = new StopWatch();
			watch.start();

			try {
				AuthenticationUtil.runAs(() -> {
					NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						NodeRef parentNodeRef = getChangeOrderFolder();

						String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

						if (logger.isDebugEnabled()) {
							logger.debug("Creating new impactWUsed change order");
						}
						ChangeOrderData changeOrderData = (ChangeOrderData) alfrescoRepository.create(parentNodeRef,
								new ChangeOrderData(generateEcoName(name), ECOState.Automatic, ChangeOrderType.ImpactWUsed, null));

						changeOrderData.setDescription(description);

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

					boolean ret = transactionService.getRetryingTransactionHelper().doInTransaction(() -> ecoService.setInProgress(ecoNodeRef), false,
							true);
					try {
						if (ret) {
							ret = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
								ecoService.calculateWUsedList(ecoNodeRef, true);
								return true;
							}, false, true);

							if (ret) {
								transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
									if (ecoService.apply(ecoNodeRef) && deleteOnApply) {
										logger.debug("It's applied and deleteOnApply is set to true, deleting ECO with NR=" + ecoNodeRef);
										nodeService.deleteNode(ecoNodeRef);
									}

									return true;

								}, false, true);
							} else {
								logger.warn("Cannot calculate wused:" + ecoNodeRef);
							}

						} else {
							logger.warn("ECO already InProgress:" + ecoNodeRef);
						}

					} catch (Exception e) {
						if (nodeService.exists(ecoNodeRef)) {
							transactionService.getRetryingTransactionHelper().doInTransaction(() -> ecoService.setInError(ecoNodeRef, e), false,
									true);
						}
						logger.error("Unable to apply eco ", e);
					}

					return null;
				}, this.userName);

			} catch (Exception e) {

				logger.error("Unable to apply eco ", e);

			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			result = (prime * result) + ((description == null) ? 0 : description.hashCode());
			result = (prime * result) + ((entityNodeRef == null) ? 0 : entityNodeRef.hashCode());
			result = (prime * result) + ((userName == null) ? 0 : userName.hashCode());
			result = (prime * result) + ((versionType == null) ? 0 : versionType.hashCode());
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
			AsyncECOGenerator other = (AsyncECOGenerator) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (description == null) {
				if (other.description != null) {
					return false;
				}
			} else if (!description.equals(other.description)) {
				return false;
			}
			if (entityNodeRef == null) {
				if (other.entityNodeRef != null) {
					return false;
				}
			} else if (!entityNodeRef.equals(other.entityNodeRef)) {
				return false;
			}
			if (userName == null) {
				if (other.userName != null) {
					return false;
				}
			} else if (!userName.equals(other.userName)) {
				return false;
			}
			if (versionType != other.versionType) {
				return false;
			}
			return true;
		}

		private ECOVersionPlugin getOuterType() {
			return ECOVersionPlugin.this;
		}

	}

}
