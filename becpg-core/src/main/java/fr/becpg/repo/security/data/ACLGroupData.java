package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;

public class ACLGroupData extends BeCPGDataObject {

	private QName nodeType;
	
	private List<QName> nodeAspects; 
	
	List<ACLEntryDataItem> acls = new ArrayList<ACLEntryDataItem>();

	public List<ACLEntryDataItem> getAcls() {
		return acls;
	}

	public void setAcls(List<ACLEntryDataItem> acls) {
		this.acls = acls;
	}

	public QName getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = DefaultTypeConverter.INSTANCE.convert(QName.class, nodeType);
	}

	public List<QName> getNodeAspects() {
		return nodeAspects;
	}

	public void setNodeAspects(List<String> nodeAspects) {
		this.nodeAspects = new ArrayList<QName>();
		if(nodeAspects!=null){
			for(String tmp : nodeAspects){
				this.nodeAspects .add(DefaultTypeConverter.INSTANCE.convert(QName.class, tmp));
			}
		}
		
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acls == null) ? 0 : acls.hashCode());
		result = prime * result + ((nodeAspects == null) ? 0 : nodeAspects.hashCode());
		result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACLGroupData other = (ACLGroupData) obj;
		if (acls == null) {
			if (other.acls != null)
				return false;
		} else if (!acls.equals(other.acls))
			return false;
		if (nodeAspects == null) {
			if (other.nodeAspects != null)
				return false;
		} else if (!nodeAspects.equals(other.nodeAspects))
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
		return "ACLGroupData [nodeType=" + nodeType + ", nodeAspects=" + nodeAspects + ", acls=" + acls + "]";
	}
	
	

	

	
	
	
}
