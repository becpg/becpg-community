package fr.becpg.repo.audit.plugin.visitor.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditModel;
import fr.becpg.repo.audit.plugin.visitor.AuditModelVisitor;
import fr.becpg.repo.batch.BatchInfo;

@Service("batchAuditModelVisitor")
public class BatchAuditModelVisitor implements AuditModelVisitor {

	@Override
	public Map<String, Serializable> visit(AuditModel model) {

		BatchInfo batchInfo = (BatchInfo) model;
		
		Map<String, Serializable> auditValues = new HashMap<>();
		
		int batchHashCode = Objects.hash(batchInfo.hashCode(), batchInfo.getStartTime());

		auditValues.put("batch/hashCode", batchHashCode);
		auditValues.put("batch/batchUser", batchInfo.getBatchUser());
		auditValues.put("batch/batchId", batchInfo.getBatchId());
		auditValues.put("batch/totalItems", batchInfo.getTotalItems());
		auditValues.put("batch/startedAt", batchInfo.getStartTime());
		auditValues.put("batch/isCompleted", batchInfo.getIsCompleted());
		if (batchInfo.getEndTime() != null)  {
			auditValues.put("batch/completedAt", batchInfo.getEndTime());
			auditValues.put("batch/duration", batchInfo.getEndTime().getTime() - batchInfo.getStartTime().getTime());
		}
		
		return auditValues;
	}

}
