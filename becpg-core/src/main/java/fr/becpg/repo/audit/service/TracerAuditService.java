package fr.becpg.repo.audit.service;

import java.util.Map;

public interface TracerAuditService {

	AutoCloseable start(String tracerScopeName);

	void stop(AutoCloseable scope);

	void putAttribute(String string, Object attribute);

	void addAnnotation(String annotation);

	void addAnnotation(String description, Map<String, String> attributes);

}
