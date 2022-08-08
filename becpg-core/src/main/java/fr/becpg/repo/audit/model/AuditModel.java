package fr.becpg.repo.audit.model;

import java.io.Serializable;
import java.util.Map;

import fr.becpg.repo.audit.plugin.visitor.AuditModelVisitor;

public interface AuditModel {
	Map<String, Serializable> accept(AuditModelVisitor visitor);
}
