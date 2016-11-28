package fr.becpg.repo.project.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.repository.AlfrescoRepository;

public class ProjectActivityPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ProjectActivityPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.projet.projectActivityPatch.result";

	QName TYPE_ACTIVITY_LIST = QName.createQName(ProjectModel.PROJECT_URI, "activityList");
	QName PROP_ACTIVITYLIST_USERID = QName.createQName(ProjectModel.PROJECT_URI, "alUserId");
	QName PROP_ACTIVITYLIST_TYPE = QName.createQName(ProjectModel.PROJECT_URI, "alType");
	QName PROP_ACTIVITYLIST_TASKID = QName.createQName(ProjectModel.PROJECT_URI, "alTaskId");
	QName PROP_ACTIVITYLIST_DELIVERABLEID = QName.createQName(ProjectModel.PROJECT_URI, "alDeliverableId");
	QName PROP_ACTIVITYLIST_DATA = QName.createQName(ProjectModel.PROJECT_URI, "alData");

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private AlfrescoRepository<ActivityListDataItem> alfrescoRepository;
	private EntityListDAO entityListDAO;
	private AssociationService associationService;
	private AttributeExtractorService attributeExtractorService;

	public void setAlfrescoRepository(AlfrescoRepository<ActivityListDataItem> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;

	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 1;
			long maxSearchNodeId = count;

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
						minSearchNodeId = minSearchNodeId + count;
						maxSearchNodeId = maxSearchNodeId + count;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("ProjectActivityPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			@Override
			public void afterProcess() throws Throwable {
			}

			@Override
			public void beforeProcess() throws Throwable {
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef activityNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				if (nodeService.exists(activityNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

					NodeRef entityNodeRef = entityListDAO.getEntity(activityNodeRef);

					JSONObject data = new JSONObject(nodeService.getProperty(activityNodeRef, PROP_ACTIVITYLIST_DATA));

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

					for (NodeRef projectNodeRef : associationService.getSourcesAssocs(activityNodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS)) {

						associationService.update(projectNodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS,
								Collections.singletonList(activityListDataItem.getNodeRef()));

					}

				} else {
					logger.warn("activityNodeRef doesn't exist : " + activityNodeRef);
				}

			}

			private NodeRef getOrCreateActivityList(NodeRef entityNodeRef) {
				NodeRef listNodeRef = null;
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
				if (listContainerNodeRef != null) {
					listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
				}
				if (listNodeRef == null) {
					listNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
				}

				return listNodeRef;
			}

		};

		batchProcessor.process(worker, true);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

}
