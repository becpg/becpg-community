package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.Map;

import fr.becpg.repo.audit.model.AuditDataType;

public interface DatabaseAuditPlugin extends AuditPlugin {

	void beforeRecordAuditEntry(Map<String, Serializable> auditValues);

	void afterRecordAuditEntry(Map<String, Serializable> auditValues);

	String getAuditApplicationId();
	
	String getAuditApplicationPath();
	
	Map<String, AuditDataType> getKeyMap();

}