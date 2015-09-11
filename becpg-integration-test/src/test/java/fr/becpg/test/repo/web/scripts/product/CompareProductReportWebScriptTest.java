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

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
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
public class CompareProductReportWebScriptTest extends AbstractCompareProductTest {

	private static final Log logger = LogFactory.getLog(CompareProductReportWebScriptTest.class);

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	
	@Test
	public void testCompareProducts() throws IOException {

		NodeRef fpNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				// Costs
				List<CostListDataItem> costList = new ArrayList<>();
				for (NodeRef cost : costs) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, cost, false);
					costList.add(costListItemData);
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

					AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, null, true, false, voluntarySources, null,
							allergen, false);
					allergenList.add(allergenListItemData);
				}
				fp1.setAllergenList(allergenList);

				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				
				fp1.getCompoListView().setCompoList(compoList);

				NodeRef fpNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(fpNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

				// CheckOut/CheckIn
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(fpNodeRef);
				Map<String, Serializable> properties = new HashMap<>();
				properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				NodeRef fpv1NodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, properties);

				// CheckOut
				workingCopyNodeRef = checkOutCheckInService.checkout(fpv1NodeRef);

				logger.debug("update workingCopy");

				ProductData workingCopy = alfrescoRepository.findOne(workingCopyNodeRef);
				workingCopy.setName("FP new version");

				// Costs
				costList = new ArrayList<>();
				for (NodeRef cost : costs) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.4d, "$/kg", null, cost, false);
					costList.add(costListItemData);
				}
				workingCopy.setCostList(costList);

				// Allergens
				allergenList = new ArrayList<>();
				for (int j = 0; j < allergens.size(); j++) {
					List<NodeRef> allSources = new ArrayList<>();
					allSources.add(allergenRawMaterialNodeRef);
					AllergenListDataItem allergenListItemData;

					if (j < 5) {
						allergenListItemData = new AllergenListDataItem(null, null, true, false, allSources, null, allergens.get(j), false);
					} else {
						allergenListItemData = new AllergenListDataItem(null, null, false, true, null, allSources, allergens.get(j), false);
					}

					allergenList.add(allergenListItemData);
				}
				workingCopy.setAllergenList(allergenList);

				compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial1NodeRef));
				compoList
						.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF2NodeRef));
				compoList
						.add(new CompoListDataItem(null, compoList.get(3), 2d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList
						.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
				workingCopy.getCompoListView().setCompoList(compoList);

				alfrescoRepository.save(workingCopy);

				properties = new HashMap<>();
				properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				NodeRef fpv2NodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, properties);
				logger.info("nodeService.getProperty(fpv2NodeRef, BeCPGModel.PROP_VERSION_LABEL)"
						+ nodeService.getProperty(fpv2NodeRef, BeCPGModel.PROP_VERSION_LABEL));
				// assertEquals("check version", "2.0",
				// nodeService.getProperty(fpv2NodeRef,
				// BeCPGModel.PROP_VERSION_LABEL));

				return fpNodeRef;

			}
		}, false, true);

		String url = String.format("/becpg/entity/compare/%s/%s/version.pdf", fpNodeRef.toString().replace("://", "/"), "1.0");
	
		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");

		logger.debug("response: " + response.getContentAsString());

	}

}
