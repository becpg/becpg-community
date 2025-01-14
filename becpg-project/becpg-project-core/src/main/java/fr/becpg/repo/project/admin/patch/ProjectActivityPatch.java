package fr.becpg.repo.project.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>ProjectActivityPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProjectActivityPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ProjectActivityPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.projet.projectActivityPatch.result";

	private static final QName TYPE_ACTIVITY_LIST = QName.createQName(ProjectModel.PROJECT_URI, "activityList");
	private static final QName PROP_ACTIVITYLIST_USERID = QName.createQName(ProjectModel.PROJECT_URI, "alUserId");
	private static final QName PROP_ACTIVITYLIST_TYPE = QName.createQName(ProjectModel.PROJECT_URI, "alType");
	private static final QName PROP_ACTIVITYLIST_TASKID = QName.createQName(ProjectModel.PROJECT_URI, "alTaskId");
	private static final QName PROP_ACTIVITYLIST_DELIVERABLEID = QName.createQName(ProjectModel.PROJECT_URI, "alDeliverableId");
	private static final QName PROP_ACTIVITYLIST_DATA = QName.createQName(ProjectModel.PROJECT_URI, "alData");

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private AlfrescoRepository<ActivityListDataItem> alfrescoRepository;
	private EntityListDAO entityListDAO;
	private AssociationService associationService;
	private AttributeExtractorService attributeExtractorService;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;

	/**
	 * <p>Getter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public BehaviourFilter getPolicyBehaviourFilter() {
		return policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Getter for the field <code>ruleService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public RuleService getRuleService() {
		return ruleService;
	}

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ActivityListDataItem> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 1;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(TYPE_ACTIVITY_LIST);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {

						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
						maxSearchNodeId = maxSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("ProjectActivityPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				ruleService.enableRules();
			}

			@Override
			public void beforeProcess() throws Throwable {
				ruleService.disableRules();
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef activityNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				policyBehaviourFilter.disableBehaviour();
				if (nodeService.exists(activityNodeRef) && activityNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

					NodeRef entityNodeRef = entityListDAO.getEntity(activityNodeRef);

					String oldData = (String) nodeService.getProperty(activityNodeRef, PROP_ACTIVITYLIST_DATA);

					logger.debug("Convert  : " + oldData);

					JSONTokener tokener = new JSONTokener(oldData);
					JSONObject data = new JSONObject(tokener);

					NodeRef itemNodeRef = (NodeRef) nodeService.getProperty(activityNodeRef, PROP_ACTIVITYLIST_DELIVERABLEID);
					if (itemNodeRef == null) {
						itemNodeRef = (NodeRef) nodeService.getProperty(activityNodeRef, PROP_ACTIVITYLIST_TASKID);
					}

					if (itemNodeRef != null) {
						data.put(EntityActivityService.PROP_DATALIST_NODEREF, itemNodeRef);
						data.put(EntityActivityService.PROP_CLASSNAME,
								attributeExtractorService.extractMetadata(nodeService.getType(itemNodeRef), itemNodeRef));
					} else {
						data.put(EntityActivityService.PROP_CLASSNAME,
								attributeExtractorService.extractMetadata(nodeService.getType(entityNodeRef), entityNodeRef));
					}

					ActivityListDataItem activityListDataItem = new ActivityListDataItem();
					activityListDataItem
							.setActivityType(ActivityType.valueOf((String) nodeService.getProperty(activityNodeRef, PROP_ACTIVITYLIST_TYPE)));
					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setUserId((String) nodeService.getProperty(activityNodeRef, PROP_ACTIVITYLIST_USERID));
					activityListDataItem.setParentNodeRef(getOrCreateActivityList(entityNodeRef));

					alfrescoRepository.save(activityListDataItem);

					nodeService.setProperty(activityListDataItem.getNodeRef(), ContentModel.PROP_CREATED,
							nodeService.getProperty(activityNodeRef, ContentModel.PROP_CREATED));

					logger.debug("Storing : " + activityListDataItem.toString());

					for (NodeRef projectNodeRef : associationService.getSourcesAssocs(activityNodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS)) {

						associationService.update(projectNodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS,
								Collections.singletonList(activityListDataItem.getNodeRef()));

					}

					// Delete Node
					nodeService.addAspect(activityNodeRef, ContentModel.ASPECT_TEMPORARY, new HashMap<>());
					nodeService.deleteNode(activityNodeRef);

				}

				policyBehaviourFilter.enableBehaviour();

			}

		};

		batchProcessor.processLong(worker, true);

		new BatchProcessor<>("ProjectActivityPatch2", transactionService.getRetryingTransactionHelper(), new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 1;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(DataListModel.TYPE_DATALIST);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {

						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
						maxSearchNodeId = maxSearchNodeId + INC;
					}
				}

				return result;
			}
		}, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000).processLong(new BatchProcessWorker<NodeRef>() {

			@Override
			public void afterProcess() throws Throwable {
				//Do Nothing

			}

			@Override
			public void beforeProcess() throws Throwable {
				//Do Nothing

			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {
				ruleService.disableRules();
				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				policyBehaviourFilter.disableBehaviour();
				if ((nodeService.exists(dataListNodeRef) && dataListNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
						&& TYPE_ACTIVITY_LIST.toPrefixString(namespaceService)
								.equals(nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE))) {
					nodeService.setProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE,
							BeCPGModel.TYPE_ACTIVITY_LIST.toPrefixString(namespaceService));
				}
				policyBehaviourFilter.enableBehaviour();
				ruleService.enableRules();
			}

		}, true);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private NodeRef getOrCreateActivityList(NodeRef entityNodeRef) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
		}
		if (listNodeRef == null) {
			listNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
		} else {
			nodeService.setProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE, BeCPGModel.TYPE_ACTIVITY_LIST.toPrefixString(namespaceService));
		}

		return listNodeRef;
	}

	/**
	 * <p>Getter for the field <code>nodeDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	/**
	 * <p>Setter for the field <code>nodeDAO</code>.</p>
	 *
	 * @param nodeDAO a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	/**
	 * <p>Getter for the field <code>patchDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	/**
	 * <p>Setter for the field <code>patchDAO</code>.</p>
	 *
	 * @param patchDAO a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	/**
	 * <p>Getter for the field <code>qnameDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

}
