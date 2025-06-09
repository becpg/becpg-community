package fr.becpg.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * beCPG model definition.
 *
 * @author querephi
 * @version $Id: $Id
 */
public final class BeCPGModel {

	private BeCPGModel() {
		throw new IllegalStateException("Constants class helper only");
	}

	/** Constant <code>BECPG_URI="http://www.bcpg.fr/model/becpg/1.0"</code> */
	public static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	/** Constant <code>BECPG_PREFIX="bcpg"</code> */
	public static final String BECPG_PREFIX = "bcpg";
	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(BECPG_URI, "bcpgmodel");

	// entityListsAspect
	/** Constant <code>ASPECT_ENTITYLISTS</code> */
	public static final QName ASPECT_ENTITYLISTS = QName.createQName(BECPG_URI, "entityListsAspect");
	/** Constant <code>ASSOC_ENTITYLISTS</code> */
	public static final QName ASSOC_ENTITYLISTS = QName.createQName(BECPG_URI, "entityLists");

	/** Constant <code>ASPECT_HIDDEN_FOLDER</code> */
	public static final QName ASPECT_HIDDEN_FOLDER = QName.createQName(BECPG_URI, "hiddenFolder");

	/** Constant <code>TYPE_ENTITYLIST_ITEM</code> */
	public static final QName TYPE_ENTITYLIST_ITEM = QName.createQName(BECPG_URI, "entityListItem");

	/** Constant <code>ASPECT_ENTITYLIST_STATE</code> */
	public static final QName ASPECT_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListStateAspect");

	/** Constant <code>PROP_ENTITYLIST_STATE</code> */
	public static final QName PROP_ENTITYLIST_STATE = QName.createQName(BECPG_URI, "entityDataListState");

	// Format
	/** Constant <code>ASPECT_ENTITY_FORMAT</code> */
	public static final QName ASPECT_ENTITY_FORMAT = QName.createQName(BECPG_URI, "entityFormatAspect");

	/** Constant <code>PROP_ENTITY_FORMAT</code> */
	public static final QName PROP_ENTITY_FORMAT = QName.createQName(BECPG_URI, "entityFormat");

	/** Constant <code>PROP_ENTITY_DATA</code> */
	public static final QName PROP_ENTITY_DATA = QName.createQName(BECPG_URI, "entityData");

	// archived entity aspect
	/** Constant <code>ASPECT_ARCHIVED_ENTITY</code> */
	public static final QName ASPECT_ARCHIVED_ENTITY = QName.createQName(BECPG_URI, "archivedEntityAspect");
	
	/** Constant <code>ASPECT_USER_AUTHENTICATION</code> */
	public static final QName ASPECT_USER_AUTHENTICATION = QName.createQName(BECPG_URI, "userAuthenticationAspect");
	
	/** Constant <code>PROP_IS_SSO_USER</code> */
	public static final QName PROP_IS_SSO_USER = QName.createQName(BECPG_URI, "isSsoUser");
	
	/** Constant <code>PROP_GENERATE_PASSWORD</code> */
	public static final QName PROP_GENERATE_PASSWORD = QName.createQName(BECPG_URI, "generatePassword");

	public enum EntityFormat {
		NODE, JSON, XML
	}

	// Caract
	/** Constant <code>TYPE_CHARACT</code> */
	public static final QName TYPE_CHARACT = QName.createQName(BeCPGModel.BECPG_URI, "charact");
	/** Constant <code>PROP_CHARACT_NAME</code> */
	public static final QName PROP_CHARACT_NAME = QName.createQName(BECPG_URI, "charactName");

	// linkedValue
	/** Constant <code>TYPE_LINKED_VALUE</code> */
	public static final QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI, "linkedValue");
	/** Constant <code>PROP_LKV_VALUE</code> */
	public static final QName PROP_LKV_VALUE = QName.createQName(BECPG_URI, "lkvValue");

	// listValue
	/** Constant <code>TYPE_LIST_VALUE</code> */
	public static final QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI, "listValue");
	/** Constant <code>PROP_LV_VALUE</code> */
	public static final QName PROP_LV_VALUE = QName.createQName(BECPG_URI, "lvValue");
	/** Constant <code>PROP_LV_CODE</code> */
	public static final QName PROP_LV_CODE = QName.createQName(BECPG_URI, "lvCode");

	/** Constant <code>TYPE_ACTIVITY_LIST</code> */
	public static final QName TYPE_ACTIVITY_LIST = QName.createQName(BECPG_URI, "activityList");
	/** Constant <code>PROP_ACTIVITYLIST_TYPE</code> */
	public static final QName PROP_ACTIVITYLIST_TYPE = QName.createQName(BECPG_URI, "alType");
	/** Constant <code>PROP_ACTIVITYLIST_DATA</code> */
	public static final QName PROP_ACTIVITYLIST_DATA = QName.createQName(BECPG_URI, "alData");
	/** Constant <code>PROP_ACTIVITYLIST_USERID</code> */
	public static final QName PROP_ACTIVITYLIST_USERID = QName.createQName(BECPG_URI, "alUserId");

	// Notification
	/** Constant <code>TYPE_NOTIFICATIONRULELIST</code> */
	public static final QName TYPE_NOTIFICATIONRULELIST = QName.createQName(BeCPGModel.BECPG_URI, "notificationRuleList");

	// entity
	/** Constant <code>TYPE_ENTITY_V2</code> */
	public static final QName TYPE_ENTITY_V2 = QName.createQName(BECPG_URI, "entityV2");

	/** Constant <code>TYPE_SYSTEM_ENTITY</code> */
	public static final QName TYPE_SYSTEM_ENTITY = QName.createQName(BECPG_URI, "systemEntity");
	
	/** Constant <code>TYPE_CONTENT</code> */
	public static final QName TYPE_CONTENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");

	// autoNum
	/** Constant <code>TYPE_AUTO_NUM</code> */
	public static final QName TYPE_AUTO_NUM = QName.createQName(BECPG_URI, "autoNum");
	/** Constant <code>PROP_AUTO_NUM_CLASS_NAME</code> */
	public static final QName PROP_AUTO_NUM_CLASS_NAME = QName.createQName(BECPG_URI, "autoNumClassName");
	/** Constant <code>PROP_AUTO_NUM_PROPERTY_NAME</code> */
	public static final QName PROP_AUTO_NUM_PROPERTY_NAME = QName.createQName(BECPG_URI, "autoNumPropertyName");
	/** Constant <code>PROP_AUTO_NUM_VALUE</code> */
	public static final QName PROP_AUTO_NUM_VALUE = QName.createQName(BECPG_URI, "autoNumValue");
	/** Constant <code>PROP_AUTO_NUM_PREFIX</code> */
	public static final QName PROP_AUTO_NUM_PREFIX = QName.createQName(BECPG_URI, "autoNumPrefix");

	// entityTpl aspect
	/** Constant <code>ASPECT_ENTITY_TPL</code> */
	public static final QName ASPECT_ENTITY_TPL = QName.createQName(BECPG_URI, "entityTplAspect");
	/** Constant <code>PROP_ENTITY_TPL_ENABLED</code> */
	public static final QName PROP_ENTITY_TPL_ENABLED = QName.createQName(BECPG_URI, "entityTplEnabled");
	/** Constant <code>PROP_ENTITY_TPL_IS_DEFAULT</code> */
	public static final QName PROP_ENTITY_TPL_IS_DEFAULT = QName.createQName(BECPG_URI, "entityTplIsDefault");
	/** Constant <code>PROP_ENTITY_TPL_DEFAULT_DEST</code> */
	public static final QName PROP_ENTITY_TPL_DEFAULT_DEST = QName.createQName(BECPG_URI, "entityTplDefaultDest");
	/** Constant <code>ASSOC_ENTITY_TPL_SCRIPT</code> */
	public static final QName ASSOC_ENTITY_TPL_SCRIPT = QName.createQName(BECPG_URI, "entityTplScript");

	// entityTplRef aspect
	/** Constant <code>ASPECT_ENTITY_TPL_REF</code> */
	public static final QName ASPECT_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRefAspect");
	/** Constant <code>ASSOC_ENTITY_TPL_REF</code> */
	public static final QName ASSOC_ENTITY_TPL_REF = QName.createQName(BECPG_URI, "entityTplRef");

	// depthLevel aspect
	/** Constant <code>ASPECT_DEPTH_LEVEL</code> */
	public static final QName ASPECT_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevelAspect");
	/** Constant <code>PROP_DEPTH_LEVEL</code> */
	public static final QName PROP_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevel");
	/** Constant <code>PROP_PARENT_LEVEL</code> */
	public static final QName PROP_PARENT_LEVEL = QName.createQName(BECPG_URI, "parentLevel");

	// depthLevel aspect
	/** Constant <code>ASPECT_SORTABLE_LIST</code> */
	public static final QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI, "sortableListAspect");
	/** Constant <code>PROP_SORT</code> */
	public static final QName PROP_SORT = QName.createQName(BECPG_URI, "sort");

	// detaillableListItem aspect
	/** Constant <code>ASPECT_DETAILLABLE_LIST_ITEM</code> */
	public static final QName ASPECT_DETAILLABLE_LIST_ITEM = QName.createQName(BECPG_URI, "detaillableListItemAspect");

	// manual aspect
	/** Constant <code>ASPECT_IS_MANUAL_LISTITEM</code> */
	public static final QName ASPECT_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItemAspect");
	/** Constant <code>PROP_IS_MANUAL_LISTITEM</code> */
	public static final QName PROP_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItem");

	// version aspect
	/** Constant <code>ASPECT_COMPOSITE_VERSION</code> */
	public static final QName ASPECT_COMPOSITE_VERSION = QName.createQName(BECPG_URI, "compositeVersion");
	/** Constant <code>PROP_VERSION_LABEL</code> */
	public static final QName PROP_VERSION_LABEL = QName.createQName(BECPG_URI, "versionLabel");
	/** Constant <code>PROP_MANUAL_VERSION_LABEL</code> */
	public static final QName PROP_MANUAL_VERSION_LABEL = QName.createQName(BECPG_URI, "manualVersionLabel");

	// permissionsTpl
	/** Constant <code>ASPECT_PERMISSIONS_TPL</code> */
	public static final QName ASPECT_PERMISSIONS_TPL = QName.createQName(BECPG_URI, "permissionsTpl");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS</code> */
	public static final QName ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS = QName.createQName(BECPG_URI, "consumerGroups");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS</code> */
	public static final QName ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS = QName.createQName(BECPG_URI, "editorGroups");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS</code> */
	public static final QName ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS = QName.createQName(BECPG_URI, "contributorGroups");
	/** Constant <code>ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS</code> */
	public static final QName ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS = QName.createQName(BECPG_URI, "collaboratorGroups");

	// code aspect
	/** Constant <code>ASPECT_CODE</code> */
	public static final QName ASPECT_CODE = QName.createQName(BECPG_URI, "codeAspect");
	/** Constant <code>PROP_CODE</code> */
	public static final QName PROP_CODE = QName.createQName(BECPG_URI, "code");

	// score aspect
	/** Constant <code>ASPECT_ENTITY_SCORE</code> */
	public static final QName ASPECT_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScoreAspect");
	/** Constant <code>PROP_ENTITY_SCORE</code> */
	public static final QName PROP_ENTITY_SCORE = QName.createQName(BECPG_URI, "entityScore");

	// sub entity aspect
	/** Constant <code>ASPECT_SUB_ENTITY</code> */
	public static final QName ASPECT_SUB_ENTITY = QName.createQName(BECPG_URI, "subEntityAspect");
	/** Constant <code>ASSOC_PARENT_ENTITY</code> */
	public static final QName ASSOC_PARENT_ENTITY = QName.createQName(BECPG_URI, "parentEntityRef");

	// systemFolder
	/** Constant <code>ASPECT_SYSTEM_FOLDER</code> */
	public static final QName ASPECT_SYSTEM_FOLDER = QName.createQName(BECPG_URI, "systemFolderAspect");
	/** Constant <code>ASPECT_DOC_LINKED_ENTITIES</code> */
	public static final QName ASPECT_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntitiesAspect");
	/** Constant <code>ASSOC_DOC_LINKED_ENTITIES</code> */
	public static final QName ASSOC_DOC_LINKED_ENTITIES = QName.createQName(BECPG_URI, "docLinkedEntities");

	// formulatedEntity
	/** Constant <code>ASPECT_FORMULATED_ENTITY</code> */
	public static final QName ASPECT_FORMULATED_ENTITY = QName.createQName(BECPG_URI, "formulatedEntityAspect");
	/** Constant <code>PROP_FORMULATED_DATE</code> */
	public static final QName PROP_FORMULATED_DATE = QName.createQName(BECPG_URI, "formulatedDate");

	// effectivity
	/** Constant <code>ASPECT_EFFECTIVITY</code> */
	public static final QName ASPECT_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "effectivityAspect");
	/** Constant <code>PROP_START_EFFECTIVITY</code> */
	public static final QName PROP_START_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "startEffectivity");
	/** Constant <code>PROP_END_EFFECTIVITY</code> */
	public static final QName PROP_END_EFFECTIVITY = QName.createQName(BeCPGModel.BECPG_URI, "endEffectivity");

	// color
	/** Constant <code>ASPECT_COLOR</code> */
	public static final QName ASPECT_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "colorAspect");
	/** Constant <code>PROP_COLOR</code> */
	public static final QName PROP_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "color");

	// legalName aspect
	/** Constant <code>ASPECT_LEGAL_NAME</code> */
	public static final QName ASPECT_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalNameAspect");
	/** Constant <code>PROP_LEGAL_NAME</code> */
	public static final QName PROP_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalName");
	/** Constant <code>PROP_PLURAL_LEGAL_NAME</code> */
	public static final QName PROP_PLURAL_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "pluralLegalName");

	// isDeleted aspect
	/** Constant <code>ASPECT_DELETED</code> */
	public static final QName ASPECT_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeletedAspect");
	/** Constant <code>PROP_IS_DELETED</code> */
	public static final QName PROP_IS_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeleted");

	/** Constant <code>ASPECT_COMPARE_WITH</code> */
	public static final QName ASPECT_COMPARE_WITH = QName.createQName(BeCPGModel.BECPG_URI, "compareWithAspect");
	/** Constant <code>ASSOC_COMPARE_WITH_ENTITIES</code> */
	public static final QName ASSOC_COMPARE_WITH_ENTITIES = QName.createQName(BECPG_URI, "compareWithEntities");

	/** Constant <code>ASPECT_ENTITY_BRANCH</code> */
	public static final QName ASPECT_ENTITY_BRANCH = QName.createQName(BeCPGModel.BECPG_URI, "entityBranchAspect");
	/** Constant <code>ASSOC_BRANCH_FROM_ENTITY</code> */
	public static final QName ASSOC_BRANCH_FROM_ENTITY = QName.createQName(BECPG_URI, "branchFromEntity");
	/** Constant <code>PROP_BRANCH_FROM_VERSION_LABEL</code> */
	public static final QName PROP_BRANCH_FROM_VERSION_LABEL = QName.createQName(BECPG_URI, "branchFromVersionLabel");
	/** Constant <code>ASSOC_AUTO_MERGE_TO</code> */
	public static final QName ASSOC_AUTO_MERGE_TO = QName.createQName(BECPG_URI, "autoMergeTo");

	/** Constant <code>PROP_AUTO_MERGE_VERSIONTYPE</code> */
	public static final QName PROP_AUTO_MERGE_VERSIONTYPE = QName.createQName(BECPG_URI, "autoMergeVersionType");
	/** Constant <code>PROP_AUTO_MERGE_COMMENTS</code> */
	public static final QName PROP_AUTO_MERGE_COMMENTS = QName.createQName(BECPG_URI, "autoMergeComments");
	/** Constant <code>PROP_AUTO_MERGE_IMPACTWUSED</code> */
	public static final QName PROP_AUTO_MERGE_IMPACTWUSED = QName.createQName(BECPG_URI, "autoMergeImpactWUsed");

	/** Constant <code>PROP_AUTO_MERGE_DATE</code> */
	public static final QName PROP_AUTO_MERGE_DATE = QName.createQName(BECPG_URI, "autoMergeDate");

	/** Constant <code>ASPECT_AUTO_MERGE_ASPECT</code> */
	public static final QName ASPECT_AUTO_MERGE_ASPECT = QName.createQName(BeCPGModel.BECPG_URI, "autoMergeAspect");

	/** Constant <code>ASPECT_LINKED_SEARCH</code> */
	public static final QName ASPECT_LINKED_SEARCH = QName.createQName(BECPG_URI, "linkedSearchAspect");
	/** Constant <code>ASSOC_LINKED_SEARCH_ASSOCIATION</code> */
	public static final QName ASSOC_LINKED_SEARCH_ASSOCIATION = QName.createQName(BECPG_URI, "linkedSearchAssociation");

	// Audit state aspect
	/** Constant <code>PROP_STATE_ACTIVITY_MODIFIED</code> */
	public static final QName PROP_STATE_ACTIVITY_MODIFIED = QName.createQName(BECPG_URI, "stateActivityModified");
	/** Constant <code>PROP_STATE_ACTIVITY_MODIFIER</code> */
	public static final QName PROP_STATE_ACTIVITY_MODIFIER = QName.createQName(BECPG_URI, "stateActivityModifier");
	/** Constant <code>PROP_STATE_ACTIVITY_PREVIOUSSTATE</code> */
	public static final QName PROP_STATE_ACTIVITY_PREVIOUSSTATE = QName.createQName(BECPG_URI, "stateActivityPreviousState");

	/** Constant <code>PROP_USER_LOCAL</code> */
	public static final QName PROP_USER_LOCALE = QName.createQName(BECPG_URI, "userLocale");
	/** Constant <code>PROP_USER_CONTENT_LOCAL</code> */
	public static final QName PROP_USER_CONTENT_LOCAL = QName.createQName(BECPG_URI, "userContentLocale");
	// code aspect
	/** Constant <code>ASPECT_ERP_CODE</code> */
	public static final QName ASPECT_ERP_CODE = QName.createQName(BECPG_URI, "erpCodeAspect");
	/** Constant <code>PROP_ERP_CODE</code> */
	public static final QName PROP_ERP_CODE = QName.createQName(BECPG_URI, "erpCode");

	// WorkflowEntity
	/** Constant <code>ASSOC_WORKFLOW_ENTITY</code> */
	public static final QName ASSOC_WORKFLOW_ENTITY = QName.createQName(BECPG_URI, "workflowEntity");

	// variant
	/** Constant <code>ASPECT_ENTITYLIST_VARIANT</code> */
	public static final QName ASPECT_ENTITYLIST_VARIANT = QName.createQName(BECPG_URI, "entityListVariantAspect");
	/** Constant <code>ASPECT_ENTITY_VARIANT</code> */
	public static final QName ASPECT_ENTITY_VARIANT = QName.createQName(BECPG_URI, "entityVariantAspect");
	/** Constant <code>ASSOC_VARIANTS</code> */
	public static final QName ASSOC_VARIANTS = QName.createQName(BECPG_URI, "variants");
	/** Constant <code>TYPE_VARIANT</code> */
	public static final QName TYPE_VARIANT = QName.createQName(BECPG_URI, "variant");
	/** Constant <code>PROP_VARIANTIDS</code> */
	public static final QName PROP_VARIANTIDS = QName.createQName(BECPG_URI, "variantIds");
	/** Constant <code>PROP_IS_DEFAULT_VARIANT</code> */
	public static final QName PROP_IS_DEFAULT_VARIANT = QName.createQName(BECPG_URI, "isDefaultVariant");
	/** Constant <code>PROP_IS_DEFAULT_VARIANT</code> */
	public static final QName PROP_VARIANT_COLUMN = QName.createQName(BECPG_URI, "variantColumn");
	
	/** Constant <code>PROP_EMAIL_TASK_RESOURCE_DISABLED</code> */
	public static final QName PROP_EMAIL_TASK_RESOURCE_DISABLED = QName.createQName(BECPG_URI, "emailTaskResourceDisabled");
	/** Constant <code>PROP_EMAIL_TASK_OBSERVER_DISABLED</code> */
	public static final QName PROP_EMAIL_TASK_OBSERVER_DISABLED = QName.createQName(BECPG_URI, "emailTaskObserverDisabled");
	/** Constant <code>PROP_EMAIL_PROJECT_NOTIFICATION_DISABLED</code> */
	public static final QName PROP_EMAIL_PROJECT_NOTIFICATION_DISABLED = QName.createQName(BECPG_URI, "emailProjectNotificationDisabled");
	/** Constant <code>PROP_EMAIL_ADMIN_NOTIFICATION_DISABLED</code> */
	public static final QName PROP_EMAIL_ADMIN_NOTIFICATION_DISABLED = QName.createQName(BECPG_URI, "emailAdminNotificationDisabled");

	//forms

	/** Constant <code>ASPECT_CUSTOM_FORM_DEFINITIONS</code> */
	public static final QName ASPECT_CUSTOM_FORM_DEFINITIONS = QName.createQName(BECPG_URI, "customFormDefinitionsAspect");

	/** Constant <code>PROP_CUSTOM_FORM_DEFINITIONS</code> */
	public static final QName PROP_CUSTOM_FORM_DEFINITIONS = QName.createQName(BECPG_URI, "customFormDefinitions");

	/** Constant <code>ASPECT_UNDELETABLE_ASPECT</code> */
	public static final QName ASPECT_UNDELETABLE_ASPECT = QName.createQName(BECPG_URI, "undeletableAspect");
	
	public static final QName ASPECT_PENDING_ENTITY_REPORT_ASPECT = QName.createQName(BECPG_URI, "pendingEntityReportAspect");
	
	
	/** Constant <code>TYPE_SAVED_SEARCH</code> */
	public static final QName TYPE_SAVED_SEARCH = QName.createQName(BECPG_URI, "savedSearch");
	/** Constant <code>PROP_SAVED_SEARCH_TYPE</code> */
	public static final QName PROP_SAVED_SEARCH_TYPE = QName.createQName(BECPG_URI, "savedSearchType");
	/** Constant <code>PROP_ING_TOX_ACUTE_ORAL</code> */
	public static final QName PROP_ING_TOX_ACUTE_ORAL = QName.createQName(BECPG_URI, "ingToxAcuteOral");
	/** Constant <code>PROP_ING_TOX_ACUTE_DERMAL</code> */
	public static final QName PROP_ING_TOX_ACUTE_DERMAL = QName.createQName(BECPG_URI, "ingToxAcuteDermal");
	/** Constant <code>PROP_ING_TOX_ACUTE_INHALATION</code> */
	public static final QName PROP_ING_TOX_ACUTE_INHALATION = QName.createQName(BECPG_URI, "ingToxAcuteInhalation");
	
	/** Constant <code>PROP_ING_TOX_AQUATIC_MFACTOR</code> */
	public static final QName PROP_ING_TOX_AQUATIC_MFACTOR = QName.createQName(BECPG_URI, "ingToxAquaticMFactor");
	/** Constant <code>PROP_ING_TOX_IS_SUPER_SENSITIZING</code> */
	public static final QName PROP_ING_TOX_IS_SUPER_SENSITIZING =  QName.createQName(BECPG_URI, "ingToxIsSuperSensitizing");
	/** Constant <code>PROP_ING_TOX_ACUTE_INHALATION_TYPE</code> */
	public static final QName PROP_ING_TOX_ACUTE_INHALATION_TYPE = QName.createQName(BECPG_URI, "ingToxAcuteInhalationType");
	/** Constant <code>ASPECT_MANUFACTURING_ASPECT</code> */
	public static final QName ASPECT_MANUFACTURING_ASPECT = QName.createQName(BECPG_URI, "manufacturingAspect");
	/** Constant <code>ASSOC_PLANTS</code> */
	public static final QName ASSOC_PLANTS = QName.createQName(BECPG_URI, "plants");
	/** Constant <code>ASSOC_SUBSIDIARY_REF</code> */
	public static final QName ASSOC_SUBSIDIARY_REF = QName.createQName(BECPG_URI, "subsidiaryRef");
	
	// document aspect
	/** Constant <code>ASPECT_DOCUMENT_ASPECT</code> */
	public static final QName ASPECT_DOCUMENT_ASPECT = QName.createQName(BECPG_URI, "documentAspect");
	/** Constant <code>PROP_DOCUMENT_STATE</code> */
	public static final QName PROP_DOCUMENT_STATE = QName.createQName(BECPG_URI, "documentState");
	/** Constant <code>PROP_DOCUMENT_IS_MANDATORY</code> */
	public static final QName PROP_DOCUMENT_IS_MANDATORY = QName.createQName(BECPG_URI, "documentIsMandatory");
	/** Constant <code>ASSOC_DOCUMENT_TYPE_REF</code> */
	public static final QName ASSOC_DOCUMENT_TYPE_REF = QName.createQName(BECPG_URI, "documentTypeRef");
	/** Constant <code>ASSOC_DOCUMENT_ENTITY_REF</code> */
	public static final QName ASSOC_DOCUMENT_ENTITY_REF = QName.createQName(BECPG_URI, "documentEntityRef");
	
	// document type
	/** Constant <code>TYPE_DOCUMENT_TYPE</code> */
	public static final QName TYPE_DOCUMENT_TYPE = QName.createQName(BECPG_URI, "documentType");
	/** Constant <code>PROP_DOCUMENT_TYPE_CATEGORY</code> */
	public static final QName PROP_DOCUMENT_TYPE_CATEGORY = QName.createQName(BECPG_URI, "docTypeCategory");
	/** Constant <code>PROP_DOCUMENT_TYPE_IS_MANDATORY</code> */
	public static final QName PROP_DOCUMENT_TYPE_IS_MANDATORY = QName.createQName(BECPG_URI, "docTypeIsMandatory");
	/** Constant <code>PROP_DOCUMENT_TYPE_EFFECTIVITY_TYPE</code> */
	public static final QName PROP_DOCUMENT_TYPE_EFFECTIVITY_TYPE = QName.createQName(BECPG_URI, "docTypeEffectivityType");
	/** Constant <code>PROP_DOCUMENT_TYPE_AUTO_EXPIRATION_DELAY</code> */
	public static final QName PROP_DOCUMENT_TYPE_AUTO_EXPIRATION_DELAY= QName.createQName(BECPG_URI, "docTypeAutoExpirationDelay");
	/** Constant <code>PROP_DOCUMENT_TYPE_FORMULA</code> */
	public static final QName PROP_DOCUMENT_TYPE_FORMULA = QName.createQName(BECPG_URI, "docTypeFormula");
	/** Constant <code>PROP_DOCUMENT_TYPE_FORMAT</code> */
	public static final QName PROP_DOCUMENT_TYPE_FORMAT = QName.createQName(BECPG_URI, "docTypeNameFormat");
	/** Constant <code>PROP_DOCUMENT_TYPE_LINKED_TYPES</code> */
	public static final QName PROP_DOCUMENT_TYPE_LINKED_TYPES = QName.createQName(BECPG_URI, "docTypeLinkedTypes");
	/** Constant <code>PROP_DOCUMENT_TYPE_LINKED_TYPES</code> */
	public static final QName PROP_DOCUMENT_TYPE_PATH = QName.createQName(BECPG_URI, "docTypeDestPath");
	/** Constant <code>ASSOC_DOCUMENT_TYPE_LINKED_CHARACTS</code> */
	public static final QName ASSOC_DOCUMENT_TYPE_LINKED_CHARACTS = QName.createQName(BECPG_URI, "docTypeLinkedCharacts");
	/** Constant <code>ASSOC_DOCUMENT_TYPE_LINKED_HIERARCHY</code> */
	public static final QName ASSOC_DOCUMENT_TYPE_LINKED_HIERARCHY = QName.createQName(BECPG_URI, "docTypeLinkedHierarchy");
	
	// cm_effectivity aspect
	/** Constant <code>ASPECT_CM_EFFECTIVITY_ASPECT</code> */
	public static final QName ASPECT_CM_EFFECTIVITY_ASPECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "effectivity");
	/** Constant <code>PROP_CM_FROM</code> */
	public static final QName PROP_CM_FROM = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "from");
	/** Constant <code>PROP_CM_TO</code> */
	public static final QName PROP_CM_TO = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "to");
	
	// AI aspect
	/** Constant <code>ASPECT_AI</code> */
	public static final QName ASPECT_AI = QName.createQName(BECPG_URI, "aiAspect");
	/** Constant <code>PROP_AI_VALIDATION_CRITERIA</code> */
	public static final QName PROP_AI_VALIDATION_CRITERIA = QName.createQName(BECPG_URI, "aiValidationCriteria");
	/** Constant <code>PROP_AI_SUGGESTED_FIELDS</code> */
	public static final QName PROP_AI_SUGGESTED_FIELDS = QName.createQName(BECPG_URI, "aiSuggestedFields");
	/** Constant <code>PROP_AI_EXTRA_PROMPT</code> */
	public static final QName PROP_AI_EXTRA_PROMPT = QName.createQName(BECPG_URI, "aiExtraPrompt");
	
	// AI validation aspect
	/** Constant <code>ASPECT_AI_VALIDATION</code> */
	public static final QName ASPECT_AI_VALIDATION = QName.createQName(BECPG_URI, "aiValidationAspect");
	/** Constant <code>PROP_AI_VALIDATION_CRITERIA</code> */
	public static final QName PROP_AI_VALIDATION_STATE = QName.createQName(BECPG_URI, "aiValidationState");
	/** Constant <code>PROP_AI_SUGGESTED_FIELDS</code> */
	public static final QName PROP_AI_VALIDATION_HINTS = QName.createQName(BECPG_URI, "aiValidationHints");
	/** Constant <code>PROP_AI_EXTRA_PROMPT</code> */
	public static final QName PROP_AI_VALIDATION_DATE = QName.createQName(BECPG_URI, "aiValidationDate");
}
