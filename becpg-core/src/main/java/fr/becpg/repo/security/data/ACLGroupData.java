package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.namespace.QName;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;

public class ACLGroupData extends BeCPGDataObject {

	private QName nodeType;
	
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

	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	

	
	
	
}
