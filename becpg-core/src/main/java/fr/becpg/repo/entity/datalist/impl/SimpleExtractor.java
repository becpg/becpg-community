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
package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.DataListSortRegistry;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class SimpleExtractor extends AbstractDataListExtractor {

	protected EntityListDAO entityListDAO;

	protected AssociationService associationService;

	protected DataListSortRegistry dataListSortRegistry;

	protected EntityDictionaryService entityDictionaryService;

	private static Log logger = LogFactory.getLog(SimpleExtractor.class);

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setDataListSortRegistry(DataListSortRegistry dataListSortRegistry) {
		this.dataListSortRegistry = dataListSortRegistry;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination,
			boolean hasWriteAccess) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());

		List<NodeRef> results = getListNodeRef(dataListFilter, pagination);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		for (NodeRef nodeRef : results) {
			// Right check not necessary
			if (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				if (ret.getComputedFields() == null) {
					ret.setComputedFields(attributeExtractorService.readExtractStructure(nodeService.getType(nodeRef), metadataFields));
				}
				if (FORMAT_CSV.equals(dataListFilter.getFormat())) {
					ret.addItem(extractCSV(nodeRef, ret.getComputedFields(), props, cache));
				} else {
					ret.addItem(extractJSON(nodeRef, ret.getComputedFields(), props, cache));
				}
			}
		}

		ret.setFullListSize(pagination.getFullListSize());

		return ret;
	}

	private List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		List<NodeRef> results = new ArrayList<NodeRef>();
		
		if (dataListFilter.isAllFilter() && entityDictionaryService.isSubClass(dataListFilter.getDataType(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

			BeCPGQueryBuilder queryBuilder = dataListFilter.getSearchQuery();

			if (logger.isDebugEnabled()) {
				logger.debug("DataType to filter :" + dataListFilter.getDataType());
			}

			Collection<QName> qnames = entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			for (QName qname : qnames) {
				if (!qname.equals(dataListFilter.getDataType())) {

					if (logger.isDebugEnabled()) {
						logger.debug("Add to ignore :" + qname);
					}
					queryBuilder.excludeType(qname);

				}

			}

			int skipOffset = (pagination.getPage() - 1) * pagination.getPageSize();
			int requestTotalCountMax = skipOffset + RepoConsts.MAX_RESULTS_1000;

			PagingRequest pageRequest = new PagingRequest(skipOffset, pagination.getPageSize(), pagination.getQueryExecutionId());
			pageRequest.setRequestTotalCountMax(requestTotalCountMax);

			results = pagination.paginate(queryBuilder.childFileFolders(pageRequest));

		} else if (dataListFilter.isSimpleItem()) {
			results.add(dataListFilter.getNodeRef());
		} else {

			BeCPGQueryBuilder queryBuilder = dataListFilter.getSearchQuery();

			// Look for Version
			if (dataListFilter.isVersionFilter()) {
				NodeRef listsContainerNodeRef = entityListDAO.getListContainer(new NodeRef(dataListFilter.getFilterData()));
				if (listsContainerNodeRef != null) {

					NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataListFilter.getDataType());
					if (dataListNodeRef != null) {
						queryBuilder = dataListFilter.getSearchQuery(dataListNodeRef);
					}
				}
			}

			results = advSearchService.queryAdvSearch(dataListFilter.getDataType(), queryBuilder, dataListFilter.getCriteriaMap(),
					pagination.getMaxResults());

			if (dataListFilter.getSortId() != null) {
				DataListSortPlugin plugin = dataListSortRegistry.getPluginById(dataListFilter.getSortId());
				if (plugin != null) {
					plugin.sort(results);
				}
			}

			results = pagination.paginate(results);
		}

		return results;
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		// if (dataListFilter.getParentNodeRef() != null) {
		// return (Date)
		// nodeService.getProperty(dataListFilter.getParentNodeRef(),
		// ContentModel.PROP_MODIFIED);
		// }
		return null;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			final AttributeExtractorMode mode, Map<QName, Serializable> properties, final Map<String, Object> props,
			final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, mode,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field) {
						List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
						if (field.isDataListItems()) {
							NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
							NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
							if (listNodeRef != null) {
								List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

								for (NodeRef itemNodeRef : results) {
									addExtracted(itemNodeRef, field, cache, mode, ret);
								}
							}
						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, cache, mode, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs = null;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, cache, mode, ret);
								}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, Map<NodeRef, Map<String, Object>> cache,
							AttributeExtractorMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (AttributeExtractorMode.CSV.equals(mode)) {
									ret.add(extractCSV(itemNodeRef, field.getChildrens(), props, cache));
								} else {
									ret.add(extractJSON(itemNodeRef, field.getChildrens(), props, cache));
								}
							}
						}
					}

				});
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return false;
	}

	@Override
	public boolean hasWriteAccess() {
		return true;
	}

}
