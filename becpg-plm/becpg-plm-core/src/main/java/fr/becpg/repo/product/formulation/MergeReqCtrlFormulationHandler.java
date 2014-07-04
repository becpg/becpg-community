/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;

/**
 * Merge ReqCtrlListDataItem to avoid duplication of items and sort them
 * @author quere
 *
 */
public class MergeReqCtrlFormulationHandler extends FormulationBaseHandler<ProductData> {

	protected static Log logger = LogFactory.getLog(MergeReqCtrlFormulationHandler.class);

	@Override
	public boolean process(ProductData productData) throws FormulateException {
		
		mergeReqCtrlList(productData.getCompoListView().getReqCtrlList());
		mergeReqCtrlList(productData.getPackagingListView().getReqCtrlList());
		mergeReqCtrlList(productData.getProcessListView().getReqCtrlList());
		
		return true;
	}

	private void mergeReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList){
		
		Map<String, ReqCtrlListDataItem> dbReqCtrlList = new HashMap<>();
		Map<String, ReqCtrlListDataItem> newReqCtrlList = new HashMap<>();
		List<ReqCtrlListDataItem> duplicates = new ArrayList<>();
		
		
		for(ReqCtrlListDataItem r : reqCtrlList){
			if(r.getNodeRef() != null){
				if(dbReqCtrlList.containsKey(r.getReqMessage())){
					duplicates.add(r);
					logger.warn("This should not happen!");
				} else {
					dbReqCtrlList.put(r.getReqMessage(), r);
				}
			}
			else{
				if(newReqCtrlList.containsKey(r.getReqMessage())){
					duplicates.add(r);
					//Merge sources
					for(NodeRef tmpref : r.getSources()){
						if(!newReqCtrlList.get(r.getReqMessage()).getSources().contains(tmpref)){
							newReqCtrlList.get(r.getReqMessage()).getSources().add(tmpref);
						}
					}
				} else {
					newReqCtrlList.put(r.getReqMessage(), r);
				}
			}
		}		

		for(ReqCtrlListDataItem dup : duplicates){
			reqCtrlList.remove(dup);
		}
		
		
		for(Map.Entry<String, ReqCtrlListDataItem> dbKV : dbReqCtrlList.entrySet()){
			if(!newReqCtrlList.containsKey(dbKV.getKey())){
				// remove
				reqCtrlList.remove(dbKV.getValue());
			}
			else{
				// update
				ReqCtrlListDataItem newReqCtrlListDataItem = newReqCtrlList.get(dbKV.getKey());
				dbKV.getValue().setReqType(newReqCtrlListDataItem.getReqType());
				dbKV.getValue().setSources(newReqCtrlListDataItem.getSources());
				reqCtrlList.remove(newReqCtrlListDataItem);		
			}
		}
		
		//sort
		sort(reqCtrlList);
	}
	
	/**
	 * Sort by type
	 *
	 */
	private void sort(List<ReqCtrlListDataItem> reqCtrlList){
		
		Collections.sort(reqCtrlList, new Comparator<ReqCtrlListDataItem>(){
       	
		final int BEFORE = -1;
   	    final int EQUAL = 0;
   	    final int AFTER = 1;	
			
			@Override
			public int compare(ReqCtrlListDataItem r1, ReqCtrlListDataItem r2) {
				
				if (r1.getReqType() != null && r1.getReqType() != null) {
					if (r1.getReqType().equals(r2.getReqType())) {
						return EQUAL;
					}
					else if(r1.getReqType().equals(RequirementType.Forbidden)){
						return BEFORE;
					}
					else if(r2.getReqType().equals(RequirementType.Forbidden)){
						return AFTER;
					}
					else if(r1.getReqType().equals(RequirementType.Tolerated)){
						return BEFORE;
					}
					else{
						return AFTER;
					}
				}
				else if(r1.getReqType() != null){
					return BEFORE;
				}
				else if(r2.getReqType() != null){
					return AFTER;
				}
				
				return EQUAL;
			}
       });  
		
		int i = 0;
		for(ReqCtrlListDataItem r : reqCtrlList){
			r.setSort(i);
			i++;
		}
	}
}
