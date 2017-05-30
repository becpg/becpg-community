/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

@AlfType
@AlfQname(qname = "bcpg:ingList")
public class IngListDataItem extends AbstractManualDataItem  implements SimpleCharactDataItem, AspectAwareDataItem, CompositeDataItem<IngListDataItem> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2710240943326822672L;

	private Double qtyPerc = 0d;
	
	private Double volumeQtyPerc;
	
	private List<NodeRef> geoOrigin = new LinkedList<>();
	
	private List<NodeRef> geoTransfo = new LinkedList<>();
	
	private List<NodeRef> bioOrigin = new LinkedList<>();
	
	private Boolean isGMO = false;
	
	private Boolean isIonized = false;	
	
	private NodeRef ing;
	
	private Boolean isManual;

	private Boolean isProcessingAid = false;
	
	private Boolean isSupport = false;
	
	private Integer depthLevel;
	
	private IngListDataItem parent;
	
    private Double mini;
	
	private Double maxi;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:ingListQtyPerc")
	public Double getQtyPerc() {
		return qtyPerc;
	}
	
	public void setQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
	}
	
	
	@AlfProp
	@AlfQname(qname="bcpg:ingListQtyMini")
	public Double getMini() {
		return mini;
	}
	
	public void setMini(Double mini) {
		this.mini = mini;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:ingListQtyMaxi")
	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	@AlfProp
	@AlfQname(qname="bcpg:ingListVolumeQtyPerc")
	public Double getVolumeQtyPerc() {
		return volumeQtyPerc;
	}


	public void setVolumeQtyPerc(Double volumeQtyPerc) {
		this.volumeQtyPerc = volumeQtyPerc;
	}


	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingListGeoOrigin")
	public List<NodeRef> getGeoOrigin() {
		return geoOrigin;
	}
	

	public void setGeoOrigin(List<NodeRef> geoOrigin) {
		this.geoOrigin = geoOrigin;
	}
	
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingListGeoTransfo")
	public List<NodeRef> getGeoTransfo() {
		return geoTransfo;
	}

	public void setGeoTransfo(List<NodeRef> geoTransfo) {
		this.geoTransfo = geoTransfo;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingListBioOrigin")
	public List<NodeRef> getBioOrigin() {
		return bioOrigin;
	}
	

	public void setBioOrigin(List<NodeRef> bioOrigin) {
		this.bioOrigin = bioOrigin;
	}

	@AlfProp
	@AlfQname(qname="bcpg:ingListIsGMO")
	public Boolean getIsGMO() {
		return isGMO;
	}
	

	public void setIsGMO(Boolean isGMO) {
		this.isGMO = isGMO;
	}
	
	
	
	@AlfProp
	@AlfQname(qname="bcpg:ingListIsProcessingAid")
	public Boolean getIsProcessingAid() {
		return isProcessingAid;
	}


	public void setIsProcessingAid(Boolean isProcessingAid) {
		this.isProcessingAid = isProcessingAid;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:ingListIsSupport")
	public Boolean getIsSupport() {
		return isSupport;
	}

	public void setIsSupport(Boolean isSupport) {
		this.isSupport = isSupport;
	}
	
	
	

	@AlfProp
	@AlfQname(qname="bcpg:ingListIsIonized")
	public Boolean getIsIonized() {
		return isIonized;
	}
	

	

	public void setIsIonized(Boolean isIonized) {
		this.isIonized = isIonized;
	}
	
	
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname="bcpg:ingListIng")
	@InternalField
	public NodeRef getIng() {
		return ing;
	}
	

	public void setIng(NodeRef ing) {
		this.ing = ing;
	}
	
	//////////////////////////////
	
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return ing;
	}

	@Override
	public Double getValue() {
		return qtyPerc;
	}

	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setIng(nodeRef);
		
	}


	@Override
	public void setValue(Double value) {
		setQtyPerc(value);
		
	}
	
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:parentLevel")
	public IngListDataItem getParent() {
		return this.parent;
	}


	@Override
	public void setParent(IngListDataItem parent) {
		this.parent = parent;		
	}
	
	/**
	 * Instantiates a new ing list data item.
	 */
	public IngListDataItem()
	{
		super();
	}
	

	public IngListDataItem(NodeRef nodeRef,	Double qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized, Boolean processingAid, NodeRef ing, Boolean isManual)
	{
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
	
	public IngListDataItem(NodeRef nodeRef, IngListDataItem ingList, Double qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> geoTransfo, List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized, Boolean processingAid, Boolean isSupport, NodeRef ing, Boolean isManual)
	{
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
	 * @param i
	 */
	public IngListDataItem(IngListDataItem i){
		
		setNodeRef(i.getNodeRef());
		setQtyPerc(i.getQtyPerc());
		setGeoOrigin(i.getGeoOrigin());
		setBioOrigin(i.getBioOrigin());
		setIsGMO(i.getIsGMO());
		setIsIonized(i.getIsIonized());
		setIng(i.getIng());
		setIsManual(i.getIsManual());
		setIsProcessingAid(i.getIsProcessingAid());
		setIsSupport(i.getIsSupport());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bioOrigin == null) ? 0 : bioOrigin.hashCode());
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((geoOrigin == null) ? 0 : geoOrigin.hashCode());
		result = prime * result + ((geoTransfo == null) ? 0 : geoTransfo.hashCode());
		result = prime * result + ((ing == null) ? 0 : ing.hashCode());
		result = prime * result + ((isGMO == null) ? 0 : isGMO.hashCode());
		result = prime * result + ((isIonized == null) ? 0 : isIonized.hashCode());
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
		result = prime * result + ((isProcessingAid == null) ? 0 : isProcessingAid.hashCode());
		result = prime * result + ((isSupport == null) ? 0 : isSupport.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((parent == null || parent == this) ? 0 : parent.hashCode());
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
		result = prime * result + ((volumeQtyPerc == null) ? 0 : volumeQtyPerc.hashCode());
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
		IngListDataItem other = (IngListDataItem) obj;
		if (bioOrigin == null) {
			if (other.bioOrigin != null)
				return false;
		} else if (!bioOrigin.equals(other.bioOrigin))
			return false;
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (geoOrigin == null) {
			if (other.geoOrigin != null)
				return false;
		} else if (!geoOrigin.equals(other.geoOrigin))
			return false;
		if (geoTransfo == null) {
			if (other.geoTransfo != null)
				return false;
		} else if (!geoTransfo.equals(other.geoTransfo))
			return false;
		if (ing == null) {
			if (other.ing != null)
				return false;
		} else if (!ing.equals(other.ing))
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
		if (isManual == null) {
			if (other.isManual != null)
				return false;
		} else if (!isManual.equals(other.isManual))
			return false;
		if (isProcessingAid == null) {
			if (other.isProcessingAid != null)
				return false;
		} else if (!isProcessingAid.equals(other.isProcessingAid))
			return false;
		if (isSupport == null) {
			if (other.isSupport != null)
				return false;
		} else if (!isSupport.equals(other.isSupport))
			return false;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (mini == null) {
			if (other.mini != null)
				return false;
		} else if (!mini.equals(other.mini))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
			return false;
		if (volumeQtyPerc == null) {
			if (other.volumeQtyPerc != null)
				return false;
		} else if (!volumeQtyPerc.equals(other.volumeQtyPerc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IngListDataItem [qtyPerc=" + qtyPerc + ", volumeQtyPerc=" + volumeQtyPerc + ", geoOrigin=" + geoOrigin + ", geoTransfo=" + geoTransfo
				+ ", bioOrigin=" + bioOrigin + ", isGMO=" + isGMO + ", isIonized=" + isIonized + ", ing=" + ing + ", isManual=" + isManual
				+ ", isProcessingAid=" + isProcessingAid + ", isSupport=" + isSupport + ", depthLevel=" + depthLevel + ", parent=" + parent
				+ ", mini=" + mini + ", maxi=" + maxi + "]";
	}
}
