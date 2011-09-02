package fr.becpg.repo.security.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;

public class SecurityServiceImpl implements SecurityService {

	private Map<String,NodeRef> acls = new HashMap<String, NodeRef>(); 
	
	
	private BeCPGDao<ACLGroupData> aclGroupDao;

	public void setAclGroupDao(BeCPGDao<ACLGroupData> aclGroupDao) {
		this.aclGroupDao = aclGroupDao;
	}

	@Override
	public int computeAccessMode(QName nodeType, String name) {
		String key = computeAclKey(nodeType,name);
		if(acls.containsKey(key)){
			NodeRef aclEntry =  acls.get(key);
			
			//TODO check permission
			
		}
		
		return SecurityService.WRITE_ACCESS;
	}

	private String computeAclKey(QName nodeType, String name) {
		return 	nodeType.getLocalName()+"_"+name;
	}
	
	public void init(){
		computeAcl();
	}

	public void computeAcl() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
