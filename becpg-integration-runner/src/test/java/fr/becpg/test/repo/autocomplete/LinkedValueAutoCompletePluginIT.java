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
import fr.becpg.repo.autocomplete.AutoCompleteService;
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
	
	/**
	 * Ticket #33174: during bulk/multiple edit the parent autocomplete is opened with itemKind=type,
	 * so no datalist path nor destination nodeRef is available. The datalist must then be resolved from
	 * the entityNodeRef plus the list name, otherwise the path stays null and the request crashes (500).
	 */
	@Test
	public void testLinkedValuePluginBulkEdit() {

		NodeRef linkedListNodeRef = inWriteTx(() -> {

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

		NodeRef productHierarchyEntityNodeRef = inReadTx(
				() -> repoService.getFolderByPath(repositoryHelper.getCompanyHome(), "/cm:System/cm:ProductHierarchy"));

		inReadTx(() -> {
			// No path, no destination, itemId is the type (not a nodeRef): only entityNodeRef + list are available
			HashMap<String, String> extras = new HashMap<>();
			extras.put(AutoCompleteService.EXTRA_PARAM_ITEMID, BeCPGModel.TYPE_LINKED_VALUE.toPrefixString(namespaceService));
			extras.put(AutoCompleteService.EXTRA_PARAM_LIST, "finishedProduct_Hierarchy");

			Map<String, Serializable> bulkEditProps = new HashMap<>();
			bulkEditProps.put(AutoCompleteService.PROP_ENTITYNODEREF, productHierarchyEntityNodeRef.toString());
			bulkEditProps.put(AutoCompleteService.EXTRA_PARAM, extras);

			AutoCompletePage autoCompletePage = linkedValueAutoCompletePlugin.suggest("allLinkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED,
					bulkEditProps);
			assertTrue(autoCompletePage.getResults().stream().anyMatch(r -> LINKED_VALUE_NAME.equals(r.getName())));

			// When nothing allows to resolve the path, the plugin must degrade gracefully (no 500)
			Map<String, Serializable> unresolvableProps = new HashMap<>();
			HashMap<String, String> unresolvableExtras = new HashMap<>();
			unresolvableExtras.put(AutoCompleteService.EXTRA_PARAM_ITEMID, BeCPGModel.TYPE_LINKED_VALUE.toPrefixString(namespaceService));
			unresolvableProps.put(AutoCompleteService.EXTRA_PARAM, unresolvableExtras);

			AutoCompletePage emptyPage = linkedValueAutoCompletePlugin.suggest("allLinkedvalue", "*", 0, RepoConsts.MAX_RESULTS_UNLIMITED,
					unresolvableProps);
			assertTrue(emptyPage.getResults().isEmpty());

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
