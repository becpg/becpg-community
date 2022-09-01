package fr.becpg.repo.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>CheckSumHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CheckSumHelper {
	
     private static final  Log logger = LogFactory.getLog(CheckSumHelper.class);
	
     private CheckSumHelper() {
    	 super();
     }
     
	/**
	 * <p>updateChecksum.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param checksum a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static  String updateChecksum(String key, String value, String checksum) {
		try {
			JSONObject json = null;

			if (value == null) {
				json = new JSONObject();
			} else {
				json = new JSONObject(value);
			}

			if (checksum != null) {
				json.put(key, checksum);
			} else {
				json.remove(key);
			}

			return json.toString();

		} catch (JSONException e) {
			logger.error(e, e);
		}
		return null;
	}

	/**
	 * <p>isSameChecksum.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param checksum a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isSameChecksum(String key, String value, String checksum) {
		try {
			if ((value != null)) {
				JSONObject json = new JSONObject(value);
				if (json.has(key)) {
					return (checksum != null) && checksum.equals(json.getString(key));
				}
			}
		} catch (JSONException e) {
			logger.error(e, e);
		}
		return false;
	}
}
