package fr.becpg.repo.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;
import fr.becpg.repo.helper.impl.EntitySourceAssoc;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.impl.DataListSearchFilter;
import fr.becpg.repo.search.impl.DataListSearchFilterField;
import fr.becpg.repo.search.impl.SearchConfig;

/**
 * <p>ProductAdvSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductAdvSearchPlugin implements AdvSearchPlugin {

	private static final Log logger = LogFactory.getLog(ProductAdvSearchPlugin.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private WUsedListService wUsedListService;

	@Autowired
	private AlfrescoRepository<ProductSpecificationData> alfrescoRepository;

	private static final String CRITERIA_PACKAGING_LIST_PRODUCT = "assoc_bcpg_packagingListProduct_added";
	private static final String CRITERIA_PROCESS_LIST_RESSOURCE = "assoc_mpm_plResource_added";
	private static final String CRITERIA_COMPO_LIST_PRODUCT = "assoc_bcpg_compoListProduct_added";

	private static final String CRITERIA_NOTRESPECTED_SPECIFICATIONS = "assoc_bcpg_advNotRespectedProductSpecs_added";
	private static final String CRITERIA_RESPECTED_SPECIFICATIONS = "assoc_bcpg_advRespectedProductSpecs_added";

	private Set<String> keysToExclude = new HashSet<>();

	private void initKeys() {
		keysToExclude.add(CRITERIA_PACKAGING_LIST_PRODUCT);
		keysToExclude.add(CRITERIA_PROCESS_LIST_RESSOURCE);
		keysToExclude.add(CRITERIA_COMPO_LIST_PRODUCT);

		keysToExclude.add(CRITERIA_NOTRESPECTED_SPECIFICATIONS);
		keysToExclude.add(CRITERIA_RESPECTED_SPECIFICATIONS);

		keysToExclude.addAll(SearchConfig.getKeysToExclude());

	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria, SearchConfig searchConfig) {

		boolean isAssocSearch = isAssocSearch(criteria);

		if (isAssocSearch) {

			nodes = filterByAssociations(nodes, datatype, criteria);

			if ((datatype != null) && entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT)) {

				if (searchConfig.getDataListSearchFilters() != null) {
					for (DataListSearchFilter filter : searchConfig.getDataListSearchFilters()) {
						nodes = getSearchNodesByListCriteria(nodes, criteria, filter);
					}
				}

				getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_PACKAGING_LIST_PRODUCT, PLMModel.ASSOC_PACKAGINGLIST_PRODUCT, null, null);
				getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_COMPO_LIST_PRODUCT, PLMModel.ASSOC_COMPOLIST_PRODUCT, null, null);
				getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_PROCESS_LIST_RESSOURCE, MPMModel.ASSOC_PL_RESOURCE, null, null);

				nodes = getSearchNodesBySpecificationCriteria(nodes, criteria);

			}
		}

		return nodes;
	}

	private List<NodeRef> getSearchNodesByListCriteria(List<NodeRef> nodes, Map<String, String> criteria, DataListSearchFilter filter) {

		List<NodeRef> entities = new ArrayList<>();

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		List<EntitySourceAssoc> entitySourceAssocs = null;
		List<EntitySourceAssoc> notEntitySourceAssocs = null;

		for (DataListSearchFilterField assocFilter : filter.getAssocsFilters()) {
			String propValue = null;

			if (assocFilter.getValue() != null) {
				propValue = assocFilter.getValue();
			} else {
				propValue = criteria.get(assocFilter.getHtmlId());
			}

			if ((propValue != null) && !propValue.isBlank()) {

				List<AssociationCriteriaFilter> criteriaFilters = buildCriteriaFilters(criteria, filter);

				List<EntitySourceAssoc> tmp = associationService.getEntitySourceAssocs(extractNodeRefs(propValue), assocFilter.getAttributeQname(),
						"or".equals(assocFilter.getOperator()) || "not".equals(assocFilter.getOperator()), criteriaFilters);

				if ("not".equals(assocFilter.getOperator())) {

					if (notEntitySourceAssocs == null) {
						notEntitySourceAssocs = tmp;
					} else {
						merge(notEntitySourceAssocs, tmp, true);
					}

				} else {

					if (entitySourceAssocs == null) {
						entitySourceAssocs = tmp;
					} else {
						merge(entitySourceAssocs, tmp, true);
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Found dataList items to filter: " + assocFilter.getAttributeQname() + ", size:  " + tmp.size()
							+ " items, operator: " + assocFilter.getOperator());
				}
			}
		}

		if (entitySourceAssocs != null) {

			if (notEntitySourceAssocs != null) {
				merge(entitySourceAssocs, notEntitySourceAssocs, false);
			}

			for (EntitySourceAssoc assocRef : entitySourceAssocs) {

				if (nodes.contains(assocRef.getEntityNodeRef())) {
					entities.add(assocRef.getEntityNodeRef());
				}
			}

			nodes.retainAll(entities);

		} else if (notEntitySourceAssocs != null) {
			nodes.removeAll(notEntitySourceAssocs.stream().map(a -> a.getEntityNodeRef()).collect(Collectors.toList()));

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesByListCriteria " + filter.getName() + " executed in  " + watch.getTotalTimeSeconds() + " seconds - size after "
					+ nodes.size());
		}

		return nodes;

	}

	private List<AssociationCriteriaFilter> buildCriteriaFilters(Map<String, String> criteria, DataListSearchFilter filter) {
		List<AssociationCriteriaFilter> criteriaFilters = new ArrayList<>();

		for (DataListSearchFilterField propFilter : filter.getPropFilters()) {

			String criteriaValue = null;

			if (propFilter.getValue() != null) {
				criteriaValue = propFilter.getValue();
			} else {
				criteriaValue = criteria.get(propFilter.getHtmlId());
			}

			if ((criteriaValue != null) && !criteriaValue.isBlank()) {
				QName attributeQName = propFilter.getAttributeQname();

				// Warning : this will be a problem with generic raw materials
				if (!PLMModel.TYPE_RAWMATERIAL.getPrefixedQName(namespaceService).getPrefixString().equals(criteria.get("datatype")) && PLMModel.PROP_NUTLIST_VALUE.equals(attributeQName)) {

					attributeQName = PLMModel.PROP_NUTLIST_FORMULATED_VALUE;

				}

				PropertyDefinition propertyDef = entityDictionaryService.getProperty(attributeQName);
				if (propertyDef != null) {

					AssociationCriteriaFilter criteriaFilter = new AssociationCriteriaFilter(attributeQName, propertyDef.getDataType(), criteriaValue,
							(propFilter.getHtmlId() != null) && propFilter.getHtmlId().contains("-range"));

					criteriaFilters.add(criteriaFilter);

				}

			}

		}
		return criteriaFilters;
	}

	private void merge(List<EntitySourceAssoc> sources, List<EntitySourceAssoc> targets, boolean retain) {

		Iterator<EntitySourceAssoc> e = sources.iterator();
		while (e.hasNext()) {
			EntitySourceAssoc source = e.next();

			boolean contains = false;
			for (EntitySourceAssoc target : targets) {
				if (target.getDataListItemNodeRef().equals(source.getDataListItemNodeRef())
						&& target.getEntityNodeRef().equals(source.getEntityNodeRef())) {
					contains = true;
					break;
				}
			}

			if (retain) {
				if (!contains) {
					e.remove();
				}
			} else {
				if (contains) {
					e.remove();
				}
			}

		}

	}

	/** {@inheritDoc} */
	@Override
	public Set<String> getIgnoredFields(QName datatype, SearchConfig searchConfig) {
		Set<String> ret = new HashSet<>();
		if ((datatype != null) && entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT)) {

			if (keysToExclude.isEmpty()) {
				initKeys();
			}

			ret.addAll(keysToExclude);
		}
		return ret;
	}

	/**
	 * Take in account criteria on associations (ie :
	 * assoc_bcpg_supplierAssoc_added)
	 *
	 * @param datatype
	 *
	 * @return filtered list of nodes by associations
	 */
	private List<NodeRef> filterByAssociations(List<NodeRef> nodes, QName datatype, Map<String, String> criteria) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		Set<NodeRef> nodesToKeepOr = new HashSet<>();
		boolean hasOrOperand = false;

		for (Map.Entry<String, String> criterion : criteria.entrySet()) {

			String key = criterion.getKey();
			String propValue = criterion.getValue();

			// association
			if (key.startsWith("assoc_") && !propValue.isEmpty()) {

				String assocName = key.substring(6);
				if (assocName.endsWith("_added")) {
					if (!entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT) || !keysToExclude.contains(key)) {

						boolean isOROperand = false;

						if (assocName.endsWith("_or_added")) {
							isOROperand = true;
							hasOrOperand = true;
							assocName = assocName.substring(0, assocName.length() - 9);
						} else {

							assocName = assocName.substring(0, assocName.length() - 6);
						}
						assocName = assocName.replace("_", ":");
						QName assocQName = QName.createQName(assocName, namespaceService);

						String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
						Set<NodeRef> nodesToKeep = new HashSet<>();

						for (String strNodeRef : arrValues) {
							NodeRef nodeRef = new NodeRef(strNodeRef);
							if (nodeService.exists(nodeRef)) {
								nodesToKeep.addAll(associationService.getSourcesAssocs(nodeRef, assocQName));
							}

						}
						if (!isOROperand) {
							nodes.retainAll(nodesToKeep);
						} else {
							nodesToKeepOr.addAll(nodesToKeep);
						}
					}
				}
			}
		}

		if (hasOrOperand) {
			nodes.retainAll(nodesToKeepOr);
		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("filterByAssociations executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	private List<NodeRef> extractNodeRefs(String propValue) {
		String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
		List<NodeRef> ret = new ArrayList<>();

		for (String strNodeRef : arrValues) {

			if (!strNodeRef.isBlank()) {

				NodeRef nodeRef = new NodeRef(strNodeRef);

				if (nodeService.exists(nodeRef)) {
					ret.add(nodeRef);
					if (logger.isDebugEnabled()) {
						int size = associationService.getSourcesAssocs(nodeRef, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION).size();
						if (size > 0) {
							logger.debug("Found linked  associated search to add :" + size);
						}

					}
					ret.addAll(associationService.getSourcesAssocs(nodeRef, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION));
				}
			}

		}

		return ret;
	}

	private List<NodeRef> getSearchNodesBySpecificationCriteria(List<NodeRef> nodes, Map<String, String> criteria) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(CRITERIA_NOTRESPECTED_SPECIFICATIONS)) {

			String propValue = criteria.get(CRITERIA_NOTRESPECTED_SPECIFICATIONS);
			if ((propValue != null) && !propValue.isBlank()) {
				for (NodeRef nodeRef : extractNodeRefs(propValue)) {

					ProductSpecificationData productSpecificationData = alfrescoRepository.findOne(nodeRef);
					List<NodeRef> retainNodes = new ArrayList<>();
					for (NodeRef productNodeRef : nodes) {
						boolean retain = false;
						for (SpecCompatibilityDataItem dataItem : productSpecificationData.getSpecCompatibilityList()) {
							if ((dataItem.getSourceItem() != null) && dataItem.getSourceItem().equals(productNodeRef)) {
								retain = true;
								break;
							}

						}
						if (retain) {
							retainNodes.add(productNodeRef);
						}

					}
					nodes.retainAll(retainNodes);

				}

			}
		}

		if (criteria.containsKey(CRITERIA_RESPECTED_SPECIFICATIONS)) {

			String propValue = criteria.get(CRITERIA_RESPECTED_SPECIFICATIONS);
			if ((propValue != null) && !propValue.isEmpty()) {
				for (NodeRef nodeRef : extractNodeRefs(propValue)) {
					ProductSpecificationData productSpecificationData = alfrescoRepository.findOne(nodeRef);
					List<NodeRef> removedNodes = new ArrayList<>();
					for (NodeRef productNodeRef : nodes) {
						boolean remove = false;
						for (SpecCompatibilityDataItem dataItem : productSpecificationData.getSpecCompatibilityList()) {
							if ((dataItem.getSourceItem() != null) && dataItem.getSourceItem().equals(productNodeRef)) {
								remove = true;
								break;
							}

						}
						if (remove) {
							removedNodes.add(productNodeRef);
						}

					}
					nodes.removeAll(removedNodes);

				}
			}

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesBySpecificationCriteria executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;

	}

	private List<NodeRef> getSearchNodesByWUsedCriteria(List<NodeRef> nodes, Map<String, String> criteria, String criteriaAssocString,
			QName criteriaAssoc, QName criteriaAssocValue, String criteriaValue) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(criteriaAssocString)) {

			String propValue = criteria.get(criteriaAssocString);

			if ((propValue != null) && !propValue.isBlank()) {
				List<NodeRef> toFilterByNodes = extractNodeRefs(propValue);

				if (!toFilterByNodes.isEmpty()) {

					MultiLevelListData ret = wUsedListService.getWUsedEntity(toFilterByNodes, WUsedOperator.OR, criteriaAssoc, -1);
					if (ret != null) {
						nodes.retainAll(ret.getAllChilds());
					}
				}
			}

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesByWUsedCriteria executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	private boolean isAssocSearch(Map<String, String> criteria) {
		if (criteria != null) {
			for (Map.Entry<String, String> criterion : criteria.entrySet()) {
				String key = criterion.getKey();
				// association
				if (key.startsWith("assoc_")) {
					return true;
				}
			}
		}
		return false;
	}

}
