/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.GUID;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.FilReqOperator;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class IngsCalculatingVisitor.
 * 
 * @author querephi
 */
public class IngsCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The Constant NO_GRP. */
	public static final String NO_GRP = "-";
	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";

	private String tmpDiluentKey = "KEY_DILUANT";

	/** The logger. */
	private static Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);

	private NodeService nodeService;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Calculate ingredient list");

		// no compo, nor ingList on formulated product => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT) ||
				(!alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLIST) &&
				 !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLABELINGLIST))) {
			logger.debug("no compo => no formulation");
			return true;
		}

		if (formulatedProduct.getIngList() != null) {
			for (IngListDataItem il : formulatedProduct.getIngList()) {
				if (il.getIsManual() == null || !il.getIsManual()) {
					// reset
					il.setQtyPerc(null);
					il.setMini(null);
					il.setMaxi(null);
					il.setIsGMO(false);
					il.setIsProcessingAid(true);
					il.setIsIonized(false);
					il.getGeoOrigin().clear();
					il.getGeoTransfo().clear();
					il.getBioOrigin().clear();
				}
			}
		} else {
			formulatedProduct.setIngList(new LinkedList<IngListDataItem>());
		}

		if(formulatedProduct.getProductSpecifications()==null){
			formulatedProduct.setProductSpecifications(new LinkedList<ProductSpecificationData>());
		}
		
		// Load product specification

		// IngList
		calculateIL(formulatedProduct);

		return true;
	}

	/**
	 * Calculate the ingredient list of a product.
	 * 
	 * @param productData
	 *            the product data
	 * @return the list
	 * @throws FormulateException
	 */
	private void calculateIL(ProductData formulatedProduct) throws FormulateException {

		@SuppressWarnings("unchecked")
		List<CompoListDataItem> compoList = formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT);

		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<NodeRef, ReqCtrlListDataItem>();
		Map<String, Double> totalQtyIngMap = new HashMap<String, Double>();
		Map<String, Double> totalQtyVolMap = new HashMap<String, Double>();
		NodeRef diluantIngNodeRef = null;

		List<IngListDataItem> retainNodes = new ArrayList<IngListDataItem>();

		// manuel
		for (IngListDataItem i : formulatedProduct.getIngList()) {
			if (i.getIsManual() != null && i.getIsManual()) {
				retainNodes.add(i);
			}
		}

		Double totalQtyUsed = 0d, totalVolumeUsed = 0d;
		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<IngListDataItem> componentIngList = (List) alfrescoRepository.loadDataList(compoItem.getProduct(), PLMModel.TYPE_INGLIST,
						PLMModel.TYPE_INGLIST);

				visitILOfPart(formulatedProduct, compoItem, componentIngList, retainNodes, totalQtyIngMap, totalQtyVolMap, reqCtrlMap);

				QName type = nodeService.getType(compoItem.getProduct());
				if (type != null
						&& (type.isMatch(PLMModel.TYPE_RAWMATERIAL) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || type
								.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) && compoItem.getDeclType() != DeclarationType.Omit) {
					Double qty = FormulationHelper.getQtyInKg(compoItem);
					if (qty != null) {
						totalQtyUsed += qty * FormulationHelper.getYield(compoItem) / 100;
					}

					Double vol = (Double) compoItem.getVolume();
					if (vol != null) {
						totalVolumeUsed += vol / 100;
					}
				}

				if (nodeService.hasAspect(compoItem.getProduct(), PLMModel.ASPECT_DILUENT) && !componentIngList.isEmpty()) {
					diluantIngNodeRef = componentIngList.get(0).getIng();
				}

			}
		}

		formulatedProduct.getIngList().retainAll(retainNodes);

		if (totalQtyUsed != 0) {
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
				// qtyPerc
				Double totalQtyIng = totalQtyIngMap.get(ingListDataItem.getName());

				if (diluantIngNodeRef != null && diluantIngNodeRef.equals(ingListDataItem.getIng()) && totalQtyVolMap.get(tmpDiluentKey) != null) {
					totalQtyIng += totalQtyVolMap.get(tmpDiluentKey);
				}

				if (totalQtyIng != null) {
					if (ingListDataItem.getParent() != null) {
						Double parentTotalQtyIng = totalQtyIngMap.get(ingListDataItem.getParent().getName());
						if (parentTotalQtyIng != null) {
							ingListDataItem.setQtyPerc(totalQtyIng / parentTotalQtyIng * 100);
						} else {
							ingListDataItem.setQtyPerc(null);
						}
					} else {
						ingListDataItem.setQtyPerc(totalQtyIng / totalQtyUsed);
					}
				} else {
					ingListDataItem.setQtyPerc(null);
				}
				

				// qtyVolumePerc
				if (totalQtyVolMap.get(ingListDataItem.getName()) != null) {
					ingListDataItem.setVolumeQtyPerc(totalQtyVolMap.get(ingListDataItem.getName()) / totalVolumeUsed);
				}

				// add detailable aspect
				if (!ingListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					ingListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}

		}

		// check formulated product
		checkILOfFormulatedProduct(formulatedProduct.getIngList(), formulatedProduct.getProductSpecifications(), reqCtrlMap);

		// sort collection
		sortIL(formulatedProduct.getIngList());

		formulatedProduct.getCompoListView().getReqCtrlList().addAll(reqCtrlMap.values());
	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 * 
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 * @param totalQtyVolMap
	 * @throws FormulateException
	 */
	private void visitILOfPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem, List<IngListDataItem> componentIngList,
			List<IngListDataItem> retainNodes, Map<String, Double> totalQtyIngMap, Map<String, Double> totalQtyVolMap,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) throws FormulateException {

		// check product respect specification
		checkILOfPart(compoListDataItem.getProduct(), compoListDataItem.getDeclType(), componentIngList,
				formulatedProduct.getProductSpecifications() , reqCtrlMap);

		if (componentIngList == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("CompoItem: " + compoListDataItem.getProduct() + " - doesn't have ing ");
			}

			return;
		}

		// calculate ingList of formulated product
		calculateILOfPart(compoListDataItem, CompositeHelper.getHierarchicalCompoList(componentIngList), formulatedProduct.getIngList(), retainNodes,
				totalQtyIngMap, totalQtyVolMap, null);
	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 * 
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 * @param totalQtyVolMap
	 * @throws FormulateException
	 */
	private void calculateILOfPart(CompoListDataItem compoListDataItem, Composite<IngListDataItem> compositeIngList, List<IngListDataItem> ingList,
			List<IngListDataItem> retainNodes, Map<String, Double> totalQtyIngMap, Map<String, Double> totalQtyVolMap,
			IngListDataItem parentIngListDataItem) throws FormulateException {

		// OMIT is not taken in account
		if (compoListDataItem.getDeclType() == DeclarationType.Omit) {
			return;
		}

		for (Composite<IngListDataItem> component : compositeIngList.getChildren()) {

			// Look for ing
			IngListDataItem ingListDataItem = component.getData();
			NodeRef ingNodeRef = ingListDataItem.getIng();
			IngListDataItem newIngListDataItem = findIngListDataItem(ingList, ingListDataItem);

			if (newIngListDataItem == null) {

				newIngListDataItem = new IngListDataItem();
				newIngListDataItem.setName(GUID.generate());
				newIngListDataItem.setIng(ingNodeRef);
				newIngListDataItem.setParent(parentIngListDataItem);
				newIngListDataItem.setDepthLevel(parentIngListDataItem == null ? 1 : parentIngListDataItem.getDepthLevel() + 1);
				newIngListDataItem.setIsProcessingAid(true);
				ingList.add(newIngListDataItem);
			}
			
			

			if (!retainNodes.contains(newIngListDataItem)) {
				retainNodes.add(newIngListDataItem);
			}

			Double totalQtyIng = totalQtyIngMap.get(newIngListDataItem.getName());
			if (totalQtyIng == null) {
				totalQtyIng = 0d;
				totalQtyIngMap.put(newIngListDataItem.getName(), totalQtyIng);
			}

			// Calculate qty
			Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
			Double qtyIng = ingListDataItem.getQtyPerc();

			if (qty != null && qtyIng != null) {
				qty *= FormulationHelper.getYield(compoListDataItem) / 100;
				Double valueToAdd = qty * qtyIng;

				Double volumeQty = totalQtyVolMap.get(newIngListDataItem.getName());
				if (volumeQty == null) {
					volumeQty = 0d;
				}

				Double volumeReconstitution = FormulationHelper.getVolumeReconstitution(compoListDataItem, nodeService);

				if (volumeReconstitution != null) {

					Double diluentVolume = (volumeReconstitution - compoListDataItem.getVolume()) * 100;

					// Raw material
					totalQtyVolMap.put(newIngListDataItem.getName(), volumeQty + volumeReconstitution);

					Double qtyDiluent = totalQtyVolMap.get(tmpDiluentKey);
					if (qtyDiluent == null) {
						qtyDiluent = 0d;
					}

					qtyDiluent -= diluentVolume;
					valueToAdd += diluentVolume;

					// Decrease diluent
					totalQtyVolMap.put(tmpDiluentKey, qtyDiluent);

				} else {
					// Semi finished
					if (ingListDataItem.getVolumeQtyPerc() != null && compoListDataItem.getVolume() != null) {

						totalQtyVolMap.put(newIngListDataItem.getName(),
								volumeQty + ingListDataItem.getVolumeQtyPerc() * compoListDataItem.getVolume() / 100);
					}
				}

				totalQtyIng += valueToAdd;
				totalQtyIngMap.put(newIngListDataItem.getName(), totalQtyIng);
			}

			// Calculate geo origins
			for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
				if (!newIngListDataItem.getGeoOrigin().contains(geoOrigin)) {
					newIngListDataItem.getGeoOrigin().add(geoOrigin);
				}
			}
			
			// Calculate geo transfo
			for (NodeRef geoTransfo : ingListDataItem.getGeoTransfo()) {
				if (!newIngListDataItem.getGeoTransfo().contains(geoTransfo)) {
					newIngListDataItem.getGeoTransfo().add(geoTransfo);
				}
			}

			// Calculate bio origins
			for (NodeRef bioOrigin : ingListDataItem.getBioOrigin()) {
				if (!newIngListDataItem.getBioOrigin().contains(bioOrigin)) {
					newIngListDataItem.getBioOrigin().add(bioOrigin);
				}
			}


			//Processing Aid 
			if (ingListDataItem.getIsProcessingAid()==null || !ingListDataItem.getIsProcessingAid()) {
				newIngListDataItem.setIsProcessingAid(false);
			}
			
			// GMO
			if (ingListDataItem.getIsGMO() && !newIngListDataItem.getIsGMO()) {
				newIngListDataItem.setIsGMO(true);
			}

			// Ionized
			if (ingListDataItem.getIsIonized() && !newIngListDataItem.getIsIonized()) {
				newIngListDataItem.setIsIonized(true);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("productData: " + compoListDataItem.getProduct() + " - ing: "
						+ nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME) + " qtyPerc: " + totalQtyIng);
			}

			// recursive
			if (!component.isLeaf()) {
				calculateILOfPart(compoListDataItem, component, ingList, retainNodes, totalQtyIngMap, totalQtyVolMap, newIngListDataItem);
			}
		}
	}

	/**
	 * check the ingredients of the part according to the specification
	 * 
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 */
	private void checkILOfPart(NodeRef productNodeRef, DeclarationType declType, List<IngListDataItem> ingList, List<ProductSpecificationData> productSpecicationDataList,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(productNodeRef))) {

			// datalist ingList is null or empty
			if ((!alfrescoRepository.hasDataList(productNodeRef, PLMModel.TYPE_INGLIST) || ingList.isEmpty())) {
				
				if(declType ==  null || !declType.equals(DeclarationType.DoNotDetails)) {
					// req not respected
					String message = I18NUtil.getMessage(MESSAGE_MISSING_INGLIST);

					ReqCtrlListDataItem reqCtrl = null;
					for (ReqCtrlListDataItem r : reqCtrlMap.values()) {
						if (message.equals(r.getReqMessage())) {
							reqCtrl = r;
							break;
						}
					}

					if (reqCtrl == null) {
						reqCtrl = new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>());
						reqCtrlMap.put(null, reqCtrl);
					}

					if (!reqCtrl.getSources().contains(productNodeRef)) {
						reqCtrl.getSources().add(productNodeRef);
					}
				}				
			} else {
				for (ProductSpecificationData productSpecificationData : productSpecicationDataList) {

					for (IngListDataItem ingListDataItem : ingList) {
						if (logger.isDebugEnabled()) {
							logger.debug("For " + productNodeRef + " testing ing :"
									+ nodeService.getProperty(ingListDataItem.getCharactNodeRef(), ContentModel.PROP_NAME));
						}
						for (ForbiddenIngListDataItem fil : productSpecificationData.getForbiddenIngList()) {

							// GMO
							if (fil.getIsGMO() != null && !fil.getIsGMO().isEmpty() && !fil.getIsGMO().equals(ingListDataItem.getIsGMO().toString())) {
								continue; // check next rule
							}

							// Ionized
							if (fil.getIsIonized() != null && !fil.getIsIonized().isEmpty()
									&& !fil.getIsIonized().equals(ingListDataItem.getIsIonized().toString())) {
								continue; // check next rule
							}

							
							// Ings
							if (!fil.getIngs().isEmpty()) {
								if (!fil.getIngs().contains(ingListDataItem.getIng())) {
									continue; // check next rule
								} else if (fil.getQtyPercMaxi() != null) {
									continue; // check next rule (we will check
												// in
												// checkILOfFormulatedProduct)
								}
							}

							// GeoOrigins
							if (!fil.getGeoOrigins().isEmpty()) {
								boolean hasGeoOrigin = false;
								for (NodeRef n : ingListDataItem.getGeoOrigin()) {
									if (fil.getGeoOrigins().contains(n)) {
										hasGeoOrigin = true;
									}
								}

								if(FilReqOperator.DoNotContains.equals(fil.getOperator())){
									if (hasGeoOrigin) {
										continue; // check next rule
									}
								} else {
									if (!hasGeoOrigin) {
										continue; // check next rule
									}
								}
								
							}
							
							// GeoTransfo
							if (!fil.getGeoTransfo().isEmpty()) {
								boolean hasGeoTransfo = false;
								for (NodeRef n : ingListDataItem.getGeoTransfo()) {
									if (fil.getGeoTransfo().contains(n)) {
										hasGeoTransfo = true;
									}
								}

								if(FilReqOperator.DoNotContains.equals(fil.getOperator())){
									if (hasGeoTransfo) {
										continue; // check next rule
									}
								} else {
									if (!hasGeoTransfo) {
										continue; // check next rule
									}
								}
							}

							// BioOrigins
							if (!fil.getBioOrigins().isEmpty()) {
								boolean hasBioOrigin = false;
								for (NodeRef n : ingListDataItem.getBioOrigin()) {
									if (fil.getBioOrigins().contains(n)) {
										hasBioOrigin = true;
									}
								}

								if(FilReqOperator.DoNotContains.equals(fil.getOperator())){
									if (hasBioOrigin) {
										continue; // check next rule
									}
								} else {
									if (!hasBioOrigin) {
										continue; // check next rule
									}
								}
								
							}

							logger.debug("Adding not respected for :" + fil.getReqMessage());
							// req not respected
							ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
							if (reqCtrl == null) {
								reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), new ArrayList<NodeRef>());
								reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
							}

							if (!reqCtrl.getSources().contains(productNodeRef)) {
								reqCtrl.getSources().add(productNodeRef);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * check the ingredients of the part according to the specification
	 * 
	 */
	private void checkILOfFormulatedProduct(Collection<IngListDataItem> ingList, List<ProductSpecificationData> productSpecicationDataList,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		for (ProductSpecificationData productSpecification : productSpecicationDataList) {

			for (IngListDataItem ingListDataItem : ingList) {

				for (ForbiddenIngListDataItem fil : productSpecification.getForbiddenIngList()) {

					// Ings
					if (!fil.getIngs().isEmpty()) {
						if (fil.getIngs().contains(ingListDataItem.getIng())) {
							if (fil.getQtyPercMaxi() != null && fil.getQtyPercMaxi() <= ingListDataItem.getQtyPerc()) {

								// req not respected
								ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
								if (reqCtrl == null) {
									reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), new ArrayList<NodeRef>());
									reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
								}
							}
						}
					}
				}
			}
		}
	}

	//TODO refactor this method
	@Deprecated
	private IngListDataItem findIngListDataItem(List<IngListDataItem> ingLists, IngListDataItem ingList) {

		if (ingList != null && ingList.getIng() != null) {
			for (IngListDataItem i : ingLists) {
				if (ingList.getIng().equals(i.getIng())) {
					// check parent
					IngListDataItem parentIngListDataItem = ingList.getParent();
					IngListDataItem p = i.getParent();
					int j = 0;
					boolean isFound = true;
					while (parentIngListDataItem != null || p != null) {
						if (j > 256) {
							logger.warn("Cycle detected...");
							isFound = false;
							break;
						}
						if ((parentIngListDataItem != null && p == null) || (parentIngListDataItem == null && p != null)) {
							isFound = false;
							break;
						} else if (parentIngListDataItem != null
								&& p != null
								&& ((parentIngListDataItem.getIng() != null && !parentIngListDataItem.getIng().equals(p.getIng())) || (p.getIng() != null && !p
										.getIng().equals(parentIngListDataItem.getIng())))) {
							isFound = false;
							break;
						}

						parentIngListDataItem = parentIngListDataItem.getParent();
						p = p.getParent();
						j++;
					}
					if (isFound) {
						return i;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Sort ingList by qty perc in descending order.
	 * 
	 * @param costList
	 *            the cost list
	 * @return the list
	 */
	private void sortIL(List<IngListDataItem> ingList) {

		Collections.sort(ingList, new Comparator<IngListDataItem>() {

			@Override
			public int compare(IngListDataItem i1, IngListDataItem i2) {

				// increase
				if ((i1.getParent() == null && i2.getParent() == null) || (i1.getParent() != null && i1.getParent().equals(i2.getParent()))) {
					if(i2.getQtyPerc()!=null){
						return i2.getQtyPerc().compareTo(i1.getQtyPerc());
					} else {
						return i2.getQtyPerc() == i1.getQtyPerc() ? 0 : -1;
					}
				} else {
					IngListDataItem root1 = findRoot(i1);
					IngListDataItem root2 = findRoot(i2);
					if (root1.equals(root2)) {
						return i1.getDepthLevel().compareTo(i2.getDepthLevel());
					} else {
						return root2.getQtyPerc().compareTo(root1.getQtyPerc());
					}
				}
			}

		});

		int i = 1;
		for (IngListDataItem il : ingList) {
			il.setSort(i);
			i++;
		}
	}

	private IngListDataItem findRoot(IngListDataItem ingList) {
		while (ingList.getParent() != null) {
			ingList = ingList.getParent();
		}
		return ingList;
	}
}
