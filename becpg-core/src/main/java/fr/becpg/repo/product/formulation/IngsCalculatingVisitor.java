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
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.CompositeIng;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem.NullableBoolean;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Class IngsCalculatingVisitor.
 *
 * @author querephi
 */
public class IngsCalculatingVisitor implements ProductVisitor{
		
	/** The Constant DEFAULT_DENSITY. */
	public static final float DEFAULT_DENSITY = 1f;
	
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
	
//	@Override
//	public FinishedProductData visit(FinishedProductData finishedProductData) {
//		//nothing to do
//		return finishedProductData;		
//	}
//
//	@Override
//	public RawMaterialData visit(RawMaterialData rawMaterialData) {
//		return rawMaterialData;
//	}
//
//	@Override
//	public PackagingMaterialData visit(PackagingMaterialData packagingMaterialData) {
//		//nothing to do
//		return packagingMaterialData;
//	}
//
//	@Override
//	public SemiFinishedProductData visit(SemiFinishedProductData semiFinishedProductData) {
//		// TODO Auto-generated method stub
//		return semiFinishedProductData;
//	}
//
//	@Override
//	public LocalSemiFinishedProduct visit(LocalSemiFinishedProduct localSemiFinishedProductData) {
//		// TODO Auto-generated method stub
//		return localSemiFinishedProductData;
//	}
	
/* (non-Javadoc)
 * @see fr.becpg.repo.product.ProductVisitor#visit(fr.becpg.repo.food.ProductData)
 */
@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{
		logger.debug("Calculate ingredient list");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
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
			mlTextILL.addValue(Locale.ENGLISH, compositeIng.getIngLabeling(Locale.ENGLISH));
			mlTextILL.addValue(Locale.FRENCH, compositeIng.getIngLabeling(Locale.FRENCH));
			ingLabelingList.add(new IngLabelingListDataItem(null, compositeIng.getName(), mlTextILL, Boolean.FALSE));
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
	
		List<CompoListDataItem> compoList = formulatedProduct.getCompoList();
		Map<NodeRef, IngListDataItem> ingMap = new HashMap<NodeRef, IngListDataItem>();
		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<NodeRef, ReqCtrlListDataItem>();
		Map<NodeRef, Float> totalQtyIngMap = new HashMap<NodeRef, Float>();
		
		if(compoList != null){
			for(CompoListDataItem compoItem : compoList){
				visitILOfPart(productSpecicationData, compoItem, ingMap, totalQtyIngMap, reqCtrlMap);
			}
		}		
				
		Float totalQty = 0f;
		for(Float totalQtyIng : totalQtyIngMap.values())
			totalQty += totalQtyIng;
				
		if(totalQty != 0){
			for(IngListDataItem ingListDataItem : ingMap.values()){
				Float totalQtyIng = totalQtyIngMap.get(ingListDataItem.getIng());
				ingListDataItem.setQtyPerc(100 * totalQtyIng / totalQty);				
			}
		}
		
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
	private void visitILOfPart(ProductData productSpecicationData, CompoListDataItem compoListDataItem, Map<NodeRef, IngListDataItem> ingMap, Map<NodeRef, Float> totalQtyIngMap, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) throws FormulateException{				
			
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
	private void calculateILOfPart(ProductData productData, CompoListDataItem compoListDataItem, Map<NodeRef, IngListDataItem> ingMap, Map<NodeRef, Float> totalQtyIngMap) throws FormulateException{
		
		//OMIT is not taken in account
		if(DeclarationType.parse(compoListDataItem.getDeclType()) == DeclarationType.OMIT){
			return;
		}
		
		for(IngListDataItem ingListDataItem : productData.getIngList()){						
					
			//Look for ing
			NodeRef ingNodeRef = ingListDataItem.getIng();
			IngListDataItem newIngListDataItem = ingMap.get(ingNodeRef);
			Float totalQtyIng = totalQtyIngMap.get(ingNodeRef);
			
			logger.trace("productData: " + productData.getName() + " - ing: " + nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME));
			
			if(newIngListDataItem == null){
				
				newIngListDataItem =new IngListDataItem();
				newIngListDataItem.setIng(ingNodeRef);				
				ingMap.put(ingNodeRef, newIngListDataItem);
				
				totalQtyIng = 0f;			
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);
			}															
			
			//Calculate qty
			Float qty = FormulationHelper.getQty(compoListDataItem);
			Float density = (productData.getDensity() != null) ? productData.getDensity():DEFAULT_DENSITY; //density is null => 1
			Float qtyIng = ingListDataItem.getQtyPerc();
						
			if(qty != null && qtyIng != null){
				
				logger.trace("totalQtyIng before: " + totalQtyIng);
				
				Float valueToAdd = density * qty * qtyIng;
				totalQtyIng += valueToAdd;
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);
								
				logger.trace("valueToAdd = density: " + density + " - qty: " + qty + " - qtyIng: " + qtyIng);
				logger.trace("ing: " + nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME) + " - value to add: " + valueToAdd + " - totalQtyIng: " + totalQtyIng);
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
					if(fil.isGMO() != null && !fil.isGMO().equals(NullableBoolean.Null)){
						Boolean b = fil.isGMO().equals(NullableBoolean.True) ? Boolean.TRUE : Boolean.FALSE;
						if(!b.equals(ingListDataItem.isGMO())){
							continue; // check next rule
						}
					}
					
					// Ionized
					if(fil.isIonized() != null && !fil.isIonized().equals(NullableBoolean.Null)){
						Boolean b = fil.isIonized().equals(NullableBoolean.True) ? Boolean.TRUE : Boolean.FALSE;
						if(!b.equals(ingListDataItem.isIonized())){
							continue; // check next rule
						}
					}
					
					// Ings
					if(fil.getIngs().size() > 0){
						if(!fil.getIngs().contains(ingListDataItem.getIng())){
							continue; // check next rule
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
	 * Calculate the ingredient labeling of a product.
	 *
	 * @param productData the product data
	 * @return the list
	 * @throws FormulateException 
	 */
	private List<CompositeIng> calculateILL(ProductData productData) throws FormulateException{
		
		List<CompoListDataItem> compoList = productData.getCompoList();
		List<CompositeIng> compositeIngList = new ArrayList<CompositeIng>();
		MLText mlName = new MLText();
		mlName.addValue(Locale.getDefault(), NO_GRP);
		mlName.addValue(Locale.ENGLISH, NO_GRP);
		mlName.addValue(Locale.FRENCH, NO_GRP);
		CompositeIng defaultCompositeIng = new CompositeIng(NO_GRP, mlName);		
		
		if(compoList != null){
			
			for(int index = 0; index<compoList.size() ; index++){
				
				CompoListDataItem compoListDataItem = compoList.get(index);		
				DeclarationType declarationType = DeclarationType.parse(compoListDataItem.getDeclType());
				
				if(declarationType == DeclarationType.DETAIL){
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
				else if(declarationType == DeclarationType.DECLARE && compoListDataItem.getDeclGrp() != null && !compoListDataItem.getDeclGrp().isEmpty()){
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
				else if(declarationType == DeclarationType.DO_NOT_DECLARE){
					defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, compoListDataItem);
				}
				else if(declarationType == DeclarationType.DECLARE){
					logger.trace(String.format("calculateILL - DECLARE : defaultCompositeIng: %s - current product: %s", defaultCompositeIng.getName(), nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME)));
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
		String ingName = null;
		if(compoListDataItem.getDeclGrp().isEmpty()){
			ingName = (String)nodeService.getProperty(compoListDataItem.getProduct(), BeCPGModel.PROP_PRODUCT_LEGALNAME);
		}
		else{
			ingName = compoListDataItem.getDeclGrp();
		}
		
		//TODO manage mltext in product
		MLText mlName = new MLText();
		mlName.addValue(Locale.getDefault(), ingName);
		mlName.addValue(Locale.ENGLISH, ingName);
		mlName.addValue(Locale.FRENCH, ingName);
		
		CompositeIng compositeIng = new CompositeIng(ingName, mlName);			
		
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
		DeclarationType declarationType = DeclarationType.parse(compoListDataItem.getDeclType());
		boolean isDeclared = (declarationType == DeclarationType.DO_NOT_DECLARE) ? false:true;
		CompositeIng compositeIng = parentIng;
		logger.trace("part: " + part.getLegalName());
		
		//OMIT, DETAIL
		if(declarationType == DeclarationType.OMIT){
			return parentIng;//nothing to do...
		}
		else if(declarationType == DeclarationType.DETAIL){
			
			//TODO manage mltext in product
			MLText mlName = new MLText();
			mlName.addValue(Locale.getDefault(), part.getLegalName());
			mlName.addValue(Locale.ENGLISH, part.getLegalName());
			mlName.addValue(Locale.FRENCH, part.getLegalName());
			
			compositeIng = new CompositeIng(part.getLegalName(), mlName);
			parentIng.add(compositeIng, isDeclared);			
		}
		
		if(part.getIngList() != null){
		
			for(IngListDataItem ingListDataItem : part.getIngList()){						
				
				//Look for ing
				NodeRef ingNodeRef = ingListDataItem.getIng();
				String ingName =  (String)nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME);			
							
				IngItem ingItem = (compositeIng.get(ingName, isDeclared)  instanceof IngItem) ? (IngItem)compositeIng.get(ingName, isDeclared) : null;						
				
				if(ingItem == null){
					
					MLText mlName = (MLText)mlNodeService.getProperty(ingNodeRef, BeCPGModel.PROP_LEGAL_NAME);
					ingItem =new IngItem(ingName, mlName, 0f);
					compositeIng.add(ingItem, isDeclared);
				}															
				
				//Calculate qty
				Float qty = FormulationHelper.getQty(compoListDataItem);
				Float density = (part.getDensity() != null) ? part.getDensity():DEFAULT_DENSITY; //density is null => 1
				Float qtyIng = ingListDataItem.getQtyPerc();
							
				if(qty != null && qtyIng != null){
					
					Float totalQtyIng = ingItem.getQty();
					
					Float valueToAdd = density * qty * qtyIng;
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
				
				List<NodeRef> manualLinks = entityListDAO.getManualLinks(listNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
				
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
				
				List<NodeRef> manualLinks = entityListDAO.getManualLinks(listNodeRef, BeCPGModel.TYPE_INGLIST);
				
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
