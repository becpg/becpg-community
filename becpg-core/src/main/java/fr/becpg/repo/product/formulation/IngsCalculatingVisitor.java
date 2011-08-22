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
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.CompositeIng;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;

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
	public ProductData visit(ProductData formulatedProduct){
		logger.debug("Calculate ingredient list");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
		}
		
		//IngList
		List<IngListDataItem> ingList = calculateIL(formulatedProduct);
		formulatedProduct.setIngList(ingList);
		logger.debug("ingList.size: " + ingList.size());
		
		//IngLabelling
		logger.debug("Calculate Ingredient Labeling");
		List<CompositeIng> compositeIngs = calculateILL(formulatedProduct);
		Collections.sort(compositeIngs);
		List<IngLabelingListDataItem> ingLabelingList = new ArrayList<IngLabelingListDataItem>(compositeIngs.size());
		
		for(CompositeIng compositeIng : compositeIngs){
						
			MLText mlTextILL = new MLText();			
			mlTextILL.addValue(Locale.ENGLISH, compositeIng.getIngLabeling(Locale.ENGLISH));
			mlTextILL.addValue(Locale.FRENCH, compositeIng.getIngLabeling(Locale.FRENCH));
			ingLabelingList.add(new IngLabelingListDataItem(null, compositeIng.getName(), mlTextILL));
		}
		logger.debug("ingLabelingList.size: " + ingLabelingList.size());
		formulatedProduct.setIngLabelingList(ingLabelingList);
		
		return formulatedProduct;
	}
	
	/**
	 * Calculate the ingredient list of a product.
	 *
	 * @param productData the product data
	 * @return the list
	 */
	private List<IngListDataItem> calculateIL(ProductData productData){
	
		List<CompoListDataItem> compoList = productData.getCompoList();
		Map<NodeRef, IngListDataItem> ingMap = new HashMap<NodeRef, IngListDataItem>();
		Map<NodeRef, Float> totalQtyIngMap = new HashMap<NodeRef, Float>();
		
		if(compoList != null){
			for(CompoListDataItem compoItem : compoList){
				calculateILOfPart(compoItem, ingMap, totalQtyIngMap);
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
		
		//sort collection
		List<IngListDataItem> ingList = new ArrayList<IngListDataItem>(ingMap.values());				
		Collections.sort(ingList);
		
		return ingList;
	}
	
	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param compoListDataItem the compo list data item
	 * @param ingMap the ing map
	 * @param totalQtyIngMap the total qty ing map
	 */
	private void calculateILOfPart(CompoListDataItem compoListDataItem, Map<NodeRef, IngListDataItem> ingMap, Map<NodeRef, Float> totalQtyIngMap){
		
		//OMIT is not taken in account
		if(DeclarationType.parse(compoListDataItem.getDeclType()) == DeclarationType.OMIT){
			return;
		}
			
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_INGLIST);
		ProductData productData = productDAO.find(compoListDataItem.getProduct(), dataLists);		
		
		if(productData.getIngList() == null){
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
	 * Calculate the ingredient labeling of a product.
	 *
	 * @param productData the product data
	 * @return the list
	 */
	private List<CompositeIng> calculateILL(ProductData productData){
		
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
				else if(declarationType == DeclarationType.DECLARE && !compoListDataItem.getDeclGrp().isEmpty()){
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
	 */
	private CompositeIng calculateILLOfCompositeIng(List<CompoListDataItem> compoList, int parentIndex, int lastChild){
		
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
	 */
	private CompositeIng calculateILLOfCompositeIng(CompositeIng parentIng, CompoListDataItem compoListDataItem){
						
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
					
					MLText mlName = (MLText)mlNodeService.getProperty(ingNodeRef, BeCPGModel.PROP_ING_MLNAME);
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
}
