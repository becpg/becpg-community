package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public abstract class AbstractSimpleListFormulationHandler<T extends SimpleListDataItem> extends FormulationBaseHandler<ProductData> {

	public static final String UNIT_SEPARATOR = "/";
	public static final String MESSAGE_MISSING_MANDATORY_CHARACT = "message.formulate.missing.mandatory.charact";
	
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
	
	protected abstract Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType);
	
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharactsFromList(List<T> simpleListDataList){
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<NodeRef, List<NodeRef>>(simpleListDataList.size());
		for(SimpleListDataItem sl : simpleListDataList){
			if(isCharactFormulated(sl)){
				mandatoryCharacts.put(sl.getCharactNodeRef(), new ArrayList<NodeRef>());
			}			
		}
		return mandatoryCharacts;
	}

	protected void formulateSimpleList(ProductData formulatedProduct, List<T> simpleListDataList) throws FormulateException{
		logger.debug("formulateSimpleList");	
		
		copyProductTemplateList(formulatedProduct, simpleListDataList);

		if(simpleListDataList != null){			
			for(SimpleListDataItem sl : simpleListDataList){
				// reset value if formulated
				if(isCharactFormulated(sl)){
					sl.setValue(null);
					sl.setMini(null);
					sl.setMaxi(null);
					
					// add detailable aspect
					if(!sl.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)){
						sl.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
					}
				}							
			}
		}
				
		visitChildren(formulatedProduct, simpleListDataList);		
		
		//sort
		sort(simpleListDataList);
	}
	
	protected void visitChildren(ProductData formulatedProduct, List<T> simpleListDataList) throws FormulateException{
		
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct.getNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT);
		
		if(formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
			
			Map<NodeRef, List<NodeRef>> mandatoryCharacts = getMandatoryCharacts(formulatedProduct, BeCPGModel.TYPE_RAWMATERIAL);
			
			for(CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
				Double qty = FormulationHelper.getQty(compoItem);
				
				if(qty != null){
					visitPart(compoItem.getProduct(), simpleListDataList, qty, netQty, mandatoryCharacts);
				}			
			}
			
			addReqCtrlList(formulatedProduct.getCompoListView().getReqCtrlList(), mandatoryCharacts);
		}		
	}
	
	protected void addReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList, Map<NodeRef, List<NodeRef>> mandatoryCharacts){
		
		//ReqCtrlList
		for(Map.Entry<NodeRef, List<NodeRef>> mandatoryCharact : mandatoryCharacts.entrySet()){
			if(mandatoryCharact.getValue() != null && !mandatoryCharact.getValue().isEmpty()){
				String message = I18NUtil.getMessage(MESSAGE_MISSING_MANDATORY_CHARACT,
									nodeService.getProperty(mandatoryCharact.getKey(), ContentModel.PROP_NAME));
				
				reqCtrlList.add(new ReqCtrlListDataItem(null,  RequirementType.Tolerated, message, mandatoryCharact.getValue()));					
			}
		}
	}
	
	/**
	 * Visit part.
	 *
	 */
	protected void visitPart(NodeRef componentNodeRef,  
			List<T> simpleListDataList,
			Double qtyUsed, 
			Double netQty, 
			Map<NodeRef, List<NodeRef>> mandatoryCharacts) throws FormulateException{								
		
		if(!BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(componentNodeRef))){
			
			List<? extends SimpleListDataItem> componentSimpleListDataList = alfrescoRepository.loadDataList(componentNodeRef, getDataListVisited() ,getDataListVisited());
			
			if(!alfrescoRepository.hasDataList(componentNodeRef, getDataListVisited()) || 
					componentSimpleListDataList.isEmpty()){			
				
				logger.debug("simpleListDataList " + getDataListVisited() + " is null or empty");
				for(NodeRef charactNodeRef : mandatoryCharacts.keySet()){
					addMissingMandatoryCharact(mandatoryCharacts, charactNodeRef, componentNodeRef);
				}
				return;
			}
			else{
				
				for(SimpleListDataItem newSimpleListDataItem : simpleListDataList){			
					if(newSimpleListDataItem.getCharactNodeRef() != null && isCharactFormulated(newSimpleListDataItem)){
						
						// look for charact in component
						SimpleListDataItem slDataItem = null;				
						for(SimpleListDataItem s : componentSimpleListDataList){
							if(newSimpleListDataItem.getCharactNodeRef() != null && s.getCharactNodeRef() != null &&
									newSimpleListDataItem.getCharactNodeRef().equals(s.getCharactNodeRef())){						
								slDataItem = s;
								break;
							}
						}
									
						//is it a mandatory charact ?
						if(slDataItem == null || slDataItem.getValue() == null){					
							addMissingMandatoryCharact(mandatoryCharacts, newSimpleListDataItem.getCharactNodeRef(), componentNodeRef);
						}
						
						//Calculate values			
						if(slDataItem != null && qtyUsed != null){
												
							Double origValue = newSimpleListDataItem.getValue() != null ? newSimpleListDataItem.getValue() : 0d;
							Double value = slDataItem.getValue();
							if(value != null){
								newSimpleListDataItem.setValue(FormulationHelper.calculateValue(newSimpleListDataItem.getValue(), qtyUsed, slDataItem.getValue(), netQty));
							}
							else{
								value = 0d;
							}
							
							Double origMini = newSimpleListDataItem.getMini() != null ? newSimpleListDataItem.getMini() : origValue;
							Double miniValue = slDataItem.getMini() != null ? slDataItem.getMini() : value;
							if(miniValue < value || origMini < origValue){
								newSimpleListDataItem.setMini(FormulationHelper.calculateValue(newSimpleListDataItem.getMini(), qtyUsed, miniValue, netQty));
							}
							
							Double origMaxi = newSimpleListDataItem.getMaxi() != null ? newSimpleListDataItem.getMaxi() : origValue;
							Double maxiValue = slDataItem.getMaxi() != null ? slDataItem.getMaxi() : value;
							if(maxiValue > value || origMaxi > origValue){
								newSimpleListDataItem.setMaxi(FormulationHelper.calculateValue(newSimpleListDataItem.getMaxi(), qtyUsed, maxiValue, netQty));
							}					
							
							if(logger.isDebugEnabled()){
								logger.debug("valueToAdd = qtyUsed * value : " + qtyUsed + " * " + slDataItem.getValue());
								if(newSimpleListDataItem.getNodeRef()!=null){
									logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), ContentModel.PROP_NAME) + " - newValue : " + newSimpleListDataItem.getValue());
									logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), ContentModel.PROP_NAME) + " - newMini : " + newSimpleListDataItem.getMini());
									logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), ContentModel.PROP_NAME) + " - newMaxi : " + newSimpleListDataItem.getMaxi());
								}
							}					
						}		
					}						
				}	
			}
		}		
	}
	
	protected void addMissingMandatoryCharact(Map<NodeRef, List<NodeRef>> mandatoryCharacts, NodeRef charactNodeRef, NodeRef componentNodeRef){
		if(mandatoryCharacts.containsKey(charactNodeRef)){
			List<NodeRef> sources = mandatoryCharacts.get(charactNodeRef);
			if(sources==null){
				sources = mandatoryCharacts.put(charactNodeRef, new ArrayList<NodeRef>());
			}
			if(!sources.contains(componentNodeRef)){
				sources.add(componentNodeRef);
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
	 * Sort by name.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	protected void sort(List<T> simpleList){
			
		Collections.sort(simpleList, new Comparator<T>(){
        	
            @Override
			public int compare(T c1, T c2){
            	
            	String name1 = (String)nodeService.getProperty(c1.getCharactNodeRef(), ContentModel.PROP_NAME);
            	String name2 = (String)nodeService.getProperty(c2.getCharactNodeRef(), ContentModel.PROP_NAME);
            	
            	// increase
                return name1.compareTo(name2);                
            }

        });  
		
		int i=1;
		for(T sl : simpleList){
			sl.setSort(i);
			i++;
		}
	}
	
	/**
	 * Copy missing item from template
	 * @param formulatedProduct
	 * @param simpleListDataList
	 */
	protected void copyProductTemplateList(ProductData formulatedProduct, List<T> simpleListDataList){
		
		if(formulatedProduct.getEntityTpl() !=null){
			
			//TODO do not use loadDataList
			List<T> templateSimpleListDataList = alfrescoRepository.loadDataList(formulatedProduct.getEntityTpl().getNodeRef(), getDataListVisited(), getDataListVisited());
			
			for(T tsl : templateSimpleListDataList){
				if(tsl.getCharactNodeRef() != null){
					boolean isFound = false;
					for(T sl : simpleListDataList){
						if(tsl.getCharactNodeRef().equals(sl.getCharactNodeRef())){
							isFound = true;
							break;
						}
					}
					if(!isFound){
						tsl.setNodeRef(null);
						tsl.setParentNodeRef(null);
						simpleListDataList.add(tsl);
					}
				}			
			}
		}		
	}
}
