package fr.becpg.repo.form.column.decorator;

import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>ProductColumnDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductColumnDecorator implements ColumnDecorator {
	
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;
	private EntityListDAO entityListDAO;


	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}


	/** {@inheritDoc} */
	@Override
	public boolean match(Item item) {
		return item.getId().equals(PLMModel.TYPE_PACKAGINGLIST.getLocalName()) || item.getKind().equals(PLMModel.TYPE_COMPOLIST.getLocalName());
	}

	/** {@inheritDoc} */
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
