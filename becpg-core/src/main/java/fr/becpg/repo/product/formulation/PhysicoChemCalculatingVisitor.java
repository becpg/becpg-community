/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;

/**
 * The Class PhysicoChemCalculatingVisitor.
 *
 * @author querephi
 */
public class PhysicoChemCalculatingVisitor implements ProductVisitor {
	
	/** The Constant QTY_FOR_PIECE. */
	public static final Double QTY_FOR_PIECE = 1d;
	
	/** The Constant DEFAULT_DENSITY. */
	public static final Double DEFAULT_DENSITY = 1d;
	
	/** The Constant DEFAULT_QUANTITY. */
	public static final Double DEFAULT_QUANTITY = 0d;
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(PhysicoChemCalculatingVisitor.class);
	
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
	
/* (non-Javadoc)
 * @see fr.becpg.repo.product.ProductVisitor#visit(fr.becpg.repo.food.ProductData)
 */
	@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{
		logger.debug("PhysicoChem calculating visitor");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
		}
		
		// init physicoChem with dbValues
		Map<NodeRef, PhysicoChemListDataItem> physicoChemMap = new HashMap<NodeRef, PhysicoChemListDataItem>();
		if(formulatedProduct.getPhysicoChemList() != null){			
			for(PhysicoChemListDataItem pcl : formulatedProduct.getPhysicoChemList()){
				// reset value
				pcl.setValue(0d);
				physicoChemMap.put(pcl.getPhysicoChem(), pcl);
			}
		}
		
		for(CompoListDataItem compoItem : formulatedProduct.getCompoList()){			
			visitPart(formulatedProduct, compoItem, physicoChemMap);
		}				
		
		//Take in account net weight
		Double qty = (formulatedProduct.getUnit() != ProductUnit.P) ? formulatedProduct.getQty():QTY_FOR_PIECE; //unit => qty == 1
		if(qty==null){
			qty = DEFAULT_QUANTITY;
		}
		Double density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
		Double netWeight = qty * density;
		
		for(PhysicoChemListDataItem n : physicoChemMap.values()){
			
			if(netWeight != 0.0d){
				if(n.getValue() != null)
					n.setValue(n.getValue() / netWeight);
				if(n.getMini() != null)
					n.setMini(n.getMini() / netWeight);
				if(n.getMaxi() != null)
					n.setMaxi(n.getMaxi() / netWeight);
			}					
		}
		
		formulatedProduct.setPhysicoChemList(new ArrayList<PhysicoChemListDataItem>(physicoChemMap.values()));				

		return formulatedProduct;
	}

	/**
	 * Visit part.
	 *
	 * @param formulatedProduct the formulated product
	 * @param compoListDataItem the compo list data item
	 * @param physicoChemMap the nut map
	 * @throws FormulateException 
	 */
	private void visitPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem,  Map<NodeRef, PhysicoChemListDataItem> physicoChemMap) throws FormulateException{
		
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);			
		ProductData productData = productDAO.find(compoListDataItem.getProduct(), dataLists);
		
		if(productData.getPhysicoChemList() == null){
			return;
		}
		
		for(PhysicoChemListDataItem physicoChemListDataItem : productData.getPhysicoChemList()){			
			
			//Look for charact
			NodeRef pcNodeRef = physicoChemListDataItem.getPhysicoChem();
			Boolean isFormulated = (Boolean)nodeService.getProperty(pcNodeRef, BeCPGModel.PROP_PHYSICO_CHEM_FORMULATED);
			
			if(isFormulated != null && isFormulated.booleanValue()){
				
				PhysicoChemListDataItem newPhysicoChemListDataItem = physicoChemMap.get(pcNodeRef);
				
				if(newPhysicoChemListDataItem == null){
					newPhysicoChemListDataItem =new PhysicoChemListDataItem();
					newPhysicoChemListDataItem.setPhysicoChem(pcNodeRef);				
									
					physicoChemMap.put(pcNodeRef, newPhysicoChemListDataItem);
				}									
				
				//Calculate values
				Double qty = FormulationHelper.getQty(compoListDataItem);
				Double density = (productData.getDensity() != null) ? productData.getDensity():DEFAULT_DENSITY; //density is null => 1
				
				
				if(qty != null){
					
					// value
					Double origValue = newPhysicoChemListDataItem.getValue() != null ? newPhysicoChemListDataItem.getValue() : 0d;
					Double newValue = origValue;
					Double value = physicoChemListDataItem.getValue();
					
					if(value != null){
											
						Double valueToAdd = density * qty * value;											
						newValue += valueToAdd;
						newPhysicoChemListDataItem.setValue(newValue);						
					}
					else{
						value = 0d;
					}
					
					//mini
					Double newMini = newPhysicoChemListDataItem.getMini() != null ? newPhysicoChemListDataItem.getMini() : origValue;
					Double mini = physicoChemListDataItem.getMini() != null ? physicoChemListDataItem.getMini() : value;
					
					if(mini < value || newMini < origValue){
					
						Double valueToAdd = density * qty * mini;										
						newMini += valueToAdd;
						newPhysicoChemListDataItem.setMini(newMini);
					}
					
					//maxi
					Double newMaxi = newPhysicoChemListDataItem.getMaxi() != null ? newPhysicoChemListDataItem.getMaxi() : origValue;
					Double maxi = physicoChemListDataItem.getMaxi() != null ? physicoChemListDataItem.getMaxi() : value;
					
					if(maxi > value || newMaxi > origValue){
					
						Double valueToAdd = density * qty * maxi;					
						newMaxi += valueToAdd;
						newPhysicoChemListDataItem.setMaxi(newMaxi);
					}
				}	
			}
								
		}
	}
}
