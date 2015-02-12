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

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:labelClaimList")
public class LabelClaimListDataItem extends AbstractManualDataItem {

	private NodeRef labelClaim;
	private String type;
	private Boolean isClaimed;
	private Boolean isFormulated;
	private String errorLog;
	
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:lclLabelClaim")
	@DataListIdentifierAttr
	public NodeRef getLabelClaim() {
		return labelClaim;
	}
	public void setLabelClaim(NodeRef labelClaim) {
		this.labelClaim = labelClaim;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lclType")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lclIsClaimed")
	public Boolean getIsClaimed() {
		return isClaimed;
	}
	public void setIsClaimed(Boolean isClaimed) {
		this.isClaimed = isClaimed;
	}
	
	
	
	@AlfProp
	@AlfQname(qname="bcpg:lclIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}
	
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lclFormulaErrorLog")
	public String getErrorLog() {
		return errorLog;
	}
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	
	public LabelClaimListDataItem(){
		super();
	}
	
	
	public LabelClaimListDataItem(NodeRef labelClaim, String type, Boolean isClaimed) {
		super();
		this.labelClaim = labelClaim;
		this.type = type;
		this.isClaimed = isClaimed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((errorLog == null) ? 0 : errorLog.hashCode());
		result = prime * result + ((isClaimed == null) ? 0 : isClaimed.hashCode());
		result = prime * result + ((isFormulated == null) ? 0 : isFormulated.hashCode());
		result = prime * result + ((labelClaim == null) ? 0 : labelClaim.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		LabelClaimListDataItem other = (LabelClaimListDataItem) obj;
		if (errorLog == null) {
			if (other.errorLog != null)
				return false;
		} else if (!errorLog.equals(other.errorLog))
			return false;
		if (isClaimed == null) {
			if (other.isClaimed != null)
				return false;
		} else if (!isClaimed.equals(other.isClaimed))
			return false;
		if (isFormulated == null) {
			if (other.isFormulated != null)
				return false;
		} else if (!isFormulated.equals(other.isFormulated))
			return false;
		if (labelClaim == null) {
			if (other.labelClaim != null)
				return false;
		} else if (!labelClaim.equals(other.labelClaim))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "LabelClaimListDataItem [labelClaim=" + labelClaim + ", type=" + type + ", isClaimed=" + isClaimed + ", isFormulated=" + isFormulated + ", errorLog=" + errorLog
				+ "]";
	}
		

}
