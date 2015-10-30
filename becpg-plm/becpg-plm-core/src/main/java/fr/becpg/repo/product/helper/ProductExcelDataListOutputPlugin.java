package fr.becpg.repo.product.helper;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Service;

import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.entity.datalist.impl.ExcelDataListOutputPlugin;
import fr.becpg.repo.entity.datalist.impl.MultiLevelExtractor;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service
public class ProductExcelDataListOutputPlugin implements ExcelDataListOutputPlugin {

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private NodeService nodeService;

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return PLMModel.TYPE_COMPOLIST.equals(dataListFilter.getDataType()) || PLMModel.TYPE_PACKAGINGLIST.equals(dataListFilter.getDataType())
				|| MPMModel.TYPE_PROCESSLIST.equals(dataListFilter.getDataType());
	}

	@Override
	public ExcelFieldTitleProvider getExcelFieldTitleProvider(DataListFilter dataListFilter) {
		return new DynamicColumnNameResolver(dataListFilter);
	}

	@Override
	public List<Map<String, Object>> decorate(List<Map<String, Object>> items) throws IOException {
		try {

			Map<String, JSONArray> subCache = new HashMap<>();

			for (Map<String, Object> item : items) {
				decorate(item, subCache);
			}

		} catch (JSONException e) {
			throw new WebScriptException("Unable to parse JSON", e);
		}

		return items;
	}

	private Map<String, Object> decorate(Map<String, Object> items, Map<String, JSONArray> subCache) throws JSONException {

		for (Map.Entry<String, Object> item : items.entrySet()) {
			if (item.getKey().contains("dynamicCharactColumn")) {

				if (item.getValue() != null) {

					if (isMultiLevel(items)) {
						item.setValue("");

						if ((subCache != null) && (subCache.get(item.getKey()) != null)) {
							item.setValue(extractSubValue(subCache.get(item.getKey()), items));
						}
					} else {
						if (item.getValue() instanceof String) {
							String value = (String) item.getValue();

							if (value.contains(JsonFormulaHelper.JSON_COMP_ITEMS)) {
								JSONTokener tokener = new JSONTokener(value);
								JSONObject jsonObject = new JSONObject(tokener);
								JSONArray array = (JSONArray) jsonObject.get(JsonFormulaHelper.JSON_COMP_ITEMS);
								item.setValue(((JSONObject) array.get(0)).get(JsonFormulaHelper.JSON_VALUE));
							} else if (value.contains(JsonFormulaHelper.JSON_SUB_VALUES)) {

								JSONTokener tokener = new JSONTokener(value);
								JSONObject jsonObject = new JSONObject(tokener);
								if (jsonObject.has(JsonFormulaHelper.JSON_VALUE)) {
									item.setValue(jsonObject.get(JsonFormulaHelper.JSON_VALUE));
								} else {
									item.setValue("");
								}
								if (jsonObject.has(JsonFormulaHelper.JSON_SUB_VALUES)) {
									subCache.put(item.getKey(), (JSONArray) jsonObject.get(JsonFormulaHelper.JSON_SUB_VALUES));
								}
							}

						}
					}

				} else {
					if (isMultiLevel(items)) {
						item.setValue("");

						if ((subCache != null) && (subCache.get(item.getKey()) != null)) {
							item.setValue(extractSubValue(subCache.get(item.getKey()), items));
						}
					}

				}

			}

		}

		return items;
	}

	private Serializable extractSubValue(JSONArray jsonArray, Map<String, Object> items) throws JSONException {
		for (int j = 0; j < jsonArray.length(); j++) {
			String path = jsonArray.getJSONObject(j).getString(JsonFormulaHelper.JSON_PATH);
			if ((path != null) && path.equals(items.get(AbstractDataListExtractor.PROP_PATH))) {
				if (jsonArray.getJSONObject(j).has(JsonFormulaHelper.JSON_VALUE)) {
					return (Serializable) jsonArray.getJSONObject(j).get(JsonFormulaHelper.JSON_VALUE);
				}
			}
		}
		return "";
	}

	private boolean isMultiLevel(Map<String, Object> items) {
		return items.containsKey(MultiLevelExtractor.PROP_IS_MULTI_LEVEL) && (Boolean) items.get(MultiLevelExtractor.PROP_IS_MULTI_LEVEL);
	}

	public class DynamicColumnNameResolver implements ExcelFieldTitleProvider {

		Map<String, String> dynamicColumnNames = new HashMap<>();

		public DynamicColumnNameResolver(DataListFilter filter) {

			for (NodeRef nodeRef : BeCPGQueryBuilder.createQuery().parent(filter.getParentNodeRef()).ofType(PLMModel.TYPE_DYNAMICCHARACTLIST)
					.isNotNull(PLMModel.PROP_DYNAMICCHARACT_COLUMN).inDB().list()) {

				dynamicColumnNames.put(((String) nodeService.getProperty(nodeRef, PLMModel.PROP_DYNAMICCHARACT_COLUMN)).replace("bcpg_", ""),
						(String) nodeService.getProperty(nodeRef, PLMModel.PROP_DYNAMICCHARACT_TITLE));

			}

		}

		@Override
		public String getTitle(AttributeExtractorStructure field) {

			if (dynamicColumnNames.containsKey(field.getFieldDef().getName().getLocalName())) {
				return dynamicColumnNames.get(field.getFieldDef().getName().getLocalName());
			}

			return field.getFieldDef().getTitle(dictionaryService);
		}

		@Override
		public boolean isAllowed(AttributeExtractorStructure field) {
			if (field.getFieldDef().getName().getLocalName().contains("dynamicCharactColumn")) {
				if (!dynamicColumnNames.containsKey(field.getFieldDef().getName().getLocalName())) {
					return false;
				}
			} else if (PLMModel.PROP_COMPARE_WITH_DYN_COLUMN.equals(field.getFieldDef().getName())
					|| ForumModel.PROP_COMMENT_COUNT.equals(field.getFieldDef().getName())) {
				return false;
			}

			return true;
		}

	}

}
