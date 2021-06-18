/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>SmartContentExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SmartContentExtractor extends SimpleExtractor {

	/** {@inheritDoc} */
	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		List<NodeRef> results = paginatedSearchCache.getSearchResults(pagination.getQueryExecutionId());

		if (results == null) {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().andFTSQuery(extractFTSQuery(dataListFilter.getParentNodeRef()));

			results = queryBuilder.list();
			
			pagination.setQueryExecutionId(paginatedSearchCache.storeSearchResults(results));

		}

		return pagination.paginate(results);
	}

	private String extractFTSQuery(NodeRef parentNodeRef) {
		return (String) nodeService.getProperty(parentNodeRef, org.alfresco.model.ContentModel.PROP_DESCRIPTION);
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().startsWith(RepoConsts.SMART_CONTENT_PREFIX) && !dataListFilter.isVersionFilter();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
