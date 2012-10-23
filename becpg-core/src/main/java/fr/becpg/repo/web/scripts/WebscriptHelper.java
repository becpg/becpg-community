package fr.becpg.repo.web.scripts;

import java.util.LinkedList;
import java.util.List;

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
	
	
}
