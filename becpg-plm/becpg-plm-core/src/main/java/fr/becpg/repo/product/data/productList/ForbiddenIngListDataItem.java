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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.RegulatoryEntityItem;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ForbiddenIngListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:forbiddenIngList")
public class ForbiddenIngListDataItem extends BeCPGDataObject implements RegulatoryEntityItem {

	public enum ReqQtyPercType{
		
		 QtyPercWithYield,QtyPercWithSecondaryYield,QtyPerc1,QtyPerc2,QtyPerc3,QtyPerc4,QtyPerc5,Mini,Maxi;
		
		public static ReqQtyPercType fromString(String type) {
			try {
				return ReqQtyPercType.valueOf(type);
			} catch (IllegalArgumentException | NullPointerException e) {
				return null;
			}
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8044333209643017576L;
	private RequirementType reqType;
	private MLText reqMessage;
	private Double qtyPercMaxi;
	private Double qtyPercMini;
	private ReqQtyPercType qtyPercType;
	private Integer ingLevel;
	private String qtyPercMaxiUnit;
	private String isGMO;
	private String isIonized;
	private List<NodeRef> ings = new ArrayList<>();
	private List<NodeRef> geoOrigins = new ArrayList<>();
	private List<NodeRef> requiredGeoOrigins = new ArrayList<>();
	private List<NodeRef> geoTransfo = new ArrayList<>();
	private List<NodeRef> bioOrigins = new ArrayList<>();
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();

	/**
	 * <p>Setter for the field <code>qtyPercMaxiUnit</code>.</p>
	 *
	 * @param qtyPercMaxiUnit a {@link java.lang.String} object
	 */
	public void setQtyPercMaxiUnit(String qtyPercMaxiUnit) {
		this.qtyPercMaxiUnit = qtyPercMaxiUnit;
	}

	/**
	 * <p>Getter for the field <code>regulatoryCountriesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/** {@inheritDoc} */
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsagesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/** {@inheritDoc} */
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}

	/**
	 * <p>Getter for the field <code>reqType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filReqType")
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
	 * <p>Getter for the field <code>ingLevel</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filIngLevel")
	public Integer getIngLevel() {
		return ingLevel;
	}

	/**
	 * <p>Setter for the field <code>ingLevel</code>.</p>
	 *
	 * @param ingLevel a {@link java.lang.Integer} object
	 */
	public void setIngLevel(Integer ingLevel) {
		this.ingLevel = ingLevel;
	}

	/**
	 * <p>Getter for the field <code>reqMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:filReqMessage")
	public MLText getReqMessage() {
		return reqMessage;
	}

	/**
	 * <p>Setter for the field <code>reqMessage</code>.</p>
	 *
	 * @param reqMessage a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setReqMessage(MLText reqMessage) {
		this.reqMessage = reqMessage;
	}

	/**
	 * <p>getRegulatoryType.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 */
	public RequirementType getRegulatoryType() {
		return getReqType();
	}

	/** {@inheritDoc} */
	public void setRegulatoryType(RequirementType regulatoryType) {
		setReqType(regulatoryType);
	}

	/**
	 * <p>getRegulatoryMessage.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public MLText getRegulatoryMessage() {
		return getReqMessage();
	}

	/** {@inheritDoc} */
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		setReqMessage(regulatoryMessage);
	}

	/**
	 * <p>Getter for the field <code>qtyPercMaxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filQtyPercMaxi")
	public Double getQtyPercMaxi() {
		return qtyPercMaxi;
	}
	
	

	/**
	 * <p>Getter for the field <code>qtyPercMaxiUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filQtyPercMaxiUnit")
	public String getQtyPercMaxiUnit() {
		return qtyPercMaxiUnit;
	}

	/**
	 * <p>Setter for the field <code>qtyPercMaxi</code>.</p>
	 *
	 * @param qtyPercMaxi a {@link java.lang.Double} object.
	 */
	public void setQtyPercMaxi(Double qtyPercMaxi) {
		this.qtyPercMaxi = qtyPercMaxi;
	}

	
	/**
	 * <p>Getter for the field <code>qtyPercMini</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filQtyPercMini")
	public Double getQtyPercMini() {
		return qtyPercMini;
	}

	/**
	 * <p>Setter for the field <code>qtyPercMini</code>.</p>
	 *
	 * @param qtyPercMini a {@link java.lang.Double} object
	 */
	public void setQtyPercMini(Double qtyPercMini) {
		this.qtyPercMini = qtyPercMini;
	}

	/**
	 * <p>Getter for the field <code>qtyPercType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem.ReqQtyPercType} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filQtyPercType")
	public ReqQtyPercType getQtyPercType() {
		return qtyPercType;
	}

	/**
	 * <p>Setter for the field <code>qtyPercType</code>.</p>
	 *
	 * @param qtyPercType a {@link fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem.ReqQtyPercType} object
	 */
	public void setQtyPercType(ReqQtyPercType qtyPercType) {
		this.qtyPercType = qtyPercType;
	}

	/**
	 * <p>Getter for the field <code>isGMO</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filIsGMO")
	public String getIsGMO() {
		return isGMO;
	}

	/**
	 * <p>Setter for the field <code>isGMO</code>.</p>
	 *
	 * @param isGMO a {@link java.lang.String} object.
	 */
	public void setIsGMO(String isGMO) {
		this.isGMO = isGMO;
	}

	/**
	 * <p>Setter for the field <code>isGMO</code>.</p>
	 *
	 * @param isGMO a {@link java.lang.Boolean} object.
	 */
	public void setIsGMO(Boolean isGMO) {
		this.isGMO = isGMO != null ? isGMO.toString() : null;
	}

	/**
	 * <p>Getter for the field <code>isIonized</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:filIsIonized")
	public String getIsIonized() {
		return isIonized;
	}

	/**
	 * <p>Setter for the field <code>isIonized</code>.</p>
	 *
	 * @param isIonized a {@link java.lang.Boolean} object.
	 */
	public void setIsIonized(Boolean isIonized) {
		this.isIonized = isIonized != null ? isIonized.toString() : null;
	}

	/**
	 * <p>Setter for the field <code>isIonized</code>.</p>
	 *
	 * @param isIonized a {@link java.lang.String} object.
	 */
	public void setIsIonized(String isIonized) {
		this.isIonized = isIonized;
	}

	/**
	 * <p>Getter for the field <code>ings</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:filIngs")
	public List<NodeRef> getIngs() {
		return ings;
	}

	/**
	 * <p>Setter for the field <code>ings</code>.</p>
	 *
	 * @param ings a {@link java.util.List} object.
	 */
	public void setIngs(List<NodeRef> ings) {
		this.ings = ings;
	}

	/**
	 * <p>Getter for the field <code>geoOrigins</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:filGeoOrigins")
	public List<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	/**
	 * <p>Setter for the field <code>geoOrigins</code>.</p>
	 *
	 * @param geoOrigins a {@link java.util.List} object.
	 */
	public void setGeoOrigins(List<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}

	/**
	 * <p>Getter for the field <code>requiredGeoOrigins</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:filRequiredGeoOrigins")
	public List<NodeRef> getRequiredGeoOrigins() {
		return requiredGeoOrigins;
	}

	/**
	 * <p>Setter for the field <code>requiredGeoOrigins</code>.</p>
	 *
	 * @param requiredGeoOrigins a {@link java.util.List} object.
	 */
	public void setRequiredGeoOrigins(List<NodeRef> requiredGeoOrigins) {
		this.requiredGeoOrigins = requiredGeoOrigins;
	}

	/**
	 * <p>Getter for the field <code>geoTransfo</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:filGeoTransfo")
	public List<NodeRef> getGeoTransfo() {
		return geoTransfo;
	}

	/**
	 * <p>Setter for the field <code>geoTransfo</code>.</p>
	 *
	 * @param geoTransfo a {@link java.util.List} object.
	 */
	public void setGeoTransfo(List<NodeRef> geoTransfo) {
		this.geoTransfo = geoTransfo;
	}

	/**
	 * <p>Getter for the field <code>bioOrigins</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:filBioOrigins")
	public List<NodeRef> getBioOrigins() {
		return bioOrigins;
	}

	/**
	 * <p>Setter for the field <code>bioOrigins</code>.</p>
	 *
	 * @param bioOrigins a {@link java.util.List} object.
	 */
	public void setBioOrigins(List<NodeRef> bioOrigins) {
		this.bioOrigins = bioOrigins;
	}

	/**
	 * <p>Constructor for ForbiddenIngListDataItem.</p>
	 */
	public ForbiddenIngListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ForbiddenIngListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reqType a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 * @param reqMessage a {@link java.lang.String} object.
	 * @param qtyPercMaxi a {@link java.lang.Double} object.
	 * @param isGMO a {@link java.lang.Boolean} object.
	 * @param isIonized a {@link java.lang.Boolean} object.
	 * @param ings a {@link java.util.List} object.
	 * @param geoOrigins a {@link java.util.List} object.
	 * @param bioOrigins a {@link java.util.List} object.
	 */
	public ForbiddenIngListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, Double qtyPercMaxi, Boolean isGMO, Boolean isIonized,
			List<NodeRef> ings, List<NodeRef> geoOrigins, List<NodeRef> bioOrigins) {
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMessage = new MLText(reqMessage);
		this.qtyPercMaxi = qtyPercMaxi;
		this.geoOrigins = geoOrigins;
		this.bioOrigins = bioOrigins;
		setIsGMO(isGMO);
		setIsIonized(isIonized);
		this.ings = ings;
	}

	/**
	 * <p>Constructor for ForbiddenIngListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reqType a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 * @param reqMessage a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param qtyPercMaxi a {@link java.lang.Double} object.
	 * @param isGMO a {@link java.lang.Boolean} object.
	 * @param isIonized a {@link java.lang.Boolean} object.
	 * @param ings a {@link java.util.List} object.
	 * @param geoOrigins a {@link java.util.List} object.
	 * @param bioOrigins a {@link java.util.List} object.
	 */
	public ForbiddenIngListDataItem(NodeRef nodeRef, RequirementType reqType, MLText reqMessage, Double qtyPercMaxi, Boolean isGMO, Boolean isIonized,
			List<NodeRef> ings, List<NodeRef> geoOrigins, List<NodeRef> bioOrigins) {
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMessage = reqMessage;
		this.qtyPercMaxi = qtyPercMaxi;
		this.geoOrigins = geoOrigins;
		this.bioOrigins = bioOrigins;
		setIsGMO(isGMO);
		setIsIonized(isIonized);
		this.ings = ings;
	}


	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem} object
	 */
	public static ForbiddenIngListDataItem build() {
		return new ForbiddenIngListDataItem();
	}

	/**
	 * <p>withIngs.</p>
	 *
	 * @param ings a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem} object
	 */
	public ForbiddenIngListDataItem withIngs(List<NodeRef> ings) {
		this.ings = ings;
		return this;
	}
	
	
	/**
	 * <p>withQtyPercMaxi.</p>
	 *
	 * @param qtyPercMaxi a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem} object
	 */
	public ForbiddenIngListDataItem withQtyPercMaxi(Double qtyPercMaxi) {
		this.qtyPercMaxi = qtyPercMaxi;
		return this;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(bioOrigins, geoOrigins, geoTransfo, ingLevel, ings, isGMO, isIonized, qtyPercMaxi, qtyPercMaxiUnit,
				qtyPercMini, qtyPercType, regulatoryCountriesRef, regulatoryUsagesRef, reqMessage, reqType, requiredGeoOrigins);
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
		ForbiddenIngListDataItem other = (ForbiddenIngListDataItem) obj;
		return Objects.equals(bioOrigins, other.bioOrigins) && Objects.equals(geoOrigins, other.geoOrigins)
				&& Objects.equals(geoTransfo, other.geoTransfo) && Objects.equals(ingLevel, other.ingLevel) && Objects.equals(ings, other.ings)
				&& Objects.equals(isGMO, other.isGMO) && Objects.equals(isIonized, other.isIonized) && Objects.equals(qtyPercMaxi, other.qtyPercMaxi)
				&& Objects.equals(qtyPercMaxiUnit, other.qtyPercMaxiUnit) && Objects.equals(qtyPercMini, other.qtyPercMini)
				&& qtyPercType == other.qtyPercType && Objects.equals(regulatoryCountriesRef, other.regulatoryCountriesRef)
				&& Objects.equals(regulatoryUsagesRef, other.regulatoryUsagesRef) && Objects.equals(reqMessage, other.reqMessage)
				&& reqType == other.reqType && Objects.equals(requiredGeoOrigins, other.requiredGeoOrigins);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ForbiddenIngListDataItem [reqType=" + reqType + ", reqMessage=" + reqMessage + ", qtyPercMaxi=" + qtyPercMaxi + ", qtyPercMini="
				+ qtyPercMini + ", qtyPercType=" + qtyPercType + ", ingLevel=" + ingLevel + ", qtyPercMaxiUnit=" + qtyPercMaxiUnit + ", isGMO="
				+ isGMO + ", isIonized=" + isIonized + ", ings=" + ings + ", geoOrigins=" + geoOrigins + ", requiredGeoOrigins=" + requiredGeoOrigins
				+ ", geoTransfo=" + geoTransfo + ", bioOrigins=" + bioOrigins + ", regulatoryCountriesRef=" + regulatoryCountriesRef
				+ ", regulatoryUsagesRef=" + regulatoryUsagesRef + "]";
	}

}
