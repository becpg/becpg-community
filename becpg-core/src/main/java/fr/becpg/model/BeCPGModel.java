package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * beCPG model definition.
 *
 * @author querephi
 * @version $Id: $Id
 */
public final class BeCPGModel {

	/** Constant <code>BECPG_URI="http://www.bcpg.fr/model/becpg/1.0"</code> */
	public final static String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	/** Constant <code>BECPG_PREFIX="bcpg"</code> */
	public final static String BECPG_PREFIX = "bcpg";
	/** Constant <code>MODEL</code> */
	public final static QName MODEL = QName.createQName(BECPG_URI, "bcpgmodel");

	// entityListsAspect
	/** Constant <code>ASPECT_ENTITYLISTS</code> */
	public final static QName ASPECT_ENTITYLISTS = QName.createQName(BECPG_URI, "entityListsAspect");
	/** Constant <code>ASSOC_ENTITYLISTS</code> */
	public final static QName ASSOC_ENTITYLISTS = QName.createQName(BECPG_URI, "entityLists");

	/** Constant <code>ASPECT_HIDDEN_FOLDER</code> */
	public final static QName ASPECT_HIDDEN_FOLDER = QName.createQName(BECPG_URI, "hiddenFolder");

	/** Constant <code>TYPE_ENTITYLIST_ITEM</code> */
	public final static QName TYPE_ENTITYLIST_ITEM = QName.createQName(BECPG_URI, "entityListItem");

	/** Constant <code>ASPECT_ENTITYLIST_STATE</code> */
	public final static QName ASPECT_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListStateAspect");

	/** Constant <code>PROP_ENTITYLIST_STATE</code> */
	public final static QName PROP_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListState");

	// Caract
	/** Constant <code>TYPE_CHARACT</code> */
	public final static QName TYPE_CHARACT = QName.createQName(BeCPGModel.BECPG_URI, "charact");
	/** Constant <code>PROP_CHARACT_NAME</code> */
	public final static QName PROP_CHARACT_NAME = QName.createQName(BECPG_URI, "charactName");

	// linkedValue
	/** Constant <code>TYPE_LINKED_VALUE</code> */
	public final static QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI, "linkedValue");
	/** Constant <code>PROP_LKV_VALUE</code> */
	public final static QName PROP_LKV_VALUE = QName.createQName(BECPG_URI, "lkvValue");

	// listValue
	/** Constant <code>TYPE_LIST_VALUE</code> */
	public final static QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI, "listValue");
	/** Constant <code>PROP_LV_VALUE</code> */
	public final static QName PROP_LV_VALUE = QName.createQName(BECPG_URI, "lvValue");
	/** Constant <code>PROP_LV_CODE</code> */
	public final static QName PROP_LV_CODE = QName.createQName(BECPG_URI, "lvCode");

	/** Constant <code>TYPE_ACTIVITY_LIST</code> */
	public final static QName TYPE_ACTIVITY_LIST = QName.createQName(BECPG_URI, "activityList");
	/** Constant <code>PROP_ACTIVITYLIST_TYPE</code> */
	public final static QName PROP_ACTIVITYLIST_TYPE = QName.createQName(BECPG_URI, "alType");
	/** Constant <code>PROP_ACTIVITYLIST_DATA</code> */
	public final static QName PROP_ACTIVITYLIST_DATA = QName.createQName(BECPG_URI, "alData");
	/** Constant <code>PROP_ACTIVITYLIST_USERID</code> */
	public final static QName PROP_ACTIVITYLIST_USERID = QName.createQName(BECPG_URI, "alUserId");

	// Notification
	/** Constant <code>TYPE_NOTIFICATIONRULELIST</code> */
	public final static QName TYPE_NOTIFICATIONRULELIST = QName.createQName(BeCPGModel.BECPG_URI, "notificationRuleList");

	// entity
	/** Constant <code>TYPE_ENTITY_V2</code> */
	public final static QName TYPE_ENTITY_V2 = QName.createQName(BECPG_URI, "entityV2");

	/** Constant <code>TYPE_SYSTEM_ENTITY</code> */
	public final static QName TYPE_SYSTEM_ENTITY = QName.createQName(BECPG_URI, "systemEntity");

	// autoNum
	/** Constant <code>TYPE_AUTO_NUM</code> */
	public final static QName TYPE_AUTO_NUM = QName.createQName(BECPG_URI, "autoNum");
	/** Constant <code>PROP_AUTO_NUM_CLASS_NAME</code> */
	public final static QName PROP_AUTO_NUM_CLASS_NAME = QName.createQName(BECPG_URI, "autoNumClassName");
	/** Constant <code>PROP_AUTO_NUM_PROPERTY_NAME</code> */
	public final static QName PROP_AUTO_NUM_PROPERTY_NAME = QName.createQName(BECPG_URI, "autoNumPropertyName");
	/** Constant <code>PROP_AUTO_NUM_VALUE</code> */
	public final static QName PROP_AUTO_NUM_VALUE = QName.createQName(BECPG_URI, "autoNumValue");
	/** Constant <code>PROP_AUTO_NUM_PREFIX</code> */
	public final static QName PROP_AUTO_NUM_PREFIX = QName.createQName(BECPG_URI, "autoNumPrefix");

	// entityTpl aspect
	/** Constant <code>ASPECT_ENTITY_TPL</code> */
	public final static QName ASPECT_ENTITY_TPL = QName.createQName(BECPG_URI, "entityTplAspect");
	/** Constant <code>PROP_ENTITY_TPL_ENABLED</code> */
	public final static QName PROP_ENTITY_TPL_ENABLED = QName.createQName(BECPG_URI, "entityTplEnabled");
	/** Constant <code>PROP_ENTITY_TPL_IS_DEFAULT</code> */
	public final static QName PROP_ENTITY_TPL_IS_DEFAULT = QName.createQName(BECPG_URI, "entityTplIsDefault");
	/** Constant <code>PROP_ENTITY_TPL_DEFAULT_DEST</code> */
	public final static QName PROP_ENTITY_TPL_DEFAULT_DEST = QName.createQName(BECPG_URI, "entityTplDefaultDest");
	/** Constant <code>ASSOC_ENTITY_TPL_SCRIPT</code> */
	public static final QName ASSOC_ENTITY_TPL_SCRIPT = QName.createQName(BECPG_URI, "entityTplScript");

	// entityTplRef aspect
	/** Constant <code>ASPECT_ENTITY_TPL_REF</code> */
	public final static QName ASPECT_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRefAspect");
	/** Constant <code>ASSOC_ENTITY_TPL_REF</code> */
	public final static QName ASSOC_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRef");

	// depthLevel aspect
	/** Constant <code>ASPECT_DEPTH_LEVEL</code> */
	public final static QName ASPECT_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevelAspect");
	/** Constant <code>PROP_DEPTH_LEVEL</code> */
	public final static QName PROP_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevel");
	/** Constant <code>PROP_PARENT_LEVEL</code> */
	public final static QName PROP_PARENT_LEVEL = QName.createQName(BECPG_URI, "parentLevel");

	// depthLevel aspect
	/** Constant <code>ASPECT_SORTABLE_LIST</code> */
	public final static QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI, "sortableListAspect");
	/** Constant <code>PROP_SORT</code> */
	public final static QName PROP_SORT = QName.createQName(BECPG_URI, "sort");

	// detaillableListItem aspect
	/** Constant <code>ASPECT_DETAILLABLE_LIST_ITEM</code> */
	public final static QName ASPECT_DETAILLABLE_LIST_ITEM = QName.createQName(BECPG_URI, "detaillableListItemAspect");

	// manual aspect
	/** Constant <code>ASPECT_IS_MANUAL_LISTITEM</code> */
	public final static QName ASPECT_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItemAspect");
	/** Constant <code>PROP_IS_MANUAL_LISTITEM</code> */
	public final static QName PROP_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItem");

	// version aspect
	/** Constant <code>ASPECT_COMPOSITE_VERSION</code> */
	public final static QName ASPECT_COMPOSITE_VERSION = QName.createQName(BECPG_URI, "compositeVersion");
	/** Constant <code>PROP_VERSION_LABEL</code> */
	public final static QName PROP_VERSION_LABEL = QName.createQName(BECPG_URI, "versionLabel");

	// permissionsTpl
	/** Constant <code>ASPECT_PERMISSIONS_TPL</code> */
	public final static QName ASPECT_PERMISSIONS_TPL = QName.createQName(BECPG_URI, "permissionsTpl");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS</code> */
	public final static QName ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS = QName.createQName(BECPG_URI, "consumerGroups");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS</code> */
	public final static QName ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS = QName.createQName(BECPG_URI, "editorGroups");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS</code> */
	public final static QName ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS = QName.createQName(BECPG_URI, "contributorGroups");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS</code> */
	public final static QName ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS = QName.createQName(BECPG_URI, "collaboratorGroups");

	// code aspect
	/** Constant <code>ASPECT_CODE</code> */
	public final static QName ASPECT_CODE = QName.createQName(BECPG_URI, "codeAspect");
	/** Constant <code>PROP_CODE</code> */
	public final static QName PROP_CODE = QName.createQName(BECPG_URI, "code");

	// score aspect
	/** Constant <code>ASPECT_ENTITY_SCORE</code> */
	public final static QName ASPECT_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScoreAspect");
	/** Constant <code>PROP_ENTITY_SCORE</code> */
	public final static QName PROP_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScore");

	// systemFolder
	/** Constant <code>ASPECT_SYSTEM_FOLDER</code> */
	public final static QName ASPECT_SYSTEM_FOLDER = QName.createQName(BECPG_URI, "systemFolderAspect");
	/** Constant <code>ASPECT_DOC_LINKED_ENTITIES</code> */
	public final static QName ASPECT_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntitiesAspect");
	/** Constant <code>ASSOC_DOC_LINKED_ENTITIES</code> */
	public final static QName ASSOC_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntities");

	// formulatedEntity
	/** Constant <code>ASPECT_FORMULATED_ENTITY</code> */
	public final static QName ASPECT_FORMULATED_ENTITY = QName.createQName(BECPG_URI, "formulatedEntityAspect");
	/** Constant <code>PROP_FORMULATED_DATE</code> */
	public final static QName PROP_FORMULATED_DATE = QName.createQName(BECPG_URI, "formulatedDate");

	// effectivity
	/** Constant <code>ASPECT_EFFECTIVITY</code> */
	public final static QName ASPECT_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "effectivityAspect");
	/** Constant <code>PROP_START_EFFECTIVITY</code> */
	public final static QName PROP_START_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "startEffectivity");
	/** Constant <code>PROP_END_EFFECTIVITY</code> */
	public final static QName PROP_END_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "endEffectivity");

	// color
	/** Constant <code>ASPECT_COLOR</code> */
	public final static QName ASPECT_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "colorAspect");
	/** Constant <code>PROP_COLOR</code> */
	public final static QName PROP_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "color");

	// legalName aspect
	/** Constant <code>ASPECT_LEGAL_NAME</code> */
	public final static QName ASPECT_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalNameAspect");
	/** Constant <code>PROP_LEGAL_NAME</code> */
	public final static QName PROP_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalName");
	/** Constant <code>PROP_PLURAL_LEGAL_NAME</code> */
	public final static QName PROP_PLURAL_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "pluralLegalName");

	// isDeleted aspect
	/** Constant <code>ASPECT_DELETED</code> */
	public final static QName ASPECT_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeletedAspect");
	/** Constant <code>PROP_IS_DELETED</code> */
	public final static QName PROP_IS_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeleted");

	/** Constant <code>ASPECT_COMPARE_WITH</code> */
	public final static QName ASPECT_COMPARE_WITH = QName.createQName(BeCPGModel.BECPG_URI, "compareWithAspect");
	/** Constant <code>ASSOC_COMPARE_WITH_ENTITIES</code> */
	public final static QName ASSOC_COMPARE_WITH_ENTITIES = QName.createQName(BECPG_URI, "compareWithEntities");

	/** Constant <code>ASPECT_ENTITY_BRANCH</code> */
	public final static QName ASPECT_ENTITY_BRANCH = QName.createQName(BeCPGModel.BECPG_URI, "entityBranchAspect");
	/** Constant <code>ASSOC_BRANCH_FROM_ENTITY</code> */
	public final static QName ASSOC_BRANCH_FROM_ENTITY = QName.createQName(BECPG_URI, "branchFromEntity");
	/** Constant <code>PROP_BRANCH_FROM_VERSION_LABEL</code> */
	public final static QName PROP_BRANCH_FROM_VERSION_LABEL = QName.createQName(BECPG_URI, "branchFromVersionLabel");
	/** Constant <code>ASSOC_AUTO_MERGE_TO</code> */
	public final static QName ASSOC_AUTO_MERGE_TO = QName.createQName(BECPG_URI, "autoMergeTo");

	/** Constant <code>PROP_AUTO_MERGE_VERSIONTYPE</code> */
	public final static QName PROP_AUTO_MERGE_VERSIONTYPE = QName.createQName(BECPG_URI, "autoMergeVersionType");
	/** Constant <code>PROP_AUTO_MERGE_COMMENTS</code> */
	public final static QName PROP_AUTO_MERGE_COMMENTS = QName.createQName(BECPG_URI, "autoMergeComments");
	/** Constant <code>PROP_AUTO_MERGE_IMPACTWUSED</code> */
	public final static QName PROP_AUTO_MERGE_IMPACTWUSED = QName.createQName(BECPG_URI, "autoMergeImpactWUsed");
	/** Constant <code>ASPECT_AUTO_MERGE_ASPECT</code> */
	public final static QName ASPECT_AUTO_MERGE_ASPECT = QName.createQName(BeCPGModel.BECPG_URI, "autoMergeAspect");

	/** Constant <code>ASPECT_LINKED_SEARCH</code> */
	public final static QName ASPECT_LINKED_SEARCH = QName.createQName(BECPG_URI, "linkedSearchAspect");
	/** Constant <code>ASSOC_LINKED_SEARCH_ASSOCIATION</code> */
	public final static QName ASSOC_LINKED_SEARCH_ASSOCIATION = QName.createQName(BECPG_URI, "linkedSearchAssociation");

	// Audit state aspect
	/** Constant <code>PROP_STATE_ACTIVITY_MODIFIED</code> */
	public final static QName PROP_STATE_ACTIVITY_MODIFIED = QName.createQName(BECPG_URI, "stateActivityModified");
	/** Constant <code>PROP_STATE_ACTIVITY_MODIFIER</code> */
	public final static QName PROP_STATE_ACTIVITY_MODIFIER = QName.createQName(BECPG_URI, "stateActivityModifier");
	/** Constant <code>PROP_STATE_ACTIVITY_PREVIOUSSTATE</code> */
	public final static QName PROP_STATE_ACTIVITY_PREVIOUSSTATE = QName.createQName(BECPG_URI, "stateActivityPreviousState");

	/** Constant <code>PROP_USER_LOCAL</code> */
	public final static QName PROP_USER_LOCAL = QName.createQName(BECPG_URI, "userLocale");
	/** Constant <code>PROP_USER_CONTENT_LOCAL</code> */
	public final static QName PROP_USER_CONTENT_LOCAL = QName.createQName(BECPG_URI, "userContentLocale");
	// code aspect
	/** Constant <code>ASPECT_ERP_CODE</code> */
	public final static QName ASPECT_ERP_CODE = QName.createQName(BECPG_URI, "erpCodeAspect");
	/** Constant <code>PROP_ERP_CODE</code> */
	public final static QName PROP_ERP_CODE = QName.createQName(BECPG_URI, "erpCode");

	// WorkflowEntity
	/** Constant <code>ASSOC_WORKFLOW_ENTITY</code> */
	public final static QName ASSOC_WORKFLOW_ENTITY = QName.createQName(BECPG_URI, "workflowEntity");

	// variant
	/** Constant <code>ASPECT_ENTITYLIST_VARIANT</code> */
	public final static QName ASPECT_ENTITYLIST_VARIANT = QName.createQName(BECPG_URI, "entityListVariantAspect");
	/** Constant <code>ASPECT_ENTITY_VARIANT</code> */
	public final static QName ASPECT_ENTITY_VARIANT = QName.createQName(BECPG_URI, "entityVariantAspect");
	/** Constant <code>ASSOC_VARIANTS</code> */
	public final static QName ASSOC_VARIANTS = QName.createQName(BECPG_URI, "variants");
	/** Constant <code>TYPE_VARIANT</code> */
	public final static QName TYPE_VARIANT = QName.createQName(BECPG_URI, "variant");
	/** Constant <code>PROP_VARIANTIDS</code> */
	public final static QName PROP_VARIANTIDS = QName.createQName(BECPG_URI, "variantIds");
	/** Constant <code>PROP_IS_DEFAULT_VARIANT</code> */
	public final static QName PROP_IS_DEFAULT_VARIANT = QName.createQName(BECPG_URI, "isDefaultVariant");
	

}
