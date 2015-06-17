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
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:reqCtrlList")
public class ReqCtrlListDataItem extends BeCPGDataObject {

	
	private RequirementType reqType;
	private String reqMessage;
	private Integer sort;
	private List<NodeRef> sources = new ArrayList<>();

	@AlfProp
	@AlfQname(qname="bcpg:rclReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	@AlfProp
	@AlfQname(qname="bcpg:rclReqMessage")
	public String getReqMessage() {
		return reqMessage;
	}

	public void setReqMessage(String reqMessage) {
		this.reqMessage = reqMessage;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:rclSources")
	public List<NodeRef> getSources() {
		return sources;
	}

	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}
	
	
	
	public ReqCtrlListDataItem() {
		super();
	}

	public ReqCtrlListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, List<NodeRef> sources){
		super();
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMessage = reqMessage;
		this.sources = sources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((reqMessage == null) ? 0 : reqMessage.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((sources == null) ? 0 : sources.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReqCtrlListDataItem other = (ReqCtrlListDataItem) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (reqMessage == null) {
			if (other.reqMessage != null)
				return false;
		} else if (!reqMessage.equals(other.reqMessage))
			return false;
		if (reqType != other.reqType)
			return false;
		if (sources == null) {
			if (other.sources != null)
				return false;
		} else if (!sources.equals(other.sources))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReqCtrlListDataItem [nodeRef=" + nodeRef + ", reqType=" + reqType + ", reqMessage=" + reqMessage + ", sources=" + sources + "]";
	}
	
	
}
