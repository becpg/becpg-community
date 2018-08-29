package fr.becpg.repo.jscript.app;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.CustomResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Return current state of Annotation module
 * @author quere
 *
 */
public class AnnotationCustomResponse implements CustomResponse{
	
	private static Log logger = LogFactory.getLog(AnnotationCustomResponse.class);
	private String annotationAuthorization;
	
	public void setAnnotationAuthorization(String annotationAuthorization) {
		this.annotationAuthorization = annotationAuthorization;
	}

	@Override
	public Serializable populate() {
		boolean isEnabled = false;
		if(annotationAuthorization != null && !annotationAuthorization.isEmpty()){
			isEnabled = true;
		}
		logger.debug("annotationAuthorization " + annotationAuthorization + " isEnabled: " + isEnabled);
		Map<String, Serializable> jsonObj = new LinkedHashMap<String, Serializable>(1);
		jsonObj.put("enabled", isEnabled);
		return (Serializable)jsonObj;
	}

}
