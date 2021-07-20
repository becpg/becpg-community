package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
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

	@Value("${beCPG.eco.automatic.withoutRecord}")
	private Boolean withoutRecord = false;

	@Value("${beCPG.eco.automatic.revision.type}")
	private String automaticRevisionType = RevisionType.NoRevision.toString();

	@Value("${beCPG.eco.automatic.states}")
	private String statesToRegister = "";

	@Value("${beCPG.eco.automatic.deleteOnApply}")
	private Boolean deleteOnApply = false;

	@Value("${beCPG.eco.automatic.enable}")
	private Boolean isEnable = false;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ECOService ecoService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private PreferenceService preferenceService;

	@Autowired
	private FormulationService<ProductData> formulationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private WUsedListService wUsedListService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Override
	public boolean addAutomaticChangeEntry(final NodeRef entityNodeRef, final ChangeOrderData currentUserChangeOrderData) {

		if (Boolean.TRUE.equals(withoutRecord) && currentUserChangeOrderData == null) {
			return false;
		}

		if (!accept(entityNodeRef)) {
			return false;
		}

		return AuthenticationUtil.runAsSystem(() -> {
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

			replacementList.add(new ReplacementListDataItem(RevisionType.valueOf(automaticRevisionType), Collections.singletonList(entityNodeRef),
					entityNodeRef, 100));

			if (logger.isDebugEnabled()) {
				logger.debug("Adding nodeRef " + entityNodeRef + " to automatic change order :" + changeOrderData.getName());
				logger.debug("Revision type : " + automaticRevisionType);
			}

			changeOrderData.setReplacementList(replacementList);

			alfrescoRepository.save(changeOrderData);

			return true;
		});

	}

	private boolean accept(NodeRef entityNodeRef) {

		String productState = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE);

		if ((productState == null) || productState.isEmpty() || !statesToRegister.contains(productState)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping product state : " + productState);
			}
			return false;
		}

		QName nodeType = nodeService.getType(entityNodeRef);

		if (PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeType)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping local semi finished product");
			}
			return false;
		}

		return true;
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
		if (isEnable) {

			if (autoMergeBranch()) {

				if (withoutRecord) {
					return reformulateChangedEntities();
				} else {

					if (shouldApplyAutomaticECO) {
						if (logger.isDebugEnabled()) {
							logger.debug("Try to apply automatic change order");
						}

						final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							NodeRef parentNodeRef = getChangeOrderFolder();
							return getAutomaticECONoderef(parentNodeRef);
						}, false, true);

						if (ecoNodeRef != null) {

							transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
								ecoService.setInProgress(ecoNodeRef);
								return true;
							}, false, true);

							ret = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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

							}, false, true);

							if (ret) {
								
								transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
									ecoService.setInProgress(ecoNodeRef);
									return true;
								}, false, true);

								return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
									if (logger.isDebugEnabled()) {
										logger.debug("Found automatic change order to apply :" + ecoNodeRef);
									}
									try {

										if (ecoService.apply(ecoNodeRef) && deleteOnApply) {
											logger.debug("It's applied and deleteOnApply is set to true, deleting ECO with NR=" + ecoNodeRef);
											nodeService.deleteNode(ecoNodeRef);
										}

									} catch (Exception e) {
										if (e instanceof ConcurrencyFailureException) {
											throw (ConcurrencyFailureException) e;
										}
										logger.error(e, e);
										return false;
									}
									return true;
								}, false, true);
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean autoMergeBranch() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, +1);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		String dateRange = dateFormat.format(cal.getTime());

		String ftsQuery = String.format("@bcpg\\:autoMergeDate:[MIN TO %s]", dateRange);

		logger.debug("Start of auto merge entities for: " + ftsQuery);

		List<NodeRef> nodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> BeCPGQueryBuilder.createQuery()
				.ofType(PLMModel.TYPE_PRODUCT).withAspect(BeCPGModel.ASPECT_AUTO_MERGE_ASPECT).andFTSQuery(ftsQuery).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list(), false, true);

		for (NodeRef entityNodeRef : nodeRefs) {

			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				if (logger.isDebugEnabled()) {
					logger.debug("Found product to merge: " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " (" + entityNodeRef
							+ ") ");
				}
				try {
					AuthenticationUtil.runAsSystem(() -> {
						entityVersionService.mergeBranch(entityNodeRef);

						return true;
					});

				} catch (Exception e) {
					if (e instanceof ConcurrencyFailureException) {
						throw (ConcurrencyFailureException) e;
					}
					logger.error("Cannot merge node:" + entityNodeRef, e);
				}

				return true;

			}, false, true);
		}

		return true;
	}

	private boolean reformulateChangedEntities() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Boolean ret = true;

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		String dateRange = dateFormat.format(cal.getTime());

		String ftsQuery = String.format("@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX]", dateRange, dateRange);

		logger.debug("Start of reformulate changed entities for: " + ftsQuery);

		List<NodeRef> nodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> BeCPGQueryBuilder.createQuery()
				.ofType(PLMModel.TYPE_PRODUCT).andFTSQuery(ftsQuery).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list(), false, true);

		final Set<NodeRef> formulatedEntities = new HashSet<>();

		for (NodeRef entityNodeRef : nodeRefs) {

			List<NodeRef> toReformulates = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				if (accept(entityNodeRef)) {

					if (logger.isDebugEnabled()) {
						logger.debug("Found modified product: " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " ("
								+ entityNodeRef + ") ");
					}
					try {

						QName associationQName = evaluateWUsedAssociation(entityNodeRef);

						if (associationQName != null) {
							MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(Arrays.asList(entityNodeRef), WUsedOperator.AND,
									associationQName, RepoConsts.MAX_DEPTH_LEVEL);

							if (logger.isTraceEnabled()) {
								logger.trace("WUsed to apply:" + wUsedData.toString());
								logger.trace("Leaf size :" + wUsedData.getAllLeafs().size());

							}

							return wUsedData.getAllLeafs();
						}
					} catch (Exception e) {
						if (e instanceof ConcurrencyFailureException) {
							throw (ConcurrencyFailureException) e;
						}
						logger.error(e, e);
					}
				}
				return new ArrayList<>();

			}, false, true);

			if(logger.isDebugEnabled()) {
				logger.debug(" - reformulating: " + toReformulates.size() + " entities");
			}
			
			for (NodeRef toReformulate : toReformulates) {

				if (!formulatedEntities.contains(toReformulate)) {

					ret = ret && formulatedEntities.add(toReformulate);

					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						if (logger.isDebugEnabled()) {
							logger.debug("Reformulating product: " + nodeService.getProperty(toReformulate, ContentModel.PROP_NAME) + " ("
									+ toReformulate + ") ");
						}
						try {
							policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
							policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
							policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

							L2CacheSupport.doInCacheContext(() -> {
								AuthenticationUtil.runAsSystem(() -> {
									formulationService.formulate(toReformulate);

									return true;
								});

							}, false, true);

						} catch (Exception e) {
							if (e instanceof ConcurrencyFailureException) {
								throw (ConcurrencyFailureException) e;
							}
							logger.error("Cannot reformulate node:" + toReformulate, e);
							return false;
						} finally {
							policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
							policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
							policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
						}

						return true;

					}, false, true);
				}
			}

		}

		logger.debug("End of reformulate changed entities");

		return ret;
	}

	private QName evaluateWUsedAssociation(NodeRef targetAssocNodeRef) {

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) {
			return PLMModel.ASSOC_COMPOLIST_PRODUCT;
		} else if (nodeType.isMatch(PLMModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(PLMModel.TYPE_PACKAGINGKIT)) {
			return PLMModel.ASSOC_PACKAGINGLIST_PRODUCT;
		} else if (nodeType.isMatch(PLMModel.TYPE_RESOURCEPRODUCT)) {
			return MPMModel.ASSOC_PL_RESOURCE;
		}

		return null;
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
