package fr.becpg.repo.batch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>WorkProviderFactory class.</p>
 *
 * @author matthieu
 * @since 25.2.0.31
 */
public class WorkProviderFactory {

	private static final int DEFAULT_PULL_SIZE = 1000;
	private static final int DEFAULT_PUSH_SIZE = 15;

	private WorkProviderFactory() {

	}

	/**
	 * <p>fromList.</p>
	 *
	 * @param items a {@link java.util.List} object
	 * @param <T> a T class
	 * @return a {@link fr.becpg.repo.batch.WorkProviderFactory.DefaultWorkProviderBuilder} object
	 */
	public static <T> DefaultWorkProviderBuilder<T> fromList(List<T> items) {
		return new DefaultWorkProviderBuilder<>(items);
	}

	/**
	 * <p>fromSourceAssocs.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 * @param targetRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param assocName a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link fr.becpg.repo.batch.WorkProviderFactory.SourceAssocsWorkProviderBuilder} object
	 */
	public static SourceAssocsWorkProviderBuilder fromSourceAssocs(AssociationService associationService, NodeRef targetRef, QName assocName) {
		return new SourceAssocsWorkProviderBuilder(associationService, targetRef, assocName);
	}

	/**
	 * <p>fromQueryBuilder.</p>
	 *
	 * @param beCPGQueryBuilder a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object
	 * @return a {@link fr.becpg.repo.batch.WorkProviderFactory.QueryBuilderWorkProviderBuilder} object
	 */
	public static QueryBuilderWorkProviderBuilder fromQueryBuilder(BeCPGQueryBuilder beCPGQueryBuilder) {
		return new QueryBuilderWorkProviderBuilder(beCPGQueryBuilder);
	}

	public static class DefaultWorkProviderBuilder<T> {

		private DefaultWorkProviderBuilder(List<T> items) {
			this.items = items;
		}

		private List<T> items;

		private int pullSize = DEFAULT_PULL_SIZE;

		private int pushSize = DEFAULT_PUSH_SIZE;

		public DefaultWorkProviderBuilder<T> pullSize(int pullSize) {
			this.pullSize = pullSize;
			return this;
		}

		public DefaultWorkProviderBuilder<T> pushSize(int pushSize) {
			this.pushSize = pushSize;
			return this;
		}

		public WorkProviderFactory.BatchWorkProvider<T> build() {
			return new WorkProviderFactory.BatchWorkProvider<>(items, pullSize, pushSize, items.size());
		}
	}

	public static class SourceAssocsWorkProviderBuilder {

		public SourceAssocsWorkProviderBuilder(AssociationService associationService, NodeRef targetRef, QName assocName) {
			this.associationService = associationService;
			this.targetRef = targetRef;
			this.assocName = assocName;
		}

		private AssociationService associationService;

		private NodeRef targetRef;

		private QName assocName;

		private int pullSize = DEFAULT_PULL_SIZE;

		private int pushSize = DEFAULT_PUSH_SIZE;

		private int maxResults = RepoConsts.MAX_RESULTS_UNLIMITED;

		private TransactionService transactionService;

		public SourceAssocsWorkProviderBuilder splitTransactions(TransactionService transactionService) {
			this.transactionService = transactionService;
			return this;
		}

		public SourceAssocsWorkProviderBuilder pullSize(int pullSize) {
			this.pullSize = pullSize;
			return this;
		}

		public SourceAssocsWorkProviderBuilder pushSize(int pushSize) {
			this.pushSize = pushSize;
			return this;
		}

		public SourceAssocsWorkProviderBuilder maxResults(int maxResults) {
			this.maxResults = maxResults;
			return this;
		}

		public SourceAssocsWorkProvider build() {
			return new SourceAssocsWorkProvider(transactionService, pullSize, pushSize, maxResults, associationService, targetRef, assocName);
		}
	}

	public static class QueryBuilderWorkProviderBuilder {

		public QueryBuilderWorkProviderBuilder(BeCPGQueryBuilder beCPGQueryBuilder) {
			this.beCPGQueryBuilder = beCPGQueryBuilder;
		}

		private BeCPGQueryBuilder beCPGQueryBuilder;

		private int pullSize = DEFAULT_PULL_SIZE;

		private int pushSize = DEFAULT_PUSH_SIZE;

		private int maxResults = RepoConsts.MAX_RESULTS_UNLIMITED;

		private TransactionService transactionService;

		public QueryBuilderWorkProviderBuilder splitTransactions(TransactionService transactionService) {
			this.transactionService = transactionService;
			return this;
		}

		public QueryBuilderWorkProviderBuilder pullSize(int pullSize) {
			this.pullSize = pullSize;
			return this;
		}

		public QueryBuilderWorkProviderBuilder pushSize(int pushSize) {
			this.pushSize = pushSize;
			return this;
		}

		public QueryBuilderWorkProviderBuilder maxResults(int maxResults) {
			this.maxResults = maxResults;
			return this;
		}
		
		public List<NodeRef> collect() {
			return build().collect();
		}

		public QueryBuilderWorkProvider build() {
			return new QueryBuilderWorkProvider(transactionService, pullSize, pushSize, beCPGQueryBuilder, maxResults);
		}
	}

	private static class BatchWorkProvider<T> implements BatchProcessWorkProvider<T> {

		protected Deque<T> pendingItems = new ArrayDeque<>();

		protected TransactionService transactionService;

		protected int pullSize = DEFAULT_PULL_SIZE;

		private int pushSize = DEFAULT_PUSH_SIZE;

		private int maxResults = RepoConsts.MAX_RESULTS_UNLIMITED;

		private int currentCount = 0;

		protected int totalEstimatedSize = 0;

		protected int pullIndex = 0;
		
		protected Integer lastPullSize = null;

		protected BatchWorkProvider(List<T> pendingItems, int pullSize, int pushSize, int maxResults) {
			super();
			this.pendingItems.addAll(pendingItems);
			this.pullSize = pullSize;
			this.pushSize = pushSize;
			this.totalEstimatedSize = pendingItems.size();
		}

		protected BatchWorkProvider(TransactionService transactionService, int pullSize, int pushSize, int maxResults) {
			super();
			this.transactionService = transactionService;
			this.pullSize = pullSize;
			this.pushSize = pushSize;
			this.maxResults = maxResults;
		}

		@Override
		public int getTotalEstimatedWorkSize() {
			return totalEstimatedSize;
		}

		@Override
		public long getTotalEstimatedWorkSizeLong() {
			return totalEstimatedSize;
		}

		public Collection<T> getNextWork() {
			Collection<T> pushedItems = new ArrayList<>();
			collectPushedItems(pushedItems);
			if (canPushMoreItems(pushedItems)) {
				if (canPullMoreItems()) {
					List<T> pulledItems = null;
					if (transactionService != null) {
						pulledItems = transactionService.getRetryingTransactionHelper().doInTransaction(this::pullItems, true, true);
					} else {
						pulledItems = pullItems();
					}
					lastPullSize = pulledItems.size();
					totalEstimatedSize += pulledItems.size();
					pendingItems.addAll(pulledItems);
				}
				collectPushedItems(pushedItems);
			}
			return pushedItems;
		}

		private boolean canPullMoreItems() {
			return lastPullSize == null || lastPullSize == pullSize;
		}
		
		public List<T> collect() {
			List<T> allItems = new ArrayList<>();
			Collection<T> nextWork = null;
			do {
				nextWork = getNextWork();
				allItems.addAll(nextWork);
			} while (!nextWork.isEmpty());
			return allItems;
		}

		private void collectPushedItems(Collection<T> pushedItems) {
			while (!pendingItems.isEmpty() && canPushMoreItems(pushedItems)) {
				pushedItems.add(pendingItems.removeFirst());
				currentCount++;
			}
		}

		private boolean canPushMoreItems(Collection<T> pushedItems) {
			return pushedItems.size() < pushSize && (maxResults == RepoConsts.MAX_RESULTS_UNLIMITED || currentCount < maxResults);
		}

		protected List<T> pullItems() {
			return List.of();
		}
	}

	public static class SourceAssocsWorkProvider extends BatchWorkProvider<NodeRef> {

		private AssociationService associationService;

		private NodeRef targetRef;

		private QName assocName;

		private SourceAssocsWorkProvider(TransactionService transactionService, int pullSize, int pushSize, int totalSize,
				AssociationService associationService, NodeRef targetRef, QName assocName) {
			super(transactionService, pullSize, pushSize, totalSize);
			this.associationService = associationService;
			this.targetRef = targetRef;
			this.assocName = assocName;
		}

		@Override
		protected List<NodeRef> pullItems() {
			List<NodeRef> sourcesAssocs = associationService.getSourcesAssocs(targetRef, assocName, false, pullSize, pullIndex);
			pullIndex += sourcesAssocs.size();
			return sourcesAssocs;
		}
	}

	public static class QueryBuilderWorkProvider extends BatchWorkProvider<NodeRef> {

		private BeCPGQueryBuilder beCPGQueryBuilder;

		private QueryBuilderWorkProvider(TransactionService transactionService, int pullSize, int pushSize, BeCPGQueryBuilder beCPGQueryBuilder,
				int maxResults) {
			super(transactionService, pullSize, pushSize, maxResults);
			this.beCPGQueryBuilder = beCPGQueryBuilder;
			this.beCPGQueryBuilder.maxResults(pullSize);
		}

		@Override
		protected List<NodeRef> pullItems() {
			beCPGQueryBuilder.page(++pullIndex);
			return beCPGQueryBuilder.list();
		}
	}

}
