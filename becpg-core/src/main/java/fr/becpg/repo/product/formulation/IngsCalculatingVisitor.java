/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.CompositeIng;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * The Class IngsCalculatingVisitor.
 *
 * @author querephi
 */
public class IngsCalculatingVisitor implements ProductVisitor{
		
	/** The Constant DEFAULT_DENSITY. */
	public static final Double DEFAULT_DENSITY = 1d;
	
	/** The Constant NO_GRP. */
	public static final String  NO_GRP = "-";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(IngsCalculatingVisitor.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The ml node service. */
	private NodeService mlNodeService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	private EntityListDAO entityListDAO;
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO){
		this.productDAO = productDAO;
	}
	
	/**
	 * Sets the ml node service.
	 *
	 * @param mlNodeService the new ml node service
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{
		logger.debug("Calculate ingredient list");
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE)){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
		}
		
		// Load product specification
    	ProductData productSpecicationData = null;
    	List<AssociationRef> productSpecificationAssocRefs = nodeService.getTargetAssocs(formulatedProduct.getNodeRef(), BeCPGModel.ASSOC_PRODUCT_SPECIFICATION);
    	if(productSpecificationAssocRefs != null && productSpecificationAssocRefs.size() > 0 && 
    			productSpecificationAssocRefs.get(0).getTargetRef() != null){
    		
    		Collection<QName> dataLists = new ArrayList<QName>();				
    		dataLists.add(BeCPGModel.TYPE_FORBIDDENINGLIST);
        	productSpecicationData = productDAO.find(productSpecificationAssocRefs.get(0).getTargetRef(), dataLists); 
    	}
		
		//IngList
		calculateIL(formulatedProduct, productSpecicationData);
		
		//IngLabelling
		logger.debug("Calculate Ingredient Labeling");
		List<CompositeIng> compositeIngs = calculateILL(formulatedProduct);
		Collections.sort(compositeIngs);
		List<IngLabelingListDataItem> ingLabelingList = new ArrayList<IngLabelingListDataItem>(compositeIngs.size());
		
		for(CompositeIng compositeIng : compositeIngs){
						
			MLText mlTextILL = new MLText();			
			mlTextILL.addValue(Locale.getDefault(), compositeIng.getIngLabeling(Locale.getDefault()));

			for(Locale locale : compositeIng.getLocales()){
				mlTextILL.addValue(locale, compositeIng.getIngLabeling(locale));
			}
			ingLabelingList.add(new IngLabelingListDataItem(null, compositeIng.getIng(), mlTextILL, Boolean.FALSE));
		}
		
		// manual listItem
		ingLabelingList = getILLToUpdate(formulatedProduct.getNodeRef(), ingLabelingList);
		
		formulatedProduct.setIngLabelingList(ingLabelingList);
		
		return formulatedProduct;
	}
	
	/**
	 * Calculate the ingredient list of a product.
	 *
	 * @param productData the product data
	 * @return the list
	 * @throws FormulateException 
	 */
	private void calculateIL(ProductData formulatedProduct, ProductData productSpecicationData) throws FormulateException{
	
		List<CompoListDataItem> compoList = formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE);
		Map<NodeRef, IngListDataItem> ingMap = new HashMap<NodeRef, IngListDataItem>();
		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<NodeRef, ReqCtrlListDataItem>();
		Map<NodeRef, Double> totalQtyIngMap = new HashMap<NodeRef, Double>();
		
		if(compoList != null){
			for(CompoListDataItem compoItem : compoList){
				visitILOfPart(productSpecicationData, compoItem, ingMap, totalQtyIngMap, reqCtrlMap);
			}
		}		
				
		Double totalQty = 0d;
		for(Double totalQtyIng : totalQtyIngMap.values())
			totalQty += totalQtyIng;
				
		if(totalQty != 0){
			for(IngListDataItem ingListDataItem : ingMap.values()){
				Double totalQtyIng = totalQtyIngMap.get(ingListDataItem.getIng());
				ingListDataItem.setQtyPerc(100 * totalQtyIng / totalQty);				
			}
		}
		
		//check formulated product
		checkILOfFormulatedProduct(ingMap.values(), productSpecicationData, reqCtrlMap);
		
		// manual listItem
		List<IngListDataItem> ingList = getILToUpdate(formulatedProduct.getNodeRef(), ingMap);
		
		//sort collection					
		Collections.sort(ingList);		
		formulatedProduct.setIngList(ingList);
				
		formulatedProduct.getReqCtrlList().addAll(reqCtrlMap.values());
	}
	
	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param compoListDataItem the compo list data item
	 * @param ingMap the ing map
	 * @param totalQtyIngMap the total qty ing map
	 * @throws FormulateException 
	 */
	private void visitILOfPart(ProductData productSpecicationData, CompoListDataItem compoListDataItem, Map<NodeRef, IngListDataItem> ingMap, Map<NodeRef, Double> totalQtyIngMap, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) throws FormulateException{				
			
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_INGLIST);
		ProductData productData = productDAO.find(compoListDataItem.getProduct(), dataLists);		
		
		if(productData.getIngList() == null){
			return;
		}
		
		// calculate ingList of formulated product
		calculateILOfPart(productData, compoListDataItem, ingMap, totalQtyIngMap);
		
		// check product respect specification
		checkILOfPart(productData, productSpecicationData, reqCtrlMap);
	}
	
	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param compoListDataItem the compo list data item
	 * @param ingMap the ing map
	 * @param totalQtyIngMap the total qty ing map
	 * @throws FormulateException 
	 */
	private void calculateILOfPart(ProductData productData, CompoListDataItem compoListDataItem, Map<NodeRef, IngListDataItem> ingMap, Map<NodeRef, Double> totalQtyIngMap) throws FormulateException{
		
		//OMIT is not taken in account
		if(compoListDataItem.getDeclType() == DeclarationType.Omit){
			return;
		}
		
		for(IngListDataItem ingListDataItem : productData.getIngList()){						
					
			//Look for ing
			NodeRef ingNodeRef = ingListDataItem.getIng();
			IngListDataItem newIngListDataItem = ingMap.get(ingNodeRef);
			Double totalQtyIng = totalQtyIngMap.get(ingNodeRef);
			
			logger.trace("productData: " + productData.getName() + " - ing: " + nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME));
			
			if(newIngListDataItem == null){
				
				newIngListDataItem =new IngListDataItem();
				newIngListDataItem.setIng(ingNodeRef);				
				ingMap.put(ingNodeRef, newIngListDataItem);
				
				totalQtyIng = 0d;			
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);
			}															
			
			//Calculate qty
			Double qty = FormulationHelper.getQty(compoListDataItem, nodeService);
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
			if(ingListDataItem.isGMO() && !newIngListDataItem.isGMO()){
				newIngListDataItem.setIsGMO(true);
			}
			
			//Ionized
			if(ingListDataItem.isIonized() && !newIngListDataItem.isIonized()){
				newIngListDataItem.setIonized(true);
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
	private void checkILOfPart(ProductData productData, ProductData productSpecicationData, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap){
		
		if(productSpecicationData != null && productSpecicationData.getForbiddenIngList() != null){
		
			for(IngListDataItem ingListDataItem : productData.getIngList()){										
				
				for(ForbiddenIngListDataItem fil : productSpecicationData.getForbiddenIngList()){					
					
					// GMO
					if(fil.isGMO() != null && !fil.isGMO().equals(ingListDataItem.isGMO())){
						continue; // check next rule
					}
					
					// Ionized
					if(fil.isIonized() != null && !fil.isIonized().equals(ingListDataItem.isIonized())){
						continue; // check next rule
					}
					
					// Ings
					if(fil.getIngs().size() > 0){
						if(!fil.getIngs().contains(ingListDataItem.getIng())){
							continue; // check next rule																			
						}
						else if(fil.getQtyPercMaxi() != null){
							continue; // check next rule (we will check in checkILOfFormulatedProduct)
						}
					}
					
					// GeoOrigins
					if(fil.getGeoOrigins().size() > 0){
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
					if(fil.getBioOrigins().size() > 0){
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
					
					// req not respected
					ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
					if(reqCtrl == null){
						reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), new ArrayList<NodeRef>());
						reqCtrlMap.put(fil.getNodeRef(), reqCtrl);						
					}
					
					if(!reqCtrl.getSources().contains(productData.getNodeRef())){
						reqCtrl.getSources().add(productData.getNodeRef());
					}					
				}
			}
		}		
	}
	
	/**
	 * check the ingredients of the part according to the specification
	 *
	 */
	private void checkILOfFormulatedProduct(Collection<IngListDataItem> ingList, ProductData productSpecicationData, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap){
		
		if(productSpecicationData != null && productSpecicationData.getForbiddenIngList() != null){
		
			for(IngListDataItem ingListDataItem : ingList){										
				
				for(ForbiddenIngListDataItem fil : productSpecicationData.getForbiddenIngList()){										
					
					// Ings
					if(fil.getIngs().size() > 0){
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
		
		List<CompoListDataItem> compoList = productData.getCompoList(EffectiveFilters.EFFECTIVE);
		List<CompositeIng> compositeIngList = new ArrayList<CompositeIng>();
				
		CompositeIng defaultCompositeIng = new CompositeIng(null, null);		
		
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
						
					logger.trace(String.format("calculateILL - DETAIL : parentIndex: %d - lastChild: %d", parentIndex, lastChild));
									
					//localSemiFinished
					if(parentIndex != lastChild){
						CompositeIng detailedIng = calculateILLOfCompositeIng(compoList, parentIndex, lastChild);
						defaultCompositeIng.add(detailedIng, true);
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
					
					logger.trace(String.format("calculateILL - DECLARE with grp : parentIndex: %d - lastChild: %d", parentIndex, lastChild));
					CompositeIng grpIng = calculateILLOfCompositeIng(compoList, parentIndex, lastChild);
					compositeIngList.add(grpIng);
					index = lastChild;
				}
				else if(declarationType == DeclarationType.DoNotDeclare){
					defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, compoListDataItem);
				}
				else if(declarationType == DeclarationType.Declare){
					logger.trace("calculateILL - DECLARE : defaultCompositeIng: " +  defaultCompositeIng.getIng() + " - current product: " + nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
					defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, compoListDataItem);
				}		
			}
		}		
		
		//add no grp if there is one ingredient
		if(defaultCompositeIng.getIngList().size() > 0)
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
		CompositeIng compositeIng = new CompositeIng(grpNodeRef, (MLText)mlNodeService.getProperty(grpNodeRef, BeCPGModel.PROP_PRODUCT_LEGALNAME));			
		
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
						
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_INGLIST);			
		ProductData part = productDAO.find(compoListDataItem.getProduct(), dataLists);
		DeclarationType declarationType = compoListDataItem.getDeclType();
		boolean isDeclared = (declarationType == DeclarationType.DoNotDeclare) ? false:true;
		CompositeIng compositeIng = parentIng;
		
		//OMIT, DETAIL
		if(declarationType == DeclarationType.Omit){
			return parentIng;//nothing to do...
		}
		else if(declarationType == DeclarationType.Detail){
			
			MLText mlText =  (MLText)mlNodeService.getProperty(part.getNodeRef(), BeCPGModel.PROP_LEGAL_NAME);
			compositeIng = new CompositeIng(part.getNodeRef(), mlText);
			parentIng.add(compositeIng, isDeclared);			
		}
		
		if(part.getIngList() != null){
		
			for(IngListDataItem ingListDataItem : part.getIngList()){						
				
				//Look for ing
				NodeRef ingNodeRef = ingListDataItem.getIng();			
							
				IngItem ingItem = (compositeIng.get(ingNodeRef, isDeclared)  instanceof IngItem) ? (IngItem)compositeIng.get(ingNodeRef, isDeclared) : null;						
				
				if(ingItem == null){
					
					MLText mlName = (MLText)mlNodeService.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
					ingItem =new IngItem(ingNodeRef, mlName, 0d);
					compositeIng.add(ingItem, isDeclared);
				}															
				
				//Calculate qty
				Double qty = FormulationHelper.getQty(compoListDataItem, nodeService);
				Double qtyIng = ingListDataItem.getQtyPerc();
							
				if(qty != null && qtyIng != null){
					
					Double totalQtyIng = ingItem.getQty();
					
					Double valueToAdd = qty * qtyIng;
					totalQtyIng += valueToAdd;
					ingItem.setQty(totalQtyIng);								
				}
			}
		}		
		
		logger.trace("return parentIng: " + parentIng.getIngLabeling(Locale.FRENCH));
		return parentIng;
	}
	
	/**
	 * Calculate ill to update
	 * @param productNodeRef
	 * @param costMap
	 * @return
	 */
	private List<IngLabelingListDataItem> getILLToUpdate(NodeRef productNodeRef, List<IngLabelingListDataItem> illList){
				
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		
		if(listContainerNodeRef != null){
			
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
			
			if(listNodeRef != null){
				
				List<NodeRef> manualLinks = entityListDAO.getManualListItems(listNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
				
				for(NodeRef manualLink : manualLinks){
					
					IngLabelingListDataItem illListDataItem = productDAO.loadIngLabelingListItem(manualLink);		    		
					illList.add(illListDataItem);
				}
			}
		}
		
		return illList;
	}
	
	/**
	 * Calculate ingList to update
	 * @param productNodeRef
	 * @param costMap
	 * @return
	 */
	private List<IngListDataItem> getILToUpdate(NodeRef productNodeRef, Map<NodeRef, IngListDataItem> ingMap){
		
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		
		if(listContainerNodeRef != null){
			
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);
			
			if(listNodeRef != null){
				
				List<NodeRef> manualLinks = entityListDAO.getManualListItems(listNodeRef, BeCPGModel.TYPE_INGLIST);
				
				if(!manualLinks.isEmpty()){
					ingMap.clear();
				}
				
				for(NodeRef manualLink : manualLinks){
					
					IngListDataItem ingListDataItem = productDAO.loadIngListItem(manualLink);		    		
					ingMap.put(ingListDataItem.getIng(), ingListDataItem);
				}
			}
		}
		
		return new ArrayList<IngListDataItem>(ingMap.values());
	}
}
