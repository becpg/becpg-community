package fr.becpg.repo.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import fr.becpg.repo.batch.BatchInfo;

@Service
public class AuditModelVisitor {

	public Map<String, Serializable> visitBatchInfo(BatchInfo batchInfo) {
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
