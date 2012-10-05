package fr.becpg.repo.product.formulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.SimpleListDataItem;

public class AbstractCalculatingVisitor {

	public static final String UNIT_SEPARATOR = "/";
	
	private static Log logger = LogFactory.getLog(AbstractCalculatingVisitor.class);
	
	protected ProductDAO productDAO;
	
	protected EntityListDAO entityListDAO;

	protected NodeService nodeService;
	
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	protected Map<NodeRef, SimpleListDataItem> getFormulatedList(ProductData formulatedProduct) throws FormulateException{
		logger.debug("getFormulatedList");
			
		Map<NodeRef, SimpleListDataItem> simpleListDataMap = new HashMap<NodeRef, SimpleListDataItem>();
		
		// init with dbValues
		List<? extends SimpleListDataItem> simpleListDataList = productDAO.loadSimpleList(formulatedProduct.getNodeRef(), getDataListVisited());//		
		if(simpleListDataList != null){			
			for(SimpleListDataItem sl : simpleListDataList){
				// reset value if formulated
				if(isCharactFormulated(sl.getCharactNodeRef())){
					sl.setValue(null);
					sl.setMini(null);
					sl.setMaxi(null);
				}				
				simpleListDataMap.put(sl.getCharactNodeRef(), sl);
			}
		}
		
		visitChildren(formulatedProduct, simpleListDataMap);			
		
		// manual listItem
		return getListToUpdate(formulatedProduct.getNodeRef(), simpleListDataMap);
	}
	
	protected void visitChildren(ProductData formulatedProduct, Map<NodeRef, SimpleListDataItem> simpleListDataMap) throws FormulateException{
		
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct);
		
		if(formulatedProduct.getCompoList() != null && formulatedProduct.getCompoList().size()>0){
		
			for(CompoListDataItem compoItem : formulatedProduct.getCompoList()){
				Double qty = FormulationHelper.getQty(compoItem, nodeService);
				
				if(qty != null){
					visitPart(compoItem.getProduct(), simpleListDataMap, qty, netWeight);
				}			
			}
		}		
	}
	
	/**
	 * Visit part.
	 *
	 * @param formulatedProduct the formulated product
	 * @param compoListDataItem the compo list data item
	 * @param physicoChemMap the nut map
	 * @throws FormulateException 
	 */
	protected void visitPart(NodeRef componentNodeRef,  Map<NodeRef, SimpleListDataItem> simpleListDataMap, Double qtyUsed, Double netWeight) throws FormulateException{
				
		List<? extends SimpleListDataItem> simpleListDataList = productDAO.loadSimpleList(componentNodeRef, getDataListVisited());		
		
		if(simpleListDataList == null){
			logger.debug("simpleListDataList is null");
			return;
		}	
		
		for(SimpleListDataItem slDataItem : simpleListDataList){			
			
			//Look for charact
			NodeRef slNodeRef = slDataItem.getCharactNodeRef();
			
			if(isCharactFormulated(slNodeRef)){
				
				SimpleListDataItem newSimpleListDataItem = simpleListDataMap.get(slNodeRef);
				
				if(newSimpleListDataItem == null){
					newSimpleListDataItem =new PhysicoChemListDataItem();
					newSimpleListDataItem.setCharactNodeRef(slNodeRef);													
					simpleListDataMap.put(slNodeRef, newSimpleListDataItem);
				}
				
				//Calculate values				
				if(qtyUsed != null){
										
					Double origValue = newSimpleListDataItem.getValue() != null ? newSimpleListDataItem.getValue() : 0d;
					Double value = slDataItem.getValue();
					if(value != null){
						newSimpleListDataItem.setValue(FormulationHelper.calculateValue(newSimpleListDataItem.getValue(), qtyUsed, slDataItem.getValue(), netWeight));
					}
					else{
						value = 0d;
					}
					
					Double origMini = newSimpleListDataItem.getMini() != null ? newSimpleListDataItem.getMini() : origValue;
					Double miniValue = slDataItem.getMini() != null ? slDataItem.getMini() : value;
					if(miniValue < value || origMini < origValue){
						newSimpleListDataItem.setMini(FormulationHelper.calculateValue(newSimpleListDataItem.getMini(), qtyUsed, miniValue, netWeight));
					}
					
					Double origMaxi = newSimpleListDataItem.getMaxi() != null ? newSimpleListDataItem.getMaxi() : origValue;
					Double maxiValue = slDataItem.getMaxi() != null ? slDataItem.getMaxi() : value;
					if(maxiValue > value || origMaxi > origValue){
						newSimpleListDataItem.setMaxi(FormulationHelper.calculateValue(newSimpleListDataItem.getMaxi(), qtyUsed, maxiValue, netWeight));
					}					
					
					if(logger.isDebugEnabled()){
						logger.debug("valueToAdd = qtyUsed * value : " + qtyUsed + " * " + slDataItem.getValue());
						logger.debug("charact: " + nodeService.getProperty(slNodeRef, ContentModel.PROP_NAME) + " - newValue : " + newSimpleListDataItem.getValue());
						logger.debug("charact: " + nodeService.getProperty(slNodeRef, ContentModel.PROP_NAME) + " - newMini : " + newSimpleListDataItem.getMini());
						logger.debug("charact: " + nodeService.getProperty(slNodeRef, ContentModel.PROP_NAME) + " - newMaxi : " + newSimpleListDataItem.getMaxi());
					}					
				}	
			}							
		}
	}
	
	protected QName getDataListVisited(){
		
		logger.error("Unimplemented getDataListVisited");
		return null;
	}

	protected boolean isCharactFormulated(NodeRef slNodeRef){
		return true;
	}

	/**
	 * Calculate listItem to update
	 * @param productNodeRef
	 * @param costMap
	 * @return
	 */
	protected Map<NodeRef, SimpleListDataItem> getListToUpdate(NodeRef productNodeRef, Map<NodeRef, SimpleListDataItem> slMap){
				
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		
		if(listContainerNodeRef != null){
			
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, getDataListVisited());
			
			if(listNodeRef != null){
				
				List<NodeRef> manualLinks = entityListDAO.getManualListItems(listNodeRef, getDataListVisited());
				
				for(NodeRef manualLink : manualLinks){
										
					SimpleListDataItem sl = (SimpleListDataItem)productDAO.loadItemByType(manualLink, getDataListVisited());		    		
					slMap.put(sl.getCharactNodeRef(), sl);
				}
			}
		}
		
		return slMap;
	}
}
