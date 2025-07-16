package fr.becpg.repo.sample;

import java.util.Calendar;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>Abstract SampleProductBuilder class.</p>
 *
 * @author matthieu
 */
public abstract class SampleProductBuilder {


    @FunctionalInterface
	public interface ProductBuilder {
        void build(SampleProductBuilder builder, FinishedProductData product);
    }
	
	
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	protected NodeService nodeService;
	protected NodeRef destFolder;
	protected NamespacePrefixResolver namespacePrefixResolver;

	// Protected constructor to prevent direct instantiation
	/**
	 * <p>Constructor for SampleProductBuilder.</p>
	 *
	 * @param builder a {@link fr.becpg.repo.sample.SampleProductBuilder.Builder} object
	 */
	protected SampleProductBuilder(Builder<?> builder) {
		this.alfrescoRepository = builder.alfrescoRepository;
		this.nodeService = builder.nodeService;
		this.destFolder = builder.destFolder;
		this.namespacePrefixResolver = builder.namespacePrefixResolver;
	}

	// Static generic Builder class
	public abstract static class Builder<T extends Builder<T>> {
		private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
		private NodeService nodeService;
		private NodeRef destFolder;
		private NamespacePrefixResolver namespacePrefixResolver;

		public T withAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
			this.alfrescoRepository = alfrescoRepository;
			return self();
		}

		public T withNodeService(NodeService nodeService) {
			this.nodeService = nodeService;
			return self();
		}

		public T withDestFolder(NodeRef destFolder) {
			this.destFolder = destFolder;
			return self();
		}
		
		public T withNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
			this.namespacePrefixResolver = namespacePrefixResolver;
			return self();
		}

		protected abstract T self();

		public abstract SampleProductBuilder build();
	}



	/**
	 * <p>createTestProduct.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public abstract FinishedProductData createTestProduct();
	
	/**
	 * <p>uniqueName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	protected String uniqueName(String name) {
		return name +" - "+ Calendar.getInstance().getTimeInMillis();
	}
}
