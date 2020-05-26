package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * beCPG model definition.
 *
 * @author querephi
 */
public final class BeCPGModel {

	public final static String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	public final static String BECPG_PREFIX = "bcpg";
	public final static QName MODEL = QName.createQName(BECPG_URI, "bcpgmodel");

	// entityListsAspect
	public final static QName ASPECT_ENTITYLISTS = QName.createQName(BECPG_URI, "entityListsAspect");
	public final static QName ASSOC_ENTITYLISTS = QName.createQName(BECPG_URI, "entityLists");

	public final static QName ASPECT_HIDDEN_FOLDER = QName.createQName(BECPG_URI, "hiddenFolder");

	public final static QName TYPE_ENTITYLIST_ITEM = QName.createQName(BECPG_URI, "entityListItem");

	public final static QName ASPECT_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListStateAspect");

	public final static QName PROP_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListState");

	// Caract
	public final static QName TYPE_CHARACT = QName.createQName(BeCPGModel.BECPG_URI, "charact");
	public final static QName PROP_CHARACT_NAME = QName.createQName(BECPG_URI, "charactName");

	// linkedValue
	public final static QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI, "linkedValue");
	public final static QName PROP_LKV_VALUE = QName.createQName(BECPG_URI, "lkvValue");

	// listValue
	public final static QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI, "listValue");
	public final static QName PROP_LV_VALUE = QName.createQName(BECPG_URI, "lvValue");
	public final static QName PROP_LV_CODE = QName.createQName(BECPG_URI, "lvCode");

	public final static QName TYPE_ACTIVITY_LIST = QName.createQName(BECPG_URI, "activityList");
	public final static QName PROP_ACTIVITYLIST_TYPE = QName.createQName(BECPG_URI, "alType");
	public final static QName PROP_ACTIVITYLIST_DATA = QName.createQName(BECPG_URI, "alData");
	public final static QName PROP_ACTIVITYLIST_USERID = QName.createQName(BECPG_URI, "alUserId");

	// Notification
	public final static QName TYPE_NOTIFICATIONRULELIST = QName.createQName(BeCPGModel.BECPG_URI, "notificationRuleList");

	// entity
	public final static QName TYPE_ENTITY_V2 = QName.createQName(BECPG_URI, "entityV2");

	public final static QName TYPE_SYSTEM_ENTITY = QName.createQName(BECPG_URI, "systemEntity");

	// autoNum
	public final static QName TYPE_AUTO_NUM = QName.createQName(BECPG_URI, "autoNum");
	public final static QName PROP_AUTO_NUM_CLASS_NAME = QName.createQName(BECPG_URI, "autoNumClassName");
	public final static QName PROP_AUTO_NUM_PROPERTY_NAME = QName.createQName(BECPG_URI, "autoNumPropertyName");
	public final static QName PROP_AUTO_NUM_VALUE = QName.createQName(BECPG_URI, "autoNumValue");
	public final static QName PROP_AUTO_NUM_PREFIX = QName.createQName(BECPG_URI, "autoNumPrefix");

	// entityTpl aspect
	public final static QName ASPECT_ENTITY_TPL = QName.createQName(BECPG_URI, "entityTplAspect");
	public final static QName PROP_ENTITY_TPL_ENABLED = QName.createQName(BECPG_URI, "entityTplEnabled");
	public final static QName PROP_ENTITY_TPL_IS_DEFAULT = QName.createQName(BECPG_URI, "entityTplIsDefault");
	public final static QName PROP_ENTITY_TPL_DEFAULT_DEST = QName.createQName(BECPG_URI, "entityTplDefaultDest");
	public static final QName PROP_ENTITY_TPL_SCRIPT = QName.createQName(BECPG_URI, "entityTplScript");

	// entityTplRef aspect
	public final static QName ASPECT_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRefAspect");
	public final static QName ASSOC_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRef");

	// depthLevel aspect
	public final static QName ASPECT_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevelAspect");
	public final static QName PROP_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevel");
	public final static QName PROP_PARENT_LEVEL = QName.createQName(BECPG_URI, "parentLevel");

	// depthLevel aspect
	public final static QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI, "sortableListAspect");
	public final static QName PROP_SORT = QName.createQName(BECPG_URI, "sort");

	// detaillableListItem aspect
	public final static QName ASPECT_DETAILLABLE_LIST_ITEM = QName.createQName(BECPG_URI, "detaillableListItemAspect");

	// manual aspect
	public final static QName ASPECT_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItemAspect");
	public final static QName PROP_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItem");

	// version aspect
	public final static QName ASPECT_COMPOSITE_VERSION = QName.createQName(BECPG_URI, "compositeVersion");
	public final static QName PROP_VERSION_LABEL = QName.createQName(BECPG_URI, "versionLabel");

	// permissionsTpl
	public final static QName ASPECT_PERMISSIONS_TPL = QName.createQName(BECPG_URI, "permissionsTpl");
	public final static QName ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS = QName.createQName(BECPG_URI, "consumerGroups");
	public final static QName ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS = QName.createQName(BECPG_URI, "editorGroups");
	public final static QName ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS = QName.createQName(BECPG_URI, "contributorGroups");
	public final static QName ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS = QName.createQName(BECPG_URI, "collaboratorGroups");

	// code aspect
	public final static QName ASPECT_CODE = QName.createQName(BECPG_URI, "codeAspect");
	public final static QName PROP_CODE = QName.createQName(BECPG_URI, "code");

	// score aspect
	public final static QName ASPECT_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScoreAspect");
	public final static QName PROP_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScore");

	// systemFolder
	public final static QName ASPECT_SYSTEM_FOLDER = QName.createQName(BECPG_URI, "systemFolderAspect");
	public final static QName ASPECT_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntitiesAspect");
	public final static QName ASSOC_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntities");

	// formulatedEntity
	public final static QName ASPECT_FORMULATED_ENTITY = QName.createQName(BECPG_URI, "formulatedEntityAspect");
	public final static QName PROP_FORMULATED_DATE = QName.createQName(BECPG_URI, "formulatedDate");

	// effectivity
	public final static QName ASPECT_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "effectivityAspect");
	public final static QName PROP_START_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "startEffectivity");
	public final static QName PROP_END_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "endEffectivity");

	// color
	public final static QName ASPECT_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "colorAspect");
	public final static QName PROP_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "color");

	// legalName aspect
	public final static QName ASPECT_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalNameAspect");
	public final static QName PROP_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalName");
	public final static QName PROP_PLURAL_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "pluralLegalName");

	// isDeleted aspect
	public final static QName ASPECT_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeletedAspect");
	public final static QName PROP_IS_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeleted");

	public final static QName ASPECT_COMPARE_WITH = QName.createQName(BeCPGModel.BECPG_URI, "compareWithAspect");
	public final static QName ASSOC_COMPARE_WITH_ENTITIES = QName.createQName(BECPG_URI, "compareWithEntities");

	public final static QName ASPECT_ENTITY_BRANCH = QName.createQName(BeCPGModel.BECPG_URI, "entityBranchAspect");
	public final static QName ASSOC_BRANCH_FROM_ENTITY = QName.createQName(BECPG_URI, "branchFromEntity");
	public final static QName PROP_BRANCH_FROM_VERSION_LABEL = QName.createQName(BECPG_URI, "branchFromVersionLabel");
	public final static QName ASSOC_AUTO_MERGE_TO = QName.createQName(BECPG_URI, "autoMergeTo");

	public final static QName PROP_AUTO_MERGE_VERSIONTYPE = QName.createQName(BECPG_URI, "autoMergeVersionType");
	public final static QName PROP_AUTO_MERGE_COMMENTS = QName.createQName(BECPG_URI, "autoMergeComments");
	public final static QName PROP_AUTO_MERGE_IMPACTWUSED = QName.createQName(BECPG_URI, "autoMergeImpactWUsed");
	public final static QName ASPECT_AUTO_MERGE_ASPECT = QName.createQName(BeCPGModel.BECPG_URI, "autoMergeAspect");

	public final static QName ASPECT_LINKED_SEARCH = QName.createQName(BECPG_URI, "linkedSearchAspect");
	public final static QName ASSOC_LINKED_SEARCH_ASSOCIATION = QName.createQName(BECPG_URI, "linkedSearchAssociation");

	// Audit state aspect
	public final static QName PROP_STATE_ACTIVITY_MODIFIED = QName.createQName(BECPG_URI, "stateActivityModified");
	public final static QName PROP_STATE_ACTIVITY_MODIFIER = QName.createQName(BECPG_URI, "stateActivityModifier");
	public final static QName PROP_STATE_ACTIVITY_PREVIOUSSTATE = QName.createQName(BECPG_URI, "stateActivityPreviousState");

	public final static QName PROP_USER_LOCAL = QName.createQName(BECPG_URI, "userLocale");
	public final static QName PROP_USER_CONTENT_LOCAL = QName.createQName(BECPG_URI, "userContentLocale");
	// code aspect
	public final static QName ASPECT_ERP_CODE = QName.createQName(BECPG_URI, "erpCodeAspect");
	public final static QName PROP_ERP_CODE = QName.createQName(BECPG_URI, "erpCode");

	// WorkflowEntity
	public final static QName ASSOC_WORKFLOW_ENTITY = QName.createQName(BECPG_URI, "workflowEntity");

	// variant
	public final static QName ASPECT_ENTITYLIST_VARIANT = QName.createQName(BECPG_URI, "entityListVariantAspect");
	public final static QName ASPECT_ENTITY_VARIANT = QName.createQName(BECPG_URI, "entityVariantAspect");
	public final static QName ASSOC_VARIANTS = QName.createQName(BECPG_URI, "variants");
	public final static QName TYPE_VARIANT = QName.createQName(BECPG_URI, "variant");
	public final static QName PROP_VARIANTIDS = QName.createQName(BECPG_URI, "variantIds");
	public final static QName PROP_IS_DEFAULT_VARIANT = QName.createQName(BECPG_URI, "isDefaultVariant");
	

}
