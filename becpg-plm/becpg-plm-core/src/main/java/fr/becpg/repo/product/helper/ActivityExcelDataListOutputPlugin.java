package fr.becpg.repo.product.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.ExcelDataListOutputPlugin;
import fr.becpg.repo.entity.datalist.impl.StandardExcelDataListOutputPlugin;

/**
 * <p>ActivityExcelDataListOutputPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ActivityExcelDataListOutputPlugin extends StandardExcelDataListOutputPlugin implements ExcelDataListOutputPlugin {


	@Autowired
	protected EntityListDAO entityListDAO;

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListFilter.getDataType());
	}

	/** {@inheritDoc} */
	@Override
	public List<Map<String, Object>> decorate(List<Map<String, Object>> items) throws IOException {
		if (items != null) {
			try {

				Map<String, JSONArray> subCache = new HashMap<>();

				for (Map<String, Object> item : items) {
					decorate(item, subCache);
				}

			} catch (JSONException e) {
				throw new WebScriptException("Unable to parse JSON", e);
			}
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> decorate(Map<String, Object> items, Map<String, JSONArray> subCache) throws JSONException {
		String activityType = "";
		if (items != null && !items.isEmpty() && items.containsKey("prop_bcpg_alType")) {
			activityType = (String) items.get("prop_bcpg_alType");
			for (Map.Entry<String, Object> item : items.entrySet()) {
				if (item.getValue() != null) {
					if (item.getKey().equals("prop_bcpg_alData") && item.getValue() instanceof JSONObject) {
						JSONObject data = (JSONObject) item.getValue();
						if (data.has("title") || activityType.equals(I18NUtil.getMessage("entity.activity.type.datalist"))) {
							String className = data.has("className") ? data.getString("className") : "entity";
							String title = data.has("title") ? data.getString("title") : "";
							String activityEvent = data.has("activityEvent") ? data.getString("activityEvent").toLowerCase() : "";
							if (activityType.equals(I18NUtil.getMessage("entity.activity.type.state"))) {
								title = I18NUtil.getMessage("entity.activity.state.change", title,
										I18NUtil.getMessage("data.state." + data.getString("beforeState").toLowerCase()),
										I18NUtil.getMessage("data.state." + data.getString("afterState").toLowerCase()));
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.datalist"))) {
								if (!data.has("title") || data.getString("title").indexOf(className) > -1) {
									title = I18NUtil.getMessage("entity.activity.datalist.simple", I18NUtil.getMessage("data.list." + className));
								} else {
									title = I18NUtil.getMessage("entity.activity.datalist." + activityEvent, title,
											I18NUtil.getMessage("data.list." + className));
								}
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.entity"))) {
								title = I18NUtil.getMessage("entity.activity.entity", title);
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.formulation"))) {
								title = I18NUtil.getMessage("entity.activity.formulation", title);
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.report"))) {
								title = I18NUtil.getMessage("entity.activity.report", title);
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.comment"))) {
								title = I18NUtil.getMessage("entity.activity.comment." + activityEvent, title)
										+ (data.has("content") ? " : \"" + data.getString("content") + "\"" : "");
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.content"))) {
								title = I18NUtil.getMessage("entity.activity.content." + activityEvent, title);
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.merge")) && data.has("branchTitle")) {
								title = I18NUtil.getMessage("entity.activity.merge", title, data.getString("branchTitle"));
							} else if (activityType.equals(I18NUtil.getMessage("entity.activity.type.version")) && data.has("versionLabel")
									&& data.has("versionNodeRef")) {
								title = I18NUtil.getMessage("entity.activity.version", title, data.getString("versionLabel"),
										data.getString("versionNodeRef"));
							}
							item.setValue(title);
						}
					} else if (item.getKey().contains("alUserId") && item.getValue() instanceof HashMap<?, ?>) {
						Map<String, Object> user = (HashMap<String, Object>) item.getValue();
						if (user.containsKey("displayValue")) {
							item.setValue(user.get("displayValue"));
						} else if (user.containsKey("value")) {
							item.setValue(user.get("value"));
						}
					}
				}
			}
		}

		return items;
	}

	
}
