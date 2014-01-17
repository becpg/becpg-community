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
package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BaseObject;

public abstract class AbstractProductDataView extends BaseObject {

	protected List<ReqCtrlListDataItem> reqCtrlList;
	protected List<DynamicCharactListItem> dynamicCharactList;
	
	public abstract List<? extends CompositionDataItem> getMainDataList();

	@DataList
	@AlfQname(qname="bcpg:reqCtrlList")
	public List<ReqCtrlListDataItem> getReqCtrlList() {
		return reqCtrlList;
	}

	public void setReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		this.reqCtrlList = reqCtrlList;
	}

	
	@DataList
	@AlfQname(qname="bcpg:dynamicCharactList")
	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	
}
