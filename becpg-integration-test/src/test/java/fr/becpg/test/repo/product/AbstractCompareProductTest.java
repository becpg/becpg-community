package fr.becpg.test.repo.product;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.comparison.CompareEntityService;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareOperator;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.PLMBaseTestCase;

public abstract class AbstractCompareProductTest extends PLMBaseTestCase {
	
	private static final Log logger = LogFactory.getLog(AbstractCompareProductTest.class);

	@Resource
	protected CompareEntityService compareEntityService;

	@Resource
	protected EntityListDAO entityListDAO;

	protected NodeRef localSF1NodeRef;
	protected NodeRef rawMaterial1NodeRef;
	protected NodeRef rawMaterial2NodeRef;
	protected NodeRef localSF2NodeRef;
	protected NodeRef rawMaterial3NodeRef;
	protected NodeRef rawMaterial4NodeRef;
	protected NodeRef fp1NodeRef;
	protected NodeRef fp2NodeRef;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();

		Date startTime = new Date();
		
		initObjects();
		
		waitForSolr(startTime);
	}
	
	private void initObjects() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");
				rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");
				rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");
				rawMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3).getNodeRef();

				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");
				rawMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial4).getNodeRef();

				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");
				alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial5).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF1).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");
				localSF2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF2).getNodeRef();

				return null;

			}
		}, false, true);

	}
	
	
	
	/**
	 * Check the row is present in the comparison result.
	 */
	protected boolean checkCompareRow(List<CompareResultDataItem> compareResult, String productList, String characteristic, String property,
			String values) {

		for (CompareResultDataItem c : compareResult) {

			String tempProductList = c.getEntityList() == null ? "" : c.getEntityList().toString();
			String tempCharacteristic = c.getCharacteristic() == null ? "" : (String) nodeService.getProperty(c.getCharacteristic(),
					ContentModel.PROP_NAME);
			String tempProperty = c.getProperty() == null ? "" : c.getProperty().toString();

			if (productList.equals(tempProductList) && characteristic.equals(tempCharacteristic) && property.equals(tempProperty)
					&& c.getValues().toString().equals(values.toString())) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Check the row is present in the structural comparison result.
	 */
	protected boolean checkStructCompareRow(List<StructCompareResultDataItem> structCompareResult, String productList, int depthLevel,
			StructCompareOperator operator, String product1, String product2, String properties1, String properties2) {

		for (StructCompareResultDataItem c : structCompareResult) {

			String tempProductList = c.getEntityList() == null ? "" : c.getEntityList().toString();
			String tempProduct1 = "";
			if (c.getCharacteristic1() != null) {
				List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic1(), PLMModel.ASSOC_COMPOLIST_PRODUCT);
				NodeRef productNodeRef = ((AssociationRef) compoAssocRefs.get(0)).getTargetRef();
				tempProduct1 = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
			}

			String tempProduct2 = "";
			if (c.getCharacteristic2() != null) {
				List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(c.getCharacteristic2(), PLMModel.ASSOC_COMPOLIST_PRODUCT);
				NodeRef productNodeRef = ((AssociationRef) compoAssocRefs.get(0)).getTargetRef();
				tempProduct2 = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
			}

			if (productList.equals(tempProductList) && depthLevel == c.getDepthLevel() && operator.equals(c.getOperator())
					&& product1.equals(tempProduct1) && product2.equals(tempProduct2) && properties1.toString().equals(c.getProperties1().toString())
					&& properties2.toString().equals(c.getProperties2().toString())) {

				return true;
			}
		}

		return false;
	}
	
}
