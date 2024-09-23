package fr.becpg.repo.autocomplete.impl.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.DataListModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;

@Service
public class QNameAutoCompletePlugin implements AutoCompletePlugin {

	private static final String SOURCE_TYPE_QNAME = "qname";

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private EntityListDAO entityListDAO;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	@Autowired
	private NamespaceService namespaceService;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_QNAME };
	}

	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String attribute = (String) props.get(AutoCompleteService.PROP_ATTRIBUTE_NAME);

		if ("entityLists".equals(attribute)) {
			NodeRef entityNodeRef = new NodeRef((String) props.get(AutoCompleteService.PROP_NODEREF));
			NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
			List<NodeRef> dataLists = nodeService.getChildAssocs(listContainer).stream().map(a -> a.getChildRef()).toList();

			return new AutoCompletePage(dataLists, pageNum, pageSize, dataLists1 -> {
				List<AutoCompleteEntry> suggestions = new ArrayList<>();
				if (dataLists1 != null) {
					for (NodeRef dataList : dataLists1) {
						String qname = (String) nodeService.getProperty(dataList, DataListModel.PROP_DATALIST_ITEM_TYPE);
						String title = entityDictionaryService.getType(QName.createQName(qname, namespaceService)).getTitle(entityDictionaryService);
						suggestions.add(new AutoCompleteEntry(qname, title, "file"));
					}
				}
				return suggestions;
			});
		}

		return null;

	}

}
