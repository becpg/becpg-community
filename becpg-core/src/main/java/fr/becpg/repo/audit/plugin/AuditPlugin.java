package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.Map;

import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;

public interface AuditPlugin {

	boolean applyTo(AuditType type);

	String getAuditApplicationId();
	
	String getAuditApplicationPath();
	
	Map<String, AuditDataType> getStatisticsKeyMap();

	boolean isDatabaseEnable();
	
	boolean isStopWatchEnable();
	
	boolean isTracerEnable();

	Class<?> getAuditClass();

	void beforeRecordAuditEntry(Map<String, Serializable> auditValues);

	void afterRecordAuditEntry(Map<String, Serializable> auditValues);

}
