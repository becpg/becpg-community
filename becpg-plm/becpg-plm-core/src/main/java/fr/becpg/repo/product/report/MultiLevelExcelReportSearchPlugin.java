package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.formulation.FormulationHelper;

/**
 * <p>MultiLevelExcelReportSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class MultiLevelExcelReportSearchPlugin extends DynamicCharactExcelReportSearchPlugin {

	// Allowed Parameter1 AllLevel MaxLevel2 OnlyLevel2

	@Autowired
	MultiLevelDataListService multiLevelDataListService;
	
	// Allowed Parameter1 WUsedAllLevel WUsedMaxLevel2 WUsedOnlyLevel2
	
	@Autowired
	private WUsedListService wUsedListService;

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {
		String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;

		return PLMModel.TYPE_PACKAGINGLIST.equals(itemType) || ((parameter != null) && !parameter.isEmpty() && parameter.contains("Level"));
	}

	/** {@inheritDoc} */
	@Override
	public int fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {
		String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;

		boolean wUsed = false;
		QName pivotAssoc = null;
		
		String depthLevel;
		
		if (parameter != null) {

			wUsed = parameter.contains("wUsed");
			if (wUsed) {
				parameter = parameter.replace("wUsed", "");
				pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(itemType);
				mainType = entityDictionaryService.getTargetType(pivotAssoc);
			} 
			depthLevel = parameter.replace("Level", "").replace("Max", "").replace("Only", "");
		} else {
			depthLevel = "All";
		}

		ExcelCellStyles excelCellStyles = new ExcelCellStyles(sheet.getWorkbook());
		
		final int depthLevelNum = "All".equals(depthLevel) ? -1 : Integer.parseInt(depthLevel);
		
		final Map<NodeRef, Map<QName, Serializable>> wUsedAssocCache = wUsed ? new HashMap<>() : null;

		for (NodeRef entityNodeRef : searchResults) {
			QName entityType = nodeService.getType(entityNodeRef);
			if (mainType.equals(entityType) || entityDictionaryService.isSubClass(entityType, mainType)) {
				Serializable key = keyColumn != null ? nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName()) : null;
				if (key == null) {
					key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
				}
				if (key == null) {
					key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				}
				final MultiLevelListData listData;
				if (!wUsed) {
					DataListFilter dataListFilter = new DataListFilter();
					dataListFilter.setDataType(itemType);
					Map<String, String> criteriaMap = new HashMap<>();
					criteriaMap.put(DataListFilter.PROP_DEPTH_LEVEL, depthLevel);
					dataListFilter.setCriteriaMap(criteriaMap);
					dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));
	
					listData = multiLevelDataListService.getMultiLevelListData(dataListFilter);
				} else {
					listData = wUsedListService.getWUsedEntity(entityNodeRef, pivotAssoc, depthLevelNum);
				}

				Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);

				entityItems.putAll(getDynamicProperties(entityNodeRef, itemType));

				rownum = appendNextLevel(listData, sheet, itemType, metadataFields, cache, rownum, key, null, parameters, entityItems,
						new HashMap<>(), excelCellStyles, mainType, wUsedAssocCache);

			}
		}

		return rownum;
	}

	/**
	 * <p>appendNextLevel.</p>
	 *
	 * @param listData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 * @param sheet an {@link org.apache.poi.xssf.usermodel.XSSFSheet} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param cache a {@link java.util.Map} object.
	 * @param rownum an int.
	 * @param key a {@link java.io.Serializable} object.
	 * @param parentQty a {@link java.lang.Double} object.
	 * @param parameters an array of {@link java.lang.String} objects.
	 * @param entityItems a {@link java.util.Map} object.
	 * @param dynamicCharactColumnCache a {@link java.util.Map} object
	 * @param excelCellStyles a {@link fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles} object
	 * @param wUsedAssoc a {org.alfresco.service.cmr.repository.NodeRef} object
	 * @return an int.
	 */
	protected int appendNextLevel(MultiLevelListData listData, XSSFSheet sheet, QName itemType,
			List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache, int rownum,
			Serializable key, Double parentQty, String[] parameters, Map<String, Object> entityItems,
			Map<String, String> dynamicCharactColumnCache, ExcelCellStyles excelCellStyles, QName wUsedEntityType, Map<NodeRef, Map<QName, Serializable>> wUsedAssocCache) {
		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef itemNodeRef = entry.getKey();
			if (itemType.equals(nodeService.getType(itemNodeRef))) {
				if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {

					Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
					Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);

					for (Entry<String, Object> itemEntry : item.entrySet()) {
						String itemKey = itemEntry.getKey();
						Object itemValue = itemEntry.getValue();
						if (itemKey.startsWith("prop_bcpg_dynamicCharactColumn")) {
							if (dynamicCharactColumnCache.get(itemKey) == null && JsonFormulaHelper.isJsonString(itemValue)) {
								dynamicCharactColumnCache.put(itemKey, (String) itemValue);
								Object value = JsonFormulaHelper.cleanCompareJSON((String) itemValue);
								item.put(itemKey, value);
							} else if (dynamicCharactColumnCache.get(itemKey) != null) {
								Object subValue = JsonFormulaHelper.extractComponentValue(dynamicCharactColumnCache.get(itemKey),
										itemNodeRef.getId());
								if (subValue != null) {
									item.put(itemKey, subValue);
								}
							}
						}
					}

					if (entityItems != null) {
						item.putAll(entityItems);
					}

					item.put("prop_bcpg_depthLevel", entry.getValue().getDepth());
					item.put("prop_bcpg_parent", nodeService.getProperty(listData.getEntityNodeRef(), BeCPGModel.PROP_CODE));

					Double qty = null;

					if (PLMModel.TYPE_PACKAGINGLIST.equals(itemType)) {
						qty = (Double) item.get("prop_bcpg_packagingListQty");

						if ((qty != null) && PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(listData.getEntityNodeRef()))
								&& PackagingLevel.Tertiary.toString()
										.equals(nodeService.getProperty(itemNodeRef, PLMModel.PROP_PACKAGINGLIST_PKG_LEVEL))) {
							Integer nbByPalet = (Integer) nodeService.getProperty(listData.getEntityNodeRef(),
									PackModel.PROP_PALLET_BOXES_PER_PALLET);
							if ((nbByPalet != null) && (nbByPalet > 0)) {
								qty = qty / (nbByPalet * 1d);
							}
						}

						if ((qty != null) && (parentQty != null)) {
							qty = qty / parentQty;
							item.put("prop_bcpg_packagingListQty", qty);
						}

					} else if (PLMModel.TYPE_COMPOLIST.equals(itemType)) {
						qty = (Double) item.get("prop_bcpg_compoListQty");
						if ((qty != null) && (parentQty != null)) {

							Double parentNetWeight = FormulationHelper.getNetWeight(listData.getEntityNodeRef(), nodeService,
									FormulationHelper.DEFAULT_NET_WEIGHT);

							if ((parentNetWeight != null) && (parentNetWeight != 0d)) {
								qty = (parentQty * qty) / parentNetWeight;
								item.put("qty", qty);
							} else {
								qty = 0d;
							}
						}
					}

					for (AttributeExtractorStructure metadataField : new ArrayList<>(metadataFields)) {
						if (metadataField.isFormulaField()) {
							if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("image")) {
								item.put(metadataField.getFieldName(),
										eval(listData.getEntityNodeRef(), itemNodeRef, metadataField.getFormula(), item));
							} else {
								item.put(metadataField.getFieldName(), metadataField.getFormula());
							}
						}
					}
					
					if (wUsedAssocCache != null) {
						item.putAll(
								wUsedAssocCache
										.computeIfAbsent(itemNodeRef,
												unused -> nodeService
														.getProperties(entityListDAO.getEntity(itemNodeRef)))
										.entrySet().stream().filter(property -> property.getValue() != null)
										.collect(Collectors.toMap(
												property -> "wUsedEntity_" + property.getKey()
														.toPrefixString(namespaceService).replaceFirst(":", "_"),
												Entry::getValue)));
					}

					String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;
					
					if ((parameter == null) || !parameter.contains("OnlyLevel") || parameter.equals("OnlyLevel" + entry.getValue().getDepth())) {
						Row row = sheet.createRow(rownum++);

						int cellNum = 0;
						Cell cell = row.createCell(cellNum++);
						cell.setCellValue("VALUES");

						if (key != null) {
							cell = row.createCell(cellNum++);
							cell.setCellValue(String.valueOf(key));
						}

						ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);

					}
					rownum = appendNextLevel(entry.getValue(), sheet, itemType, metadataFields, cache, rownum, key, qty, parameters, entityItems,
							dynamicCharactColumnCache, excelCellStyles, wUsedEntityType, wUsedAssocCache);

				}
			}

		}
		return rownum;
	}

}
