package fr.becpg.repo.listvalue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

public abstract class AbstractListValuePluginTest extends RepoBaseTestCase {


	protected NodeRef rawMaterial1NodeRef;
	protected NodeRef rawMaterial2NodeRef;
	protected NodeRef rawMaterial3NodeRef;
	protected NodeRef localSF1NodeRef;
	protected NodeRef localSF2NodeRef;
	protected NodeRef finishedProductNodeRef;


	@Resource
	private PermissionService permissionService;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				BeCPGTestHelper.createUsers(repoBaseTestCase);
				initProducts();

				return null;

			}
		}, false, true);
	}

	public void initProducts() {
		authenticationComponent.setSystemUserAsCurrentUser();
		rawMaterial1NodeRef = createRawMaterial(testFolderNodeRef, "RM1");
		rawMaterial2NodeRef = createRawMaterial(testFolderNodeRef, "RM2");
		rawMaterial3NodeRef = createRawMaterial(testFolderNodeRef, "RM3");

		authenticationComponent.setCurrentUser(BeCPGTestHelper.USER_ONE);
		/*-- Local semi finished product 1 --*/
		LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
		localSF1.setName("Local semi finished 1");
		localSF1.setLegalName("Legal Local semi finished 1");
		localSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF1).getNodeRef();

		permissionService.setInheritParentPermissions(localSF1NodeRef, false);
		permissionService.clearPermission(localSF1NodeRef, null);
		
		
		authenticationComponent.setCurrentUser(BeCPGTestHelper.USER_TWO);
		/*-- Local semi finished product 2 --*/
		LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
		localSF2.setName("Local semi finished 2");
		localSF2.setLegalName("Legal Local semi finished 2");
		localSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF2).getNodeRef();

		permissionService.setInheritParentPermissions(localSF2NodeRef, false);
		permissionService.clearPermission(localSF2NodeRef, null);

		authenticationComponent.setSystemUserAsCurrentUser();
		
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Produit fini 1");
		finishedProduct.setLegalName("Legal Produit fini 1");
		finishedProduct.setUnit(ProductUnit.kg);
		finishedProduct.setQty(2d);
		finishedProduct.setUnitPrice(12.4d);
		List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
		CompoListDataItem item = new CompoListDataItem(null, (CompoListDataItem) null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef);

		compoList.add(item);
		compoList.add(new CompoListDataItem(null, item, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
		compoList.add(new CompoListDataItem(null, item, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
		item = new CompoListDataItem(null, (CompoListDataItem) null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef);

		compoList.add(item);
		compoList.add(new CompoListDataItem(null, item, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
		finishedProduct.getCompoListView().setCompoList(compoList);

		finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();
	}
	
}
