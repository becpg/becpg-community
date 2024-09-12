package fr.becpg.repo.activity;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public interface EntityActivityExtractorService {

	void formatPostLookup(JSONObject postLookup);

	Map<String, Object> extractAuditActivityData(JSONObject auditActivityData, List<AttributeExtractorStructure> metadataFields, FormatMode mode);
	
}
