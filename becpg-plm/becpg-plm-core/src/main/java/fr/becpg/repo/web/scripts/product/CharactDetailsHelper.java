/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.csv.writer.CSVConfig;
import org.apache.commons.csv.writer.CSVField;
import org.apache.commons.csv.writer.CSVWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.PropertyFormats;
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

		SortedSet<NodeRef> compEls = new TreeSet<>(new Comparator<NodeRef>() {
			@Override
			public int compare(NodeRef o1, NodeRef o2) {
				return attributeExtractorService.extractPropName(o1).compareTo(attributeExtractorService.extractPropName(o2));
			}
		});

		int idx = 0;
		for (Map.Entry<NodeRef, Map<NodeRef, CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
			metadata = new JSONObject();
			metadata.put("colIndex", idx++);
			metadata.put("colType", "Double");
			metadata.put("colName", attributeExtractorService.extractPropName(entry.getKey()));
			String colUnit = "";

			for (Map.Entry<NodeRef, CharactDetailsValue> value : entry.getValue().entrySet()) {
				compEls.add(value.getKey());
				colUnit = value.getValue().getUnit();
			}
			
			metadata.put("colUnit", colUnit);
			metadatas.put(metadata);
		}

		// Entity nut 1, nut2, nut3

		List<List<Object>> resultsets = new LinkedList<>();
		List<Object> totals = new LinkedList<>();
		totals.add(I18NUtil.getMessage("entity.datalist.item.details.totals"));
		for (NodeRef compoEl : compEls) {
			List<Object> tmp = new ArrayList<>();
			tmp.add(attributeExtractorService.extractPropName(compoEl));
			for (Map.Entry<NodeRef, Map<NodeRef, CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				Double total = 0d;

				if (totals.size() > tmp.size()) {
					total = (Double) totals.get(tmp.size());
				} else {
					totals.add(0d);
				}

				if (entry.getValue().containsKey(compoEl)) {
					tmp.add(entry.getValue().get(compoEl).getValue());
					total += entry.getValue().get(compoEl).getValue()!=null ? entry.getValue().get(compoEl).getValue() : 0d;
				} else {
					tmp.add(0d);
				}
				
				totals.set(tmp.size()-1,total);
			}
			tmp.add(compoEl);
			tmp.add(nodeService.getType(compoEl));
			tmp.add(nodeService.getType(compoEl).getLocalName());
			resultsets.add(tmp);
		}
		resultsets.add(totals);
		obj.put("metadatas", metadatas);
		obj.put("resultsets", resultsets);
		return obj;

	}

	public static void writeCSV(CharactDetails charactDetails, final NodeService nodeService, final AttributeExtractorService attributeExtractorService, Writer writer) {

		PropertyFormats propertyFormats = new PropertyFormats(false);

		CSVConfig csvConfig = new CSVConfig();
		csvConfig.setDelimiter(';');
		csvConfig.setValueDelimiter('"');
		csvConfig.setIgnoreValueDelimiter(false);
		csvConfig.addField(new CSVField(getYAxisLabel()));

		SortedSet<NodeRef> compEls = new TreeSet<>(new Comparator<NodeRef>() {
			@Override
			public int compare(NodeRef o1, NodeRef o2) {
				return attributeExtractorService.extractPropName(o1).compareTo(attributeExtractorService.extractPropName(o2));
			}

		});

		Map<String, String> rowHeader = new HashMap<>();
		rowHeader.put(getYAxisLabel(), getYAxisLabel());
		for (Map.Entry<NodeRef, Map<NodeRef, CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {

			CSVField field = new CSVField(entry.getKey().toString());
			String colUnit = "";
			for (Map.Entry<NodeRef, CharactDetailsValue> value : entry.getValue().entrySet()) {
				compEls.add(value.getKey());
				colUnit = value.getValue().getUnit();
			}
			rowHeader.put(entry.getKey().toString(), attributeExtractorService.extractPropName(entry.getKey())+" ("+colUnit+")");
			csvConfig.addField(field);

		}

		CSVWriter csvWriter = new CSVWriter(csvConfig);

		csvWriter.setWriter(writer);
		csvWriter.writeRecord(rowHeader);
		

		for (NodeRef compoEl : compEls) {
			Map<String, String> tmp = new HashMap<>();
			tmp.put(getYAxisLabel(), attributeExtractorService.extractPropName(compoEl));
			for (Map.Entry<NodeRef, Map<NodeRef, CharactDetailsValue>> entry : charactDetails.getData().entrySet()) {
				if (entry.getValue().containsKey(compoEl) && entry.getValue().get(compoEl) != null) {
					tmp.put(entry.getKey().toString(), propertyFormats.formatDecimal(entry.getValue().get(compoEl).getValue()));
				} else {
					tmp.put(entry.getKey().toString(), "");
				}
			}
			csvWriter.writeRecord(tmp);
		}

	}

	private static String getYAxisLabel() {
		return I18NUtil.getMessage("entity.datalist.item.details.yaxis.label");
	}

}
