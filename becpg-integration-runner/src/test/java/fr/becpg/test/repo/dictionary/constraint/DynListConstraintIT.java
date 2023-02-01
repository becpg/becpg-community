/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.dictionary.constraint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration tests for DynListConstraint class.
 * 
 * @author valentin.leblanc
 */
public class DynListConstraintIT extends PLMBaseTestCase {

	protected static final Log logger = LogFactory.getLog(DynListConstraintIT.class);
	
	private static final String AUTHORITY_GROUP = "AUTHORITY_GROUP_" + DynListConstraintIT.class.getSimpleName();

	private static final String NUT_TYPES_PATH = "cm:System/cm:Lists/bcpg:entityLists/cm:NutTypes";

	@Autowired
	private AuthorityService authorityService;
	
	@Test
	public void testListValueConstraintDeletion() {

		final NodeRef listValueNode = inWriteTx(() -> {

			NodeRef nutTypesNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), NUT_TYPES_PATH);

			NodeRef testNutTypeNodeRef = nodeService.getChildByName(nutTypesNodeRef, ContentModel.ASSOC_CONTAINS, DynListConstraintIT.class.getSimpleName());

			if (testNutTypeNodeRef != null) {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_LIST_VALUE);

				try {
					nodeService.deleteNode(testNutTypeNodeRef);
					beCPGCacheService.clearAllCaches();
				} finally {
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_LIST_VALUE);
				}
			}
			
			Map<QName, Serializable> props = new HashMap<>();
			
			props.put(BeCPGModel.PROP_LV_CODE, DynListConstraintIT.class.getSimpleName());
			props.put(BeCPGModel.PROP_LV_VALUE, DynListConstraintIT.class.getSimpleName());
			props.put(ContentModel.PROP_NAME, DynListConstraintIT.class.getSimpleName());
			props.put(BeCPGModel.PROP_IS_DELETED, false);
			
			testNutTypeNodeRef = nodeService
					.createNode(nutTypesNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_LIST_VALUE, props)
					.getChildRef();

			return testNutTypeNodeRef;

		});

		DynListConstraint dynListConstraint = findDynListContraint(PLMModel.PROP_NUTTYPE);

		final DynListConstraint finalDynListConstraint = dynListConstraint;

		inWriteTx(() -> {
			
			// value is not deleted : it is alowed
			assertTrue(finalDynListConstraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
			
			return true;
		
		});

		NodeRef newListValueNode = inWriteTx(() -> {

			nodeService.setProperty(listValueNode, BeCPGModel.PROP_IS_DELETED, true);

			return listValueNode;

		});

		inWriteTx(() -> {
			
			// value is deleted : it is not alowed
			assertFalse(finalDynListConstraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));

			// if we include deleted values : it is allowed
			assertTrue(finalDynListConstraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));

			return true;
			
		});

		inWriteTx(() -> {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_LIST_VALUE);

			try {
				nodeService.deleteNode(newListValueNode);
				beCPGCacheService.clearAllCaches();
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_LIST_VALUE);
			}
			
			return true;
			
		});

	}

	private DynListConstraint findDynListContraint(QName propQName) {
		PropertyDefinition nutTypePropDefinition = dictionaryDAO.getProperty(propQName);

		DynListConstraint dynListConstraint = null;

		for (ConstraintDefinition constraint : nutTypePropDefinition.getConstraints()) {
			if (constraint.getConstraint() instanceof DynListConstraint) {
				dynListConstraint = (DynListConstraint) constraint.getConstraint();
				break;
			}
		}
		return dynListConstraint;
	}

	@Test
	public void testListValuePermissions() {
		
		final NodeRef listValueNode = inWriteTx(() -> {
			
			BeCPGTestHelper.createUsers();
			
			if (authorityService.authorityExists(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP)) {
				authorityService.deleteAuthority(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP);
			}
			
			BeCPGTestHelper.createGroup(AUTHORITY_GROUP, BeCPGTestHelper.USER_ONE);

			NodeRef nutTypesNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), NUT_TYPES_PATH);

			NodeRef testNutTypeNodeRef = nodeService.getChildByName(nutTypesNodeRef, ContentModel.ASSOC_CONTAINS, DynListConstraintIT.class.getSimpleName());

			if (testNutTypeNodeRef != null) {
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_LIST_VALUE);

				try {
					nodeService.deleteNode(testNutTypeNodeRef);
					beCPGCacheService.clearAllCaches();
				} finally {
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_LIST_VALUE);
				}
			}
			
			Map<QName, Serializable> props = new HashMap<>();
			
			props.put(BeCPGModel.PROP_LV_CODE, DynListConstraintIT.class.getSimpleName());
			props.put(BeCPGModel.PROP_LV_VALUE, DynListConstraintIT.class.getSimpleName());
			props.put(ContentModel.PROP_NAME, DynListConstraintIT.class.getSimpleName());
			props.put(BeCPGModel.PROP_IS_DELETED, false);
			
			testNutTypeNodeRef = nodeService
					.createNode(nutTypesNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_LIST_VALUE, props)
					.getChildRef();

			return testNutTypeNodeRef;

		});
		
		DynListConstraint constraint = findDynListContraint(PLMModel.PROP_NUTTYPE);
		
		inReadTx(() -> {
			setFullyAuthenticatedUser(() -> {
				assertTrue(constraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
				assertTrue(constraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));
				return null;
			}, BeCPGTestHelper.USER_ONE);
			
			setFullyAuthenticatedUser(() -> {
				assertTrue(constraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
				assertTrue(constraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));
				return null;
			}, BeCPGTestHelper.USER_TWO);
			return null;
		});
		
		inWriteTx(() -> {
			return nodeService.createAssociation(listValueNode, authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP), SecurityModel.ASSOC_READ_GROUPS);
		});
		
		inReadTx(() -> {
			setFullyAuthenticatedUser(() -> {
				assertTrue(constraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
				assertTrue(constraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));
				return null;
			}, BeCPGTestHelper.USER_ONE);
			
			setFullyAuthenticatedUser(() -> {
				assertFalse(constraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
				assertTrue(constraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));
				return null;
			}, BeCPGTestHelper.USER_TWO);
			
			return null;
		});
		
		inWriteTx(() -> {
			nodeService.removeAssociation(listValueNode, authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP), SecurityModel.ASSOC_READ_GROUPS);
			return null;
		});
		
		inReadTx(() -> {
			setFullyAuthenticatedUser(() -> {
				assertTrue(constraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
				assertTrue(constraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));
				return null;
			}, BeCPGTestHelper.USER_ONE);
			
			setFullyAuthenticatedUser(() -> {
				assertTrue(constraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
				assertTrue(constraint.getAllowedValues(false).contains(DynListConstraintIT.class.getSimpleName()));
				return null;
			}, BeCPGTestHelper.USER_TWO);
			
			return null;
		});
			
		inWriteTx(() -> {
			nodeService.removeAssociation(listValueNode, authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP), SecurityModel.ASSOC_READ_GROUPS);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_LIST_VALUE);
			
			try {
				nodeService.deleteNode(listValueNode);
				beCPGCacheService.clearAllCaches();
				return null;
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_LIST_VALUE);
			}
		});
	}
	
	private <T> void setFullyAuthenticatedUser(Supplier<T> supplier, String username) {
		
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(username);
			supplier.get();
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}
	
}
