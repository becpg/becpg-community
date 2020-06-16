package fr.becpg.repo.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
import fr.becpg.repo.helper.impl.AssociationServiceImpl.EntitySourceAssoc;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

@Service
public class ProductAdvSearchPlugin implements AdvSearchPlugin {

	private static final Log logger = LogFactory.getLog(AdvSearchPlugin.class);

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


	private class DataListSearchFilterField {

		private String operator = "or";
		private String value;
		private String htmlId;
		private QName attributeQname;

	}

	private class DataListSearchFilter {

		private String name;
		private List<DataListSearchFilterField> assocsFilters = new ArrayList<>();
		private List<DataListSearchFilterField> propFilters = new ArrayList<>();

		public DataListSearchFilter(String name) {
			super();
			this.name = name;
		}

	}

	List<DataListSearchFilter> dataListSearchFilters = new ArrayList<>();

	@PostConstruct
	public void init() throws UnsupportedEncodingException, IOException, JSONException {

		Resource res = new ClassPathResource("beCPG/search/productAdvSearch.json");

		BufferedReader streamReader = new BufferedReader(new InputStreamReader(res.getInputStream(), "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();

		String inputStr;
		while ((inputStr = streamReader.readLine()) != null) {
			responseStrBuilder.append(inputStr);
		}

		JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

		JSONObject filters = jsonObject.getJSONObject("filters");

		for (@SuppressWarnings("unchecked")
		Iterator<String> iterator = filters.keys(); iterator.hasNext();) {
			String filterName = iterator.next();

			JSONArray jsonArray = filters.getJSONArray(filterName);

			DataListSearchFilter filter = new DataListSearchFilter(filterName);

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject conf = jsonArray.getJSONObject(i);

				DataListSearchFilterField field = new DataListSearchFilterField();

				field.attributeQname = QName.createQName(conf.getString("attribute"), namespaceService);

				if (conf.has("operator")) {
					field.operator = conf.getString("operator").toLowerCase();
				}

				if (conf.has("value")) {
					field.value = conf.getString("value");
				}

				if (conf.has("htmlId")) {
					field.htmlId = conf.getString("htmlId");

					keysToExclude.add(field.htmlId);

				}

				if (entityDictionaryService.isAssoc(field.attributeQname)) {
					filter.assocsFilters.add(field);
				} else {
					filter.propFilters.add(field);
				}

			}

			dataListSearchFilters.add(filter);

		}

	}

	private static Set<String> keysToExclude = new HashSet<>();

	static {

		keysToExclude.add(CRITERIA_PACKAGING_LIST_PRODUCT);
		keysToExclude.add(CRITERIA_PROCESS_LIST_RESSOURCE);
		keysToExclude.add(CRITERIA_COMPO_LIST_PRODUCT);

		keysToExclude.add(CRITERIA_NOTRESPECTED_SPECIFICATIONS);
		keysToExclude.add(CRITERIA_RESPECTED_SPECIFICATIONS);

	}

	@Override
	public List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria) {

		boolean isAssocSearch = isAssocSearch(criteria);

		if (isAssocSearch) {
			nodes = filterByAssociations(nodes, datatype, criteria);

			if ((datatype != null) && entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT)) {

				if (dataListSearchFilters != null) {
					for (DataListSearchFilter filter : dataListSearchFilters) {
						nodes = getSearchNodesByListCriteria(nodes, criteria, filter);
					}
				}

				nodes = getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_PACKAGING_LIST_PRODUCT, PLMModel.ASSOC_PACKAGINGLIST_PRODUCT, null,
						null);
				nodes = getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_COMPO_LIST_PRODUCT, PLMModel.ASSOC_COMPOLIST_PRODUCT, null, null);
				nodes = getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_PROCESS_LIST_RESSOURCE, MPMModel.ASSOC_PL_RESOURCE, null, null);

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

		for (DataListSearchFilterField assocFilter : filter.assocsFilters) {
			String propValue = null;

			if (assocFilter.value != null) {
				propValue = assocFilter.value;
			} else {
				propValue = criteria.get(assocFilter.htmlId);
			}

			if (propValue != null) {

				List<EntitySourceAssoc> tmp = associationService.getEntitySourceAssocs(extractNodeRefs(propValue), assocFilter.attributeQname,
						"or".equals(assocFilter.operator) || "not".equals(assocFilter.operator));

				if ("not".equals(assocFilter.operator)) {

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

			}
		}

		if (entitySourceAssocs != null) {

			if (notEntitySourceAssocs != null) {
				merge(entitySourceAssocs, notEntitySourceAssocs, false);
			}

			for (EntitySourceAssoc assocRef : entitySourceAssocs) {

				boolean match = true;
				for (DataListSearchFilterField propFilter : filter.propFilters) {
					String criteriaValue = null;

					if (propFilter.value != null) {
						criteriaValue = propFilter.value;
					} else {
						criteriaValue = criteria.get(propFilter.htmlId);
					}

					if ((criteriaValue != null) && !criteriaValue.isEmpty()) {
						match = false;

						NodeRef n = assocRef.getDataListItemNodeRef();
						Object value = nodeService.getProperty(n, propFilter.attributeQname);
						if (PLMModel.PROP_NUTLIST_VALUE.equals(propFilter.attributeQname) && (value == null)) {
							value = nodeService.getProperty(n, PLMModel.PROP_NUTLIST_FORMULATED_VALUE);
						}

						if (value != null) {
							if (value instanceof String) {
								if (criteriaValue.equals(value)) {
									match = true;
								}

							} else if (value instanceof Boolean) {
								if (Boolean.valueOf(criteriaValue).equals(value)) {
									match = true;
								}

							} else if (value instanceof Double) {
								String[] splitted = criteriaValue.split("\\|");
								if (splitted.length == 2) {
									if (logger.isDebugEnabled()) {
										logger.debug("filter by range: " + splitted[0] + "->" + splitted[1] + ", value=" + value);
									}
									if ((splitted[0].isEmpty() || (((Double) value) >= Double.valueOf(splitted[0])))
											&& (splitted[1].isEmpty() || (((Double) value) <= Double.valueOf(splitted[1])))) {
										match = true;
									}
								} else if (splitted.length == 1) {
									if ((splitted[0].isEmpty() || (((Double) value) >= Double.valueOf(splitted[0])))) {
										match = true;
									}
								}
							}
						}

					}

				}
				if (match) {
					entities.add(assocRef.getEntityNodeRef());
				}

			}

			nodes.retainAll(entities);
		} else if (notEntitySourceAssocs != null) {
			nodes.removeAll(notEntitySourceAssocs.stream().map(a -> a.getEntityNodeRef()).collect(Collectors.toList()));

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesByListCriteria " + filter.name + " executed in  " + watch.getTotalTimeSeconds() + " seconds");
		}

		return nodes;

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

			if(retain) {
				if(!contains) {
					e.remove();
				}
			} else {
				if(contains) {
					e.remove();
				}
			}
			
		}

	}

	@Override
	public Set<String> getIgnoredFields(QName datatype) {
		Set<String> ret = new HashSet<>();
		if ((datatype != null) && entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT)) {
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

			NodeRef nodeRef = new NodeRef(strNodeRef);

			if (nodeService.exists(nodeRef)) {
				ret.add(nodeRef);
				if (logger.isDebugEnabled()) {
					logger.debug("Adding associated search :"
							+ associationService.getSourcesAssocs(nodeRef, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION).size());

				}

				ret.addAll(associationService.getSourcesAssocs(nodeRef, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION));
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
			if ((propValue != null) && !propValue.isEmpty()) {
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

			List<NodeRef> toFilterByNodes = extractNodeRefs(propValue);

			if (!toFilterByNodes.isEmpty()) {

				MultiLevelListData ret = wUsedListService.getWUsedEntity(toFilterByNodes, WUsedOperator.OR, criteriaAssoc, -1);
				if (ret != null) {
					nodes.retainAll(ret.getAllChilds());
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
