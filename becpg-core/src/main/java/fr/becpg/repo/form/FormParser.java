package fr.becpg.repo.form;

import java.io.InputStream;
import java.util.Map;

import fr.becpg.repo.form.impl.BecpgFormDefinition;

public interface FormParser {

	public static final String PROP_FORCE =  "force";
	public static final String PROP_READONLY = "readonly";
	
	void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, InputStream in) throws  Exception;
	void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, String in) throws  Exception;

}
