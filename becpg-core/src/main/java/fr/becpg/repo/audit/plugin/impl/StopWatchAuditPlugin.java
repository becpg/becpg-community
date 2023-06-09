package fr.becpg.repo.audit.plugin.impl;

import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;

@Service
public class StopWatchAuditPlugin implements AuditPlugin {

	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.STOPWATCH.equals(type);
	}

	@Override
	public boolean isDatabaseEnable() {
		return false;
	}
	
	@Override
	public boolean isStopWatchEnable() {
		return true;
	}

	@Override
	public boolean isTracerEnable() {
		return false;
	}

	@Override
	public Class<?> getAuditClass() {
		return getClass();
	}

}