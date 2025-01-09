package fr.becpg.test.repo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public abstract class StandardProductBuilder {

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

	protected NodeRef getOrCreateIng(String ingName) {

		NodeRef ingFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath("/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Ings");

		NodeRef ret = nodeService.getChildByName(ingFolder, ContentModel.ASSOC_CONTAINS, ingName);

		if (ret == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, ingName);
			ChildAssociationRef childAssocRef = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties);
			ret = childAssocRef.getChildRef();
		}

		return ret;
	}
	

	protected NodeRef getOrCreatePhysico(String name) {
		NodeRef ingFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath("/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:PhysicoChems");

		NodeRef ret = nodeService.getChildByName(ingFolder, ContentModel.ASSOC_CONTAINS, name);

		if (ret == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
			ChildAssociationRef childAssocRef = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties);
			ret = childAssocRef.getChildRef();
		}

		return ret;
	}

	public abstract FinishedProductData createTestProduct();
}
