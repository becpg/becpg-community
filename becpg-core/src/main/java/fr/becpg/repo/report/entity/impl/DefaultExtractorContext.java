package fr.becpg.repo.report.entity.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContextCallBack;

/**
 * <p>DefaultExtractorContext class.</p>
 *
 * @author matthieu
 */
public class DefaultExtractorContext {

	boolean isInDataListContext = false;

	Map<String, String> preferences;
	Map<String, Boolean> cache = new HashMap<>(4);

	Set<NodeRef> extractedNodes = new HashSet<>();

	EntityReportData reportData = new EntityReportData();

	private boolean isInfiniteLoop = false;

	/**
	 * <p>Constructor for DefaultExtractorContext.</p>
	 *
	 * @param preferences a {@link java.util.Map} object
	 */
	public DefaultExtractorContext(Map<String, String> preferences) {
		super();
		this.preferences = preferences;
	}
	
	/**
	 * <p>setInfiniteLoop.</p>
	 *
	 * @param isInfiniteLoop a boolean
	 */
	public void setInfiniteLoop(boolean isInfiniteLoop) {
		this.isInfiniteLoop = isInfiniteLoop;
	}

	/**
	 * <p>Getter for the field <code>cache</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, Boolean> getCache() {
		return cache;
	}

	/**
	 * <p>Getter for the field <code>preferences</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, String> getPreferences() {
		return preferences;
	}

	/**
	 * <p>Getter for the field <code>extractedNodes</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<NodeRef> getExtractedNodes() {
		return extractedNodes;
	}

	/**
	 * <p>Getter for the field <code>reportData</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.report.entity.EntityReportData} object
	 */
	public EntityReportData getReportData() {
		return reportData;
	}

	/**
	 * <p>prefsContains.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param defaultValue a {@link java.lang.String} object
	 * @param query a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean prefsContains(String key, String defaultValue, String query) {
		if (((defaultValue != null) && defaultValue.contains(query))
				|| (preferences.containsKey(key) && preferences.get(key).contains(query))) {
			return true;
		}

		return false;
	}

	/**
	 * <p>multiPrefsEquals.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param defaultValue a {@link java.lang.String} object
	 * @param query a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean multiPrefsEquals(String key, String defaultValue, String query) {
		if (((defaultValue != null) && Arrays.asList(defaultValue.split(",")).contains(query))
				|| (preferences.containsKey(key) && Arrays.asList(preferences.get(key).split(",")).contains(query))) {
			return true;
		}

		return false;
	}

	/**
	 * <p>isPrefOn.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param defaultValue a {@link java.lang.Boolean} object
	 * @return a boolean
	 */
	public boolean isPrefOn(String key, Boolean defaultValue) {
		if (Boolean.TRUE.equals(defaultValue)
				|| (preferences.containsKey(key) && "true".equalsIgnoreCase(preferences.get(key)))) {
			return true;
		}

		return false;
	}

	/**
	 * <p>getPrefValue.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param defaultValue a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getPrefValue(String key, String defaultValue) {

		if (preferences.containsKey(key)) {
			return preferences.get(key);
		}

		return defaultValue;
	}

	/**
	 * <p>isNotEmptyPrefs.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param defaultValue a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean isNotEmptyPrefs(String key, String defaultValue) {
		if (((defaultValue != null) && !defaultValue.isEmpty())
				|| (preferences.containsKey(key) && !preferences.get(key).isEmpty())) {
			return true;
		}

		return false;
	}

	/**
	 * <p>isInDataListContext.</p>
	 *
	 * @return a boolean
	 */
	public boolean isInDataListContext() {
		return isInDataListContext;
	}

	/**
	 * <p>doInDataListContext.</p>
	 *
	 * @param callBack a {@link fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContextCallBack} object
	 */
	public void doInDataListContext(DefaultExtractorContextCallBack callBack) {
		try {
			isInDataListContext = true;
			callBack.run();
		} finally {
			isInDataListContext = false;
		}

	}

	/**
	 * <p>isInfiniteLoop.</p>
	 *
	 * @return a boolean
	 */
	public boolean isInfiniteLoop() {
		return isInfiniteLoop;
	}

}
