package fr.becpg.repo.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>JsonHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonHelper {
	
	/**
	 * <p>extractCriteria.</p>
	 *
	 * @param jsonObject a {@link org.json.JSONObject} object.
	 * @return a {@link java.util.Map} object.
	 * @throws org.json.JSONException if any.
	 */
	@SuppressWarnings("unchecked")
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
			} 
		}
		return value;
	}
	
	
}
