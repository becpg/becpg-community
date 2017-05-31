/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
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
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;

/**
 * 
 * @author matthieu
 *
 */
public class CharactDetailsHelper {
	
	private static final String PREVIOUS_COST_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_costListPreviousValue.title");
	private static final String FUTURE_COST_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_costListFutureValue.title");
	private static final String MINI_VALUE_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_nutListMini.title");
	private static final String MAXI_VALUE_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_nutListMaxi.title");
	private static final String LEVEL_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_depthLevel.title");
	private static final String PRODUCT_TYPE_KEY = I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_productType.title");
	
	public static JSONObject toJSONObject(final CharactDetails charactDetails, final NodeService nodeService, final AttributeExtractorService attributeExtractorService) throws JSONException {

		JSONObject obj = new JSONObject();
		JSONArray metadatas = new JSONArray();
		JSONObject metadata = new JSONObject();
		metadata.put("colIndex", 0);
		metadata.put("colType", "String");
		metadata.put("colName", getYAxisLabel());
		metadatas.put(metadata);

		List<CharactDetailsValue> compEls = new LinkedList<>();

		// translation -> index in resulting array
		Map<String, Integer> additionalValues = new LinkedHashMap<String, Integer>();
		List<Object> totals = new LinkedList<>();
		String colUnit = "";
		for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {			
			String propName = attributeExtractorService.extractPropName(entry.getKey());

			for (CharactDetailsValue value : entry.getValue()) {
				
				if(!compEls.contains(value)){
					value.setName(propName);
					compEls.add(value);
				}
				colUnit = value.getUnit();

				fillAdditionalValuesMap(additionalValues, value, propName);
			}
		}

		// put previous, future, headers if necessary
		Map<String, Integer> indexMap = createColumnMap(charactDetails.getData(), additionalValues, attributeExtractorService, metadatas.length());
		
		writeMetadata(colUnit, metadatas, indexMap);
		// Entity nut 1, nut2, nut3

		List<List<Object>> resultsets = new LinkedList<>();
		totals.add(I18NUtil.getMessage("entity.datalist.item.details.totals"));
		
		for(int i = 0; i< indexMap.size(); ++i){
			totals.add(0d);
		}
		
		for (CharactDetailsValue charactDetailsValue : compEls) {
			
			String currentDetailsName = charactDetailsValue.getName();
			List<Object> tmp = new ArrayList<>();
						
			tmp.add(attributeExtractorService.extractPropName(charactDetailsValue.getKeyNodeRef()));
			
			//insert padding in tmp so columns fit
			for(int i=0; i<indexMap.size(); i++){
				tmp.add(null);
			}
			
			//set charact value, increase its total
			for (Map.Entry<NodeRef,List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				Integer currentIndex = indexMap.get(currentDetailsName);
				Double total = (Double) totals.get(currentIndex);

				if (entry.getValue().contains(charactDetailsValue)) {
					Double value = entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getValue();

					tmp.set(currentIndex, value);
					if(entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getLevel()==0){
						total += value!=null ? value : 0d;
					}
				}
				totals.set(currentIndex,total);
			}
					
			//set additional values to tmp
			for(Entry<String, Integer> entry : additionalValues.entrySet()){
								
				String key = entry.getKey();
				if(!key.contains(currentDetailsName)){
					continue;
				}
				
				Integer index = indexMap.get(key);
				Double currentAdditionalValue = 0d;

				if(key.contains(PREVIOUS_COST_KEY)){
					currentAdditionalValue = charactDetailsValue.getPreviousValue();
				} else if(key.contains(FUTURE_COST_KEY)){
					currentAdditionalValue = charactDetailsValue.getFutureValue();
				} else if(key.contains(MINI_VALUE_KEY)){
					currentAdditionalValue = charactDetailsValue.getMini();
				} else if(key.contains(MAXI_VALUE_KEY)){
					currentAdditionalValue = charactDetailsValue.getMaxi();
				}

				if(currentAdditionalValue != null){
					computeTotals(index, totals, currentAdditionalValue);
				}
				tmp.set(index, currentAdditionalValue);
			}
			
			tmp.add(charactDetailsValue.getKeyNodeRef());
			tmp.add(nodeService.getType(charactDetailsValue.getKeyNodeRef()));
			tmp.add(nodeService.getType(charactDetailsValue.getKeyNodeRef()).getLocalName());
			tmp.add(charactDetailsValue.getLevel());

			resultsets.add(tmp);
		}
		resultsets.add(totals);

		obj.put("metadatas", metadatas);
		obj.put("resultsets", resultsets);

		return obj;

	}
	
	private static void writeMetadata(String colUnit, JSONArray metadatas, Map<String, Integer> indexMap) throws JSONException{
		for(Entry<String, Integer> entry : indexMap.entrySet()){
			JSONObject metadata = new JSONObject();
			metadata.put("colType", "Double");
			metadata.put("colIndex", entry.getValue());
			metadata.put("colName", entry.getKey());
			metadata.put("colUnit", colUnit);
			metadatas.put(metadata);
		}
	}
	
	private static Map<String, Integer> createColumnMap(Map<NodeRef, List<CharactDetailsValue>> characts, Map<String, Integer> additionalValues, AttributeExtractorService attributeExtractorService, Integer idx){
		Map<String, Integer> res = new LinkedHashMap<>();
		for(Entry<NodeRef, List<CharactDetailsValue>> currentCharact : characts.entrySet()){
			String currentCharactName = attributeExtractorService.extractPropName(currentCharact.getKey());
			res.put(currentCharactName, idx);
			idx = completeAdditionalValues(currentCharactName, res, idx, additionalValues);
			++idx;
		}
		return res;
	}
	
	private static Integer completeAdditionalValues(String charactName, Map<String, Integer> indexMap, Integer idx, Map<String, Integer> additionalValues){
		for(Entry<String, Integer> additionalValue : additionalValues.entrySet()){
			if(additionalValue.getKey().contains(charactName)){
				indexMap.put(additionalValue.getKey(), ++idx);
			}
		}
		return idx;
	}
	
	private static void computeTotals(Integer index, List<Object> totals, Double currentValue){
		if(totals.size() > index){
			totals.set(index, (Double)totals.get(index) + currentValue);
		} else {
			totals.add(index, currentValue);
		}
	}
	
	private static void fillAdditionalValuesMap(Map<String, Integer> additionalValues, CharactDetailsValue currentValue, String propName){
		if(currentValue.getPreviousValue() != null){
			additionalValues.put(propName+", "+PREVIOUS_COST_KEY, null);
		}
		if(currentValue.getFutureValue() != null){
			additionalValues.put(propName+", "+FUTURE_COST_KEY, null);
		}
		if(currentValue.getMini() != null){
			additionalValues.put(propName+", "+MINI_VALUE_KEY, null);
		}
		if(currentValue.getMaxi() != null){
			additionalValues.put(propName+", "+MAXI_VALUE_KEY, null);
		}
	}

	private static String getYAxisLabel() {
		return I18NUtil.getMessage("entity.datalist.item.details.yaxis.label");
	}

	public static void writeXLS(CharactDetails charactDetails, NodeService nodeService, AttributeExtractorService attributeExtractorService,
			OutputStream outputStream) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		int rownum = 0;
		int cellnum = 0;
		Row row = sheet.createRow(rownum++);
		XSSFCellStyle style = workbook.createCellStyle();

		XSSFColor green = new XSSFColor(new java.awt.Color(0, 102, 0));

		style.setFillForegroundColor(green);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.WHITE.index);
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
				
		Map<String, Integer> additionalValues = new LinkedHashMap<String, Integer>();
		List<CharactDetailsValue> compEls = new LinkedList<>();

		for (Map.Entry<NodeRef,List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
			for (CharactDetailsValue value : entry.getValue()) {
				String propName = attributeExtractorService.extractPropName(entry.getKey());
				if(!compEls.contains(value)){
					value.setName(propName);
					compEls.add(value);
				}
				
				fillAdditionalValuesMap(additionalValues, value, propName);
			}
		}
		
		Map<String, Integer> indexMap = createColumnMap(charactDetails.getData(), additionalValues, attributeExtractorService, cellnum);
		//add sorted headers 
		for(Entry<String, Integer> entry : indexMap.entrySet()){
			cell = row.createCell(entry.getValue());
			cell.setCellValue(entry.getKey());
			cell.setCellStyle(style);
		}
		
		for (CharactDetailsValue charactDetailsValue : compEls) {
			cellnum = 0;
			String prefix = "";
			if(charactDetailsValue.getLevel()>0){
				prefix = "└";
				for(int i = 0;i< charactDetailsValue.getLevel();i++){
					prefix += "──";
				}
				prefix += ">";
			}
						
			row = sheet.createRow(rownum++);
			cell = row.createCell(cellnum++);
			cell.setCellValue(prefix+attributeExtractorService.extractPropName(charactDetailsValue.getKeyNodeRef()));
						
			//product type cell
			cell = row.createCell(cellnum++);
			String type = nodeService.getType(charactDetailsValue.getKeyNodeRef()).getLocalName();
			String typeTitle = I18NUtil.getMessage("bcpg_bcpgmodel.type.bcpg_"+type+".title");
			cell.setCellValue(typeTitle);
			
			//level depth cell
			cell = row.createCell(cellnum++);
			cell.setCellValue(charactDetailsValue.getLevel());
			
			String currentDetailsName = charactDetailsValue.getName();
			
			//set charact value to cell
			for (Map.Entry<NodeRef,List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				
				Integer currentIndex = indexMap.get(currentDetailsName);
							
				if (entry.getValue().contains(charactDetailsValue)) {
					
					cell = row.createCell(currentIndex);
					Double value = 	entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getValue();
					cell.setCellValue(value);
				}
			}			
			
			//put additional characts to cells
			for(Entry<String, Integer> entry : additionalValues.entrySet()){
				
				String key = entry.getKey();
				
				if(!key.contains(currentDetailsName)){
					continue;
				}
				
				Integer index = indexMap.get(key);
				Double currentAdditionalValue = 0d;

				if(key.contains(PREVIOUS_COST_KEY)){
					currentAdditionalValue = charactDetailsValue.getPreviousValue();
				} else if(key.contains(FUTURE_COST_KEY)){
					currentAdditionalValue = charactDetailsValue.getFutureValue();
				} else if(key.contains(MINI_VALUE_KEY)){
					currentAdditionalValue = charactDetailsValue.getMini();
				} else if(key.contains(MAXI_VALUE_KEY)){
					currentAdditionalValue = charactDetailsValue.getMaxi();
				}
				
				cell = row.createCell(index);
				cell.setCellValue(currentAdditionalValue);
			}
		}
		
		workbook.write(outputStream);
		
	}

}
