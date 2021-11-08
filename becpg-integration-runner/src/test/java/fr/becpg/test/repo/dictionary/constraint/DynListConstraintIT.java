/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.dictionary.constraint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration tests for DynListConstraint class.
 * 
 * @author valentin.leblanc
 */
public class DynListConstraintIT extends PLMBaseTestCase {

	protected static final Log logger = LogFactory.getLog(DynListConstraintIT.class);

	private static final String NUT_TYPES_PATH = "cm:System/cm:Lists/bcpg:entityLists/cm:NutTypes";

	@Test
	public void testListValueConstraintDeletion() {

		final NodeRef listValueNode = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef nutTypesNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), NUT_TYPES_PATH);

			NodeRef testNutTypeNodeRef = nodeService.getChildByName(nutTypesNodeRef, ContentModel.ASSOC_CONTAINS, DynListConstraintIT.class.getSimpleName());

			Map<QName, Serializable> props = new HashMap<>();

			props.put(BeCPGModel.PROP_LV_CODE, DynListConstraintIT.class.getSimpleName());
			props.put(BeCPGModel.PROP_LV_VALUE, DynListConstraintIT.class.getSimpleName());
			props.put(ContentModel.PROP_NAME, DynListConstraintIT.class.getSimpleName());
			props.put(BeCPGModel.PROP_IS_DELETED, false);

			if (testNutTypeNodeRef != null) {
				nodeService.setProperties(testNutTypeNodeRef, props);
			} else {
				testNutTypeNodeRef = nodeService
						.createNode(nutTypesNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_LIST_VALUE, props)
						.getChildRef();
			}

			return testNutTypeNodeRef;

		}, false, true);

		PropertyDefinition nutTypePropDefinition = dictionaryDAO.getProperty(PLMModel.PROP_NUTTYPE);

		DynListConstraint dynListConstraint = null;

		for (ConstraintDefinition constraint : nutTypePropDefinition.getConstraints()) {
			if (constraint.getConstraint() instanceof DynListConstraint) {
				dynListConstraint = (DynListConstraint) constraint.getConstraint();
				break;
			}
		}

		final DynListConstraint finalDynListConstraint = dynListConstraint;

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			// value is not deleted : it is alowed
			assertTrue(finalDynListConstraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));
			
			return true;
		
		}, true, true);

		NodeRef newListValueNode = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			nodeService.setProperty(listValueNode, BeCPGModel.PROP_IS_DELETED, true);

			return listValueNode;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			// value is deleted : it is not alowed
			assertFalse(finalDynListConstraint.getAllowedValues().contains(DynListConstraintIT.class.getSimpleName()));

			// if we include deleted values : it is allowed
			assertTrue(finalDynListConstraint.getAllowedValues(true).contains(DynListConstraintIT.class.getSimpleName()));

			return true;
			
		}, true, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_LIST_VALUE);

			try {
				nodeService.deleteNode(newListValueNode);
				beCPGCacheService.clearAllCaches();
			} finally {
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_LIST_VALUE);
			}
			
			return true;
			
		}, false, true);

	}

}
