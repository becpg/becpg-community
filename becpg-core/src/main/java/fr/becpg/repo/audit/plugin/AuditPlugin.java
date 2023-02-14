package fr.becpg.repo.audit.plugin;

import fr.becpg.repo.audit.model.AuditType;

public interface AuditPlugin {

	boolean applyTo(AuditType type);

	boolean isDatabaseEnable();
	
	boolean isStopWatchEnable();
	
	boolean isTracerEnable();

	Class<?> getAuditClass();

}
