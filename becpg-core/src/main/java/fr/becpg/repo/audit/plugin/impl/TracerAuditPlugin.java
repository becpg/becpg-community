package fr.becpg.repo.audit.plugin.impl;

import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;

@Service
public class TracerAuditPlugin implements AuditPlugin {

	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.TRACER.equals(type);
	}

	@Override
	public boolean isDatabaseEnable() {
		return false;
	}
	
	@Override
	public boolean isStopWatchEnable() {
		return false;
	}

	@Override
	public boolean isTracerEnable() {
		return true;
	}

	@Override
	public Class<?> getAuditClass() {
		return getClass();
	}

}