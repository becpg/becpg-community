package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import fr.becpg.repo.product.data.ProductData;
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
        void build(SampleProductBuilder builder, ProductData product);
    }
	
	
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	protected NodeService nodeService;
	protected NodeRef destFolder;

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
	}

	// Static generic Builder class
	public abstract static class Builder<T extends Builder<T>> {
		private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
		private NodeService nodeService;
		private NodeRef destFolder;

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

		protected abstract T self();

		public abstract SampleProductBuilder build();
	}

	Map<Pair<QName, String>, NodeRef> characts = new HashMap<>();

	/**
	 * <p>getOrCreateCharact.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getOrCreateCharact(String name, QName type) {

		return characts.computeIfAbsent(new Pair<>(type, name), p -> {
			Map<QName, Serializable> prop = Map.of(ContentModel.PROP_NAME, name);

			return nodeService
					.createNode(destFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) prop.get(ContentModel.PROP_NAME)), type, prop)
					.getChildRef();

		});
	}



	/**
	 * <p>createTestProduct.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public abstract ProductData createTestProduct();
	
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
