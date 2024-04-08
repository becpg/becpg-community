/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetailAdditionalValue;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;

/**
 * <p>CharactDetailsHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CharactDetailsHelper {

	private static final Map<String, String> FORECAST_KEYS = new HashMap<>();
	
	private static final String MINI_VALUE_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_nutListMini.title");
	private static final String MAXI_VALUE_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_nutListMaxi.title");
	private static final String LEVEL_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_depthLevel.title");
	private static final String PRODUCT_TYPE_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_productType.title");

	
	private CharactDetailsHelper() {
		//Do NoThing
	}
	
	/**
	 * <p>toJSONObject.</p>
	 *
	 * @param charactDetails a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public static JSONObject toJSONObject(final CharactDetails charactDetails, final NodeService nodeService,
			final AttributeExtractorService attributeExtractorService) throws JSONException {

		JSONObject obj = new JSONObject();
		JSONArray metadatas = new JSONArray();
		JSONObject metadata = new JSONObject();
		metadata.put("colIndex", 0);
		metadata.put("colType", "String");
		metadata.put("colName", getYAxisLabel());
		metadatas.put(metadata);

		List<CharactDetailsValue> compEls = new LinkedList<>();

		Map<String, String> additionalValues = createAdditionalValuesMap();
		List<Object> totals = new LinkedList<>();
		Map<String, String> colUnits = new HashMap<>();
		for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
			String propName = attributeExtractorService.extractPropName(entry.getKey());

			for (CharactDetailsValue value : entry.getValue()) {

				if (!compEls.contains(value)) {
					value.setName(propName);
					compEls.add(value);
				}
				colUnits.put(propName, value.getUnit());
				
				for (CharactDetailAdditionalValue additionalValue : value.getAdditionalValues()) {
					colUnits.put(additionalValue.getColumnName(), additionalValue.getUnit());
				}

				fillAdditionalValuesMap(additionalValues, value, propName);
			}
		}

		// put previous, future, headers if necessary
		Map<String, Integer> indexMap = createColumnMap(charactDetails.getData(), additionalValues, attributeExtractorService, metadatas.length());

		writeMetadata(colUnits, metadatas, indexMap);
		// Entity nut 1, nut2, nut3

		List<List<Object>> resultsets = new LinkedList<>();
		totals.add(I18NUtil.getMessage("entity.datalist.item.details.totals"));

		for (int i = 0; i < indexMap.size(); ++i) {
			totals.add(0d);
		}

		Map<NodeRef, List<Object>> tmpMap = new LinkedHashMap<>();

		for (CharactDetailsValue charactDetailsValue : compEls) {

			String currentDetailsName = charactDetailsValue.getName();
			List<Object> tmp;
			if (!tmpMap.containsKey(charactDetailsValue.getCompositeNodeRef())) {

				tmp = new ArrayList<>();
				tmp.add(attributeExtractorService.extractPropName(charactDetailsValue.getKeyNodeRef()));

				// insert padding in tmp so columns fit
				for (int i = 0; i < indexMap.size(); i++) {
					tmp.add(null);
				}
				tmpMap.put(charactDetailsValue.getCompositeNodeRef(), tmp);
			} else {
				tmp = tmpMap.get(charactDetailsValue.getCompositeNodeRef());
			}

			// set charact value, increase its total
			for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
					Integer currentIndex = indexMap.get(currentDetailsName);
					Double total = (Double) totals.get(currentIndex);
					Optional<CharactDetailsValue> matchingCharact = compEls.stream()
							.filter(elt -> elt.keyEquals(charactDetailsValue) && elt.getName().equals(currentDetailsName)).findFirst();
					if (matchingCharact.isPresent()) {
						
						int entryIndex = entry.getValue().indexOf(matchingCharact.get());
						if (entryIndex != -1) {
							Double value = entry.getValue().get(entryIndex).getValue();
							tmp.set(currentIndex, value);
							if (entry.getValue().get(entryIndex).getLevel() == 0) {
								total += value != null ? value : 0d;
							}
						}
					}
					totals.set(currentIndex, total);
			}

			// set additional values to tmp
			for (Entry<String, String> entry : additionalValues.entrySet()) {

				String key = entry.getKey();
				if (!entry.getValue().equals(currentDetailsName)) {
					continue;
				}

				Integer index = indexMap.get(key);
				Double currentAdditionalValue = null;
				boolean isNutrient = key.equals(MINI_VALUE_KEY) || key.equals(MAXI_VALUE_KEY);

				if (FORECAST_KEYS.values().contains(key)) {
					String forecastColumn = FORECAST_KEYS.keySet().stream().filter(k -> FORECAST_KEYS.get(k).equals(key)).findFirst().orElseGet(null);
					currentAdditionalValue = charactDetailsValue.getForecastValue(forecastColumn);
				} else if (key.equals(MINI_VALUE_KEY)) {
					currentAdditionalValue = charactDetailsValue.getMini();
				} else if (key.equals(MAXI_VALUE_KEY)) {
					currentAdditionalValue = charactDetailsValue.getMaxi();
				} else {
					CharactDetailAdditionalValue additionalValue = charactDetailsValue.getAdditionalValue(key);
					if (additionalValue != null) {
						currentAdditionalValue = additionalValue.getValue();
					}
				}
				
				Integer level = charactDetailsValue.getLevel();

				Object elementToDisplay = currentAdditionalValue;
				if (currentAdditionalValue == null) {
					if (isNutrient) {
						elementToDisplay = "—";
						currentAdditionalValue = charactDetailsValue.getValue() != null ? charactDetailsValue.getValue() : 0d;
					} else {
						elementToDisplay = "";
						currentAdditionalValue = 0d;
					}
				}

				if (level == 0) {
					computeTotals(index, totals, currentAdditionalValue);
				}
				tmp.set(index, elementToDisplay);
			}

			tmp.add(charactDetailsValue.getKeyNodeRef());
			tmp.add(nodeService.getType(charactDetailsValue.getKeyNodeRef()));
			tmp.add(nodeService.getType(charactDetailsValue.getKeyNodeRef()).getLocalName());
			tmp.add(charactDetailsValue.getLevel());
		}

		resultsets.addAll(tmpMap.values());
		resultsets.add(totals);

		obj.put("metadatas", metadatas);
		obj.put("resultsets", resultsets);

		return obj;

	}

	private static void writeMetadata(Map<String, String> colUnits, JSONArray metadatas, Map<String, Integer> indexMap) throws JSONException {
		String colUnit = "";
		for (Entry<String, Integer> entry : indexMap.entrySet()) {
			JSONObject metadata = new JSONObject();
			metadata.put("colType", "Double");
			metadata.put("colIndex", entry.getValue());
			metadata.put("colName", entry.getKey());
			if (colUnits.containsKey(entry.getKey())) {
				// Mini, Maxi, Future, Previous columns
				colUnit = colUnits.get(entry.getKey());
			}
				metadata.put("colUnit", colUnit);
			metadatas.put(metadata);
		}
	}

	private static Map<String, Integer> createColumnMap(Map<NodeRef, List<CharactDetailsValue>> characts, Map<String, String> additionalValues,
			AttributeExtractorService attributeExtractorService, Integer idx) {
		Map<String, Integer> res = new LinkedHashMap<>();
		for (Entry<NodeRef, List<CharactDetailsValue>> currentCharact : characts.entrySet()) {
			String currentCharactName = attributeExtractorService.extractPropName(currentCharact.getKey());
				res.put(currentCharactName, idx);
			idx = completeAdditionalValues(currentCharactName, res, idx, additionalValues);
			++idx;
		}
		return res;
	}

	private static Integer completeAdditionalValues(String charactName, Map<String, Integer> indexMap, Integer idx,
			Map<String, String> additionalValues) {
		for (Entry<String, String> additionalValue : additionalValues.entrySet()) {
			if (additionalValue.getValue().equals(charactName)) {
				indexMap.put(additionalValue.getKey(), ++idx);
			}
		}
		return idx;
	}

	private static void computeTotals(Integer index, List<Object> totals, Double currentValue) {
		if (totals.size() > index) {
			totals.set(index, (Double) totals.get(index) + currentValue);
		} else {
			totals.add(index, currentValue);
		}
	}

	private static void fillAdditionalValuesMap(Map<String, String> additionalValues, CharactDetailsValue currentValue, String propName) {

		for (String forecastColumn : currentValue.getForecastColumns()) {
			if (currentValue.getForecastValue(forecastColumn) != null) {
				String forecastKey = I18NUtil.getMessage("bcpg_bcpgmodel.property." + forecastColumn.replace(":", "_") + ".title");
				FORECAST_KEYS.put(forecastColumn, forecastKey);
				additionalValues.put(forecastKey, propName);
			}
		}
		
		if (currentValue.getMini() != null) {
			additionalValues.put(MINI_VALUE_KEY, propName);
		}
		if (currentValue.getMaxi() != null) {
			additionalValues.put(MAXI_VALUE_KEY, propName);
		}
		
		for (CharactDetailAdditionalValue additionalValue : currentValue.getAdditionalValues()) {
			additionalValues.put(additionalValue.getColumnName(), propName);
		}

	}

	private static String getYAxisLabel() {
		return I18NUtil.getMessage("entity.datalist.item.details.yaxis.label");
	}

	/**
	 * <p>writeXLS.</p>
	 *
	 * @param charactDetails a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public static void writeXLS(CharactDetails charactDetails, NodeService nodeService, AttributeExtractorService attributeExtractorService,
			OutputStream outputStream) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet();
			int rownum = 0;
			int cellnum = 0;
			Row row = sheet.createRow(rownum++);
			XSSFCellStyle style = workbook.createCellStyle();
			
			byte[] rgb = {(byte)  0, (byte) 102, (byte) 0};
			
			XSSFColor green = new XSSFColor(rgb, new DefaultIndexedColorMap());

			style.setFillForegroundColor(green);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			XSSFFont font = workbook.createFont();
			font.setColor(HSSFColorPredefined.WHITE.getIndex());
			style.setFont(font);

			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(getYAxisLabel());
			cell.setCellStyle(style);

			cell = row.createCell(cellnum++);
			cell.setCellValue(PRODUCT_TYPE_KEY);
			cell.setCellStyle(style);

			cell = row.createCell(cellnum++);
			cell.setCellValue(LEVEL_KEY);
			cell.setCellStyle(style);

			Map<String, String> additionalValues = createAdditionalValuesMap();
			List<CharactDetailsValue> compEls = new LinkedList<>();

			for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				for (CharactDetailsValue value : entry.getValue()) {
					String propName = attributeExtractorService.extractPropName(entry.getKey());
					if (!compEls.contains(value)) {
						value.setName(propName);
						compEls.add(value);
					}

					fillAdditionalValuesMap(additionalValues, value, propName);
				}
			}

			Map<String, Integer> indexMap = createColumnMap(charactDetails.getData(), additionalValues, attributeExtractorService, cellnum);
			// add sorted headers
			for (Entry<String, Integer> entry : indexMap.entrySet()) {
				cell = row.createCell(entry.getValue());
				cell.setCellValue(entry.getKey());
				cell.setCellStyle(style);
			}

			Map<NodeRef, Integer> rowIndexes = new HashMap<>();

			for (CharactDetailsValue charactDetailsValue : compEls) {
				cellnum = 0;
				String prefix = "";
				if (charactDetailsValue.getLevel() > 0) {
					prefix = "└";
					for (int i = 0; i < charactDetailsValue.getLevel(); i++) {
						prefix += "──";
					}
					prefix += ">";
				}

				String currentDetailsName = charactDetailsValue.getName();

				if (rowIndexes.containsKey(charactDetailsValue.getCompositeNodeRef())) {
					rownum = rowIndexes.get(charactDetailsValue.getCompositeNodeRef());
					row = sheet.getRow(rownum);
				} else {
					rownum = rowIndexes.size() + 1;
					rowIndexes.put(charactDetailsValue.getCompositeNodeRef(), rownum);
					row = sheet.createRow(rownum);
				}

				cell = row.createCell(cellnum++);
				cell.setCellValue(prefix + attributeExtractorService.extractPropName(charactDetailsValue.getKeyNodeRef()));

				// product type cell
				cell = row.createCell(cellnum++);
				String type = nodeService.getType(charactDetailsValue.getKeyNodeRef()).getLocalName();
				String typeTitle = I18NUtil.getMessage("bcpg_bcpgmodel.type.bcpg_" + type + ".title");
				cell.setCellValue(typeTitle);

				// level depth cell
				cell = row.createCell(cellnum++);
				cell.setCellValue(charactDetailsValue.getLevel());

				// set charact value to cell
				for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {

						Integer currentIndex = indexMap.get(currentDetailsName);
						
						Optional<CharactDetailsValue> matchingCharact = compEls.stream()
								.filter(elt -> elt.keyEquals(charactDetailsValue) && elt.getName().equals(currentDetailsName)).findFirst();
						if (matchingCharact.isPresent()) {
							
							int entryIndex = entry.getValue().indexOf(matchingCharact.get());
							if (entryIndex != -1) {
								Double value = entry.getValue().get(entryIndex).getValue();
								cell = row.createCell(currentIndex);
								cell.setCellValue(value);
							}

					}
				}

				// put additional characts to cells
				for (Entry<String, String> entry : additionalValues.entrySet()) {

					String key = entry.getKey();

					if (!entry.getValue().equals(currentDetailsName)) {
						continue;
					}

					Integer index = indexMap.get(key);
					Double currentAdditionalValue = 0d;

					if (FORECAST_KEYS.values().contains(key)) {
						String forecastColumn = FORECAST_KEYS.keySet().stream().filter(k -> FORECAST_KEYS.get(k).equals(key)).findFirst().orElseGet(null);
						currentAdditionalValue = charactDetailsValue.getForecastValue(forecastColumn);
					} else if (key.equals(MINI_VALUE_KEY)) {
						currentAdditionalValue = charactDetailsValue.getMini();
					} else if (key.equals(MAXI_VALUE_KEY)) {
						currentAdditionalValue = charactDetailsValue.getMaxi();
					} else {
						CharactDetailAdditionalValue additionalValue = charactDetailsValue.getAdditionalValue(key);
						if (additionalValue != null) {
							currentAdditionalValue = additionalValue.getValue();
						}
					}
					
					if (currentAdditionalValue == null) {
						currentAdditionalValue = 0d;
					}

					cell = row.createCell(index);
					cell.setCellValue(currentAdditionalValue);
				}
			}

			workbook.write(outputStream);
		}
	}

	// translation -> index in resulting array, sorted so it goes value - mini -
	// maxi, or value - previous - future
	private static Map<String, String> createAdditionalValuesMap() {
		return new LinkedHashMap<>();
	}

}
