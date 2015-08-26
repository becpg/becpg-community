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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.CharactDetailsVisitor;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

@Service
public class SimpleCharactDetailsVisitor implements CharactDetailsVisitor {

	private static final Log logger = LogFactory.getLog(SimpleCharactDetailsVisitor.class);

	protected AlfrescoRepository<SimpleCharactDataItem> alfrescoRepository;
	
	protected NodeService nodeService;
	
	protected QName dataListType;

	public void setAlfrescoRepository(AlfrescoRepository<SimpleCharactDataItem> alfrescoRepository) {
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
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems) throws FormulateException {

		CharactDetails ret = new CharactDetails(extractCharacts(dataListItems));
		Double netQty = FormulationHelper.getNetQtyInLorKg(productData,FormulationHelper.DEFAULT_NET_WEIGHT);

		if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			for (CompoListDataItem compoListDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				Double qty = FormulationHelper.getQtyInKg(compoListDataItem);			
				visitPart(compoListDataItem.getProduct(), ret, qty, netQty);
			}
		}		

		return ret;
	}

	protected List<NodeRef> extractCharacts(List<NodeRef> dataListItems) {

		List<NodeRef> ret = new ArrayList<>();
		if (dataListItems != null) {
			for (NodeRef dataListItem : dataListItems) {

				SimpleCharactDataItem o = alfrescoRepository.findOne(dataListItem);
				if (o != null ) {
					ret.add(o.getCharactNodeRef());
				}
			}
		}

		return ret;
	}

	protected void visitPart(NodeRef entityNodeRef, CharactDetails charactDetails, Double qty, Double netQty)
			throws FormulateException {

		if(entityNodeRef == null){
			return;
		}

		if (!alfrescoRepository.hasDataList(entityNodeRef,dataListType)) {
			logger.debug("no datalist for this product, exit. dataListType: " + dataListType + " entity: " + entityNodeRef);
			return;
		}
		
		List<SimpleCharactDataItem> simpleCharactDataList = alfrescoRepository.loadDataList(entityNodeRef,dataListType,  dataListType);

		for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
			if (simpleCharact != null && charactDetails.hasElement(simpleCharact.getCharactNodeRef())) {

				Double value = (simpleCharact.getValue() != null ? simpleCharact.getValue() : 0d);
				value = value * qty;
				if (netQty != 0d) {
					value = value / netQty;
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("Add new charact detail. Charact: " + 
							nodeService.getProperty(simpleCharact.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + 
							" - entityNodeRef: " + nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CHARACT_NAME) + 
							" - qty: " + qty +
							" - value: " + value);
				}
				charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(), entityNodeRef, value);
			}
		}
	}
}
