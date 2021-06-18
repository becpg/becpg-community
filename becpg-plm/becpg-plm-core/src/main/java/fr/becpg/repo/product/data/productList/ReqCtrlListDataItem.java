/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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

/**
 * <p>ReqCtrlListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:reqCtrlList")
public class ReqCtrlListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = -3851143080201225383L;
	private RequirementType reqType;
	private MLText reqMlMessage;
	private Double reqMaxQty;
	private NodeRef charact;
	private List<NodeRef> sources = new LinkedList<>();
	private RequirementDataType reqDataType;
	private String regulatoryCode;
	
	// Do not put in hashCode and equals
	private String formulationChainId;
	// Do not put in hashCode and equals
	private Integer sort;
	
	
	
	/**
	 * <p>Getter for the field <code>formulationChainId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:rclFormulationChainId")
	public String getFormulationChainId() {
		return formulationChainId;
	}

	/**
	 * <p>Setter for the field <code>formulationChainId</code>.</p>
	 *
	 * @param formulationChainId a {@link java.lang.String} object.
	 */
	public void setFormulationChainId(String formulationChainId) {
		this.formulationChainId = formulationChainId;
	}

	
	/**
	 * <p>Getter for the field <code>rclReqMaxQty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:rclReqMaxQty")
	public Double getReqMaxQty() {
		return reqMaxQty;
	}

	/**
	 * <p>Setter for the field <code>rclReqMaxQty</code>.</p>
	 *
	 * @param reqMaxQty a {@link java.lang.Double} object.
	 */
	public void setReqMaxQty(Double reqMaxQty) {
		this.reqMaxQty = reqMaxQty;
	}

	/**
	 * <p>Getter for the field <code>reqType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:rclReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	/**
	 * <p>Setter for the field <code>reqType</code>.</p>
	 *
	 * @param reqType a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 */
	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	/**
	 * <p>getReqMessage.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getReqMessage() {
		return reqMlMessage!=null ? MLTextHelper.getClosestValue(reqMlMessage, Locale.getDefault()) : null;
	}
	
	/**
	 * <p>getKey.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
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
	
	
	/**
	 * <p>Getter for the field <code>reqMlMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:rclReqMessage")
	public MLText getReqMlMessage() {
		return reqMlMessage;
	}

	/**
	 * <p>Setter for the field <code>reqMlMessage</code>.</p>
	 *
	 * @param reqMlMessage a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setReqMlMessage(MLText reqMlMessage) {
		this.reqMlMessage = reqMlMessage;
	}

	/**
	 * <p>Getter for the field <code>sort</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	/**
	 * <p>Setter for the field <code>sort</code>.</p>
	 *
	 * @param sort a {@link java.lang.Integer} object.
	 */
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	/**
	 * <p>Getter for the field <code>charact</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:rclCharact")
	public NodeRef getCharact() {
		return charact;
	}

	/**
	 * <p>Setter for the field <code>charact</code>.</p>
	 *
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setCharact(NodeRef charact) {
		this.charact = charact;
	}

	/**
	 * <p>Getter for the field <code>sources</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:rclSources")
	public List<NodeRef> getSources() {
		return sources;
	}

	/**
	 * <p>Setter for the field <code>sources</code>.</p>
	 *
	 * @param sources a {@link java.util.List} object.
	 */
	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}

	/**
	 * <p>Getter for the field <code>reqDataType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementDataType} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:rclDataType")
	public RequirementDataType getReqDataType() {
		return reqDataType;
	}

	/**
	 * <p>Setter for the field <code>reqDataType</code>.</p>
	 *
	 * @param reqDataType a {@link fr.becpg.repo.product.data.constraints.RequirementDataType} object.
	 */
	public void setReqDataType(RequirementDataType reqDataType) {
		this.reqDataType = reqDataType != null ? reqDataType : RequirementDataType.Nutrient;
	}
	
	
	/**
	 * <p>Getter for the field <code>regulatoryCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryCode")
	public String getRegulatoryCode() {
		return regulatoryCode;
	}

	/**
	 * <p>Setter for the field <code>regulatoryCode</code>.</p>
	 *
	 * @param regulatoryCode a {@link java.lang.String} object.
	 */
	public void setRegulatoryCode(String regulatoryCode) {
		this.regulatoryCode = regulatoryCode;
	}

	/**
	 * <p>Constructor for ReqCtrlListDataItem.</p>
	 */
	public ReqCtrlListDataItem() {
		super();
	}


	/**
	 * <p>Constructor for ReqCtrlListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reqType a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 * @param reqMessage a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param sources a {@link java.util.List} object.
	 * @param reqDataType a {@link fr.becpg.repo.product.data.constraints.RequirementDataType} object.
	 */
	public ReqCtrlListDataItem(NodeRef nodeRef, RequirementType reqType, MLText reqMessage, NodeRef charact, List<NodeRef> sources, RequirementDataType reqDataType){
		super();
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMlMessage = reqMessage;
		this.charact = charact;
		this.sources = sources;
		this.reqDataType = reqDataType != null ? reqDataType : RequirementDataType.Nutrient;
	}
	

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(charact, reqMaxQty, regulatoryCode, reqDataType, reqMlMessage, reqType, sources);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReqCtrlListDataItem other = (ReqCtrlListDataItem) obj;
		return Objects.equals(charact, other.charact) && Objects.equals(reqMaxQty, other.reqMaxQty)
				&& Objects.equals(regulatoryCode, other.regulatoryCode) && reqDataType == other.reqDataType
				&& Objects.equals(reqMlMessage, other.reqMlMessage) && reqType == other.reqType && Objects.equals(sources, other.sources);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ReqCtrlListDataItem [reqType=" + reqType + ", reqMlMessage=" + reqMlMessage + ", rclReqMaxQty=" + reqMaxQty + ", charact="
				+ charact + ", sources=" + sources + ", reqDataType=" + reqDataType + ", regulatoryCode=" + regulatoryCode + ", formulationChainId="
				+ formulationChainId + ", sort=" + sort + "]";
	}
	
	
}
