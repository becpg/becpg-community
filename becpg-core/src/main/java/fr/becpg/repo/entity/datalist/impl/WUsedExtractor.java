/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.WUsedFilter;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.NestedAdvSearchPlugin;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter.AssociationCriteriaFilterMode;
import java.util.ArrayList;

/**
 * <p>
 * WUsedExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class WUsedExtractor extends MultiLevelExtractor {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(WUsedExtractor.class);

	private WUsedListService wUsedListService;

	private NamespaceService namespaceService;

	private NestedAdvSearchPlugin nestedAdvSearchPlugin;

	/**
	 * <p>
	 * Setter for the field <code>wUsedListService</code>.
	 * </p>
	 *
	 * @param wUsedListService
	 *            a {@link fr.becpg.repo.entity.datalist.WUsedListService}
	 *            object.
	 */
	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	/**
	 * <p>
	 * Setter for the field <code>namespaceService</code>.
	 * </p>
	 *
	 * @param namespaceService
	 *            a {@link org.alfresco.service.namespace.NamespaceService}
	 *            object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>
	 * Setter for the field <code>nestedAdvSearchPlugin</code>.
	 * </p>
	 *
	 * @param nestedAdvSearchPlugin
	 *            a {@link fr.becpg.repo.search.impl.NestedAdvSearchPlugin}
	 *            object.
	 */
	public void setNestedAdvSearchPlugin(NestedAdvSearchPlugin nestedAdvSearchPlugin) {
		this.nestedAdvSearchPlugin = nestedAdvSearchPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		QName associationName = null;

		if ((dataListFilter.getDataListName() != null) && (dataListFilter.getDataListName().indexOf(RepoConsts.WUSED_SEPARATOR) >= 0)) {
			associationName = QName.createQName(dataListFilter.getDataListName().split(RepoConsts.WUSED_SEPARATOR)[1].replace("_", ":"),
					namespaceService);
		} else {
			associationName = entityDictionaryService.getDefaultPivotAssoc(dataListFilter.getDataType());
		}

		if (associationName == null) {
			logger.warn("No wUsed association name found for :" + dataListFilter.getDataType());
			return ret;
		}

		Map<String, Object> props = new HashMap<>();
		String assocName = associationName.toPrefixString(namespaceService);

		// Rights are being checked latter
		dataListFilter.setHasWriteAccess(true);
		props.put(PROP_ACCESSRIGHT, true);
		props.put(PROP_REVERSE_ASSOC, assocName);
		props.put(PROP_DISABLE_TREE, true);

		int pageSize = dataListFilter.getPagination().getPageSize();
		int startIndex = (dataListFilter.getPagination().getPage() - 1) * dataListFilter.getPagination().getPageSize();

		MultiLevelListData wUsedData = paginatedSearchCache.getSearchMultiLevelResults(dataListFilter.getPagination().getQueryExecutionId());

		if (wUsedData == null) {

			wUsedData = wUsedListService.getWUsedEntity(getWusedNodeRefs(dataListFilter), getWUsedOperator(dataListFilter),
					getWUsedFilter(dataListFilter, associationName), associationName, dataListFilter.getMaxDepth());

			dataListFilter.getPagination().setQueryExecutionId(paginatedSearchCache.storeMultiLevelSearchResults(wUsedData));

		}

		sortWUsedData(wUsedData, dataListFilter);

		appendNextLevel(ret, metadataFields, wUsedData, 0, startIndex, pageSize, props, dataListFilter);

		ret.setFullListSize(wUsedData.getSize());

		return ret;

	}

	/**
	 * <p>Sorts the where-used tree according to the column selected in the data grid.</p>
	 *
	 * <p>The sort is applied in-memory, before pagination, on the node displayed for each row (each
	 * tree entry key) and recursively on every sub-level so the tree structure is preserved. When no
	 * explicit sort is requested the natural query order is kept.</p>
	 *
	 * @param wUsedData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 */
	private void sortWUsedData(MultiLevelListData wUsedData, DataListFilter dataListFilter) {
		if (dataListFilter.isDefaultSort()) {
			return;
		}

		Map<String, Boolean> sortMap = dataListFilter.getSortMap();
		if ((sortMap == null) || sortMap.isEmpty()) {
			return;
		}

		Entry<String, Boolean> sortEntry = sortMap.entrySet().iterator().next();
		String sortKey = sortEntry.getKey();
		if ((sortKey == null) || !sortKey.startsWith("@")) {
			return;
		}

		QName sortQName;
		try {
			sortQName = QName.createQName(sortKey.substring(1));
		} catch (Exception e) {
			logger.warn("Unable to parse wUsed sort field: " + sortKey);
			return;
		}

		boolean ascending = !Boolean.FALSE.equals(sortEntry.getValue());
		Comparator<String> valueComparator = ascending ? String.CASE_INSENSITIVE_ORDER : String.CASE_INSENSITIVE_ORDER.reversed();
		Comparator<NodeRef> comparator = Comparator.comparing(nodeRef -> extractSortValue(nodeRef, sortQName),
				Comparator.nullsLast(valueComparator));

		sortTree(wUsedData, comparator);
	}

	/**
	 * <p>Reorders the entries of a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} tree
	 * and all its sub-levels using the given comparator.</p>
	 *
	 * @param listData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object
	 * @param comparator a {@link java.util.Comparator} object
	 */
	private void sortTree(MultiLevelListData listData, Comparator<NodeRef> comparator) {
		Map<NodeRef, MultiLevelListData> tree = listData.getTree();
		if (tree.isEmpty()) {
			return;
		}

		List<Entry<NodeRef, MultiLevelListData>> entries = new ArrayList<>(tree.entrySet());
		entries.sort(Entry.comparingByKey(comparator));

		tree.clear();
		for (Entry<NodeRef, MultiLevelListData> entry : entries) {
			tree.put(entry.getKey(), entry.getValue());
			sortTree(entry.getValue(), comparator);
		}
	}

	/**
	 * <p>Extracts a comparable display value for the given sort field on a row node. Properties are
	 * formatted as they are displayed, associations are resolved to their (sorted, comma-separated)
	 * names. Blank values are returned as {@code null} so empty rows are kept last.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param sortQName a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.lang.String} object, or {@code null} when there is no value
	 */
	private String extractSortValue(NodeRef nodeRef, QName sortQName) {
		String value = null;

		PropertyDefinition propertyDef = entityDictionaryService.getProperty(sortQName);
		if (propertyDef != null) {
			Serializable property = nodeService.getProperty(nodeRef, sortQName);
			if (property != null) {
				value = attributeExtractorService.getStringValue(propertyDef, property,
						attributeExtractorService.getPropertyFormats(FormatMode.JSON, false));
			}
		} else if (entityDictionaryService.getAssociation(sortQName) != null) {
			List<String> names = new ArrayList<>();
			for (AssociationRef assocRef : nodeService.getTargetAssocs(nodeRef, sortQName)) {
				String name = attributeExtractorService.extractPropName(assocRef.getTargetRef());
				if (name != null) {
					names.add(name);
				}
			}
			Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
			value = String.join(", ", names);
		}

		return ((value == null) || value.isBlank()) ? null : value;
	}

	/**
	 * <p>getWUsedFilter.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @param reverseAssociationName a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link fr.becpg.repo.entity.datalist.WUsedFilter} object
	 */
	private WUsedFilter getWUsedFilter(final DataListFilter dataListFilter, final QName reverseAssociationName) {
		return new WUsedFilter() {

			@Override
			public void filter(MultiLevelListData wUsedData) {
				if (dataListFilter.getFilterId().equals(DataListFilter.FORM_FILTER)) {
					Map<String, Map<String, String>> nested = nestedAdvSearchPlugin.extractNested(dataListFilter.getCriteriaMap());

					if (!nested.isEmpty()) {

						for (Map.Entry<String, Map<String, String>> nestedEntry : nested.entrySet()) {
							String assocName = nestedEntry.getKey();
							QName assocQName = QName.createQName(assocName, namespaceService);
							Map<String, String> criteriaMap = nestedAdvSearchPlugin.cleanCriteria(nestedEntry.getValue());

							for (Iterator<Entry<NodeRef, MultiLevelListData>> iterator = wUsedData.getTree().entrySet().iterator(); iterator
									.hasNext();) {
								Entry<NodeRef, MultiLevelListData> entry = iterator.next();
								boolean foundMatch = false;

								if (assocQName.equals(reverseAssociationName)) {
									foundMatch = nestedAdvSearchPlugin.match(entry.getValue().getEntityNodeRef(), criteriaMap);
								} else {

									NodeRef nodeRef = entry.getKey();

									List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocQName);

									for (AssociationRef assocRef : assocRefs) {
										if (nestedAdvSearchPlugin.match(assocRef.getTargetRef(), criteriaMap)) {
											foundMatch = true;
										}
									}

								}

								if (!foundMatch) {
									iterator.remove();
								}
							}
						}

					}
				}
			}

			@Override
			public WUsedFilterKind getFilterKind() {
				return WUsedFilterKind.STANDARD;
			}

			@Override
			public List<AssociationCriteriaFilter> getCriteriaFilters() {
				List<AssociationCriteriaFilter> criteriaFilters = new ArrayList<>();
				if ((dataListFilter.getExtraParams() != null) && !dataListFilter.getExtraParams().isBlank()) {
					try {
						JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
						if (jsonObject.has("typeFilter")) {
							String typeFilter = (String) jsonObject.get("typeFilter");
							if ((typeFilter != null) && !typeFilter.isEmpty() && !"all".equalsIgnoreCase(typeFilter)) {
								criteriaFilters.add(new AssociationCriteriaFilter(typeFilter));
							}
						}
					} catch (JSONException e) {
						logger.error(e);
					}
				}
				if (dataListFilter.getFilterId().equals(DataListFilter.FORM_FILTER)) {
					if (dataListFilter.getCriteriaMap() != null && !dataListFilter.getCriteriaMap().isEmpty()) {
						for (Map.Entry<String, String> entry : dataListFilter.getCriteriaMap().entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							if (value != null && !value.isEmpty() && !key.equals(DataListFilter.PROP_DEPTH_LEVEL) && !key.startsWith("nested_")) {
								if (key.startsWith(AttributeExtractorService.PROP_SUFFIX)) {
									String qNameStr = key.replace(AttributeExtractorService.PROP_SUFFIX, "").replace("_", ":");
									boolean isRange = qNameStr.endsWith("-range");
									boolean isDateRange = qNameStr.endsWith("-date-range");
									if (isDateRange) {
										qNameStr = qNameStr.substring(0, qNameStr.length() - "-date-range".length());
									} else if (isRange) {
										qNameStr = qNameStr.substring(0, qNameStr.length() - "-range".length());
									}
									try {
										QName qName = QName.createQName(qNameStr, namespaceService);
										PropertyDefinition propertyDef = entityDictionaryService.getProperty(qName);
										if (propertyDef != null) {
											String criteriaValue = cleanValueForDB(value);
											AssociationCriteriaFilterMode mode;
											boolean dateRange;
											if (isRange) {
												mode = AssociationCriteriaFilterMode.RANGE;
												dateRange = isDateRange;
												if (isDateRange) {
													criteriaValue = cropDateRangeValue(criteriaValue);
												}
											} else if (isDateProperty(propertyDef)) {
												criteriaValue = buildLocalDayUtcRange(cropDateBound(criteriaValue));
												mode = AssociationCriteriaFilterMode.RANGE;
												dateRange = false;
											} else {
												mode = AssociationCriteriaFilterMode.EQUALS;
												dateRange = false;
											}
											AssociationCriteriaFilter filter = new AssociationCriteriaFilter(qName, criteriaValue, mode);
											filter.setEntityFilter(!isListItemProperty(dataListFilter.getDataType(), qName));
											filter.setDateRange(dateRange);
											criteriaFilters.add(filter);
										}
									} catch (Exception e) {
										logger.error("Error parsing QName: " + qNameStr, e);
									}
								}
							}
						}
					}
				}
				return criteriaFilters;
			}

		};
	}

	private String cleanValueForDB(String criteriaValue) {
		if (criteriaValue != null && criteriaValue.startsWith("=")) {
			return criteriaValue.substring(1);
		}
		return criteriaValue;
	}

	/**
	 * <p>Crops each bound of a packed date range ({@code from|to}) to its leading {@code YYYY-MM-DD}
	 * part so the comparison is done on the date only.</p>
	 *
	 * <p>Effectivity dates are {@code d:datetime} properties persisted as full ISO8601 strings with a
	 * time and a timezone offset (e.g. {@code 2026-06-23T00:00:00.000+02:00}). Comparing such strings
	 * lexicographically against the bounds sent by the filter form ({@code Z} suffix, no milliseconds)
	 * yields wrong results (the {@code +02:00} offset sorts before {@code Z}, milliseconds shift the
	 * upper bound), which made every date filter return an empty list. Keeping only the date part makes
	 * the range comparison timezone and time independent, consistent with the Solr-based advanced search.</p>
	 *
	 * @param rangeValue the packed {@code from|to} range value
	 * @return the range value with both bounds cropped to their date part
	 */
	private String cropDateRangeValue(String rangeValue) {
		if (rangeValue == null) {
			return null;
		}
		int sepIndex = rangeValue.indexOf('|');
		if (sepIndex < 0) {
			return cropDateBound(rangeValue);
		}
		return cropDateBound(rangeValue.substring(0, sepIndex)) + "|" + cropDateBound(rangeValue.substring(sepIndex + 1));
	}

	private String cropDateBound(String bound) {
		return ((bound != null) && (bound.length() > 10)) ? bound.substring(0, 10) : bound;
	}

	/** ISO8601 UTC formatter matching the datetime strings persisted by Alfresco ({@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}). */
	private static final DateTimeFormatter UTC_ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

	/**
	 * <p>Turns a single local day ({@code YYYY-MM-DD}, as sent by the WUsed date picker) into a packed
	 * {@code from|to} range of UTC datetime bounds covering that whole day in the server timezone.</p>
	 *
	 * <p>Effectivity dates are {@code d:datetime} persisted in UTC (e.g. midnight of 24/06/2026 in
	 * {@code Europe/Paris} is stored {@code 2026-06-23T22:00:00.000Z}). Cropping the stored value to its
	 * date part therefore yields the previous day for any midnight-local date, so a filter on the displayed
	 * day matched nothing. Comparing the full stored datetime against the day's UTC bounds instead is
	 * timezone-correct: for a filter day {@code D} it selects every datetime falling within {@code D} in the
	 * server timezone.</p>
	 *
	 * @param isoDate the local day in {@code YYYY-MM-DD} form
	 * @return the packed {@code from|to} range of UTC ISO8601 bounds for that day
	 */
	private String buildLocalDayUtcRange(String isoDate) {
		LocalDate day = LocalDate.parse(isoDate);
		ZoneId zone = ZoneId.systemDefault();
		String from = UTC_ISO8601.format(day.atStartOfDay(zone).toInstant());
		String to = UTC_ISO8601.format(day.atTime(LocalTime.MAX).atZone(zone).toInstant());
		return from + "|" + to;
	}

	/**
	 * <p>Indicates whether a property is a {@code d:date} or {@code d:datetime}.</p>
	 *
	 * <p>The WUsed filter form exposes effectivity dates ({@code bcpg:startEffectivity} /
	 * {@code bcpg:endEffectivity}) as single date pickers, which submit the property key without a
	 * {@code -date-range} suffix and a single date value. Such a value must be matched against the
	 * whole day (see {@link #buildLocalDayUtcRange(String)}), not compared exactly against the full
	 * ISO8601 datetime stored in the database.</p>
	 *
	 * @param propertyDef the property definition to test
	 * @return {@code true} if the property holds a date or datetime value
	 */
	private boolean isDateProperty(PropertyDefinition propertyDef) {
		if (propertyDef.getDataType() == null) {
			return false;
		}
		QName dataType = propertyDef.getDataType().getName();
		return DataTypeDefinition.DATE.equals(dataType) || DataTypeDefinition.DATETIME.equals(dataType);
	}

	/**
	 * <p>Indicates whether a property is carried by the data list item type (including its mandatory
	 * aspects) rather than by the where-used entity.</p>
	 *
	 * <p>In the WUsed grid, effectivity dates ({@code bcpg:startEffectivity} / {@code bcpg:endEffectivity},
	 * from the mandatory {@code bcpg:effectivityAspect} of {@code bcpg:compoList}) belong to the composition
	 * line, not to the using product. Such filters must therefore target the list item node, not the entity
	 * node, otherwise they compare against the entity's own (often absent) effectivity and drop every row.</p>
	 *
	 * @param itemType the data list item type (e.g. {@code bcpg:compoList})
	 * @param propQName the filtered property
	 * @return {@code true} if the property is defined on the item type or one of its mandatory aspects
	 */
	private boolean isListItemProperty(QName itemType, QName propQName) {
		if (itemType == null) {
			return false;
		}
		ClassDefinition classDef = entityDictionaryService.getClass(itemType);
		if (classDef == null) {
			return false;
		}
		if (classDef.getProperties().containsKey(propQName)) {
			return true;
		}
		for (AspectDefinition aspectDef : classDef.getDefaultAspects(true)) {
			if (aspectDef.getProperties().containsKey(propQName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>getWUsedOperator.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a {@link fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator} object
	 */
	private WUsedOperator getWUsedOperator(DataListFilter dataListFilter) {
		if ((dataListFilter.getExtraParams() != null) && (dataListFilter.getExtraParams().length() > 0)) {
			try {
				JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
				if (jsonObject.has("operator")) {
					return Enum.valueOf(WUsedOperator.class, (String) jsonObject.get("operator"));
				}

			} catch (JSONException e) {
				logger.error(e);
			}
		}
		return WUsedOperator.AND;
	}

	/**
	 * <p>getWusedNodeRefs.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a {@link java.util.List} object
	 */
	private List<NodeRef> getWusedNodeRefs(DataListFilter dataListFilter) {

		List<NodeRef> ret = dataListFilter.getEntityNodeRefs();

		if ((ret == null) || ret.isEmpty()) {
			if ((dataListFilter.getExtraParams() != null) && (dataListFilter.getExtraParams().length() > 0)) {
				try {
					JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
					if ( jsonObject.has("searchQuery")) {
						JSONObject searchQuery = (JSONObject) jsonObject.get("searchQuery");
						String searchTerm = (String) jsonObject.get("searchTerm");
						if (searchQuery != null) {
							Map<String, String> criteriaMap = JsonHelper.extractCriteria(searchQuery);
							QName datatype = QName.createQName(searchQuery.getString("datatype"), namespaceService);

							BeCPGQueryBuilder queryBuilder = advSearchService.createSearchQuery(datatype, searchTerm, null, true, null, null);

							ret = advSearchService.queryAdvSearch(datatype, queryBuilder, criteriaMap, RepoConsts.MAX_RESULTS_256);
						}
					}

				} catch (JSONException e) {
					logger.error(e);
				}
			}

		}
		return ret;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> extractJSON(NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props,
			Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.extractJSON(nodeRef, metadataFields, props, cache);

		Map<String, Object> permissions = (Map<String, Object>) ret.get(PROP_PERMISSIONS);
		Map<String, Boolean> userAccess = (Map<String, Boolean>) permissions.get(PROP_USERACCESS);

		userAccess.put("delete", userAccess.get("delete"));
		userAccess.put("create", false);
		
		//TODO permissions not checked for dataLists
		
		userAccess.put("edit", userAccess.get("edit"));
		userAccess.put("sort", false);
		userAccess.put("details", false);
		userAccess.put("wused", true);

		ret.put(PROP_PERMISSIONS, permissions);

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && (dataListFilter.getDataListName() != null)
				&& dataListFilter.getDataListName().startsWith(RepoConsts.WUSED_PREFIX);
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
