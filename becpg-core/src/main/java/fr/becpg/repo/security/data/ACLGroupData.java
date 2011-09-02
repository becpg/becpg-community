package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;

public class ACLGroupData extends BeCPGDataObject {

	List<ACLEntryDataItem> acls = new ArrayList<ACLEntryDataItem>();

	public List<ACLEntryDataItem> getAcls() {
		return acls;
	}

	public void setAcls(List<ACLEntryDataItem> acls) {
		this.acls = acls;
	}
	
	
}
