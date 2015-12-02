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
package fr.becpg.repo.product.formulation.details;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.CharactDetailsVisitor;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;

@Service
public class SimpleCharactDetailsVisitor implements CharactDetailsVisitor {

	private static final Log logger = LogFactory.getLog(SimpleCharactDetailsVisitor.class);

	protected AlfrescoRepository<? extends RepositoryEntity> alfrescoRepository;

	protected NodeService nodeService;

	protected EntityDictionaryService entityDictionaryService;
	
	protected QName dataListType;


	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<? extends RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void setDataListType(QName dataListType) {
		this.dataListType = dataListType;
	}

	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems, Integer level) throws FormulateException {

		CharactDetails ret = createCharactDetails(dataListItems);
		
		if(level == null){
			level = 0;
		}

		Double netQty = FormulationHelper.getNetQtyInLorKg(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		visitRecur(productData, ret, 0, level, netQty,  netQty);

		return ret;
	}

	public CharactDetails visitRecur(ProductData subProductData, CharactDetails ret, Integer currLevel, Integer maxLevel, Double subQuantity , Double netQty)
			throws FormulateException {

		if (subProductData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

			for (CompoListDataItem compoListDataItem : subProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				Double qty = FormulationHelper.getQtyInKg(compoListDataItem)
						/ FormulationHelper.getNetQtyInLorKg(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT) * subQuantity;
				Double qtyUsed = null;
				if (qty != null) {
					qtyUsed = qty * FormulationHelper.getYield(compoListDataItem) / 100;
				}

				visitPart(subProductData.getNodeRef(), compoListDataItem.getProduct(), ret, qtyUsed, netQty, currLevel);

				if (((maxLevel < 0) || (currLevel < maxLevel)) && !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()))) {

					visitRecur((ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct()), ret, currLevel+1, maxLevel, qty, netQty);
				}

			}
		}

		return ret;
	}

	protected CharactDetails createCharactDetails(List<NodeRef> dataListItems) {
		
		List<NodeRef> tmp = new ArrayList<>();
		if (dataListItems != null) {
			for (NodeRef dataListItem : dataListItems) {

				SimpleCharactDataItem o = (SimpleCharactDataItem) alfrescoRepository.findOne(dataListItem);
				if (o != null) {
					tmp.add(o.getCharactNodeRef());
				}
			}
		}
		
		CharactDetails ret = new CharactDetails(tmp);
		
		return ret;
	}

	protected void visitPart(NodeRef parent, NodeRef entityNodeRef, CharactDetails charactDetails, Double qtyUsed, Double netQty, Integer currLevel) throws FormulateException {

		if (entityNodeRef == null) {
			return;
		}

		if (!alfrescoRepository.hasDataList(entityNodeRef, dataListType)) {
			logger.debug("no datalist for this product, exit. dataListType: " + dataListType + " entity: " + entityNodeRef);
			return;
		}

		@SuppressWarnings("unchecked")
		List<SimpleCharactDataItem> simpleCharactDataList = (List<SimpleCharactDataItem>) alfrescoRepository.loadDataList(entityNodeRef, dataListType, dataListType);

		for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
			if (simpleCharact != null && charactDetails.hasElement(simpleCharact.getCharactNodeRef())) {
				
				String unit = null;
				
				if (simpleCharact instanceof UnitAwareDataItem) {
					unit =  ((UnitAwareDataItem) simpleCharact).getUnit();
				} 

				Double value = FormulationHelper.calculateValue(0d, qtyUsed, simpleCharact.getValue(), netQty, unit);

				if(value!=null && value!= 0d){
					if (logger.isDebugEnabled()) {
						logger.debug("Add new charact detail. Charact: "
								+ nodeService.getProperty(simpleCharact.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - entityNodeRef: "
								+ nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CHARACT_NAME) + " - netQty: " + netQty + " - qty: " + qtyUsed
								+ " - value: " + value);
					}
					
					
					charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(),new CharactDetailsValue(parent, entityNodeRef, value, currLevel, unit));
				}
			}
		}
	}
}
