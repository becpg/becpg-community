package fr.becpg.repo.activity;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>EntityActivityExtractorService interface.</p>
 *
 * @author matthieu
 */
public interface EntityActivityExtractorService {

	/**
	 * <p>formatPostLookup.</p>
	 *
	 * @param postLookup a {@link org.json.JSONObject} object
	 */
	void formatPostLookup(JSONObject postLookup);

	/**
	 * <p>extractAuditActivityData.</p>
	 *
	 * @param auditActivityData a {@link org.json.JSONObject} object
	 * @param metadataFields a {@link java.util.List} object
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object
	 * @return a {@link java.util.Map} object
	 */
	Map<String, Object> extractAuditActivityData(JSONObject auditActivityData, List<AttributeExtractorStructure> metadataFields, FormatMode mode);
	
}
