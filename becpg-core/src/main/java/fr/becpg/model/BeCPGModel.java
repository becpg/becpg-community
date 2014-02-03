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

	// linkedValue
	QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI, "linkedValue");

	// lkvValue
	QName PROP_LKV_VALUE = QName.createQName(BECPG_URI, "lkvValue");

	// listValue
	QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI, "listValue");

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
	@Deprecated
	QName PROP_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "legalName");

	// isDeleted aspect
	QName ASPECT_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeletedAspect");
	QName PROP_IS_DELETED = QName.createQName(BeCPGModel.BECPG_URI, "isDeleted");

}
