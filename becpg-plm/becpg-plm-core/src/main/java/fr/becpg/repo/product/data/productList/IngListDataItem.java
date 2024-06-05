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
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>IngListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:ingList")
@MultiLevelDataList
public class IngListDataItem extends AbstractManualDataItem
		implements SimpleCharactDataItem, AspectAwareDataItem, CompositeDataItem<IngListDataItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2710240943326822672L;

	private Double qtyPerc = 0d;

	private Double qtyPercWithYield = null;

	private Double qtyPercWithSecondaryYield = null;

	private Double volumeQtyPerc;

	private List<NodeRef> geoOrigin = new LinkedList<>();

	private List<NodeRef> geoTransfo = new LinkedList<>();

	private List<NodeRef> bioOrigin = new LinkedList<>();

	private List<NodeRef> claims = new LinkedList<>();

	private Boolean isGMO = false;

	private Boolean isIonized = false;

	private NodeRef ing;

	private Boolean isProcessingAid = false;

	private Boolean isSupport = false;

	private Integer depthLevel;

	private IngListDataItem parent;

	private Double mini;

	private Double maxi;

	private DeclarationType declType = DeclarationType.Detail;

	/**
	 * <p>Getter for the field <code>qtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListQtyPerc")
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
	 * <p>Getter for the field <code>qtyPercWithYield</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListQtyPercWithYield")
	public Double getQtyPercWithYield() {
		return qtyPercWithYield;
	}

	/**
	 * <p>Setter for the field <code>qtyPercWithYield</code>.</p>
	 *
	 * @param qtyPercWithYield a {@link java.lang.Double} object
	 */
	public void setQtyPercWithYield(Double qtyPercWithYield) {
		this.qtyPercWithYield = qtyPercWithYield;
	}

	/**
	 * <p>Getter for the field <code>qtyPercWithSecondaryYield</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListQtyPercWithSecondaryYield")
	public Double getQtyPercWithSecondaryYield() {
		return qtyPercWithSecondaryYield;
	}

	/**
	 * <p>Setter for the field <code>qtyPercWithSecondaryYield</code>.</p>
	 *
	 * @param qtyPercWithSecondaryYield a {@link java.lang.Double} object
	 */
	public void setQtyPercWithSecondaryYield(Double qtyPercWithSecondaryYield) {
		this.qtyPercWithSecondaryYield = qtyPercWithSecondaryYield;
	}

	/**
	 * <p>Getter for the field <code>mini</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListQtyMini")
	public Double getMini() {
		return mini;
	}

	/**
	 * <p>Setter for the field <code>mini</code>.</p>
	 *
	 * @param mini a {@link java.lang.Double} object.
	 */
	public void setMini(Double mini) {
		this.mini = mini;
	}

	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListQtyMaxi")
	public Double getMaxi() {
		return maxi;
	}

	/**
	 * <p>Setter for the field <code>maxi</code>.</p>
	 *
	 * @param maxi a {@link java.lang.Double} object.
	 */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	/**
	 * <p>Getter for the field <code>volumeQtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListVolumeQtyPerc")
	public Double getVolumeQtyPerc() {
		return volumeQtyPerc;
	}

	/**
	 * <p>Setter for the field <code>volumeQtyPerc</code>.</p>
	 *
	 * @param volumeQtyPerc a {@link java.lang.Double} object.
	 */
	public void setVolumeQtyPerc(Double volumeQtyPerc) {
		this.volumeQtyPerc = volumeQtyPerc;
	}

	/**
	 * <p>Getter for the field <code>geoOrigin</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:ingListGeoOrigin")
	public List<NodeRef> getGeoOrigin() {
		return geoOrigin;
	}

	/**
	 * <p>Setter for the field <code>geoOrigin</code>.</p>
	 *
	 * @param geoOrigin a {@link java.util.List} object.
	 */
	public void setGeoOrigin(List<NodeRef> geoOrigin) {
		this.geoOrigin = geoOrigin;
	}

	/**
	 * <p>Getter for the field <code>geoTransfo</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:ingListGeoTransfo")
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
	 * <p>Getter for the field <code>bioOrigin</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:ingListBioOrigin")
	public List<NodeRef> getBioOrigin() {
		return bioOrigin;
	}

	/**
	 * <p>Setter for the field <code>bioOrigin</code>.</p>
	 *
	 * @param bioOrigin a {@link java.util.List} object.
	 */
	public void setBioOrigin(List<NodeRef> bioOrigin) {
		this.bioOrigin = bioOrigin;
	}

	/**
	 * <p>Getter for the field <code>claims</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:ingListClaims")
	public List<NodeRef> getClaims() {
		return claims;
	}

	/**
	 * <p>Setter for the field <code>claims</code>.</p>
	 *
	 * @param claims a {@link java.util.List} object
	 */
	public void setClaims(List<NodeRef> claims) {
		this.claims = claims;
	}

	/**
	 * <p>Getter for the field <code>isGMO</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListIsGMO")
	public Boolean getIsGMO() {
		return isGMO;
	}

	/**
	 * <p>Setter for the field <code>isGMO</code>.</p>
	 *
	 * @param isGMO a {@link java.lang.Boolean} object.
	 */
	public void setIsGMO(Boolean isGMO) {
		this.isGMO = isGMO;
	}

	/**
	 * <p>Getter for the field <code>isProcessingAid</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListIsProcessingAid")
	public Boolean getIsProcessingAid() {
		return isProcessingAid;
	}

	/**
	 * <p>Setter for the field <code>isProcessingAid</code>.</p>
	 *
	 * @param isProcessingAid a {@link java.lang.Boolean} object.
	 */
	public void setIsProcessingAid(Boolean isProcessingAid) {
		this.isProcessingAid = isProcessingAid;
	}

	/**
	 * <p>Getter for the field <code>isSupport</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListIsSupport")
	public Boolean getIsSupport() {
		return isSupport;
	}

	/**
	 * <p>Setter for the field <code>isSupport</code>.</p>
	 *
	 * @param isSupport a {@link java.lang.Boolean} object.
	 */
	public void setIsSupport(Boolean isSupport) {
		this.isSupport = isSupport;
	}

	/**
	 * <p>Getter for the field <code>isIonized</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListIsIonized")
	public Boolean getIsIonized() {
		return isIonized;
	}

	/**
	 * <p>Setter for the field <code>isIonized</code>.</p>
	 *
	 * @param isIonized a {@link java.lang.Boolean} object.
	 */
	public void setIsIonized(Boolean isIonized) {
		this.isIonized = isIonized;
	}

	/**
	 * <p>Getter for the field <code>ing</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc = false)
	@AlfQname(qname = "bcpg:ingListIng")
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
	 * Getter for the field <code>declType</code>.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.DeclarationType}
	 *         object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingListDeclType")
	public DeclarationType getDeclType() {
		return declType;
	}

	/**
	 * <p>
	 * Setter for the field <code>declType</code>.
	 * </p>
	 *
	 * @param declType
	 *            a
	 *            {@link fr.becpg.repo.product.data.constraints.DeclarationType}
	 *            object.
	 */
	public void setDeclType(DeclarationType declType) {
		if (declType == null) {
			declType = DeclarationType.Detail;
		}

		this.declType = declType;
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
	@InternalField
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public IngListDataItem getParent() {
		return this.parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(IngListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * Instantiates a new ing list data item.
	 */
	public IngListDataItem() {
		super();
	}
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	public static IngListDataItem build() {
		return new IngListDataItem();
	}

	/**
	 * <p>withIngredient.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	public IngListDataItem withIngredient(NodeRef ing) {
		this.ing = ing;
		return this;
	}

	/**
	 * <p>withQtyPerc.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	public IngListDataItem withQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
		return this;
	}
	

	/**
	 * <p>Constructor for IngListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qtyPerc a {@link java.lang.Double} object.
	 * @param geoOrigin a {@link java.util.List} object.
	 * @param bioOrigin a {@link java.util.List} object.
	 * @param isGMO a {@link java.lang.Boolean} object.
	 * @param isIonized a {@link java.lang.Boolean} object.
	 * @param processingAid a {@link java.lang.Boolean} object.
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param isManual a {@link java.lang.Boolean} object.
	 */
	@Deprecated
	public IngListDataItem(NodeRef nodeRef, Double qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized,
			Boolean processingAid, NodeRef ing, Boolean isManual) {
		setNodeRef(nodeRef);
		setQtyPerc(qtyPerc);
		setGeoOrigin(geoOrigin);
		setBioOrigin(bioOrigin);
		setIsGMO(isGMO);
		setIsIonized(isIonized);
		setIng(ing);
		setIsManual(isManual);
		setIsProcessingAid(processingAid);
	}

	/**
	 * <p>Constructor for IngListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param ingList a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 * @param qtyPerc a {@link java.lang.Double} object.
	 * @param geoOrigin a {@link java.util.List} object.
	 * @param geoTransfo a {@link java.util.List} object.
	 * @param bioOrigin a {@link java.util.List} object.
	 * @param isGMO a {@link java.lang.Boolean} object.
	 * @param isIonized a {@link java.lang.Boolean} object.
	 * @param processingAid a {@link java.lang.Boolean} object.
	 * @param isSupport a {@link java.lang.Boolean} object.
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param isManual a {@link java.lang.Boolean} object.
	 */
	@Deprecated
	public IngListDataItem(NodeRef nodeRef, IngListDataItem ingList, Double qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> geoTransfo,
			List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized, Boolean processingAid, Boolean isSupport, NodeRef ing, Boolean isManual) {
		setNodeRef(nodeRef);
		setParent(ingList);
		setQtyPerc(qtyPerc);
		setGeoOrigin(geoOrigin);
		setGeoTransfo(geoTransfo);
		setBioOrigin(bioOrigin);
		setIsGMO(isGMO);
		setIsIonized(isIonized);
		setIng(ing);
		setIsManual(isManual);
		setIsProcessingAid(processingAid);
		setIsSupport(isSupport);
	}

	/**
	 * Copy contructor
	 *
	 * @param i a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 */
	public IngListDataItem(IngListDataItem i) {
		super(i);
		this.qtyPerc = i.qtyPerc;
		this.qtyPercWithYield = i.qtyPercWithYield;
		this.qtyPercWithSecondaryYield = i.qtyPercWithSecondaryYield;
		this.volumeQtyPerc = i.volumeQtyPerc;
		this.geoOrigin = this.geoOrigin != null ? new LinkedList<>(i.geoOrigin) : null;
		this.geoTransfo = this.geoTransfo != null ? new LinkedList<>(i.geoTransfo) : null;
		this.bioOrigin = this.bioOrigin != null ? new LinkedList<>(i.bioOrigin) : null;
		this.claims = this.claims != null ? new LinkedList<>(i.claims) : null;
		this.isGMO = i.isGMO;
		this.isIonized = i.isIonized;
		this.ing = i.ing;
		this.isProcessingAid = i.isProcessingAid;
		this.isSupport = i.isSupport;
		this.depthLevel = i.depthLevel;
		this.parent = i.parent;
		this.mini = i.mini;
		this.maxi = i.maxi;
		this.declType = i.declType;
	}

	/** {@inheritDoc} */
	@Override
	public IngListDataItem copy() {
		IngListDataItem ret = new IngListDataItem(this);
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
		result = prime * result + Objects.hash(bioOrigin, claims, depthLevel, geoOrigin, geoTransfo, ing, isGMO, isIonized, isProcessingAid,
				isSupport, maxi, mini, parent, qtyPerc, qtyPercWithYield, qtyPercWithSecondaryYield, volumeQtyPerc);
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
		IngListDataItem other = (IngListDataItem) obj;
		return Objects.equals(bioOrigin, other.bioOrigin) && Objects.equals(claims, other.claims) && Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(geoOrigin, other.geoOrigin) && Objects.equals(geoTransfo, other.geoTransfo) && Objects.equals(ing, other.ing)
				&& Objects.equals(isGMO, other.isGMO) && Objects.equals(isIonized, other.isIonized)
				&& Objects.equals(isProcessingAid, other.isProcessingAid) && Objects.equals(isSupport, other.isSupport)
				&& Objects.equals(maxi, other.maxi) && Objects.equals(mini, other.mini) && Objects.equals(parent, other.parent)
				&& Objects.equals(qtyPerc, other.qtyPerc) && Objects.equals(qtyPercWithYield, other.qtyPercWithYield)
				&& Objects.equals(qtyPercWithSecondaryYield, other.qtyPercWithSecondaryYield) && Objects.equals(volumeQtyPerc, other.volumeQtyPerc);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IngListDataItem [qtyPerc=" + qtyPerc + ", qtyPercWithYield=" + qtyPercWithYield + ", qtyPercWithSecondaryYield="
				+ qtyPercWithSecondaryYield + ", volumeQtyPerc=" + volumeQtyPerc + ", geoOrigin=" + geoOrigin + ", geoTransfo=" + geoTransfo
				+ ", bioOrigin=" + bioOrigin + ", claims=" + claims + ", isGMO=" + isGMO + ", isIonized=" + isIonized + ", ing=" + ing
				+ ", isProcessingAid=" + isProcessingAid + ", isSupport=" + isSupport + ", depthLevel=" + depthLevel + ", parent=" + parent
				+ ", mini=" + mini + ", maxi=" + maxi + "]";
	}

	/** {@inheritDoc} */
	@Override
	public Boolean shouldDetailIfZero() {
		return true;
	}
}
