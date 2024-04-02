package fr.becpg.repo.activity;

import java.util.List;

import org.json.JSONObject;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public interface EntityActivityExtractorService {

	void formatPostLookup(JSONObject postLookup);

	Object extractAuditActivityData(JSONObject auditActivityData, List<AttributeExtractorStructure> metadataFields);
	
}
