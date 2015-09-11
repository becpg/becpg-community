/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.listvalue.ListValueService;

@Service
public class EntityVersionsListValuePlugin implements ListValuePlugin {

	private static final String SOURCE_TYPE_BRANCHES = "branches";

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_BRANCHES };
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String nodeRef = (String) props.get(ListValueService.PROP_NODEREF);

		NodeRef entityNodeRef = null;
		if (nodeRef != null) {
			entityNodeRef = new NodeRef(nodeRef);
		}

		List<NodeRef> branches = entityVersionService.getAllVersionBranches(entityNodeRef);

		for (Iterator<NodeRef> iterator = branches.iterator(); iterator.hasNext();) {
			if (entityNodeRef.equals(iterator.next())) {
				iterator.remove();
			}

		}

		return new ListValuePage(branches, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME, nodeService));

	}

}
