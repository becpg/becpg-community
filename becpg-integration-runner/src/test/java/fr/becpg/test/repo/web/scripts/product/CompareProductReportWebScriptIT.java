/*
 *
 */
package fr.becpg.test.repo.web.scripts.product;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.repo.product.AbstractCompareProductTest;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class CompareProductReportWebScriptTest.
 *
 * @author querephi
 */
public class CompareProductReportWebScriptIT extends AbstractCompareProductTest {

	private static final Log logger = LogFactory.getLog(CompareProductReportWebScriptIT.class);
	
	@Autowired
	private EntityVersionService entityVersionService;

	@Test
	public void testCompareProducts() throws IOException {

		NodeRef fpNodeRef = inWriteTx(() -> {

			logger.debug("createRawMaterial 1");

			FinishedProductData fp1 = new FinishedProductData();
			fp1.setName("FP 1");

			// Costs
			List<CostListDataItem> costList = new ArrayList<>();
			for (NodeRef cost1 : costs) {
				CostListDataItem costListItemData1 = new CostListDataItem(null, 12.2d, "€/kg", null, cost1, false);
				costList.add(costListItemData1);
			}
			fp1.setCostList(costList);

			// create an MP for the allergens
			RawMaterialData allergenRawMaterial = new RawMaterialData();
			allergenRawMaterial.setName("MP allergen");
			NodeRef allergenRawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), allergenRawMaterial).getNodeRef();

			// Allergens
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			for (NodeRef allergen : allergens) {
				List<NodeRef> voluntarySources = new ArrayList<>();
				voluntarySources.add(allergenRawMaterialNodeRef);

				AllergenListDataItem allergenListItemData1 = new AllergenListDataItem(null, null, true, false, voluntarySources, null, allergen,
						false);
				allergenList.add(allergenListItemData1);
			}
			fp1.setAllergenList(allergenList);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));

			fp1.getCompoListView().setCompoList(compoList);

			NodeRef fpNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

			Map<QName, Serializable> aspectProperties = new HashMap<>();
			aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			nodeService.addAspect(fpNodeRef1, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

			// CheckOut/CheckIn
			NodeRef destNodeRef = nodeService.getPrimaryParent(fpNodeRef1).getParentRef();
			NodeRef workingCopyNodeRef = entityVersionService.createBranch(fpNodeRef1, destNodeRef);

			NodeRef fpv1NodeRef = entityVersionService.mergeBranch(workingCopyNodeRef, fpNodeRef1, VersionType.MAJOR, "");

			// CheckOut
			destNodeRef = nodeService.getPrimaryParent(fpv1NodeRef).getParentRef();
			workingCopyNodeRef = entityVersionService.createBranch(fpv1NodeRef, destNodeRef);

			logger.debug("update workingCopy");

			ProductData workingCopy = (ProductData) alfrescoRepository.findOne(workingCopyNodeRef);
			workingCopy.setName("FP new version");

			// Costs
			costList = new ArrayList<>();
			for (NodeRef cost2 : costs) {
				CostListDataItem costListItemData2 = new CostListDataItem(null, 12.4d, "$/kg", null, cost2, false);
				costList.add(costListItemData2);
			}
			workingCopy.setCostList(costList);

			// Allergens
			allergenList = new ArrayList<>();
			for (int j = 0; j < allergens.size(); j++) {
				List<NodeRef> allSources = new ArrayList<>();
				allSources.add(allergenRawMaterialNodeRef);
				AllergenListDataItem allergenListItemData2;

				if (j < 5) {
					allergenListItemData2 = new AllergenListDataItem(null, null, true, false, allSources, null, allergens.get(j), false);
				} else {
					allergenListItemData2 = new AllergenListDataItem(null, null, false, true, null, allSources, allergens.get(j), false);
				}

				allergenList.add(allergenListItemData2);
			}
			workingCopy.setAllergenList(allergenList);

			compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(2d).withQtyUsed(0d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial4NodeRef));
			workingCopy.getCompoListView().setCompoList(compoList);

			alfrescoRepository.save(workingCopy);

			NodeRef fpv2NodeRef = entityVersionService.mergeBranch(workingCopyNodeRef, fpNodeRef1, VersionType.MAJOR, "");
			logger.info("nodeService.getProperty(fpv2NodeRef, BeCPGModel.PROP_VERSION_LABEL)"
					+ nodeService.getProperty(fpv2NodeRef, BeCPGModel.PROP_VERSION_LABEL));
			// assertEquals("check version", "2.0",
			// nodeService.getProperty(fpv2NodeRef,
			// BeCPGModel.PROP_VERSION_LABEL));

			return fpNodeRef1;

		});

		// when the version is created, a node in version history folder is created for reports,
		// wait for this node to be created and use it for comparison
		waitForSolr();
		
		inWriteTx(() -> {
			String url = String.format("/becpg/entity/compare/%s/%s/version.pdf", fpNodeRef.toString().replace("://", "/"), "1.0");
	
			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
	
			Assert.assertNotNull(response);
			
			return response;
		});

	}
	
	

}
