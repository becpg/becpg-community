package fr.becpg.repo.helper.json;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>JsonHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class JsonHelper {
	
	
	private JsonHelper() {
		//Singleton
	}
	
	
    /** Constant <code>MAPPER</code> */
    public static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * <p>extractCriteria.</p>
	 *
	 * @param jsonObject a {@link org.json.JSONObject} object.
	 * @return a {@link java.util.Map} object.
	 * @throws org.json.JSONException if any.
	 */
	public static Map<String, String> extractCriteria(JSONObject jsonObject) throws JSONException {

		Map<String, String> criteriaMap = new HashMap<>();

		Iterator<String> iterator = jsonObject.keys();

		while (iterator.hasNext()) {

			String key = iterator.next();
			String value = jsonObject.getString(key);
			criteriaMap.put(key, value);
		}

		return criteriaMap;

	}
	
	public static JsonData createJsonData() {
		return new JsonData(MAPPER.createObjectNode());
	}

	/**
	 * <p>formatValue.</p>
	 *
	 * @param value a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object formatValue(Object value) {
		if (value != null) {
			if (value instanceof Date) {
				return ISO8601DateFormat.format((Date) value);
			} else if (value instanceof Double) {
				Double d = (Double) value;
				if (d.isInfinite()) {
					return 0 == d.compareTo(Double.POSITIVE_INFINITY) ? "23456789012E777" : "-23456789012E777";
				} else if(d.isNaN()) {
					return null;
				}
			} else if (value instanceof Float) {
				Float f = (Float) value;
				if (f.isInfinite()) {
					return 0 == f.compareTo(Float.POSITIVE_INFINITY) ? "23456789012E777" : "-23456789012E777";
				} else if(f.isNaN()) {
					return null;
				}
			} else if (value instanceof NodeRef) {
				return value.toString();
			} 
		}
		return value;
	}
	
	/**
	 * <p>serialize.</p>
	 *
	 * @param object a {@link java.lang.Object} object
	 * @return a {@link java.lang.String} object
	 * @throws com.fasterxml.jackson.core.JsonProcessingException if any.
	 */
	public static String serialize(Object object) {
		try {
			return MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new JsonException(e);
		}
	}
	
	/**
	 * <p>readTree.</p>
	 *
	 * @param json a {@link java.lang.String} object
	 * @return a {@link com.fasterxml.jackson.databind.JsonNode} object
	 * @throws com.fasterxml.jackson.core.JsonProcessingException if any.
	 */
	public static JsonData read(String json) {
		try {
			return new JsonData(MAPPER.readTree(json));
		} catch (JsonProcessingException e) {
			throw new JsonException(e);
		}
	}
	
}
