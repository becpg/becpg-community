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
package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;

@AlfType
@AlfQname(qname = "sec:aclGroup")
public class ACLGroupData extends BeCPGDataObject {

	private String nodeType;
	
	List<ACLEntryDataItem> acls = new ArrayList<ACLEntryDataItem>();

	@DataList
	@AlfQname(qname = "sec:aclEntry")
	public List<ACLEntryDataItem> getAcls() {
		return acls;
	}

	public void setAcls(List<ACLEntryDataItem> acls) {
		this.acls = acls;
	}

	@AlfProp
	@AlfQname(qname = "sec:nodeType")
	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType =  nodeType;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((acls == null) ? 0 : acls.hashCode());
		result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
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
		ACLGroupData other = (ACLGroupData) obj;
		if (acls == null) {
			if (other.acls != null)
				return false;
		} else if (!acls.equals(other.acls))
			return false;
		if (nodeType == null) {
			if (other.nodeType != null)
				return false;
		} else if (!nodeType.equals(other.nodeType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ACLGroupData [nodeType=" + nodeType + ", acls=" + acls + "]";
	}
	
	
}
