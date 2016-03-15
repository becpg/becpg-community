package fr.becpg.repo.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelper {
	
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
	
	
}
