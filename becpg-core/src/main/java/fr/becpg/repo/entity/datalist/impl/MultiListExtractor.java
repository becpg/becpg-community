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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.datalist.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * 
 * @author matthieu
 * 
 */
public class MultiListExtractor extends SimpleExtractor {

	private static final Log logger = LogFactory.getLog(MultiListExtractor.class);

	private static final String MULTI_LIST_EXTRACTOR_PREFIX = "multiList_";

	private NamespaceService namespaceService;

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {
		NodeRef entityNodeRef = dataListFilter.getEntityNodeRef();

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

		NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);
		if (listContainer != null) {
			String[] listNames = dataListFilter.getDataListName().substring(MULTI_LIST_EXTRACTOR_PREFIX.length()).split("_");
			for (String listName : listNames) {
				NodeRef listNodeRef = entityListDAO.getList(listContainer, listName);
				if (listNodeRef != null) {
					QName typeQname = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
							namespaceService);

					queryBuilder.inType(typeQname);
					queryBuilder.inParent(listNodeRef);

				} else {
					logger.warn("No list found of name: " + listName);
				}
			}

		}

		queryBuilder.maxResults(pagination.getMaxResults());

		queryBuilder.addSort(dataListFilter.getSortMap());
		List<NodeRef> results = queryBuilder.list();

		if (dataListFilter.getSortId() != null) {
			DataListSortPlugin plugin = dataListSortRegistry.getPluginById(dataListFilter.getSortId());
			if (plugin != null) {
				plugin.sort(results);
			}
		}

		return pagination.paginate(results);

	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && dataListFilter.getDataListName() != null
				&& dataListFilter.getDataListName().startsWith(MULTI_LIST_EXTRACTOR_PREFIX);
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
