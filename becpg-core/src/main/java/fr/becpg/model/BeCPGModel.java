package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * beCPG model definition.
 * 
 * @author querephi
 */
public interface BeCPGModel {

	String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	String BECPG_PREFIX = "bcpg";
	QName MODEL = QName.createQName(BECPG_URI, "bcpgmodel");

	// entityListsAspect
	QName ASPECT_ENTITYLISTS = QName.createQName(BECPG_URI, "entityListsAspect");
	QName ASSOC_ENTITYLISTS = QName.createQName(BECPG_URI, "entityLists");

	QName ASPECT_HIDDEN_FOLDER = QName.createQName(BECPG_URI, "hiddenFolder");

	QName TYPE_ENTITYLIST_ITEM = QName.createQName(BECPG_URI, "entityListItem");
	
	QName ASPECT_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListStateAspect");
	
	QName PROP_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListState");
	
    // Caract
	QName TYPE_CHARACT = QName.createQName(BeCPGModel.BECPG_URI, "charact");
	QName PROP_CHARACT_NAME  = QName.createQName(BECPG_URI, "charactName");
	
	// linkedValue
	QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI, "linkedValue");
	QName PROP_LKV_VALUE = QName.createQName(BECPG_URI, "lkvValue");

	// listValue
	QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI, "listValue");
	QName PROP_LV_VALUE = QName.createQName(BECPG_URI, "lvValue");
	QName PROP_LV_CODE = QName.createQName(BECPG_URI, "lvCode");
	
	QName TYPE_ACTIVITY_LIST = QName.createQName(BECPG_URI, "activityList");
	QName PROP_ACTIVITYLIST_TYPE =  QName.createQName(BECPG_URI, "alType");
	QName PROP_ACTIVITYLIST_DATA =  QName.createQName(BECPG_URI, "alData");
	QName PROP_ACTIVITYLIST_USERID=  QName.createQName(BECPG_URI, "alUserId");
	
	//Notification 
	QName TYPE_NOTIFICATIONRULELIST = QName.createQName(BeCPGModel.BECPG_URI, "notificationRuleList");
	
	// entity
	QName TYPE_ENTITY_V2 = QName.createQName(BECPG_URI, "entityV2");

	QName TYPE_SYSTEM_ENTITY = QName.createQName(BECPG_URI, "systemEntity");

	// autoNum
	QName TYPE_AUTO_NUM = QName.createQName(BECPG_URI, "autoNum");
	QName PROP_AUTO_NUM_CLASS_NAME = QName.createQName(BECPG_URI, "autoNumClassName");
	QName PROP_AUTO_NUM_PROPERTY_NAME = QName.createQName(BECPG_URI, "autoNumPropertyName");
	QName PROP_AUTO_NUM_VALUE = QName.createQName(BECPG_URI, "autoNumValue");
	QName PROP_AUTO_NUM_PREFIX = QName.createQName(BECPG_URI, "autoNumPrefix");

	// entityTpl aspect
	QName ASPECT_ENTITY_TPL = QName.createQName(BECPG_URI, "entityTplAspect");
	QName PROP_ENTITY_TPL_ENABLED = QName.createQName(BECPG_URI, "entityTplEnabled");
	QName PROP_ENTITY_TPL_IS_DEFAULT = QName.createQName(BECPG_URI, "entityTplIsDefault");
	QName PROP_ENTITY_TPL_DEFAULT_DEST = QName.createQName(BECPG_URI, "entityTplDefaultDest");

	// entityTplRef aspect
	QName ASPECT_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRefAspect");
	QName ASSOC_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRef");

	// depthLevel aspect
	QName ASPECT_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevelAspect");
	QName PROP_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevel");
	QName PROP_PARENT_LEVEL = QName.createQName(BECPG_URI, "parentLevel");

	// depthLevel aspect
	QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI, "sortableListAspect");
	QName PROP_SORT = QName.createQName(BECPG_URI, "sort");

	// detaillableListItem aspect
	QName ASPECT_DETAILLABLE_LIST_ITEM = QName.createQName(BECPG_URI, "detaillableListItemAspect");

	// manual aspect
	QName ASPECT_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItemAspect");
	QName PROP_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItem");

	// version aspect
	QName ASPECT_COMPOSITE_VERSION = QName.createQName(BECPG_URI, "compositeVersion");
	QName PROP_VERSION_LABEL = QName.createQName(BECPG_URI, "versionLabel");

	// permissionsTpl
	QName ASPECT_PERMISSIONS_TPL = QName.createQName(BECPG_URI, "permissionsTpl");
	QName ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS = QName.createQName(BECPG_URI, "consumerGroups");
	QName ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS = QName.createQName(BECPG_URI, "editorGroups");
	QName ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS = QName.createQName(BECPG_URI, "contributorGroups");
	QName ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS = QName.createQName(BECPG_URI, "collaboratorGroups");

	// code aspect
	QName ASPECT_CODE = QName.createQName(BECPG_URI, "codeAspect");
	QName PROP_CODE = QName.createQName(BECPG_URI, "code");
	
	// score aspect
	QName ASPECT_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScoreAspect");
	QName PROP_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScore");

	// systemFolder
	QName ASPECT_SYSTEM_FOLDER = QName.createQName(BECPG_URI, "systemFolderAspect");
	QName ASPECT_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntitiesAspect");
	QName ASSOC_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntities");

	//formulatedEntity
	QName ASPECT_FORMULATED_ENTITY = QName.createQName(BECPG_URI, "formulatedEntityAspect");
	QName PROP_FORMULATED_DATE = QName.createQName(BECPG_URI, "formulatedDate");

	// effectivity
	QName ASPECT_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "effectivityAspect");
	QName PROP_START_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "startEffectivity");
	QName PROP_END_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "endEffectivity");

	// color
	QName ASPECT_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "colorAspect");
	QName PROP_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "color");

	// legalName aspect
	QName ASPECT_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalNameAspect");
	QName PROP_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalName");
	QName PROP_PLURAL_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "pluralLegalName");

	// isDeleted aspect
	QName ASPECT_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeletedAspect");
	QName PROP_IS_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeleted");
	
	
	QName ASPECT_COMPARE_WITH = QName.createQName(BeCPGModel.BECPG_URI, "compareWithAspect");
	QName ASSOC_COMPARE_WITH_ENTITIES = QName.createQName(BECPG_URI, "compareWithEntities");
	
	QName ASPECT_ENTITY_BRANCH = QName.createQName(BeCPGModel.BECPG_URI, "entityBranchAspect");
	QName ASSOC_BRANCH_FROM_ENTITY = QName.createQName(BECPG_URI, "branchFromEntity");
	QName PROP_BRANCH_FROM_VERSION_LABEL = QName.createQName(BECPG_URI, "branchFromVersionLabel");
	QName ASSOC_AUTO_MERGE_TO = QName.createQName(BECPG_URI, "autoMergeTo");
	

	QName PROP_AUTO_MERGE_VERSIONTYPE =  QName.createQName(BECPG_URI, "autoMergeVersionType");
	QName PROP_AUTO_MERGE_COMMENTS  =  QName.createQName(BECPG_URI, "autoMergeComments");
	QName PROP_AUTO_MERGE_IMPACTWUSED  =  QName.createQName(BECPG_URI, "autoMergeImpactWUsed");
	QName ASPECT_AUTO_MERGE_ASPECT = QName.createQName(BeCPGModel.BECPG_URI, "autoMergeAspect");
	
	
	QName ASPECT_LINKED_SEARCH = QName.createQName(BECPG_URI, "linkedSearchAspect");
	QName ASSOC_LINKED_SEARCH_ASSOCIATION = QName.createQName(BECPG_URI, "linkedSearchAssociation");
	
	//Audit state aspect
	QName PROP_STATE_ACTIVITY_MODIFIED = QName.createQName(BECPG_URI,"stateActivityModified");
	QName PROP_STATE_ACTIVITY_MODIFIER = QName.createQName(BECPG_URI,"stateActivityModifier");
	QName PROP_STATE_ACTIVITY_PREVIOUSSTATE = QName.createQName(BECPG_URI,"stateActivityPreviousState");
	
	
	QName PROP_USER_LOCAL = QName.createQName(BECPG_URI,"userLocale");
	QName PROP_USER_CONTENT_LOCAL = QName.createQName(BECPG_URI,"userContentLocale");
	// code aspect
	QName ASPECT_ERP_CODE = QName.createQName(BECPG_URI, "erpCodeAspect");
	QName PROP_ERP_CODE = QName.createQName(BECPG_URI, "erpCode");

	// WorkflowEntity
	QName ASSOC_WORKFLOW_ENTITY = QName.createQName(BECPG_URI, "workflowEntity");
	
	// variant
	QName ASPECT_ENTITYLIST_VARIANT = QName.createQName(BECPG_URI, "entityListVariantAspect");
	QName ASPECT_ENTITY_VARIANT = QName.createQName(BECPG_URI, "entityVariantAspect");
	QName ASSOC_VARIANTS = QName.createQName(BECPG_URI, "variants");
	QName TYPE_VARIANT = QName.createQName(BECPG_URI, "variant");
	QName PROP_VARIANTIDS = QName.createQName(BECPG_URI, "variantIds");
	QName PROP_IS_DEFAULT_VARIANT = QName.createQName(BECPG_URI, "isDefaultVariant");
	
}
