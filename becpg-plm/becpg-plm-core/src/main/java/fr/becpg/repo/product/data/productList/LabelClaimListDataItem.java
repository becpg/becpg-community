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
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.CopiableDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>LabelClaimListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:labelClaimList")
public class LabelClaimListDataItem extends AbstractManualDataItem implements SimpleCharactDataItem, CopiableDataItem, AspectAwareDataItem, RegulatoryEntityItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1232488781703843974L;
	/** Constant <code>VALUE_TRUE="true"</code> */
	public static final String VALUE_TRUE = "true";
	/** Constant <code>VALUE_FALSE="false"</code> */
	public static final String VALUE_FALSE = "false";
	/** Constant <code>VALUE_EMPTY=""</code> */
	public static final String VALUE_EMPTY = "";
	/** Constant <code>VALUE_NA="na"</code> */
	public static final String VALUE_NA = "na";
	/** Constant <code>VALUE_SUITABLE="suitable"</code> */
	public static final String VALUE_SUITABLE = "suitable";
	/** Constant <code>VALUE_SUITABLE="suitable"</code> */
	public static final String VALUE_CERTIFIED = "certified";
	
	private NodeRef labelClaim;
	private String type;
	private String labelClaimValue;
	private Double percentClaim;
	private Double percentApplicable;
	private Boolean isFormulated;
	private String errorLog;
	private RequirementType regulatoryType;
	private MLText regulatoryMessage;
	private List<NodeRef> missingLabelClaims = new ArrayList<>();
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();
	
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
	 * <p>Getter for the field <code>regulatoryType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryType")
	public RequirementType getRegulatoryType() {
		return regulatoryType;
	}

	/** {@inheritDoc} */
	public void setRegulatoryType(RequirementType regulatoryType) {
		this.regulatoryType = regulatoryType;
	}

	/**
	 * <p>Getter for the field <code>regulatoryMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:regulatoryText")
	public MLText getRegulatoryMessage() {
		return regulatoryMessage;
	}

	/** {@inheritDoc} */
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		this.regulatoryMessage = regulatoryMessage;
	}

	/**
	 * <p>Getter for the field <code>labelClaim</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:lclLabelClaim")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getLabelClaim() {
		return labelClaim;
	}
	/**
	 * <p>Setter for the field <code>labelClaim</code>.</p>
	 *
	 * @param labelClaim a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setLabelClaim(NodeRef labelClaim) {
		this.labelClaim = labelClaim;
	}
	
	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lclType")
	public String getType() {
		return type;
	}
	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * <p>Getter for the field <code>labelClaimValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lclClaimValue")
	public String getLabelClaimValue() {
		return labelClaimValue;
	}
	/**
	 * <p>Setter for the field <code>labelClaimValue</code>.</p>
	 *
	 * @param labelClaimValue a {@link java.lang.String} object.
	 */
	public void setLabelClaimValue(String labelClaimValue) {
		this.labelClaimValue = labelClaimValue;
	}
	
	
	
	/**
	 * <p>Getter for the field <code>percentClaim</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lclPercentClaim")
	public Double getPercentClaim() {
		return percentClaim;
	}
	/**
	 * <p>Setter for the field <code>percentClaim</code>.</p>
	 *
	 * @param percentClaim a {@link java.lang.Double} object
	 */
	public void setPercentClaim(Double percentClaim) {
		this.percentClaim = percentClaim;
	}
	
	/**
	 * <p>Getter for the field <code>percentApplicable</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lclPercentApplicable")
	public Double getPercentApplicable() {
		return percentApplicable;
	}
	/**
	 * <p>Setter for the field <code>percentApplicable</code>.</p>
	 *
	 * @param percentApplicable a {@link java.lang.Double} object
	 */
	public void setPercentApplicable(Double percentApplicable) {
		this.percentApplicable = percentApplicable;
	}
	/**
	 * <p>getIsClaimed.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getIsClaimed() {
		return VALUE_TRUE.equals(labelClaimValue) || VALUE_CERTIFIED.equals(labelClaimValue);
	}
	
	/**
	 * <p>setIsClaimed.</p>
	 *
	 * @param isClaimed a {@link java.lang.Boolean} object.
	 */
	public void setIsClaimed(Boolean isClaimed) {
		if(Boolean.TRUE.equals(isClaimed)){
			this.labelClaimValue = VALUE_TRUE;
		} else  {
			this.labelClaimValue = VALUE_FALSE;
		}
	}
	
	/**
	 * <p>Getter for the field <code>isFormulated</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:lclIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}
	
	/**
	 * <p>Setter for the field <code>isFormulated</code>.</p>
	 *
	 * @param isFormulated a {@link java.lang.Boolean} object.
	 */
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}
	
	/**
	 * <p>Getter for the field <code>errorLog</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:lclFormulaErrorLog")
	public String getErrorLog() {
		return errorLog;
	}
	/**
	 * <p>Setter for the field <code>errorLog</code>.</p>
	 *
	 * @param errorLog a {@link java.lang.String} object.
	 */
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	/**
	 * <p>Getter for the field <code>missingLabelClaims</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname="bcpg:lclMissingLabelClaims")
	public List<NodeRef> getMissingLabelClaims() {
		return missingLabelClaims;
	}
	/**
	 * <p>Setter for the field <code>missingLabelClaims</code>.</p>
	 *
	 * @param missingLabelClaims a {@link java.util.List} object.
	 */
	public void setMissingLabelClaims(List<NodeRef> missingLabelClaims) {
		this.missingLabelClaims = missingLabelClaims;
	}
	
	/**
	 * <p>withMissingLabelClaims.</p>
	 *
	 * @param missingLabelClaims a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withMissingLabelClaims(List<NodeRef> missingLabelClaims) {
		setMissingLabelClaims(missingLabelClaims);
		return this;
	}
	
	/**
	 * <p>withLabelClaim.</p>
	 *
	 * @param labelClaim a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withLabelClaim(NodeRef labelClaim) {
		setLabelClaim(labelClaim);
		return this;
	}
	
	/**
	 * <p>withType.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withType(String type) {
		setType(type);
		return this;
	}
	
	/**
	 * <p>withLabelClaimValue.</p>
	 *
	 * @param labelClaimValue a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withLabelClaimValue(String labelClaimValue) {
		setLabelClaimValue(labelClaimValue);
		return this;
	}
	
	/**
	 * <p>withIsClaimed.</p>
	 *
	 * @param isClaimed a {@link java.lang.Boolean} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withIsClaimed(Boolean isClaimed) {
		setIsClaimed(isClaimed);
		return this;
	}
	

	public LabelClaimListDataItem withIsCertified(Boolean isCertified) {
		if(Boolean.TRUE.equals(isCertified)){
			this.labelClaimValue = VALUE_CERTIFIED;
		} 
		return this;
	}
	
	/**
	 * <p>withPercentClaim.</p>
	 *
	 * @param percentClaim a {@link java.lang.Double} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withPercentClaim(Double percentClaim) {
		setPercentClaim(percentClaim);
		return this;
	}
	
	/**
	 * <p>withPercentApplicable.</p>
	 *
	 * @param percentApplicable a {@link java.lang.Double} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withPercentApplicable(Double percentApplicable) {
		setPercentApplicable(percentApplicable);
		return this;
	}
	
	/**
	 * <p>withIsFormulated.</p>
	 *
	 * @param isFormulated a {@link java.lang.Boolean} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withIsFormulated(Boolean isFormulated) {
		setIsFormulated(isFormulated);
		return this;
	}
	
	/**
	 * <p>withRegulatoryCountriesRef.</p>
	 *
	 * @param regulatoryCountriesRef a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withRegulatoryCountriesRef(List<NodeRef> regulatoryCountriesRef) {
		setRegulatoryCountriesRef(regulatoryCountriesRef);
		return this;
	}
	
	/**
	 * <p>withRegulatoryUsagesRef.</p>
	 *
	 * @param regulatoryUsagesRef a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withRegulatoryUsagesRef(List<NodeRef> regulatoryUsagesRef) {
		setRegulatoryUsagesRef(regulatoryUsagesRef);
		return this;
	}
	
	/**
	 * <p>withRegulatoryType.</p>
	 *
	 * @param regulatoryType a {@link fr.becpg.repo.product.data.constraints.RequirementType} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withRegulatoryType(RequirementType regulatoryType) {
		setRegulatoryType(regulatoryType);
		return this;
	}
	
	/**
	 * <p>withRegulatoryMessage.</p>
	 *
	 * @param regulatoryMessage a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem withRegulatoryMessage(MLText regulatoryMessage) {
		setRegulatoryMessage(regulatoryMessage);
		return this;
	}
	/**
	 * <p>Constructor for LabelClaimListDataItem.</p>
	 */
	public LabelClaimListDataItem(){
		super();
	}
	
	/**
	 * Creates a new builder for LabelClaimListDataItem
	 * @return a new builder instance
	 */
	public static LabelClaimListDataItem build() {
		return new LabelClaimListDataItem();
	}
	
	
	/**
	 * <p>Constructor for LabelClaimListDataItem.</p>
	 *
	 * @param labelClaim a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param type a {@link java.lang.String} object.
	 * @param isClaimed a {@link java.lang.Boolean} object.
	 */
	@Deprecated
	public LabelClaimListDataItem(NodeRef labelClaim, String type, Boolean isClaimed) {
		super();
		this.labelClaim = labelClaim;
		this.type = type;
		this.labelClaimValue = Boolean.TRUE.equals(isClaimed) ? VALUE_TRUE : VALUE_FALSE;
	}
	
	/**
	 * <p>Constructor for LabelClaimListDataItem.</p>
	 *
	 * @param labelClaimItem a {@link fr.becpg.repo.product.data.productList.LabelClaimListDataItem} object.
	 */
	public LabelClaimListDataItem(LabelClaimListDataItem labelClaimItem) {
		super(labelClaimItem);
		this.labelClaim = labelClaimItem.labelClaim;
		this.type = labelClaimItem.type;
		this.labelClaimValue = labelClaimItem.labelClaimValue;
		this.isFormulated = labelClaimItem.isFormulated;
		this.regulatoryCountriesRef = new ArrayList<>(labelClaimItem.regulatoryCountriesRef);
		this.regulatoryUsagesRef = new ArrayList<>(labelClaimItem.regulatoryUsagesRef);
		this.regulatoryMessage = labelClaimItem.regulatoryMessage;
		this.regulatoryType = labelClaimItem.regulatoryType;
	
	}
	
	
	/** {@inheritDoc} */
	@Override
	public LabelClaimListDataItem copy() {
		return new LabelClaimListDataItem(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(isFormulated, labelClaim, type);
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
		LabelClaimListDataItem other = (LabelClaimListDataItem) obj;
		return Objects.equals(isFormulated, other.isFormulated) && Objects.equals(labelClaim, other.labelClaim) && Objects.equals(type, other.type);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelClaimListDataItem [labelClaim=" + labelClaim + ", type=" + type + ", labelClaimValue=" + labelClaimValue + ", percentClaim="
				+ percentClaim + ", percentApplicable=" + percentApplicable + ", isFormulated=" + isFormulated + ", errorLog=" + errorLog
				+ ", missingLabelClaims=" + missingLabelClaims + ", isManual=" + isManual + ", sort=" + sort + ", nodeRef=" + nodeRef
				+ ", parentNodeRef=" + parentNodeRef + ", name=" + name + "]";
	}
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setLabelClaim(nodeRef);
	}
	
	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		setPercentClaim(value);
	}
	/** {@inheritDoc} */
	@Override
	public NodeRef getCharactNodeRef() {
		return getLabelClaim();
	}
	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return getPercentClaim();
	}
	
	/** {@inheritDoc} */
	@Override
	public Boolean shouldDetailIfZero() {
		return true;
	}

		

}
