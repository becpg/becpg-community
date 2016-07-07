/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:reqCtrlList")
public class ReqCtrlListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = -3851143080201225383L;
	private RequirementType reqType;
	private MLText reqMlMessage;
	private Integer sort;
	private NodeRef charact;
	private List<NodeRef> sources = new LinkedList<>();
	private RequirementDataType reqDataType;

	@AlfProp
	@AlfQname(qname="bcpg:rclReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	public String getReqMessage() {
		return reqMlMessage!=null ? reqMlMessage.getDefaultValue() : null;
	}
	
	public String getKey() {
		String key = "key-";
		if(getReqMessage()!=null){
			key +=getReqMessage();
		} 
		if(reqDataType!=null){
			key+= reqDataType.toString();
		}
		return key;
	}
	
	
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:rclReqMessage")
	public MLText getReqMlMessage() {
		return reqMlMessage;
	}

	public void setReqMlMessage(MLText reqMlMessage) {
		this.reqMlMessage = reqMlMessage;
	}

	@AlfProp
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:rclCharact")
	public NodeRef getCharact() {
		return charact;
	}

	public void setCharact(NodeRef charact) {
		this.charact = charact;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:rclSources")
	public List<NodeRef> getSources() {
		return sources;
	}

	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}

	@AlfProp
	@AlfQname(qname="bcpg:rclDataType")
	public RequirementDataType getReqDataType() {
		return reqDataType;
	}

	public void setReqDataType(RequirementDataType reqDataType) {
		this.reqDataType = reqDataType != null ? reqDataType : RequirementDataType.Nutrient;
	}

	public ReqCtrlListDataItem() {
		super();
	}

	
	public ReqCtrlListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, NodeRef charact, List<NodeRef> sources, RequirementDataType reqDataType){
		super();
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMlMessage = new MLText(reqMessage);
		this.charact = charact;
		this.sources = sources;
		this.reqDataType = reqDataType != null ? reqDataType : RequirementDataType.Nutrient;
	}
	
	public ReqCtrlListDataItem(NodeRef nodeRef, RequirementType reqType, MLText reqMessage, NodeRef charact, List<NodeRef> sources, RequirementDataType reqDataType){
		super();
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMlMessage = reqMessage;
		this.charact = charact;
		this.sources = sources;
		this.reqDataType = reqDataType != null ? reqDataType : RequirementDataType.Nutrient;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charact == null) ? 0 : charact.hashCode());
		result = prime * result + ((reqDataType == null) ? 0 : reqDataType.hashCode());
		result = prime * result + ((reqMlMessage == null) ? 0 : reqMlMessage.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		result = prime * result + ((sources == null) ? 0 : sources.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReqCtrlListDataItem other = (ReqCtrlListDataItem) obj;
		if (charact == null) {
			if (other.charact != null)
				return false;
		} else if (!charact.equals(other.charact))
			return false;
		if (reqDataType != other.reqDataType)
			return false;
		if (reqMlMessage == null) {
			if (other.reqMlMessage != null)
				return false;
		} else if (!reqMlMessage.equals(other.reqMlMessage))
			return false;
		if (reqType != other.reqType)
			return false;
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
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
		return "ReqCtrlListDataItem [nodeRef=" + nodeRef + ", reqType=" + reqType + ", reqMessage=" + reqMlMessage + ", sources=" + sources + ", reqDataType="+reqDataType+"]";
	}
	
	
}
