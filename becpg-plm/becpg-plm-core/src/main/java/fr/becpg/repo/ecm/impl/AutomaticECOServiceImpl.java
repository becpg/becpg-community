package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.ECMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("automaticECOService")
public class AutomaticECOServiceImpl implements AutomaticECOService {

	private static final String CURRENT_ECO_PREF = "fr.becpg.ecm.currentEcmNodeRef";

	private static final Log logger = LogFactory.getLog(AutomaticECOService.class);

	@Autowired
	private RepoService repoService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Value("${beCPG.eco.automatic.apply}")
	private Boolean shouldApplyAutomaticECO = false;

	@Value("${beCPG.eco.automatic.revision.type}")
	private String automaticRevisionType = RevisionType.NoRevision.toString();
	
	@Value("${beCPG.eco.automatic.states}")
	private String statesToRegister = "";

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ECOService ecoService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private PreferenceService preferenceService;

	@Override
	public boolean addAutomaticChangeEntry(final NodeRef entityNodeRef,final ChangeOrderData currentUserChangeOrderData) {

		String productState = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE);
		
		if (productState == null || productState.isEmpty() || !statesToRegister.contains(productState)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping product state : " + productState);
			}
			return false;
		}

		return AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>() {
			public Boolean doWork() throws Exception {
				NodeRef parentNodeRef = getChangeOrderFolder();

				ChangeOrderData changeOrderData = currentUserChangeOrderData;

				if (changeOrderData == null) {
					changeOrderData = new ChangeOrderData(generateEcoName(null), ECOState.Automatic, ChangeOrderType.Replacement, null);

					NodeRef ret = getAutomaticECONoderef(parentNodeRef);

					if (ret != null) {
						changeOrderData = (ChangeOrderData) alfrescoRepository.findOne(ret);
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Creating new automatic change order");
						}
						changeOrderData = (ChangeOrderData) alfrescoRepository.create(parentNodeRef, changeOrderData);
					}
				}

				List<ReplacementListDataItem> replacementList = changeOrderData.getReplacementList();

				if (replacementList == null) {
					replacementList = new ArrayList<>();
				}

				// avoid recreate same entry
				for (ReplacementListDataItem item : replacementList) {
					if (entityNodeRef.equals(item.getTargetItem())) {
						if (logger.isDebugEnabled()) {
							logger.debug("NodeRef " + entityNodeRef + " already present in automatic change order :" + changeOrderData.getName());
						}
						return false;
					}
				}

				replacementList.add(new ReplacementListDataItem(RevisionType.valueOf(automaticRevisionType), Collections.singletonList(entityNodeRef), entityNodeRef, 100));

				if (logger.isDebugEnabled()) {
					logger.debug("Adding nodeRef " + entityNodeRef + " to automatic change order :" + changeOrderData.getName());
					logger.debug("Revision type : " + automaticRevisionType );
				}

				changeOrderData.setReplacementList(replacementList);

				alfrescoRepository.save(changeOrderData);

				return true;
			}
		});

	}

	private NodeRef getAutomaticECONoderef(NodeRef parentFolderNodeRef) {
		return BeCPGQueryBuilder.createQuery().parent(parentFolderNodeRef).ofType(ECMModel.TYPE_ECO)
				.andPropEquals(ECMModel.PROP_ECO_STATE, ECOState.Automatic.toString()).inDB().singleValue();
	}

	private String generateEcoName(String name) {
		if (name != null) {
			return name + "-" + I18NUtil.getMessage("plm.ecm.current.name", new Date());
		}
		return I18NUtil.getMessage("plm.ecm.automatic.name", new Date());
	}

	private NodeRef getChangeOrderFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
	}

	@Override
	public boolean applyAutomaticEco() {

		boolean ret;

		if (shouldApplyAutomaticECO) {
			if (logger.isDebugEnabled()) {
				logger.debug("Try to apply automatic change order");
			}

			final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
				public NodeRef execute() throws Throwable {
					NodeRef parentNodeRef = getChangeOrderFolder();
					return getAutomaticECONoderef(parentNodeRef);
				}
			}, false, true);

			if (ecoNodeRef != null) {

				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
					public Boolean execute() throws Throwable {
						ecoService.setInProgress(ecoNodeRef);
						return true;
					}
				}, false, true);

				ret = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
					public Boolean execute() throws Throwable {

						if (logger.isDebugEnabled()) {
							logger.debug("Found automatic change order to calculate WUsed :" + ecoNodeRef);
						}
						try {
							ecoService.calculateWUsedList(ecoNodeRef, true);
						} catch (Exception e) {
							if (e instanceof ConcurrencyFailureException) {
								throw (ConcurrencyFailureException) e;
							}
							logger.error(e, e);
							return false;
						}
						return true;

					}
				}, false, true);

				if (ret) {
					return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
						public Boolean execute() throws Throwable {
							if (logger.isDebugEnabled()) {
								logger.debug("Found automatic change order to apply :" + ecoNodeRef);
							}
							try {
								ecoService.apply(ecoNodeRef);
							} catch (Exception e) {
								if (e instanceof ConcurrencyFailureException) {
									throw (ConcurrencyFailureException) e;
								}
								logger.error(e, e);
								return false;
							}
							return true;
						}
					}, false, true);
				}
			}
		}
		return false;
	}

	@Override
	public ChangeOrderData createAutomaticEcoForUser(String name) {
		NodeRef parentNodeRef = getChangeOrderFolder();

		ChangeOrderData changeOrderData = new ChangeOrderData(generateEcoName(name), ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, null);

		changeOrderData = (ChangeOrderData) alfrescoRepository.create(parentNodeRef, changeOrderData);
		String curUserName = AuthenticationUtil.getFullyAuthenticatedUser();
		Map<String, Serializable> prefs = preferenceService.getPreferences(curUserName);
		prefs.put(CURRENT_ECO_PREF, changeOrderData.getNodeRef().toString());
		preferenceService.setPreferences(curUserName, prefs);

		return changeOrderData;
	}

	@Override
	public ChangeOrderData getCurrentUserChangeOrderData() {
		String curUserName = AuthenticationUtil.getFullyAuthenticatedUser();
		Map<String, Serializable> prefs = preferenceService.getPreferences(curUserName);

		String prefNodeRef = (String) prefs.get(CURRENT_ECO_PREF);
		if (prefNodeRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found pref nodeRef : " + prefNodeRef);
			}
			NodeRef currentUserNodeRef = new NodeRef(prefNodeRef);
			if (nodeService.exists(currentUserNodeRef)
					&& ECOState.ToCalculateWUsed.toString().equals(nodeService.getProperty(currentUserNodeRef, ECMModel.PROP_ECO_STATE))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found current automatic Eco for user :" + curUserName);
				}
				return (ChangeOrderData) alfrescoRepository.findOne(currentUserNodeRef);
			} else {
				logger.info("Removing invalid eco automatic noderef from user prefs : " + curUserName);
				logger.info("Node doesn't exist ? " + nodeService.exists(currentUserNodeRef));
				prefs.put(CURRENT_ECO_PREF, null);
				preferenceService.setPreferences(curUserName, prefs);
			}
		}
		return null;
	}

}
