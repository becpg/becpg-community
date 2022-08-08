package fr.becpg.repo.audit.plugin.visitor;

import java.io.Serializable;
import java.util.Map;

import fr.becpg.repo.audit.model.AuditModel;

public interface AuditModelVisitor {

	public Map<String, Serializable> visit(AuditModel model);

}
