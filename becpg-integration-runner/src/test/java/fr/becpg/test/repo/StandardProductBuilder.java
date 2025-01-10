package fr.becpg.test.repo;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;

public abstract class StandardProductBuilder {


    @FunctionalInterface
	public interface ProductBuilder {
        void build(StandardProductBuilder builder, FinishedProductData product);
    }
	
	
	protected AlfrescoRepository<ProductData> alfrescoRepository;
	protected NodeService nodeService;
	protected NodeRef destFolder;

	// Protected constructor to prevent direct instantiation
	protected StandardProductBuilder(Builder<?> builder) {
		this.alfrescoRepository = builder.alfrescoRepository;
		this.nodeService = builder.nodeService;
		this.destFolder = builder.destFolder;
	}

	// Static generic Builder class
	public abstract static class Builder<T extends Builder<T>> {
		private AlfrescoRepository<ProductData> alfrescoRepository;
		private NodeService nodeService;
		private NodeRef destFolder;

		public T withAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
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

		protected abstract T self();

		public abstract StandardProductBuilder build();
	}



	public abstract FinishedProductData createTestProduct();
}
