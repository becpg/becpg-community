package fr.becpg.repo.form;

import java.io.InputStream;
import java.util.Map;

import fr.becpg.repo.form.impl.BecpgFormDefinition;

/**
 * <p>FormParser interface.</p>
 *
 * @author matthieu
 */
public interface FormParser {

	/** Constant <code>PROP_FORCE="force"</code> */
	public static final String PROP_FORCE =  "force";
	/** Constant <code>PROP_READONLY="readOnly"</code> */
	public static final String PROP_READONLY = "readOnly";
	
	/**
	 * <p>visitConfig.</p>
	 *
	 * @param definitions a {@link java.util.Map} object
	 * @param in a {@link java.io.InputStream} object
	 * @throws java.lang.Exception if any.
	 */
	void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, InputStream in) throws  Exception;
	/**
	 * <p>visitConfig.</p>
	 *
	 * @param definitions a {@link java.util.Map} object
	 * @param in a {@link java.lang.String} object
	 * @throws java.lang.Exception if any.
	 */
	void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, String in) throws  Exception;

}
