package fr.becpg.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;

public class BeCPGTestHelper {

	private static Log logger = LogFactory.getLog(BeCPGTestHelper.class);
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";
	
	
	
	public static NodeRef createTestFolder(RepoBaseTestCase repoBaseTestCase){
		
		NodeRef folderNodeRef = repoBaseTestCase.nodeService.getChildByName(repoBaseTestCase.repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
		if(folderNodeRef != null)
		{
			repoBaseTestCase.fileFolderService.delete(folderNodeRef);    		
		}			
		return repoBaseTestCase.fileFolderService.create(repoBaseTestCase.repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
		
	}
	
	
	public static NodeRef createMultiLevelProduct(NodeRef testFolder, RepoBaseTestCase repoBaseTestCase){

		/*-- Create raw material --*/
		logger.debug("/*-- Create raw material --*/");
		RawMaterialData rawMaterial1 = new RawMaterialData();
		rawMaterial1.setName("Raw material 1");
		NodeRef rawMaterial1NodeRef = repoBaseTestCase.productDAO.create(testFolder, rawMaterial1, null);
		RawMaterialData rawMaterial2 = new RawMaterialData();
		rawMaterial2.setName("Raw material 2");
		NodeRef rawMaterial2NodeRef = repoBaseTestCase.productDAO.create(testFolder, rawMaterial2, null);
		LocalSemiFinishedProduct lSF1 = new LocalSemiFinishedProduct();
		lSF1.setName("Local semi finished 1");
		NodeRef lSF1NodeRef = repoBaseTestCase.productDAO.create(testFolder, lSF1, null);

		LocalSemiFinishedProduct lSF2 = new LocalSemiFinishedProduct();
		lSF2.setName("Local semi finished 2");
		NodeRef lSF2NodeRef = repoBaseTestCase.productDAO.create(testFolder, lSF2, null);
		
		LocalSemiFinishedProduct lSF3 = new LocalSemiFinishedProduct();
		lSF3.setName("Local semi finished 3");
		NodeRef lSF3NodeRef = repoBaseTestCase.productDAO.create(testFolder, lSF3, null);
		
		LocalSemiFinishedProduct lSF4 = new LocalSemiFinishedProduct();
		lSF4.setName("Local semi finished 4");
		NodeRef lSF4NodeRef = repoBaseTestCase.productDAO.create(testFolder, lSF4, null);

		/*-- Create finished product --*/
		logger.debug("/*-- Create finished product --*/");
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Finished Product");
		finishedProduct.setHierarchy1(repoBaseTestCase.HIERARCHY1_FROZEN_REF);
		finishedProduct.setHierarchy2(repoBaseTestCase.HIERARCHY2_PIZZA_REF);
		List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
		compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF1NodeRef));
		compoList.add(new CompoListDataItem(null, 2, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF2NodeRef));
		compoList.add(new CompoListDataItem(null, 3, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
		compoList.add(new CompoListDataItem(null, 1, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF3NodeRef));
		compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial2NodeRef));
		compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, lSF4NodeRef));
		compoList.add(new CompoListDataItem(null, 1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef));
		finishedProduct.setCompoList(compoList);
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		return repoBaseTestCase.productDAO.create(testFolder, finishedProduct, dataLists);
		
	}
	
	
}
