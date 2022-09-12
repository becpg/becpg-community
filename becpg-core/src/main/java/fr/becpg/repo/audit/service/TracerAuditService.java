package fr.becpg.repo.audit.service;

public interface TracerAuditService {

	AutoCloseable start(String tracerScopeName);

	void stop(AutoCloseable scope);

	void putAttribute(String string, Object attribute);

	void addAnnotation(String string);

}
