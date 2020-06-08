/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
import java.util.Locale;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
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
	private String regulatoryCode;
	private String formulationChainId;
	
	
	
	
	@AlfProp
	@AlfQname(qname="bcpg:rclFormulationChainId")
	public String getFormulationChainId() {
		return formulationChainId;
	}

	public void setFormulationChainId(String formulationChainId) {
		this.formulationChainId = formulationChainId;
	}

	@AlfProp
	@AlfQname(qname="bcpg:rclReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	public String getReqMessage() {
		return reqMlMessage!=null ? MLTextHelper.getClosestValue(reqMlMessage, Locale.getDefault()) : null;
	}
	
	public String getKey() {
		String key = "key-";
		if(getReqMessage()!=null){
			key +=getReqMessage();
		} 
		if(reqType!=null){
			key+= reqType.toString();
		}
		if(reqDataType!=null){
			key+= reqDataType.toString();
		}
		if(regulatoryCode!=null){
			key+= regulatoryCode.toString();
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
	
	
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryCode")
	public String getRegulatoryCode() {
		return regulatoryCode;
	}

	public void setRegulatoryCode(String regulatoryCode) {
		this.regulatoryCode = regulatoryCode;
	}

	public ReqCtrlListDataItem() {
		super();
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
		result = prime * result + Objects.hash(charact, regulatoryCode, reqDataType, reqMlMessage, reqType);
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
		return Objects.equals(charact, other.charact) && Objects.equals(regulatoryCode, other.regulatoryCode) && reqDataType == other.reqDataType
				&& Objects.equals(reqMlMessage, other.reqMlMessage) && reqType == other.reqType;
	}

	@Override
	public String toString() {
		return "ReqCtrlListDataItem [reqType=" + reqType + ", reqMlMessage=" + reqMlMessage + ", sort=" + sort + ", charact=" + charact + ", sources="
				+ sources + ", reqDataType=" + reqDataType + ", regulatoryCode=" + regulatoryCode + ", formulationChainId=" + formulationChainId
				+ "]";
	}
	
	
}
