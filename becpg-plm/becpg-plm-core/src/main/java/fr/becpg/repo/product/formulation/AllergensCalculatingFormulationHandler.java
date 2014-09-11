/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.AllergenType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * The Class AllergensCalculatingVisitor.
 * 
 * @author querephi
 */
public class AllergensCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);

	protected AlfrescoRepository<AllergenListDataItem> alfrescoRepository;

	protected NodeService nodeService;

	public void setAlfrescoRepository(AlfrescoRepository<AllergenListDataItem> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Start AllergensCalculatingVisitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE) && !formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		Set<NodeRef> visitedProducts = new HashSet<NodeRef>();
		List<AllergenListDataItem> retainNodes = new ArrayList<AllergenListDataItem>();

		if (formulatedProduct.getAllergenList() != null) {
			for (AllergenListDataItem a : formulatedProduct.getAllergenList()) {
				if (a.getIsManual() != null && a.getIsManual()) {
					// manuel
					retainNodes.add(a);
				} else {
					// reset
					a.setQtyPerc(null);
					a.setVoluntary(false);
					a.setInVoluntary(false);
					a.getVoluntarySources().clear();
					a.getInVoluntarySources().clear();
				}
			}
		} else {
			formulatedProduct.setAllergenList(new LinkedList<AllergenListDataItem>());
		}

		boolean isGenericRawMaterial = formulatedProduct instanceof RawMaterialData;
		
		Map<String, ReqCtrlListDataItem> errors = new HashMap<String, ReqCtrlListDataItem>();
		Map<String, ReqCtrlListDataItem> rclCtrlMap = new HashMap<String, ReqCtrlListDataItem>();
		// compoList
		Double totalQtyUsed = 0d;
		
		for (CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE)) {
			
			NodeRef part = compoItem.getProduct();
			Double qtyUsed = null;
			Double qty = FormulationHelper.getQtyInKg(compoItem);
			if (qty != null) {
				qtyUsed = qty * FormulationHelper.getYield(compoItem) / 100;
				totalQtyUsed += qtyUsed;
			}
			
			if (!visitedProducts.contains(part)) {
				List<ReqCtrlListDataItem> ret = visitPart(compoItem, part, formulatedProduct.getAllergenList(), retainNodes,qtyUsed, isGenericRawMaterial, errors);
				for(ReqCtrlListDataItem error : ret){
					if(!rclCtrlMap.containsKey(error.getReqMessage())){
						rclCtrlMap.put(error.getReqMessage(), error);
					}
				}
				
				visitedProducts.add(part);
			}
		}

		//Set qty in perc and reset
		for(AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()){
			if (allergenListDataItem.getIsManual() == null || !allergenListDataItem.getIsManual()) {
				if(allergenListDataItem.getQtyPerc()!=null && totalQtyUsed!=null && totalQtyUsed>0){
					allergenListDataItem.setQtyPerc((allergenListDataItem.getQtyPerc()/totalQtyUsed)*100);	
					
					Double regulatoryThreshold = (Double)nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD);
					if(regulatoryThreshold!=null && allergenListDataItem.getQtyPerc()!=null
							&& regulatoryThreshold >= allergenListDataItem.getQtyPerc() ){
						allergenListDataItem.setInVoluntary(false);
						allergenListDataItem.setVoluntary(false);
					}
					
				}
			}
		}
		
		// process
		if (formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			for (ProcessListDataItem processItem : formulatedProduct.getProcessList(EffectiveFilters.EFFECTIVE)) {

				NodeRef resource = processItem.getResource();
				if (resource != null && !visitedProducts.contains(resource)) {
					// TODO : resource is not a product => il faudrait déplacer
					// les méthodes loadAllergenList ailleurs que dans
					visitPart(processItem, resource, formulatedProduct.getAllergenList(), retainNodes, null, null, null);
					visitedProducts.add(resource);
				}
			}
		}

		formulatedProduct.getAllergenList().retainAll(retainNodes);
		formulatedProduct.getCompoListView().getReqCtrlList().addAll(rclCtrlMap.values());
		// sort
		sort(formulatedProduct.getAllergenList());

		return true;
	}

	/**
	 * Visit part.
	 * 
	 * @param part
	 *            the part
	 * @param qtyUsed 
	 * @param isRawMaterial 
	 * @param totalQtyPercMap 
	 * @param allergenMap
	 *            the allergen map
	 */
	private List<ReqCtrlListDataItem> visitPart(VariantDataItem variantDataItem, NodeRef part, List<AllergenListDataItem> allergenList,
			List<AllergenListDataItem> retainNodes, Double qtyUsed, Boolean isRawMaterial, Map<String,ReqCtrlListDataItem> errors) {

		List<ReqCtrlListDataItem> ret = new ArrayList<ReqCtrlListDataItem>();
		
		List<AllergenListDataItem> allergenListDataItems = alfrescoRepository.loadDataList(part, PLMModel.TYPE_ALLERGENLIST,
				PLMModel.TYPE_ALLERGENLIST);

		
		for (AllergenListDataItem allergenListDataItem : allergenListDataItems) {
			// Look for allergen
			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
			if (allergenNodeRef != null) {

				
				AllergenListDataItem newAllergenListDataItem = findAllergenListDataItem(allergenList, allergenNodeRef);

				if (newAllergenListDataItem == null) {
					newAllergenListDataItem = new AllergenListDataItem();
					newAllergenListDataItem.setAllergen(allergenNodeRef);
					allergenList.add(newAllergenListDataItem);
				}

				if (!retainNodes.contains(newAllergenListDataItem)) {
					// Reset existing variants
					newAllergenListDataItem.setVariants(null);
					retainNodes.add(newAllergenListDataItem);
				}

				if (newAllergenListDataItem.getIsManual() == null || !newAllergenListDataItem.getIsManual()) {
					// Define voluntary presence
					if (allergenListDataItem.getVoluntary()) {
						newAllergenListDataItem.setVoluntary(true);
					}

					// Define involuntary
					if (allergenListDataItem.getInVoluntary()) {
						newAllergenListDataItem.setInVoluntary(true);
					}

					// Define voluntary, add it when : not present and vol
					if (allergenListDataItem.getVoluntary()) {
						// is it raw material ?
						if (allergenListDataItem.getVoluntarySources().isEmpty()) {
							if (!newAllergenListDataItem.getVoluntarySources().contains(part)) {
								newAllergenListDataItem.getVoluntarySources().add(part);
							}
						} else {
							for (NodeRef p : allergenListDataItem.getVoluntarySources()) {
								if (!newAllergenListDataItem.getVoluntarySources().contains(p)) {
									newAllergenListDataItem.getVoluntarySources().add(p);
								}
							}
						}
					}

					// Define invol, add it when : not present and inVol
					if (allergenListDataItem.getInVoluntary()) {
						// is it raw material ?
						if (allergenListDataItem.getInVoluntarySources().isEmpty()) {
							if (!newAllergenListDataItem.getInVoluntarySources().contains(part)) {
								newAllergenListDataItem.getInVoluntarySources().add(part);
							}
						} else {
							for (NodeRef p : allergenListDataItem.getInVoluntarySources()) {
								if (!newAllergenListDataItem.getInVoluntarySources().contains(p)) {
									newAllergenListDataItem.getInVoluntarySources().add(p);
								}
							}
						}
					}
					
					//Add qty
					if(allergenListDataItem.getVoluntary() || allergenListDataItem.getInVoluntary()){
						if(isRawMaterial != null ){
							if(isRawMaterial){
								if(allergenListDataItem.getQtyPerc()!=null ){
									if(newAllergenListDataItem.getQtyPerc()==null
											|| newAllergenListDataItem.getQtyPerc()< allergenListDataItem.getQtyPerc()){
										newAllergenListDataItem.setQtyPerc(allergenListDataItem.getQtyPerc());
									}
								}
							} else {
								String message = I18NUtil.getMessage("message.formulate.allergen.error.nullQtyPerc", nodeService.getProperty(allergenNodeRef, ContentModel.PROP_NAME));
								ReqCtrlListDataItem error = errors.get(message);
								
								if(allergenListDataItem.getQtyPerc()!=null && qtyUsed!=null 
										&& (newAllergenListDataItem.getQtyPerc()!=null || error == null) ){
									if(newAllergenListDataItem.getQtyPerc() == null){
										newAllergenListDataItem.setQtyPerc(0d);
									}
									
									if(logger.isDebugEnabled()){
										logger.debug("Add "+ nodeService.getProperty(allergenNodeRef, ContentModel.PROP_NAME) +" - "
												+ allergenListDataItem.getQtyPerc()+" * "+qtyUsed
												+" to "+newAllergenListDataItem.getQtyPerc());
									}
									
									newAllergenListDataItem.setQtyPerc(
											newAllergenListDataItem.getQtyPerc()
											+ allergenListDataItem.getQtyPerc() * qtyUsed /100);
									
								} else {
									
										boolean isFirst = true;
										if(error !=null){
											if(!error.getSources().contains(part)){
												error.getSources().add(part);
											}	
										} else {
											isFirst = false;
												List<NodeRef> sourceNodeRefs =  new ArrayList<NodeRef>();
													sourceNodeRefs.add(part);
												
												error =  new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, sourceNodeRefs);
												errors.put(message, error);
										}
										
										if(newAllergenListDataItem.getQtyPerc()!=null || (!isFirst &&
												allergenListDataItem.getQtyPerc()!=null && qtyUsed!=null )){
											if (logger.isDebugEnabled()) {
												logger.debug("Adding allergen error " + error.toString());
											}
											ret.add(error);
										}
								
								
									newAllergenListDataItem.setQtyPerc(null);
								}
							}
						}
					}

					// Add variants if it adds an allergen
					if (variantDataItem.getVariants() != null && (allergenListDataItem.getVoluntary() || allergenListDataItem.getInVoluntary())) {
						if (newAllergenListDataItem.getVariants() == null) {
							newAllergenListDataItem.setVariants(new ArrayList<NodeRef>());
						}

						for (NodeRef variant : variantDataItem.getVariants()) {
							if (!newAllergenListDataItem.getVariants().contains(variant)) {
								newAllergenListDataItem.getVariants().add(variant);
							}
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	private AllergenListDataItem findAllergenListDataItem(List<AllergenListDataItem> allergenList, NodeRef allergenNodeRef) {
		if (allergenNodeRef != null && allergenList != null) {
			for (AllergenListDataItem a : allergenList) {
				if (allergenNodeRef.equals(a.getAllergen())) {
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Sort allergens by type and name.
	 *
	 * @param costList
	 *            the cost list
	 * @return the list
	 */
	protected void sort(List<AllergenListDataItem> allergenList) {

		Collections.sort(allergenList, new Comparator<AllergenListDataItem>() {

			final int BEFORE = -1;
			final int EQUAL = 0;
			final int AFTER = 1;

			@Override
			public int compare(AllergenListDataItem a1, AllergenListDataItem a2) {

				int comp = EQUAL;
				String type1 = (String) nodeService.getProperty(a1.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);
				String type2 = (String) nodeService.getProperty(a2.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);

				if (type1 == null) {
					logger.warn("AllergenType is null for " + a1.getAllergen());
				} else if (type2 == null) {
					logger.warn("AllergenType is null for " + a2.getAllergen());
				} else {

					comp = type1.compareTo(type2);

					if (EQUAL == comp) {

						String allergenName1 = (String) nodeService.getProperty(a1.getAllergen(), ContentModel.PROP_NAME);
						String allergenName2 = (String) nodeService.getProperty(a2.getAllergen(), ContentModel.PROP_NAME);

						comp = allergenName1.compareTo(allergenName2);
					} else {

						if (AllergenType.Major.toString().equals(type1)) {
							comp = BEFORE;
						} else {
							comp = AFTER;
						}
					}
				}

				return comp;
			}

		});

		int i = 1;
		for (AllergenListDataItem al : allergenList) {
			al.setSort(i);
			i++;
		}
	}
}
