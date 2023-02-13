/*
 *
 */
package fr.becpg.test.repo.autocomplete;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.impl.plugins.LinkedValueAutoCompletePlugin;
import fr.becpg.test.BeCPGTestHelper;

public class LinkedValueAutoCompletePluginIT extends AbstractAutoCompletePluginTest {

	private static final String AUTHORITY_GROUP = "AUTHORITY_GROUP_" + LinkedValueAutoCompletePluginIT.class.getSimpleName();

	private static final String LINKED_VALUE_NAME = LinkedValueAutoCompletePluginIT.class.getSimpleName();
	
	private static final String PRODUCT_HIERARCHY_PATH = "/cm:System/cm:ProductHierarchy/bcpg:entityLists/cm:finishedProduct_Hierarchy";

	@Autowired
	private LinkedValueAutoCompletePlugin linkedValueAutoCompletePlugin;

	@Test
	public void testLinkedValuePlugin() {

		NodeRef linkedListNodeRef = inWriteTx(() -> {

			BeCPGTestHelper.createUsers();
			
			if (authorityService.authorityExists(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP)) {
				authorityService.deleteAuthority(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP);
			}
			
			BeCPGTestHelper.createGroup(AUTHORITY_GROUP, BeCPGTestHelper.USER_ONE);
			
			NodeRef productHierarchyNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), PRODUCT_HIERARCHY_PATH);

			NodeRef linkedList = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, LINKED_VALUE_NAME);

			if (linkedList != null) {
				nodeService.deleteNode(linkedList);
				beCPGCacheService.clearAllCaches();
			}
			
			Map<QName, Serializable> props = new HashMap<>();
			
			props.put(BeCPGModel.PROP_DEPTH_LEVEL, 1);
			props.put(BeCPGModel.PROP_LKV_VALUE, LINKED_VALUE_NAME);
			props.put(ContentModel.PROP_NAME, LINKED_VALUE_NAME);
			
			return nodeService.createNode(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
					BeCPGModel.TYPE_LINKED_VALUE, props).getChildRef();

		});
		
		waitForSolr();
		
		Map<String, Serializable> props = new HashMap<>();
		props.put("path", "System/ProductHierarchy/bcpg:entityLists/finishedProduct_Hierarchy");
		
		inReadTx(() -> {
			setFullyAuthenticatedUser(() -> {
				AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("linkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, props);
				assertTrue(autoCompletePage.getResults().stream().anyMatch(r -> LINKED_VALUE_NAME.equals(r.getName())));
				return null;
			}, BeCPGTestHelper.USER_ONE);
			
			setFullyAuthenticatedUser(() -> {
				AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("linkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, props);
				assertTrue(autoCompletePage.getResults().stream().anyMatch(r -> LINKED_VALUE_NAME.equals(r.getName())));
				return null;
			}, BeCPGTestHelper.USER_TWO);
			
			return null;
		});
		

		inWriteTx(() -> {
			return nodeService.createAssociation(linkedListNodeRef, authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP), SecurityModel.ASSOC_READ_GROUPS);
		});
		
		inReadTx(() -> {
			setFullyAuthenticatedUser(() -> {
				AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("linkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, props);
				assertTrue(autoCompletePage.getResults().stream().anyMatch(r -> LINKED_VALUE_NAME.equals(r.getName())));
				return null;
			}, BeCPGTestHelper.USER_ONE);
			
			setFullyAuthenticatedUser(() -> {
				AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("linkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, props);
				assertTrue(autoCompletePage.getResults().stream().allMatch(r -> !LINKED_VALUE_NAME.equals(r.getName())));
				return null;
			}, BeCPGTestHelper.USER_TWO);
			
			return null;
		});
		
		inWriteTx(() -> {
			nodeService.removeAssociation(linkedListNodeRef, authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + AUTHORITY_GROUP), SecurityModel.ASSOC_READ_GROUPS);
			return null;
		});
		
		inReadTx(() -> {
			setFullyAuthenticatedUser(() -> {
				AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("linkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, props);
				assertTrue(autoCompletePage.getResults().stream().anyMatch(r -> LINKED_VALUE_NAME.equals(r.getName())));
				return null;
			}, BeCPGTestHelper.USER_ONE);
			
			setFullyAuthenticatedUser(() -> {
				AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("linkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED, props);
				assertTrue(autoCompletePage.getResults().stream().anyMatch(r -> LINKED_VALUE_NAME.equals(r.getName())));
				return null;
			}, BeCPGTestHelper.USER_TWO);
			
			return null;
		});
		
		inWriteTx(() -> {
			nodeService.deleteNode(linkedListNodeRef);
			return null;
		});
		
	}
	
	private <T> T setFullyAuthenticatedUser(Supplier<T> supplier, String username) {
		
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(username);
			return supplier.get();
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}
}
