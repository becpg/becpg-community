package fr.becpg.repo.report.entity.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor.DefaultExtractorContextCallBack;

public class DefaultExtractorContext {

	boolean isInDataListContext = false;

	Map<String, String> preferences;
	Map<String, Boolean> cache = new HashMap<>(4);

	Set<NodeRef> extractedNodes = new HashSet<>();

	EntityReportData reportData = new EntityReportData();

	private boolean isInfiniteLoop = false;

	public DefaultExtractorContext(Map<String, String> preferences) {
		super();
		this.preferences = preferences;
	}
	
	public void setInfiniteLoop(boolean isInfiniteLoop) {
		this.isInfiniteLoop = isInfiniteLoop;
	}

	public Map<String, Boolean> getCache() {
		return cache;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public Set<NodeRef> getExtractedNodes() {
		return extractedNodes;
	}

	public EntityReportData getReportData() {
		return reportData;
	}

	public boolean prefsContains(String key, String defaultValue, String query) {
		if (((defaultValue != null) && defaultValue.contains(query))
				|| (preferences.containsKey(key) && preferences.get(key).contains(query))) {
			return true;
		}

		return false;
	}

	public boolean multiPrefsEquals(String key, String defaultValue, String query) {
		if (((defaultValue != null) && Arrays.asList(defaultValue.split(",")).contains(query))
				|| (preferences.containsKey(key) && Arrays.asList(preferences.get(key).split(",")).contains(query))) {
			return true;
		}

		return false;
	}

	public boolean isPrefOn(String key, Boolean defaultValue) {
		if (Boolean.TRUE.equals(defaultValue)
				|| (preferences.containsKey(key) && "true".equalsIgnoreCase(preferences.get(key)))) {
			return true;
		}

		return false;
	}

	public String getPrefValue(String key, String defaultValue) {

		if (preferences.containsKey(key)) {
			return preferences.get(key);
		}

		return defaultValue;
	}

	public boolean isNotEmptyPrefs(String key, String defaultValue) {
		if (((defaultValue != null) && !defaultValue.isEmpty())
				|| (preferences.containsKey(key) && !preferences.get(key).isEmpty())) {
			return true;
		}

		return false;
	}

	public boolean isInDataListContext() {
		return isInDataListContext;
	}

	public void doInDataListContext(DefaultExtractorContextCallBack callBack) {
		try {
			isInDataListContext = true;
			callBack.run();
		} finally {
			isInDataListContext = false;
		}

	}

	public boolean isInfiniteLoop() {
		return isInfiniteLoop;
	}

}