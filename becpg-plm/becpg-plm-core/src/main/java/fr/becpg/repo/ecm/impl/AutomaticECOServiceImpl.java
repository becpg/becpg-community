package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	private static Log logger = LogFactory.getLog(AutomaticECOService.class);

	@Autowired
	private RepoService repoService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Value("${beCPG.eco.automatic.apply}")
	private Boolean shouldApplyAutomaticECO = false;

	@Value("${beCPG.eco.automatic.version}")
	private Boolean shouldCreateNewVersion = false;
	
	@Value("${beCPG.eco.automatic.states}")
	private String statesToRegister  = "";

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ECOService ecoService;
	
	@Autowired
	private NodeService nodeService;

	@Override
	public ChangeOrderData addAutomaticChangeEntry(NodeRef entityNodeRef) {
		
		String productState = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE);
		
		if(productState == null || productState.isEmpty() || !statesToRegister.contains(productState)){
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping product state : "+productState);
			}
			
			return null;
		}
		
	
		NodeRef parentNodeRef = getChangeOrderFolder();
		ChangeOrderData changeOrderData = new ChangeOrderData(generateEcoName(), ECOState.Automatic, ChangeOrderType.Simulation, null);

		NodeRef ret = getAutomaticECONoderef(parentNodeRef);

		if (ret != null) {
			changeOrderData = (ChangeOrderData) alfrescoRepository.findOne(ret);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new automatic change order");
			}
			changeOrderData = (ChangeOrderData) alfrescoRepository.create(parentNodeRef, changeOrderData);
		}

		List<ReplacementListDataItem> replacementList = changeOrderData.getReplacementList();

		if (replacementList == null) {
			replacementList = new ArrayList<ReplacementListDataItem>();
		}

		// avoid recreate same entry
		for (ReplacementListDataItem item : replacementList) {
			if (entityNodeRef.equals(item.getTargetItem())) {
				if (logger.isDebugEnabled()) {
					logger.debug("NodeRef " + entityNodeRef + " already present in automatic change order :" + changeOrderData.getName());
				}
				return changeOrderData;
			}
		}

		replacementList.add(new ReplacementListDataItem(shouldCreateNewVersion ? RevisionType.Major : RevisionType.Minor, Arrays
				.asList(entityNodeRef), entityNodeRef, 100));

		if (logger.isDebugEnabled()) {
			logger.debug("Adding nodeRef " + entityNodeRef + " to automatic change order :" + changeOrderData.getName());
			logger.debug("Revision type : " + (shouldCreateNewVersion ? RevisionType.Major : RevisionType.Minor));
		}

		changeOrderData.setReplacementList(replacementList);

		alfrescoRepository.save(changeOrderData);

		return changeOrderData;
	}

	private NodeRef getAutomaticECONoderef(NodeRef parentFolderNodeRef) {
		return BeCPGQueryBuilder.createQuery().parent(parentFolderNodeRef).ofType(ECMModel.TYPE_ECO)
				.andPropEquals(ECMModel.PROP_ECO_STATE, ECOState.Automatic.toString()).inDB().singleValue();
	}

	private String generateEcoName() {
		return I18NUtil.getMessage("plm.ecm.automatic.name", new Date());
	}

	private NodeRef getChangeOrderFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
	}

	@Override
	public boolean applyAutomaticEco() {

		boolean ret = false;

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

}
