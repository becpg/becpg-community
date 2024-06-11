package fr.becpg.repo.form.column.decorator;

import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.form.BecpgFormService;

/**
 * <p>ProductColumnDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductColumnDecorator implements ColumnDecorator, InitializingBean {
	
	@Autowired
	private NodeService nodeService;
	@Autowired
	@Qualifier("cachedDictionaryService")
	private DictionaryService dictionaryService;
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	private BecpgFormService becpgFormService;

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		becpgFormService.registerDecorator(this);
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
			listNodeRef = entityListDAO.getList(listContainerNodeRef, QName.createQName(item.getId(), namespaceService));
		}
		return listNodeRef;

	}


}
