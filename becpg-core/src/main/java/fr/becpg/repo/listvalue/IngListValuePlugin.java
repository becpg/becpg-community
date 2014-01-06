package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;

@Service
public class IngListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_INGLIST_PARENT_LEVEL = "ingListParentLevel";

	private EntityListDAO entityListDAO;

	private AssociationService associationService;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_INGLIST_PARENT_LEVEL };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		List<ListValueEntry> result = new ArrayList<>();

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listsContainerNodeRef != null) {

			NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, BeCPGModel.TYPE_INGLIST);

			NodeRef itemId = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef((String) extras.get("itemId"));
				}
			}

			for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, BeCPGModel.TYPE_INGLIST)) {
				if (!dataListItemNodeRef.equals(itemId)) {
					NodeRef nut = associationService.getTargetAssoc(dataListItemNodeRef, BeCPGModel.ASSOC_INGLIST_ING);
					if (nut != null) {
						String name = (String) nodeService.getProperty(nut, ContentModel.PROP_NAME);
						result.add(new ListValueEntry(dataListItemNodeRef.toString(), name, BeCPGModel.TYPE_ING.getLocalName()));
					}
				}

			}

		}

		return new ListValuePage(result, pageNum, pageSize, null);

	}

}
