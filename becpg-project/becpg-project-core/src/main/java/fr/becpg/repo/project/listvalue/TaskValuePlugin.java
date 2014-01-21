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
package fr.becpg.repo.project.listvalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;

public class TaskValuePlugin extends EntityListValuePlugin {

	private static Log logger = LogFactory.getLog(TaskValuePlugin.class);

	private static final String SOURCE_TYPE_TASK_VALUE = "TaskValue";

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TASK_VALUE };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		logger.debug("TaskValue sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		QName type = QName.createQName(className, namespaceService);
		
		NodeRef itemId = null;
		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
		if (extras != null) {
			if (extras.get("itemId") != null) {
				itemId = new NodeRef((String) extras.get("itemId"));
			}
		}
		
		return suggestDatalistItem(entityNodeRef, itemId, type, ProjectModel.PROP_TL_TASK_NAME, query, pageNum, pageSize);				
	}
	
	private ListValuePage suggestDatalistItem(NodeRef entityNodeRef, NodeRef itemId, QName datalistType, QName propertyQName, String query, Integer pageNum, Integer pageSize) {
		String queryPath = "";

		query = prepareQuery(query);
		
		queryPath += LuceneHelper.mandatory(LuceneHelper.getCondType(datalistType));
		queryPath += LuceneHelper.getCondContainsValue(propertyQName, query, Operator.AND);
		queryPath += LuceneHelper.getCond(String.format(" +PATH:\"%s/*/*/*\"", nodeService.getPath(entityNodeRef).toPrefixString(namespaceService)), Operator.AND);
		if(itemId != null){
			queryPath += LuceneHelper.getCondEqualID(itemId, LuceneHelper.Operator.NOT);
		}
		
		logger.debug("suggestDatalistItem for query : " + queryPath);

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, LuceneHelper.getSort(propertyQName), RepoConsts.MAX_SUGGESTIONS);

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(propertyQName, nodeService));
	}
}
