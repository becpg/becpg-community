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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>SmartContentExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SmartContentExtractor extends SimpleExtractor {

	private static final Pattern ftsPattern = Pattern.compile("^fts\\((.*)\\)$");

	private ExpressionService expressionService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>expressionService</code>.</p>
	 *
	 * @param expressionService a {@link fr.becpg.repo.expressions.ExpressionService} object
	 */
	public void setExpressionService(ExpressionService expressionService) {
		this.expressionService = expressionService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		List<NodeRef> results = paginatedSearchCache.getSearchResults(pagination.getQueryExecutionId());

		if (results == null) {

			String condition = (String) nodeService.getProperty(dataListFilter.getParentNodeRef(), ContentModel.PROP_DESCRIPTION);

			if (condition == null) {
				throw new IllegalArgumentException("Description in smart content should not be null");
			}

			if ((condition.startsWith("spel") || condition.startsWith("js"))) {
				if (dataListFilter.getEntityNodeRef() != null) {
					NodeRef entityNodeRef = (NodeRef) expressionService.eval(condition,
							alfrescoRepository.findOne(dataListFilter.getEntityNodeRef()));
					if ((entityNodeRef != null)) {
						dataListFilter.setEntityNodeRefs(Arrays.asList(entityNodeRef));
						dataListFilter.setGuessContainer(true);
						return super.getListNodeRef(dataListFilter, pagination);
					}
					return new ArrayList<>();
				}
			} else {

				Matcher match = ftsPattern.matcher(condition);
				if (match.matches()) {
					condition = match.group(1);
				}

				BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().andFTSQuery(condition);

				results = queryBuilder.list();

				pagination.setQueryExecutionId(paginatedSearchCache.storeSearchResults(results));
			}

		}

		return pagination.paginate(results);
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().startsWith(RepoConsts.SMART_CONTENT_PREFIX);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
