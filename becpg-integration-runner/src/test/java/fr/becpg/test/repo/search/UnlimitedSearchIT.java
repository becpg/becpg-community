/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.search;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.WorkProviderFactory;
import fr.becpg.repo.batch.WorkProviderFactory.QueryBuilderWorkProvider;
import fr.becpg.repo.batch.WorkProviderFactory.SourceAssocsWorkProvider;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.RepoBaseTestCase;
import fr.becpg.test.utils.MemoryStats;

/**
 *
 * @author matthieu
 */
public class UnlimitedSearchIT extends RepoBaseTestCase {

	protected static final Log logger = LogFactory.getLog(UnlimitedSearchIT.class);

	@Autowired
	private DownloadStorage downloadStorage;
	
	@Autowired
	private AssociationService associationService;

	@Test
	public void testUnlimitedSearch() throws Exception {

		Date date = Calendar.getInstance().getTime();

		final int searchSize = 1015;

		//in DB test
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			for (int i = 0; i < searchSize; i++) {

				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, "UnlimitedSearchTest" + i);
				properties.put(ContentModel.PROP_TITLE, "UnlimitedSearchTest-" + date.getTime());
				nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
						BeCPGModel.TYPE_LIST_VALUE, properties);

			}
			return true;
		}, false, true);

		//in DB test
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_LIST_VALUE).inDB().ftsLanguage().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
					.andPropEquals(ContentModel.PROP_TITLE, "UnlimitedSearchTest-" + date.getTime());

			assertEquals(query.list().size(), searchSize);
			assertEquals(query.andBetween(ContentModel.PROP_MODIFIED, ISO8601DateFormat.format(date),
					ISO8601DateFormat.format(Calendar.getInstance().getTime())).list().size(), searchSize);

			return true;
		}, false, true);

		waitForSolr();
		//solr  test
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_LIST_VALUE).andPropQuery(ContentModel.PROP_NAME, "UnlimitedSearchTest").andPropEquals(ContentModel.PROP_TITLE,
					"UnlimitedSearchTest-" + date.getTime()).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);

			assertEquals(query.list().size(), searchSize);
			assertEquals(query.andBetween(ContentModel.PROP_MODIFIED, ISO8601DateFormat.format(date),
					ISO8601DateFormat.format(Calendar.getInstance().getTime())).list().size(), searchSize);

			return true;
		}, false, true);
	}
	
//	@Test
	public void createManyContents() {
		int bigIndex = 1;
		int numberPerTx = 100;
		int numberOfTx = 1000;
		for (int j = 0; j < numberOfTx; j++) {
			final int txIndex = j;
			inWriteTx(() -> {
				for (int i = 0; i < numberPerTx; i++) {
					final int index = i;
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, "test_content_" + bigIndex + "_" + txIndex + "_" + index);
					properties.put(BeCPGModel.PROP_ERP_CODE, "123456789");
					nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
							ContentModel.TYPE_CONTENT, properties);
				}
				return null;
			});
			logger.info("created " + ((j + 1) * numberPerTx) + " contents...");
		}
	}
	
//	@Test
	public void createManyAssocs() {
		Set<NodeRef> searchResults = inWriteTx(() -> {
			return findAllNodes();
		});
		
		Iterator<NodeRef> it = searchResults.iterator();
		NodeRef targetNode = it.next();
		
		inWriteTx(() -> {
			int totalCount = 0;
			int batchCount = 0;
			while (it.hasNext()) {
				nodeService.createAssociation(it.next(), targetNode, ContentModel.ASSOC_ORIGINAL);
				totalCount++;
				if (totalCount % 1000 == 0) {
					batchCount++;
					logger.info("created " + totalCount + " assocs (batch " + batchCount + ")");
				}
			}
			logger.info("Total created: " + totalCount + " assocs");
			return null;
		});
	}
	
//	@Test
	public void test_SOLR() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDBIfPossible()
					.ftsLanguage()
					.inPath("/app:company_home/cm:Junit_x0020_Tests/cm:fr_becpg_test_repo_search_UnlimitedSearchIT_createManyContents")
					.andPropEquals(ContentModel.PROP_NAME, "test_content_")
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			memStats.snapshot();
			List<NodeRef> list = query.list();
			memStats.snapshot();
			long stop = System.currentTimeMillis();
			memStats.end();
			logger.info("test_SOLR results: " + list.size() + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_SOLR " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_SOLR_no_fetch() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDBIfPossible()
					.ftsLanguage()
					.inPath("/app:company_home/cm:Junit_x0020_Tests/cm:fr_becpg_test_repo_search_UnlimitedSearchIT_createManyContents")
					.andPropEquals(ContentModel.PROP_NAME, "test_content_")
					.bulkFetchEnabled(false)
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			memStats.snapshot();
			List<NodeRef> list = query.list();
			memStats.snapshot();
			long stop = System.currentTimeMillis();
			memStats.end();
			logger.info("test_SOLR_no_fetch results: " + list.size() + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_SOLR_no_fetch " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_SOLR_pagination() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDBIfPossible()
					.ftsLanguage()
					.inPath("/app:company_home/cm:Junit_x0020_Tests/cm:fr_becpg_test_repo_search_UnlimitedSearchIT_createManyContents")
					.andPropEquals(ContentModel.PROP_NAME, "test_content_");
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_SOLR_pagination results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_SOLR_pagination " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_SOLR_pagination_no_fetch() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDBIfPossible()
					.ftsLanguage()
					.bulkFetchEnabled(false)
					.inPath("/app:company_home/cm:Junit_x0020_Tests/cm:fr_becpg_test_repo_search_UnlimitedSearchIT_createManyContents")
					.andPropEquals(ContentModel.PROP_NAME, "test_content_");
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_SOLR_pagination_no_fetch results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_SOLR_pagination_no_fetch " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_SOLR_pagination_tx() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDBIfPossible()
					.ftsLanguage()
					.inPath("/app:company_home/cm:Junit_x0020_Tests/cm:fr_becpg_test_repo_search_UnlimitedSearchIT_createManyContents")
					.andPropEquals(ContentModel.PROP_NAME, "test_content_");
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.splitTransactions(transactionService)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_SOLR_pagination_tx results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_SOLR_pagination_tx " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_SOLR_pagination_tx_no_fetch() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDBIfPossible()
					.ftsLanguage()
					.bulkFetchEnabled(false)
					.inPath("/app:company_home/cm:Junit_x0020_Tests/cm:fr_becpg_test_repo_search_UnlimitedSearchIT_createManyContents")
					.andPropEquals(ContentModel.PROP_NAME, "test_content_");
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.splitTransactions(transactionService)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_SOLR_pagination_tx_no_fetch results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_SOLR_pagination_tx_no_fetch " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_DB() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
					.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			
			memStats.snapshot();
			List<NodeRef> list = query.list();
			memStats.snapshot();
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_DB results: " + list.size() + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_DB " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_DB_with_parent() {
		inWriteTx(() -> {
			NodeRef parent = new NodeRef("workspace://SpacesStore/04c5f69c-0769-4a5a-85f6-9c0769fa5a85");
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
					.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
					.inParent(parent)
					.maxResults(1).ftsLanguage();
			
			memStats.snapshot();
			List<NodeRef> list = query.list();
			memStats.snapshot();
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_DB results: " + list.size() + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_DB " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_DB_no_fetch() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
					.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
					.bulkFetchEnabled(false)
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			
			memStats.snapshot();
			List<NodeRef> list = query.list();
			memStats.snapshot();
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_DB_no_fetch results: " + list.size() + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_DB_no_fetch " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_DB_paginated() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
					.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_DB_paginated results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_DB_paginated " + memStats.report());
			return true;
		});
	}
	
//	@Test
	public void test_DB_paginated_no_fetch() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
					.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
					.bulkFetchEnabled(false)
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_DB_paginated_no_fetch results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_DB_paginated_no_fetch " + memStats.report());
			return true;
		});
	}
	
//	@Test
	@Deprecated
	public void test_DB_paginated_tx() {
		inWriteTx(() -> {
			MemoryStats memStats = new MemoryStats();
			memStats.start();
			
			long start = System.currentTimeMillis();
			BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
					.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
					.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			
			QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
					.pushSize(1000)
					.splitTransactions(transactionService)
					.build();
			
			int allNodes = 0;
			Collection<NodeRef> nextWork = List.of();
			int page = 1;
			do {
				nextWork = workProvider.getNextWork();
				allNodes += nextWork.size();
				memStats.snapshot();
				logger.info("page " + page + ", total nodes: " + allNodes);
				page++;
			} while (!nextWork.isEmpty());
			
			long stop = System.currentTimeMillis();
			memStats.end();
			
			logger.info("test_DB_paginated_tx results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
			logger.info("test_DB_paginated_tx " + memStats.report());
			return true;
		});
	}
	
	private Set<NodeRef> findAllNodes() {
		Set<NodeRef> allNodes = new HashSet<>();
		
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(ContentModel.TYPE_CONTENT).inDB()
				.andPropEquals(BeCPGModel.PROP_ERP_CODE, "123456789")
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
		
		QueryBuilderWorkProvider workProvider = WorkProviderFactory.fromQueryBuilder(query)
				.pushSize(1000)
				.build();
		
		Collection<NodeRef> nextWork = List.of();
		int page = 1;
		do {
			nextWork = workProvider.getNextWork();
			allNodes.addAll(nextWork);
			logger.info("page " + page + ", total nodes: " + allNodes.size());
			page++;
		} while (!nextWork.isEmpty());

		return allNodes;
	}

//	@Test
	public void test_sourceAssocs() {
		NodeRef targetNodeRef = new NodeRef("workspace://SpacesStore/0fcc411f-4828-4cda-8c41-1f4828ccdad8");

		MemoryStats memStats = new MemoryStats();
		memStats.start();
		long start = System.currentTimeMillis();

		List<NodeRef> allNodes = associationService.getSourcesAssocs(targetNodeRef, ContentModel.ASSOC_ORIGINAL);
		
		long stop = System.currentTimeMillis();
		memStats.end();
		
		logger.info("test_sourceAssocs results: " + allNodes.size() + ", in " + (((stop - start) / 1000d)) + " seconds");
		logger.info("test_sourceAssocs " + memStats.report());
	}
	
//	@Test
	@Deprecated
	public void test_sourceAssocs_paginated() {
		
		NodeRef targetNodeRef = new NodeRef("workspace://SpacesStore/0fcc411f-4828-4cda-8c41-1f4828ccdad8");
		
		MemoryStats memStats = new MemoryStats();
		memStats.start();
		long start = System.currentTimeMillis();
		
		SourceAssocsWorkProvider workProvider = WorkProviderFactory.fromSourceAssocs(associationService, targetNodeRef, ContentModel.ASSOC_ORIGINAL)
				.pullSize(1000)
				.pushSize(1000)
				.build();
		
		int allNodes = 0;
		Collection<NodeRef> nextWork = List.of();
		int page = 1;
		do {
			nextWork = workProvider.getNextWork();
			allNodes += nextWork.size();
			memStats.snapshot();
			logger.info("page " + page + ", total nodes: " + allNodes);
			page++;
		} while (!nextWork.isEmpty());
		
		long stop = System.currentTimeMillis();
		memStats.end();
		
		logger.info("test_sourceAssocs_paginated results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
		logger.info("test_sourceAssocs_paginated " + memStats.report());
	}
	
//	@Test
	@Deprecated
	public void test_sourceAssocs_paginated_tx() {
		NodeRef targetNodeRef = new NodeRef("workspace://SpacesStore/0fcc411f-4828-4cda-8c41-1f4828ccdad8");
		
		MemoryStats memStats = new MemoryStats();
		memStats.start();
		long start = System.currentTimeMillis();
		
		SourceAssocsWorkProvider workProvider = WorkProviderFactory.fromSourceAssocs(associationService, targetNodeRef, ContentModel.ASSOC_ORIGINAL)
				.pullSize(1000)
				.pushSize(1000)
				.splitTransactions(transactionService)
				.build();
		
		int allNodes = 0;
		Collection<NodeRef> nextWork = List.of();
		int page = 1;
		do {
			nextWork = workProvider.getNextWork();
			allNodes += nextWork.size();
			memStats.snapshot();
			logger.info("page " + page + ", total nodes: " + allNodes);
			page++;
		} while (!nextWork.isEmpty());
		
		long stop = System.currentTimeMillis();
		memStats.end();
		
		logger.info("test_sourceAssocs_paginated_tx results: " + allNodes + ", in " + (((stop - start) / 1000d)) + " seconds");
		logger.info("test_sourceAssocs_paginated_tx " + memStats.report());
	}
	
//	@Test
	public void testCreateAssocsPaginated() {
	    NodeRef downloadNode1 = inWriteTx(() -> {
	    	return downloadStorage.createDownloadNode(false);
	    });
	    
	    Set<NodeRef> searchResults = inWriteTx(() -> {
			return findAllNodes();
		});
	    
	    Iterator<NodeRef> it = searchResults.iterator();
		for (int i = 0; i < 100; i++) {
			final int txIndex = i;
			inWriteTx(() -> {
				int j = 0;
				while (it.hasNext() && j < 1000) {
					NodeRef node = it.next();
					if (nodeService.exists(node)) {
						downloadStorage.addNodeToDownload(downloadNode1, node);
					}
					j++;
				}
				logger.info("created " + (1000 * txIndex) + " assocs");
				return downloadNode1;
			});
		}
	}

}
