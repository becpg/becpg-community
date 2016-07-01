/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	public static JSONObject toJSONObject(final CharactDetails charactDetails, final NodeService nodeService, final AttributeExtractorService attributeExtractorService) throws JSONException {

		JSONObject obj = new JSONObject();

		JSONArray metadatas = new JSONArray();
		JSONObject metadata = new JSONObject();
		metadata.put("colIndex", 0);
		metadata.put("colType", "String");
		metadata.put("colName", getYAxisLabel());
		metadatas.put(metadata);

		List<CharactDetailsValue> compEls = new LinkedList<>();

		int idx = 0;
		for (Map.Entry<NodeRef, List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
			metadata = new JSONObject();
			metadata.put("colIndex", idx++);
			metadata.put("colType", "Double");
			metadata.put("colName", attributeExtractorService.extractPropName(entry.getKey()));
			String colUnit = "";

			for (CharactDetailsValue value : entry.getValue()) {
				if(!compEls.contains(value)){
					compEls.add(value);
				}
				colUnit = value.getUnit();
			}
			
			metadata.put("colUnit", colUnit);
			metadatas.put(metadata);
		}

		// Entity nut 1, nut2, nut3

		List<List<Object>> resultsets = new LinkedList<>();
		List<Object> totals = new LinkedList<>();
		totals.add(I18NUtil.getMessage("entity.datalist.item.details.totals"));
		for (CharactDetailsValue charactDetailsValue : compEls) {
			List<Object> tmp = new ArrayList<>();
			tmp.add(attributeExtractorService.extractPropName(charactDetailsValue.getKeyNodeRef()));
			for (Map.Entry<NodeRef,List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				Double total = 0d;

				if (totals.size() > tmp.size()) {
					total = (Double) totals.get(tmp.size());
				} else {
					totals.add(0d);
				}

				if (entry.getValue().contains(charactDetailsValue)) {
					Double value = entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getValue();
					
					tmp.add(value);
					if(entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getLevel()==0){
						total += value!=null ? value : 0d;
					}
				} else {
					tmp.add(0d);
				}
				
				totals.set(tmp.size()-1,total);
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
		List<CharactDetailsValue> compEls = new LinkedList<>();

		for (Map.Entry<NodeRef,List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {

			String colUnit = "";
			for (CharactDetailsValue value : entry.getValue()) {
				if(!compEls.contains(value)){
					compEls.add(value);
				}
				colUnit = value.getUnit();
			}
			cell = row.createCell(cellnum++);
			cell.setCellValue(attributeExtractorService.extractPropName(entry.getKey())+" ("+colUnit+")");
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
			for (Map.Entry<NodeRef,List<CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				cell = row.createCell(cellnum++);
				if (entry.getValue().contains(charactDetailsValue) && entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)) != null
						&& entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getValue()!=null) {
					cell.setCellValue(entry.getValue().get(entry.getValue().indexOf(charactDetailsValue)).getValue());
				}
			}
		}

		
		
		workbook.write(outputStream);
		
	}

}
