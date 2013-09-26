package fr.becpg.repo.search.impl.lucene.analysis;

import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.RepoBaseTestCase;

public class NodeRefAnalyserTest extends RepoBaseTestCase {
	
	private static Log logger = LogFactory.getLog(NodeRefAnalyserTest.class);
	
	@Resource
	protected AlfrescoRepository<ProductData> alfrescoRepository;
	
	@Test
	public void testSearchByNodeRefProperty(){
		
	
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				for(int i=0 ; i<20 ; i++){
					
					int modulo = i % 2;
					
					NodeRef hierarchyNodeRef = modulo == 0 ? HIERARCHY1_FROZEN_REF : HIERARCHY1_SEA_FOOD_REF;
					RawMaterialData rawMaterialData = new RawMaterialData();
					rawMaterialData.setName("Raw material " + i);
					rawMaterialData.setHierarchy1(hierarchyNodeRef);
					rawMaterialData.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
					
					rawMaterialData.setParentNodeRef(testFolderNodeRef);
					rawMaterialData = (RawMaterialData) alfrescoRepository.save(rawMaterialData);
				}
				
				
				
				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL));
				
				List<NodeRef> rawMaterialNodeRefs = beCPGSearchService.luceneSearch(query);
				
				for(NodeRef rawMaterialNodeRef : rawMaterialNodeRefs){
					logger.debug("RawMaterial " + getHierarchy(rawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1));
				}
				
				rawMaterialNodeRefs = beCPGSearchService.luceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_PRODUCT_HIERARCHY1, true)); 
				
				for(NodeRef rawMaterialNodeRef : rawMaterialNodeRefs){
					logger.debug("RawMaterial sorted " + getHierarchy(rawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1));
				}				
				
				query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL)) + 
						LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PRODUCT_HIERARCHY1, HIERARCHY1_SEA_FOOD_REF.toString(), LuceneHelper.Operator.AND) +
						LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PRODUCT_HIERARCHY2, HIERARCHY2_CRUSTACEAN_REF.toString(), LuceneHelper.Operator.AND);
				
				rawMaterialNodeRefs = beCPGSearchService.luceneSearch(query);
				
				logger.debug("Found " + rawMaterialNodeRefs.size() + " raw materials");	
				//assertEquals(2, rawMaterialNodeRefs.size());
				
				query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL)) + 
						LuceneHelper.getCondContainsValue(BeCPGModel.PROP_PRODUCT_HIERARCHY1, "Sea", LuceneHelper.Operator.AND) +
						LuceneHelper.getCondContainsValue(BeCPGModel.PROP_PRODUCT_HIERARCHY2, HIERARCHY2_CRUSTACEAN, LuceneHelper.Operator.AND);
				
				rawMaterialNodeRefs = beCPGSearchService.luceneSearch(query);
				
				logger.debug("Found " + rawMaterialNodeRefs.size() + " raw materials");				
				//assertEquals(2, rawMaterialNodeRefs.size());
				
				return null;
			}
		}, false, true);
		
		String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL)) + 
				LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PRODUCT_HIERARCHY1, HIERARCHY1_SEA_FOOD_REF.toString(), LuceneHelper.Operator.AND) +
				LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PRODUCT_HIERARCHY2, HIERARCHY2_CRUSTACEAN_REF.toString(), LuceneHelper.Operator.AND);		
		checkAnalyser(query, 2);
		
		query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL)) + 
				LuceneHelper.getCondContainsValue(BeCPGModel.PROP_PRODUCT_HIERARCHY1, "Sea", LuceneHelper.Operator.AND) +
				LuceneHelper.getCondContainsValue(BeCPGModel.PROP_PRODUCT_HIERARCHY2, HIERARCHY2_CRUSTACEAN, LuceneHelper.Operator.AND);
		checkAnalyser(query, 2);
	}
	
	private void checkAnalyser(final String query, final int expected){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
										
				List<NodeRef> rawMaterialNodeRefs = beCPGSearchService.luceneSearch(query);
				
				logger.debug("Found " + rawMaterialNodeRefs.size() + " raw materials");									
				//assertEquals(expected, rawMaterialNodeRefs.size());
				
				return null;
			}
		}, false, true);
	}
	
	private String getHierarchy(NodeRef nodeRef, QName hierarchyQName)	{
		
		NodeRef hierarchyNodeRef = (NodeRef)nodeService.getProperty(nodeRef, hierarchyQName);
		return HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
	}

}
