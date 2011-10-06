/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.GetChildren;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.Leaf;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostDetailsListDataItem;
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
	
	public static final String KEY_COST_DETAILS = "%s-%s";
	
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
		logger.debug("Costs calculating visitor");
		
		CompositeCosts compositeCosts = new CompositeCosts();
			
		/*
		 * Calculate the costs of the compoList
		 */
		
		logger.debug("###costVisitor : " + formulatedProduct.getCompoList());
		
		if(formulatedProduct.getCompoList() != null){						
		
			logger.debug("###costVisitor : visitCompoListChildren");
			Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(formulatedProduct.getCompoList());		
			compositeCosts = visitCompoListChildren(formulatedProduct, composite);
		}
		
		/*
		 * Calculate the costs of the packaging
		 */
		if(formulatedProduct.getPackagingList() != null){
			for(PackagingListDataItem packagingListDataItem : formulatedProduct.getPackagingList()){
				Float qty = FormulationHelper.getQty(packagingListDataItem);
				visitCostLeaf(packagingListDataItem.getProduct(), qty, formulatedProduct.getUnit(), compositeCosts.getCostMap(), compositeCosts.getCostDetailsMap());
			}
		}		
		
		List<CostListDataItem> costList = new ArrayList<CostListDataItem>(compositeCosts.getCostMap().values());
		List<CostDetailsListDataItem> costDetailsList = new ArrayList<CostDetailsListDataItem>(compositeCosts.getCostDetailsMap().values());
		
		//Take in account net weight, calculate cost details perc
		if(formulatedProduct.getUnit() != ProductUnit.P){
			Float qty = formulatedProduct.getQty();
			Float density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
			Float netWeight = qty * density;
			
			for(CostListDataItem c : costList){		
				if(c.getValue() != null)
					c.setValue(c.getValue() / netWeight);
			}
			
			for(CostDetailsListDataItem c : costDetailsList){		
				if(c.getValue() != null)
					c.setValue(c.getValue() / netWeight);															
			}
		}
		
		// cost details perc
		for(CostDetailsListDataItem c : costDetailsList){
			
			if(c.getValue() != null){
			
				Float sum = compositeCosts.getCostMap().get(c.getCost()).getValue();
				c.setPercentage(c.getValue() / sum * 100);
			}
			else{
				c.setPercentage(0f);
			}			
			
			logger.debug("###perc: " + c.getPercentage());
		}
		
		//sort
		List<CostListDataItem> costListSorted = sortCost(costList);
		List<CostDetailsListDataItem> costDetailsListSorted = sortCostDetails(costDetailsList);
				
		formulatedProduct.setCostList(costListSorted);			
		logger.debug("costList.size: " + costList.size());
		
		formulatedProduct.setCostDetailsList(costDetailsListSorted);			
		logger.debug("costDetailsList.size: " + costDetailsListSorted.size());
		
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
	private CompositeCosts visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite) throws FormulateException{
	
		CompositeCosts compositeCosts = new CompositeCosts();
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					
			
			logger.debug("###costVisitor: visitCompoListChildren - for");
			
			// take in account the loss perc
			Float lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0;
			
			if(component instanceof Composite){
				
				// calculate children costs
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				CompositeCosts childrenCosts =  visitCompoListChildren(formulatedProduct, c);
				
				logger.debug("###costVisitor: visitCompoListChildren - childrenCosts.getCostMap().size()" + childrenCosts.getCostMap().size());				
				
				/*
				 *  costs
				 */
				for(Map.Entry<NodeRef, CostListDataItem> kv : childrenCosts.getCostMap().entrySet()){						
					
					// valueToAdd
					Float valueToAdd = kv.getValue().getValue();					
					if(valueToAdd != null){
						valueToAdd = valueToAdd * (1 + lossPerc / 100);
					}
					
					CostListDataItem newCostListDataItem = compositeCosts.getCostMap().get(kv.getKey());
					if(newCostListDataItem == null){
						newCostListDataItem = kv.getValue();
						newCostListDataItem.setValue(0f); // initialize
						compositeCosts.getCostMap().put(kv.getKey(), newCostListDataItem);
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
				
				/*
				 *  costsDetails
				 */
				for(Map.Entry<String, CostDetailsListDataItem> kv : childrenCosts.getCostDetailsMap().entrySet()){						
					
					// valueToAdd
					Float valueToAdd = kv.getValue().getValue();					
					if(valueToAdd != null){
						valueToAdd = valueToAdd * (1 + lossPerc / 100);
					}
					
					CostDetailsListDataItem newCostDetailsListDataItem = compositeCosts.getCostDetailsMap().get(kv.getKey());
					if(newCostDetailsListDataItem == null){
						newCostDetailsListDataItem = kv.getValue();
						newCostDetailsListDataItem.setValue(0f); // initialize
						compositeCosts.getCostDetailsMap().put(kv.getKey(), newCostDetailsListDataItem);
					}
					
					// calculate newValue
					Float newValue = newCostDetailsListDataItem.getValue();
					if(newValue != null){
						newValue += valueToAdd;
					}
					else{
						newValue = valueToAdd;
					}
					
					newCostDetailsListDataItem.setValue(newValue);
				}
			}
			else{
				CompoListDataItem compoListDataItem = component.getData();
				Float qty = FormulationHelper.getQty(compoListDataItem);
				qty = qty * (1 + lossPerc / 100);
				visitCostLeaf(compoListDataItem.getProduct(), qty, formulatedProduct.getUnit(), compositeCosts.getCostMap(), compositeCosts.getCostDetailsMap());
				
				logger.debug("###visitCostLeaf compositeCosts.getCostMap().size() : " + compositeCosts.getCostMap().size());
			}			
		}	
		
		return compositeCosts;
	}

	/**
	 * Calculate the costs of a leaf
	 * @param qty
	 * @param productUnit
	 * @param lossPerc
	 * @param costDetailsMap
	 * @param leafProductData
	 */
	private void visitCostLeaf(NodeRef leafNodeRef, Float qty, ProductUnit productUnit, Map<NodeRef, CostListDataItem> costMap, Map<String, CostDetailsListDataItem> costDetailsMap){
		
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_COSTLIST);
		dataLists.add(BeCPGModel.TYPE_COSTDETAILSLIST);
		ProductData leafProductData = productDAO.find(leafNodeRef, dataLists);
		
		logger.debug("###visitCostLeaf : nodeRef: " + leafNodeRef + " - costList: " + leafProductData.getCostList());
		
		if(leafProductData.getCostList() == null){
			return;
		}
		
		logger.debug("###visitCostLeaf 1");
		
		/*
		 * Costs
		 */
		
		for(CostListDataItem costListDataItem : leafProductData.getCostList()){			
			
			NodeRef costNodeRef = costListDataItem.getCost();
			
			//Look for cost			
			CostListDataItem newCostListDataItem = costMap.get(costNodeRef);
			
			if(newCostListDataItem == null){
				newCostListDataItem =new CostListDataItem();
				newCostListDataItem.setCost(costNodeRef);
				
				String unit = calculateUnit(productUnit, (String)nodeService.getProperty(costNodeRef, BeCPGModel.PROP_COSTCURRENCY));			
				newCostListDataItem.setUnit(unit);
				costMap.put(costNodeRef, newCostListDataItem);				
			}				
			
			//Calculate value
			Float costValue = newCostListDataItem.getValue();			
			Float value = costListDataItem.getValue();
			
			if(qty != null && value != null){
				
				Float valueToAdd = qty * value;
				if(costValue != null){
					costValue += valueToAdd;
				}
				else{
					costValue = valueToAdd;
				}					
			}			
			newCostListDataItem.setValue(costValue);
		}
		
		/*
		 * Cost details
		 */
		
		// not a formulated product
		if(leafProductData.getCostDetailsList() == null){
			
			for(CostListDataItem costListDataItem : leafProductData.getCostList()){			
				
				NodeRef costNodeRef = costListDataItem.getCost();								
				
				//Look for costDetails
				String costDetailsKey = getCostDetailsKey(costNodeRef, leafProductData.getNodeRef());
				CostDetailsListDataItem newCostDetailsListDataItem = costDetailsMap.get(costDetailsKey);
				
				if(newCostDetailsListDataItem == null){
					newCostDetailsListDataItem = new CostDetailsListDataItem();
					newCostDetailsListDataItem.setCost(costNodeRef);
					newCostDetailsListDataItem.setSource(leafProductData.getNodeRef());
					
					String unit = calculateUnit(productUnit, (String)nodeService.getProperty(costNodeRef, BeCPGModel.PROP_COSTCURRENCY));			
					newCostDetailsListDataItem.setUnit(unit);
					costDetailsMap.put(costDetailsKey, newCostDetailsListDataItem);				
				}
				
				//Calculate value
				Float costDetailsValue = newCostDetailsListDataItem.getValue();				
				Float value = costListDataItem.getValue();
				
				if(qty != null && value != null){
					
					Float valueToAdd = qty * value;
					
					if(costDetailsValue != null){
						costDetailsValue += valueToAdd;
					}
					else{
						costDetailsValue = valueToAdd;
					}
				}			
				newCostDetailsListDataItem.setValue(costDetailsValue);
			}
		}
		// formulated product
		else{
			
			for(CostDetailsListDataItem costDetailsListDataItem : leafProductData.getCostDetailsList()){			
				
				NodeRef costNodeRef = costDetailsListDataItem.getCost();
				NodeRef sourceNodeRef = costDetailsListDataItem.getSource();
				
				//Look for costDetails
				String costDetailsKey = getCostDetailsKey(costNodeRef, sourceNodeRef);
				CostDetailsListDataItem newCostDetailsListDataItem = costDetailsMap.get(costDetailsKey);
				
				if(newCostDetailsListDataItem == null){
					newCostDetailsListDataItem = new CostDetailsListDataItem();
					newCostDetailsListDataItem.setCost(costNodeRef);
					newCostDetailsListDataItem.setSource(sourceNodeRef);
					
					String unit = calculateUnit(productUnit, (String)nodeService.getProperty(costNodeRef, BeCPGModel.PROP_COSTCURRENCY));			
					newCostDetailsListDataItem.setUnit(unit);
					costDetailsMap.put(costDetailsKey, newCostDetailsListDataItem);				
				}
				
				//Calculate value
				Float costDetailsValue = newCostDetailsListDataItem.getValue();
				Float value = costDetailsListDataItem.getValue();
				
				if(qty != null && value != null){
					
					Float valueToAdd = qty * value;
					
					if(costDetailsValue != null){
						costDetailsValue += valueToAdd;
					}
					else{
						costDetailsValue = valueToAdd;
					}
				}			
				newCostDetailsListDataItem.setValue(costDetailsValue);
			}
		}
		
		logger.debug("###visitCostLeaf costMap.size() : " + costMap.size());
	}
	
	/**
	 * Sort costs by name.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	private List<CostListDataItem> sortCost(List<CostListDataItem> costList){
			
		// TODO: quelle méthode est la meilleure ? sortCost ou sortCostDetails ??? (perf)
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
	 * sort costs by name and value
	 * @param costDetailsList
	 * @return
	 */
	private List<CostDetailsListDataItem> sortCostDetails(List<CostDetailsListDataItem> costDetailsList){
				
		// TODO: quelle méthode est la meilleure ? sortCost ou sortCostDetails ??? (perf)
        Collections.sort(costDetailsList, new Comparator<CostDetailsListDataItem>()
        {
            @Override
			public int compare(CostDetailsListDataItem c1, CostDetailsListDataItem c2)
            {
            	String costName1 = (String)nodeService.getProperty(c1.getCost(), ContentModel.PROP_NAME);
            	String costName2 = (String)nodeService.getProperty(c2.getCost(), ContentModel.PROP_NAME);
            	
            	// increase
                int result = costName1.compareTo(costName2);
                if (result == 0)
                {
                    Float value1 = c1.getValue();
                    Float value2 = c2.getValue();
                    
                    if (value1 != null && value2 != null)
                    {
                    	// decrease
                        result = value2.compareTo(value1);
                    }                    
                }
                return result;
            }

        });
        
        return costDetailsList;
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
		
	private String getCostDetailsKey(NodeRef cost, NodeRef source){
		
		return String.format(KEY_COST_DETAILS, cost, source);
	}
	
	private class CompositeCosts{
		
		private Map<NodeRef, CostListDataItem> costMap = new HashMap<NodeRef, CostListDataItem>(); 
		private Map<String, CostDetailsListDataItem> costDetailsMap = new HashMap<String, CostDetailsListDataItem>();
		
		public Map<NodeRef, CostListDataItem> getCostMap() {
			return costMap;
		}		
		public Map<String, CostDetailsListDataItem> getCostDetailsMap() {
			return costDetailsMap;
		}			
	}
}