/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

@Service
public class IngListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_INGLIST_PARENT_LEVEL = "ingListParentLevel";

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AssociationService associationService;


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

			NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, PLMModel.TYPE_INGLIST);

			NodeRef itemId = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef((String) extras.get("itemId"));
				}
			}

			for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, PLMModel.TYPE_INGLIST)) {
				if (!dataListItemNodeRef.equals(itemId)) {
					NodeRef nut = associationService.getTargetAssoc(dataListItemNodeRef, PLMModel.ASSOC_INGLIST_ING);
					if (nut != null) {
						String name = (String) nodeService.getProperty(nut, ContentModel.PROP_NAME);
						result.add(new ListValueEntry(dataListItemNodeRef.toString(), name, PLMModel.TYPE_ING.getLocalName()));
					}
				}

			}

		}

		return new ListValuePage(result, pageNum, pageSize, null);

	}

}
