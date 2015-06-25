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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class ParentValuePlugin extends EntityListValuePlugin {
	
	private static Log logger = LogFactory.getLog(ParentValuePlugin.class);

	private static final String SOURCE_TYPE_PARENT_VALUE = "ParentValue";
	
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
		return new String[] { SOURCE_TYPE_PARENT_VALUE };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		logger.debug("ParentValue sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		QName type = QName.createQName(className, namespaceService);
		
		String listName = (String) props.get(ListValueService.PROP_PATH);

		String attributeName = (String) props.get(ListValueService.PROP_ATTRIBUTE_NAME);
		QName attributeQName = QName.createQName(attributeName, namespaceService);

		String queryFilter = (String) props.get(ListValueService.PROP_FILTER);
		
		
		NodeRef itemId = null;
		
		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
		if (extras != null) {
			if (extras.get("itemId") != null) {
				itemId = new NodeRef((String) extras.get("itemId"));
			}
		}
		
		NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listsContainerNodeRef != null) {
			NodeRef dataListNodeRef;
				if(listName == null){	
					dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, type);
				} else {
					dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, listName);
				}
			if(dataListNodeRef != null){
				if(dictionaryService.getProperty(attributeQName) != null){
					return suggestFromProp(dataListNodeRef, itemId, type, attributeQName, query, queryFilter, pageNum, pageSize, props);
				}
				else{
					return suggestFromAssoc(dataListNodeRef, itemId, type, attributeQName, query, queryFilter, pageNum, pageSize, props);
				}
			}
		}		
		return new ListValuePage(new ArrayList<>(), pageNum, pageSize, null); 
	}

	private ListValuePage suggestFromProp(NodeRef dataListNodeRef, NodeRef itemId, QName datalistType, QName propertyQName, String query, String queryFilter,
			Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		

		BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery().ofType(datalistType).andPropQuery(propertyQName, prepareQuery(query))
				.parent(dataListNodeRef);
		
		if(queryFilter !=null  && queryFilter.length()>0){
			String[] splitted = queryFilter.split("\\|");
			beCPGQueryBuilder.andPropEquals(QName.createQName(splitted[0], namespaceService), splitted[1]);
		}
		
		if(props.containsKey(ListValueService.PROP_PARENT)){
			String parent = (String) props.get(ListValueService.PROP_PARENT);
			if(parent!=null){
				if(!parent.isEmpty() && NodeRef.isNodeRef(parent)){
					beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parent);
				} else {
					beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL,String.valueOf(RepoConsts.DEFAULT_LEVEL));
				}
			}
		}
		
		
		if (itemId != null) {
			beCPGQueryBuilder.andNotID(itemId);
		}
		
		logger.debug("suggestDatalistItem for query : " + beCPGQueryBuilder.toString());
		List<NodeRef> ret = beCPGQueryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS).list();
		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(propertyQName, nodeService));
	}
	
	private ListValuePage suggestFromAssoc(NodeRef dataListNodeRef, NodeRef itemId, QName datalistType, QName associationQName, String query, String queryFilter,
			Integer pageNum, Integer pageSize, Map<String, Serializable> props){
		
		List<ListValueEntry> result = new ArrayList<>();				
		for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, datalistType)) {
			if (!dataListItemNodeRef.equals(itemId)) {
				NodeRef targetNode = associationService.getTargetAssoc(dataListItemNodeRef, associationQName);
				if (targetNode != null) {
					String name = (String) nodeService.getProperty(targetNode, ContentModel.PROP_NAME);
					result.add(new ListValueEntry(dataListItemNodeRef.toString(), name, datalistType.getLocalName()));
				}
			}
		}
		return new ListValuePage(result, pageNum, pageSize, null);
	}
}
