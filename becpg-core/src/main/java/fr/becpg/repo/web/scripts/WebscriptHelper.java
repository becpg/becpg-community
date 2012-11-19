package fr.becpg.repo.web.scripts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class WebscriptHelper {


	public static final String PARAM_FIELDS = "metadataFields";
	
	public static List<String> extractMetadataFields(WebScriptRequest req){
	
		String fields = req.getParameter(PARAM_FIELDS);
		List<String> metadataFields = new LinkedList<String>();
	
		if (fields != null && fields.length() > 0) {
			String[] splitted = fields.split(",");
			for (String field : splitted) {
				metadataFields.add(field.replace("_", ":"));
			}
		}
		
		return metadataFields;
	}
	
	

	public static  Map<String, Boolean> extractSortMap(String sort, NamespaceService namespaceService) {

		Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
		if (sort != null && sort.length() != 0) {
			boolean asc = true;
			int separator = sort.indexOf('|');
			if (separator != -1) {
				asc = ("true".equals(sort.substring(separator + 1)));
				sort = sort.substring(0, separator);
			}
			String column;
			if (sort.charAt(0) == '.') {
				// handle pseudo cm:content fields
				column = "@{http://www.alfresco.org/model/content/1.0}content" + sort;
			} else if (sort.indexOf(':') != -1) {
				// handle attribute field sort
				column = "@" +  QName.createQName(sort, namespaceService).toString();
			} else {
				// other sort types e.g. TYPE
				column = sort;
			}
			sortMap.put(column, asc);
		}

		return sortMap;

	}
	
}
