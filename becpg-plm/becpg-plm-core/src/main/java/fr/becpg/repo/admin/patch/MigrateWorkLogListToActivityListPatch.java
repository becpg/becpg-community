package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.repository.AlfrescoRepository;

public class MigrateWorkLogListToActivityListPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(MigrateWorkLogListToActivityListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.migrateWorkLog.result";
	private static final String PROP_ACTIVITY_EVENT = "activityEvent";
	private static final String PROP_ENTITY_NODEREF = "entityNodeRef";
	private static final String PROP_BEFORE_STATE = "beforeState";
	private static final String PROP_AFTER_STATE = "afterState";
	private static final String PROP_CONTENT = "content";
	private static final String PROP_TITLE = "title";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private EntityListDAO entityListDAO;
	private EntityTplService entityTplService;
	private BehaviourFilter policyBehaviourFilter;
	private AlfrescoRepository<ActivityListDataItem> alfrescoRepository;

	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		policyBehaviourFilter.disableBehaviour();

		List<NodeRef> ncList = new ArrayList<>();
		final Pair<Long, QName> val = getQnameDAO().getQName(QualityModel.TYPE_NC);
		if (val != null) {
			Long typeQNameId = val.getFirst();
			List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, 1L, getPatchDAO().getMaxAdmNodeID());
			for (Long nodeid : nodeids) {
				NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
				if (!status.isDeleted()) {
					if (nodeService.exists(status.getNodeRef())) {
						ncList.add(status.getNodeRef());
					}
				}
			}
			// iterate over nonConformities
			for (NodeRef ncNodeRef : ncList) {

				logger.info("Migrate workLog list of non conformity: " + ncNodeRef);

				int sort = 100;
				NodeRef activityListRef = entityTplService.createActivityList(ncNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

				NodeRef datalistsRef = nodeService.getChildByName(ncNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, "DataLists");

				NodeRef worklogAssocRef = nodeService.getChildByName(datalistsRef, ContentModel.ASSOC_CONTAINS, "workLog");

				if (worklogAssocRef == null) {
					continue;
				}
				if (nodeService.hasAspect(ncNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
					nodeService.deleteNode(worklogAssocRef);
					continue;
				}

				List<ChildAssociationRef> wlAssocListRefs = nodeService.getChildAssocs(worklogAssocRef);

				String beforeState = null;

				/** Work Log **/

				// iterate over workLog list and add elements to activity list
				for (ChildAssociationRef wlNodeRef : wlAssocListRefs) {

					Date wlCreated = (Date) nodeService.getProperty(wlNodeRef.getChildRef(), ContentModel.PROP_CREATED);
					String wlCreator = (String) nodeService.getProperty(wlNodeRef.getChildRef(), ContentModel.PROP_CREATOR);
					String wlState = (String) nodeService.getProperty(wlNodeRef.getChildRef(),
							QName.createQName("http://www.bcpg.fr/model/quality/1.0", "wlState"));
					String wlComment = (String) nodeService.getProperty(wlNodeRef.getChildRef(),
							QName.createQName("http://www.bcpg.fr/model/quality/1.0", "wlComment"));

					ActivityListDataItem activityListDataItem = new ActivityListDataItem();

					JSONObject data = new JSONObject();
					data.put(PROP_ACTIVITY_EVENT, ActivityEvent.Create);
					activityListDataItem.setActivityType(ActivityType.Entity);

					if (beforeState != null) {
						data.put(PROP_ACTIVITY_EVENT, ActivityEvent.Update);
						activityListDataItem.setActivityType(ActivityType.State);
						data.put(PROP_BEFORE_STATE, beforeState);
						data.put(PROP_AFTER_STATE, wlState);
					}

					if ((wlComment != null) && !wlComment.isEmpty()) {
						data.put(PROP_CONTENT, wlComment);
					}

					data.put(PROP_ENTITY_NODEREF, ncNodeRef);
					data.put(PROP_TITLE, nodeService.getProperty(ncNodeRef, ContentModel.PROP_NAME));

					activityListDataItem.setActivityData(data.toString());
					activityListDataItem.setUserId(wlCreator);
					activityListDataItem.setParentNodeRef(activityListRef);

					NodeRef activityListDataItemRef = alfrescoRepository.save(activityListDataItem).getNodeRef();
					Map<QName, Serializable> aspectProperties = new HashMap<>();
					aspectProperties.put(BeCPGModel.PROP_SORT, sort);
					nodeService.addAspect(activityListDataItemRef, BeCPGModel.ASPECT_SORTABLE_LIST, aspectProperties);
					nodeService.setProperty(activityListDataItemRef, ContentModel.PROP_CREATED, wlCreated);

					beforeState = wlState;
					sort++;
				}

				// delete workLog list
				nodeService.deleteNode(worklogAssocRef);
			}

			policyBehaviourFilter.enableBehaviour();
		}
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

	public RuleService getRuleService() {
		return ruleService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public BehaviourFilter getPolicyBehaviourFilter() {
		return policyBehaviourFilter;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public EntityListDAO getEntityListDAO() {
		return entityListDAO;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public AlfrescoRepository<ActivityListDataItem> getAlfrescoRepository() {
		return alfrescoRepository;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ActivityListDataItem> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public EntityTplService getEntityTplService() {
		return entityTplService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

}
