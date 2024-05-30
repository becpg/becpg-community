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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.DataListItemExtractor;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.DataListSortRegistry;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * <p>SimpleExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SimpleExtractor extends AbstractDataListExtractor {

	protected EntityListDAO entityListDAO;

	protected AssociationService associationService;

	protected DataListSortRegistry dataListSortRegistry;

	protected PaginatedSearchCache paginatedSearchCache;
	
	private static final Map<QName, DataListItemExtractor> dataListItemExtractors = new HashMap<>();

	private static final Log logger = LogFactory.getLog(SimpleExtractor.class);

	public static void registerDataListItemExtractor(QName key, DataListItemExtractor dataListItemExtractor) {
		dataListItemExtractors.put(key, dataListItemExtractor);
	}
	
	/**
	 * <p>Setter for the field <code>paginatedSearchCache</code>.</p>
	 *
	 * @param paginatedSearchCache a {@link fr.becpg.repo.search.PaginatedSearchCache} object.
	 */
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>dataListSortRegistry</code>.</p>
	 *
	 * @param dataListSortRegistry a {@link fr.becpg.repo.entity.datalist.DataListSortRegistry} object.
	 */
	public void setDataListSortRegistry(DataListSortRegistry dataListSortRegistry) {
		this.dataListSortRegistry = dataListSortRegistry;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		List<NodeRef> results = getListNodeRef(dataListFilter, dataListFilter.getPagination());

		Map<String, Object> props = new HashMap<>();
		props.put(PROP_ACCESSRIGHT, dataListFilter.hasWriteAccess());

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		for (NodeRef nodeRef : results) {
			// Right check not necessary
			if (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				if (ret.getComputedFields() == null) {
					ret.setComputedFields(attributeExtractorService.readExtractStructure(nodeService.getType(nodeRef), metadataFields));
				}
				if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
					ret.addItem(extractExport(
							RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat()) ? FormatMode.XLSX : FormatMode.CSV,
							nodeRef, ret.getComputedFields(), props, cache));
				} else {
					ret.addItem(extractJSON(nodeRef, ret.getComputedFields(), props, cache));
				}
			}
		}

		ret.setFullListSize(dataListFilter.getPagination().getFullListSize());

		return ret;
	}

	/**
	 * <p>getListNodeRef.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @param pagination a {@link fr.becpg.repo.entity.datalist.data.DataListPagination} object.
	 * @return a {@link java.util.List} object.
	 */
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		List<NodeRef> results = paginatedSearchCache.getSearchResults(pagination.getQueryExecutionId());

		if (results == null) {

			results = new LinkedList<>();

			if (dataListFilter.isGuessContainer() && (dataListFilter.getEntityNodeRef() != null)) {
				NodeRef listsContainerNodeRef = entityListDAO.getListContainer(dataListFilter.getEntityNodeRef());
				if (listsContainerNodeRef != null) {
					NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataListFilter.getDataType());
					if (dataListNodeRef != null) {
						dataListFilter.setParentNodeRef(dataListNodeRef);
					}
				}

				if (dataListFilter.getParentNodeRef() == null) {
					if (logger.isDebugEnabled()) {
						logger.debug("No container found return empty results");
					}
					return results;
				}

			}

			if (dataListFilter.isAllFilter() && entityDictionaryService.isSubClass(dataListFilter.getDataType(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

				if (logger.isDebugEnabled()) {
					logger.debug("DataType to filter :" + dataListFilter.getDataType());
				}

				results = entityListDAO.getListItems(dataListFilter.getParentNodeRef(), dataListFilter.getDataType(), dataListFilter.getSortMap());
				pagination.setQueryExecutionId(paginatedSearchCache.storeSearchResults(results));

			} else if (dataListFilter.isSimpleItem()) {
				results.add(dataListFilter.getNodeRef());
			} else {

				BeCPGQueryBuilder queryBuilder = dataListFilter.getSearchQuery();

				// Look for Version
				if (dataListFilter.isVersionFilter()) {
					NodeRef listsContainerNodeRef = entityListDAO.getListContainer(new NodeRef(dataListFilter.getFilterData()));
					if (listsContainerNodeRef != null) {

						NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataListFilter.getDataListName());
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
						plugin.sort(results, dataListFilter.getSortMap());
					}
				}

				pagination.setQueryExecutionId(paginatedSearchCache.storeSearchResults(results));

			}
		}

		return pagination.paginate(results);
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		// if (dataListFilter.getParentNodeRef() != null) {
		// return (Date)
		// nodeService.getProperty(dataListFilter.getParentNodeRef(),
		// ContentModel.PROP_MODIFIED);
		// }
		return null;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			 FormatMode mode, Map<QName, Serializable> properties, final Map<String, Object> props,
			final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, mode,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field,FormatMode mode) {
						List<Map<String, Object>> ret = new ArrayList<>();
						if (field.isDataListItems()) {
							
							if (dataListItemExtractors.get(field.getFieldQname()) != null) {
								List<NodeRef> results = dataListItemExtractors.get(field.getFieldQname()).extractItems(nodeRef);
								for (NodeRef itemNodeRef : results) {
									addExtracted(itemNodeRef, field, mode, ret);
								}
							} else {
								NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
								NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
								if (listNodeRef != null) {
									List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());
									
									for (NodeRef itemNodeRef : results) {
										addExtracted(itemNodeRef, field, mode, ret);
									}
								}
							}
							
						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, mode, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, mode, ret);
								}

							}else if(field.getFieldDef() instanceof PropertyDefinition 
									&& DataTypeDefinition.NODE_REF.equals(((PropertyDefinition)field.getFieldDef()).getDataType().getName())  ) {

									Object value = properties.get(field.getFieldDef().getName());
									if(value!=null) {
										if (!((PropertyDefinition) field.getFieldDef()).isMultiValued()) {
											
											addExtracted((NodeRef) value, field, mode, ret);
										} else {
											@SuppressWarnings("unchecked")
											List<NodeRef> values = (List<NodeRef>) value;
											for (NodeRef tempValue : values) {
												addExtracted(tempValue, field, mode, ret);
											}
	
										}
									}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, 
							FormatMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
									ret.add(extractExport(mode, itemNodeRef, field.getChildrens(), props, cache));
								} else {
									ret.add(extractJSON(itemNodeRef, field.getChildrens(), props, cache));
								}
							}
						}
					}

				});
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return true;
	}

}
