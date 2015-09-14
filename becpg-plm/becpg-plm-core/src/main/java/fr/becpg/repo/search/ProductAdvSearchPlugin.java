package fr.becpg.repo.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;

@Service
public class ProductAdvSearchPlugin implements AdvSearchPlugin {

	private static final Log logger = LogFactory.getLog(AdvSearchPlugin.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityListDAO entityListDAO;

	private static final String CRITERIA_ING = "assoc_bcpg_ingListIng_added";

	private static final String CRITERIA_GEO_ORIGIN = "assoc_bcpg_ingListGeoOrigin_added";

	private static final String CRITERIA_BIO_ORIGIN = "assoc_bcpg_ingListBioOrigin_added";

	private static final String CRITERIA_PACK_LABEL = "assoc_pack_llLabel_added";

	private static final String CRITERIA_PACKAGING_LIST_PRODUCT = "assoc_bcpg_packagingListProduct_added";

	private static final String CRITERIA_LABEL_CLAIM = "assoc_bcpg_lclLabelClaim_added";

	private static final String CRITERIA_PACK_LABEL_POSITION = "prop_pack_llPosition";

	@Override
	public List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria) {

		boolean isAssocSearch = isAssocSearch(criteria);

		if (isAssocSearch) {
			nodes = filterByAssociations(nodes, criteria);

			if (datatype != null && entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT)) {
				nodes = getSearchNodesByIngListCriteria(nodes, criteria);
				nodes = getSearchNodesByLabelingCriteria(nodes, criteria);
				nodes = getSearchNodesByLabelClaim(nodes, criteria);
				nodes = getSearchNodesByPackagingListProduct(nodes, criteria);
			}
		}

		return nodes;
	}

	@Override
	public Set<String> getIgnoredFields(QName datatype) {
		Set<String> ret  = new HashSet<>();
		if (datatype != null && entityDictionaryService.isSubClass(datatype, PLMModel.TYPE_PRODUCT)) {
			ret.add(CRITERIA_PACK_LABEL_POSITION);
		}
		return ret;
	}
	
	/**
	 * Take in account criteria on associations (ie :
	 * assoc_bcpg_supplierAssoc_added)
	 * 
	 * @return filtered list of nodes by associations
	 */
	private List<NodeRef> filterByAssociations(List<NodeRef> nodes, Map<String, String> criteria) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		for (Map.Entry<String, String> criterion : criteria.entrySet()) {

			String key = criterion.getKey();
			String propValue = criterion.getValue();

			// association
			if (key.startsWith("assoc_") && !propValue.isEmpty()) {

				String assocName = key.substring(6);
				if (assocName.endsWith("_added")) {
					// TODO : should be generic
					if (!key.equals(CRITERIA_ING) && !key.equals(CRITERIA_GEO_ORIGIN) && !key.equals(CRITERIA_BIO_ORIGIN)
							&& !key.equals(CRITERIA_PACK_LABEL) && !key.equals(CRITERIA_LABEL_CLAIM) && !key.equals(CRITERIA_PACKAGING_LIST_PRODUCT)) {

						assocName = assocName.substring(0, assocName.length() - 6);
						assocName = assocName.replace("_", ":");
						QName assocQName = QName.createQName(assocName, namespaceService);

						String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
						for (String strNodeRef : arrValues) {

							NodeRef nodeRef = new NodeRef(strNodeRef);

							if (nodeService.exists(nodeRef)) {

								List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, assocQName);

								// remove nodes that don't respect the
								// assoc_ criteria
								List<NodeRef> nodesToKeep = new ArrayList<>();

								for (AssociationRef assocRef : assocRefs) {

									nodesToKeep.add(assocRef.getSourceRef());
								}

								nodes.retainAll(nodesToKeep);
							}

						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("filterByAssociations executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	private List<NodeRef> getSearchNodesByPackagingListProduct(List<NodeRef> nodes, Map<String, String> criteria) {
		List<NodeRef> packagingListItems = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(CRITERIA_PACKAGING_LIST_PRODUCT)) {

			String propValue = criteria.get(CRITERIA_PACKAGING_LIST_PRODUCT);

			// criteria on label
			if (!propValue.isEmpty()) {

				NodeRef nodeRef = new NodeRef(propValue);

				if (nodeService.exists(nodeRef)) {

					List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, PLMModel.ASSOC_PACKAGINGLIST_PRODUCT);
					packagingListItems = new ArrayList<>(assocRefs.size());

					for (AssociationRef assocRef : assocRefs) {

						NodeRef n = assocRef.getSourceRef();
						if (isWorkSpaceProtocol(n)) {

							packagingListItems.add(n);
						}
					}
				}
			}

		}

		if (packagingListItems != null) {

			List<NodeRef> productNodeRefs = new ArrayList<>();
			for (NodeRef packagingListItem : packagingListItems) {

				if (isWorkSpaceProtocol(packagingListItem)) {

					NodeRef rootNodeRef = entityListDAO.getEntity(packagingListItem);

					// we don't display history version
					if (rootNodeRef != null && !nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
						productNodeRefs.add(rootNodeRef);
					}

				}
			}

			if (productNodeRefs != null) {
				nodes.retainAll(productNodeRefs);
			}

		}

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("getSearchNodesByPackagingListProduct executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	/**
	 * Take in account criteria on ing list criteria
	 * 
	 * @return
	 */
	private List<NodeRef> getSearchNodesByIngListCriteria(List<NodeRef> nodes, Map<String, String> criteria) {

		List<NodeRef> ingListItems = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		for (Map.Entry<String, String> criterion : criteria.entrySet()) {

			String key = criterion.getKey();
			String propValue = criterion.getValue();

			// criteria on ing
			if (key.equals(CRITERIA_ING) && !propValue.isEmpty()) {

				NodeRef nodeRef = new NodeRef(propValue);

				if (nodeService.exists(nodeRef)) {

					List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, PLMModel.ASSOC_INGLIST_ING);
					ingListItems = new ArrayList<>(assocRefs.size());

					for (AssociationRef assocRef : assocRefs) {

						NodeRef n = assocRef.getSourceRef();
						if (isWorkSpaceProtocol(n)) {
							ingListItems.add(n);
						}
					}
				}
			}

			// criteria on geo origin, we query as an OR operator
			if (key.equals(CRITERIA_GEO_ORIGIN) && !propValue.isEmpty()) {

				List<NodeRef> ingListGeoOrigins = new ArrayList<>();

				String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
				for (String strNodeRef : arrValues) {

					NodeRef nodeRef = new NodeRef(strNodeRef);

					if (nodeService.exists(nodeRef)) {

						List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, PLMModel.ASSOC_INGLIST_GEO_ORIGIN);

						for (AssociationRef assocRef : assocRefs) {

							NodeRef n = assocRef.getSourceRef();
							if (isWorkSpaceProtocol(n)) {
								ingListGeoOrigins.add(n);
							}
						}
					}
				}

				if (ingListItems == null) {
					ingListItems = ingListGeoOrigins;
				} else {
					ingListItems.retainAll(ingListGeoOrigins);
				}
			}

			// criteria on bio origin, we query as an OR operator
			if (key.equals(CRITERIA_BIO_ORIGIN) && !propValue.isEmpty()) {

				List<NodeRef> ingListBioOrigins = new ArrayList<>();

				String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
				for (String strNodeRef : arrValues) {

					NodeRef nodeRef = new NodeRef(strNodeRef);

					if (nodeService.exists(nodeRef)) {

						List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, PLMModel.ASSOC_INGLIST_BIO_ORIGIN);

						for (AssociationRef assocRef : assocRefs) {

							NodeRef n = assocRef.getSourceRef();
							if (isWorkSpaceProtocol(n)) {
								ingListBioOrigins.add(n);
							}
						}
					}
				}

				if (ingListItems == null) {
					ingListItems = ingListBioOrigins;
				} else {
					ingListItems.retainAll(ingListBioOrigins);
				}
			}
		}

		// determine the product WUsed of the ing list items
		if (ingListItems != null) {

			List<NodeRef> productNodeRefs = new ArrayList<>();
			for (NodeRef ingListItem : ingListItems) {

				if (isWorkSpaceProtocol(ingListItem)) {

					NodeRef rootNodeRef = entityListDAO.getEntity(ingListItem);

					// we don't display history version
					if (rootNodeRef != null && !nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
						productNodeRefs.add(rootNodeRef);
					}
				}
			}

			if (productNodeRefs != null) {
				nodes.retainAll(productNodeRefs);
			}
		}

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("getSearchNodesByIngListCriteria executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	/**
	 * Take in account criteria on ing list criteria
	 * 
	 * @return
	 */
	private List<NodeRef> getSearchNodesByLabelingCriteria(List<NodeRef> nodes, Map<String, String> criteria) {

		List<NodeRef> labelingListItems = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(CRITERIA_PACK_LABEL)) {

			String propValue = criteria.get(CRITERIA_PACK_LABEL);

			// criteria on label
			if (!propValue.isEmpty()) {

				NodeRef nodeRef = new NodeRef(propValue);

				if (nodeService.exists(nodeRef)) {

					List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, PackModel.ASSOC_LL_LABEL);
					labelingListItems = new ArrayList<>(assocRefs.size());

					for (AssociationRef assocRef : assocRefs) {

						NodeRef n = assocRef.getSourceRef();
						if (isWorkSpaceProtocol(n)) {

							if (criteria.containsKey(CRITERIA_PACK_LABEL_POSITION) && !criteria.get(CRITERIA_PACK_LABEL_POSITION).isEmpty()) {

								if (criteria.get(CRITERIA_PACK_LABEL_POSITION).equals(
										"\"" + nodeService.getProperty(n, PackModel.PROP_LL_POSITION) + "\"")) {
									labelingListItems.add(n);
								}
							} else {

								labelingListItems.add(n);
							}
						}
					}
				}
			}

		}

		if (labelingListItems != null) {

			List<NodeRef> productNodeRefs = new ArrayList<>();
			for (NodeRef labelingListItem : labelingListItems) {

				if (isWorkSpaceProtocol(labelingListItem)) {

					NodeRef rootNodeRef = entityListDAO.getEntity(labelingListItem);

					// we don't display history version
					if (rootNodeRef != null && !nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
						productNodeRefs.add(rootNodeRef);
					}

				}
			}

			if (productNodeRefs != null) {
				nodes.retainAll(productNodeRefs);
			}

		}

		if (logger.isDebugEnabled()) {
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
	private List<NodeRef> getSearchNodesByLabelClaim(List<NodeRef> nodes, Map<String, String> criteria) {

		List<NodeRef> labelClaimListItems = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (criteria.containsKey(CRITERIA_LABEL_CLAIM)) {

			String propValue = criteria.get(CRITERIA_LABEL_CLAIM);

			// criteria on label
			if (!propValue.isEmpty()) {

				NodeRef nodeRef = new NodeRef(propValue);

				if (nodeService.exists(nodeRef)) {

					List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, PLMModel.ASSOC_LCL_LABELCLAIM);
					labelClaimListItems = new ArrayList<>(assocRefs.size());

					for (AssociationRef assocRef : assocRefs) {

						NodeRef n = assocRef.getSourceRef();
						if (isWorkSpaceProtocol(n)) {

							Boolean isClaimed = "true".equals(nodeService.getProperty(n, PLMModel.PROP_LCL_CLAIM_VALUE));
							if (isClaimed) {
								labelClaimListItems.add(n);
							}
						}
					}
				}
			}

		}

		// determine the product WUsed of the ing list items
		if (labelClaimListItems != null) {
			List<NodeRef> productNodeRefs = new ArrayList<>();
			for (NodeRef labelClaimListItem : labelClaimListItems) {

				if (isWorkSpaceProtocol(labelClaimListItem)) {

					NodeRef rootNodeRef = entityListDAO.getEntity(labelClaimListItem);

					// we don't display history version
					if (rootNodeRef != null && !nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
						productNodeRefs.add(rootNodeRef);
					}

				}
			}
			if (productNodeRefs != null) {
				nodes.retainAll(productNodeRefs);
			}
		}

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("getSearchNodesByLabelClaim executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	private boolean isWorkSpaceProtocol(NodeRef nodeRef) {

		if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)) {
			return true;
		} else {
			return false;
		}
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
