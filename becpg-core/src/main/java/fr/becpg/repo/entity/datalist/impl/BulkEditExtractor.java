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

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>BulkEditExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BulkEditExtractor extends SimpleExtractor {

	private final String BULK_EDIT_NAME = "bulk-edit";

	private NamespaceService namespaceService;

	private final Log logger = LogFactory.getLog(BulkEditExtractor.class);

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && BULK_EDIT_NAME.equals(dataListFilter.getDataListName());
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDefaultExtractor() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		List<NodeRef> results = paginatedSearchCache.getSearchResults(pagination.getQueryExecutionId());

		if (results == null) {

			if ((dataListFilter.getEntityNodeRefs() != null) && (dataListFilter.getEntityNodeRefs().size() > 1)) {
				results = dataListFilter.getEntityNodeRefs();
			} else {

				if (logger.isDebugEnabled()) {
					logger.debug("Getting bulk-edit results for :" + dataListFilter.toString());
				}

				BeCPGQueryBuilder queryBuilder = dataListFilter.getSearchQuery();

				
				if(ContentModel.TYPE_CONTENT.equals(dataListFilter.getDataType())){
					queryBuilder.excludeSearch();
					if (!dataListFilter.getFilterId().equals(DataListFilter.NODE_PATH_FILTER)) {
						queryBuilder.excludePath(RepoConsts.ENTITIES_HISTORY_XPATH + "//*");
					}
				}
				
				//Set AND operator
				queryBuilder.andOperator();
				
				// Look for path
				if (dataListFilter.getFilterId().equals(DataListFilter.NODE_PATH_FILTER)) {
					String path = nodeService.getPath(new NodeRef(dataListFilter.getFilterData())).toPrefixString(namespaceService);
					if (logger.isDebugEnabled()) {
						logger.debug("Getting bulk-edit results  in path:" + path);
					}

					queryBuilder.inPath(path + "/");
				}

				try {
					if ((dataListFilter.getExtraParams() != null) && (dataListFilter.getCriteriaMap() != null)) {
						
						JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
						if ((jsonObject != null) && jsonObject.has("searchTerm")) {
							String searchTerm = (String) jsonObject.get("searchTerm");
							if ((searchTerm != null) && (searchTerm.length() > 0)) {
								queryBuilder.andFTSQuery(searchTerm);
							}
						}
					}
				} catch (JSONException e) {
					logger.error(e);
				}

				results = advSearchService.queryAdvSearch(dataListFilter.getDataType(), queryBuilder, dataListFilter.getCriteriaMap(),
						pagination.getMaxResults());
			}

			pagination.setQueryExecutionId(paginatedSearchCache.storeSearchResults(results));

		} else if (logger.isDebugEnabled()) {
			logger.debug("Retrieve results from queryId for pagination :" + pagination.getQueryExecutionId());
		}
		return pagination.paginate(results);

	}

}
