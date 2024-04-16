package fr.becpg.repo.audit.plugin;

import fr.becpg.repo.audit.model.AuditType;

public interface AuditPlugin {

	public static final String ID = "id";
	public static final String STARTED_AT = "startedAt";
	public static final String COMPLETED_AT = "completedAt";
	public static final String DURATION = "duration";

	boolean applyTo(AuditType type);

	boolean isDatabaseEnable();
	
	boolean isStopWatchEnable();
	
	boolean isTracerEnable();

	Class<?> getAuditedClass();

}
