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
