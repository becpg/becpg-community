package fr.becpg.test.repo.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareOperator;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;

public class CompareDocumentServiceTest extends AbstractCompareProductTest {

	private static final Log logger = LogFactory.getLog(CompareDocumentServiceTest.class);
	
	/**
	 * Test struct comparison on Document
	 */
	@Test
	public void testStructcomparisonDocument(){	
		
		fp1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.debug("createDocument 1");
			FinishedProductData fp1 = new FinishedProductData();
			fp1.setName("FP 1");
			fp1.setUnit(ProductUnit.kg);
			fp1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();
			
			//Creation of a folder into  the FP node
			NodeRef folderNoderef = createFolderNode(fp1NodeRef, "Presentation", "some folder content");
			//Creation of files into  the folder node
			createFileNode(folderNoderef, "fx.pdf", "some file content");
			createFileNode(folderNoderef, "f1.pdf",  "some file content");
			
			NodeRef subFolderNoderef = createFolderNode(folderNoderef, "Sub-Presentation", "some folder content");
			createFileNode(subFolderNoderef, "f2.pdf", "some file content");
			createFileNode(subFolderNoderef, "f3.pdf",  "some file content");	
			
			NodeRef folder2Noderef = createFolderNode(fp1NodeRef, "WonderLand", "some folder content");
			NodeRef subFolder2Noderef = createFolderNode(folder2Noderef, "Sub-WonderLand", "some folder content");
			createFileNode(subFolder2Noderef, "f4.pdf", "some file content");
			
			NodeRef folder3Noderef = createFolderNode(fp1NodeRef, "Empty-folder", "some folder content");
			createFolderNode(folder3Noderef, "Sub-Empty-folder", "some folder content");
			
			return fp1NodeRef;
		}, false, true);
		
		fp2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.debug("createDocument 2");
			FinishedProductData fp2 = new FinishedProductData();
			fp2.setName("FP 2");
			fp2.setUnit(ProductUnit.kg);
			fp2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

			//Creation of a folder into  the FP node
			NodeRef folderNoderef =createFolderNode(fp2NodeRef, "Presentation","some folder content");
			//Creation of  files into  the folder node
			createFileNode(folderNoderef, "fx.pdf", "some file content");
			NodeRef fileNodeRef = createFileNode(folderNoderef, "f1.pdf", "some file content");
			ContentData contentData =  (ContentData )nodeService.getProperty(fileNodeRef, ContentModel.PROP_CONTENT);
			ContentData newContentData = new ContentData(contentData.getContentUrl(), contentData.getMimetype(), 75l, contentData.getEncoding());
			nodeService.setProperty(fileNodeRef,  ContentModel.PROP_CONTENT, newContentData);
			
			NodeRef subFolderNoderef = createFolderNode(fp2NodeRef, "Transport",  "some folder content");
			createFileNode(subFolderNoderef, "f3.pdf",  "some file content");
			createFileNode(fp2NodeRef, "f4.pdf",  "some file content");
			
			NodeRef folder2Noderef = createFolderNode(fp2NodeRef, "WonderLand", "some folder content");
			createFolderNode(folder2Noderef, "Sub-WonderLand", "some folder content");
			
			return  fp2NodeRef;
		}, false, true);
		
		
		List<NodeRef> productsNodeRef = new ArrayList<>();
		productsNodeRef.add(fp2NodeRef);
		
		List<CompareResultDataItem> compareResult = new ArrayList<>();
		Map<String, List<StructCompareResultDataItem>> structCompareResults = new HashMap<>();
		compareEntityService.compare(fp1NodeRef, productsNodeRef, compareResult, structCompareResults);
		
		List<StructCompareResultDataItem> structCompareResult = structCompareResults.get("FP 1 - FP 2 - Documents");
		
		
		for(StructCompareResultDataItem structCompareResultItem : structCompareResult){
			String fileName1 = "";
			String fileName2 = "";
			if(structCompareResultItem.getCharacteristic1() != null) {
				fileName1 = (String)   nodeService.getProperty(structCompareResultItem.getCharacteristic1(), ContentModel.PROP_NAME);
			}
			if(structCompareResultItem.getCharacteristic2() != null) {
				fileName2 =  (String) nodeService.getProperty(structCompareResultItem.getCharacteristic2(), ContentModel.PROP_NAME);
			}
			
			String tempfile = structCompareResultItem.getEntityList() == null ? "" : structCompareResultItem.getEntityList().toString();
			logger.debug("content : " + tempfile + ", depthLevel : "+ structCompareResultItem.getDepthLevel() +" ,  Operator : " + structCompareResultItem.getOperator() + " , file Name 1 :" + fileName1 +
					"  , file Name 2 : " + fileName2 + " ,  properties  1 : " +  structCompareResultItem.getProperties1().toString() + ",  properties 2 : " + structCompareResultItem.getProperties2().toString());
		}
	
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Added, "", "f4.pdf", "{}", "{{http://www.alfresco.org/model/content/1.0}name=f4.pdf}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
		StructCompareOperator.Modified, "", "Transport", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 2,
				StructCompareOperator.Added, "", "f3.pdf", "{}", "{{http://www.alfresco.org/model/content/1.0}name=f3.pdf}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Equal, "Brief", "Brief", "{}", "{}"));
		
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Equal, "Documents", "Documents", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Equal, "Images", "Images", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Equal, "Empty-folder", "", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 2,
				StructCompareOperator.Equal, "Sub-Empty-folder", "", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Modified, "WonderLand", "WonderLand", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 2,
				StructCompareOperator.Modified, "Sub-WonderLand", "Sub-WonderLand", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 3,
				StructCompareOperator.Removed, "f4.pdf","",  "{{http://www.alfresco.org/model/content/1.0}name=f4.pdf}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 1,
				StructCompareOperator.Modified, "Presentation", "Presentation", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 2,
				StructCompareOperator.Equal, "fx.pdf", "fx.pdf", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 2,
				StructCompareOperator.Modified, "f1.pdf", "f1.pdf", "{{http://www.alfresco.org/model/content/1.0}name=f1.pdf}", 
				"{{http://www.alfresco.org/model/content/1.0}name=f1.pdf}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 2,
				StructCompareOperator.Modified, "Sub-Presentation", "", "{}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 3,
				StructCompareOperator.Removed, "f2.pdf", "", "{{http://www.alfresco.org/model/content/1.0}name=f2.pdf}", "{}"));
		
		assertTrue(checkStructCompareRow(structCompareResult, "{http://www.alfresco.org/model/content/1.0}content", 3,
					StructCompareOperator.Removed, "f3.pdf", "", "{{http://www.alfresco.org/model/content/1.0}name=f3.pdf}", "{}"));
		
	
		
	}
	
	 /**
	  * Creates a new  folder node setting the content provided.
	  *
	  * @param  parent   the parent node reference
	  * @param  name     the name of the newly created content object
	  * @param  text     the content text to be set on the newly created node
	  * @return NodeRef  node reference to the newly created content node
	  */
	private NodeRef createFolderNode(NodeRef parent, String name, String text)
	{
			FileInfo  folderInfoTEST =  fileFolderService.create(parent, name, ContentModel.TYPE_FOLDER);
		   NodeRef folderNoderefTEST = folderInfoTEST.getNodeRef();

		   ContentWriter writer = contentService.getWriter(folderNoderefTEST, ContentModel.PROP_CONTENT, true); 
		    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN); 
		    writer.setEncoding("UTF-8"); 
		    writer.putContent(text); 
	    
		    return folderNoderefTEST;
	}
	
	/**
	  * Creates a new  file node setting the content provided.
	  *
	  * @param  parent   the parent node reference
	  * @param  name     the name of the file.
	  * @param  text     the content text to be set on the file node
	  * @return NodeRef  node reference of the file nodeRef
	  */
	private NodeRef createFileNode(NodeRef parent, String name, String text) {
		// Create a map that contain the values of  node properties.
		logger.debug("Création du fichier");
	   FileInfo  fileInfoTEST =  fileFolderService.create(parent, name, ContentModel.TYPE_CONTENT); 
	   NodeRef fileNoderefTEST = fileInfoTEST.getNodeRef();
	   ContentWriter writer = this.contentService.getWriter(fileNoderefTEST, ContentModel.PROP_CONTENT, true); 
	    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
	    writer.setEncoding("UTF-8"); 
	    writer.putContent(text); 
	    return fileNoderefTEST;
	    
		
	}    
}
