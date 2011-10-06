/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.sort.CostListDataItemDecorator;
import fr.becpg.repo.product.data.productList.sort.CostListSortComparator;

// TODO: Auto-generated Javadoc
/**
 * The Class CostsCalculatingVisitor.
 *
 * @author querephi
 */
public class CostsCalculatingVisitor implements ProductVisitor {
	
	/** The Constant QTY_FOR_UNIT. */
	public static final float QTY_FOR_UNIT = 1f;
	
	/** The Constant DEFAULT_DENSITY. */
	public static final float DEFAULT_DENSITY = 1f;
	
	/** The Constant UNIT_SEPARATOR. */
	public static final String UNIT_SEPARATOR = "/";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CostsCalculatingVisitor.class);
	
	/** The node service. */
	private NodeService nodeService;
	
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

//	@Override
//	public FinishedProductData visit(FinishedProductData finishedProductData) {
//		visitProduct(finishedProductData);		
//	}
//
//	@Override
//	public RawMaterialData visit(RawMaterialData rawMaterialData) {
//		//Nothing to do
//	}
//
//	@Override
//	public PackagingMaterialData visit(PackagingMaterialData packagingMaterialData) {
//		//Nothing to do
//	}
//
//	@Override
//	public void visit(SemiFinishedProductData semiFinishedProductData) {
//		visitProduct(semiFinishedProductData);
//	}
//
//	@Override
//	public void visit(LocalSemiFinishedProduct localSemiFinishedProductData) {
//		//Nothing to do		
//	}
	
	/* (non-Javadoc)
 * @see fr.becpg.repo.product.ProductVisitor#visit(fr.becpg.repo.food.ProductData)
 */
@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{
		logger.debug("Costs compoList calculating visitor");
		
		Map<NodeRef, CostListDataItem> costMap = new HashMap<NodeRef, CostListDataItem>(); 
			
		/*
		 * Calculate the costs of the compoList
		 */
		
		if(formulatedProduct.getCompoList() != null){						
		
			Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList());		
			costMap = visitCompoListChildren(formulatedProduct, composite);
		}
		
		/*
		 * Calculate the costs of the packaging
		 */
		if(formulatedProduct.getPackagingList() != null){
			for(PackagingListDataItem packagingListDataItem : formulatedProduct.getPackagingList()){
				visitPackagingLeaf(formulatedProduct, packagingListDataItem, costMap);
			}
		}		
		
		List<CostListDataItem> costList = new ArrayList<CostListDataItem>(costMap.values());
		
		//Take in account net weight
		if(formulatedProduct.getUnit() != ProductUnit.P){
			Float qty = formulatedProduct.getQty();
			Float density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
			Float netWeight = qty * density;
			for(CostListDataItem c : costList){		
				if(c.getValue() != null)
					c.setValue(c.getValue() / netWeight);
			}
		}
		
		//sort
		List<CostListDataItem> costListSorted = sort(costList); 
		
		formulatedProduct.setCostList(costListSorted);
		logger.debug("costList.size: " + costList.size());
		return formulatedProduct;
	}

	/**
	 * Calculate the costs of the children
	 * @param productUnit
	 * @param fatherPos : -1, root
	 * @param compoList
	 * @param costMap
	 * @throws FormulateException 
	 */
	private Map<NodeRef, CostListDataItem> visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite) throws FormulateException{
	
		Map<NodeRef, CostListDataItem> costMap = new HashMap<NodeRef, CostListDataItem>();
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			if(component instanceof Composite){
				
				// calculate children costs
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				Map<NodeRef, CostListDataItem> childrenCostMap =  visitCompoListChildren(formulatedProduct, c);
				
				// take in account the loss perc
				Float lossPerc = c.getData().getLossPerc() != null ? c.getData().getLossPerc() : 0;
				
				for(Map.Entry<NodeRef, CostListDataItem> kv : childrenCostMap.entrySet()){						
					
					// valueToAdd
					Float valueToAdd = kv.getValue().getValue();					
					if(valueToAdd != null){
						valueToAdd = valueToAdd * (1 + lossPerc / 100);
					}
					
					CostListDataItem newCostListDataItem = costMap.get(kv.getKey());
					if(newCostListDataItem == null){
						newCostListDataItem = kv.getValue();
						newCostListDataItem.setValue(0f); // initialize
						costMap.put(kv.getKey(), newCostListDataItem);
					}
					
					// calculate newValue
					Float newValue = newCostListDataItem.getValue();
					if(newValue != null){
						newValue += valueToAdd;
					}
					else{
						newValue = valueToAdd;
					}
					
					newCostListDataItem.setValue(newValue);
				}				
			}
			else{
				visitCompoListLeaf(formulatedProduct, component.getData(), costMap);
			}			
		}	
		
		return costMap;
	}

	/**
	 * Visit a leaf (SF/RM) of the compoList
	 *
	 * @param formulatedProduct the formulated product
	 * @param compoListDataItem the compo list data item
	 * @param costMap the cost map
	 * @throws FormulateException 
	 */
	private void visitCompoListLeaf(ProductData formulatedProduct, CompoListDataItem compoListDataItem, Map<NodeRef, CostListDataItem> costMap) throws FormulateException{
		
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_COSTLIST);
		ProductData productData = productDAO.find(compoListDataItem.getProduct(), dataLists);
		
		if(productData.getCostList() == null){
			return;
		}
		
		for(CostListDataItem costListDataItem : productData.getCostList()){			
			
			//Look for cost
			NodeRef costNodeRef = costListDataItem.getCost();
			CostListDataItem newCostListDataItem = costMap.get(costNodeRef);
			
			if(newCostListDataItem == null){
				newCostListDataItem =new CostListDataItem();
				newCostListDataItem.setCost(costNodeRef);
				
				String unit = calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(costNodeRef, BeCPGModel.PROP_COSTCURRENCY));			
				newCostListDataItem.setUnit(unit);
				costMap.put(costNodeRef, newCostListDataItem);				
			}					
			
			//Calculate value
			Float newValue = newCostListDataItem.getValue();
			Float qty = FormulationHelper.getQty(compoListDataItem);
			Float lossPerc = compoListDataItem.getLossPerc() != null ? compoListDataItem.getLossPerc() : 0;
			Float value = costListDataItem.getValue();
			
			if(qty != null && value != null){
				
				Float valueToAdd = qty * value * (1 + lossPerc / 100);
				if(newValue != null){
					newValue += valueToAdd;
				}
				else{
					newValue = valueToAdd;
				}
			}			
			newCostListDataItem.setValue(newValue);
		}
	}
	
	/**
	 * Visit a packaging item
	 *
	 * @param formulatedProduct the formulated product
	 * @param packagingListDataItem the packaging list data item
	 * @param costMap the cost map
	 */
	private void visitPackagingLeaf(ProductData formulatedProduct, PackagingListDataItem packagingListDataItem, Map<NodeRef, CostListDataItem> costMap){
		
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_COSTLIST);
		ProductData productData = productDAO.find(packagingListDataItem.getProduct(), dataLists);
		
		if(productData.getCostList() == null){
			return;
		}
		
		for(CostListDataItem costListDataItem : productData.getCostList()){			
			
			//Look for cost
			NodeRef costNodeRef = costListDataItem.getCost();
			CostListDataItem newCostListDataItem = costMap.get(costNodeRef);
			
			if(newCostListDataItem == null){
				newCostListDataItem =new CostListDataItem();
				newCostListDataItem.setCost(costNodeRef);
				
				String unit = calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(costNodeRef, BeCPGModel.PROP_COSTCURRENCY));			
				newCostListDataItem.setUnit(unit);
				costMap.put(costNodeRef, newCostListDataItem);
			}					
			
			//Calculate value
			Float newValue = newCostListDataItem.getValue();
			Float qty = FormulationHelper.getQty(packagingListDataItem);
			Float value = costListDataItem.getValue();
			
			if(qty != null && value != null){
				
				Float valueToAdd = qty * value;
				if(newValue != null){
					newValue += valueToAdd;
				}
				else{
					newValue = valueToAdd;
				}
			}			
			newCostListDataItem.setValue(newValue);
		}
	}
	
	/**
	 * Sort costs by name.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	private List<CostListDataItem> sort(List<CostListDataItem> costList){
			
		List<CostListDataItemDecorator> costListDecorated = new ArrayList<CostListDataItemDecorator>();
		for(CostListDataItem costListDataItem : costList){
			CostListDataItemDecorator c = new CostListDataItemDecorator();
			c.setCostListDataItem(costListDataItem);
			c.setCostName(((String)nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME)));
			costListDecorated.add(c);
		}
        Collections.sort(costListDecorated, new CostListSortComparator());
       
        List<CostListDataItem> costListSorted = new ArrayList<CostListDataItem>();
        for(CostListDataItemDecorator c : costListDecorated)
        	costListSorted.add(c.getCostListDataItem());
                
        return costListSorted;
	}
	
	/**
	 * Calculate the costListUnit
	 * @param productUnit
	 * @param costUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String costUnit){
		
		return costUnit + calculateSuffixUnit(productUnit);
	}
	
	/**
	 * Calculate the suffix of the costListUnit
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit){
		return productUnit != null ? UNIT_SEPARATOR + productUnit : UNIT_SEPARATOR + ProductUnit.kg;
	}
		
}
