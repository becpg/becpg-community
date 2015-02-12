/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.listvalue;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public abstract class AbstractListValuePluginTest extends PLMBaseTestCase {

	protected NodeRef createFinishProductNodeRef(){
		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				authenticationComponent.setSystemUserAsCurrentUser();
				
				BeCPGPLMTestHelper.createUsers();
				
				NodeRef rawMaterial1NodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "RM1");
				NodeRef rawMaterial2NodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "RM2");
				NodeRef rawMaterial3NodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "RM3");

				authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_ONE);
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				NodeRef localSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF1).getNodeRef();

				permissionService.setInheritParentPermissions(localSF1NodeRef, false);
				permissionService.clearPermission(localSF1NodeRef, null);

				authenticationComponent.setCurrentUser(BeCPGPLMTestHelper.USER_TWO);
				/*-- Local semi finished product 2 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");
				NodeRef localSF2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF2).getNodeRef();

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
				CompoListDataItem item = new CompoListDataItem(null, (CompoListDataItem) null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF1NodeRef);

				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, item, 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				item = new CompoListDataItem(null, (CompoListDataItem) null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef);

				compoList.add(item);
				compoList.add(new CompoListDataItem(null, item, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);

				return  alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}}, false, true);
	}

}
