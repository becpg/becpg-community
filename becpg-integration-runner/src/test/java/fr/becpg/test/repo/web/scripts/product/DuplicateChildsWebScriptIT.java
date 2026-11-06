/*
 *
 */
package fr.becpg.test.repo.web.scripts.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * Tests the "copy children" action on a compoList line (#36850, #21096): the source product
 * is only read, and the destination list gets unique sorts following the source order.
 *
 * @author matthieu
 */
public class DuplicateChildsWebScriptIT extends PLMBaseTestCase {

	private static final String DUPLICATE_CHILDS_URL = "/becpg/datalists/action/duplicate/childs/node/";
	private static final String TEST_USER_PASSWORD = "PWD";
	private static final String ADMIN = "admin";

	/**
	 * Tests that a user with read-only access on the source product can copy its children,
	 * and that the source compoList is left untouched even with consecutive sorts.
	 */
	@Test
	public void testCopyChildsWithReadOnlySource() throws Exception {
		NodeRef sourceNodeRef = inWriteTx(() -> createSourceWithConsecutiveSorts());
		NodeRef destNodeRef = inWriteTx(() -> createDestination(sourceNodeRef));
		inWriteTx(() -> grantReadOnlySourceAccess(sourceNodeRef, destNodeRef));

		List<Integer> sourceSortsBefore = inReadTx(() -> getSorts(sourceNodeRef));
		Serializable sourceModifiedBefore = inReadTx(() -> nodeService.getProperty(sourceNodeRef, ContentModel.PROP_MODIFIED));

		duplicateChilds(destNodeRef, BeCPGPLMTestHelper.USER_ONE, TEST_USER_PASSWORD);

		assertEquals(sourceSortsBefore, inReadTx(() -> getSorts(sourceNodeRef)));
		assertEquals(sourceModifiedBefore, inReadTx(() -> nodeService.getProperty(sourceNodeRef, ContentModel.PROP_MODIFIED)));
		assertEquals(List.of("RM A", "RM 1", "RM 2", "RM 3", "RM B"), inReadTx(() -> getProductNames(destNodeRef)));
		assertUniqueSorts(inReadTx(() -> getSorts(destNodeRef)));
	}

	/**
	 * Tests that copying a multi-level compoList keeps the source order, gives unique sorts
	 * and links the copied children to the copied parents, never to the source lines (#21096).
	 */
	@Test
	public void testCopyMultiLevelChilds() throws Exception {
		NodeRef sourceNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()));
		NodeRef destNodeRef = inWriteTx(() -> createDestination(sourceNodeRef));

		List<Integer> sourceSortsBefore = inReadTx(() -> getSorts(sourceNodeRef));
		List<String> sourceNames = inReadTx(() -> getProductNames(sourceNodeRef));

		duplicateChilds(destNodeRef, ADMIN, null);

		List<String> expectedNames = new ArrayList<>();
		expectedNames.add("RM A");
		expectedNames.addAll(sourceNames);
		expectedNames.add("RM B");

		assertEquals(sourceSortsBefore, inReadTx(() -> getSorts(sourceNodeRef)));
		assertEquals(expectedNames, inReadTx(() -> getProductNames(destNodeRef)));
		assertUniqueSorts(inReadTx(() -> getSorts(destNodeRef)));
		assertEquals(List.of(), inReadTx(() -> findLinesOutsideTheTree(destNodeRef)));
	}

	private NodeRef createSourceWithConsecutiveSorts() {
		List<CompoListDataItem> compoList = new ArrayList<>();
		compoList.add(createCompoLine(createRawMaterial("RM 1")));
		compoList.add(createCompoLine(createRawMaterial("RM 2")));
		compoList.add(createCompoLine(createRawMaterial("RM 3")));
		SemiFinishedProductData source = SemiFinishedProductData.build().withName("SF copy childs").withCompoList(compoList);
		NodeRef sourceNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), source).getNodeRef();

		int sort = 1;
		for (CompoListDataItem line : getCompoList(sourceNodeRef)) {
			nodeService.setProperty(line.getNodeRef(), BeCPGModel.PROP_SORT, sort++);
		}
		return sourceNodeRef;
	}

	private NodeRef createDestination(NodeRef sourceNodeRef) {
		List<CompoListDataItem> compoList = new ArrayList<>();
		compoList.add(createCompoLine(createRawMaterial("RM A")));
		compoList.add(createCompoLine(sourceNodeRef));
		compoList.add(createCompoLine(createRawMaterial("RM B")));
		FinishedProductData dest = FinishedProductData.build().withName("FP copy childs").withUnit(ProductUnit.kg).withCompoList(compoList);
		return alfrescoRepository.create(getTestFolderNodeRef(), dest).getNodeRef();
	}

	private Void grantReadOnlySourceAccess(NodeRef sourceNodeRef, NodeRef destNodeRef) {
		BeCPGPLMTestHelper.createUsers();
		permissionService.setPermission(getTestFolderNodeRef(), BeCPGPLMTestHelper.USER_ONE, PermissionService.CONSUMER, true);
		permissionService.setInheritParentPermissions(sourceNodeRef, false);
		permissionService.setPermission(sourceNodeRef, BeCPGPLMTestHelper.USER_ONE, PermissionService.CONSUMER, true);
		permissionService.setInheritParentPermissions(destNodeRef, false);
		permissionService.setPermission(destNodeRef, BeCPGPLMTestHelper.USER_ONE, RepoConsts.PERMISSION_COLLABORATOR, true);
		return null;
	}

	private CompoListDataItem createCompoLine(NodeRef productNodeRef) {
		return CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(productNodeRef);
	}

	private NodeRef createRawMaterial(String name) {
		RawMaterialData rawMaterial = new RawMaterialData();
		rawMaterial.setName(name);
		return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
	}

	private void duplicateChilds(NodeRef destNodeRef, String username, String password) throws Exception {
		NodeRef lineNodeRef = inReadTx(() -> getCompoList(destNodeRef).get(1).getNodeRef());
		NodeRef listNodeRef = inReadTx(() -> nodeService.getPrimaryParent(lineNodeRef).getParentRef());

		JSONObject data = new JSONObject();
		data.put("nodeRefs", new JSONArray(List.of(lineNodeRef.toString())));

		String url = DUPLICATE_CHILDS_URL + listNodeRef.toString().replace("://", "/");
		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data.toString(), "application/json"), 200, username, password);

		JSONArray results = new JSONObject(response.getContentAsString()).getJSONArray("results");
		assertTrue(results.length() > 0);
		for (int i = 0; i < results.length(); i++) {
			assertTrue(results.getJSONObject(i).getBoolean("success"));
		}
	}

	private List<CompoListDataItem> getCompoList(NodeRef productNodeRef) {
		return ((ProductData) alfrescoRepository.findOne(productNodeRef)).getCompoList();
	}

	private List<Integer> getSorts(NodeRef productNodeRef) {
		List<Integer> sorts = new ArrayList<>();
		for (CompoListDataItem line : getCompoList(productNodeRef)) {
			sorts.add((Integer) nodeService.getProperty(line.getNodeRef(), BeCPGModel.PROP_SORT));
		}
		return sorts;
	}

	private List<String> getProductNames(NodeRef productNodeRef) {
		List<String> names = new ArrayList<>();
		for (CompoListDataItem line : getCompoList(productNodeRef)) {
			names.add((String) nodeService.getProperty(line.getProduct(), ContentModel.PROP_NAME));
		}
		return names;
	}

	private void assertUniqueSorts(List<Integer> sorts) {
		assertFalse("sorts " + sorts, sorts.contains(null));
		assertEquals("sorts " + sorts, sorts.size(), new HashSet<>(sorts).size());
	}

	/**
	 * Assertions are made outside the transaction: an assertion failing inside it is reported
	 * as "Exception in Transaction", without its message.
	 *
	 * @param productNodeRef the product whose compoList is checked
	 * @return the lines that are not compoList lines or whose parent level is not a line of the list
	 */
	private List<NodeRef> findLinesOutsideTheTree(NodeRef productNodeRef) {
		Set<NodeRef> lineNodeRefs = new HashSet<>();
		for (CompoListDataItem line : getCompoList(productNodeRef)) {
			lineNodeRefs.add(line.getNodeRef());
		}
		List<NodeRef> linesOutsideTheTree = new ArrayList<>();
		for (NodeRef lineNodeRef : lineNodeRefs) {
			NodeRef parentLevel = (NodeRef) nodeService.getProperty(lineNodeRef, BeCPGModel.PROP_PARENT_LEVEL);
			boolean isCompoLine = PLMModel.TYPE_COMPOLIST.equals(nodeService.getType(lineNodeRef));
			if (!isCompoLine || ((parentLevel != null) && !lineNodeRefs.contains(parentLevel))) {
				linesOutsideTheTree.add(lineNodeRef);
			}
		}
		return linesOutsideTheTree;
	}
}
