package fr.becpg.repo.audit.plugin;

import io.opencensus.trace.AttributeValue;

public interface TracerAuditService {

	AutoCloseable start(String tracerScopeName);

	void stop(AutoCloseable scope);

	void putAttribute(String string, AttributeValue stringAttributeValue);

	void addAnnotation(String string);

}
