package fr.becpg.repo.audit.plugin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import fr.becpg.repo.audit.model.AuditDataType;

public abstract class AbstractAuditPlugin implements AuditPlugin, InitializingBean {
	
	protected static final Map<String, AuditDataType> KEY_MAP = new HashMap<>();

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

	public Map<String, AuditDataType> getKeyMap() {
		return KEY_MAP;
	}
	
}
