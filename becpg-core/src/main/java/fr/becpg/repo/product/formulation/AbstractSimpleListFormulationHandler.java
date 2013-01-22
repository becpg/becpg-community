package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.repository.model.SimpleListDataItem;

public abstract class AbstractSimpleListFormulationHandler<T extends SimpleListDataItem> extends FormulationBaseHandler<ProductData> {

	public static final String UNIT_SEPARATOR = "/";
	
	private static Log logger = LogFactory.getLog(AbstractSimpleListFormulationHandler.class);
	

	protected AlfrescoRepository<T> alfrescoRepository;
	
	protected EntityListDAO entityListDAO;

	protected NodeService nodeService;


	public void setAlfrescoRepository(AlfrescoRepository<T> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public T createNewInstance() throws InstantiationException, IllegalAccessException{
		return getInstanceClass().newInstance();
	}
	
	protected abstract Class<T> getInstanceClass();
	
	protected abstract QName getDataListVisited();
	

	protected void formulateSimpleList(ProductData formulatedProduct, List<T> simpleListDataList) throws FormulateException{
		logger.debug("formulateSimpleList");			

		if(simpleListDataList != null){			
			for(SimpleListDataItem sl : simpleListDataList){
				// reset value if formulated
				if(isCharactFormulated(sl)){
					sl.setValue(null);
					sl.setMini(null);
					sl.setMaxi(null);
				}
			}
		}
		
		List<T> retainNodes = new ArrayList<T>();
		
		//manuel
		for(T sl : simpleListDataList){
			if(sl.getIsManual()!= null && sl.getIsManual()){
				retainNodes.add(sl);
			}
		}
		
		visitChildren(formulatedProduct, simpleListDataList, retainNodes);
		
		logger.debug("###simpleListDataList size: " + simpleListDataList.size() + " retainNodes: " + retainNodes.size());
		simpleListDataList.retainAll(retainNodes);			
		logger.debug("simpleListDataList size: " + simpleListDataList.size());
		
		//sort
		sort(simpleListDataList);
	}
	
	protected void visitChildren(ProductData formulatedProduct, List<T> simpleListDataList, List<T> retainNodes) throws FormulateException{
		
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct);
		
		if(formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE)){
		
			for(CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE)){
				Double qty = FormulationHelper.getQty(compoItem, nodeService);
				
				if(qty != null){
					visitPart(compoItem.getProduct(), simpleListDataList, retainNodes, qty, netWeight);
				}			
			}
		}		
	}
	
	/**
	 * Visit part.
	 *
	 */
	protected void visitPart(NodeRef componentNodeRef,  List<T> simpleListDataList, List<T> retainNodes, Double qtyUsed, Double netWeight) throws FormulateException{
				
		List<? extends SimpleListDataItem> componentSimpleListDataList = alfrescoRepository.loadDataList(componentNodeRef, getDataListVisited() ,getDataListVisited());					
		
		if(componentSimpleListDataList == null){
			logger.debug("simpleListDataList is null");
			return;
		}	
		
		logger.debug("###componentSimpleListDataList nodeRef: " + componentNodeRef + " name: " + (String)nodeService.getProperty(componentNodeRef, ContentModel.PROP_NAME) + "  size :" + componentSimpleListDataList.size());
		
		for(SimpleListDataItem slDataItem : componentSimpleListDataList){			
			
			//Look for charact
			NodeRef slNodeRef = slDataItem.getCharactNodeRef();
			
			T newSimpleListDataItem = findSimpleListDataItem(simpleListDataList, slNodeRef);
			
			if(newSimpleListDataItem == null){
				try {
					newSimpleListDataItem = createNewInstance();
				} catch (InstantiationException e) {
					throw new FormulateException(e);
				} catch (IllegalAccessException e) {
					throw new FormulateException(e);
				}
				newSimpleListDataItem.setCharactNodeRef(slNodeRef);													
				simpleListDataList.add(newSimpleListDataItem);					
			}
			
			if(!retainNodes.contains(newSimpleListDataItem)){
				retainNodes.add(newSimpleListDataItem);
			}			
			
			if(isCharactFormulated(newSimpleListDataItem)){
				
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
						if(slNodeRef!=null){
							logger.debug("charact: " + nodeService.getProperty(slNodeRef, ContentModel.PROP_NAME) + " - newValue : " + newSimpleListDataItem.getValue());
							logger.debug("charact: " + nodeService.getProperty(slNodeRef, ContentModel.PROP_NAME) + " - newMini : " + newSimpleListDataItem.getMini());
							logger.debug("charact: " + nodeService.getProperty(slNodeRef, ContentModel.PROP_NAME) + " - newMaxi : " + newSimpleListDataItem.getMaxi());
						}
					}					
				}	
			}							
		}
	}
	
	protected boolean isCharactFormulated(SimpleListDataItem sl){
		return sl.getIsManual()==null || !sl.getIsManual().booleanValue();
	}
	
	protected T findSimpleListDataItem(List<T> simpleList, NodeRef charactNodeRef){
		if(charactNodeRef != null){
			for(T s : simpleList){
				if(charactNodeRef.equals(s.getCharactNodeRef())){
					return s;
				}
			}
		}
		return null;		
	}
	
	/**
	 * Sort costs by name.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	protected void sort(List<T> simpleList){
			
		Collections.sort(simpleList, new Comparator<T>(){
        	
            @Override
			public int compare(T c1, T c2){
            	
            	String costName1 = (String)nodeService.getProperty(c1.getCharactNodeRef(), ContentModel.PROP_NAME);
            	String costName2 = (String)nodeService.getProperty(c2.getCharactNodeRef(), ContentModel.PROP_NAME);
            	
            	// increase
                return costName1.compareTo(costName2);                
            }

        });  
		
		int i=1;
		for(T sl : simpleList){
			sl.setSort(i);
		}
	}
}
