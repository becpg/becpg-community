package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.BeCPGDataObject;
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

	

	
	
	
}
