package fr.becpg.repo.product.helper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Service;

import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.entity.datalist.impl.ExcelDataListOutputPlugin;
import fr.becpg.repo.entity.datalist.impl.MultiLevelExtractor;
import fr.becpg.repo.form.column.decorator.DynamicColumnNameResolver;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.JsonFormulaHelper;

/**
 * <p>ProductExcelDataListOutputPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductExcelDataListOutputPlugin implements ExcelDataListOutputPlugin {

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	protected EntityListDAO entityListDAO;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private NamespaceService namespaceService;

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return PLMModel.TYPE_COMPOLIST.equals(dataListFilter.getDataType()) || PLMModel.TYPE_PACKAGINGLIST.equals(dataListFilter.getDataType())
				|| MPMModel.TYPE_PROCESSLIST.equals(dataListFilter.getDataType())
				|| PLMModel.TYPE_DYNAMICCHARACTLIST.equals(dataListFilter.getDataType());
	}

	/** {@inheritDoc} */
	@Override
	public ExcelFieldTitleProvider getExcelFieldTitleProvider(DataListFilter dataListFilter) {
		return new DynamicColumnNameResolver(dataListFilter, nodeService, dictionaryService);
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
								if (jsonObject.has(JsonFormulaHelper.JSON_SUB_VALUES) && (subCache != null)) {
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

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extractExtrasSheet(DataListFilter dataListFilter) {

		if (PLMModel.TYPE_DYNAMICCHARACTLIST.equals(dataListFilter.getDataType())) {
			return null;
		}

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());
		Set<String> metadataFields = new HashSet<>();

		if (dataListFilter.getEntityNodeRef() != null) {

			NodeRef listsContainerNodeRef = entityListDAO.getListContainer(dataListFilter.getEntityNodeRef());

			if (listsContainerNodeRef != null) {
				NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, dataListFilter.getDataListName());

				if (dataListNodeRef != null) {
					List<NodeRef> dynamicCharacts = entityListDAO.getListItems(dataListNodeRef, PLMModel.TYPE_DYNAMICCHARACTLIST);

					if (!dynamicCharacts.isEmpty()) {
						dynamicCharacts.forEach(nodeRef -> {

							Map<String, Object> temp = new HashMap<>();
							nodeService.getProperties(nodeRef).forEach((key, value) -> {

								if (key.equals(PLMModel.PROP_DYNAMICCHARACT_TITLE) || key.equals(PLMModel.PROP_DYNAMICCHARACT_VALUE)) {
									String mtField = "prop_" + key.toPrefixString(namespaceService).replace(":", "_");
									
									if (value != null && key.equals(PLMModel.PROP_DYNAMICCHARACT_VALUE)) {
										value = (Serializable) JsonFormulaHelper.cleanCompareJSON(value.toString());
									}
									
									temp.put(mtField, value);
									if (ret.getComputedFields() == null) {
										metadataFields.add(key.toPrefixString(namespaceService));
									}
								}

							});

							if (!temp.isEmpty()) {
								ret.addItem(temp);
							}
						});

						if (ret.getComputedFields() == null) {
							ret.setComputedFields(attributeExtractorService.readExtractStructure(PLMModel.TYPE_DYNAMICCHARACTLIST,
									new ArrayList<>(metadataFields)));
						}
						
						dataListFilter.setDataListName(PLMModel.TYPE_DYNAMICCHARACTLIST.getLocalName());
						dataListFilter.setDataType(PLMModel.TYPE_DYNAMICCHARACTLIST);
					}
				}
			}
		}

		return ret;
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

}
