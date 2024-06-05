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

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8044333209643017576L;
	private RequirementType reqType;
	private MLText reqMessage;
	private Double qtyPercMaxi;
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
	@AlfQname(qname="bcpg:filReqType")
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
	 * <p>Getter for the field <code>reqMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:filReqMessage")
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
	 * <p>Getter for the field <code>qtyPercMaxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:filQtyPercMaxi")
	public Double getQtyPercMaxi() {
		return qtyPercMaxi;
	}
	
	/**
	 * <p>Getter for the field <code>qtyPercMaxiUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:filQtyPercMaxiUnit")
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
	 * <p>Getter for the field <code>isGMO</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:filIsGMO")
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
		this.isGMO = isGMO!=null ? isGMO.toString() : null;
	}


	/**
	 * <p>Getter for the field <code>isIonized</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:filIsIonized")
	public String getIsIonized() {
		return isIonized;
	}
	
	/**
	 * <p>Setter for the field <code>isIonized</code>.</p>
	 *
	 * @param isIonized a {@link java.lang.Boolean} object.
	 */
	public void setIsIonized(Boolean isIonized) {
		this.isIonized  = isIonized!=null ? isIonized.toString() : null;
	}
	
	/**
	 * <p>Setter for the field <code>isIonized</code>.</p>
	 *
	 * @param isIonized a {@link java.lang.String} object.
	 */
	public void setIsIonized(String isIonized) {
		this.isIonized  = isIonized;
	}
	

	/**
	 * <p>Getter for the field <code>ings</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:filIngs")
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
	@AlfQname(qname="bcpg:filGeoOrigins")
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
	@AlfQname(qname="bcpg:filRequiredGeoOrigins")
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
	@AlfQname(qname="bcpg:filGeoTransfo")
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
	@AlfQname(qname="bcpg:filBioOrigins")
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
	public ForbiddenIngListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, Double qtyPercMaxi, Boolean isGMO, Boolean isIonized, List<NodeRef> ings, List<NodeRef> geoOrigins, List<NodeRef> bioOrigins)
	{
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
	public ForbiddenIngListDataItem(NodeRef nodeRef, RequirementType reqType, MLText reqMessage, Double qtyPercMaxi, Boolean isGMO, Boolean isIonized, List<NodeRef> ings, List<NodeRef> geoOrigins, List<NodeRef> bioOrigins)
	{
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

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bioOrigins == null) ? 0 : bioOrigins.hashCode());
		result = prime * result + ((geoOrigins == null) ? 0 : geoOrigins.hashCode());
		result = prime * result + ((geoTransfo == null) ? 0 : geoTransfo.hashCode());
		result = prime * result + ((ings == null) ? 0 : ings.hashCode());
		result = prime * result + ((isGMO == null) ? 0 : isGMO.hashCode());
		result = prime * result + ((isIonized == null) ? 0 : isIonized.hashCode());
		result = prime * result + ((qtyPercMaxi == null) ? 0 : qtyPercMaxi.hashCode());
		result = prime * result + ((reqMessage == null) ? 0 : reqMessage.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((requiredGeoOrigins == null) ? 0 : requiredGeoOrigins.hashCode());
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
		if (bioOrigins == null) {
			if (other.bioOrigins != null)
				return false;
		} else if (!bioOrigins.equals(other.bioOrigins))
			return false;
		if (geoOrigins == null) {
			if (other.geoOrigins != null)
				return false;
		} else if (!geoOrigins.equals(other.geoOrigins))
			return false;
		if (geoTransfo == null) {
			if (other.geoTransfo != null)
				return false;
		} else if (!geoTransfo.equals(other.geoTransfo))
			return false;
		if (ings == null) {
			if (other.ings != null)
				return false;
		} else if (!ings.equals(other.ings))
			return false;
		if (isGMO == null) {
			if (other.isGMO != null)
				return false;
		} else if (!isGMO.equals(other.isGMO))
			return false;
		if (isIonized == null) {
			if (other.isIonized != null)
				return false;
		} else if (!isIonized.equals(other.isIonized))
			return false;
		if (qtyPercMaxi == null) {
			if (other.qtyPercMaxi != null)
				return false;
		} else if (!qtyPercMaxi.equals(other.qtyPercMaxi))
			return false;
		if (reqMessage == null) {
			if (other.reqMessage != null)
				return false;
		} else if (!reqMessage.equals(other.reqMessage))
			return false;
		if (reqType != other.reqType)
			return false;
		if (requiredGeoOrigins == null) {
			if (other.requiredGeoOrigins != null)
				return false;
		} else if (!requiredGeoOrigins.equals(other.requiredGeoOrigins))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ForbiddenIngListDataItem [reqType=" + reqType + ", reqMessage=" + reqMessage + ", qtyPercMaxi=" + qtyPercMaxi + ", isGMO=" + isGMO
				+ ", isIonized=" + isIonized + ", ings=" + ings + ", geoOrigins=" + geoOrigins + ", requiredGeoOrigins=" + requiredGeoOrigins
				+ ", geoTransfo=" + geoTransfo + ", bioOrigins=" + bioOrigins + "]";
	}	
	
	
	
}
