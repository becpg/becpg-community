package fr.becpg.repo.form.column.decorator;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class ProductColumnDecorator implements ColumnDecorator {
	
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;
	private EntityListDAO entityListDAO;


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}


	@Override
	public boolean match(Item item) {
		return item.getId().equals(PLMModel.TYPE_PACKAGINGLIST.getLocalName()) || item.getKind().equals(PLMModel.TYPE_COMPOLIST.getLocalName());
	}

	@Override
	public DataGridFormFieldTitleProvider createTitleResolver(NodeRef entityNodeRef, Item item) {
		DataListFilter filter = new DataListFilter(); 
		filter.setParentNodeRef(getList(entityNodeRef, item));
		return new DynamicColumnNameResolver(filter, nodeService, dictionaryService);
	}

	private NodeRef getList(NodeRef entityNodeRef, Item item) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			System.out.println(" List :  " + QName.createQName(item.getId(), namespaceService));
			listNodeRef = entityListDAO.getList(listContainerNodeRef, QName.createQName(item.getId(), namespaceService));
		}
		return listNodeRef;

	}


}
