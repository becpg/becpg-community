/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.CompositeIng;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class IngsCalculatingVisitor.
 *
 * @author querephi
 */
@Service
public class IngsCalculatingFormulationHandler extends FormulationBaseHandler<ProductData>{
	
	/** The Constant NO_GRP. */
	public static final String  NO_GRP = "-";
	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";
	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);
	
	private NodeService nodeService;
	
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private NodeService mlNodeService;
	
	private AssociationService associationService;
	
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}
	
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Calculate ingredient list");
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compo => no formulation");
			return true;
		}
		
		if(formulatedProduct.getIngList()!=null){
			for(IngListDataItem il : formulatedProduct.getIngList()){
				if(il.getIsManual() == null || !il.getIsManual()){
					//reset
					il.setQtyPerc(null);
					il.setIsGMO(false);
					il.setIsIonized(false);
					il.getGeoOrigin().clear();
					il.getBioOrigin().clear();					
				}
			}
		}
		
		// Load product specification
		List<ProductData> productSpecicationDataList = new ArrayList<ProductData>();    	
    	List<NodeRef> productSpecificationAssocRefs = associationService.getTargetAssocs(formulatedProduct.getNodeRef(), BeCPGModel.ASSOC_PRODUCT_SPECIFICATIONS);
    	if(productSpecificationAssocRefs != null && !productSpecificationAssocRefs.isEmpty()){    		
    		for(NodeRef productSpecificationAssocRef : productSpecificationAssocRefs){
    			productSpecicationDataList.add((ProductData) alfrescoRepository.findOne(productSpecificationAssocRef));            	
    		}    		
    	}
		
    	//IngList
		calculateIL(formulatedProduct, productSpecicationDataList);
    	
		//IngLabeling
		logger.debug("Calculate Ingredient Labeling");
		List<IngLabelingListDataItem> retainNodes = new ArrayList<IngLabelingListDataItem>();
		List<CompositeIng> compositeIngs = calculateILL(formulatedProduct);
		Collections.sort(compositeIngs);
		
		for(CompositeIng compositeIng : compositeIngs){
						
			MLText mlTextILL = new MLText();			
			mlTextILL.addValue(Locale.getDefault(), compositeIng.getIngLabeling(Locale.getDefault()));

			for(Locale locale : compositeIng.getLocales()){
				mlTextILL.addValue(locale, compositeIng.getIngLabeling(locale));
			}
			IngLabelingListDataItem ill = findIngLabelingListDataItem(formulatedProduct.getIngLabelingList(), compositeIng.getIng());
			if(ill == null){
				ill = new IngLabelingListDataItem(null, compositeIng.getIng(), mlTextILL, Boolean.FALSE);
				formulatedProduct.getIngLabelingList().add(ill);
			}
			else if(ill.getIsManual()==null || !ill.getIsManual()){
				ill.setValue(mlTextILL);
			}			
			retainNodes.add(ill);
		}
		
		formulatedProduct.getIngLabelingList().retainAll(retainNodes);
		
		return true;
	}
	
	/**
	 * Calculate the ingredient list of a product.
	 *
	 * @param productData the product data
	 * @return the list
	 * @throws FormulateException 
	 */
	private void calculateIL(ProductData formulatedProduct, List<ProductData> productSpecicationDataList) throws FormulateException{
	
		List<CompoListDataItem> compoList = formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT);
		
		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<NodeRef, ReqCtrlListDataItem>();
		Map<NodeRef, Double> totalQtyIngMap = new HashMap<NodeRef, Double>();
		List<IngListDataItem> retainNodes = new ArrayList<IngListDataItem>();
		
		//manuel
		for(IngListDataItem i : formulatedProduct.getIngList()){
			if(i.getIsManual()!= null && i.getIsManual()){
				retainNodes.add(i);
			}
		}
		
		if(compoList != null){
			for(CompoListDataItem compoItem : compoList){
				visitILOfPart(productSpecicationDataList, compoItem, formulatedProduct.getIngList(), retainNodes, totalQtyIngMap, reqCtrlMap);
			}
		}		
		
		formulatedProduct.getIngList().retainAll(retainNodes);
				
		Double totalQty = 0d;
		for(Double totalQtyIng : totalQtyIngMap.values())
			totalQty += totalQtyIng;
				
		if(totalQty != 0){
			for(IngListDataItem ingListDataItem : formulatedProduct.getIngList()){
				// qtyPerc
				Double totalQtyIng = totalQtyIngMap.get(ingListDataItem.getIng());
				ingListDataItem.setQtyPerc(100 * totalQtyIng / totalQty);
				
				// add detailable aspect
				if(!ingListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)){
					ingListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}
		}
		
		//check formulated product
		checkILOfFormulatedProduct(formulatedProduct.getIngList(), productSpecicationDataList, reqCtrlMap);
		
		//sort collection					
		sortIL(formulatedProduct.getIngList());		
		
		formulatedProduct.getCompoListView().getReqCtrlList().addAll(reqCtrlMap.values());
	}
	
	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param compoListDataItem the compo list data item
	 * @param ingMap the ing map
	 * @param totalQtyIngMap the total qty ing map
	 * @throws FormulateException 
	 */
	private void visitILOfPart(List<ProductData> productSpecicationDataList, CompoListDataItem compoListDataItem, List<IngListDataItem> ingList, List<IngListDataItem> retainNodes, Map<NodeRef, Double> totalQtyIngMap, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) throws FormulateException{				
			
		@SuppressWarnings("unchecked")
		List<IngListDataItem> componentIngList = (List)alfrescoRepository.loadDataList(compoListDataItem.getProduct(), BeCPGModel.TYPE_INGLIST, BeCPGModel.TYPE_INGLIST);
		
		// check product respect specification
		checkILOfPart(compoListDataItem.getProduct(), componentIngList, productSpecicationDataList, reqCtrlMap);
		
		if(componentIngList == null){
			if(logger.isDebugEnabled()){
				logger.debug("CompoItem: " + compoListDataItem.getProduct() + " - doesn't have ing ");
			}
			
			return;
		}
		
		// calculate ingList of formulated product
		calculateILOfPart(compoListDataItem, componentIngList, ingList, retainNodes, totalQtyIngMap);		
	}
	
	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param compoListDataItem the compo list data item
	 * @param ingMap the ing map
	 * @param totalQtyIngMap the total qty ing map
	 * @throws FormulateException 
	 */
	private void calculateILOfPart(CompoListDataItem compoListDataItem, List<IngListDataItem> componentIngList, List<IngListDataItem> ingList, List<IngListDataItem> retainNodes, Map<NodeRef, Double> totalQtyIngMap) throws FormulateException{
		
		//OMIT is not taken in account
		if(compoListDataItem.getDeclType() == DeclarationType.Omit){			
			return;
		}
		
		for(IngListDataItem ingListDataItem : componentIngList){						
					
			//Look for ing
			NodeRef ingNodeRef = ingListDataItem.getIng();
			IngListDataItem newIngListDataItem = findIngListDataItem(ingList, ingNodeRef);						
			
			if(newIngListDataItem == null){
				
				newIngListDataItem =new IngListDataItem();
				newIngListDataItem.setIng(ingNodeRef);				
				ingList.add(newIngListDataItem);				
			}	
			
			if(!retainNodes.contains(newIngListDataItem)){
				retainNodes.add(newIngListDataItem);
			}			
			
			Double totalQtyIng = totalQtyIngMap.get(ingNodeRef);
			if(totalQtyIng == null){
				totalQtyIng = 0d;
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);
			}
			
			//Calculate qty
			Double qty = FormulationHelper.getQty(compoListDataItem);
			Double qtyIng = ingListDataItem.getQtyPerc();
						
			if(qty != null && qtyIng != null){
				
				Double valueToAdd = qty * qtyIng;
				totalQtyIng += valueToAdd;
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);				
			}
			
			//Calculate geo origins
			for(NodeRef geoOrigin : ingListDataItem.getGeoOrigin()){
				if(!newIngListDataItem.getGeoOrigin().contains(geoOrigin)){
					newIngListDataItem.getGeoOrigin().add(geoOrigin);
				}
			}
			
			//Calculate bio origins
			for(NodeRef bioOrigin : ingListDataItem.getBioOrigin()){
				if(!newIngListDataItem.getBioOrigin().contains(bioOrigin)){
					newIngListDataItem.getBioOrigin().add(bioOrigin);
				}
			}
			
			//GMO
			if(ingListDataItem.getIsGMO() && !newIngListDataItem.getIsGMO()){
				newIngListDataItem.setIsGMO(true);
			}
			
			//Ionized
			if(ingListDataItem.getIsIonized() && !newIngListDataItem.getIsIonized()){
				newIngListDataItem.setIsIonized(true);
			}
			
			if(logger.isDebugEnabled()){
				logger.debug("productData: " + compoListDataItem.getProduct() + " - ing: " + nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME) + " qtyPerc: " + totalQtyIng);
			}
		}
	}
	
	/**
	 * check the ingredients of the part according to the specification
	 *
	 * @param compoListDataItem the compo list data item
	 * @param ingMap the ing map
	 * @param totalQtyIngMap the total qty ing map
	 */
	private void checkILOfPart(NodeRef productNodeRef, List<IngListDataItem> ingList, List<ProductData> productSpecicationDataList, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap){
		
		
		if(!BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(productNodeRef))){
			
			// datalist ingList is null or empty
			if((!alfrescoRepository.hasDataList(productNodeRef, BeCPGModel.TYPE_INGLIST) || 
				ingList.isEmpty())){
			
				// req not respected
				String message = I18NUtil.getMessage(MESSAGE_MISSING_INGLIST);
				
				ReqCtrlListDataItem reqCtrl = null;
				for(ReqCtrlListDataItem r : reqCtrlMap.values()){
					if(message.equals(r.getReqMessage())){	
						reqCtrl = r;
						break;
					}
				}
				
				if(reqCtrl == null){
					reqCtrl = new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>());
					reqCtrlMap.put(null, reqCtrl);						
				}
				
				if(!reqCtrl.getSources().contains(productNodeRef)){
					reqCtrl.getSources().add(productNodeRef);
				}
			}
			else{
				for(ProductData productSpecificationData : productSpecicationDataList){
					
					for(IngListDataItem ingListDataItem : ingList){	
						if(logger.isDebugEnabled()){
							logger.debug("For " + productNodeRef + " testing ing :"+ nodeService.getProperty(ingListDataItem.getCharactNodeRef(), ContentModel.PROP_NAME));
						}
						for(ForbiddenIngListDataItem fil : productSpecificationData.getForbiddenIngList()){					
							
							// GMO
							if(fil.getIsGMO() != null && !fil.getIsGMO().isEmpty() && !fil.getIsGMO().equals(ingListDataItem.getIsGMO().toString())){
								continue; // check next rule
							}
							
							// Ionized
							if(fil.getIsIonized() != null && !fil.getIsIonized().isEmpty() && !fil.getIsIonized().equals(ingListDataItem.getIsIonized().toString())){
								continue; // check next rule
							}
							
							// Ings
							if(!fil.getIngs().isEmpty()){
								if(!fil.getIngs().contains(ingListDataItem.getIng())){
									continue; // check next rule																			
								}
								else if(fil.getQtyPercMaxi() != null){
									continue; // check next rule (we will check in checkILOfFormulatedProduct)
								}
							}
							
							// GeoOrigins
							if(!fil.getGeoOrigins().isEmpty()){
								boolean hasGeoOrigin = false;
								for(NodeRef n : ingListDataItem.getGeoOrigin()){						
									if(fil.getGeoOrigins().contains(n)){
										hasGeoOrigin = true;
									}
								}
								
								if(!hasGeoOrigin){
									continue; // check next rule
								}
							}
							
							// BioOrigins
							if(!fil.getBioOrigins().isEmpty()){
								boolean hasBioOrigin = false;
								for(NodeRef n : ingListDataItem.getBioOrigin()){						
									if(fil.getBioOrigins().contains(n)){
										hasBioOrigin = true;
									}
								}
								
								if(!hasBioOrigin){
									continue; // check next rule
								}
							}
							
							logger.debug("Adding not respected for :"+ fil.getReqMessage());
							// req not respected
							ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
							if(reqCtrl == null){
								reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), new ArrayList<NodeRef>());
								reqCtrlMap.put(fil.getNodeRef(), reqCtrl);						
							}
							
							if(!reqCtrl.getSources().contains(productNodeRef)){
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
	private void checkILOfFormulatedProduct(Collection<IngListDataItem> ingList, List<ProductData> productSpecicationDataList, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap){
		
		for(ProductData productSpecification : productSpecicationDataList){
		
			for(IngListDataItem ingListDataItem : ingList){										
				
				for(ForbiddenIngListDataItem fil : productSpecification.getForbiddenIngList()){										
					
					// Ings
					if(!fil.getIngs().isEmpty()){
						if(fil.getIngs().contains(ingListDataItem.getIng())){							
							if(fil.getQtyPercMaxi() != null && fil.getQtyPercMaxi() <= ingListDataItem.getQtyPerc()){
							
								// req not respected
								ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
								if(reqCtrl == null){
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
	
	
	
	/**
	 * Calculate the ingredient labeling of a product.
	 *
	 * @param productData the product data
	 * @return the list
	 * @throws FormulateException 
	 */
	private List<CompositeIng> calculateILL(ProductData productData) throws FormulateException{
		
		List<CompoListDataItem> compoList = productData.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT);
		List<CompositeIng> compositeIngList = new ArrayList<CompositeIng>();
				
		CompositeIng defaultCompositeIng = new CompositeIng(null, null, null, null);		
		
		if(compoList != null){
			
			for(int index = 0; index<compoList.size() ; index++){
				
				CompoListDataItem compoListDataItem = compoList.get(index);		
				DeclarationType declarationType = compoListDataItem.getDeclType();
				
				if(declarationType == DeclarationType.Detail){
					int parentIndex = index;
					//PQU 19/03/2011
					//int lastChild = index + 1;
					int lastChild = index;
					while(lastChild + 1 < compoList.size() && compoList.get(parentIndex).getDepthLevel() < compoList.get(lastChild + 1).getDepthLevel())
						lastChild++;
								
					//localSemiFinished
					if(parentIndex != lastChild){
						CompositeIng detailedIng = calculateILLOfCompositeIng(compoList, parentIndex, lastChild);
						defaultCompositeIng.add(detailedIng, true);
						
						//Calculate qtyRMUsed
						addQtyRMUsed(compoListDataItem, defaultCompositeIng, mlNodeService);
					}
					else{
						defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, compoListDataItem);
					}				
					index = lastChild;
				}
				else if(declarationType == DeclarationType.Group){
					int parentIndex = index;
					//int lastChild = index + 1;
					int lastChild = index;
					while(lastChild + 1 < compoList.size() && compoList.get(parentIndex).getDepthLevel() < compoList.get(lastChild + 1).getDepthLevel())
						lastChild++;
					
					CompositeIng grpIng = calculateILLOfCompositeIng(compoList, parentIndex, lastChild);
					compositeIngList.add(grpIng);
					index = lastChild;
				}
				else if(declarationType == DeclarationType.DoNotDeclare){
					defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, compoListDataItem);
				}
				else if(declarationType == DeclarationType.Declare){
					defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, compoListDataItem);
				}		
			}
		}			
		
		//add no grp if there is one ingredient
		if(!defaultCompositeIng.getIngList().isEmpty())
			compositeIngList.add(defaultCompositeIng);
		return compositeIngList;
	}
	
	/**
	 * Calculate the labeling for a composite ingredient (DETAIL, GRP).
	 *
	 * @param compoList the compo list
	 * @param parentIndex the parent index
	 * @param lastChild the last child
	 * @return the composite ing
	 * @throws FormulateException 
	 */
	private CompositeIng calculateILLOfCompositeIng(List<CompoListDataItem> compoList, int parentIndex, int lastChild) throws FormulateException{
		
		CompoListDataItem compoListDataItem =  compoList.get(parentIndex);
		NodeRef grpNodeRef = compoListDataItem.getProduct();	
		MLText mlText = (MLText)mlNodeService.getProperty(grpNodeRef, BeCPGModel.PROP_PRODUCT_LEGALNAME);
		Double qtyUsed = FormulationHelper.getQty(compoListDataItem);
		CompositeIng compositeIng = new CompositeIng(grpNodeRef, mlText, qtyUsed, null);		
		
		if(logger.isDebugEnabled()){
			logger.debug("New compositeIng " + compositeIng.getName(Locale.getDefault()) + " qtyUsed: " + qtyUsed);
		}		
		
		int startIndex = parentIndex;
		//localSemiFinished
		if(parentIndex != lastChild)
			startIndex++;
			
		for(int index=startIndex ; index <=lastChild ; index++){
			compoListDataItem =  compoList.get(index);						
			compositeIng = calculateILLOfCompositeIng(compositeIng, compoListDataItem);
		}			

		return compositeIng;
	}
	
	/**
	 * Add the ingredients labeling of the part in the composite ingredient.
	 *
	 * @param parentIng the parent ing
	 * @param compoListDataItem the compo list data item
	 * @return the composite ing
	 * @throws FormulateException 
	 */
	private CompositeIng calculateILLOfCompositeIng(CompositeIng parentIng, CompoListDataItem compoListDataItem) throws FormulateException{
							
		if(logger.isDebugEnabled()){
			logger.debug("calculateILLOfCompositeIng: " + compoListDataItem.getQty() + 
					" product " + nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
		}
		
		@SuppressWarnings("rawtypes")
		List<IngListDataItem> ingList = (List)alfrescoRepository.loadDataList(compoListDataItem.getProduct(), BeCPGModel.TYPE_INGLIST, BeCPGModel.TYPE_INGLIST);
		DeclarationType declarationType = compoListDataItem.getDeclType();
		boolean isDeclared = (declarationType == DeclarationType.DoNotDeclare) ? false:true;
		CompositeIng compositeIng = parentIng;
		
		//Calculate qtyRMUsed
		Double qty = FormulationHelper.getQty(compoListDataItem);
		addQtyRMUsed(compoListDataItem, compositeIng, mlNodeService);
		
		//OMIT, DETAIL
		if(declarationType == DeclarationType.Omit){
			return parentIng;//nothing to do...
		}
		else if(declarationType == DeclarationType.Detail){
						
			MLText mlText =  (MLText)mlNodeService.getProperty(compoListDataItem.getProduct(), BeCPGModel.PROP_LEGAL_NAME);
			compositeIng = new CompositeIng(compoListDataItem.getProduct(), mlText, qty, null);
			compositeIng.setQtyRMUsed(qty);
			parentIng.add(compositeIng, isDeclared);	
			
			logger.debug("Add detailed ing : " + mlText.getDefaultValue() + " qty: " + qty);
		}		
		
		if(ingList != null){
		
			for(IngListDataItem ingListDataItem : ingList){						
				
				//Look for ing
				NodeRef ingNodeRef = ingListDataItem.getIng();			
							
				IngItem ingItem = (compositeIng.get(ingNodeRef, isDeclared)  instanceof IngItem) ? (IngItem)compositeIng.get(ingNodeRef, isDeclared) : null;						
				
				if(ingItem == null){
					
					MLText mlName = (MLText)mlNodeService.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
					String ingType = (String)nodeService.getProperty(ingNodeRef, BeCPGModel.PROP_ING_TYPE);
					ingItem =new IngItem(ingNodeRef, mlName, 0d, ingType);
					compositeIng.add(ingItem, isDeclared);
				}															
				
				Double qtyPerc = ingListDataItem.getQtyPerc();
				
				if(qtyPerc == null){
					ingItem.setQty(null);
				}
				else{
					// if one ingItem has null perc -> must be null
					if(ingItem.getQty() != null){
						if(qty != null){
							
							Double totalQtyIng = ingItem.getQty();
							
							Double valueToAdd = qty * qtyPerc / 100;
							totalQtyIng += valueToAdd;
							ingItem.setQty(totalQtyIng);				
						}
					}
				}								
			}
		}		
		
		if(logger.isTraceEnabled()){
			logger.trace("return parentIng: " + parentIng.getIngLabeling(Locale.FRENCH));
		}
		
		return parentIng;
	}	
	
	private IngListDataItem findIngListDataItem(List<IngListDataItem> ingList, NodeRef ingNodeRef){
		if(ingNodeRef != null){
			for(IngListDataItem i : ingList){
				if(ingNodeRef.equals(i.getIng())){
					return i;
				}
			}
		}
		return null;		
	}
	
	private IngLabelingListDataItem findIngLabelingListDataItem(List<IngLabelingListDataItem> ill, NodeRef ingNodeRef){
		for(IngLabelingListDataItem i : ill){
			if((ingNodeRef == null && i.getGrp() == null) || (ingNodeRef != null && ingNodeRef.equals(i.getGrp()))){
				return i;
			}
		}
		return null;		
	}
	
	private void addQtyRMUsed(CompoListDataItem compoListDataItem, CompositeIng compositeIng, NodeService nodeService) throws FormulateException{
		
		Double qty = FormulationHelper.getQty(compoListDataItem);
		Double qtyRMUsed = compositeIng.getQtyRMUsed();
		qtyRMUsed += qty;
		compositeIng.setQtyRMUsed(qtyRMUsed);
	}
	
	/**
	 * Sort ingList by qty perc in descending order.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	private void sortIL(List<IngListDataItem> ingList){
			
		Collections.sort(ingList, new Comparator<IngListDataItem>(){
        	
            @Override
			public int compare(IngListDataItem i1, IngListDataItem i2){
            	
            	// increase
            	return i2.getQtyPerc().compareTo(i1.getQtyPerc());            	             
            }

        });  
		
		int i=1;
		for(IngListDataItem il : ingList){
			il.setSort(i);
			i++;
		}
	}
}
