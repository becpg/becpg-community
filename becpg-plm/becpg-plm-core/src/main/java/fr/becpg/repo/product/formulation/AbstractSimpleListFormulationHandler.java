/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

public abstract class AbstractSimpleListFormulationHandler<T extends SimpleListDataItem> extends FormulationBaseHandler<ProductData> {

	public static final String UNIT_SEPARATOR = "/";
	public static final String MESSAGE_MISSING_MANDATORY_CHARACT = "message.formulate.missing.mandatory.charact";
	
	private static Log logger = LogFactory.getLog(AbstractSimpleListFormulationHandler.class);
	

	protected AlfrescoRepository<T> alfrescoRepository;
	
	protected EntityListDAO entityListDAO;

	protected NodeService nodeService;

	protected boolean transientFormulation = false;
	

	public void setTransientFormulation(boolean transientFormulation) {
		this.transientFormulation = transientFormulation;
	}

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
		
		copyProductTemplateList(formulatedProduct, simpleListDataList);
				
		visitChildren(formulatedProduct, simpleListDataList);		
		
		//sort
	//	sort(simpleListDataList);
	}
	
	@SuppressWarnings("unchecked")
	protected void visitChildren(ProductData formulatedProduct, List<T> simpleListDataList) throws FormulateException{
		
        Map<NodeRef, Double> totalQties = new HashMap<NodeRef, Double>();		
		
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		
		if(formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
			
			Map<NodeRef, List<NodeRef>> mandatoryCharacts = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);
			
			for(CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
				Double weight = FormulationHelper.getQty(compoItem);
				Double vol = FormulationHelper.getNetVolume(compoItem, nodeService);
				
				if(weight != null){
					visitPart(compoItem.getProduct(), simpleListDataList, weight, vol, netQty, mandatoryCharacts, totalQties);
				}			
			}
			
			addReqCtrlList(formulatedProduct.getCompoListView().getReqCtrlList(), mandatoryCharacts);
		}		
		
		//Case Generic MP
		if( formulatedProduct instanceof RawMaterialData){
			if(logger.isDebugEnabled()){
				logger.debug("Case generic MP adjust value to total");
			}
			for(SimpleListDataItem newSimpleListDataItem : simpleListDataList){			
				if(totalQties.containsKey(newSimpleListDataItem.getCharactNodeRef()) ){
					Double totalQty = totalQties.get(newSimpleListDataItem.getCharactNodeRef());
					if(newSimpleListDataItem.getValue()!=null){
						newSimpleListDataItem.setValue(newSimpleListDataItem.getValue()*netQty/totalQty);
					}
				}
			}
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
	 * @param valueCount 
	 *
	 */
	protected void visitPart(NodeRef componentNodeRef,  
			List<T> simpleListDataList,
			Double weightUsed,
			Double volUsed,
			Double netQty,			
			Map<NodeRef, List<NodeRef>> mandatoryCharacts,
			Map<NodeRef, Double> totalQties) throws FormulateException{								
		
		if(!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(componentNodeRef))){
			
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
						
						// calculate charact from qty or vol ?
						Double qtyUsed = isCharactFormulatedFromVol(newSimpleListDataItem) ? volUsed : weightUsed;
						
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
						if(slDataItem == null || (slDataItem.getValue() == null && slDataItem.getMaxi() == null && slDataItem.getMini() == null)){					
							addMissingMandatoryCharact(mandatoryCharacts, newSimpleListDataItem.getCharactNodeRef(), componentNodeRef);
						}
						
						//Calculate values			
						if(slDataItem != null && qtyUsed != null ){
												
							Double origValue = newSimpleListDataItem.getValue() != null ? newSimpleListDataItem.getValue() : 0d;
							Double value = slDataItem.getValue();
							if(value != null){
								newSimpleListDataItem.setValue(FormulationHelper.calculateValue(newSimpleListDataItem.getValue(), qtyUsed, slDataItem.getValue(), netQty));
								
								if(totalQties!=null){
									Double currentQty = totalQties.get(newSimpleListDataItem.getCharactNodeRef());
									if(currentQty==null){
										currentQty = 0d;
									}
									totalQties.put(newSimpleListDataItem.getCharactNodeRef(),currentQty+qtyUsed);
								}
								
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
	
	protected boolean isCharactFormulatedFromVol(SimpleListDataItem sl){
		return false;
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
