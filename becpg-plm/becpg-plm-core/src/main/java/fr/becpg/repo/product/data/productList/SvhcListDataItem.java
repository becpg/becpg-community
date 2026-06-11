/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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

import fr.becpg.repo.regulatory.RegulatoryEntityItem;
import fr.becpg.repo.regulatory.RequirementType;
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
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * <p>SvhcListDataItem class.</p>
 *
 * @author frederic
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:svhcList")
public class SvhcListDataItem extends AbstractManualDataItem implements SimpleListDataItem, MinMaxValueDataItem, SimpleCharactDataItem, AspectAwareDataItem, RegulatoryEntityItem {

	/** Constant <code>serialVersionUID=-2710240943326822672L</code> */
	private static final long serialVersionUID = -2710240943326822672L;

	private Double qtyPerc;
	private Double migrationPerc;
	private NodeRef ing;
	private List<String> reasonsForInclusion;
	private Double mini;
	private Double maxi;

	private RequirementType regulatoryType;
	private MLText regulatoryMessage;
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>qtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:svhcListQtyPerc")
	public Double getQtyPerc() {
		return qtyPerc;
	}

	/**
	 * <p>Setter for the field <code>qtyPerc</code>.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object.
	 */
	public void setQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
	}
	
	/**
	 * <p>Getter for the field <code>migrationPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:svhcListMigrationPerc")
	public Double getMigrationPerc() {
		return migrationPerc;
	}

	/**
	 * <p>Setter for the field <code>migrationPerc</code>.</p>
	 *
	 * @param migrationPerc a {@link java.lang.Double} object
	 */
	public void setMigrationPerc(Double migrationPerc) {
		this.migrationPerc = migrationPerc;
	}

	/**
	 * <p>Getter for the field <code>ing</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:svhcListIng")
	@InternalField
	public NodeRef getIng() {
		return ing;
	}

	/**
	 * <p>Setter for the field <code>ing</code>.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setIng(NodeRef ing) {
		this.ing = ing;
	}

	/**
	 * <p>
	 * Getter for the field <code>reasonsForInclusion</code>.
	 * </p>
	 *
	 * @return a list of reasons for inclusion of the substance in the list of svhc.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:svhcReasonsForInclusion")
	public List<String> getReasonsForInclusion() {
		return reasonsForInclusion;
	}

	/**
	 * <p>
	 * Setter for the field <code>reasonsForInclusion</code>.
	 * </p>
	 *
	 * @param reasonsForInclusion a {@link java.util.List} object
	 */
	public void setReasonsForInclusion(List<String> reasonsForInclusion) {
		this.reasonsForInclusion = reasonsForInclusion;
	}

	//////////////////////////////

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getIng();
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return getQtyPerc();
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setIng(nodeRef);

	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		setQtyPerc(value);
	}
	

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:svhcListMini")
	public Double getMini() {
		return mini;
	}

	/** {@inheritDoc} */
	@Override
	public void setMini(Double mini) {
		this.mini = mini;
	}
	

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:svhcListMaxi")
	public Double getMaxi() {
		if(maxi == null) {
			return qtyPerc;
		}
		return maxi;
	}

	/** {@inheritDoc} */
	@Override
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	/**
	 * Instantiates a new ing list data item.
	 */
	public SvhcListDataItem() {
		super();
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 */
	public static SvhcListDataItem build() {
		return new SvhcListDataItem();
	}

	/**
	 * <p>withIngredient.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 */
	public SvhcListDataItem withIngredient(NodeRef ing) {
		this.ing = ing;
		return this;
	}

	/**
	 * <p>withQtyPerc.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 */
	public SvhcListDataItem withQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
		return this;
	}
	

	/**
	 * <p>withMigrationPerc.</p>
	 *
	 * @param migrationPerc a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 */
	public SvhcListDataItem withMigrationPerc(Double migrationPerc) {
		this.migrationPerc = migrationPerc;
		return this;
	}

	/**
	 * <p>isManual.</p>
	 *
	 * @param isManual a boolean
	 * @return a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 */
	public SvhcListDataItem isManual(boolean isManual) {
		this.isManual = isManual;
		return this;
	}

	/**
	 * <p>withReasonsForInclusion.</p>
	 *
	 * @param reasonsForInclusion a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 */
	public SvhcListDataItem withReasonsForInclusion(List<String> reasonsForInclusion) {
		this.reasonsForInclusion = reasonsForInclusion;
		return this;
	}

	/**
	 * <p>Getter for the field <code>regulatoryCountriesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@Override
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsagesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@Override
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}

	/**
	 * <p>Getter for the field <code>regulatoryType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RequirementType} object
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryType")
	public RequirementType getRegulatoryType() {
		return regulatoryType;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryType(RequirementType regulatoryType) {
		this.regulatoryType = regulatoryType;
	}

	/**
	 * <p>Getter for the field <code>regulatoryMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@Override
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:regulatoryText")
	public MLText getRegulatoryMessage() {
		return regulatoryMessage;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		this.regulatoryMessage = regulatoryMessage;
	}

	/**
	 * Copy constructor
	 *
	 * @param i a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object.
	 */
	public SvhcListDataItem(SvhcListDataItem i) {
		super(i);
		this.qtyPerc = i.qtyPerc;
		this.ing = i.ing;
		this.reasonsForInclusion = i.reasonsForInclusion;
		this.migrationPerc = i.migrationPerc;
		this.regulatoryType = i.regulatoryType;
		this.regulatoryMessage = i.regulatoryMessage;
		this.mini = i.mini;
		this.maxi = i.maxi;
		if (i.regulatoryCountriesRef != null) {
			this.regulatoryCountriesRef = new ArrayList<>(i.regulatoryCountriesRef);
		}
		if (i.regulatoryUsagesRef != null) {
			this.regulatoryUsagesRef = new ArrayList<>(i.regulatoryUsagesRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public SvhcListDataItem copy() {
		SvhcListDataItem ret = new SvhcListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(ing, migrationPerc, qtyPerc, reasonsForInclusion, mini, maxi);
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
		SvhcListDataItem other = (SvhcListDataItem) obj;
		return Objects.equals(ing, other.ing) && Objects.equals(migrationPerc, other.migrationPerc) && Objects.equals(qtyPerc, other.qtyPerc)
				&& Objects.equals(reasonsForInclusion, other.reasonsForInclusion) && Objects.equals(mini, other.mini) && Objects.equals(maxi, other.maxi);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SvhcListDataItem [qtyPerc=" + qtyPerc + ", migrationPerc=" + migrationPerc + ", ing=" + ing + ", reasonsForInclusion="
				+ reasonsForInclusion + "]";
	}

	/** {@inheritDoc} */
	@Override
	public Boolean shouldDetailIfZero() {
		return true;
	}

}
