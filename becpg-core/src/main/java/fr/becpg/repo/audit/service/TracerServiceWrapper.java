package fr.becpg.repo.audit.service;

import java.util.Map;

public class TracerServiceWrapper {

	private TracerAuditService tracerAuditService;

	private AutoCloseable tracerScope;
	
	private String scopeName;
	
	public TracerServiceWrapper(TracerAuditService tracerAuditService, String scopeName) {
		this.tracerAuditService = tracerAuditService;
		this.scopeName = scopeName;
	}

	public void start() {
		tracerScope = tracerAuditService.start(scopeName);
	}

	public void stop() {
		tracerAuditService.stop(tracerScope);
	}

	public void putAttribute(String string, Object attribute) {
		tracerAuditService.putAttribute(string, attribute);
	}

	public void addAnnotation(String annotation) {
		tracerAuditService.addAnnotation(annotation);
	}

	public void addAnnotation(String description, Map<String, String> attributes) {
		tracerAuditService.addAnnotation(description, attributes);
	}

}
