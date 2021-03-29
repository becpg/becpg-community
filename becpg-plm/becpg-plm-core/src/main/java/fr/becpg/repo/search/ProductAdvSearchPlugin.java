package fr.becpg.repo.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import fr.becpg.model.PackModel;
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

	private static final String CRITERIA_ING = "assoc_bcpg_ingListIng_added";
	private static final String CRITERIA_ING_AND = "assoc_bcpg_advIlIngAnd_added";
	private static final String CRITERIA_ING_NOT = "assoc_bcpg_advIlIngNot_added";
	private static final String CRITERIA_ING_RANGE = "prop_bcpg_ingListQtyPerc-range";

	private static final String CRITERIA_GEO_ORIGIN = "assoc_bcpg_ingListGeoOrigin_added";
	private static final String CRITERIA_BIO_ORIGIN = "assoc_bcpg_ingListBioOrigin_added";

	private static final String CRITERIA_PHYSICO = "assoc_bcpg_pclPhysicoChem_added";
	private static final String CRITERIA_PHYSICO_RANGE = "prop_bcpg_pclValue-range";
	private static final String CRITERIA_PHYSICO_1 = "assoc_bcpg_advPclPhysicoChem1_added";
	private static final String CRITERIA_PHYSICO_RANGE_1 = "prop_bcpg_advPclValue1-range";
	private static final String CRITERIA_PHYSICO_2 = "assoc_bcpg_advPclPhysicoChem2_added";
	private static final String CRITERIA_PHYSICO_RANGE_2 = "prop_bcpg_advPclValue2-range";

	private static final String CRITERIA_COST = "assoc_bcpg_costListCost_added";
	private static final String CRITERIA_COST_RANGE = "prop_bcpg_costListValue-range";
	private static final String CRITERIA_COST_1 = "assoc_bcpg_advClCost1_added";
	private static final String CRITERIA_COST_RANGE_1 = "prop_bcpg_advClCost1-range";
	private static final String CRITERIA_COST_2 = "assoc_bcpg_advClCost2_added";
	private static final String CRITERIA_COST_RANGE_2 = "prop_bcpg_advClCost2-range";

	private static final String CRITERIA_NUTS = "assoc_bcpg_nutListNut_added";
	private static final String CRITERIA_NUTS_RANGE = "prop_bcpg_nutListValue-range";
	private static final String CRITERIA_NUTS_1 = "assoc_bcpg_advNlNut1_added";
	private static final String CRITERIA_NUTS_RANGE_1 = "prop_bcpg_advNlValue1-range";
	private static final String CRITERIA_NUTS_2 = "assoc_bcpg_advNlNut2_added";
	private static final String CRITERIA_NUTS_RANGE_2 = "prop_bcpg_advNlValue2-range";
	private static final String CRITERIA_NUTS_3 = "assoc_bcpg_advNlNut3_added";
	private static final String CRITERIA_NUTS_RANGE_3 = "prop_bcpg_advNlValue3-range";

	private static final String CRITERIA_ALLERGEN = "assoc_bcpg_allergenListAllergen_added";
	private static final String CRITERIA_ALLERGEN_VOL_AND = "assoc_bcpg_advAlVolAnd_added";
	private static final String CRITERIA_ALLERGEN_VOL_NOT = "assoc_bcpg_advAlVolNot_added";
	private static final String CRITERIA_ALLERGEN_INVOL_AND = "assoc_bcpg_advAlInVolAnd_added";
	private static final String CRITERIA_ALLERGEN_INVOL_NOT = "assoc_bcpg_advAlInVolNot_added";

	private static final String CRITERIA_LABEL_CLAIM = "assoc_bcpg_lclLabelClaim_added";
	private static final String CRITERIA_LABEL_CLAIM_AND = "assoc_bcpg_advLclClaimAnd_added";
	private static final String CRITERIA_LABEL_CLAIM_NOT = "assoc_bcpg_advLclClaimNot_added";

	private static final String CRITERIA_MICROBIO = "assoc_bcpg_mblMicrobio_added";
	private static final String CRITERIA_MICROBIO_RANGE = "prop_bcpg_mblValue-range";

	private static final String CRITERIA_PACK_LABEL = "assoc_pack_llLabel_added";

	private static final String CRITERIA_PACKAGING_LIST_PRODUCT = "assoc_bcpg_packagingListProduct_added";
	private static final String CRITERIA_PROCESS_LIST_RESSOURCE = "assoc_mpm_plResource_added";
	private static final String CRITERIA_COMPO_LIST_PRODUCT = "assoc_bcpg_compoListProduct_added";

	private static final String CRITERIA_PACK_LABEL_POSITION = "prop_pack_llPosition";

	private static final String CRITERIA_NOTRESPECTED_SPECIFICATIONS = "assoc_bcpg_advNotRespectedProductSpecs_added";
	private static final String CRITERIA_RESPECTED_SPECIFICATIONS = "assoc_bcpg_advRespectedProductSpecs_added";

	private static Set<String> keysToExclude = new HashSet<>();

	static {
		keysToExclude.add(CRITERIA_ING);
		keysToExclude.add(CRITERIA_ING_AND);
		keysToExclude.add(CRITERIA_ING_NOT);
		keysToExclude.add(CRITERIA_ING_RANGE);

		keysToExclude.add(CRITERIA_GEO_ORIGIN);
		keysToExclude.add(CRITERIA_BIO_ORIGIN);

		keysToExclude.add(CRITERIA_PHYSICO);
		keysToExclude.add(CRITERIA_PHYSICO_1);
		keysToExclude.add(CRITERIA_PHYSICO_2);
		keysToExclude.add(CRITERIA_PHYSICO_RANGE);
		keysToExclude.add(CRITERIA_PHYSICO_RANGE_1);
		keysToExclude.add(CRITERIA_PHYSICO_RANGE_2);

		keysToExclude.add(CRITERIA_COST);
		keysToExclude.add(CRITERIA_COST_1);
		keysToExclude.add(CRITERIA_COST_2);
		keysToExclude.add(CRITERIA_COST_RANGE);
		keysToExclude.add(CRITERIA_COST_RANGE_1);
		keysToExclude.add(CRITERIA_COST_RANGE_2);

		keysToExclude.add(CRITERIA_NUTS);
		keysToExclude.add(CRITERIA_NUTS_1);
		keysToExclude.add(CRITERIA_NUTS_2);
		keysToExclude.add(CRITERIA_NUTS_3);
		keysToExclude.add(CRITERIA_NUTS_RANGE);
		keysToExclude.add(CRITERIA_NUTS_RANGE_1);
		keysToExclude.add(CRITERIA_NUTS_RANGE_2);
		keysToExclude.add(CRITERIA_NUTS_RANGE_3);

		keysToExclude.add(CRITERIA_ALLERGEN);
		keysToExclude.add(CRITERIA_ALLERGEN_VOL_AND);
		keysToExclude.add(CRITERIA_ALLERGEN_VOL_NOT);
		keysToExclude.add(CRITERIA_ALLERGEN_INVOL_AND);
		keysToExclude.add(CRITERIA_ALLERGEN_INVOL_NOT);

		keysToExclude.add(CRITERIA_LABEL_CLAIM);
		keysToExclude.add(CRITERIA_LABEL_CLAIM_AND);
		keysToExclude.add(CRITERIA_LABEL_CLAIM_NOT);

		keysToExclude.add(CRITERIA_MICROBIO);
		keysToExclude.add(CRITERIA_MICROBIO_RANGE);

		keysToExclude.add(CRITERIA_PACK_LABEL);
		keysToExclude.add(CRITERIA_PACK_LABEL_POSITION);

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
				nodes = getSearchNodesByIngListCriteria(nodes, criteria);
				nodes = getSearchNodesByLabelingCriteria(nodes, criteria);
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_LABEL_CLAIM, PLMModel.ASSOC_LCL_LABELCLAIM,
						PLMModel.PROP_LCL_CLAIM_VALUE, "true");
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_LABEL_CLAIM_AND, PLMModel.ASSOC_LCL_LABELCLAIM,
						PLMModel.PROP_LCL_CLAIM_VALUE, "true");
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_LABEL_CLAIM_NOT, PLMModel.ASSOC_LCL_LABELCLAIM,
						PLMModel.PROP_LCL_CLAIM_VALUE, "false");

				nodes = getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_PACKAGING_LIST_PRODUCT, PLMModel.ASSOC_PACKAGINGLIST_PRODUCT, null,
						null);
				nodes = getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_COMPO_LIST_PRODUCT, PLMModel.ASSOC_COMPOLIST_PRODUCT, null, null);
				nodes = getSearchNodesByWUsedCriteria(nodes, criteria, CRITERIA_PROCESS_LIST_RESSOURCE, MPMModel.ASSOC_PL_RESOURCE, null, null);

				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_ING_AND, PLMModel.ASSOC_INGLIST_ING, PLMModel.PROP_INGLIST_QTY_PERC,
						criteria.get(CRITERIA_ING_RANGE));

				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_ALLERGEN, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN,
						PLMModel.PROP_ALLERGENLIST_VOLUNTARY, "true");
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_ALLERGEN_VOL_AND, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN,
						PLMModel.PROP_ALLERGENLIST_VOLUNTARY, "true");
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_ALLERGEN_VOL_NOT, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN,
						PLMModel.PROP_ALLERGENLIST_VOLUNTARY, "false");
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_ALLERGEN_INVOL_AND, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN,
						PLMModel.PROP_ALLERGENLIST_INVOLUNTARY, "true");
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_ALLERGEN_INVOL_NOT, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN,
						PLMModel.PROP_ALLERGENLIST_INVOLUNTARY, "false");

				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_COST, PLMModel.ASSOC_COSTLIST_COST, PLMModel.PROP_COSTLIST_VALUE,
						criteria.get(CRITERIA_COST_RANGE));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_COST_1, PLMModel.ASSOC_COSTLIST_COST, PLMModel.PROP_COSTLIST_VALUE,
						criteria.get(CRITERIA_COST_RANGE_1));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_COST_2, PLMModel.ASSOC_COSTLIST_COST, PLMModel.PROP_COSTLIST_VALUE,
						criteria.get(CRITERIA_COST_RANGE_2));

				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_NUTS, PLMModel.ASSOC_NUTLIST_NUT, PLMModel.PROP_NUTLIST_VALUE,
						criteria.get(CRITERIA_NUTS_RANGE));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_NUTS_1, PLMModel.ASSOC_NUTLIST_NUT, PLMModel.PROP_NUTLIST_VALUE,
						criteria.get(CRITERIA_NUTS_RANGE_1));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_NUTS_2, PLMModel.ASSOC_NUTLIST_NUT, PLMModel.PROP_NUTLIST_VALUE,
						criteria.get(CRITERIA_NUTS_RANGE_2));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_NUTS_3, PLMModel.ASSOC_NUTLIST_NUT, PLMModel.PROP_NUTLIST_VALUE,
						criteria.get(CRITERIA_NUTS_RANGE_3));

				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_PHYSICO, PLMModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM,
						PLMModel.PROP_PHYSICOCHEMLIST_VALUE, criteria.get(CRITERIA_PHYSICO_RANGE));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_PHYSICO_1, PLMModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM,
						PLMModel.PROP_PHYSICOCHEMLIST_VALUE, criteria.get(CRITERIA_PHYSICO_RANGE_1));
				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_PHYSICO_2, PLMModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM,
						PLMModel.PROP_PHYSICOCHEMLIST_VALUE, criteria.get(CRITERIA_PHYSICO_RANGE_2));

				nodes = getSearchNodesByListCriteria(nodes, criteria, CRITERIA_MICROBIO, PLMModel.ASSOC_MICROBIOLIST_MICROBIO,
						PLMModel.PROP_MICROBIOLIST_VALUE, criteria.get(CRITERIA_MICROBIO_RANGE));

				nodes = getSearchNodesBySpecificationCriteria(nodes, criteria);

			}
		}

		return nodes;
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

	/**
	 * Take in account criteria on ing list criteria
	 *
	 * @return
	 */
	private List<NodeRef> getSearchNodesByIngListCriteria(List<NodeRef> nodes, Map<String, String> criteria) {

		List<NodeRef> ingListItemsEntity = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		for (Map.Entry<String, String> criterion : criteria.entrySet()) {

			String key = criterion.getKey();
			String propValue = criterion.getValue();

			if ((propValue != null) && !propValue.isEmpty()) {

				// criteria on ing
				if ((key.equals(CRITERIA_ING) || key.equals(CRITERIA_ING_AND))) {

					List<NodeRef> filter = associationService
							.getEntitySourceAssocs(extractNodeRefs(propValue,key.equals(CRITERIA_ING)), PLMModel.ASSOC_INGLIST_ING, key.equals(CRITERIA_ING)).stream()
							.map(a -> a.getEntityNodeRef()).collect(Collectors.toList());

					if (ingListItemsEntity == null) {
						ingListItemsEntity = filter;
					} else {
						ingListItemsEntity.retainAll(filter);
					}

				} else if (key.equals(CRITERIA_ING_NOT)) {
					nodes.removeAll(associationService.getEntitySourceAssocs(extractNodeRefs(propValue,true), PLMModel.ASSOC_INGLIST_ING, true).stream()
							.map(a -> a.getEntityNodeRef()).collect(Collectors.toList()));

				} else if (key.equals(CRITERIA_GEO_ORIGIN)) {

					List<NodeRef> filter = associationService
							.getEntitySourceAssocs(extractNodeRefs(propValue,true), PLMModel.ASSOC_INGLIST_GEO_ORIGIN, true).stream()
							.map(a -> a.getEntityNodeRef()).collect(Collectors.toList());

					if (ingListItemsEntity == null) {
						ingListItemsEntity = filter;
					} else {
						ingListItemsEntity.retainAll(filter);
					}
				} else if (key.equals(CRITERIA_BIO_ORIGIN) && !propValue.isEmpty()) {

					List<NodeRef> filter = associationService
							.getEntitySourceAssocs(extractNodeRefs(propValue,true), PLMModel.ASSOC_INGLIST_BIO_ORIGIN, true).stream()
							.map(a -> a.getEntityNodeRef()).collect(Collectors.toList());
					if (ingListItemsEntity == null) {
						ingListItemsEntity = filter;
					} else {
						ingListItemsEntity.retainAll(filter);
					}

				}
			}
		}

		if (ingListItemsEntity != null) {
			nodes.retainAll(ingListItemsEntity);
		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesByIngListCriteria executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	private List<NodeRef> extractNodeRefs(String propValue, boolean isOrOperator) {
		String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
		List<NodeRef> ret = new ArrayList<>();

		for (String strNodeRef : arrValues) {

			NodeRef nodeRef = new NodeRef(strNodeRef);

			if (nodeService.exists(nodeRef)) {
				ret.add(nodeRef);
				if(isOrOperator) {
					if (logger.isDebugEnabled()) {
						logger.debug("Adding associated search :"
								+ associationService.getSourcesAssocs(nodeRef, BeCPGModel.ASSOC_LINKED_SEARCH_ASSOCIATION).size());
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
			if ((propValue != null) && !propValue.isEmpty()) {
				for (NodeRef nodeRef : extractNodeRefs(propValue,false)) {

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
				for (NodeRef nodeRef : extractNodeRefs(propValue,false)) {
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

	/**
	 * Take in account criteria on ing list criteria
	 *
	 * @return
	 */
	private List<NodeRef> getSearchNodesByLabelingCriteria(List<NodeRef> nodes, Map<String, String> criteria) {

		List<NodeRef> labelingListItemsEntity = new ArrayList<>();

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(CRITERIA_PACK_LABEL)) {

			String propValue = criteria.get(CRITERIA_PACK_LABEL);

			// criteria on label
			if ((propValue != null) && !propValue.isEmpty()) {

				for (EntitySourceAssoc assocRef : associationService.getEntitySourceAssocs(extractNodeRefs(propValue,true), PackModel.ASSOC_LL_LABEL,
						true)) {

					NodeRef n = assocRef.getDataListItemNodeRef();

					if (criteria.containsKey(CRITERIA_PACK_LABEL_POSITION) && !criteria.get(CRITERIA_PACK_LABEL_POSITION).isEmpty()) {

						if (criteria.get(CRITERIA_PACK_LABEL_POSITION).equals("\"" + nodeService.getProperty(n, PackModel.PROP_LL_POSITION) + "\"")) {
							labelingListItemsEntity.add(assocRef.getEntityNodeRef());
						}
					} else {

						labelingListItemsEntity.add(assocRef.getEntityNodeRef());
					}
				}

				nodes.retainAll(labelingListItemsEntity);

			}

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesByLabelingCriteria executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	/**
	 * Take in account criteria on label claim list criteria
	 *
	 * @return
	 */
	private List<NodeRef> getSearchNodesByListCriteria(List<NodeRef> nodes, Map<String, String> criteria, String criteriaAssocString,
			QName criteriaAssoc, QName criteriaAssocValue, String criteriaValue) {

		new HashMap<>();

		List<NodeRef> entities = new ArrayList<>();

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(criteriaAssocString)) {

			String propValue = criteria.get(criteriaAssocString);

			if ((propValue != null) && !propValue.isEmpty()) {

				for (EntitySourceAssoc assocRef : associationService.getEntitySourceAssocs(extractNodeRefs(propValue,true), criteriaAssoc, true)) {

					NodeRef n = assocRef.getDataListItemNodeRef();
					if ((criteriaAssocValue != null) && (criteriaValue != null) && !criteriaValue.isEmpty()) {
						Object value = nodeService.getProperty(n, criteriaAssocValue);
						if (PLMModel.PROP_NUTLIST_VALUE.equals(criteriaAssocValue) && (value == null)) {
							value = nodeService.getProperty(n, PLMModel.PROP_NUTLIST_FORMULATED_VALUE);
						}

						if (value != null) {
							if (value instanceof String) {
								if (criteriaValue.equals(value)) {
									entities.add(assocRef.getEntityNodeRef());
								}

							} else if (value instanceof Boolean) {
								if (Boolean.valueOf(criteriaValue).equals(value)) {
									entities.add(assocRef.getEntityNodeRef());
								}

							} else if (value instanceof Double) {
								String[] splitted = criteriaValue.split("\\|");
								if (splitted.length == 2) {
									if (logger.isDebugEnabled()) {
										logger.debug("filter by range: " + splitted[0] + "->" + splitted[1] + ", value=" + value);
									}
									if ((splitted[0].isEmpty() || (((Double) value) >= Double.valueOf(splitted[0])))
											&& (splitted[1].isEmpty() || (((Double) value) <= Double.valueOf(splitted[1])))) {
										entities.add(assocRef.getEntityNodeRef());
									}
								} else if (splitted.length == 1) {
									if ((splitted[0].isEmpty() || (((Double) value) >= Double.valueOf(splitted[0])))) {
										entities.add(assocRef.getEntityNodeRef());
									}
								}
							}
						}
					} else {
						entities.add(assocRef.getEntityNodeRef());
					}

				}

				nodes.retainAll(entities);
			}
		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getSearchNodesByListCriteria " + criteriaAssoc.toPrefixString(namespaceService) + " executed in  "
					+ watch.getTotalTimeSeconds() + " seconds");
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

			List<NodeRef> toFilterByNodes = extractNodeRefs(propValue,false);

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
