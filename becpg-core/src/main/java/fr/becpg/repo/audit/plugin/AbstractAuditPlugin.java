package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.rest.api.Audit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

public abstract class AbstractAuditPlugin implements AuditPlugin, InitializingBean {
	
	private static final String STARTED_AT = "startedAt";
	private static final String COMPLETED_AT = "completedAt";
	private static final String DURATION = "duration";
	
	protected static final Map<String, String> KEY_MAP = new HashMap<>();

	static {
		KEY_MAP.put(STARTED_AT, "date");
		KEY_MAP.put(COMPLETED_AT, "date");
		KEY_MAP.put(DURATION, "int");
	}

	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;
	
	private boolean databaseEnable = false;
	
	private boolean stopWatchEnable = false;
	
	private boolean tracerEnable = false;
	
	private String auditParameters;
	
	protected void setAuditParameters(String auditParameters) {
		this.auditParameters = auditParameters;
	}
	
	@Override
	public boolean isDatabaseEnable() {
		return databaseEnable;
	}
	
	@Override
	public boolean isStopWatchEnable() {
		return stopWatchEnable;
	}
	
	@Override
	public boolean isTracerEnable() {
		return tracerEnable;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (auditParameters != null && !auditParameters.isBlank()) {
			String[] parameters = auditParameters.split(",");
			
			for (String parameter : parameters) {
				if ("stopwatch".equals(parameter)) {
					stopWatchEnable = true;
				} else if ("audit".equals(parameter)) {
					databaseEnable = true;
				} else if ("tracer".equals(parameter)) {
					tracerEnable = true;
				}
			}
		}
	}

	public Map<String, String> getStatisticsKeyMap() {
		return KEY_MAP;
	}
	
	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
	}
	
	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
	}
	
}
