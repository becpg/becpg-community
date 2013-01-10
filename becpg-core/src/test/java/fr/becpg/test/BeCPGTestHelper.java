package fr.becpg.test;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;

public class BeCPGTestHelper {

	private static Log logger = LogFactory.getLog(BeCPGTestHelper.class);
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";
	
	public static String PRODUCT_NAME = "Finished Product";
	
	public static NodeRef createTestFolder(RepoBaseTestCase repoBaseTestCase){
		
		NodeRef folderNodeRef = repoBaseTestCase.nodeService.getChildByName(repoBaseTestCase.repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
		if(folderNodeRef != null)
		{
		
			repoBaseTestCase.fileFolderService.delete(folderNodeRef);    		
		}			
		return  repoBaseTestCase.fileFolderService.create(repoBaseTestCase.repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

	}
	
	
	public static NodeRef createMultiLevelProduct(NodeRef testFolder, RepoBaseTestCase repoBaseTestCase){

		/*-- Create raw material --*/		
		logger.debug("/*-- Create raw material --*/");
		RawMaterialData rawMaterial1 = new RawMaterialData();
		rawMaterial1.setName("Raw material 1");
		NodeRef rawMaterial1NodeRef = repoBaseTestCase.alfrescoRepository.create(testFolder, rawMaterial1).getNodeRef();
		RawMaterialData rawMaterial2 = new RawMaterialData();
		rawMaterial2.setName("Raw material 2");
		NodeRef rawMaterial2NodeRef = repoBaseTestCase.alfrescoRepository.create(testFolder, rawMaterial2).getNodeRef();
		LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
		lSF1.setName("Local semi finished 1");
		NodeRef lSF1NodeRef = repoBaseTestCase.alfrescoRepository.create(testFolder, lSF1).getNodeRef();

		LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
		lSF2.setName("Local semi finished 2");
		NodeRef lSF2NodeRef = repoBaseTestCase.alfrescoRepository.create(testFolder, lSF2).getNodeRef();
		
		LocalSemiFinishedProductData lSF3 = new LocalSemiFinishedProductData();
		lSF3.setName("Local semi finished 3");
		NodeRef lSF3NodeRef = repoBaseTestCase.alfrescoRepository.create(testFolder, lSF3).getNodeRef();
		
		LocalSemiFinishedProductData lSF4 = new LocalSemiFinishedProductData();
		lSF4.setName("Local semi finished 4");
		NodeRef lSF4NodeRef = repoBaseTestCase.alfrescoRepository.create(testFolder, lSF4).getNodeRef();

		/*-- Create finished product --*/
		logger.debug("/*-- Create finished product --*/");
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Finished Product");
		finishedProduct.setHierarchy1(repoBaseTestCase.HIERARCHY1_FROZEN_REF);
		finishedProduct.setHierarchy2(repoBaseTestCase.HIERARCHY2_PIZZA_REF);
		List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
		CompoListDataItem parent1 = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF1NodeRef);
		CompoListDataItem child1 =new CompoListDataItem(null,parent1, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF2NodeRef);
		CompoListDataItem child12 =new CompoListDataItem(null,child1, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef);
		CompoListDataItem parent2 =new CompoListDataItem(null,(CompoListDataItem) null, 1d, 4d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, lSF3NodeRef);
		CompoListDataItem child2 =new CompoListDataItem(null, parent2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial2NodeRef);
		CompoListDataItem child21 =new CompoListDataItem(null, parent2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, lSF4NodeRef);
		CompoListDataItem parent3 = new CompoListDataItem(null, (CompoListDataItem)null, 3d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Omit, rawMaterial1NodeRef);
		
		compoList.add(parent1);
		compoList.add(child1);
		compoList.add(child12);
		compoList.add(parent2);
		compoList.add(child2);
		compoList.add(child21);
		compoList.add(parent3);
		
		
		
		finishedProduct.getCompoListView().setCompoList(compoList);
		return repoBaseTestCase.alfrescoRepository.create(testFolder, finishedProduct).getNodeRef();
		
	}
	
	
}
