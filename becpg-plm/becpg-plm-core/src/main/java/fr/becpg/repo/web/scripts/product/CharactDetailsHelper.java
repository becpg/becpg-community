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
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
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
import fr.becpg.repo.product.data.CharactDetails;

/**
 * 
 * @author matthieu
 *
 */
public class CharactDetailsHelper {

	
	
	public static JSONObject toJSONObject(final CharactDetails charactDetails, final NodeService nodeService) throws JSONException {

		
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
				return ((String) nodeService.getProperty(o1, ContentModel.PROP_NAME)).compareTo((String) nodeService.getProperty(o2, ContentModel.PROP_NAME));
			}
		});
		
		int idx = 0;
		for (Map.Entry<NodeRef, Map<NodeRef, Double>> entry : charactDetails.getData().entrySet()) {
			metadata = new JSONObject();
			metadata.put("colIndex", idx++);
			metadata.put("colType", "Double");
			metadata.put("colName", nodeService.getProperty(entry.getKey(), ContentModel.PROP_NAME));
			
			metadatas.put(metadata);
			
			for (Map.Entry<NodeRef, Double> value : entry.getValue().entrySet()) {
				compEls.add(value.getKey());
			}
		}
		
		//Entity nut 1, nut2, nut3

		List<List<Object>> resultsets = new ArrayList<>();
		
		for (NodeRef compoEl : compEls) {
			List<Object> tmp = new ArrayList<>();
			tmp.add(nodeService.getProperty(compoEl, ContentModel.PROP_NAME));
			for (Map.Entry<NodeRef, Map<NodeRef, Double>> entry : charactDetails.getData().entrySet()) {
				if (entry.getValue().containsKey(compoEl)) {
						tmp.add(entry.getValue().get(compoEl));
					} else {
						tmp.add(0d);
					}
			}
			tmp.add(compoEl);
			tmp.add(nodeService.getType(compoEl));
			resultsets.add(tmp);
		}
		obj.put("metadatas", metadatas);
		obj.put("resultsets", resultsets);
		return obj;

	}

	public static void writeCSV(CharactDetails charactDetails,final NodeService nodeService, Writer writer) {
		
		 PropertyFormats propertyFormats = new PropertyFormats(false);
		
		CSVConfig csvConfig = new CSVConfig();
		csvConfig.setDelimiter(';');
		csvConfig.setValueDelimiter('"');
		csvConfig.setIgnoreValueDelimiter(false);
		csvConfig.addField(new CSVField(getYAxisLabel()));
		
		
		SortedSet<NodeRef> compEls = new TreeSet<>(new Comparator<NodeRef>() {
			@Override
			public int compare(NodeRef o1, NodeRef o2) {
				return ((String) nodeService.getProperty(o1, ContentModel.PROP_NAME)).compareTo((String) nodeService.getProperty(o2, ContentModel.PROP_NAME));
			}

		});
		
		
		for (Map.Entry<NodeRef, Map<NodeRef, Double>> entry : charactDetails.getData().entrySet()) {
			
			CSVField field = new CSVField((String) nodeService.getProperty(entry.getKey(), ContentModel.PROP_NAME));
			for (Map.Entry<NodeRef, Double> value : entry.getValue().entrySet()) {
				compEls.add(value.getKey());
			}
			
			csvConfig.addField(field);

		}
		
		CSVWriter csvWriter = new CSVWriter(csvConfig);
		
		csvWriter.setWriter(writer);
		
		for (NodeRef compoEl : compEls) {
			Map<String,String> tmp = new HashMap<>();
			tmp.put(getYAxisLabel(),(String)nodeService.getProperty(compoEl, ContentModel.PROP_NAME));
			for (Map.Entry<NodeRef, Map<NodeRef, Double>> entry : charactDetails.getData().entrySet()) {
				if (entry.getValue().containsKey(compoEl) && entry.getValue().get(compoEl) != null) {
						tmp.put((String) nodeService.getProperty(entry.getKey(), ContentModel.PROP_NAME), propertyFormats.formatDecimal(entry.getValue().get(compoEl)));
					} else {
						tmp.put((String) nodeService.getProperty(entry.getKey(), ContentModel.PROP_NAME),"");
					}
			}
			csvWriter.writeRecord(tmp);
		}
		
		
	}

	private static String getYAxisLabel() {
		return I18NUtil.getMessage("entity.datalist.item.details.yaxis.label");
	}

}
